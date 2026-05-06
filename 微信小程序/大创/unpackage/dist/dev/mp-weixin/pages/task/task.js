"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const STORAGE_JOINED_KEY = "joinedTaskIds";
const _sfc_main = {
  __name: "task",
  setup(__props) {
    const tasks = common_vendor.ref([]);
    const formatDate = (str) => {
      if (!str)
        return "";
      try {
        return new Date(str).toLocaleString("zh-CN");
      } catch {
        return str;
      }
    };
    const checkPendingTask = () => {
      const pendingTask = common_vendor.index.getStorageSync("pendingTask");
      if (pendingTask) {
        try {
          const task = JSON.parse(pendingTask);
          const isDuplicate = tasks.value.some((item) => item.id === task.id);
          if (!isDuplicate)
            tasks.value.push(task);
          common_vendor.index.removeStorageSync("pendingTask");
        } catch (e) {
          common_vendor.index.__f__("error", "at pages/task/task.vue:47", "解析任务失败:", e);
        }
      }
    };
    const loadTasks = async () => {
      var _a, _b;
      try {
        let res = await utils_api.taskAPI.getTaskPage({ page: 1, size: 50 });
        if (Array.isArray(res))
          res = res[1];
        const payload = ((_a = res == null ? void 0 : res.data) == null ? void 0 : _a.data) || {};
        const { records = [] } = payload;
        const base = records.filter((it) => it.status !== "ENDED").map((it) => ({
          id: it.id,
          title: it.title,
          content: it.description,
          dueDate: it.dueDate,
          points: it.points,
          capacity: it.capacity,
          status: it.status,
          signed: false
        }));
        try {
          const myRes = await utils_api.taskAPI.getMyTasks();
          const myList = ((_b = myRes == null ? void 0 : myRes.data) == null ? void 0 : _b.data) || [];
          const serverSet = new Set((myList || []).map((i) => i.id || i.task_id));
          let localSet = /* @__PURE__ */ new Set();
          try {
            const joined = common_vendor.index.getStorageSync(STORAGE_JOINED_KEY) || "[]";
            localSet = new Set(JSON.parse(joined));
          } catch {
          }
          base.forEach((row) => {
            if (serverSet.has(row.id) || localSet.has(row.id))
              row.signed = true;
          });
        } catch {
        }
        tasks.value = base;
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/task.vue:83", "加载任务失败", e);
      }
    };
    const isFull = (task) => {
      if (!task)
        return false;
      if (typeof task.capacity === "number")
        return task.capacity <= 0;
      return false;
    };
    common_vendor.onMounted(() => {
      checkPendingTask();
      loadTasks();
    });
    common_vendor.onShow(() => {
      checkPendingTask();
      loadTasks();
    });
    const join = async (index) => {
      try {
        await utils_api.taskAPI.userJoin(tasks.value[index].id);
        tasks.value[index].signed = true;
        try {
          const joined = common_vendor.index.getStorageSync(STORAGE_JOINED_KEY) || "[]";
          const set = new Set(JSON.parse(joined));
          set.add(tasks.value[index].id);
          common_vendor.index.setStorageSync(STORAGE_JOINED_KEY, JSON.stringify(Array.from(set)));
        } catch {
        }
        common_vendor.index.showToast({ title: "报名成功", icon: "success" });
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/task.vue:110", "报名失败", e);
      }
    };
    const cancel = async (index) => {
      try {
        await utils_api.taskAPI.userCancel(tasks.value[index].id);
        tasks.value[index].signed = false;
        try {
          const joined = common_vendor.index.getStorageSync(STORAGE_JOINED_KEY) || "[]";
          const arr = JSON.parse(joined).filter((id) => id !== tasks.value[index].id);
          common_vendor.index.setStorageSync(STORAGE_JOINED_KEY, JSON.stringify(arr));
        } catch {
        }
        common_vendor.index.showToast({ title: "已取消", icon: "success" });
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/task.vue:125", "取消失败", e);
      }
    };
    common_vendor.onLoad(() => {
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(tasks.value, (task, index, i0) => {
          return common_vendor.e({
            a: common_vendor.t(task.title),
            b: common_vendor.t(task.points),
            c: common_vendor.t(formatDate(task.dueDate)),
            d: common_vendor.t(task.content),
            e: isFull(task)
          }, isFull(task) ? {} : {}, {
            f: !task.signed
          }, !task.signed ? {
            g: isFull(task),
            h: common_vendor.o(($event) => join(index), index)
          } : {
            i: task.status === "PENDING_END",
            j: common_vendor.o(($event) => cancel(index), index)
          }, {
            k: index
          });
        }),
        b: tasks.value.length === 0
      }, tasks.value.length === 0 ? {} : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-62407536"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/task/task.js.map
