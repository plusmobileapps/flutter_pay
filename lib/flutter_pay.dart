import 'dart:async';

import 'package:flutter/services.dart';

class FlutterPay {
  static const MethodChannel _channel =
      const MethodChannel('flutter_pay');

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

}
