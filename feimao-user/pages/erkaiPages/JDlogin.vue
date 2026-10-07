<template>
	<view class="JD">
		<view class="JD_Back" @click="Back">
			<image src="../../static/HeiBack.png" class="imageBFB"></image>
		</view>
		
		<view class="JD_title">
			<text>欢迎登录电动车租赁</text><br />
			<text>酒店端</text>
		</view>
		
		<view class="JD_input">
			<view class="JD_input_icon">
				<image src="../../static/JDadmin.png" class="imageBFB"></image>
			</view>
			<u--input placeholder="请输入账号" border="false" v-model="username"></u--input>
		</view>
		
		<view class="JD_input" style="margin: 25rpx auto 0;">
			<view class="JD_input_icon">
				<image src="../../static/JDpass.png" class="imageBFB"></image>
			</view>
			<u--input placeholder="请输入密码" type='password' border="false" v-model="password"></u--input>
		</view>
		
		<view class="JD_btn" @click="LinkJDindex">登录</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				password:'',
				username:''
			}
		},
		methods: {
			LinkJDindex(){
				uni.request({
					url:this.YM + '/api/shop/login',
					method:'POST',
					data:{
						username:this.username,
						password:this.password
					},
					success:res=>{
						console.log(res,'登录');
						if(res.data.code==1){
							uni.setStorageSync('JDtoken',res.data.data.userInfo.token)
							uni.navigateTo({
								url:'/pages/erkaiPages/JDindex'
							})
						}else{
							uni.showToast({
								title:res.data.msg,
								icon:'none',
								duration: 2000
							});
							return false
						}
					}
				})
				
			},
			Back(){
				uni.navigateBack()
			}
		}
	}
</script>

<style>
.JD {
    width: 100vw;
    height: 100vh;
    background: linear-gradient(to bottom, #FFF4D7 20%, #FFFFFF 80%);
    float: left;
}
.JD_Back{
	width: 42rpx;
	height: 42rpx;
	margin-top: 112rpx;
	margin-left: 35rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}
.JD_title{
	margin-left: 45rpx;
	margin-top: 80rpx;
	color: rgba(0, 0, 0, 1.0);
	font-size: 40rpx;
	font-weight: 600;
}
.JD_input{
	width: 628rpx;
	height: 98rpx;
	line-height: 98rpx;
	border-radius: 16rpx;
	background-color: rgba(255, 255, 255, 1);
	text-align: center;
	box-shadow: 0px 4rpx 12rpx 0px rgba(0, 0, 0, 0.04);
	margin: 70rpx auto 0;
	display: flex;
	align-items: center;
}
.JD_input_icon{
	width: 40rpx;
	height: 40rpx;
	margin-left: 30rpx;
	display: flex;
	align-items: center;
	justify-self: unset;
	margin-right: 15rpx;
}

.JD_btn{
	width: 628rpx;
	height: 96rpx;
	line-height: 96rpx;
	border-radius: 118rpx;
	background-color: rgba(237, 153, 57, 1);
	color: rgba(255, 255, 255, 1);
	font-size: 28rpx;
	text-align: center;
	font-family: Arial;
	margin: 60rpx auto 0;
}
</style>
