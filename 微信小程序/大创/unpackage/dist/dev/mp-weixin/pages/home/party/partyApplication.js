"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_api = require("../../../utils/api.js");
const _sfc_main = {
  __name: "partyApplication",
  setup(__props) {
    const formData = common_vendor.ref({
      partyApplication: ""
    });
    const attachments = common_vendor.ref([]);
    const submitting = common_vendor.ref(false);
    const typeOptions = ["入党申请", "积极分子申请", "预备党员申请"];
    const selectedType = common_vendor.ref("");
    const onTypeChange = (e) => {
      const idx = Number(e.detail.value);
      selectedType.value = typeOptions[idx];
    };
    const chooseImage = () => {
      common_vendor.index.chooseImage({
        count: 6,
        success: async (res) => {
          for (const fp of res.tempFilePaths) {
            try {
              const { url } = await utils_api.uploadAPI.uploadFile(fp);
              attachments.value.push(url);
            } catch (e) {
              common_vendor.index.showToast({ title: "上传失败", icon: "none" });
            }
          }
        }
      });
    };
    const removeAttachment = (i) => {
      attachments.value.splice(i, 1);
    };
    const onSubmit = async () => {
      var _a;
      if (!selectedType.value) {
        common_vendor.index.showToast({ title: "请选择申请类型", icon: "none" });
        return;
      }
      if (!formData.value.partyApplication) {
        common_vendor.index.showToast({ title: "请填写申请内容", icon: "none" });
        return;
      }
      try {
        if (submitting.value)
          return;
        submitting.value = true;
        common_vendor.index.showLoading({ title: "提交中..." });
        const details = `${formData.value.partyApplication}`;
        let respRaw;
        if (selectedType.value === "入党申请") {
          respRaw = await utils_api.applicationAPI.applyParty(details, attachments.value);
        } else if (selectedType.value === "积极分子申请") {
          respRaw = await utils_api.applicationAPI.applyPositive(details, attachments.value);
        } else {
          const today = (/* @__PURE__ */ new Date()).toISOString().slice(0, 10);
          respRaw = await utils_api.applicationAPI.applyPrepare(details, today + "T00:00:00", today + "T00:00:00", attachments.value);
        }
        const resp = Array.isArray(respRaw) ? respRaw[0] : respRaw;
        if (resp.statusCode === 200 && ((_a = resp.data) == null ? void 0 : _a.code) === 200) {
          common_vendor.index.showToast({ title: "已提交", icon: "success" });
          setTimeout(() => common_vendor.index.navigateBack(), 800);
        } else {
          const msg = resp.data && (resp.data.message || resp.data.msg) ? `${resp.data.message || resp.data.msg}` : `提交失败(${resp.statusCode})`;
          common_vendor.index.__f__("error", "at pages/home/party/partyApplication.vue:99", "提交失败:", resp);
          common_vendor.index.showToast({ title: msg, icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/party/partyApplication.vue:103", "提交异常:", e);
        common_vendor.index.showToast({ title: (e == null ? void 0 : e.message) || "提交失败", icon: "none" });
      } finally {
        submitting.value = false;
        common_vendor.index.hideLoading();
      }
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(selectedType.value || "请选择申请类型"),
        b: typeOptions,
        c: common_vendor.o(onTypeChange, "33"),
        d: formData.value.partyApplication,
        e: common_vendor.o(($event) => formData.value.partyApplication = $event.detail.value, "be"),
        f: common_vendor.f(attachments.value, (u, i, i0) => {
          return {
            a: u,
            b: common_vendor.o(($event) => removeAttachment(i), i),
            c: i
          };
        }),
        g: common_vendor.o(chooseImage, "91"),
        h: common_vendor.o(onSubmit, "ad")
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-35dc8692"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/home/party/partyApplication.js.map
