"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "draft",
  setup(__props) {
    const draft = common_vendor.ref([
      {
        title: "2024年春季校园运动会",
        content: "一、活动时间：2024年4月20日（周六）8:00-17:00；二、活动地点：学校田径场；三、参与对象：全体师生；四、比赛项目：1. 田径类：100米、400米、800米、跳远、跳高、铅球；五、注意事项：1. 参赛选手需提前30分钟到场签到；2. 自备运动装备，穿运动鞋参赛；3. 身体不适者禁止参赛，如有特殊情况需提前告知"
      },
      {
        title: "2024年校园美食节",
        content: "本周在东苑餐厅门口举办美食节，欢迎同学们品尝各色美味，请同学们合理消费，垃圾不乱扔，做到卫生消费"
      }
    ]);
    common_vendor.onLoad((options) => {
      if (options.newActivity) {
        const newActivity = JSON.parse(decodeURIComponent(options.newActivity));
        draft.value.push(newActivity);
      }
    });
    const deleteDraft = (index) => {
      draft.value.splice(index, 1);
    };
    const publish = (index) => {
      const publication = {
        id: Date.now(),
        // 用时间戳作为唯一ID
        title: draft.value[index].title,
        content: draft.value[index].content,
        signed: false
      };
      draft.value.splice(index, 1);
      common_vendor.index.setStorageSync("pendingTask", JSON.stringify(publication));
      common_vendor.index.switchTab({
        url: "/pages/task/task",
        success: () => {
          common_vendor.index.showToast({ title: "发布成功", icon: "success" });
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/home/draft.vue:63", "跳转失败:", err);
          common_vendor.index.showToast({ title: "发布失败", icon: "none" });
        }
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(draft.value, (member, index, i0) => {
          return {
            a: common_vendor.t(member.title),
            b: common_vendor.t(member.content),
            c: common_vendor.o(($event) => deleteDraft(index), index),
            d: common_vendor.o(($event) => publish(index), index),
            e: index
          };
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-6ac24a7f"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/draft.js.map
