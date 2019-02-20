import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_pay/flutter_pay.dart';
import 'dart:io' show Platform;
import 'package:flutter_pay/FlutterPayButton.dart';


void main() => runApp(MyApp());

class MyApp extends StatefulWidget {

  bool isReadyCheckMade = false;
  bool flutterPayIsAvailable = false;

  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  void initState() {
    super.initState();

  }

  @override
  Widget build(BuildContext context) {
    if (!widget.isReadyCheckMade) {
      checkIsAvailable();
    }

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Flutter Pay'),
        ),
        body: _getBodyWidget(),
      ),
    );
  }

  Widget _getBodyWidget() {
    if (widget.flutterPayIsAvailable) {
      return FlutterPayButon(buttonTheme: FlutterPayButtonTheme.BLACK, onClickListener: _onFlutterPayButtonClicked);
    } else {
      return Text('Google Pay is not available');
    }
  }


  
  Future<void> checkIsAvailable() async {
    String config = await rootBundle.loadString('assets/flutter_pay/flutter_pay_config.json');
    bool isAvailable = await FlutterPay.loadConfigAndCheckAvailability(json: config);
    setState(() {
      widget.isReadyCheckMade = true;
      widget.flutterPayIsAvailable = isAvailable;
    });
  }

  void _onFlutterPayButtonClicked() {
    FlutterPay.openGooglePaySetup();
  }
}

