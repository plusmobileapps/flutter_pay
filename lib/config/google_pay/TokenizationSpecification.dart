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

  TokenizationSpecification(
      {this.type,
      this.gateway,
      this.gatewayMerchantId,
      this.protocolVersion,
      this.publicKey});

  TokenizationSpecification.fromJson(Map<String, dynamic> decodedJson)
      : type = decodedJson[_TYPE],
        gateway = decodedJson[_GATEWAY],
        gatewayMerchantId = decodedJson[_GATEWAY_MERCHANT_ID],
        protocolVersion = decodedJson[_PROTOCOL_VERSION],
        publicKey = decodedJson[_PUBLIC_KEY];

  Map<String, dynamic> toJson() => {
        _TYPE: type,
        _GATEWAY: gateway,
        _GATEWAY_MERCHANT_ID: gatewayMerchantId,
        _PROTOCOL_VERSION: protocolVersion,
        _PUBLIC_KEY: publicKey
      };
}
