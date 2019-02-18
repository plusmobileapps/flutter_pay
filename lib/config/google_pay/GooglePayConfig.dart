import 'dart:convert';
import 'TokenizationSpecification.dart';

const _ALLOWED_CARDS = "allowedCards";
const _ALLOWED_AUTH_METHODS = "allowedAuthMethods";
const _MERCHANT_NAME = "merchantName";
const _TOKENIZATION_SPECIFICATION = "tokenizationSpecification";

class GooglePayConfig {
  final List<String> allowedCards;
  final List<String> allowedAuthMethods;
  final String merchantName;
  final TokenizationSpecification tokenizationSpecification;

  GooglePayConfig({this.allowedCards, this.allowedAuthMethods, this.merchantName, this.tokenizationSpecification});

  factory GooglePayConfig.fromJson(Map<String, dynamic> decodedJson) {
    List<String> allowedCards = new List();
    List<String> allowedAuthMethods = new List();
    for(var card in decodedJson[_ALLOWED_CARDS]) {
      allowedCards.add(card);
    }

    for(var authMethod in decodedJson[_ALLOWED_AUTH_METHODS]) {
      allowedAuthMethods.add(authMethod);
    }

    return new GooglePayConfig(
      allowedCards: allowedCards,
      allowedAuthMethods: allowedAuthMethods,
      merchantName: decodedJson[_MERCHANT_NAME],
      tokenizationSpecification: TokenizationSpecification.fromJson(decodedJson)
    );
  }

  Map<String, dynamic> toJson() =>
      {
        _ALLOWED_CARDS: allowedCards,
        _ALLOWED_AUTH_METHODS: allowedAuthMethods,
        _MERCHANT_NAME: merchantName,
        _TOKENIZATION_SPECIFICATION: jsonEncode(tokenizationSpecification)
      };
}


