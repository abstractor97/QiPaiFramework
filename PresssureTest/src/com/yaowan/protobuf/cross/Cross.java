// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cross.proto

package com.yaowan.protobuf.cross;

public final class Cross {
  private Cross() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }
  public interface CrossRoleOrBuilder
      extends com.google.protobuf.MessageLiteOrBuilder {
    
    // required int64 rid = 1;
    boolean hasRid();
    long getRid();
    
    // optional int32 gold = 2;
    boolean hasGold();
    int getGold();
    
    // optional string nick = 3;
    boolean hasNick();
    String getNick();
    
    // optional int32 head = 4;
    boolean hasHead();
    int getHead();
    
    // optional int32 level = 5;
    boolean hasLevel();
    int getLevel();
    
    // optional int32 sex = 6;
    boolean hasSex();
    int getSex();
  }
  public static final class CrossRole extends
      com.google.protobuf.GeneratedMessageLite
      implements CrossRoleOrBuilder {
    // Use CrossRole.newBuilder() to construct.
    private CrossRole(Builder builder) {
      super(builder);
    }
    private CrossRole(boolean noInit) {}
    
    private static final CrossRole defaultInstance;
    public static CrossRole getDefaultInstance() {
      return defaultInstance;
    }
    
    public CrossRole getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    private int bitField0_;
    // required int64 rid = 1;
    public static final int RID_FIELD_NUMBER = 1;
    private long rid_;
    public boolean hasRid() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public long getRid() {
      return rid_;
    }
    
    // optional int32 gold = 2;
    public static final int GOLD_FIELD_NUMBER = 2;
    private int gold_;
    public boolean hasGold() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public int getGold() {
      return gold_;
    }
    
    // optional string nick = 3;
    public static final int NICK_FIELD_NUMBER = 3;
    private java.lang.Object nick_;
    public boolean hasNick() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    public String getNick() {
      java.lang.Object ref = nick_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          nick_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getNickBytes() {
      java.lang.Object ref = nick_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        nick_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // optional int32 head = 4;
    public static final int HEAD_FIELD_NUMBER = 4;
    private int head_;
    public boolean hasHead() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    public int getHead() {
      return head_;
    }
    
    // optional int32 level = 5;
    public static final int LEVEL_FIELD_NUMBER = 5;
    private int level_;
    public boolean hasLevel() {
      return ((bitField0_ & 0x00000010) == 0x00000010);
    }
    public int getLevel() {
      return level_;
    }
    
    // optional int32 sex = 6;
    public static final int SEX_FIELD_NUMBER = 6;
    private int sex_;
    public boolean hasSex() {
      return ((bitField0_ & 0x00000020) == 0x00000020);
    }
    public int getSex() {
      return sex_;
    }
    
    private void initFields() {
      rid_ = 0L;
      gold_ = 0;
      nick_ = "";
      head_ = 0;
      level_ = 0;
      sex_ = 0;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      if (!hasRid()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeInt64(1, rid_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeInt32(2, gold_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeBytes(3, getNickBytes());
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeInt32(4, head_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        output.writeInt32(5, level_);
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        output.writeInt32(6, sex_);
      }
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(1, rid_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, gold_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(3, getNickBytes());
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(4, head_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(5, level_);
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(6, sex_);
      }
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static com.yaowan.protobuf.cross.Cross.CrossRole parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.yaowan.protobuf.cross.Cross.CrossRole parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.yaowan.protobuf.cross.Cross.CrossRole parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.yaowan.protobuf.cross.Cross.CrossRole parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.yaowan.protobuf.cross.Cross.CrossRole parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.yaowan.protobuf.cross.Cross.CrossRole parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.yaowan.protobuf.cross.Cross.CrossRole parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.yaowan.protobuf.cross.Cross.CrossRole parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.yaowan.protobuf.cross.Cross.CrossRole parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.yaowan.protobuf.cross.Cross.CrossRole parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.yaowan.protobuf.cross.Cross.CrossRole prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageLite.Builder<
          com.yaowan.protobuf.cross.Cross.CrossRole, Builder>
        implements com.yaowan.protobuf.cross.Cross.CrossRoleOrBuilder {
      // Construct using com.yaowan.protobuf.cross.Cross.CrossRole.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private void maybeForceBuilderInitialization() {
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        rid_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000001);
        gold_ = 0;
        bitField0_ = (bitField0_ & ~0x00000002);
        nick_ = "";
        bitField0_ = (bitField0_ & ~0x00000004);
        head_ = 0;
        bitField0_ = (bitField0_ & ~0x00000008);
        level_ = 0;
        bitField0_ = (bitField0_ & ~0x00000010);
        sex_ = 0;
        bitField0_ = (bitField0_ & ~0x00000020);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.yaowan.protobuf.cross.Cross.CrossRole getDefaultInstanceForType() {
        return com.yaowan.protobuf.cross.Cross.CrossRole.getDefaultInstance();
      }
      
      public com.yaowan.protobuf.cross.Cross.CrossRole build() {
        com.yaowan.protobuf.cross.Cross.CrossRole result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private com.yaowan.protobuf.cross.Cross.CrossRole buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        com.yaowan.protobuf.cross.Cross.CrossRole result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public com.yaowan.protobuf.cross.Cross.CrossRole buildPartial() {
        com.yaowan.protobuf.cross.Cross.CrossRole result = new com.yaowan.protobuf.cross.Cross.CrossRole(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.rid_ = rid_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.gold_ = gold_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.nick_ = nick_;
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000008;
        }
        result.head_ = head_;
        if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
          to_bitField0_ |= 0x00000010;
        }
        result.level_ = level_;
        if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
          to_bitField0_ |= 0x00000020;
        }
        result.sex_ = sex_;
        result.bitField0_ = to_bitField0_;
        return result;
      }
      
      public Builder mergeFrom(com.yaowan.protobuf.cross.Cross.CrossRole other) {
        if (other == com.yaowan.protobuf.cross.Cross.CrossRole.getDefaultInstance()) return this;
        if (other.hasRid()) {
          setRid(other.getRid());
        }
        if (other.hasGold()) {
          setGold(other.getGold());
        }
        if (other.hasNick()) {
          setNick(other.getNick());
        }
        if (other.hasHead()) {
          setHead(other.getHead());
        }
        if (other.hasLevel()) {
          setLevel(other.getLevel());
        }
        if (other.hasSex()) {
          setSex(other.getSex());
        }
        return this;
      }
      
      public final boolean isInitialized() {
        if (!hasRid()) {
          
          return false;
        }
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              
              return this;
            default: {
              if (!parseUnknownField(input, extensionRegistry, tag)) {
                
                return this;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              rid_ = input.readInt64();
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              gold_ = input.readInt32();
              break;
            }
            case 26: {
              bitField0_ |= 0x00000004;
              nick_ = input.readBytes();
              break;
            }
            case 32: {
              bitField0_ |= 0x00000008;
              head_ = input.readInt32();
              break;
            }
            case 40: {
              bitField0_ |= 0x00000010;
              level_ = input.readInt32();
              break;
            }
            case 48: {
              bitField0_ |= 0x00000020;
              sex_ = input.readInt32();
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // required int64 rid = 1;
      private long rid_ ;
      public boolean hasRid() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public long getRid() {
        return rid_;
      }
      public Builder setRid(long value) {
        bitField0_ |= 0x00000001;
        rid_ = value;
        
        return this;
      }
      public Builder clearRid() {
        bitField0_ = (bitField0_ & ~0x00000001);
        rid_ = 0L;
        
        return this;
      }
      
      // optional int32 gold = 2;
      private int gold_ ;
      public boolean hasGold() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public int getGold() {
        return gold_;
      }
      public Builder setGold(int value) {
        bitField0_ |= 0x00000002;
        gold_ = value;
        
        return this;
      }
      public Builder clearGold() {
        bitField0_ = (bitField0_ & ~0x00000002);
        gold_ = 0;
        
        return this;
      }
      
      // optional string nick = 3;
      private java.lang.Object nick_ = "";
      public boolean hasNick() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      public String getNick() {
        java.lang.Object ref = nick_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          nick_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setNick(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
        nick_ = value;
        
        return this;
      }
      public Builder clearNick() {
        bitField0_ = (bitField0_ & ~0x00000004);
        nick_ = getDefaultInstance().getNick();
        
        return this;
      }
      void setNick(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000004;
        nick_ = value;
        
      }
      
      // optional int32 head = 4;
      private int head_ ;
      public boolean hasHead() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      public int getHead() {
        return head_;
      }
      public Builder setHead(int value) {
        bitField0_ |= 0x00000008;
        head_ = value;
        
        return this;
      }
      public Builder clearHead() {
        bitField0_ = (bitField0_ & ~0x00000008);
        head_ = 0;
        
        return this;
      }
      
      // optional int32 level = 5;
      private int level_ ;
      public boolean hasLevel() {
        return ((bitField0_ & 0x00000010) == 0x00000010);
      }
      public int getLevel() {
        return level_;
      }
      public Builder setLevel(int value) {
        bitField0_ |= 0x00000010;
        level_ = value;
        
        return this;
      }
      public Builder clearLevel() {
        bitField0_ = (bitField0_ & ~0x00000010);
        level_ = 0;
        
        return this;
      }
      
      // optional int32 sex = 6;
      private int sex_ ;
      public boolean hasSex() {
        return ((bitField0_ & 0x00000020) == 0x00000020);
      }
      public int getSex() {
        return sex_;
      }
      public Builder setSex(int value) {
        bitField0_ |= 0x00000020;
        sex_ = value;
        
        return this;
      }
      public Builder clearSex() {
        bitField0_ = (bitField0_ & ~0x00000020);
        sex_ = 0;
        
        return this;
      }
      
      // @@protoc_insertion_point(builder_scope:com.yaowan.protobuf.cross.CrossRole)
    }
    
    static {
      defaultInstance = new CrossRole(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:com.yaowan.protobuf.cross.CrossRole)
  }
  
  
  static {
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
