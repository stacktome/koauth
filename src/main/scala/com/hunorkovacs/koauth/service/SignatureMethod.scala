package com.hunorkovacs.koauth.service

sealed trait SignatureMethod {
  def algorithmName: String
  def oauthSignatureMethodName: String
}

case object HmacSha1 extends SignatureMethod {
  override def algorithmName: String = "HmacSHA1"

  override def oauthSignatureMethodName: String = "HMAC-SHA1"
}

case object HmacSha256 extends SignatureMethod {
  override def algorithmName: String = "HmacSHA256"

  override def oauthSignatureMethodName: String = "HMAC-SHA256"
}

case object HmacSha512 extends SignatureMethod {
  override def algorithmName: String = "HmacSHA512"

  override def oauthSignatureMethodName: String = "HMAC-SHA512"
}