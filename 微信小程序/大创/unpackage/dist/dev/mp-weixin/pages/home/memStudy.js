"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_auth = require("../../utils/auth.js");
const _sfc_main = {
  __name: "memStudy",
  setup(__props) {
    const videoList = common_vendor.ref([]);
    const loadingVideo = common_vendor.ref(false);
    const loadVideos = async () => {
      var _a, _b;
      loadingVideo.value = true;
      try {
        const res = await utils_auth.requestWithAuth({ url: "/study/resources?type=VIDEO", method: "GET" });
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          videoList.value = res.data.data || [];
        } else if (res.statusCode === 401 || ((_b = res.data) == null ? void 0 : _b.code) === 401) {
          common_vendor.index.showToast({ title: "请先登录后再查看学习资源", icon: "none" });
          setTimeout(() => common_vendor.index.navigateTo({ url: "/pages/my/login/login" }), 600);
        }
      } catch (e) {
        common_vendor.index.showToast({ title: (e == null ? void 0 : e.message) || "网络错误", icon: "none" });
      } finally {
        loadingVideo.value = false;
      }
    };
    const mapType = (t) => ({ VIDEO: "视频" })[t] || t;
    const formatTime = (t) => {
      if (!t)
        return "";
      try {
        return new Date(t).toLocaleString();
      } catch {
        return t;
      }
    };
    const open = async (item) => {
      if (item.type === "VIDEO") {
        common_vendor.index.navigateTo({
          url: `/pages/home/articles/article3?src=${encodeURIComponent(item.url)}&title=${encodeURIComponent(item.title)}&id=${item.id}`
        });
      }
    };
    common_vendor.onMounted(() => {
      loadVideos();
    });
    common_vendor.onShow(() => {
      loadVideos();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(videoList.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.title),
            b: item.coverUrl
          }, item.coverUrl ? {
            c: item.coverUrl
          } : {}, {
            d: common_vendor.t(mapType(item.type)),
            e: common_vendor.t(formatTime(item.createdAt)),
            f: item.id,
            g: common_vendor.o(($event) => open(item), item.id)
          });
        }),
        b: loadingVideo.value
      }, loadingVideo.value ? {} : !videoList.value.length ? {} : {}, {
        c: !videoList.value.length
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-72412b3b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/memStudy.js.map
