package com.serverless

class Response(message:String) {
  val message: String = message
    get

  override fun toString(): String {
    return message
  }
}