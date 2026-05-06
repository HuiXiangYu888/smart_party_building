"use strict";
const common_vendor = require("../common/vendor.js");
const utils_auth = require("./auth.js");
const BASE_URL = "http://127.0.0.1:8000";
const userAPI = {
  // 用户端登录
  login: (data) => {
    return common_vendor.index.request({
      url: `${BASE_URL}/auth/user/login`,
      method: "POST",
      header: { "Content-Type": "application/json" },
      data
    });
  },
  // 获取用户信息
  getUserInfo: () => {
    return utils_auth.requestWithAuth({
      url: `${BASE_URL}/user/info`,
      method: "GET"
    });
  },
  // 更新用户信息
  updateUserInfo: (data) => {
    return utils_auth.requestWithAuth({
      url: `${BASE_URL}/user/info`,
      method: "PUT",
      data
    });
  },
  // 修改密码
  changePassword: (data) => {
    return utils_auth.requestWithAuth({
      url: `${BASE_URL}/user/password`,
      method: "PUT",
      data
    });
  }
};
const taskAPI = {
  // 获取任务分页（用户端也复用管理端分页）
  getTaskPage: (params) => {
    return common_vendor.index.request({
      url: `${BASE_URL}/tasks/user/page`,
      method: "GET",
      data: params
    });
  },
  // 用户报名（鉴权）
  userJoin: (taskId) => utils_auth.requestWithAuth({ url: `${BASE_URL}/tasks/user/${taskId}/join`, method: "POST" }),
  // 用户取消报名（鉴权）
  userCancel: (taskId) => utils_auth.requestWithAuth({ url: `${BASE_URL}/tasks/user/${taskId}/cancel`, method: "POST" }),
  // 提交评价
  submitEvaluation: (taskId, data) => {
    return utils_auth.requestWithAuth({
      url: `${BASE_URL}/tasks/${taskId}/evaluations`,
      method: "POST",
      data
    });
  },
  // 查看评价
  listEvaluations: (taskId) => {
    return utils_auth.requestWithAuth({
      url: `${BASE_URL}/tasks/${taskId}/evaluations`,
      method: "GET"
    });
  },
  // 我的任务（报名记录）
  getMyTasks: () => utils_auth.requestWithAuth({ url: `${BASE_URL}/tasks/user/my`, method: "GET" })
};
const pointsAPI = {
  getMyPointsList: () => utils_auth.requestWithAuth({ url: `${BASE_URL}/points/user/list`, method: "GET" }),
  getMyPointsSummary: () => utils_auth.requestWithAuth({ url: `${BASE_URL}/points/user/summary`, method: "GET" })
};
const uploadAPI = {
  // 上传文件（走后端 /files/upload，再转OSS）
  uploadFile: (filePath) => {
    return new Promise((resolve, reject) => {
      common_vendor.index.uploadFile({
        url: `${BASE_URL}/files/upload`,
        filePath,
        name: "file",
        header: {
          "Authorization": `Bearer ${common_vendor.index.getStorageSync("accessToken") || ""}`
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data || "{}");
            if (data && data.code === 200) {
              resolve(data.data);
            } else {
              reject(new Error(data.message || "上传失败"));
            }
          } catch (error) {
            reject(new Error("上传失败"));
          }
        },
        fail: (error) => {
          reject(error);
        }
      });
    });
  }
};
const applicationAPI = {
  applyPositive: (details, attachments) => utils_auth.requestWithAuth({
    url: `${BASE_URL}/user/applications/positive`,
    method: "POST",
    data: { details, attachments }
  }),
  applyParty: (details, attachments) => utils_auth.requestWithAuth({
    url: `${BASE_URL}/user/applications/party`,
    method: "POST",
    data: { details, attachments }
  }),
  applyPrepare: (evaluationReport, probationStart, probationEnd, attachments) => utils_auth.requestWithAuth({
    url: `${BASE_URL}/user/applications/prepare`,
    method: "POST",
    data: { evaluationReport, probationStart, probationEnd, attachments }
  }),
  getMyHistory: () => utils_auth.requestWithAuth({ url: `${BASE_URL}/user/applications/history`, method: "GET" })
};
exports.applicationAPI = applicationAPI;
exports.pointsAPI = pointsAPI;
exports.taskAPI = taskAPI;
exports.uploadAPI = uploadAPI;
exports.userAPI = userAPI;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/api.js.map
