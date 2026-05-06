"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  __name: "myActivity",
  setup(__props) {
    const list = common_vendor.ref([]);
    const formatDate = (str) => {
      if (!str)
        return "";
      try {
        return new Date(str).toLocaleString("zh-CN");
      } catch {
        return str;
      }
    };
    const translateStatus = (s) => {
      if (s === "PUBLISHING")
        return "发布中";
      if (s === "PENDING_END")
        return "待完结";
      if (s === "ENDED")
        return "已完结";
      return s || "";
    };
    const loadMyTasks = async () => {
      var _a;
      try {
        const res = await utils_api.taskAPI.getMyTasks();
        const raw = ((_a = res == null ? void 0 : res.data) == null ? void 0 : _a.data) || [];
        list.value = raw.map((it) => ({
          id: it.id,
          title: it.title,
          dueDate: it.due_date || it.dueDate,
          points: it.points,
          status: it.status,
          assignmentStatus: it.assignment_status || it.assignmentStatus
        }));
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/my/myActivity.vue:51", "获取活动记录失败", e);
      }
    };
    common_vendor.onMounted(loadMyTasks);
    common_vendor.onShow(loadMyTasks);
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: list.value.length === 0
      }, list.value.length === 0 ? {} : {
        b: common_vendor.f(list.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.title),
            b: common_vendor.t(item.points),
            c: common_vendor.t(formatDate(item.dueDate)),
            d: common_vendor.t(translateStatus(item.status)),
            e: item.id || index
          };
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-711fe52b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/my/myActivity.js.map
