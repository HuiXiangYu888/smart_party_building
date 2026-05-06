"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_auth = require("../../utils/auth.js");
const _sfc_main = {
  __name: "record",
  setup(__props) {
    const records = common_vendor.ref([]);
    const totalDuration = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const loadStudyRecords = async () => {
      var _a, _b;
      loading.value = true;
      try {
        const res = await utils_auth.requestWithAuth({ url: "/study/records/user", method: "GET" });
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          records.value = res.data.data || [];
        } else if (res.statusCode === 401 || ((_b = res.data) == null ? void 0 : _b.code) === 401) {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          setTimeout(() => common_vendor.index.navigateTo({ url: "/pages/my/login/login" }), 600);
        }
      } catch (e) {
        common_vendor.index.showToast({ title: (e == null ? void 0 : e.message) || "网络错误", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const loadTotalDuration = async () => {
      var _a;
      try {
        const res = await utils_auth.requestWithAuth({ url: "/study/records/user/total-duration", method: "GET" });
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          totalDuration.value = res.data.data || 0;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/my/record.vue:73", "加载总学习时长失败:", e);
      }
    };
    const formatTime = (timeStr) => {
      if (!timeStr)
        return "";
      try {
        const date = new Date(timeStr);
        return date.toLocaleString("zh-CN", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit"
        });
      } catch {
        return timeStr;
      }
    };
    const formatDuration = (minutes) => {
      if (!minutes || minutes === 0)
        return "0分钟";
      const hours = Math.floor(minutes / 60);
      const mins = minutes % 60;
      if (hours > 0) {
        return `${hours}小时${mins}分钟`;
      }
      return `${mins}分钟`;
    };
    common_vendor.onMounted(() => {
      loadStudyRecords();
      loadTotalDuration();
    });
    common_vendor.onShow(() => {
      loadStudyRecords();
      loadTotalDuration();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(formatDuration(totalDuration.value)),
        b: common_vendor.f(records.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.title || item.videoTitle || "" || "未知视频"),
            b: common_vendor.t(formatTime(item.startedAt)),
            c: common_vendor.t(formatDuration(item.duration)),
            d: index
          };
        }),
        c: records.value.length === 0
      }, records.value.length === 0 ? {} : {}, {
        d: loading.value
      }, loading.value ? {} : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d7595071"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/my/record.js.map
