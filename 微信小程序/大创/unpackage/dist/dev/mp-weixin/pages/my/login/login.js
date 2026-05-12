"use strict";
const common_vendor = require("../../../common/vendor.js");
const common_assets = require("../../../common/assets.js");
const utils_auth = require("../../../utils/auth.js");
const utils_api = require("../../../utils/api.js");
const _sfc_main = {
  __name: "login",
  setup(__props) {
    const isLoading = common_vendor.ref(false);
    const loginForm = common_vendor.ref({
      username: "",
      // 用户名（学号或身份证号）
      password: ""
    });
    const isFormValid = common_vendor.computed(() => {
      return loginForm.value.username.trim() && loginForm.value.password.trim();
    });
    const handleLogin = async () => {
      if (isLoading.value || !isFormValid.value)
        return;
      isLoading.value = true;
      try {
        const loginData = {
          username: loginForm.value.username.trim(),
          password: loginForm.value.password
        };
        const response = await utils_api.userAPI.login(loginData);
        if (response.statusCode === 200 && response.data.code === 200) {
          utils_auth.saveLoginInfo(response.data.data);
          common_vendor.index.showToast({
            title: "登录成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.switchTab({
              url: "/pages/home/home"
            });
          }, 1500);
        } else {
          throw new Error(response.data.message || "登录失败");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/my/login/login.vue:117", "登录错误:", error);
        common_vendor.index.showToast({
          title: error.message || "登录失败，请重试",
          icon: "none"
        });
      } finally {
        isLoading.value = false;
      }
    };
    common_vendor.onMounted(() => {
      if (utils_auth.isLoggedIn()) {
        common_vendor.index.switchTab({
          url: "/pages/home/home"
        });
      }
    });
    return (_ctx, _cache) => {
      return {
        a: common_assets._imports_0$3,
        b: loginForm.value.username,
        c: common_vendor.o(($event) => loginForm.value.username = $event.detail.value, "e4"),
        d: loginForm.value.password,
        e: common_vendor.o(($event) => loginForm.value.password = $event.detail.value, "f2"),
        f: common_vendor.t(isLoading.value ? "登录中..." : "立即登录"),
        g: common_vendor.o(handleLogin, "ef"),
        h: isLoading.value || !isFormValid.value
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-dd394eb5"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/my/login/login.js.map
