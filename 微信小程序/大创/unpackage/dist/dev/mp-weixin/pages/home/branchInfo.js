"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_auth = require("../../utils/auth.js");
const _sfc_main = {
  __name: "branchInfo",
  setup(__props) {
    const form = common_vendor.reactive({
      id: void 0,
      name: "",
      memberCount: 0,
      leaderId: void 0,
      leaderName: "",
      leaderMobile: ""
    });
    const loading = common_vendor.ref(false);
    const error = common_vendor.ref("");
    const fetchBranch = async (branchId) => {
      var _a, _b;
      loading.value = true;
      error.value = "";
      try {
        common_vendor.index.__f__("log", "at pages/home/branchInfo.vue:59", "正在请求支部信息，支部ID:", branchId);
        const res = await utils_auth.requestWithAuth({ url: `/branches/${branchId}`, method: "GET" });
        common_vendor.index.__f__("log", "at pages/home/branchInfo.vue:61", "支部信息响应:", res);
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          const d = res.data.data || {};
          form.id = d.id;
          form.name = d.name;
          form.memberCount = d.memberCount;
          form.leaderId = d.leaderId;
          form.leaderName = d.leaderName;
          form.leaderMobile = d.leaderMobile;
          common_vendor.index.__f__("log", "at pages/home/branchInfo.vue:72", "支部信息加载成功:", form);
        } else {
          const errorMsg = ((_b = res.data) == null ? void 0 : _b.message) || "加载失败";
          error.value = errorMsg;
          common_vendor.index.showToast({ title: errorMsg, icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/branchInfo.vue:79", "请求支部信息失败:", e);
        const errorMsg = e.message || "网络错误";
        error.value = errorMsg;
        common_vendor.index.showToast({ title: errorMsg, icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const retryLoad = async () => {
      let defaultBranchId = 1;
      try {
        const ui = JSON.parse(common_vendor.index.getStorageSync("userInfo") || "{}");
        if (ui && ui.branchId)
          defaultBranchId = ui.branchId;
      } catch {
      }
      await fetchBranch(defaultBranchId);
    };
    common_vendor.onMounted(async () => {
      let defaultBranchId = 1;
      try {
        const ui = JSON.parse(common_vendor.index.getStorageSync("userInfo") || "{}");
        if (ui && ui.branchId)
          defaultBranchId = ui.branchId;
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/branchInfo.vue:103", "解析用户信息失败:", e);
      }
      await fetchBranch(defaultBranchId);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : error.value ? {
        c: common_vendor.t(error.value),
        d: common_vendor.o(retryLoad, "2a")
      } : {
        e: common_vendor.t(form.name || "-"),
        f: common_vendor.t(form.memberCount ?? "-"),
        g: common_vendor.t(form.leaderName || "-"),
        h: common_vendor.t(form.leaderMobile || "-")
      }, {
        b: error.value
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-193fb5cc"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/branchInfo.js.map
