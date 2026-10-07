<template>
	<view class="SMRZ">
		
		<view class="SMRZ_text">根据国家的相关法律法规规定以及确保账号的真实性和安全性，需进行本人实名认证，</view>
		<!-- <view class="SMRZ_text" style="color: red;">实名认证未通过，请重新提交</view> -->
		
		<view class="SMRZ_inp">
			<view class="SMRZ_inp_title">真实姓名(必填)</view>
			<view class="SMRZ_inp_input">
				<u--input placeholder="请输入您的真实姓名" border="false" v-model="value1"></u--input>
			</view>
		</view>
		
		<view class="SMRZ_inp">
			<view class="SMRZ_inp_title">身份证号(必填)</view>
			<view class="SMRZ_inp_input">
				<u--input placeholder="请输入您的身份证号" border="false" v-model="value2"></u--input>
			</view>
		</view>
		
		<view class="SMRZ_inp">
			<view class="SMRZ_inp_title">手机号(必填)</view>
			<view class="SMRZ_inp_input">
				<u--input placeholder="请输入您的手机号" border="false" v-model="value3"></u--input>
			</view>
		</view>
		
		<view class="SMRZ_up" @click="ClickUpsfz">
			<view class="SMRZ_up_image" v-if="Imagessrc1=='' || Imagessrc1==null">
				<image src="../../static/scxiangji.png" class="imageBFB"></image>
			</view>
			<view class="SMRZ_up_text" v-if="Imagessrc1=='' || Imagessrc1==null">上传身份证正面图</view>
			<image :src="Imagessrc1A" class="imageBFB" v-if="Imagessrc1!=='' && Imagessrc1!=null"></image>
		</view>
		
		<view class="SMRZ_btn" @click="ClickCG" v-if="MES.authentication==1">提交</view>
		<view class="SMRZ_btn1"  v-if="MES.authentication==2">已提交待审核</view>
		<view class="SMRZ_btn1"  v-if="MES.authentication==3">认证成功</view>
		<view class="SMRZ_btn" @click="ClickCGcx" v-if="MES.authentication==4">重新提交</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM:getApp().globalData.apiBase,
				value1:'',
				value2:'',
				value3:'',
				MES:'',
				Imagessrc1:'',
				Imagessrc1A:''
			}
		},
		onShow() {
			this.SMZ()
		},
		methods: {
			//点击上传身份证
			ClickUpsfz(){
				// console.log(this.value1,'姓名');
				// console.log(this.value2,'身份证号码');
				// console.log(this.value3,'手机号');
				// uni.setStorageSync('value1',this.value1)
				// uni.setStorageSync('value2',this.value2)
				// uni.setStorageSync('value3',this.value3)
				
				
				uni.chooseImage({
					success: (chooseImageRes) => {
						const tempFilePaths = chooseImageRes.tempFilePaths;
						uni.uploadFile({
							url: this.YM + '/api/common/upload',
							filePath: tempFilePaths[0],
							name: 'file',
							formData:{
								token:uni.getStorageSync('token'),
							},
							success: (res) => {
								let Ima = JSON.parse(res.data)
								console.log(Ima,'Ima-Ima-Ima');
								this.Imagessrc1 =Ima.data.url
								this.Imagessrc1A=this.YM + Ima.data.url
								console.log('上传的图片:', this.Imagessrc1);
								
								// this.value1=uni.getStorageSync('value1')
								// this.value2=uni.getStorageSync('value2')
								// this.value3=uni.getStorageSync('value3')
								
							}
						});
					}
				});
			},
			
			//重新提交
			ClickCGcx(){
				// this.value1='',
				// this.value2='',
				// this.value3='',
				// this.MES.authentication=1
				let daysd={
					authentication_mobile:this.value3,
					authentication_name:this.value1,
					authentication_card:this.value2,
					authentication_image:this.Imagessrc1
				}
				uni.request({
					url:this.YM + '/api/user/authentication',
					method:'POST',
					header: {
						token: uni.getStorageSync('token')
					},
					success:res=>{
						console.log(res,'实名认证');
						if(res.data.code==1){
							uni.navigateTo({
								url:'/pages/success/successSMRZ'
							})
						}else{
							uni.showToast({
								title:res.data.msg,
								icon:'none',
								duration: 1500
							});
							return false
						}
					},
					data: daysd,
				})
			},
			// 实名认证返回信息
			SMZ(){
				uni.request({
					url:this.YM + '/api/user/authenticationInfo',
					method:'POST',
					header: {
						token: uni.getStorageSync('token')
					},
					success:res=>{
						if (!res.data || res.data.code !== 1 || !res.data.data) {
							uni.showToast({
								title: res.data && res.data.msg ? res.data.msg : '认证信息加载失败',
								icon: 'none'
							})
							return
						}
						console.log(res.data.data,'实名认证信息返回');
						this.MES=res.data.data
						// authentication
						if(res.data.data.authentication_mobile){
							this.value3=res.data.data.authentication_mobile
						}
						if(res.data.data.authentication_name){
							this.value1=res.data.data.authentication_name
						}
						if(res.data.data.authentication_card){
							this.value2=res.data.data.authentication_card
						}
						if(res.data.data.authentication_image){
							this.Imagessrc1A=this.YM + res.data.data.authentication_image
						}
						if(res.data.data.authentication_image){
							this.Imagessrc1=res.data.data.authentication_image
						}
					},
				})
			},
			// 提交
			ClickCG(){
				let daysd={
					authentication_mobile:this.value3,
					authentication_name:this.value1,
					authentication_card:this.value2,
					authentication_image:this.Imagessrc1
				}
				uni.request({
					url:this.YM + '/api/user/authentication',
					method:'POST',
					header: {
						token: uni.getStorageSync('token')
					},
					success:res=>{
						console.log(res,'实名认证');
						if(res.data.code==1){
							uni.navigateTo({
								url:'/pages/success/successSMRZ'
							})
						}else{
							uni.showToast({
								title:res.data.msg,
								icon:'none',
								duration: 1500
							});
							return false
						}
					},
					data: daysd,
				})
			}
		}
	}
