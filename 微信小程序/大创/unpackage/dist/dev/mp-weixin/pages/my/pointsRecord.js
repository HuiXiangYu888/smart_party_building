"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_auth = require("../../utils/auth.js");
const _sfc_main = {
  __name: "pointsRecord",
  setup(__props) {
    const pointsList = common_vendor.ref([]);
    const totalPoints = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const loadPointsList = async () => {
      var _a, _b, _c, _d;
      loading.value = true;
      try {
        common_vendor.index.__f__("log", "at pages/my/pointsRecord.vue:56", "🔍 开始加载积分明细");
        const res = await utils_auth.requestWithAuth({ url: "/points/user/list", method: "GET" });
        common_vendor.index.__f__("log", "at pages/my/pointsRecord.vue:58", "📊 积分明细响应:", res);
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          pointsList.value = res.data.data || [];
          common_vendor.index.__f__("log", "at pages/my/pointsRecord.vue:62", "✅ 积分明细加载成功，记录数:", pointsList.value.length);
        } else if (res.statusCode === 401 || ((_b = res.data) == null ? void 0 : _b.code) === 401) {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          setTimeout(() => common_vendor.index.navigateTo({ url: "/pages/my/login/login" }), 600);
        } else {
          common_vendor.index.__f__("error", "at pages/my/pointsRecord.vue:67", "❌ 积分明细加载失败:", (_c = res.data) == null ? void 0 : _c.message);
          common_vendor.index.showToast({ title: ((_d = res.data) == null ? void 0 : _d.message) || "加载失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/my/pointsRecord.vue:71", "❌ 积分明细加载异常:", e);
        common_vendor.index.showToast({ title: (e == null ? void 0 : e.message) || "网络错误", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const loadTotalPoints = async () => {
      var _a, _b, _c;
      try {
        common_vendor.index.__f__("log", "at pages/my/pointsRecord.vue:81", "🔍 开始加载总积分");
        const res = await utils_auth.requestWithAuth({ url: "/points/user/summary", method: "GET" });
        common_vendor.index.__f__("log", "at pages/my/pointsRecord.vue:83", "📊 总积分响应:", res);
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          totalPoints.value = ((_b = res.data.data) == null ? void 0 : _b.total) || 0;
          common_vendor.index.__f__("log", "at pages/my/pointsRecord.vue:87", "✅ 总积分加载成功:", totalPoints.value);
        } else {
          common_vendor.index.__f__("error", "at pages/my/pointsRecord.vue:89", "❌ 总积分加载失败:", (_c = res.data) == null ? void 0 : _c.message);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/my/pointsRecord.vue:92", "❌ 总积分加载异常:", e);
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
    common_vendor.onMounted(() => {
      loadPointsList();
      loadTotalPoints();
    });
    common_vendor.onShow(() => {
      loadPointsList();
      loadTotalPoints();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(totalPoints.value),
        b: common_vendor.f(pointsList.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.description),
            b: common_vendor.t(item.points > 0 ? "+" : ""),
            c: common_vendor.t(item.points),
            d: common_vendor.n(item.points > 0 ? "positive" : "negative"),
            e: common_vendor.t(formatTime(item.createdAt)),
            f: index
          };
        }),
        c: pointsList.value.length === 0 && !loading.value
      }, pointsList.value.length === 0 && !loading.value ? {} : {}, {
        d: loading.value
      }, loading.value ? {} : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-0f2662e9"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/my/pointsRecord.js.map
