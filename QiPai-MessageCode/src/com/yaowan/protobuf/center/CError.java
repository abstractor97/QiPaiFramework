// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cerror.proto

package com.yaowan.protobuf.center;

public final class CError {
  private CError() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }
  public interface CMsg_21003001OrBuilder
      extends com.google.protobuf.MessageLiteOrBuilder {
  }
  public static final class CMsg_21003001 extends
      com.google.protobuf.GeneratedMessageLite
      implements CMsg_21003001OrBuilder {
    // Use CMsg_21003001.newBuilder() to construct.
    private CMsg_21003001(Builder builder) {
      super(builder);
    }
    private CMsg_21003001(boolean noInit) {}
    
    private static final CMsg_21003001 defaultInstance;
    public static CMsg_21003001 getDefaultInstance() {
      return defaultInstance;
    }
    
    public CMsg_21003001 getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    private void initFields() {
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static com.yaowan.protobuf.center.CError.CMsg_21003001 parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_21003001 parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_21003001 parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_21003001 parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_21003001 parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_21003001 parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_21003001 parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.yaowan.protobuf.center.CError.CMsg_21003001 parseDelimitedFrom(
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
    public static com.yaowan.protobuf.center.CError.CMsg_21003001 parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_21003001 parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.yaowan.protobuf.center.CError.CMsg_21003001 prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageLite.Builder<
          com.yaowan.protobuf.center.CError.CMsg_21003001, Builder>
        implements com.yaowan.protobuf.center.CError.CMsg_21003001OrBuilder {
      // Construct using com.yaowan.protobuf.center.CError.CMsg_21003001.newBuilder()
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
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.yaowan.protobuf.center.CError.CMsg_21003001 getDefaultInstanceForType() {
        return com.yaowan.protobuf.center.CError.CMsg_21003001.getDefaultInstance();
      }
      
      public com.yaowan.protobuf.center.CError.CMsg_21003001 build() {
        com.yaowan.protobuf.center.CError.CMsg_21003001 result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private com.yaowan.protobuf.center.CError.CMsg_21003001 buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        com.yaowan.protobuf.center.CError.CMsg_21003001 result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public com.yaowan.protobuf.center.CError.CMsg_21003001 buildPartial() {
        com.yaowan.protobuf.center.CError.CMsg_21003001 result = new com.yaowan.protobuf.center.CError.CMsg_21003001(this);
        return result;
      }
      
      public Builder mergeFrom(com.yaowan.protobuf.center.CError.CMsg_21003001 other) {
        if (other == com.yaowan.protobuf.center.CError.CMsg_21003001.getDefaultInstance()) return this;
        return this;
      }
      
      public final boolean isInitialized() {
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
          }
        }
      }
      
      
      // @@protoc_insertion_point(builder_scope:com.yaowan.protobuf.center.CMsg_21003001)
    }
    
    static {
      defaultInstance = new CMsg_21003001(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:com.yaowan.protobuf.center.CMsg_21003001)
  }
  
  public interface CMsg_22003001OrBuilder
      extends com.google.protobuf.MessageLiteOrBuilder {
    
    // optional int32 error_code = 1;
    boolean hasErrorCode();
    int getErrorCode();
    
    // optional int32 protocol = 2;
    boolean hasProtocol();
    int getProtocol();
    
    // optional string error_msg = 3;
    boolean hasErrorMsg();
    String getErrorMsg();
  }
  public static final class CMsg_22003001 extends
      com.google.protobuf.GeneratedMessageLite
      implements CMsg_22003001OrBuilder {
    // Use CMsg_22003001.newBuilder() to construct.
    private CMsg_22003001(Builder builder) {
      super(builder);
    }
    private CMsg_22003001(boolean noInit) {}
    
    private static final CMsg_22003001 defaultInstance;
    public static CMsg_22003001 getDefaultInstance() {
      return defaultInstance;
    }
    
    public CMsg_22003001 getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    private int bitField0_;
    // optional int32 error_code = 1;
    public static final int ERROR_CODE_FIELD_NUMBER = 1;
    private int errorCode_;
    public boolean hasErrorCode() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public int getErrorCode() {
      return errorCode_;
    }
    
    // optional int32 protocol = 2;
    public static final int PROTOCOL_FIELD_NUMBER = 2;
    private int protocol_;
    public boolean hasProtocol() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public int getProtocol() {
      return protocol_;
    }
    
    // optional string error_msg = 3;
    public static final int ERROR_MSG_FIELD_NUMBER = 3;
    private java.lang.Object errorMsg_;
    public boolean hasErrorMsg() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    public String getErrorMsg() {
      java.lang.Object ref = errorMsg_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          errorMsg_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getErrorMsgBytes() {
      java.lang.Object ref = errorMsg_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        errorMsg_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    private void initFields() {
      errorCode_ = 0;
      protocol_ = 0;
      errorMsg_ = "";
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeInt32(1, errorCode_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeInt32(2, protocol_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeBytes(3, getErrorMsgBytes());
      }
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, errorCode_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, protocol_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(3, getErrorMsgBytes());
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
    
    public static com.yaowan.protobuf.center.CError.CMsg_22003001 parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_22003001 parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_22003001 parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_22003001 parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_22003001 parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_22003001 parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_22003001 parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.yaowan.protobuf.center.CError.CMsg_22003001 parseDelimitedFrom(
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
    public static com.yaowan.protobuf.center.CError.CMsg_22003001 parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.yaowan.protobuf.center.CError.CMsg_22003001 parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.yaowan.protobuf.center.CError.CMsg_22003001 prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageLite.Builder<
          com.yaowan.protobuf.center.CError.CMsg_22003001, Builder>
        implements com.yaowan.protobuf.center.CError.CMsg_22003001OrBuilder {
      // Construct using com.yaowan.protobuf.center.CError.CMsg_22003001.newBuilder()
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
        errorCode_ = 0;
        bitField0_ = (bitField0_ & ~0x00000001);
        protocol_ = 0;
        bitField0_ = (bitField0_ & ~0x00000002);
        errorMsg_ = "";
        bitField0_ = (bitField0_ & ~0x00000004);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.yaowan.protobuf.center.CError.CMsg_22003001 getDefaultInstanceForType() {
        return com.yaowan.protobuf.center.CError.CMsg_22003001.getDefaultInstance();
      }
      
      public com.yaowan.protobuf.center.CError.CMsg_22003001 build() {
        com.yaowan.protobuf.center.CError.CMsg_22003001 result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private com.yaowan.protobuf.center.CError.CMsg_22003001 buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        com.yaowan.protobuf.center.CError.CMsg_22003001 result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public com.yaowan.protobuf.center.CError.CMsg_22003001 buildPartial() {
        com.yaowan.protobuf.center.CError.CMsg_22003001 result = new com.yaowan.protobuf.center.CError.CMsg_22003001(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.errorCode_ = errorCode_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.protocol_ = protocol_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.errorMsg_ = errorMsg_;
        result.bitField0_ = to_bitField0_;
        return result;
      }
      
      public Builder mergeFrom(com.yaowan.protobuf.center.CError.CMsg_22003001 other) {
        if (other == com.yaowan.protobuf.center.CError.CMsg_22003001.getDefaultInstance()) return this;
        if (other.hasErrorCode()) {
          setErrorCode(other.getErrorCode());
        }
        if (other.hasProtocol()) {
          setProtocol(other.getProtocol());
        }
        if (other.hasErrorMsg()) {
          setErrorMsg(other.getErrorMsg());
        }
        return this;
      }
      
      public final boolean isInitialized() {
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
              errorCode_ = input.readInt32();
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              protocol_ = input.readInt32();
              break;
            }
            case 26: {
              bitField0_ |= 0x00000004;
              errorMsg_ = input.readBytes();
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // optional int32 error_code = 1;
      private int errorCode_ ;
      public boolean hasErrorCode() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public int getErrorCode() {
        return errorCode_;
      }
      public Builder setErrorCode(int value) {
        bitField0_ |= 0x00000001;
        errorCode_ = value;
        
        return this;
      }
      public Builder clearErrorCode() {
        bitField0_ = (bitField0_ & ~0x00000001);
        errorCode_ = 0;
        
        return this;
      }
      
      // optional int32 protocol = 2;
      private int protocol_ ;
      public boolean hasProtocol() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public int getProtocol() {
        return protocol_;
      }
      public Builder setProtocol(int value) {
        bitField0_ |= 0x00000002;
        protocol_ = value;
        
        return this;
      }
      public Builder clearProtocol() {
        bitField0_ = (bitField0_ & ~0x00000002);
        protocol_ = 0;
        
        return this;
      }
      
      // optional string error_msg = 3;
      private java.lang.Object errorMsg_ = "";
      public boolean hasErrorMsg() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      public String getErrorMsg() {
        java.lang.Object ref = errorMsg_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          errorMsg_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setErrorMsg(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
        errorMsg_ = value;
        
        return this;
      }
      public Builder clearErrorMsg() {
        bitField0_ = (bitField0_ & ~0x00000004);
        errorMsg_ = getDefaultInstance().getErrorMsg();
        
        return this;
      }
      void setErrorMsg(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000004;
        errorMsg_ = value;
        
      }
      
      // @@protoc_insertion_point(builder_scope:com.yaowan.protobuf.center.CMsg_22003001)
    }
    
    static {
      defaultInstance = new CMsg_22003001(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:com.yaowan.protobuf.center.CMsg_22003001)
  }
  
  
  static {
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}