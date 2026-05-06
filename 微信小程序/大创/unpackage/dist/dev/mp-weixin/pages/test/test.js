"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_auth = require("../../utils/auth.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  __name: "test",
  setup(__props) {
    const userInfo = common_vendor.reactive({
      username: "",
      userType: ""
    });
    const testResult = common_vendor.ref("等待测试...");
    const isUserLoggedIn = common_vendor.computed(() => {
      return utils_auth.isLoggedIn();
    });
    const getUserTypeText = () => {
      if (!userInfo.userType)
        return "";
      const typeMap = {
        "MEMBER": "正式党员",
        "ACTIVIST": "积极分子",
        "PREPARE": "预备党员",
        "ADMIN": "管理员",
        "SUPER_ADMIN": "超级管理员"
      };
      return typeMap[userInfo.userType] || userInfo.userType;
    };
    const testLogin = () => {
      testResult.value = "正在测试登录功能...";
      setTimeout(() => {
        if (isUserLoggedIn.value) {
          testResult.value = "✅ 登录测试通过：用户已登录";
        } else {
          testResult.value = "❌ 登录测试失败：用户未登录，请先登录";
        }
      }, 1e3);
    };
    const testLogout = async () => {
      testResult.value = "正在测试登出功能...";
      try {
        await utils_auth.logout();
        testResult.value = "✅ 登出测试通过：已成功登出";
      } catch (error) {
        testResult.value = `❌ 登出测试失败：${error.message}`;
      }
    };
    const testRefreshToken = async () => {
      testResult.value = "正在测试令牌刷新功能...";
      try {
        await utils_auth.refreshAccessToken();
        testResult.value = "✅ 令牌刷新测试通过：令牌已刷新";
      } catch (error) {
        testResult.value = `❌ 令牌刷新测试失败：${error.message}`;
      }
    };
    const testAPI = async () => {
      testResult.value = "正在测试API调用功能...";
      try {
        const response = await utils_api.userAPI.getUserInfo();
        if (response.statusCode === 200) {
          testResult.value = "✅ API调用测试通过：成功获取用户信息";
        } else {
          testResult.value = `❌ API调用测试失败：状态码 ${response.statusCode}`;
        }
      } catch (error) {
        testResult.value = `❌ API调用测试失败：${error.message}`;
      }
    };
    const loadUserInfo = () => {
      const user = utils_auth.getUserInfo();
      if (user) {
        Object.assign(userInfo, user);
      } else {
        Object.assign(userInfo, {
          username: "",
          userType: ""
        });
      }
    };
    common_vendor.onMounted(() => {
      loadUserInfo();
    });
    common_vendor.onShow(() => {
      loadUserInfo();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(isUserLoggedIn.value ? "已登录" : "未登录"),
        b: isUserLoggedIn.value ? 1 : "",
        c: !isUserLoggedIn.value ? 1 : "",
        d: userInfo.username
      }, userInfo.username ? {
        e: common_vendor.t(userInfo.username)
      } : {}, {
        f: userInfo.userType
      }, userInfo.userType ? {
        g: common_vendor.t(getUserTypeText())
      } : {}, {
        h: common_vendor.o(testLogin, "a4"),
        i: common_vendor.o(testLogout, "b7"),
        j: common_vendor.o(testRefreshToken, "c2"),
        k: common_vendor.o(testAPI, "a3"),
        l: common_vendor.t(testResult.value)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-727d09f0"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/test.js.map
