"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  __name: "party",
  setup(__props) {
    const currentStage = common_vendor.ref("未递交入党申请");
    const computeStage = (records = []) => {
      const hasPrepareApproved = records.some((r) => {
        var _a;
        return ((_a = r.type) == null ? void 0 : _a.includes("预备")) && r.status === "已通过";
      });
      if (hasPrepareApproved)
        return "预备党员";
      const hasPositiveApproved = records.some((r) => {
        var _a;
        return ((_a = r.type) == null ? void 0 : _a.includes("积极分子")) && r.status === "已通过";
      });
      if (hasPositiveApproved)
        return "入党积极分子";
      const hasPartySubmitted = records.some((r) => {
        var _a;
        return (_a = r.type) == null ? void 0 : _a.includes("入党申请");
      });
      if (hasPartySubmitted)
        return "已递交入党申请";
      return "未递交入党申请";
    };
    const loadStage = async () => {
      var _a;
      try {
        const resRaw = await utils_api.applicationAPI.getMyHistory();
        const res = Array.isArray(resRaw) ? resRaw[0] : resRaw;
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          const list = res.data.data || [];
          currentStage.value = computeStage(list);
        } else {
          currentStage.value = "未递交入党申请";
        }
      } catch {
        currentStage.value = "未递交入党申请";
      }
    };
    common_vendor.onMounted(loadStage);
    function stageClass(s) {
      if (s === "预备党员")
        return "stage-success";
      if (s === "入党积极分子")
        return "stage-info";
      if (s === "已递交入党申请")
        return "stage-warning";
      return "stage-default";
    }
    function goPartyApplication() {
      common_vendor.index.navigateTo({ url: "/pages/home/party/partyApplication" }).then(() => {
      }).catch((err) => {
        common_vendor.index.__f__("error", "at pages/home/party.vue:81", "导航到申请提交失败:", err);
        common_vendor.index.showToast({ title: "跳转失败，稍后重试", icon: "none" });
      });
    }
    function goHistory() {
      common_vendor.index.navigateTo({ url: "/pages/home/party/history" }).then(() => {
      }).catch((err) => {
        common_vendor.index.__f__("error", "at pages/home/party.vue:89", "导航到申请记录失败:", err);
        common_vendor.index.showToast({ title: "跳转失败，稍后重试", icon: "none" });
      });
    }
    return (_ctx, _cache) => {
      return {
        a: common_assets._imports_0$2,
        b: common_vendor.o(goHistory, "c6"),
        c: common_assets._imports_0$2,
        d: common_vendor.o(goPartyApplication, "25"),
        e: common_vendor.t(currentStage.value),
        f: common_vendor.n(stageClass(currentStage.value))
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-4a377f68"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/party.js.map
