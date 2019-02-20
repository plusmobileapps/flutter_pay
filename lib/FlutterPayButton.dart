import 'package:flutter/material.dart';

enum FlutterPayButtonTheme { WHITE, BLACK }
enum FLUTTER_PAY_TYPE { GOOGLE_PAY, APPLE_PAY }

typedef OnClickListener = void Function();

class FlutterPayButon extends StatefulWidget {
  final FlutterPayButtonTheme buttonTheme;
  final OnClickListener onClickListener;

  FlutterPayButon({this.buttonTheme, this.onClickListener});

  @override
  _FlutterPayButtonState createState() => _FlutterPayButtonState();
  
}


class _FlutterPayButtonState extends State<FlutterPayButon> {
  @override
  Widget build(BuildContext context) {
    String imagePath = _getGooglePayImageAsset();
    //TODO do platform check and swap for apple pay
    

    return RaisedButton(
      child: Image.asset(imagePath),
      onPressed: widget.onClickListener,
    );
  }
  
  String _getGooglePayImageAsset() {
    if (widget.buttonTheme == FlutterPayButtonTheme.BLACK) {
      return 'assets/google-pay-black.png';
    } else {
      return 'assets/google-pay-white.png';
    }
  }

}