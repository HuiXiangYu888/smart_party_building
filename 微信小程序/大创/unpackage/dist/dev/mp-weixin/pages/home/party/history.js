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
    const formatTime = (t) => {
      if (!t)
        return "-";
      return String(t).replace("T", " ").substring(0, 19);
    };
    const truncate = (str, len) => {
      if (!str)
        return "";
      return str.length > len ? str.substring(0, len) + "..." : str;
    };
    const goDetail = (item) => {
      getApp().globalData = getApp().globalData || {};
      getApp().globalData.__historyDetail = item;
      common_vendor.index.navigateTo({
        url: "/pages/home/party/historyDetail"
      });
    };
    common_vendor.onMounted(async () => {
      var _a;
      try {
        const resRaw = await utils_api.applicationAPI.getMyHistory();
        const res = Array.isArray(resRaw) ? resRaw[0] : resRaw;
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          list.value = res.data.data || [];
        } else {
          common_vendor.index.__f__("error", "at pages/home/party/history.vue:74", "获取申请记录失败:", res);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/party/history.vue:77", "获取申请记录异常:", e);
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
          return common_vendor.e({
            a: common_vendor.t(it.name || "-"),
            b: common_vendor.t(it.studentId || "-"),
            c: common_vendor.t(it.status),
            d: common_vendor.n(tagClass(it.status)),
            e: common_vendor.t(it.type),
            f: common_vendor.t(formatTime(it.submittedAt)),
            g: it.reviewedAt
          }, it.reviewedAt ? {
            h: common_vendor.t(formatTime(it.reviewedAt))
          } : {}, {
            i: it.details
          }, it.details ? {
            j: common_vendor.t(truncate(it.details, 60))
          } : {}, {
            k: i,
            l: common_vendor.o(($event) => goDetail(it), i)
          });
        })
      }));
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-5d1739a2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/home/party/history.js.map
