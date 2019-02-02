package com.plustmobileapps.flutterpay

import org.json.JSONObject

data class GooglePayTransaction(
        val totalPrice: String = "12.34",
        val totalPriceStatus: String = "FINAL",
        val currencyCode: String = "USD"
) {

    val transactionInfo: JSONObject
        get() = JSONObject().apply {
            put("totalPrice", totalPrice)
            put("totalPriceStatus", totalPriceStatus)
            put("currencyCode", currencyCode)
        }

}