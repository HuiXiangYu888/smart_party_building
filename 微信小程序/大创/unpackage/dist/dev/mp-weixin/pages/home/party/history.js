"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_api = require("../../../utils/api.js");
const _sfc_main = {
  __name: "history",
  setup(__props) {
    const loading = common_vendor.ref(true);
    const list = common_vendor.ref([]);
    const tagClass = (s) => {
      if (s === "已通过")
        return "success";
      if (s === "待审核")
        return "warning";
      if (s === "已拒绝")
        return "danger";
      return "info";
    };
    common_vendor.onMounted(async () => {
      var _a;
      try {
        const resRaw = await utils_api.applicationAPI.getMyHistory();
        const res = Array.isArray(resRaw) ? resRaw[0] : resRaw;
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          list.value = res.data.data || [];
        } else {
          common_vendor.index.__f__("error", "at pages/home/party/history.vue:44", "获取申请记录失败:", res);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/party/history.vue:47", "获取申请记录异常:", e);
      }
      loading.value = false;
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : common_vendor.e({
        b: list.value.length === 0
      }, list.value.length === 0 ? {} : {
        c: common_vendor.f(list.value, (it, i, i0) => {
          return {
            a: common_vendor.t(it.name || "-"),
            b: common_vendor.t(it.studentId || "-"),
            c: common_vendor.t(it.type),
            d: common_vendor.t(it.submittedAt || "-"),
            e: common_vendor.t(it.status),
            f: common_vendor.n(tagClass(it.status)),
            g: i
          };
        })
      }));
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-5d1739a2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/home/party/history.js.map
