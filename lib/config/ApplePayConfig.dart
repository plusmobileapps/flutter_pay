
class ApplePayConfig {
  final String environment;

  ApplePayConfig({this.environment});

  factory ApplePayConfig.fromJson(Map<String, dynamic> decodedJson) {
    return new ApplePayConfig(
      environment: decodedJson['environment']
    );
  }

}