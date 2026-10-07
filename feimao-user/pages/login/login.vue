<template>
	<view class="login">
		
		<!-- LOGO -->
		<view class="login_LOGO">
			<image src="../../static/Clogo.jpg" class="imageBFB"></image>
		</view>
		
		<view class="login_btn" @click="ClickDL">微信授权</view>
		<view class="login_radio">
			<view class="login_radio_radio">
				<radio :value="value" :checked="checked" @click="RadioClick" class="RadioCss" color='#1470ff' />
			</view>
			<text>登录即代表您阅读并同意<text style="color: #206CFF;" @click="ClickYH">《用户协议》</text>和<text style="color: #206CFF;" @click="ClickYS">《隐私政策》</text></text>
		</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM:getApp().globalData.apiBase,
				value:'',
				checked:false,
				clickTimer:null,
				OCDE:''
			}
		},
		methods: {
			//用户协议
			ClickYH(){
				uni.navigateTo({
					url:'/pages/login/YH'
				})
			},
			//隐私政策
			ClickYS(){
				uni.navigateTo({
					url:'/pages/login/YS'
				})
			},
			//点击了授权登录
			ClickDL(){
				 const LoginIFData = uni.getStorageSync('LoginIF');
				
				    // 清除全部缓存
				    // uni.clearStorageSync();
				
				    // 重新设置名为LoginIF的缓存数据
				    uni.setStorageSync('LoginIF', LoginIFData);
				console.log('点击了授权登录');
				if(this.checked==false){
					uni.showToast({
						title:'请勾选用户协议',
						icon:'none',
						duration: 2000
					});
					return false
				}else{
					// 清除之前的定时器
					clearTimeout(this.clickTimer);
					uni.showLoading({
						title: '正在登录'
					});
					uni.getLocation({
						type: "wgs84", //返回可以用于 wx.openLocation 的经纬度
						success(res) {
							// console.log(res, '获取到的经纬度');
							const lat = res.latitude;
							const lng = res.longitude;
							uni.setStorageSync('lat', lat)
							uni.setStorageSync('lng', lng)
					
						}
					});
					// 设置新的定时器
					this.clickTimer = setTimeout(() => {
						//获取code
						uni.login({
							success: (res) => {
								console.log(res,'uni.login');
								uni.setStorageSync('code', res.code)
								this.OCDE=res.code
							}
						})
						// 获取用户信息
						uni.getUserInfo({
							desc: '获取用户信息展示',
							success: (res) => {
								console.log(res,'uni.getUserInfo');
								let data={
									code: this.OCDE,
									encryptedData: res.encryptedData,
									iv: res.iv,
								}
								uni.request({
									url: this.YM + '/api/user/login',
									method: 'POST',
									data,
									success: res => {
										console.log(res,'获取用户信息');
										if(res.data.code==1){
											uni.hideLoading();
											uni.showToast({
												title:res.data.msg,
												icon:'none',
												duration: 1500
											});
											uni.setStorageSync('token',res.data.data.userInfo.token)
											uni.setStorageSync('user_id',res.data.data.userInfo.user_id)
											
											uni.navigateBack()
											console.log('执行');
										}else{
											uni.showToast({
												title:res.data.msg,
												icon:'none',
												duration: 2000
											});
											return false
										}
									
									},
								})
							},
						})
					}, 250); // 设置延迟时间，单位为毫秒
					return false;
				}
			},
			RadioClick() {
				this.checked = !this.checked
				console.log('当前radio状态:', this.checked);
			},
		}
	}
</script>

<style>
.login{
	width: 100vw;
	height: 100vh;
	background-color: #FFFFFF;
	float: left;
}

.login_LOGO{
	width: 450rpx;
	height: 155rpx;
	border-radius: 50%;
	overflow: hidden;
	margin: 120rpx auto 0;
}

.login_btn{
	width: 556rpx;
	height: 88rpx;
	line-height: 88rpx;
	border-radius: 44rpx;
	background-color: rgba(237, 153, 57, 1);
	color: rgba(255, 255, 255, 1);
	font-size: 28rpx;
	text-align: center;
	font-family: Arial;
	margin: 150rpx auto 0;
}
.login_radio{
	width: 100%;
	height: 40rpx;
	/* background-color: darkblue; */
	display: flex;
	align-items: center;
	justify-content: center;
	margin-top: 15rpx;
	color: rgba(149, 149, 149, 1);
	font-size: 24rpx;
}
.login_radio_radio{
	width: 28rpx;
	height: 28rpx;
	/* background-color: green; */
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 10rpx;
}
.RadioCss {
		transform: scale(0.8)
	}
</style>
