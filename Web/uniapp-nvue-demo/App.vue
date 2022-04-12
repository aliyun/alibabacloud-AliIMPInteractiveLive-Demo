<script>
	import RoomEngine from './pages/RoomPaasSdk.uni.min.js'
	export default {
		globalData: {
			appId: '****',
			appKey: '****',
			deviceId: '****', // 请确保每个设备都不同
			roomEngine: RoomEngine.getInstance(),
			origin: 'https://a.b.com'
		}
	}
</script>

<style>
	/*每个页面公共css */
</style>
