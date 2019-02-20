import 'dart:core';
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

  GooglePayConfig(
      {this.allowedCards,
      this.allowedAuthMethods,
      this.merchantName,
      this.tokenizationSpecification});

  GooglePayConfig.fromJson(Map<String, dynamic> json)
      : allowedCards = _getAllowedCards(json),
        allowedAuthMethods = _getAllowedAuthMethods(json),
        merchantName = json[_MERCHANT_NAME],
        tokenizationSpecification = TokenizationSpecification.fromJson(json);

  Map<String, dynamic> toJson() => {
        _ALLOWED_CARDS: allowedCards,
        _ALLOWED_AUTH_METHODS: allowedAuthMethods,
        _MERCHANT_NAME: merchantName,
        _TOKENIZATION_SPECIFICATION: tokenizationSpecification
      };

  static List<String> _getAllowedCards(Map<String, dynamic> json) {
    List<String> allowedCards = new List();
    for (var card in json[_ALLOWED_CARDS]) {
      allowedCards.add(card);
    }
    return allowedCards;
  }

  static List<String> _getAllowedAuthMethods(Map<String, dynamic> json) {
    List<String> allowedAuthMethods = new List();
    for (var authMethod in json[_ALLOWED_AUTH_METHODS]) {
      allowedAuthMethods.add(authMethod);
    }
    return allowedAuthMethods;
  }
}
