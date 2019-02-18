package com.plustmobileapps.flutterpay

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.google.gson.Gson
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.json.JSONObject
import java.lang.IllegalStateException

const val AMEX = "AMEX"
const val DISCOVER = "DISCOVER"
const val JCB = "JCB"
const val MASTERCARD = "MASTERCARD"
const val VISA = "VISA"

sealed class TokenizationSpecifcationType {
    abstract val type: String

    data class PaymentGateway(override val type: String = TOKENIZATION_TYPE_PAYMENT_GATEWAY, val gateway: String, val gatewayMerchantId: String) : TokenizationSpecifcationType()
    data class Direct(override val type: String = TOKENIZATION_TYPE_DIRECT, val protocolVersion: String, val publicKey: String) : TokenizationSpecifcationType()
}

//flutter method calls
const val CONFIGURE_GOOGLE_PAY = "configureGooglePay"
const val CHECK_IS_READY_TO_PAY = "checkIsReadyToPay"
const val OPEN_GOOGLE_PAY_SETUP = "openGooglePaySetup"
const val OPEN_GOOGLE_PAY_TRANSACTION = "openGooglePayTransaction"

//google pay json keys
const val GOOGLE_PAY_CONFIG = "googlePayConfig"
const val GOOGLE_PAY_TRANSACTION = "googlePayTransactionInfo"
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


class FlutterPayPlugin(private val activity: Activity, registrar: Registrar) : MethodCallHandler, PluginRegistry.ActivityResultListener {

    companion object {

        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 42

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            MethodChannel(registrar.messenger(), "flutter_pay").apply {
                setMethodCallHandler(FlutterPayPlugin(registrar.activity(), registrar))
            }
        }

    }

    init {
        registrar.addActivityResultListener(this)
    }

    private lateinit var googlePayConfig: GooglePayConfig
    private val gson = Gson()

    private val paymentsClient: PaymentsClient
        get() = Wallet.getPaymentsClient(
                activity,
                Wallet.WalletOptions.Builder()
                        .setEnvironment(googlePayConfig.getEnvironmentConstant())
                        .build())

    private var call: MethodCall? = null
    private var result: Result? = null


    /**
     * All flutter method calls are communicated right here
     */
    override fun onMethodCall(call: MethodCall, result: Result) {
        this.call = call
        this.result = result
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            CONFIGURE_GOOGLE_PAY -> configureGooglePay(call, result)
            OPEN_GOOGLE_PAY_SETUP -> openGooglePaySetup(call, result)
            OPEN_GOOGLE_PAY_TRANSACTION -> openGooglePayTransaction(call, result)
            else -> result.notImplemented()
        }
    }

    private fun configureGooglePay(call: MethodCall, result: Result) {
        if (!call.hasArgument(GOOGLE_PAY_CONFIG)) {
            result.error(GOOGLE_PAY_CONFIG, "No google pay configuration set in argument of method call", Any())
            throw IllegalStateException("No argument passed for $GOOGLE_PAY_CONFIG")
        }

        val environment = call.argument<String>("environment");

        googlePayConfig = gson.fromJson(call.argument<String>(GOOGLE_PAY_CONFIG), GooglePayConfig::class.java)
        googlePayConfig.environment = environment
        if (!googlePayConfig.isValidConfiguration) {
            result.error("Invalid Google Pay Configuration was passed in", "GP", "")
            return
        }

        checkIsReadyToPay(call, result)
    }

    private fun checkIsReadyToPay(call: MethodCall, flutterResult: Result) {
        val request = IsReadyToPayRequest.fromJson(googlePayConfig.getIsReadyToPayRequest().toString())
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { task ->
            try {
                val result = task.getResult(ApiException::class.java)!!
                if (result) {
                    // show Google Pay as a payment option
                    flutterResult.success(true)
                } else {
                    flutterResult.error("Google Pay not available on device", "GP", result)
                }
            } catch (e: ApiException) {
                flutterResult.success(false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                handleGooglePayResult(resultCode, data)
                true
            }
            else -> false
        }
    }

    private fun handleGooglePayResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val intent = data ?: throw IllegalStateException("No data returned from google pay for successful callback")
                val paymentData = PaymentData.getFromIntent(intent)
                val json = paymentData?.toJson()
                val paymentMethodData = JSONObject(json).getJSONObject("paymentMethodData")
                val paymentToken = paymentMethodData
                        .getJSONObject("tokenizationData")
                        .getString("token")
                result?.success(paymentToken)
            }
            Activity.RESULT_CANCELED -> {
                result?.error("Google Pay Cancelled", "Google Pay was cancelled by the user", "")
            }
            AutoResolveHelper.RESULT_ERROR -> {
                val status = AutoResolveHelper.getStatusFromIntent(data)
                // Log the status for debugging.
                // Generally, there is no need to show an error to the user.
                // The Google Pay payment sheet will present any account errors.
                result?.error(status?.status.toString(), status?.statusMessage, "Google Pay error")
            }
        }
    }


    /**
     * this will only open up google pay for setup purposes and will not open the google pay picker to charge a user
     */
    private fun openGooglePaySetup(call: MethodCall, result: Result) {
        val request = PaymentDataRequest.fromJson(googlePayConfig.getPaymentDataRequest().toString())
        request?.let { paymentDataRequest ->
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(paymentDataRequest),
                    activity,
                    LOAD_PAYMENT_DATA_REQUEST_CODE
            )
        }
    }

    private fun openGooglePayTransaction(call: MethodCall, result: Result) {
        val transaction = gson.fromJson(call.argument<String>(GOOGLE_PAY_TRANSACTION), GooglePayTransaction::class.java)

        val request = PaymentDataRequest.fromJson(googlePayConfig.getPaymentDataRequest(transaction).toString())
        request?.let { paymentDataRequest ->
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(paymentDataRequest),
                    activity,
                    LOAD_PAYMENT_DATA_REQUEST_CODE
            )
        }

    }
}
