import { webview } from '@kit.ArkWeb';
import window from '@ohos.window';
import router from '@ohos.router';

@Entry
@Component
struct WebViewPage {
  webController: webview.WebviewController = new webview.WebviewController();
  pageUrl: string = '';
  pageTitle: string = '';
  topSafeHeight: number = 0;

  aboutToAppear() {
    const params = router.getParams() as Record<string, string>;
    this.pageUrl = params?.['url'] || '';
    this.pageTitle = params?.['title'] || '';
    try {
      let windowClass = window.findWindow('WebViewPage');
      let avoidArea = windowClass.getWindowAvoidArea(window.AvoidAreaType.TYPE_SYSTEM);
      this.topSafeHeight = px2vp(avoidArea.topRect.height);
    } catch (e) {
      this.topSafeHeight = 48;
    }
  }

  build() {
    Column() {
      Row() {
        Image($r('sys.media.ohos_ic_public_arrow_left'))
          .width(24)
          .height(24)
          .margin({ left: 16, right: 8 })
          .onClick(() => {
            router.back();
          })
        Text(this.pageTitle)
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
          .layoutWeight(1)
          .textAlign(TextAlign.Center)
          .margin({ right: 40 })
      }
      .width('100%')
      .height(56)
      .margin({ top: this.topSafeHeight })

      Web({
        src: this.pageUrl,
        controller: this.webController
      })
        .layoutWeight(1)
        .width('100%')
        .backgroundColor('#FFFFFF')
        .domStorageAccess(true)
        .fileAccess(true)
        .javaScriptAccess(true)
        .mixedMode(MixedMode.All)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }
}
