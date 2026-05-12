"use strict";
const common_vendor = require("../../../common/vendor.js");
const _sfc_main = {
  __name: "historyDetail",
  setup(__props) {
    const detail = common_vendor.ref({});
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
    const previewImage = (index) => {
      const urls = detail.value.attachments || [];
      common_vendor.index.previewImage({
        current: index,
        urls
      });
    };
    common_vendor.onMounted(() => {
      const app = getApp();
      if (app && app.globalData && app.globalData.__historyDetail) {
        detail.value = app.globalData.__historyDetail;
        delete app.globalData.__historyDetail;
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(detail.value.type),
        b: common_vendor.t(detail.value.status),
        c: common_vendor.n(tagClass(detail.value.status)),
        d: common_vendor.t(detail.value.name || "-"),
        e: common_vendor.t(detail.value.studentId || "-"),
        f: common_vendor.t(formatTime(detail.value.submittedAt)),
        g: detail.value.reviewedAt
      }, detail.value.reviewedAt ? {
        h: common_vendor.t(formatTime(detail.value.reviewedAt))
      } : {}, {
        i: detail.value.details
      }, detail.value.details ? {
        j: common_vendor.t(detail.value.details)
      } : {}, {
        k: detail.value.attachments && detail.value.attachments.length > 0
      }, detail.value.attachments && detail.value.attachments.length > 0 ? {
        l: common_vendor.t(detail.value.attachments.length),
        m: common_vendor.f(detail.value.attachments, (url, i, i0) => {
          return {
            a: i,
            b: url,
            c: common_vendor.o(($event) => previewImage(i), i)
          };
        })
      } : {}, {
        n: detail.value.comments
      }, detail.value.comments ? {
        o: common_vendor.t(detail.value.comments)
      } : {
        p: common_vendor.t(detail.value.status === "待审核" ? "等待审核中..." : "无审核意见")
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-b767923f"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/home/party/historyDetail.js.map
