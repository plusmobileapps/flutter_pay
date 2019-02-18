import 'GooglePayConfig.dart';
import 'ApplePayConfig.dart';
import 'dart:convert';

const _ENVIRONMENT = "environment";
const _GOOGLE_PAY_CONFIG = "googlePayConfig";
const _APPLE_PAY_CONFIG = "applePayConfig";

class FlutterPayConfig {



  final String environment;
  final GooglePayConfig googlePayConfig;
  final ApplePayConfig applePayConfig;

  FlutterPayConfig({this.environment, this.googlePayConfig, this.applePayConfig});

  factory FlutterPayConfig.fromJson(String json) {
    Map decoded = jsonDecode(json);
    String environment = decoded[_ENVIRONMENT];
    GooglePayConfig googlePayConfig = GooglePayConfig.fromJson(decoded[_GOOGLE_PAY_CONFIG]);
    ApplePayConfig applePayConfig = ApplePayConfig.fromJson(decoded[_APPLE_PAY_CONFIG]);

    return new FlutterPayConfig(
      environment: environment,
      googlePayConfig: googlePayConfig,
      applePayConfig: applePayConfig
    );
  }

  Map<String, dynamic> toJson() =>
      {
        _ENVIRONMENT: environment,
        _GOOGLE_PAY_CONFIG: googlePayConfig,
        _APPLE_PAY_CONFIG: applePayConfig
      };

}