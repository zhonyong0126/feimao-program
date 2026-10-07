<template>
	<view class="gywm">
		
		<view class="gywm_text">
			<u-parse :content="content"></u-parse>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM:getApp().globalData.apiBase,
				content:''
			}
		},
		onShow() {
			uni.request({
				url:this.YM + '/api/index/config',
				method:'POST',
				success:res=>{
					console.log(res.data.data,'隐私政策');
					
					const targetData1 = res.data.data.find(item => item.name === "about_us");
					if (targetData1) {
						this.content = targetData1.value
						console.log(targetData1.value);
					} else {
						this.content = '后台暂未添加，请联系工作人员'
						console.log('未找到匹配的数据');
					}
				}
			})
		},
		methods: {
			
		}
	}
</script>

<style>
.gywm{
	width: 100vw;
	height: 100vh;
	background-color: #ffffff;
	float: left;
}
/* .gywm_logo{
	width: 183rpx;
	height: 200rpx;
	margin: 80rpx auto 0;
} */
.gywm_text{
	width: 90%;
	/* height: 800rpx; */
	/* background-color: yellow; */
	margin: 30rpx auto 0;
	font-size: 28rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #333333;
}
</style>
