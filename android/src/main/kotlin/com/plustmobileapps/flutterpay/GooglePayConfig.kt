package com.plustmobileapps.flutterpay

import com.google.android.gms.wallet.WalletConstants
import io.flutter.plugin.common.MethodChannel
import org.json.JSONArray
import org.json.JSONObject
import java.lang.IllegalStateException


/**
 * The configuration file to be set from flutter with a JSON string
 *
 * @property environment Google pay Production [com.google.android.gms.wallet.WalletConstants.ENVIRONMENT_PRODUCTION] or Test environment [com.google.android.gms.wallet.WalletConstants.ENVIRONMENT_TEST]
 * @property allowedCards list of cards to be used with google pay
 * @property allowedAuthMethods list of security authentication methods to be used with google pay for tokenization
 */
data class GooglePayConfig(
        private val tokenizationSpecication: TokenizationSpecification?,
        private val allowedCards: List<String>?,
        private val allowedAuthMethods: List<String>?,
        private val merchantName: String?) {

    var environment: String? = null

    val baseRequest: JSONObject
        get() = JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
        }

    val isValidConfiguration: Boolean
        get() = !(environment == null ||
                allowedCards.isNullOrEmpty() ||
                allowedAuthMethods.isNullOrEmpty() ||
                merchantName == null)

    fun getEnvironmentConstant(): Int {
        return when (environment) {
            "test" -> WalletConstants.ENVIRONMENT_TEST
            "production" -> WalletConstants.ENVIRONMENT_PRODUCTION
            else -> throw IllegalStateException("No valid environment set for google pay in the configuration. Must be test or production")
        }
    }

    fun getTokenizationSpecification(): JSONObject {
        val type = tokenizationSpecication?.tokenizationType
                ?: throw IllegalStateException("No tokenization specification set for google pay config")
        return when (type) {
            is TokenizationSpecifcationType.PaymentGateway -> {
                JSONObject().apply {
                    put("gateway", type.gateway)
                    put("gatewayMerchantId", type.gatewayMerchantId)
                }
            }
            is TokenizationSpecifcationType.Direct -> {
                JSONObject().apply {
                    put("protocolVersion", type.protocolVersion)
                    put("publicKey", type.publicKey)
                }
            }

        }
    }

    fun getMerchantInfo(): JSONObject {
        return when {
            merchantName.isNullOrEmpty() -> throw IllegalStateException("No merchant name set google pay configuration")
            else -> JSONObject().put("merchantName", merchantName)
        }
    }

    fun getAllowedCardNetworks(): JSONArray {
        return JSONArray().apply {
            allowedCards?.forEach { put(it) }
        }
    }

    fun getAllowedCardAuthMethods(): JSONArray {
        return JSONArray().apply {
            allowedAuthMethods?.forEach { put(it) }
        }
    }

    fun getPaymentDataRequest(transaction: GooglePayTransaction? = null): JSONObject {
        return baseRequest.apply {
            put("allowedPaymentMethods", JSONArray().put(getCardPaymentMethod()))
            transaction?.transactionInfo?.let { transactionJsonObject ->
                put("transactionInfo", transactionJsonObject)
                put("merchantInfo", getMerchantInfo())
            }
        }
    }

    private fun getCardPaymentMethod(): JSONObject {
        return getBaseCardPaymentMethod().apply {
            put("tokenizationSpecification", getTokenizationSpecification())
        }
    }

    fun getBaseCardPaymentMethod() = JSONObject().apply {
        put("type", "CARD")
        put("parameters", JSONObject().apply {
            put(ALLOWED_AUTH_CARD_METHODS, getAllowedCardAuthMethods())
            put(ALLOWED_CARD_NETWORKS, getAllowedCardNetworks())
        })
    }


    fun getIsReadyToPayRequest(): JSONObject = baseRequest.apply {
        put("allowedPaymentMethods", JSONArray().put(getBaseCardPaymentMethod()))
    }


}


const val TOKENIZATION_TYPE_PAYMENT_GATEWAY = "PAYMENT_GATEWAY"
const val TOKENIZATION_TYPE_DIRECT = "DIRECT"

data class TokenizationSpecification(
        private val type: String = "",
        private val gateway: String = "",
        private val gatewayMerchantId: String = "",
        private val protocolVersion: String = "",
        private val publicKey: String = ""
) {

    lateinit var tokenizationType: TokenizationSpecifcationType
        private set

    init {
        when (type) {
            TOKENIZATION_TYPE_PAYMENT_GATEWAY -> setupPaymentGateway()
            TOKENIZATION_TYPE_DIRECT -> setupDirect()
            else -> throw IllegalStateException("No proper Tokenization specification set, must be payment gateway or direct")
        }
    }

    private fun setupPaymentGateway() {
        tokenizationType = TokenizationSpecifcationType.PaymentGateway(gateway = gateway, gatewayMerchantId = gatewayMerchantId)
    }

    private fun setupDirect() {
        tokenizationType = TokenizationSpecifcationType.Direct(protocolVersion = protocolVersion, publicKey = publicKey)
    }

}


