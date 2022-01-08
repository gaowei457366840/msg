package com.common.msg.api.storage;

import com.common.msg.api.exception.InvalidStateStoreException;
import com.common.msg.api.util.LocalHostUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.FlushOptions;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.TableFormatConfig;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;


public class RocksDBStore<K, V>
        implements KeyValueStore<K, V> {
    private static final int TTL_NOT_USED = -1;
    private static final CompressionType COMPRESSION_TYPE = CompressionType.NO_COMPRESSION;
    private static final CompactionStyle COMPACTION_STYLE = CompactionStyle.UNIVERSAL;

    private static final long WRITE_BUFFER_SIZE = 16777216L;
    private static final long BLOCK_CACHE_SIZE = 52428800L;
    private static final long BLOCK_SIZE = 4096L;
    private static final int TTL_SECONDS = -1;
    private static final int MAX_WRITE_BUFFERS = 3;
    private static final String DB_FILE_DIR = "/alidata1/admin/$/logs/data/";
    private final String name;
    private final String parentDir;
    private final Set<KeyValueIterator> openIterators = Collections.synchronizedSet(new HashSet<>());

    File dbDir;

    private final StoreSerializer<K, V> serdes;

    private RocksDB db;

    private Options options;

    private WriteOptions wOptions;

    private FlushOptions fOptions;

    private volatile boolean prepareForBulkload = false;
    private StoreContext internalProcessorContext;
    protected volatile boolean open = false;

    public RocksDBStore(String name, Class<K> keyClass, Class<V> valueClass) {
        this(name, "/alidata1/admin/$/logs/data/", keyClass, valueClass);
    }

    public RocksDBStore(String name, String parentDir, Class<K> keyClass, Class<V> valueClass) {
        this.name = name;
        this.parentDir = parentDir;
        this.serdes = StoreSerializer.withBuiltinTypes(keyClass, valueClass);
    }


    public void openDB(StoreContext context) {
        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        tableConfig.setBlockCacheSize(52428800L);
        tableConfig.setBlockSize(4096L);

        this.options = new Options();
        this.options.setTableFormatConfig((TableFormatConfig) tableConfig);
        this.options.setWriteBufferSize(16777216L);
        this.options.setCompressionType(COMPRESSION_TYPE);
        this.options.setCompactionStyle(COMPACTION_STYLE);
        this.options.setMaxWriteBufferNumber(3);
        this.options.setCreateIfMissing(true);
        this.options.setErrorIfExists(false);
        this.options.setInfoLogLevel(InfoLogLevel.ERROR_LEVEL);
        this.options.setLogFileTimeToRoll(86400L);
        this.options.setKeepLogFileNum(15L);
        this.options.setMaxManifestFileSize(268435456L);


        this.options.setIncreaseParallelism(Math.max(Runtime.getRuntime().availableProcessors(), 2));

        if (this.prepareForBulkload) {
            this.options.prepareForBulkLoad();
        }

        this.wOptions = new WriteOptions();
        this.wOptions.setDisableWAL(false);

        this.fOptions = new FlushOptions();
        this.fOptions.setWaitForFlush(true);


        this.dbDir = new File(this.parentDir.replace("$", this.name), LocalHostUtil.getHostName());

        try {
            this.db = openDB(this.dbDir, this.options, -1);
        } catch (IOException e) {
            throw new InvalidStateStoreException(e);
        }

        this.open = true;
    }


    public void init(StoreContext context) {
        this.internalProcessorContext = context;
        openDB(context);
    }


    private RocksDB openDB(File dir, Options options, int ttl) throws IOException {
        try {
            if (ttl == -1) {
                Files.createDirectories(dir.getParentFile().toPath(), (FileAttribute<?>[]) new FileAttribute[0]);
                return RocksDB.open(options, dir.getAbsolutePath());
            }
            throw new UnsupportedOperationException("Change log is not supported for store " + this.name + " since it is TTL based.");


        } catch (RocksDBException e) {
            throw new InvalidStateStoreException("Error opening store " + this.name + " at location " + dir.toString(), e);
        }
    }


    boolean isPrepareForBulkload() {
        return this.prepareForBulkload;
    }


    public String name() {
        return this.name;
    }


    public boolean persistent() {

        return true;
    }


    public boolean isOpen() {

        return this.open;
    }


    public synchronized V get(K key) {
        validateStoreOpen();
        byte[] byteValue = getInternal(this.serdes.rawKey(key));
        if (byteValue == null) {
            return null;
        }
        return this.serdes.valueFrom(byteValue);
    }


    private void validateStoreOpen() {
        if (!this.open) {
            throw new InvalidStateStoreException("Store " + this.name + " is currently closed");
        }
    }

    private byte[] getInternal(byte[] rawKey) {
        try {
            return this.db.get(rawKey);
        } catch (RocksDBException e) {
            throw new InvalidStateStoreException("Error while getting value for key " + this.serdes.keyFrom(rawKey) + " from store " + this.name, e);
        }
    }


    private void toggleDbForBulkLoading(boolean prepareForBulkload) {
        if (prepareForBulkload) {


            String[] sstFileNames = this.dbDir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.matches(".*\\.sst");
                }
            });


            if (sstFileNames != null && sstFileNames.length > 0) {
                try {
                    this.db.compactRange(true, 1, 0);
                } catch (RocksDBException e) {
                    throw new InvalidStateStoreException("Error while range compacting during restoring  store " + this.name, e);
                }


                close();
                openDB(this.internalProcessorContext);
            }
        }

        close();
        this.prepareForBulkload = prepareForBulkload;
        openDB(this.internalProcessorContext);
    }


    public synchronized void put(K key, V value) {
        Objects.requireNonNull(key, "key cannot be null");
        validateStoreOpen();
        byte[] rawKey = this.serdes.rawKey(key);
        byte[] rawValue = this.serdes.rawValue(value);
        putInternal(rawKey, rawValue);
    }


    public synchronized V putIfAbsent(K key, V value) {
        Objects.requireNonNull(key, "key cannot be null");
        V originalValue = get(key);
        if (originalValue == null) {
            put(key, value);
        }
        return originalValue;
    }

    private void restoreAllInternal(Collection<KeyValue<byte[], byte[]>> records) {
        try (WriteBatch batch = new WriteBatch()) {
            for (KeyValue<byte[], byte[]> record : records) {
                if (record.value == null) {
                    batch.delete((byte[]) record.key);
                    continue;
                }
                batch.put((byte[]) record.key, (byte[]) record.value);
            }

            this.db.write(this.wOptions, batch);
        } catch (RocksDBException e) {
            throw new InvalidStateStoreException("Error restoring batch to store " + this.name, e);
        }
    }

    private void putInternal(byte[] rawKey, byte[] rawValue) {
        if (rawValue == null) {
            try {
                this.db.delete(this.wOptions, rawKey);
            } catch (RocksDBException e) {
                throw new InvalidStateStoreException("Error while removing key " + this.serdes.keyFrom(rawKey) + " from store " + this.name, e);
            }
        } else {

            try {

                this.db.put(this.wOptions, rawKey, rawValue);
            } catch (RocksDBException e) {
                throw new InvalidStateStoreException("Error while executing put key " + this.serdes.keyFrom(rawKey) + " and value " + this.serdes.keyFrom(rawValue) + " from store " + this.name, e);
            }
        }
    }


    public void putAll(List<KeyValue<K, V>> entries) {
        try (WriteBatch batch = new WriteBatch()) {
            for (KeyValue<K, V> entry : entries) {
                Objects.requireNonNull(entry.key, "key cannot be null");
                byte[] rawKey = this.serdes.rawKey(entry.key);
                if (entry.value == null) {
                    batch.delete(rawKey);
                    continue;
                }
                byte[] value = this.serdes.rawValue(entry.value);
                batch.put(rawKey, value);
            }

            this.db.write(this.wOptions, batch);
        } catch (RocksDBException e) {
            throw new InvalidStateStoreException("Error while batch writing to store " + this.name, e);
        }
    }


    public synchronized V delete(K key) {
        Objects.requireNonNull(key, "key cannot be null");
        V value = get(key);
        put(key, null);
        return value;
    }


    public synchronized KeyValueIterator<K, V> range(K from, K to) {

        Objects.requireNonNull(from, "from cannot be null");

        Objects.requireNonNull(to, "to cannot be null");

        validateStoreOpen();


        RocksDBRangeIterator rocksDBRangeIterator = new RocksDBRangeIterator(this.name, this.db.newIterator(), this.serdes, from, to);

        this.openIterators.add(rocksDBRangeIterator);


        return rocksDBRangeIterator;
    }


    public synchronized KeyValueIterator<K, V> all() {

        validateStoreOpen();


        RocksIterator innerIter = this.db.newIterator();

        innerIter.seekToFirst();

        RocksDbIterator rocksDbIterator = new RocksDbIterator(this.name, innerIter, this.serdes);

        this.openIterators.add(rocksDbIterator);

        return rocksDbIterator;
    }

    public synchronized KeyValue<K, V> first() {

        validateStoreOpen();


        RocksIterator innerIter = this.db.newIterator();

        innerIter.seekToFirst();

        KeyValue<K, V> pair = new KeyValue<>(this.serdes.keyFrom(innerIter.key()), this.serdes.valueFrom(innerIter.value()));

        innerIter.close();


        return pair;
    }

    public synchronized KeyValue<K, V> last() {

        validateStoreOpen();


        RocksIterator innerIter = this.db.newIterator();

        innerIter.seekToLast();

        KeyValue<K, V> pair = new KeyValue<>(this.serdes.keyFrom(innerIter.key()), this.serdes.valueFrom(innerIter.value()));

        innerIter.close();


        return pair;
    }


    public long approximateNumEntries() {
        long value;

        validateStoreOpen();

        try {

            value = this.db.getLongProperty("rocksdb.estimate-num-keys");

        } catch (RocksDBException e) {

            throw new InvalidStateStoreException("Error fetching property from store " + this.name, e);
        }

        if (isOverflowing(value)) {

            return Long.MAX_VALUE;
        }

        return value;
    }


    private boolean isOverflowing(long value) {

        return (value < 0L);
    }


    public synchronized void flush() {

        if (this.db == null) {
            return;
        }


        flushInternal();
    }


    private void flushInternal() {
        try {

            this.db.flush(this.fOptions);

        } catch (RocksDBException e) {

            throw new InvalidStateStoreException("Error while executing flush from store " + this.name, e);
        }
    }


    public synchronized void close() {

        if (!this.open) {
            return;
        }


        this.open = false;

        closeOpenIterators();

        this.options.close();

        this.wOptions.close();

        this.fOptions.close();

        this.db.close();


        this.options = null;

        this.wOptions = null;

        this.fOptions = null;

        this.db = null;
    }

    private void closeOpenIterators() {
        HashSet<KeyValueIterator> iterators;

        synchronized (this.openIterators) {

            iterators = new HashSet<>(this.openIterators);
        }

        for (KeyValueIterator iterator : iterators)

            iterator.close();
    }

    private class RocksDbIterator
            implements KeyValueIterator<K, V> {
        private final String storeName;
        private final RocksIterator iter;
        private final StoreSerializer<K, V> serdes;
        private volatile boolean open = true;

        RocksDbIterator(String storeName, RocksIterator iter, StoreSerializer<K, V> serdes) {

            this.iter = iter;

            this.serdes = serdes;

            this.storeName = storeName;
        }

        byte[] peekRawKey() {

            return this.iter.key();
        }

        private KeyValue<K, V> getKeyValue() {

            return new KeyValue<>(this.serdes.keyFrom(this.iter.key()), this.serdes.valueFrom(this.iter.value()));
        }


        public synchronized boolean hasNext() {

            if (!this.open) {

                throw new InvalidStateStoreException(String.format("RocksDB store %s has closed", new Object[]{this.storeName}));
            }


            return this.iter.isValid();
        }


        public synchronized KeyValue<K, V> next() {

            if (!hasNext()) {

                throw new NoSuchElementException();
            }

            KeyValue<K, V> entry = getKeyValue();

            this.iter.next();

            return entry;
        }


        public void remove() {

            throw new UnsupportedOperationException("RocksDB iterator does not support remove()");
        }


        public synchronized void close() {

            RocksDBStore.this.openIterators.remove(this);

            this.iter.close();

            this.open = false;
        }


        public K peekNextKey() {

            if (!hasNext()) {

                throw new NoSuchElementException();
            }

            return this.serdes.keyFrom(this.iter.key());
        }
    }


    private class RocksDBRangeIterator
            extends RocksDbIterator {
        private final Comparator<byte[]> comparator = RocksDBStore.BYTES_LEXICO_COMPARATOR;
        private byte[] rawToKey;

        RocksDBRangeIterator(String storeName, RocksIterator iter, StoreSerializer<K, V> serdes, K from, K to) {

            super(storeName, iter, serdes);

            iter.seek(serdes.rawKey(from));

            this.rawToKey = serdes.rawKey(to);

            if (this.rawToKey == null) {

                throw new NullPointerException("RocksDBRangeIterator: RawToKey is null for key " + to);
            }
        }


        public synchronized boolean hasNext() {

            return (super.hasNext() && this.comparator.compare(peekRawKey(), this.rawToKey) <= 0);
        }
    }


    public static final ByteArrayComparator BYTES_LEXICO_COMPARATOR = new LexicographicByteArrayComparator();

    public static interface ByteArrayComparator
            extends Comparator<byte[]>, Serializable {
        int compare(byte[] param1ArrayOfbyte1, int param1Int1, int param1Int2, byte[] param1ArrayOfbyte2, int param1Int3, int param1Int4);
    }

    private static class LexicographicByteArrayComparator implements ByteArrayComparator {
        private LexicographicByteArrayComparator() {
        }

        public int compare(byte[] buffer1, byte[] buffer2) {

            return compare(buffer1, 0, buffer1.length, buffer2, 0, buffer2.length);
        }


        public int compare(byte[] buffer1, int offset1, int length1, byte[] buffer2, int offset2, int length2) {

            if (buffer1 == buffer2 && offset1 == offset2 && length1 == length2) {


                return 0;
            }


            int end1 = offset1 + length1;

            int end2 = offset2 + length2;

            for (int i = offset1, j = offset2; i < end1 && j < end2; i++, j++) {

                int a = buffer1[i] & 0xFF;

                int b = buffer2[j] & 0xFF;

                if (a != b) {

                    return a - b;
                }
            }

            return length1 - length2;
        }
    }
}


