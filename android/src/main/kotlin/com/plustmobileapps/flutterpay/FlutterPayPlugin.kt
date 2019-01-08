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

    private fun getAllowedCardNetworks(): JSONArray {
        return JSONArray()
                .put("AMEX")
                .put("DISCOVER")
                .put("JCB")
                .put("MASTERCARD")
                .put("VISA")
    }

    private fun getAllowedCardAuthMethods(): JSONArray {
        return JSONArray()
                .put("PAN_ONLY")
                .put("CRYPTOGRAM_3DS")
    }

    private fun getBaseCardPaymentMethod(): JSONObject {
        val cardPaymentMethod = JSONObject()
        cardPaymentMethod.put("type", "CARD")
        cardPaymentMethod.put(
                "parameters",
                JSONObject()
                        .put("allowedAuthMethods", getAllowedCardAuthMethods())
                        .put("allowedCardNetworks", getAllowedCardNetworks()))

        return cardPaymentMethod
    }

    private fun getCardPaymentMethod(): JSONObject {
        val cardPaymentMethod = getBaseCardPaymentMethod()
        cardPaymentMethod.put("tokenizationSpecification", getTokenizationSpecification())

        return cardPaymentMethod
    }

    fun getIsReadyToPayRequest(): JSONObject {
        val isReadyToPayRequest = baseRequest
        isReadyToPayRequest.put(
                "allowedPaymentMethods",
                JSONArray()
                        .put(getBaseCardPaymentMethod()))

        return isReadyToPayRequest
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

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "checkIsReadyToPay" -> checkIsReadyToPay(result)
            "openGooglePaySetup" -> openGooglePaySetup()
            else -> result.notImplemented()
        }
    }

    private fun checkIsReadyToPay(flutterResult: Result) {
        val request = IsReadyToPayRequest.fromJson(getIsReadyToPayRequest().toString())
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
