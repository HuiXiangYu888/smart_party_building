"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_auth = require("../../utils/auth.js");
const _sfc_main = {
  __name: "announcements",
  setup(__props) {
    const announcementList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const hasMore = common_vendor.ref(true);
    const currentPage = common_vendor.ref(1);
    const pageSize = common_vendor.ref(10);
    const formatTime = (timeStr) => {
      if (!timeStr)
        return "";
      try {
        const date = new Date(timeStr);
        const now = /* @__PURE__ */ new Date();
        const diff = now - date;
        if (diff < 6e4) {
          return "刚刚";
        }
        if (diff < 36e5) {
          return Math.floor(diff / 6e4) + "分钟前";
        }
        if (diff < 864e5) {
          return Math.floor(diff / 36e5) + "小时前";
        }
        if (diff < 6048e5) {
          return Math.floor(diff / 864e5) + "天前";
        }
        return date.toLocaleDateString("zh-CN");
      } catch {
        return timeStr;
      }
    };
    const handleViewDetail = (item) => {
      common_vendor.index.navigateTo({
        url: `/pages/home/announcement-detail?id=${item.id}&title=${encodeURIComponent(item.title)}`
      });
    };
    const loadData = async () => {
      var _a, _b;
      if (loading.value)
        return;
      loading.value = true;
      try {
        const params = {
          page: currentPage.value,
          size: pageSize.value
        };
        const res = await utils_auth.requestWithAuth({
          url: "/announcements/user",
          method: "GET",
          data: params
        });
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          const newData = res.data.data || [];
          if (currentPage.value === 1) {
            announcementList.value = newData;
          } else {
            announcementList.value.push(...newData);
          }
          hasMore.value = newData.length === pageSize.value;
        } else if (res.statusCode === 401 || ((_b = res.data) == null ? void 0 : _b.code) === 401) {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          setTimeout(() => common_vendor.index.navigateTo({ url: "/pages/my/login/login" }), 600);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/announcements.vue:126", "加载公告失败:", e);
        common_vendor.index.showToast({ title: "加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const loadMore = () => {
      if (hasMore.value && !loading.value) {
        currentPage.value++;
        loadData();
      }
    };
    const onRefresh = () => {
      currentPage.value = 1;
      announcementList.value = [];
      hasMore.value = true;
      loadData();
    };
    common_vendor.onMounted(() => {
      loadData();
    });
    common_vendor.onShow(() => {
      onRefresh();
    });
    common_vendor.onPullDownRefresh(() => {
      onRefresh();
      setTimeout(() => {
        common_vendor.index.stopPullDownRefresh();
      }, 1e3);
    });
    common_vendor.onReachBottom(() => {
      loadMore();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(announcementList.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.title),
            b: common_vendor.t(formatTime(item.createdAt)),
            c: common_vendor.t(item.content.length > 100 ? item.content.substring(0, 100) + "..." : item.content),
            d: item.id,
            e: common_vendor.o(($event) => handleViewDetail(item), item.id)
          };
        }),
        b: announcementList.value.length === 0 && !loading.value
      }, announcementList.value.length === 0 && !loading.value ? {} : {}, {
        c: loading.value
      }, loading.value ? {} : {}, {
        d: hasMore.value && !loading.value
      }, hasMore.value && !loading.value ? {
        e: common_vendor.o(loadMore, "19")
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-45329106"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/announcements.js.map
