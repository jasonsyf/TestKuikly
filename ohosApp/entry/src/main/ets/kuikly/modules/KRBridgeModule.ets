import { common } from '@kit.AbilityKit';
import { BusinessError } from '@kit.BasicServicesKit';
import { http } from '@kit.NetworkKit';
import { KRAny } from '@kuikly-open/render';
import { KuiklyRenderBaseModule } from '@kuikly-open/render';
import { KuiklyRenderCallback } from '@kuikly-open/render';
import util from '@ohos.util';
import fs from '@ohos.file.fs';
import { fileUri } from '@kit.CoreFileKit';
import router from '@ohos.router';

export class KRBridgeModule extends KuiklyRenderBaseModule {
  static readonly MODULE_NAME = "HRBridgeModule";
  static readonly SHOW_ALERT = "showAlert";
  static readonly CLOSE_PAGE = "closePage";
  static readonly OPEN_PAGE = "openPage";
  static readonly COPY_TO_PASTE_BOARD = "copyToPasteboard";
  static readonly TOAST = "toast";
  static readonly LOG = "log";
  static readonly REPORT_DT = "reportDT";
  static readonly REPORT_REAL_TIME = "reportRealtime";
  static readonly LOCAL_SERVE_TIME = "localServeTime";
  static readonly CURRENT_TIME_STAMP = "currentTimestamp";
  static readonly DATE_FORMATTER = "dateFormatter";
  static readonly READ_ASSET_FILE = "readAssetFile";
  static readonly HTTP_REQUEST = "httpRequest";

  syncMode(): boolean {
    return false;
  }

  call(method: string, params: KRAny, callback: KuiklyRenderCallback): KRAny {
    switch (method) {
      case KRBridgeModule.SHOW_ALERT: {
        this.showAlert(params, callback);
        break;
      }
      case KRBridgeModule.CLOSE_PAGE: {
        this.closePage();
        break;
      }
      case KRBridgeModule.OPEN_PAGE: {
        this.openPage(params);
        break;
      }
      case KRBridgeModule.COPY_TO_PASTE_BOARD: {
        this.copyToPasteboard();
        break;
      }
      case KRBridgeModule.TOAST: {
        this.toast();
        break;
      }
      case KRBridgeModule.LOG: {
        this.log(params);
        break;
      }
      case KRBridgeModule.REPORT_DT: {
        this.reportDT(params);
        break;
      }
      case KRBridgeModule.REPORT_REAL_TIME: {
        this.reportRealtime(params);
        break;
      }
      case KRBridgeModule.LOCAL_SERVE_TIME: {
        this.localServeTime(params, callback);
        break;
      }
      case KRBridgeModule.CURRENT_TIME_STAMP: {
        return this.currentTimestamp();
      }
      case KRBridgeModule.DATE_FORMATTER: {
        return this.dateFormatter(params);
      }
      case KRBridgeModule.READ_ASSET_FILE: {
        this.readAssetFile(params, callback);
        break;
      }
      case KRBridgeModule.HTTP_REQUEST: {
        this.httpRequest(params, callback);
        break;
      }
      default:
        break;
    }
    return null;
  }

  private showAlert(params: KRAny, callback: KuiklyRenderCallback) {
  }

  // TODO close page
  private closePage() {
  }

  // Open detail page in app WebView
  private openPage(params: KRAny) {
    if (params == null) {
      return
    }
    let records = JSON.parse(params as string) as Record<string, string>;
    let url = records["url"] || "";
    let title = records["title"] || "";
    if (!url) {
      return;
    }
    router.pushUrl({
      url: 'pages/WebViewPage',
      params: { url: url, title: title }
    });
  }

  // TODO copy to paste board
  private copyToPasteboard() {
  }

  // TODO open toast
  private toast() {
  }

  private log(params: KRAny) {
    if (params == null) {
      return
    }
    console.log(`KuiklyRender ${JSON.stringify(params)}`,)
  }

  private reportDT(params: KRAny) {
  }

  private reportRealtime(params: KRAny) {
  }

  private localServeTime(params: KRAny, callback: KuiklyRenderCallback) {
    callback({ "time": Date.now() / 1000.0 });
  }

  private currentTimestamp(): string {
    return Date.now().toString()
  }

  // TODO format date
  private dateFormatter(params: KRAny): string {
    console.log(`========== ${params}`)
    return "";
  }

  private readAssetFile(params: KRAny, callback: KuiklyRenderCallback) {
    // let startTimeMills = Date.now();
    let records = JSON.parse(params as string) as Record<string, string>;
    let assetPath = records["assetPath"];

    let context = getContext(this) as common.UIAbilityContext;
    /*
    context.resourceManager.getRawFileContent(assetPath, (error: BusinessError, value: Uint8Array) => {
      let textDecoder: util.TextDecoder = util.TextDecoder.create();
      let res: string = textDecoder.decodeWithStream(value);
      callback({
        "result": JSON.parse(res)
      });
    });
    */
    let resDir = context.resourceDir
    let path = resDir + '/' + assetPath
    let assetURI = fileUri.getUriFromPath(path)
    const file = fs.openSync(assetURI, fs.OpenMode.READ_ONLY);
    const stat = fs.statSync(file.fd);
    const arrayBuffer = new ArrayBuffer(stat.size);
    fs.read(file.fd, arrayBuffer, (err: BusinessError, readLen: number) => {
      if (err) {
        console.error("read failed with error message: " + err.message + ", error code: " + err.code);
      } else {
        let buf = new Uint8Array(arrayBuffer, 0, readLen);
        const textDecoder = new util.TextDecoder('utf-8');
        const res = textDecoder.decodeWithStream(new Uint8Array(buf));
        callback({
          'result': JSON.parse(res)
        });
      }
      fs.closeSync(file);
    });
  }

  private httpRequest(params: KRAny, callback: KuiklyRenderCallback) {
    let records = JSON.parse(params as string) as Record<string, string>;
    let method = records["method"] || "GET";
    let url = records["url"] || "";
    let body = records["body"];

    let requestMethod = http.RequestMethod.GET;
    if (method === "POST") {
      requestMethod = http.RequestMethod.POST;
    } else if (method === "PUT") {
      requestMethod = http.RequestMethod.PUT;
    } else if (method === "DELETE") {
      requestMethod = http.RequestMethod.DELETE;
    }

    let httpRequest = http.createHttp();
    httpRequest.request(url, {
      method: requestMethod,
      extraData: body,
      header: {
        'Content-Type': 'application/json'
      }
    }).then((response: http.HttpResponse) => {
      let result = response.result as string;
      callback(result);
      httpRequest.destroy();
    }).catch((err: BusinessError) => {
      console.error(`HTTP request failed: ${err.message}`);
      let errorResult = JSON.stringify({
        errorCode: -1,
        errorMsg: `HTTP request failed: ${err.message}`,
        data: null
      });
      callback(errorResult);
      httpRequest.destroy();
    });
  }
}