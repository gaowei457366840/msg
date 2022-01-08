package com.common.msg.kafka.network;

import com.google.protobuf.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public final class MessageProtobuf {
    public static void registerAllExtensions(ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(ExtensionRegistry registry) {
        registerAllExtensions((ExtensionRegistryLite) registry);
    }


    public static final class Message
            extends GeneratedMessageV3
            implements MessageOrBuilder {
        private static final long serialVersionUID = 0L;


        public static final int VERSION_FIELD_NUMBER = 1;


        private volatile Object version_;


        public static final int REQUESTID_FIELD_NUMBER = 2;


        private long requestId_;


        public static final int TYPE_FIELD_NUMBER = 3;


        private volatile Object type_;


        public static final int BODY_FIELD_NUMBER = 4;


        private ByteString body_;


        private byte memoizedIsInitialized;


        private Message(GeneratedMessageV3.Builder<?> builder) {
            super(builder);

            this.memoizedIsInitialized = -1;
        }

        private Message() {
            this.memoizedIsInitialized = -1;
            this.version_ = "";
            this.requestId_ = 0L;
            this.type_ = "";
            this.body_ = ByteString.EMPTY;
        }

        public final UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private Message(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            this();
            if (extensionRegistry == null) throw new NullPointerException();
            int mutable_bitField0_ = 0;
            UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    String s;
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            continue;
                        case 10:
                            s = input.readStringRequireUtf8();
                            this.version_ = s;
                            continue;
                        case 16:
                            this.requestId_ = input.readInt64();
                            continue;
                        case 26:
                            s = input.readStringRequireUtf8();
                            this.type_ = s;
                            continue;
                        case 34:
                            this.body_ = input.readBytes();
                            continue;
                    }
                    if (!parseUnknownFieldProto3(input, unknownFields, extensionRegistry, tag)) done = true;
                }
            } catch (InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (IOException e) {
                throw (new InvalidProtocolBufferException(e)).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return MessageProtobuf.internal_static_Message_descriptor;
        }

        protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return MessageProtobuf.internal_static_Message_fieldAccessorTable.ensureFieldAccessorsInitialized(Message.class, Builder.class);
        }

        public String getVersion() {
            Object ref = this.version_;
            if (ref instanceof String) return (String) ref;
            ByteString bs = (ByteString) ref;
            String s = bs.toStringUtf8();
            this.version_ = s;
            return s;
        }

        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) return true;
            if (isInitialized == 0) return false;

            this.memoizedIsInitialized = 1;
            return true;
        }

        public ByteString getVersionBytes() {
            Object ref = this.version_;
            if (ref instanceof String) {
                ByteString b = ByteString.copyFromUtf8((String) ref);
                this.version_ = b;
                return b;
            }
            return (ByteString) ref;
        }

        public long getRequestId() {
            return this.requestId_;
        }

        public String getType() {
            Object ref = this.type_;
            if (ref instanceof String)
                return (String) ref;
            ByteString bs = (ByteString) ref;
            String s = bs.toStringUtf8();
            this.type_ = s;
            return s;
        }

        public ByteString getTypeBytes() {
            Object ref = this.type_;
            if (ref instanceof String) {
                ByteString b = ByteString.copyFromUtf8((String) ref);
                this.type_ = b;
                return b;
            }
            return (ByteString) ref;
        }

        public ByteString getBody() {
            return this.body_;
        }

        public void writeTo(CodedOutputStream output) throws IOException, IOException {
            if (!getVersionBytes().isEmpty()) {
                GeneratedMessageV3.writeString(output, 1, this.version_);
            }
            if (this.requestId_ != 0L) {
                output.writeInt64(2, this.requestId_);
            }
            if (!getTypeBytes().isEmpty()) {
                GeneratedMessageV3.writeString(output, 3, this.type_);
            }
            if (!this.body_.isEmpty()) {
                output.writeBytes(4, this.body_);
            }
            this.unknownFields.writeTo(output);
        }


        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) return size;

            size = 0;
            if (!getVersionBytes().isEmpty()) {
                size += GeneratedMessageV3.computeStringSize(1, this.version_);
            }
            if (this.requestId_ != 0L) {
                size +=
                        CodedOutputStream.computeInt64Size(2, this.requestId_);
            }
            if (!getTypeBytes().isEmpty()) {
                size += GeneratedMessageV3.computeStringSize(3, this.type_);
            }
            if (!this.body_.isEmpty()) {
                size +=
                        CodedOutputStream.computeBytesSize(4, this.body_);
            }
            size += this.unknownFields.getSerializedSize();
            this.memoizedSize = size;
            return size;
        }


        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Message)) {
                return super.equals(obj);
            }
            Message other = (Message) obj;

            boolean result = true;

            result = (result && getVersion().equals(other.getVersion()));

            result = (result && getRequestId() == other.getRequestId());

            result = (result && getType().equals(other.getType()));

            result = (result && getBody().equals(other.getBody()));
            result = (result && this.unknownFields.equals(other.unknownFields));
            return result;
        }


        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + getDescriptor().hashCode();
            hash = 37 * hash + 1;
            hash = 53 * hash + getVersion().hashCode();
            hash = 37 * hash + 2;
            hash = 53 * hash + Internal.hashLong(
                    getRequestId());
            hash = 37 * hash + 3;
            hash = 53 * hash + getType().hashCode();
            hash = 37 * hash + 4;
            hash = 53 * hash + getBody().hashCode();
            hash = 29 * hash + this.unknownFields.hashCode();
            this.memoizedHashCode = hash;
            return hash;
        }


        public static Message parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return (Message) PARSER.parseFrom(data);
        }


        public static Message parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (Message) PARSER.parseFrom(data, extensionRegistry);
        }


        public static Message parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (Message) PARSER.parseFrom(data);
        }


        public static Message parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (Message) PARSER.parseFrom(data, extensionRegistry);
        }

        public static Message parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (Message) PARSER.parseFrom(data);
        }


        public static Message parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (Message) PARSER.parseFrom(data, extensionRegistry);
        }

        public static Message parseFrom(InputStream input) throws IOException {
            return
                    (Message) GeneratedMessageV3.parseWithIOException(PARSER, input);
        }


        public static Message parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return
                    (Message) GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Message parseDelimitedFrom(InputStream input) throws IOException {
            return
                    (Message) GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
        }


        public static Message parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return
                    (Message) GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }


        public static Message parseFrom(CodedInputStream input) throws IOException {
            return
                    (Message) GeneratedMessageV3.parseWithIOException(PARSER, input);
        }


        public static Message parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return
                    (Message) GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Message prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return (this == DEFAULT_INSTANCE) ? new Builder() : (new Builder())
                    .mergeFrom(this);
        }


        protected Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder
                extends GeneratedMessageV3.Builder<Builder> implements MessageOrBuilder {
            private Object version_;
            private long requestId_;
            private Object type_;
            private ByteString body_;

            public static final Descriptors.Descriptor getDescriptor() {
                return MessageProtobuf.internal_static_Message_descriptor;
            }


            protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
                return MessageProtobuf.internal_static_Message_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(Message.class, Builder.class);
            }


            private Builder() {
                this.version_ = "";


                this.type_ = "";


                this.body_ = ByteString.EMPTY;
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (Message.alwaysUseFieldBuilders) ;
            }

            public Builder clear() {
                super.clear();
                this.version_ = "";
                this.requestId_ = 0L;
                this.type_ = "";
                this.body_ = ByteString.EMPTY;
                return this;
            }

            public Descriptors.Descriptor getDescriptorForType() {
                return MessageProtobuf.internal_static_Message_descriptor;
            }

            public Message getDefaultInstanceForType() {
                return Message.getDefaultInstance();
            }

            public Message build() {
                Message result = buildPartial();
                if (!result.isInitialized()) throw newUninitializedMessageException(result);
                return result;
            }

            public Message buildPartial() {
                Message result = new Message(this);
                result.version_ = this.version_;
                result.requestId_ = this.requestId_;
                result.type_ = this.type_;
                result.body_ = this.body_;
                onBuilt();
                return result;
            }

            public Builder clone() {
                return (Builder) super.clone();
            }

            private Builder(GeneratedMessageV3.BuilderParent parent) {
                super(parent);
                this.version_ = "";
                this.type_ = "";
                this.body_ = ByteString.EMPTY;
                maybeForceBuilderInitialization();
            }

            public Builder setField(Descriptors.FieldDescriptor field, Object value) {
                return (Builder) super.setField(field, value);
            }

            public Builder clearField(Descriptors.FieldDescriptor field) {
                return (Builder) super.clearField(field);
            }

            public Builder clearOneof(Descriptors.OneofDescriptor oneof) {
                return (Builder) super.clearOneof(oneof);
            }

            public Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
                return (Builder) super.setRepeatedField(field, index, value);
            }

            public Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
                return (Builder) super.addRepeatedField(field, value);
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof Message) return mergeFrom((Message) other);
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(Message other) {
                if (other == Message.getDefaultInstance()) return this;
                if (!other.getVersion().isEmpty()) {
                    this.version_ = other.version_;
                    onChanged();
                }
                if (other.getRequestId() != 0L) setRequestId(other.getRequestId());
                if (!other.getType().isEmpty()) {
                    this.type_ = other.type_;
                    onChanged();
                }
                if (other.getBody() != ByteString.EMPTY) setBody(other.getBody());
                mergeUnknownFields(other.unknownFields);
                onChanged();
                return this;
            }

            public ByteString getBody() {
                return this.body_;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                Message parsedMessage = null;
                try {
                    parsedMessage = (Message) Message.PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (InvalidProtocolBufferException e) {
                    parsedMessage = (Message) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) mergeFrom(parsedMessage);
                }
                return this;
            }

            public String getVersion() {
                Object ref = this.version_;
                if (!(ref instanceof String)) {
                    ByteString bs = (ByteString) ref;
                    String s = bs.toStringUtf8();
                    this.version_ = s;
                    return s;
                }
                return (String) ref;
            }

            public ByteString getVersionBytes() {
                Object ref = this.version_;
                if (ref instanceof String) {
                    ByteString b = ByteString.copyFromUtf8((String) ref);
                    this.version_ = b;
                    return b;
                }
                return (ByteString) ref;
            }

            public Builder setVersion(String value) {
                if (value == null) throw new NullPointerException();
                this.version_ = value;
                onChanged();
                return this;
            }

            public Builder clearVersion() {
                this.version_ = Message.getDefaultInstance().getVersion();
                onChanged();
                return this;
            }

            public Builder setVersionBytes(ByteString value) {
                if (value == null) throw new NullPointerException();
                Message.checkByteStringIsUtf8(value);
                this.version_ = value;
                onChanged();
                return this;
            }

            public Builder setBody(ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }

                this.body_ = value;
                onChanged();
                return this;
            }

            public long getRequestId() {
                return this.requestId_;
            }

            public Builder setRequestId(long value) {
                this.requestId_ = value;
                onChanged();
                return this;
            }

            public Builder clearRequestId() {
                this.requestId_ = 0L;
                onChanged();
                return this;
            }

            public String getType() {
                Object ref = this.type_;
                if (!(ref instanceof String)) {
                    ByteString bs = (ByteString) ref;
                    String s = bs.toStringUtf8();
                    this.type_ = s;
                    return s;
                }
                return (String) ref;
            }

            public ByteString getTypeBytes() {
                Object ref = this.type_;
                if (ref instanceof String) {
                    ByteString b = ByteString.copyFromUtf8((String) ref);
                    this.type_ = b;
                    return b;
                }
                return (ByteString) ref;
            }

            public Builder setType(String value) {
                if (value == null) throw new NullPointerException();
                this.type_ = value;
                onChanged();
                return this;
            }

            public Builder clearType() {
                this.type_ = Message.getDefaultInstance().getType();
                onChanged();
                return this;
            }

            public Builder setTypeBytes(ByteString value) {
                if (value == null) throw new NullPointerException();
                Message.checkByteStringIsUtf8(value);
                this.type_ = value;
                onChanged();
                return this;
            }

            public Builder clearBody() {
                this.body_ = Message.getDefaultInstance().getBody();
                onChanged();
                return this;
            }


            public final Builder setUnknownFields(UnknownFieldSet unknownFields) {
                return (Builder) setUnknownFieldsProto3(unknownFields);
            }


            public final Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
                return (Builder) super.mergeUnknownFields(unknownFields);
            }
        }


        private static final Message DEFAULT_INSTANCE = new Message();


        public static Message getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }


        private static final Parser<Message> PARSER = (Parser<Message>) new AbstractParser<Message>() {


            public Message parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                return new Message(input, extensionRegistry);
            }
        };

        public static Parser<Message> parser() {
            return PARSER;
        }


        public Parser<Message> getParserForType() {
            return PARSER;
        }


        public Message getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }
    }


    public static Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }


    static {
        String[] descriptorData = {"\n\025MessageProtobuf.proto\"I\n\007Message\022\017\n\007version\030\001 \001(\t\022\021\n\trequestId\030\002 \001(\003\022\f\n\004type\030\003 \001(\t\022\f\n\004body\030\004 \001(\fB \n\036com.common.msg.client.networkb\006proto3"};


        Descriptors.FileDescriptor.InternalDescriptorAssigner assigner = new Descriptors.FileDescriptor.InternalDescriptorAssigner() {
            public ExtensionRegistry assignDescriptors(Descriptors.FileDescriptor root) {
                MessageProtobuf.descriptor = root;
                return null;
            }
        };

        Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new Descriptors.FileDescriptor[0], assigner);
    }

    public static interface MessageOrBuilder extends com.google.protobuf.MessageOrBuilder {
        String getVersion();

        ByteString getVersionBytes();

        long getRequestId();

        String getType();

        ByteString getTypeBytes();

        ByteString getBody();
    }

    private static final Descriptors.Descriptor internal_static_Message_descriptor = getDescriptor().getMessageTypes().get(0);
    private static final GeneratedMessageV3.FieldAccessorTable internal_static_Message_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(internal_static_Message_descriptor, new String[]{"Version", "RequestId", "Type", "Body"});
    private static Descriptors.FileDescriptor descriptor;
}


