import 'dart:async';
import 'dart:io' show Platform;
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter_pay/config/FlutterPayConfig.dart';

enum Environment { TEST, PRODUCTION }


class FlutterPay {
  static const MethodChannel _channel =
      const MethodChannel('flutter_pay');


  static Future<bool> loadConfigAndCheckAvailability({json: String, environment: Environment}) async {
    FlutterPayConfig flutterPayConfig = FlutterPayConfig.fromJson(json);
    String env = _getEnvironmentString(environment: environment);
    final bool paymentAvailable = await _channel.invokeMethod('configureGooglePay', {"googlePayConfig": jsonEncode(flutterPayConfig.googlePayConfig), "environment": env});
    return paymentAvailable;
//    if(Platform.isAndroid) {
//
//    } else if(Platform.isIOS) {
//      //check if apple pay is available here
////      flutterPayConfig.applePayConfig
//    }

  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> get googlePayToken async {
    final String googlePayToken = await _channel.invokeMethod('getGooglePayToken');
    return googlePayToken;
  }

  static void openGooglePaySetup() {
    _channel.invokeMethod('openGooglePaySetup');
  }

  static Future<bool> get checkIsReadyToPay async {
    final bool isGooglePayAvailable = await _channel.invokeMethod('checkIsReadyToPay');
    return isGooglePayAvailable;
  }

  static String _getEnvironmentString({environment: Environment}) {
    switch (environment) {
      case Environment.PRODUCTION:
        return "production";
      case Environment.TEST:
        return "test";
    }
    return "test";
  }

}
