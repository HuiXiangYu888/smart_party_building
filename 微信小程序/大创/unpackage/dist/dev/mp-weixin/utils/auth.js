"use strict";
const common_vendor = require("../common/vendor.js");
const utils_eventBus = require("./eventBus.js");
const STORAGE_KEYS = {
  ACCESS_TOKEN: "accessToken",
  REFRESH_TOKEN: "refreshToken",
  USER_INFO: "userInfo",
  TOKEN_EXPIRE_TIME: "tokenExpireTime"
};
const CONFIG = {
  BASE_URL: "http://127.0.0.1:8000",
  TOKEN_REFRESH_THRESHOLD: 5 * 60 * 1e3,
  // 5分钟前开始刷新
  REQUEST_TIMEOUT: 1e4
};
const getAccessToken = () => {
  return common_vendor.index.getStorageSync(STORAGE_KEYS.ACCESS_TOKEN);
};
const getRefreshToken = () => {
  return common_vendor.index.getStorageSync(STORAGE_KEYS.REFRESH_TOKEN);
};
const getUserInfo = () => {
  const userInfo = common_vendor.index.getStorageSync(STORAGE_KEYS.USER_INFO);
  return userInfo ? JSON.parse(userInfo) : null;
};
const saveLoginInfo = (loginData) => {
  const { accessToken, refreshToken, userId, username, userType, expiresIn } = loginData;
  const expireTime = Date.now() + expiresIn;
  common_vendor.index.setStorageSync(STORAGE_KEYS.ACCESS_TOKEN, accessToken);
  common_vendor.index.setStorageSync(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
  common_vendor.index.setStorageSync(STORAGE_KEYS.USER_INFO, JSON.stringify({
    userId,
    username,
    userType
  }));
  common_vendor.index.setStorageSync(STORAGE_KEYS.TOKEN_EXPIRE_TIME, expireTime);
  utils_eventBus.eventBus.emit(utils_eventBus.EVENTS.USER_LOGIN, {
    userId,
    username,
    userType
  });
};
const clearLoginInfo = () => {
  common_vendor.index.removeStorageSync(STORAGE_KEYS.ACCESS_TOKEN);
  common_vendor.index.removeStorageSync(STORAGE_KEYS.REFRESH_TOKEN);
  common_vendor.index.removeStorageSync(STORAGE_KEYS.USER_INFO);
  common_vendor.index.removeStorageSync(STORAGE_KEYS.TOKEN_EXPIRE_TIME);
  utils_eventBus.eventBus.emit(utils_eventBus.EVENTS.USER_LOGOUT, null);
};
const isLoggedIn = () => {
  const token = getAccessToken();
  const expireTime = common_vendor.index.getStorageSync(STORAGE_KEYS.TOKEN_EXPIRE_TIME);
  if (!token || !expireTime) {
    return false;
  }
  return Date.now() < expireTime;
};
const needRefreshToken = () => {
  const expireTime = common_vendor.index.getStorageSync(STORAGE_KEYS.TOKEN_EXPIRE_TIME);
  if (!expireTime) {
    return false;
  }
  return expireTime - Date.now() < CONFIG.TOKEN_REFRESH_THRESHOLD;
};
const refreshAccessToken = async () => {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error("刷新令牌不存在");
  }
  try {
    const response = await common_vendor.index.request({
      url: `${CONFIG.BASE_URL}/auth/refresh`,
      method: "POST",
      header: {
        "Content-Type": "application/json"
      },
      data: {
        refreshToken
      },
      timeout: CONFIG.REQUEST_TIMEOUT
    });
    if (response.statusCode === 200 && response.data.code === 200) {
      const newAccessToken = response.data.data;
      common_vendor.index.setStorageSync(STORAGE_KEYS.ACCESS_TOKEN, newAccessToken);
      return newAccessToken;
    } else {
      throw new Error(response.data.message || "令牌刷新失败");
    }
  } catch (error) {
    common_vendor.index.__f__("error", "at utils/auth.js:160", "刷新令牌失败:", error);
    clearLoginInfo();
    throw error;
  }
};
const logout = async () => {
  var _a;
  const accessToken = getAccessToken();
  const userId = (_a = getUserInfo()) == null ? void 0 : _a.userId;
  try {
    const requestOptions = {
      url: `${CONFIG.BASE_URL}/auth/logout`,
      method: "POST",
      header: {
        "Content-Type": "application/json"
      },
      timeout: CONFIG.REQUEST_TIMEOUT
    };
    if (accessToken) {
      requestOptions.header["Authorization"] = `Bearer ${accessToken}`;
    }
    if (userId) {
      requestOptions.data = { userId };
    }
    await common_vendor.index.request(requestOptions);
    common_vendor.index.__f__("log", "at utils/auth.js:196", "登出请求成功");
  } catch (e) {
    common_vendor.index.__f__("warn", "at utils/auth.js:198", "登出请求失败，但继续清理本地数据:", e);
  } finally {
    clearLoginInfo();
    common_vendor.index.__f__("log", "at utils/auth.js:202", "本地登录信息已清理");
    common_vendor.index.reLaunch({ url: "/pages/my/login/login" });
  }
};
const requestWithAuth = async (options) => {
  let token = getAccessToken();
  if (!token)
    throw new Error("未登录");
  if (needRefreshToken()) {
    token = await refreshAccessToken();
  }
  const { url, method = "GET", data = {}, header = {}, timeout = CONFIG.REQUEST_TIMEOUT } = options || {};
  return await common_vendor.index.request({
    url: url.startsWith("http") ? url : `${CONFIG.BASE_URL}${url}`,
    method,
    data,
    timeout,
    header: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
      ...header
    }
  });
};
exports.clearLoginInfo = clearLoginInfo;
exports.getUserInfo = getUserInfo;
exports.isLoggedIn = isLoggedIn;
exports.logout = logout;
exports.needRefreshToken = needRefreshToken;
exports.refreshAccessToken = refreshAccessToken;
exports.requestWithAuth = requestWithAuth;
exports.saveLoginInfo = saveLoginInfo;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/auth.js.map
