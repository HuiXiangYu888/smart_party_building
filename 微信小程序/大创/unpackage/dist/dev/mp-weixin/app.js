"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
const utils_auth = require("./utils/auth.js");
if (!Math) {
  "./pages/home/home.js";
  "./pages/task/task.js";
  "./pages/my/my.js";
  "./pages/home/myinfo.js";
  "./pages/home/addMem.js";
  "./pages/home/addActivity.js";
  "./pages/home/branchInfo.js";
  "./pages/home/draft.js";
  "./pages/home/party/partyApplication.js";
  "./pages/home/party/history.js";
  "./pages/my/myActivity.js";
  "./pages/home/party.js";
  "./pages/home/memStudy.js";
  "./pages/home/announcements.js";
  "./pages/home/announcement-detail.js";
  "./pages/home/articles/article1.js";
  "./pages/home/articles/article2.js";
  "./pages/home/articles/article3.js";
  "./pages/home/articles/article4.js";
  "./pages/home/monitor.js";
  "./pages/my/login/login.js";
  "./pages/my/record.js";
  "./pages/my/pointsRank.js";
  "./pages/my/changePassword.js";
  "./pages/my/pointsRecord.js";
  "./pages/test/test.js";
}
const _sfc_main = {
  onLaunch: function() {
    common_vendor.index.__f__("log", "at App.vue:6", "App Launch");
    this.initApp();
  },
  onShow: function() {
    common_vendor.index.__f__("log", "at App.vue:10", "App Show");
  },
  onHide: function() {
    common_vendor.index.__f__("log", "at App.vue:13", "App Hide");
  },
  methods: {
    // 初始化应用
    initApp() {
      if (utils_auth.isLoggedIn()) {
        if (utils_auth.needRefreshToken()) {
          this.refreshTokenSilently();
        }
      } else {
        common_vendor.index.reLaunch({ url: "/pages/my/login/login" });
      }
    },
    // 静默刷新令牌
    async refreshTokenSilently() {
      try {
        await utils_auth.refreshAccessToken();
        common_vendor.index.__f__("log", "at App.vue:34", "令牌刷新成功");
      } catch (error) {
        common_vendor.index.__f__("error", "at App.vue:36", "令牌刷新失败:", error);
        utils_auth.clearLoginInfo();
      }
    }
  }
};
function createApp() {
  const app = common_vendor.createSSRApp(_sfc_main);
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
//# sourceMappingURL=../.sourcemap/mp-weixin/app.js.map
