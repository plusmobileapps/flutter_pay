package com.plustmobileapps.flutterpay

import android.app.Activity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.json.JSONArray
import org.json.JSONObject
import java.lang.IllegalStateException

const val AMEX = "AMEX"
const val DISCOVER = "DISCOVER"
const val JCB = "JCB"
const val MASTERCARD = "MASTERCARD"
const val VISA = "VISA"

//flutter method calls
const val CHECK_IS_READY_TO_PAY = "checkIsReadyToPay"
const val OPEN_GOOGLE_PAY = "openGooglePaySetup"

//google pay json keys
const val ALLOWED_AUTH_CARD_METHODS = "allowedAuthMethods"
const val ALLOWED_CARD_NETWORKS = "allowedCardNetworks"

enum class AllowedCard(name: String) {
    AMEX("AMEX"),
    DISCOVER("DISCOVER"),
    JCB("JCB"),
    MASTERCARD("MASTERCARD"),
    VISA("VISA")
}

enum class AllowedCardAuthMethod(name: String) {
    PAN_ONLY("PAN_ONLY"),
    CRYPTOGRAM_3DS("CRYPTOGRAM_3DS")
}

data class GooglePayConfig(val allowedCards: List<String>, val allowedAuthMethods: List<String>)

class FlutterPayPlugin(private val activity: Activity) : MethodCallHandler {

    private val paymentsClient = Wallet.getPaymentsClient(
            activity,
            Wallet.WalletOptions.Builder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                    .build())

    private val baseRequest: JSONObject
        get() = JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
        }

    private var googlePayConfig: GooglePayConfig? = null

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            CHECK_IS_READY_TO_PAY -> checkIsReadyToPay(call, result)
            OPEN_GOOGLE_PAY -> openGooglePaySetup()
            else -> result.notImplemented()
        }
    }

    private fun checkIsReadyToPay(call: MethodCall, flutterResult: Result) {

        if (!call.hasArgument(ALLOWED_CARD_NETWORKS)) {
            throw IllegalStateException("method: $CHECK_IS_READY_TO_PAY - had no argument passed for $ALLOWED_CARD_NETWORKS")
        }

        if (!call.hasArgument(ALLOWED_AUTH_CARD_METHODS)) {
            throw IllegalStateException("method: $CHECK_IS_READY_TO_PAY - had no argument passed for $ALLOWED_AUTH_CARD_METHODS")
        }

        val allowedPaymentMethods = call.argument<List<String>>(ALLOWED_CARD_NETWORKS) ?: throw IllegalStateException("No card networks found")
        val allowedAuthCardMethods = call.argument<List<String>>(ALLOWED_AUTH_CARD_METHODS) ?: throw IllegalStateException("No card auth methods found")


        val googlePayConfig = GooglePayConfig(allowedPaymentMethods, allowedAuthCardMethods)
        this.googlePayConfig = googlePayConfig
        val request = IsReadyToPayRequest.fromJson(getIsReadyToPayRequest(googlePayConfig).toString())
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { task ->
            try {
                val result = task.getResult(ApiException::class.java)!!
                if (result) {
                    // show Google Pay as a payment option
                    flutterResult.success(true)
                }
            } catch (e: ApiException) {
                flutterResult.success(false)
            }
        }
    }


    private fun getTokenizationSpecification(): JSONObject {
        val tokenizationSpecification = JSONObject()
        tokenizationSpecification.put("type", "PAYMENT_GATEWAY")
        tokenizationSpecification.put(
                "parameters",
                JSONObject()
                        .put("gateway", "example")
                        .put("gatewayMerchantId", "exampleMerchantId"))

        return tokenizationSpecification
    }

    private fun getAllowedCardNetworks(cards: List<String>) = JSONArray().apply {
        cards.forEach { put(it) }
    }


    private fun getAllowedCardAuthMethods(allowedAuthMethods: List<String>) = JSONArray().apply {
        allowedAuthMethods.forEach { put(it) }
    }

    private fun getBaseCardPaymentMethod(config: GooglePayConfig): JSONObject {
        return JSONObject().apply {
            put("type", "CARD")
            put("parameters", JSONObject().apply {
                put(ALLOWED_AUTH_CARD_METHODS, getAllowedCardAuthMethods(config.allowedAuthMethods))
                put(ALLOWED_CARD_NETWORKS, getAllowedCardNetworks(config.allowedCards))
            })
        }
    }

    private fun getCardPaymentMethod(): JSONObject {
        val googlePayConfig = googlePayConfig ?: throw IllegalStateException("Google pay config was never setup by calling $CHECK_IS_READY_TO_PAY first")
        val cardPaymentMethod = getBaseCardPaymentMethod(googlePayConfig)
        cardPaymentMethod.put("tokenizationSpecification", getTokenizationSpecification())

        return cardPaymentMethod
    }

    fun getIsReadyToPayRequest(config: GooglePayConfig): JSONObject = baseRequest.apply {
        put("allowedPaymentMethods", JSONArray().put(getBaseCardPaymentMethod(config)))
    }

    companion object {

        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 42

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            MethodChannel(registrar.messenger(), "flutter_pay").apply {
                setMethodCallHandler(FlutterPayPlugin(registrar.activity()))
            }
        }

    }

    private fun getTransactionInfo(): JSONObject {
        val transactionInfo = JSONObject()
        transactionInfo.put("totalPrice", "12.34")
        transactionInfo.put("totalPriceStatus", "FINAL")
        transactionInfo.put("currencyCode", "USD")

        return transactionInfo
    }

    private fun getMerchantInfo(): JSONObject {
        return JSONObject()
                .put("merchantName", "Example Merchant")
    }

    fun getPaymentDataRequest(): JSONObject {
        val paymentDataRequest = baseRequest
        paymentDataRequest.put(
                "allowedPaymentMethods",
                JSONArray()
                        .put(getCardPaymentMethod()))
        paymentDataRequest.put("transactionInfo", getTransactionInfo())
        paymentDataRequest.put("merchantInfo", getMerchantInfo())

        return paymentDataRequest
    }

    private fun openGooglePaySetup() {
        val request = PaymentDataRequest.fromJson(getPaymentDataRequest().toString())
        request?.let {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request),
                    activity,
                    LOAD_PAYMENT_DATA_REQUEST_CODE
            )
        }
    }
}
