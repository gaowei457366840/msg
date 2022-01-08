package com.common.msg.kafka.network;

import com.common.msg.api.util.MillisecondClock;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.kafka.core.LinkedArray;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class BrokerMetadataManager {
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BrokerMetadataManager)) return false;
        BrokerMetadataManager other = (BrokerMetadataManager) o;
        return !!other.canEqual(this);
    }

    protected boolean canEqual(Object other) {
        return other instanceof BrokerMetadataManager;
    }

    public int hashCode() {
        return 1;
    }

    public String toString() {
        return "BrokerMetadataManager()";
    }


    private static Map<InetSocketAddress, BrokerMetadata> metadataMap = new ConcurrentHashMap<>();

    private static Integer shardingCount;

    private static String hostAddress;

    private static LinkedArray<InetSocketAddress> onlineBroker = new LinkedArray();


    public static synchronized InetSocketAddress determineNode(String destination) {
        if (StringUtil.isNullOrEmpty(destination)) {
            for (Map.Entry<InetSocketAddress, BrokerMetadata> entry : metadataMap.entrySet()) {
                BrokerMetadata metadata = entry.getValue();
                if (metadata.isActive) {
                    return entry.getKey();
                }
            }
        } else {
            int partition = Math.abs(destination.hashCode() % shardingCount.intValue());
            for (Map.Entry<InetSocketAddress, BrokerMetadata> entry : metadataMap.entrySet()) {
                BrokerMetadata metadata = entry.getValue();
                if (metadata.getPartition().contains(Integer.valueOf(partition)) &&
                        metadata.isActive) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }


    public static synchronized BrokerMetadata getActiveNode() {
        while (onlineBroker != null && !onlineBroker.isEmpty()) {
            InetSocketAddress address = (InetSocketAddress) onlineBroker.next();
            BrokerMetadata metadata = metadataMap.get(address);
            if (metadata == null) {
                throw new MqClientException("broker metadata is null. node[" + address + "]");
            }
            if (metadata.isActive &&
                    metadata.channel != null && metadata.channel.isActive()) {
                return metadata;
            }
        }

        return null;
    }

    public static synchronized BrokerMetadata getActiveNode(InetSocketAddress address) {
        BrokerMetadata metadata = metadataMap.get(address);
        if (metadata.isActive &&
                metadata.channel != null && metadata.channel.isActive()) {
            return metadata;
        }

        return null;
    }

    public static boolean isContains(InetSocketAddress address) {
        return metadataMap.containsKey(address);
    }

    public static boolean isConnected() {
        return !onlineBroker.isEmpty();
    }

    public static synchronized List<List<InetSocketAddress>> checkAndPut(List<InetSocketAddress> addresses) {
        List<InetSocketAddress> deletedAddresses = new ArrayList<>();
        List<InetSocketAddress> addedAddresses = new ArrayList<>();
        List<List<InetSocketAddress>> changedAddresses = null;


        for (Map.Entry<InetSocketAddress, BrokerMetadata> entry : metadataMap.entrySet()) {
            if (!addresses.contains(entry.getKey())) {
                deletedAddresses.add(entry.getKey());
                onlineBroker.remove(entry.getKey());
                metadataMap.remove(entry.getKey());
                if (entry.getValue() != null && ((BrokerMetadata) entry.getValue()).channel != null) {
                    ((BrokerMetadata) entry.getValue()).channel.close();
                }
            }
        }


        for (InetSocketAddress address : addresses) {
            if (!metadataMap.containsKey(address)) {
                addedAddresses.add(address);
                putIfAbsent(address, new BrokerMetadata(address, false, false, 0L));
            }
        }

        if (!deletedAddresses.isEmpty() || !addedAddresses.isEmpty()) {
            changedAddresses = new ArrayList<>();
            changedAddresses.add(0, deletedAddresses);
            changedAddresses.add(1, addedAddresses);
        }

        return changedAddresses;
    }

    public static synchronized BrokerMetadata putIfAbsent(InetSocketAddress address) {
        return putIfAbsent(address, new BrokerMetadata(address, false, false, 0L));
    }

    public static synchronized boolean needReconnection(InetSocketAddress address) {
        BrokerMetadata metadata = metadataMap.get(address);
        if (null != metadata) {
            return !metadata.isReconnection;
        }
        if (address.getAddress().getHostAddress().equals(hostAddress)) {
            return false;
        }
        throw new MqClientException("broker metadata not exist.");
    }


    public static synchronized void onReconnection(InetSocketAddress address) {
        BrokerMetadata metadata = metadataMap.get(address);
        if (null != metadata) {
            metadata.setReconnection(true);
        } else {
            throw new MqClientException("broker metadata not exist.");
        }
    }

    public static synchronized void setOffline(InetSocketAddress address) {
        BrokerMetadata metadata = metadataMap.get(address);
        if (null != metadata) {
            metadata.setActive(false);
            metadata.setOfflineTime(MillisecondClock.now());
            metadata.setChannel(null);
            onlineBroker.remove(address);
        } else {
            if (address.getAddress().getHostAddress().equals(hostAddress)) {
                return;
            }
            metadata = new BrokerMetadata(address, false, false, 0L);
            metadata.setOfflineTime(MillisecondClock.now());
            putIfAbsent(address, metadata);
        }
    }

    public static synchronized void setOnline(InetSocketAddress address, Channel channel) {
        BrokerMetadata metadata = metadataMap.get(address);
        if (null != metadata) {
            metadata.setActive(true);
            metadata.setReconnection(false);
            metadata.setOfflineTime(0L);
            metadata.setOnlineTime(MillisecondClock.now());
            metadata.setChannel(channel);
            onlineBroker.add(address);
        } else {
            metadata = new BrokerMetadata(address, false, true, MillisecondClock.now());
            metadata.setChannel(channel);
            onlineBroker.add(address);
            metadataMap.put(address, metadata);
        }
    }

    public static void setHostAddress(String address) {
        hostAddress = address;
    }

    public static synchronized Map<InetSocketAddress, BrokerMetadata> getBrokerMetadataMap() {
        return Collections.unmodifiableMap(metadataMap);
    }


    public static synchronized void clear() {
        for (Map.Entry<InetSocketAddress, BrokerMetadata> map : metadataMap.entrySet()) {
            Channel channel = ((BrokerMetadata) map.getValue()).channel;
            if (channel != null) {
                channel.close();
            }
        }
        metadataMap.clear();
        onlineBroker.clear();
    }

    private static BrokerMetadata putIfAbsent(InetSocketAddress key, BrokerMetadata value) {
        BrokerMetadata v = metadataMap.get(key);
        if (v == null) {
            v = metadataMap.put(key, value);
        }
        return v;
    }

    static class BrokerMetadata {
        final InetSocketAddress address;
        Set<Integer> partition;
        boolean isLeader;
        boolean isActive;

        public void setPartition(Set<Integer> partition) {
            this.partition = partition;
        }

        boolean isReconnection;
        long onlineTime;
        long offlineTime;
        Channel channel;

        public void setLeader(boolean isLeader) {
            this.isLeader = isLeader;
        }

        public void setActive(boolean isActive) {
            this.isActive = isActive;
        }

        public void setReconnection(boolean isReconnection) {
            this.isReconnection = isReconnection;
        }

        public void setOnlineTime(long onlineTime) {
            this.onlineTime = onlineTime;
        }

        public void setOfflineTime(long offlineTime) {
            this.offlineTime = offlineTime;
        }

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

        public String toString() {
            return "BrokerMetadataManager.BrokerMetadata(address=" + getAddress() + ", partition=" + getPartition() + ", isLeader=" + isLeader() + ", isActive=" + isActive() + ", isReconnection=" + isReconnection() + ", onlineTime=" + getOnlineTime() + ", offlineTime=" + getOfflineTime() + ", channel=" + getChannel() + ")";
        }

        public InetSocketAddress getAddress() {
            return this.address;
        }

        public Set<Integer> getPartition() {
            return this.partition;
        }

        public boolean isLeader() {
            return this.isLeader;
        }

        public boolean isActive() {
            return this.isActive;
        }

        public boolean isReconnection() {
            return this.isReconnection;
        }

        public long getOnlineTime() {
            return this.onlineTime;
        }

        public long getOfflineTime() {
            return this.offlineTime;
        }

        public Channel getChannel() {
            return this.channel;
        }

        BrokerMetadata(InetSocketAddress address, boolean isLeader, boolean isActive, long onlineTime) {
            this.address = address;
            this.isLeader = isLeader;
            this.isActive = isActive;
            this.onlineTime = onlineTime;
            this.partition = new HashSet<>();
        }


        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            BrokerMetadata metadata = (BrokerMetadata) o;

            return getAddress().equals(metadata.getAddress());
        }


        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + getAddress().hashCode();
            return result;
        }
    }
}


