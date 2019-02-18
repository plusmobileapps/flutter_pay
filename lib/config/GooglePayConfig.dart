import 'dart:convert';

class GooglePayConfig {
  final List<String> allowedCards;
  final List<String> allowedAuthMethods;
  final String merchantName;
  final TokenizationSpecification tokenizationSpecification;

  GooglePayConfig({this.allowedCards, this.allowedAuthMethods, this.merchantName, this.tokenizationSpecification});

  factory GooglePayConfig.fromJson(Map<String, dynamic> decodedJson) {
    List<String> allowedCards = new List();
    List<String> allowedAuthMethods = new List();
    for(var card in decodedJson['allowedCards']) {
      allowedCards.add(card);
    }

    for(var authMethod in decodedJson['allowedAuthMethods']) {
      allowedAuthMethods.add(authMethod);
    }

    return new GooglePayConfig(
      allowedCards: allowedCards,
      allowedAuthMethods: allowedAuthMethods,
      merchantName: decodedJson['merchantName'],
      tokenizationSpecification: TokenizationSpecification.fromJson(decodedJson)
    );
  }

  Map<String, dynamic> toJson() =>
      {
        'allowedCards': allowedCards,
        'allowedAuthMethods': allowedAuthMethods,
        'merchantName': merchantName,
        'tokenizationSpecification': jsonEncode(tokenizationSpecification)
      };
}


class TokenizationSpecification {
  final String type;
  final String gateway;
  final String gatewayMerchantId;
  final String protocolVersion;
  final String publicKey;

  TokenizationSpecification({this.type, this.gateway, this.gatewayMerchantId, this.protocolVersion, this.publicKey});

  factory TokenizationSpecification.fromJson(Map<String, dynamic> decodedJson) {
    String type = decodedJson['type'];
    String gateway = decodedJson['gateway'];
    String gatewayMerchantId = decodedJson['gatewayMerchantId'];
    String protocolVersion = decodedJson['protocolVersion'];
    String publicKey = decodedJson['publicKey'];

    return new TokenizationSpecification(
        type: type,
        gateway: gateway,
        gatewayMerchantId: gatewayMerchantId,
        protocolVersion: protocolVersion,
        publicKey: publicKey);
  }

  Map<String, dynamic> toJson() =>
      {
        'type': type,
        'gateway': gateway,
        'gatewayMerchantId': gatewayMerchantId,
        'protocolVersion': protocolVersion,
        'publicKey': publicKey
      };
}