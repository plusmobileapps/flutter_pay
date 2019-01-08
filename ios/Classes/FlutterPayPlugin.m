#import "FlutterPayPlugin.h"
#import <flutter_pay/flutter_pay-Swift.h>

@implementation FlutterPayPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterPayPlugin registerWithRegistrar:registrar];
}
@end
