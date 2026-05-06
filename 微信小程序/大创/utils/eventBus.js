/**
 * 简单的事件总线
 * 用于页面间通信和状态同步
 */

class EventBus {
    constructor() {
        this.events = {}
    }

    /**
     * 监听事件
     * @param {string} event 事件名
     * @param {function} callback 回调函数
     */
    on(event, callback) {
        if (!this.events[event]) {
            this.events[event] = []
        }
        this.events[event].push(callback)
    }

    /**
     * 移除事件监听
     * @param {string} event 事件名
     * @param {function} callback 回调函数
     */
    off(event, callback) {
        if (!this.events[event]) return
        
        const index = this.events[event].indexOf(callback)
        if (index > -1) {
            this.events[event].splice(index, 1)
        }
    }

    /**
     * 触发事件
     * @param {string} event 事件名
     * @param {any} data 数据
     */
    emit(event, data) {
        if (!this.events[event]) return
        
        this.events[event].forEach(callback => {
            try {
                callback(data)
            } catch (error) {
                console.error('事件回调执行错误:', error)
            }
        })
    }

    /**
     * 移除所有事件监听
     */
    removeAllListeners() {
        this.events = {}
    }
}

// 创建全局事件总线实例
const eventBus = new EventBus()

// 导出事件总线实例
export default eventBus

// 导出常用的事件名常量
export const EVENTS = {
    USER_LOGIN: 'user_login',
    USER_LOGOUT: 'user_logout',
    USER_INFO_UPDATED: 'user_info_updated'
}
