
const _TYPE = "type";
const _GATEWAY = "gateway";
const _GATEWAY_MERCHANT_ID = "gatewayMerchantId";
const _PROTOCOL_VERSION = "protocolVersion";
const _PUBLIC_KEY = "publicKey";

class TokenizationSpecification {
  final String type;
  final String gateway;
  final String gatewayMerchantId;
  final String protocolVersion;
  final String publicKey;

  TokenizationSpecification({this.type, this.gateway, this.gatewayMerchantId, this.protocolVersion, this.publicKey});

  factory TokenizationSpecification.fromJson(Map<String, dynamic> decodedJson) {
    String type = decodedJson[_TYPE];
    String gateway = decodedJson[_GATEWAY];
    String gatewayMerchantId = decodedJson[_GATEWAY_MERCHANT_ID];
    String protocolVersion = decodedJson[_PROTOCOL_VERSION];
    String publicKey = decodedJson[_PUBLIC_KEY];

    return new TokenizationSpecification(
        type: type,
        gateway: gateway,
        gatewayMerchantId: gatewayMerchantId,
        protocolVersion: protocolVersion,
        publicKey: publicKey);
  }

  Map<String, dynamic> toJson() =>
      {
        _TYPE: type,
        _GATEWAY: gateway,
        _GATEWAY_MERCHANT_ID: gatewayMerchantId,
        _PROTOCOL_VERSION: protocolVersion,
        _PUBLIC_KEY: publicKey
      };
}