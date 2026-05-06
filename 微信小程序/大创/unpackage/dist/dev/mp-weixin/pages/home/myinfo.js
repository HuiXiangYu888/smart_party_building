"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_auth = require("../../utils/auth.js");
const _sfc_main = {
  __name: "myinfo",
  setup(__props) {
    const myinfo = common_vendor.reactive({
      isEditing: false,
      studentId: "",
      idNumber: "",
      username: "",
      mobile: "",
      politicalStatus: "",
      branchId: void 0,
      branchName: "",
      reviewStatus: "NORMAL"
    });
    const branches = common_vendor.ref([]);
    const openApplication = common_vendor.ref(false);
    const politicalStatusOptions = ["群众", "共青团员", "预备党员", "党员", "其他"];
    const fetchBranches = async () => {
      var _a;
      try {
        const res = await utils_auth.requestWithAuth({ url: "/branches", method: "GET" });
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          branches.value = res.data.data || [];
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/myinfo.vue:81", "获取支部列表失败:", e);
      }
      if (!branches.value || branches.value.length === 0) {
        branches.value = [{ id: 1, name: "测试支部" }];
      } else if (!branches.value.some((b) => b.name === "测试支部")) {
        branches.value = [{ id: 1, name: "测试支部" }, ...branches.value];
      }
    };
    const fetchProfile = async () => {
      var _a;
      const res = await utils_auth.requestWithAuth({ url: "/members/profile", method: "GET" });
      if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
        const d = res.data.data || {};
        myinfo.studentId = d.studentId || "";
        myinfo.idNumber = d.idNumber || "";
        myinfo.username = d.username || "";
        myinfo.mobile = d.mobile || "";
        myinfo.politicalStatus = d.politicalStatus || "";
        myinfo.branchId = d.branchId;
        myinfo.reviewStatus = d.reviewStatus || "NORMAL";
        const b = branches.value.find((x) => x.id === myinfo.branchId);
        myinfo.branchName = b ? b.name : "";
      }
    };
    const fetchOpenApplication = async () => {
      var _a, _b;
      try {
        const res = await utils_auth.requestWithAuth({ url: "/settings/open-application", method: "GET" });
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          openApplication.value = !!((_b = res.data.data) == null ? void 0 : _b.open);
        }
      } catch (e) {
      }
    };
    const onBranchChange = (e) => {
      const i = Number(e.detail.value);
      const b = branches.value[i];
      myinfo.branchId = b == null ? void 0 : b.id;
      myinfo.branchName = b == null ? void 0 : b.name;
    };
    const onPoliticalChange = (e) => {
      const i = Number(e.detail.value);
      myinfo.politicalStatus = politicalStatusOptions[i];
    };
    const toggleEdit = async () => {
      var _a, _b;
      if (!myinfo.isEditing) {
        if (!openApplication.value) {
          common_vendor.index.showToast({ title: "当前未开放申请，无法编辑", icon: "none" });
          return;
        }
        myinfo.isEditing = true;
        return;
      }
      const payload = {
        studentId: myinfo.studentId,
        idNumber: myinfo.idNumber,
        username: myinfo.username,
        mobile: myinfo.mobile,
        politicalStatus: myinfo.politicalStatus,
        branchId: myinfo.branchId
      };
      const res = await utils_auth.requestWithAuth({ url: "/members/profile", method: "PUT", data: payload });
      if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
        common_vendor.index.showToast({ title: "已保存并提交审核", icon: "success" });
        myinfo.isEditing = false;
        myinfo.reviewStatus = "PENDING";
      } else {
        common_vendor.index.showToast({ title: ((_b = res.data) == null ? void 0 : _b.message) || "保存失败", icon: "none" });
      }
    };
    common_vendor.onMounted(async () => {
      await fetchBranches();
      await fetchOpenApplication();
      await fetchProfile();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: !myinfo.isEditing
      }, !myinfo.isEditing ? {
        b: common_vendor.t(myinfo.studentId)
      } : {
        c: myinfo.studentId,
        d: common_vendor.o(($event) => myinfo.studentId = $event.detail.value, "b2")
      }, {
        e: !myinfo.isEditing
      }, !myinfo.isEditing ? {
        f: common_vendor.t(myinfo.username)
      } : {
        g: myinfo.username,
        h: common_vendor.o(($event) => myinfo.username = $event.detail.value, "0c")
      }, {
        i: !myinfo.isEditing
      }, !myinfo.isEditing ? {
        j: common_vendor.t(myinfo.idNumber)
      } : {
        k: myinfo.idNumber,
        l: common_vendor.o(($event) => myinfo.idNumber = $event.detail.value, "a4")
      }, {
        m: !myinfo.isEditing
      }, !myinfo.isEditing ? {
        n: common_vendor.t(myinfo.mobile)
      } : {
        o: myinfo.mobile,
        p: common_vendor.o(($event) => myinfo.mobile = $event.detail.value, "9b")
      }, {
        q: !myinfo.isEditing
      }, !myinfo.isEditing ? {
        r: common_vendor.t(myinfo.politicalStatus || "-")
      } : {
        s: common_vendor.t(myinfo.politicalStatus || "请选择"),
        t: politicalStatusOptions,
        v: common_vendor.o(onPoliticalChange, "29")
      }, {
        w: !myinfo.isEditing
      }, !myinfo.isEditing ? {
        x: common_vendor.t(myinfo.branchName || "-")
      } : {
        y: common_vendor.t(myinfo.branchName || "请选择"),
        z: branches.value.map((b) => b.name),
        A: common_vendor.o(onBranchChange, "7f")
      }, {
        B: common_vendor.t(myinfo.reviewStatus === "PENDING" ? "审核中" : myinfo.reviewStatus === "APPROVED" ? "已通过" : "不通过"),
        C: myinfo.reviewStatus === "PENDING" ? "#e6a23c" : myinfo.reviewStatus === "APPROVED" ? "#67c23a" : "#f56c6c",
        D: common_vendor.t(myinfo.isEditing ? "提 交 审 核" : openApplication.value ? "编 辑" : "未开放，无法编辑"),
        E: common_vendor.o(($event) => toggleEdit(), "69"),
        F: !openApplication.value && !myinfo.isEditing
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-f2e33e64"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/myinfo.js.map
