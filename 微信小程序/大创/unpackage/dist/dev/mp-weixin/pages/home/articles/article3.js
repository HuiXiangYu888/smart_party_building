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
    const showProgressInfo = common_vendor.ref(false);
    const studyStartTime = common_vendor.ref(null);
    const studyDuration = common_vendor.ref(0);
    const isPlaying = common_vendor.ref(false);
    const isFirstPlay = common_vendor.ref(true);
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
    const onVideoError = (e) => {
      common_vendor.index.showToast({ title: "视频加载失败", icon: "none" });
      common_vendor.index.__f__("error", "at pages/home/articles/article3.vue:65", "video error", e);
    };
    const onLoadedMetadata = (e) => {
      totalDuration.value = Math.floor(e.detail.duration);
      loadVideoProgress();
    };
    const onLoadedData = () => {
      common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:75", "视频数据加载完成，当前进度:", currentTime.value);
      if (currentTime.value > 0 && isFirstPlay.value) {
        setTimeout(() => {
          const videoContext = common_vendor.index.createVideoContext("videoPlayer");
          if (videoContext) {
            common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:81", "onLoadedData设置播放位置到:", currentTime.value);
            videoContext.seek(currentTime.value);
          }
        }, 1e3);
      }
    };
    const onCanplay = () => {
      common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:90", "视频可以播放，当前进度:", currentTime.value);
      if (currentTime.value > 0 && isFirstPlay.value) {
        setTimeout(() => {
          const videoContext = common_vendor.index.createVideoContext("videoPlayer");
          if (videoContext) {
            common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:96", "onCanplay设置播放位置到:", currentTime.value);
            videoContext.seek(currentTime.value);
          }
        }, 500);
      }
    };
    const onPlay = () => {
      isPlaying.value = true;
      studyStartTime.value = /* @__PURE__ */ new Date();
      startStudyTimer();
      if (currentTime.value > 0 && isFirstPlay.value) {
        common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:110", "开始播放，准备设置播放位置到:", currentTime.value);
        const videoContext = common_vendor.index.createVideoContext("videoPlayer");
        if (videoContext) {
          common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:114", "onPlay立即设置播放位置到:", currentTime.value);
          videoContext.seek(currentTime.value);
        }
        setTimeout(() => {
          const videoContext2 = common_vendor.index.createVideoContext("videoPlayer");
          if (videoContext2) {
            common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:122", "onPlay延迟设置播放位置到:", currentTime.value);
            videoContext2.seek(currentTime.value);
          }
        }, 1e3);
      }
      isFirstPlay.value = false;
    };
    const onPause = () => {
      isPlaying.value = false;
      stopStudyTimer();
      saveVideoProgress();
      if (studyDuration.value > 0 && studyDuration.value < 60) {
        common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:137", "学习时长少于1分钟，不保存学习记录:", studyDuration.value, "秒");
        studyDuration.value = 0;
      }
    };
    const onEnded = () => {
      isPlaying.value = false;
      stopStudyTimer();
      saveVideoProgress();
      if (studyDuration.value >= 60) {
        saveStudyRecord();
      } else if (studyDuration.value > 0) {
        common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:151", "学习时长少于1分钟，不保存学习记录:", studyDuration.value, "秒");
        studyDuration.value = 0;
      }
    };
    const onTimeUpdate = (e) => {
      currentTime.value = Math.floor(e.detail.currentTime);
      watchPercentage.value = totalDuration.value > 0 ? currentTime.value / totalDuration.value * 100 : 0;
      if (Math.floor(currentTime.value) % 60 === 0 && currentTime.value > 0) {
        common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:162", "自动保存进度:", currentTime.value);
        saveVideoProgress();
      }
    };
    const loadVideoProgress = async () => {
      var _a, _b;
      if (!resourceId.value) {
        common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:170", "没有resourceId，跳过加载进度");
        return;
      }
      common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:174", "开始加载观看进度，resourceId:", resourceId.value);
      try {
        const res = await utils_auth.requestWithAuth({
          url: `/study/records/video/progress/${resourceId.value}`,
          method: "GET"
        });
        common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:181", "加载进度响应:", res);
        if (res.statusCode === 200 && ((_a = res.data) == null ? void 0 : _a.code) === 200) {
          const progress = res.data.data;
          common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:185", "后端返回的进度数据:", progress);
          common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:186", "currentTime字段值:", progress.currentTime);
          const savedTime = progress.currentTime || 0;
          common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:190", "解析出的保存时间:", savedTime);
          if (progress && savedTime > 0) {
            currentTime.value = savedTime;
            watchPercentage.value = progress.watchPercentage || 0;
            showProgressInfo.value = true;
            common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:196", "加载到观看进度:", savedTime, "秒");
            if (savedTime > 0) {
              common_vendor.index.showToast({
                title: `从${Math.floor(savedTime / 60)}分${savedTime % 60}秒继续播放`,
                icon: "none",
                duration: 2e3
              });
            }
          } else {
            common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:207", "没有找到保存的进度或进度为0，savedTime:", savedTime);
            showProgressInfo.value = true;
          }
        } else {
          common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:211", "加载进度失败:", ((_b = res.data) == null ? void 0 : _b.message) || "未知错误");
          showProgressInfo.value = true;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/articles/article3.vue:215", "加载观看进度失败:", e);
        showProgressInfo.value = true;
      }
    };
    const saveVideoProgress = async () => {
      if (!resourceId.value || totalDuration.value === 0) {
        common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:223", "跳过保存进度: resourceId=", resourceId.value, "totalDuration=", totalDuration.value);
        return;
      }
      common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:227", "保存观看进度:", {
        resourceId: resourceId.value,
        currentTime: currentTime.value,
        totalDuration: totalDuration.value
      });
      try {
        const res = await utils_auth.requestWithAuth({
          url: "/study/records/video/progress",
          method: "POST",
          data: {
            resourceId: resourceId.value,
            currentTime: currentTime.value,
            totalDuration: totalDuration.value
          }
        });
        common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:243", "保存进度响应:", res);
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/articles/article3.vue:245", "保存观看进度失败:", e);
      }
    };
    const saveStudyRecord = async () => {
      if (!resourceId.value || studyDuration.value === 0)
        return;
      try {
        const durationInMinutes = Math.max(1, Math.ceil(studyDuration.value / 60));
        await utils_auth.requestWithAuth({
          url: "/study/records/log",
          method: "POST",
          data: {
            resourceId: resourceId.value,
            duration: durationInMinutes
          }
        });
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/home/articles/article3.vue:266", "保存学习记录失败:", e);
      }
    };
    const startStudyTimer = () => {
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
      if (resourceId.value) {
        common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:295", "页面加载，开始加载视频进度");
        await loadVideoProgress();
      }
    });
    common_vendor.onUnload(() => {
      if (isPlaying.value) {
        stopStudyTimer();
      }
      saveVideoProgress();
      if (studyDuration.value >= 60) {
        saveStudyRecord();
      } else if (studyDuration.value > 0) {
        common_vendor.index.__f__("log", "at pages/home/articles/article3.vue:311", "学习时长少于1分钟，不保存学习记录:", studyDuration.value, "秒");
      }
    });
    common_vendor.onUnmounted(() => {
      stopStudyTimer();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(title.value || "视频播放"),
        b: src.value,
        c: currentTime.value === 0,
        d: common_vendor.o(onVideoError, "33"),
        e: common_vendor.o(onTimeUpdate, "ee"),
        f: common_vendor.o(onLoadedMetadata, "91"),
        g: common_vendor.o(onLoadedData, "fb"),
        h: common_vendor.o(onCanplay, "9b"),
        i: common_vendor.o(onPlay, "da"),
        j: common_vendor.o(onPause, "09"),
        k: common_vendor.o(onEnded, "4f"),
        l: showProgressInfo.value
      }, showProgressInfo.value ? common_vendor.e({
        m: common_vendor.t(Math.round(watchPercentage.value)),
        n: currentTime.value > 0
      }, currentTime.value > 0 ? {
        o: common_vendor.t(Math.floor(currentTime.value / 60)),
        p: common_vendor.t(currentTime.value % 60)
      } : {}) : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-407db708"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/home/articles/article3.js.map
