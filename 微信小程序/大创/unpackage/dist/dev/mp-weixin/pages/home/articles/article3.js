"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_auth = require("../../../utils/auth.js");
const _sfc_main = {
  __name: "article3",
  setup(__props) {
    const src = common_vendor.ref("");
    const title = common_vendor.ref("");
    const resourceId = common_vendor.ref(null);
    const currentTime = common_vendor.ref(0);
    const totalDuration = common_vendor.ref(0);
    const watchPercentage = common_vendor.ref(0);
    const savedTime = common_vendor.ref(0);
    const hasCompleted = common_vendor.ref(false);
    const studyDuration = common_vendor.ref(0);
    const isPlaying = common_vendor.ref(false);
    const isFirstPlay = common_vendor.ref(true);
    let lastValidTime = 0;
    let studyTimer = null;
    const fetchSignedIfNeeded = async (rawSrc, id) => {
      var _a, _b;
      if (rawSrc && rawSrc.includes("Signature="))
        return rawSrc;
      if (!id)
        return rawSrc;
      try {
        const res = await utils_auth.requestWithAuth({ url: `/study/resources/${id}/signed-url`, method: "GET" });
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200)
          return ((_b = res.data.data) == null ? void 0 : _b.url) || rawSrc;
      } catch (e) {
      }
      return rawSrc;
    };
    const formatDuration = (seconds) => {
      if (!seconds)
        return "0:00";
      const m = Math.floor(seconds / 60);
      const s = Math.floor(seconds % 60);
      return `${m}:${s.toString().padStart(2, "0")}`;
    };
    const onVideoError = (e) => {
      common_vendor.index.showToast({ title: "视频加载失败", icon: "none" });
      common_vendor.index.__f__("error", "at pages/home/articles/article3.vue:99", "video error", e);
    };
    const onLoadedMetadata = (e) => {
      totalDuration.value = Math.floor(e.detail.duration);
      loadVideoProgress();
    };
    const onPlay = () => {
      isPlaying.value = true;
      startStudyTimer();
      if (savedTime.value > 0 && isFirstPlay.value) {
        const videoContext = common_vendor.index.createVideoContext("videoPlayer");
        if (videoContext) {
          videoContext.seek(savedTime.value);
          lastValidTime = savedTime.value;
        }
      }
      isFirstPlay.value = false;
    };
    const onPause = () => {
      isPlaying.value = false;
      stopStudyTimer();
      saveVideoProgress();
    };
    const onEnded = () => {
      isPlaying.value = false;
      stopStudyTimer();
      watchPercentage.value = 100;
      currentTime.value = totalDuration.value;
      saveVideoProgress();
      if (!hasCompleted.value) {
        completeVideo();
      }
    };
    const onTimeUpdate = (e) => {
      const newTime = Math.floor(e.detail.currentTime);
      if (newTime > lastValidTime + 3) {
        const videoContext = common_vendor.index.createVideoContext("videoPlayer");
        if (videoContext) {
          videoContext.seek(lastValidTime);
          common_vendor.index.showToast({ title: "不允许快进哦~", icon: "none", duration: 1500 });
        }
        return;
      }
      if (newTime > lastValidTime) {
        lastValidTime = newTime;
      }
      currentTime.value = newTime;
      watchPercentage.value = totalDuration.value > 0 ? currentTime.value / totalDuration.value * 100 : 0;
      if (currentTime.value > 0 && currentTime.value % 30 === 0) {
        saveVideoProgress();
      }
    };
    const loadVideoProgress = async () => {
      var _a;
      if (!resourceId.value)
        return;
      try {
        const res = await utils_auth.requestWithAuth({
          url: `/study/records/video/progress/${resourceId.value}`,
          method: "GET"
        });
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          const progress = res.data.data;
          if (progress) {
            const st = progress.currentTime || 0;
            savedTime.value = st;
            lastValidTime = st;
            currentTime.value = st;
            watchPercentage.value = progress.watchPercentage || 0;
            hasCompleted.value = progress.watchPercentage >= 95;
            if (st > 0) {
              common_vendor.index.showToast({
                title: `从${formatDuration(st)}继续播放`,
                icon: "none",
                duration: 2e3
              });
            }
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/articles/article3.vue:195", "加载观看进度失败:", e);
      }
    };
    const saveVideoProgress = async () => {
      if (!resourceId.value || totalDuration.value === 0)
        return;
      try {
        await utils_auth.requestWithAuth({
          url: "/study/records/video/progress",
          method: "POST",
          data: {
            resourceId: resourceId.value,
            currentTime: currentTime.value,
            totalDuration: totalDuration.value
          }
        });
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/articles/article3.vue:213", "保存观看进度失败:", e);
      }
    };
    const completeVideo = async () => {
      var _a;
      if (!resourceId.value)
        return;
      try {
        const res = await utils_auth.requestWithAuth({
          url: "/study/records/video/complete",
          method: "POST",
          data: {
            resourceId: resourceId.value
          }
        });
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          hasCompleted.value = true;
          const msg = res.data.data || "";
          if (msg.includes("积分")) {
            common_vendor.index.showToast({ title: "🎉 完成观看，+1积分", icon: "none", duration: 2500 });
          } else {
            common_vendor.index.showToast({ title: "✓ 观看完成", icon: "none", duration: 2e3 });
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/articles/article3.vue:238", "提交完成状态失败:", e);
      }
    };
    const startStudyTimer = () => {
      if (studyTimer)
        return;
      studyTimer = setInterval(() => {
        if (isPlaying.value) {
          studyDuration.value += 1;
        }
      }, 1e3);
    };
    const stopStudyTimer = () => {
      if (studyTimer) {
        clearInterval(studyTimer);
        studyTimer = null;
      }
    };
    common_vendor.onLoad(async (opts) => {
      const raw = (opts == null ? void 0 : opts.src) ? decodeURIComponent(opts.src) : "";
      title.value = (opts == null ? void 0 : opts.title) ? decodeURIComponent(opts.title) : "";
      resourceId.value = (opts == null ? void 0 : opts.id) ? Number(opts.id) : null;
      src.value = await fetchSignedIfNeeded(raw, resourceId.value);
    });
    common_vendor.onUnload(() => {
      if (isPlaying.value)
        stopStudyTimer();
      saveVideoProgress();
    });
    common_vendor.onUnmounted(() => {
      stopStudyTimer();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(title.value || "视频播放"),
        b: src.value,
        c: savedTime.value === 0,
        d: common_vendor.o(onVideoError, "1c"),
        e: common_vendor.o(onTimeUpdate, "33"),
        f: common_vendor.o(onLoadedMetadata, "e6"),
        g: common_vendor.o(onPlay, "75"),
        h: common_vendor.o(onPause, "d4"),
        i: common_vendor.o(onEnded, "03"),
        j: Math.min(watchPercentage.value, 100) + "%",
        k: common_vendor.t(Math.round(watchPercentage.value)),
        l: totalDuration.value > 0
      }, totalDuration.value > 0 ? {
        m: common_vendor.t(formatDuration(totalDuration.value))
      } : {}, {
        n: common_vendor.t(hasCompleted.value ? "✓ 已完成观看" : "未完成"),
        o: common_vendor.n(hasCompleted.value ? "completed" : "not-completed")
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-407db708"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/home/articles/article3.js.map