</script>

<style>
.SMRZ{
	width: 100vw;
	min-height: 100vh;
	background-color:#FFFFFF;
	float: left;
}

.SMRZ_text{
	width: 700rpx;
	height: 110rpx;
	/* background-color: violet; */
	margin: 20rpx auto 0;
	font-size: 26rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #B8B8B8;
}

.SMRZ_inp{
	width: 700rpx;
	height: 150rpx;
	margin: 40rpx auto 0;
	border-bottom: 1rpx solid #EDEDED;
	/* background-color: aqua; */
}
.SMRZ_up{
	width: 700rpx;
	height: 400rpx;
	border-radius: 12rpx;
	overflow: hidden;
	display: flex;
	align-items: center;
	justify-content: center;
	margin: 40rpx auto 0;
	flex-wrap: wrap;
	border: 1rpx solid #EDEDED;
}
.SMRZ_up_image{
	width: 100rpx;
	height: 90rpx;
	/* background-color: green; */
	display: flex;
	align-items: center;
	justify-content: center;
	margin-top: 130rpx;
}
.SMRZ_up_text{
	width: 100%;
	height: 50rpx;
	text-align: center;
	line-height: 50rpx;
	margin-bottom: 90rpx;
	font-size: 26rpx
	
}
.SMRZ_inp_title{
	width: 100%;
	height: 50rpx;
	/* background-color: aliceblue; */
	line-height: 50rpx;
	font-size: 30rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #010101;
	text-indent: 0.7em;
}
.SMRZ_inp_input{
	width: 100%;
	height: 60rpx;
	display: flex;
	align-items: center;
	margin-top: 20rpx;
}

.SMRZ_btn,
.SMRZ_btn1{
	width: 556rpx;
	height: 88rpx;
	background: #ED9939;
	border-radius: 44rpx;
	font-size: 28rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #FFFFFF;
	text-align: center;
	line-height: 88rpx;
	margin: 80rpx auto 0;
}
.SMRZ_btn1{
	background: #ED9939;
	opacity: 0.5;
}
</style>
