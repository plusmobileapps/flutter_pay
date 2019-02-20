import 'package:flutter_pay/config/google_pay/GooglePayConfig.dart';
import 'package:flutter_pay/config/apple_pay/ApplePayConfig.dart';
import 'dart:convert';

const _ENVIRONMENT = "environment";
const _GOOGLE_PAY_CONFIG = "googlePayConfig";
const _APPLE_PAY_CONFIG = "applePayConfig";

class FlutterPayConfig {
  final String environment;
  final GooglePayConfig googlePayConfig;
  final ApplePayConfig applePayConfig;

  FlutterPayConfig(
      {this.environment, this.googlePayConfig, this.applePayConfig});

  FlutterPayConfig.fromJson(Map<String, dynamic> json)
      : environment = json[_ENVIRONMENT],
        googlePayConfig = GooglePayConfig.fromJson(json),
        applePayConfig = ApplePayConfig.fromJson(json);

  Map<String, dynamic> toJson() => {
        _ENVIRONMENT: environment,
        _GOOGLE_PAY_CONFIG: googlePayConfig,
        _APPLE_PAY_CONFIG: applePayConfig
      };
}
