"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const utils_auth = require("../../utils/auth.js");
const utils_eventBus = require("../../utils/eventBus.js");
const _sfc_main = {
  __name: "my",
  setup(__props) {
    const userInfo = common_vendor.reactive({
      username: "",
      userType: "",
      avatar: ""
    });
    const isUserLoggedIn = common_vendor.computed(() => {
      return userInfo.username && userInfo.username.trim() !== "";
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
    const handleUserClick = () => {
      if (!isUserLoggedIn.value) {
        common_vendor.index.navigateTo({
          url: "/pages/my/login/login",
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/my/my.vue:81", "跳转登录页面失败:", err);
            common_vendor.index.showToast({
              title: "页面跳转失败",
              icon: "none"
            });
          }
        });
      }
    };
    const handleLogout = () => {
      common_vendor.index.showModal({
        title: "确认退出",
        content: "确定要退出登录吗？",
        success: (res) => {
          if (res.confirm) {
            utils_auth.logout();
            loadUserInfo();
            common_vendor.index.showToast({
              title: "已退出登录",
              icon: "success",
              duration: 1500
            });
          }
        }
      });
    };
    function goMyActivity() {
      if (!isUserLoggedIn.value) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        return;
      }
      common_vendor.index.navigateTo({
        url: "/pages/my/myActivity"
      });
    }
    function goPointsRank() {
      if (!isUserLoggedIn.value) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        return;
      }
      common_vendor.index.navigateTo({
        url: "/pages/my/pointsRank"
      });
    }
    function goChangePassword() {
      if (!isUserLoggedIn.value) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        return;
      }
      common_vendor.index.navigateTo({
        url: "/pages/my/changePassword"
      });
    }
    const loadUserInfo = () => {
      const user = utils_auth.getUserInfo();
      if (user) {
        Object.assign(userInfo, user);
      } else {
        Object.assign(userInfo, {
          username: "",
          userType: "",
          avatar: ""
        });
      }
    };
    let loginListener = null;
    let logoutListener = null;
    let userInfoUpdateListener = null;
    common_vendor.onMounted(() => {
      loadUserInfo();
      loginListener = (userData) => {
        common_vendor.index.__f__("log", "at pages/my/my.vue:197", "收到登录事件，更新用户信息:", userData);
        Object.assign(userInfo, userData);
      };
      logoutListener = () => {
        common_vendor.index.__f__("log", "at pages/my/my.vue:203", "收到退出登录事件，清除用户信息");
        Object.assign(userInfo, {
          username: "",
          userType: "",
          avatar: ""
        });
      };
      userInfoUpdateListener = (updatedInfo) => {
        common_vendor.index.__f__("log", "at pages/my/my.vue:213", "收到用户信息更新事件:", updatedInfo);
        Object.assign(userInfo, updatedInfo);
      };
      utils_eventBus.eventBus.on(utils_eventBus.EVENTS.USER_LOGIN, loginListener);
      utils_eventBus.eventBus.on(utils_eventBus.EVENTS.USER_LOGOUT, logoutListener);
      utils_eventBus.eventBus.on(utils_eventBus.EVENTS.USER_INFO_UPDATED, userInfoUpdateListener);
    });
    common_vendor.onShow(() => {
      loadUserInfo();
    });
    common_vendor.onUnmounted(() => {
      if (loginListener) {
        utils_eventBus.eventBus.off(utils_eventBus.EVENTS.USER_LOGIN, loginListener);
      }
      if (logoutListener) {
        utils_eventBus.eventBus.off(utils_eventBus.EVENTS.USER_LOGOUT, logoutListener);
      }
      if (userInfoUpdateListener) {
        utils_eventBus.eventBus.off(utils_eventBus.EVENTS.USER_INFO_UPDATED, userInfoUpdateListener);
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: userInfo.avatar || "/static/my/头像.png",
        b: common_vendor.t(userInfo.username || "点击登录"),
        c: common_vendor.t(getUserTypeText()),
        d: isUserLoggedIn.value
      }, isUserLoggedIn.value ? {
        e: common_vendor.o(handleLogout, "2b")
      } : {}, {
        f: common_vendor.o(handleUserClick, "f1"),
        g: common_assets._imports_0$1,
        h: common_assets._imports_1$1,
        i: common_vendor.o(goMyActivity, "e5"),
        j: common_assets._imports_2$1,
        k: common_vendor.o(goPointsRank, "dd"),
        l: common_assets._imports_1$1,
        m: common_vendor.o(goChangePassword, "31")
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2f1ef635"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/my/my.js.map
