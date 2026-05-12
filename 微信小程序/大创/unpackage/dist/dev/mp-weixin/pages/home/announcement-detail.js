"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_auth = require("../../utils/auth.js");
const _sfc_main = {
  __name: "announcement-detail",
  setup(__props) {
    const announcement = common_vendor.ref({});
    const loading = common_vendor.ref(false);
    const error = common_vendor.ref("");
    const formatContent = (html) => {
      if (!html)
        return "";
      return html.replace(/<img[^>]*>/gi, function(match) {
        return match.replace(/style\s*=\s*["'][^"']*["']/gi, "").replace(/width\s*=\s*["'][^"']*["']/gi, "").replace(/height\s*=\s*["'][^"']*["']/gi, "").replace(/<img/gi, '<img style="max-width:100%; height:auto; display:block; margin:10px 0; border-radius:8px;"');
      });
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
    const loadData = async () => {
      var _a, _b, _c;
      if (loading.value)
        return;
      loading.value = true;
      error.value = "";
      try {
        const res = await utils_auth.requestWithAuth({
          url: `/announcements/${announcement.value.id}`,
          method: "GET"
        });
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          announcement.value = res.data.data || {};
        } else if (res.statusCode === 404 || ((_b = res.data) == null ? void 0 : _b.code) === 404) {
          error.value = "公告不存在";
        } else if (res.statusCode === 401 || ((_c = res.data) == null ? void 0 : _c.code) === 401) {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          setTimeout(() => common_vendor.index.navigateTo({ url: "/pages/my/login/login" }), 600);
        } else {
          error.value = "加载失败";
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/announcement-detail.vue:97", "加载公告详情失败:", e);
        error.value = "网络错误";
      } finally {
        loading.value = false;
      }
    };
    common_vendor.onLoad((options) => {
      if (options.id) {
        announcement.value.id = parseInt(options.id);
        announcement.value.title = options.title ? decodeURIComponent(options.title) : "公告详情";
        loadData();
      } else {
        error.value = "参数错误";
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(announcement.value.title || "公告详情"),
        b: announcement.value.id
      }, announcement.value.id ? {
        c: common_vendor.t(formatTime(announcement.value.createdAt)),
        d: formatContent(announcement.value.content)
      } : {}, {
        e: loading.value
      }, loading.value ? {} : {}, {
        f: error.value
      }, error.value ? {
        g: common_vendor.t(error.value),
        h: common_vendor.o(loadData, "d3")
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a186ee73"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/announcement-detail.js.map
