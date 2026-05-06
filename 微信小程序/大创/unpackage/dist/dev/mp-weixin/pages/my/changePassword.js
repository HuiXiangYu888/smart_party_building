"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  __name: "changePassword",
  setup(__props) {
    const formData = common_vendor.reactive({
      oldPassword: "",
      newPassword: "",
      confirmPassword: ""
    });
    const handleSubmit = async () => {
      if (!formData.oldPassword) {
        common_vendor.index.showToast({
          title: "请输入原密码",
          icon: "none"
        });
        return;
      }
      if (!formData.newPassword) {
        common_vendor.index.showToast({
          title: "请输入新密码",
          icon: "none"
        });
        return;
      }
      if (formData.newPassword.length < 6) {
        common_vendor.index.showToast({
          title: "新密码长度不能少于6位",
          icon: "none"
        });
        return;
      }
      if (formData.newPassword !== formData.confirmPassword) {
        common_vendor.index.showToast({
          title: "两次输入的密码不一致",
          icon: "none"
        });
        return;
      }
      common_vendor.index.showLoading({
        title: "修改中..."
      });
      try {
        const response = await utils_api.userAPI.changePassword({
          oldPassword: formData.oldPassword,
          newPassword: formData.newPassword
        });
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "密码修改成功",
          icon: "success"
        });
        formData.oldPassword = "";
        formData.newPassword = "";
        formData.confirmPassword = "";
        setTimeout(() => {
          common_vendor.index.navigateBack();
        }, 1500);
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: error.message || "修改失败",
          icon: "none"
        });
      }
    };
    return (_ctx, _cache) => {
      return {
        a: formData.oldPassword,
        b: common_vendor.o(($event) => formData.oldPassword = $event.detail.value, "48"),
        c: formData.newPassword,
        d: common_vendor.o(($event) => formData.newPassword = $event.detail.value, "cc"),
        e: formData.confirmPassword,
        f: common_vendor.o(($event) => formData.confirmPassword = $event.detail.value, "0e"),
        g: common_vendor.o(handleSubmit, "77")
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a5686c7f"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/my/changePassword.js.map
