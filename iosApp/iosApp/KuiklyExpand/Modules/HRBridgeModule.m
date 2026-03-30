#import "HRBridgeModule.h"

#import "KuiklyRenderViewController.h"
#import <OpenKuiklyIOSRender/NSObject+KR.h>
#import <SafariServices/SafariServices.h>

#define REQ_PARAM_KEY @"reqParam"
#define CMD_KEY @"cmd"
#define FROM_HIPPY_RENDER @"from_hippy_render"
// 扩展桥接接口
/*
 * @brief Native暴露接口到kotlin侧，提供kotlin侧调用native能力
 */

@implementation HRBridgeModule

@synthesize hr_rootView;

- (void)openPage:(NSDictionary *)args {
    NSDictionary *params = [args[KR_PARAM_KEY] hr_stringToDictionary];
    NSString *url = params[@"url"];
    NSString *title = params[@"title"];
    if (url.length > 0) {
        NSURL *nsUrl = [NSURL URLWithString:url];
        SFSafariViewController *safariVC = [[SFSafariViewController alloc] initWithURL:nsUrl];
        safariVC.dismissButtonStyle = SFSafariViewControllerDismissButtonStyleClose;
        dispatch_async(dispatch_get_main_queue(), ^{
            UIViewController *vc = self.hr_rootView.kr_viewController;
            UINavigationController *nav = vc.navigationController;
            if (nav) {
                [nav pushViewController:safariVC animated:YES];
            } else {
                [vc presentViewController:safariVC animated:YES completion:nil];
            }
        });
    }
}

- (void)copyToPasteboard:(NSDictionary *)args {
    NSDictionary *params = [args[KR_PARAM_KEY] hr_stringToDictionary];
    NSString *content = params[@"content"];
    UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
    pasteboard.string = content;
}

- (void)log:(NSDictionary *)args {
    NSDictionary *params = [args[KR_PARAM_KEY] hr_stringToDictionary];
    NSString *content = params[@"content"];
    NSLog(@"KuiklyRender:%@", content);
}

@end