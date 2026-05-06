"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "addActivity",
  setup(__props) {
    const activity = common_vendor.ref({
      title: "",
      content: ""
    });
    const addAct = () => {
      const newAct = {
        title: activity.value.title,
        content: activity.value.content
      };
      if (newAct.title.trim() !== "" && newAct.content.trim() !== "") {
        common_vendor.index.navigateTo({
          url: `/pages/home/draft?newActivity=${encodeURIComponent(JSON.stringify(newAct))}`
        });
        common_vendor.index.showToast({
          title: "添加成功"
        });
      } else {
        common_vendor.index.showToast({
          title: "请填写活动标题和内容",
          icon: "none"
        });
      }
    };
    return (_ctx, _cache) => {
      return {
        a: activity.value.title,
        b: common_vendor.o(($event) => activity.value.title = $event.detail.value, "f1"),
        c: activity.value.content,
        d: common_vendor.o(($event) => activity.value.content = $event.detail.value, "6f"),
        e: common_vendor.o(addAct, "08")
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a648f5d6"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/addActivity.js.map
