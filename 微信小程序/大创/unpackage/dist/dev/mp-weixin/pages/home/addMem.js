"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "addMem",
  setup(__props) {
    const newMember = common_vendor.ref({
      studentId: "",
      name: "",
      password: "",
      gender: "",
      joinDate: "",
      position: "",
      branch: ""
    });
    const addMember = () => {
      if (newMember.value.password === "123456") {
        const newPartyMember = {
          id: newMember.value.studentId,
          name: newMember.value.name,
          gender: newMember.value.gender,
          joinDate: newMember.value.joinDate,
          position: newMember.value.position,
          branch: newMember.value.branch,
          isEditing: false
        };
        common_vendor.index.navigateTo({
          url: `/pages/home/memberInfo?newMem=${encodeURIComponent(JSON.stringify(newPartyMember))}`
        });
        common_vendor.index.showToast({
          title: "添加成功"
        });
      } else {
        common_vendor.index.showToast({
          icon: "error",
          title: "添加失败"
        });
      }
    };
    return (_ctx, _cache) => {
      return {
        a: newMember.value.studentId,
        b: common_vendor.o(($event) => newMember.value.studentId = $event.detail.value, "3b"),
        c: newMember.value.name,
        d: common_vendor.o(($event) => newMember.value.name = $event.detail.value, "62"),
        e: newMember.value.password,
        f: common_vendor.o(($event) => newMember.value.password = $event.detail.value, "51"),
        g: newMember.value.gender,
        h: common_vendor.o(($event) => newMember.value.gender = $event.detail.value, "d8"),
        i: newMember.value.joinDate,
        j: common_vendor.o(($event) => newMember.value.joinDate = $event.detail.value, "01"),
        k: newMember.value.position,
        l: common_vendor.o(($event) => newMember.value.position = $event.detail.value, "b0"),
        m: newMember.value.branch,
        n: common_vendor.o(($event) => newMember.value.branch = $event.detail.value, "68"),
        o: common_vendor.o(addMember, "76")
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-b36e709b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/addMem.js.map
