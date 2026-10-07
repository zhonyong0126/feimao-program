<template>
	<view class="success">
		
		<!-- LOGO -->
		<view class="success_logo">
			<image src="../../static/ccg.png" class="imageBFB"></image>
		</view>
		<view class="success_title">您已成功支付</view>
		<view class="success_text">如有疑问请联系客服: {{Phone}}</view>
		<view class="success_btn" @click="ClickCK">查看订单</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				Phone:'114'
			}
		},
		onShow() {
			this.Phone=uni.getStorageSync('Phone')
		},
		methods: {
			ClickCK(){
				console.log('点击了查看订单');
				uni.switchTab({
					url:'/pages/index/order'
				})
			}
		}
	}
</script>

<style>
.success{
	width: 100vw;
	height: 100vh;
	float: left;
	background-color: #FFFFFF;
}

.success_logo{
	width: 126rpx;
	height: 126rpx;
	border-radius: 50%;
	/* background-color: green; */
	overflow: hidden;
	margin: 85rpx auto 0;
}
.success_title{
	width: 100%;
	height: 70rpx;
	text-align: center;
	line-height: 70rpx;
	font-size: 28rpx;
	font-family: PingFang SC;
	color: #010101;
	margin: 30rpx auto 0;
	font-weight: 600;
}
.success_text{
	width: 100%;
	height: 30rpx;
	text-align: center;
	line-height: 30rpx;
	font-size: 26rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #999999;
	margin: 10rpx auto 0;
}
.success_btn{
	width: 240rpx;
	height: 88rpx;
	border: 2rpx solid #E38B27;
	border-radius: 44rpx;
	font-size: 30rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #ED9939;
	text-align: center;
	line-height: 88rpx;
	margin: 50rpx auto 0;
}
</style>
