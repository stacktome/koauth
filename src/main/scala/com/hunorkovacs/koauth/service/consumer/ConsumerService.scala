package com.hunorkovacs.koauth.service.consumer

import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.domain.OauthParams._
import com.hunorkovacs.koauth.service.Arithmetics.{concatItemsForSignature, createAuthorizationHeader, sign}
import com.hunorkovacs.koauth.service.DefaultTokenGenerator.generateNonce
import com.hunorkovacs.koauth.service.{HmacSha1, SignatureMethod}

case class RequestWithInfo(request: KoauthRequest, signatureBase: String, header: String)

trait ConsumerService {

  def createRequestTokenRequest(request: KoauthRequest,
                                consumerKey: String,
                                consumerSecret: String,
                                callback: String,
                                signatureMethod: SignatureMethod = HmacSha1): RequestWithInfo

  def createAccessTokenRequest(request: KoauthRequest,
                               consumerKey: String,
                               consumerSecret: String,
                               requestToken: String,
                               requestTokenSecret: String,
                               verifier: String,
                               signatureMethod: SignatureMethod = HmacSha1): RequestWithInfo

  def createOauthenticatedRequest(request: KoauthRequest,
                                  consumerKey: String,
                                  consumerSecret: String,
                                  requestToken: String,
                                  requestTokenSecret: String,
                                  signatureMethod: SignatureMethod = HmacSha1): RequestWithInfo

  def createGeneralSignedRequest(request: KoauthRequest, algorithm: String = HmacSha1.algorithmName): RequestWithInfo
}

object DefaultConsumerService extends ConsumerService {

  private val secretNames = Set(ConsumerSecretName, TokenSecretName)

  def createRequestTokenRequest(request: KoauthRequest,
                                consumerKey: String,
                                consumerSecret: String,
                                callback: String,
                                signatureMethod: SignatureMethod = HmacSha1): RequestWithInfo = {
    createGeneralSignedRequest(
      KoauthRequest(request, ConsumerKeyName -> consumerKey
        :: ConsumerSecretName -> consumerSecret
        :: CallbackName -> callback
        :: SignatureMethodName -> signatureMethod.oauthSignatureMethodName
        :: basicParamList()),
      signatureMethod.algorithmName
    )
  }

  def createAccessTokenRequest(request: KoauthRequest,
                               consumerKey: String,
                               consumerSecret: String,
                               requestToken: String,
                               requestTokenSecret: String,
                               verifier: String,
                               signatureMethod: SignatureMethod = HmacSha1): RequestWithInfo = {
    createGeneralSignedRequest(
      KoauthRequest(request, ConsumerKeyName -> consumerKey
        :: ConsumerSecretName -> consumerSecret
        :: TokenName -> requestToken
        :: TokenSecretName -> requestTokenSecret
        :: VerifierName -> verifier
        :: SignatureMethodName -> signatureMethod.oauthSignatureMethodName
        :: basicParamList()),
      signatureMethod.algorithmName
    )
  }

  def createOauthenticatedRequest(request: KoauthRequest,
                                  consumerKey: String,
                                  consumerSecret: String,
                                  requestToken: String,
                                  requestTokenSecret: String,
                                  signatureMethod: SignatureMethod = HmacSha1): RequestWithInfo = {
    createGeneralSignedRequest(
      KoauthRequest(request, ConsumerKeyName -> consumerKey
        :: ConsumerSecretName -> consumerSecret
        :: TokenName -> requestToken
        :: TokenSecretName -> requestTokenSecret
        :: SignatureMethodName -> signatureMethod.oauthSignatureMethodName
        :: basicParamList()),
      signatureMethod.algorithmName
    )
  }

  def createGeneralSignedRequest(request: KoauthRequest, algorithm: String = HmacSha1.algorithmName): RequestWithInfo = {
    val consumerSecret = request.oauthParamsMap.applyOrElse(ConsumerSecretName, (s: String) => "")
    val tokenSecret = request.oauthParamsMap.applyOrElse(TokenSecretName, (s: String) => "")
    val base = createSignatureBase(request)
    val header = createAuthorizationHeader(SignatureName -> sign(base, consumerSecret, tokenSecret, algorithm)
      :: request.oauthParamsList.filterNot(p => secretNames(p._1)))
    RequestWithInfo(request, base, header)
  }

  def createSignatureBase(request: KoauthRequest): String = concatItemsForSignature(KoauthRequest(
    request.method,
    request.urlWithoutParams,
    request.urlParams,
    request.bodyParams,
    request.oauthParamsList.filterNot(p => secretNames(p._1))))

  private def basicParamList() = List(
    NonceName -> generateNonce,
    VersionName -> "1.0",
    TimestampName -> (System.currentTimeMillis() / 1000).toString
  )
}
