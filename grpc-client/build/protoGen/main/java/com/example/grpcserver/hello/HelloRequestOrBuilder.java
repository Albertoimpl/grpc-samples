// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: hello.proto

package com.example.grpcserver.hello;

@javax.annotation.Generated(value="protoc", comments="annotations:HelloRequestOrBuilder.java.pb.meta")
public interface HelloRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:com.example.grpcserver.hello.HelloRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string firstName = 1;</code>
   * @return The firstName.
   */
  java.lang.String getFirstName();
  /**
   * <code>string firstName = 1;</code>
   * @return The bytes for firstName.
   */
  com.google.protobuf.ByteString
      getFirstNameBytes();

  /**
   * <code>string lastName = 2;</code>
   * @return The lastName.
   */
  java.lang.String getLastName();
  /**
   * <code>string lastName = 2;</code>
   * @return The bytes for lastName.
   */
  com.google.protobuf.ByteString
      getLastNameBytes();
}
