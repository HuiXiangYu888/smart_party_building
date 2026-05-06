<script>
import { isLoggedIn, needRefreshToken, refreshAccessToken, clearLoginInfo } from './utils/auth.js'

export default {
	onLaunch: function() {
		console.log('App Launch')
		this.initApp()
	},
	onShow: function() {
		console.log('App Show')
	},
	onHide: function() {
		console.log('App Hide')
	},
	methods: {
		// 初始化应用
		initApp() {
			// 检查登录状态
			if (isLoggedIn()) {
				// 检查是否需要刷新令牌
				if (needRefreshToken()) {
					this.refreshTokenSilently()
				}
			} else {
				// 未登录，统一跳转登录页
				uni.reLaunch({ url: '/pages/my/login/login' })
			}
		},
		
		// 静默刷新令牌
		async refreshTokenSilently() {
			try {
				await refreshAccessToken()
				console.log('令牌刷新成功')
			} catch (error) {
				console.error('令牌刷新失败:', error)
				// 刷新失败，清除登录信息
				clearLoginInfo()
			}
		}
	}
}
</script>

<style>
	/*每个页面公共css */
</style>
