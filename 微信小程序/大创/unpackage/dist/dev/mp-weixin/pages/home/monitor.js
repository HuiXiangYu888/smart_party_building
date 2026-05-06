"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "monitor",
  setup(__props) {
    const monitor = [
      {
        name: "张三",
        learningCount: 5,
        learningTime: 1200
      },
      {
        name: "李四",
        learningCount: 3,
        learningTime: 800
      }
    ];
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(monitor, (item, index, i0) => {
          return {
            a: common_vendor.t(item.name),
            b: common_vendor.t(item.learningCount),
            c: common_vendor.t(item.learningTime),
            d: index
          };
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-411efda5"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/monitor.js.map
