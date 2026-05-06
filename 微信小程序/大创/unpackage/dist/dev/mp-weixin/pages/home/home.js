"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  __name: "home",
  setup(__props) {
    const pointsTotal = common_vendor.ref(0);
    const recentTasks = common_vendor.ref([]);
    const loadOverview = async () => {
      var _a, _b, _c;
      try {
        const resSum = await utils_api.pointsAPI.getMyPointsSummary();
        pointsTotal.value = ((_b = (_a = resSum == null ? void 0 : resSum.data) == null ? void 0 : _a.data) == null ? void 0 : _b.total) || 0;
      } catch {
      }
      try {
        const resAll = await utils_api.taskAPI.getTaskPage({ page: 1, size: 10 });
        const payload = ((_c = resAll == null ? void 0 : resAll.data) == null ? void 0 : _c.data) || {};
        const { records = [] } = payload;
        const allTasks = records.map((it) => ({
          id: it.id,
          title: it.title,
          dueDate: it.dueDate,
          points: it.points,
          status: it.status
        }));
        recentTasks.value = allTasks.slice(0, 3);
      } catch {
      }
    };
    common_vendor.onMounted(loadOverview);
    common_vendor.onShow(loadOverview);
    function goPartyNotice() {
      common_vendor.index.navigateTo({ url: "/pages/home/announcements" });
    }
    function goPartyInfo() {
      try {
        const token = common_vendor.index.getStorageSync("accessToken");
        const expire = common_vendor.index.getStorageSync("tokenExpireTime");
        const loggedIn = !!token && !!expire && Date.now() < Number(expire);
        if (!loggedIn) {
          common_vendor.index.navigateTo({ url: "/pages/my/login/login" });
          return;
        }
        common_vendor.index.navigateTo({ url: "/pages/home/branchInfo" });
      } catch (e) {
        common_vendor.index.navigateTo({ url: "/pages/my/login/login" });
      }
    }
    function goMyInfo() {
      common_vendor.index.navigateTo({ url: "/pages/home/myinfo" });
    }
    function goPartyApplication() {
      common_vendor.index.navigateTo({ url: "/pages/home/party" });
    }
    function goStudyResource() {
      common_vendor.index.navigateTo({ url: "/pages/home/memStudy" });
    }
    function goActivityList() {
      common_vendor.index.switchTab({ url: "/pages/task/task" });
    }
    function goStudyRecord() {
      common_vendor.index.navigateTo({ url: "/pages/my/record" });
    }
    function goActivityRecord() {
      common_vendor.index.navigateTo({ url: "/pages/my/myActivity" });
    }
    function goPointsRecord() {
      common_vendor.index.navigateTo({ url: "/pages/my/pointsRecord" });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({}, {
        e: common_assets._imports_0,
        f: common_vendor.o(goPartyNotice, "be"),
        g: common_assets._imports_1,
        h: common_vendor.o(goPartyInfo, "af"),
        i: common_assets._imports_1,
        j: common_vendor.o(goMyInfo, "c7"),
        k: common_assets._imports_2,
        l: common_vendor.o(goPartyApplication, "3a"),
        m: common_assets._imports_3,
        n: common_vendor.o(goStudyResource, "1f"),
        o: common_assets._imports_4,
        p: common_vendor.o(goActivityList, "46"),
        q: common_assets._imports_5,
        r: common_vendor.o(goStudyRecord, "27"),
        s: common_assets._imports_6,
        t: common_vendor.o(goActivityRecord, "75"),
        v: common_assets._imports_0,
        w: common_vendor.o(goPointsRecord, "f6")
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-07e72d3c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/home.js.map
