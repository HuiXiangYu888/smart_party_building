"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_auth = require("../../utils/auth.js");
const _sfc_main = {
  __name: "pointsRank",
  setup(__props) {
    const list = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const loadData = async () => {
      var _a, _b, _c;
      if (loading.value)
        return;
      loading.value = true;
      try {
        common_vendor.index.__f__("log", "at pages/my/pointsRank.vue:26", "🔍 开始加载积分排行");
        const res = await utils_auth.requestWithAuth({ url: "/points/rank", method: "GET" });
        common_vendor.index.__f__("log", "at pages/my/pointsRank.vue:28", "📊 积分排行响应:", res);
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          list.value = (res.data.data || []).map((x) => ({
            userId: x.userId,
            username: x.username,
            branchName: x.branchName,
            totalPoints: Number(x.totalPoints || 0)
          }));
          common_vendor.index.__f__("log", "at pages/my/pointsRank.vue:37", "✅ 积分排行加载成功，记录数:", list.value.length);
        } else {
          common_vendor.index.__f__("error", "at pages/my/pointsRank.vue:39", "❌ 积分排行加载失败:", (_b = res.data) == null ? void 0 : _b.message);
          common_vendor.index.showToast({ title: ((_c = res.data) == null ? void 0 : _c.message) || "加载失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/my/pointsRank.vue:43", "❌ 积分排行加载异常:", e);
        common_vendor.index.showToast({ title: "网络错误", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    common_vendor.onMounted(loadData);
    const getRankClass = (idx) => {
      if (idx === 0)
        return "first";
      if (idx === 1)
        return "second";
      if (idx === 2)
        return "third";
      return "";
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(list.value, (it, idx, i0) => {
          return {
            a: common_vendor.t(idx + 1),
            b: common_vendor.t(it.username || "-"),
            c: common_vendor.t(it.branchName || "-"),
            d: common_vendor.t(it.totalPoints),
            e: common_vendor.n(getRankClass(idx)),
            f: it.userId
          };
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d140bf87"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/my/pointsRank.js.map
