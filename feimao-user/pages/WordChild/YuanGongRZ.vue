<template>
	<view class="YG">
		
		<view class="YG_BJ">
			<image src="../../static/YGBJ.jpg" class="imageBFB"></image>
		</view>
		
		<view class="YG_BJ_text" v-if="Men.staff_authentication==4">员工认证未通过，请重新提交</view>
		
		<view class="YG_box">
			<view class="YG_box_L">姓名</view>
			<view class="YG_box_R">
				<u--input placeholder="请填写真实姓名" border="false" v-model="value4"></u--input>
			</view>
		</view>
		
		<view class="YG_box">
			<view class="YG_box_L">电话</view>
			<view class="YG_box_R">
				<u--input placeholder="请填写您的联系电话" border="false" v-model="value5"></u--input>
			</view>
		</view>
		
		
		
		<view class="YG_box" style="border: 0;height: 200rpx;">
			<view class="YG_box_L G">工作证明</view>
			<view class="YG_box_R G">
				<view class="YG_box_R_up" @click="Clickup">
					<view class="YG_box_R_up_box" v-if="!Imagessrc1A">
						<view class="YG_box_R_up_box_icon">
							<image src="../../static/scxiangji.png" class="imageBFB"></image>
						</view>
						<text>上传图片</text>
					</view>
					
					<image :src="Imagessrc1A" class="imageBFB" v-else></image>
				</view>
			</view>
		</view>
		
		<view class="YG_box_btn" @click="ClickTijiao" v-if="Men.staff_authentication==1">提交</view>
		
		<view class="YG_box_btn1" v-if="Men.staff_authentication==2">审核中</view>
		
		<view class="YG_box_btn" v-if="Men.staff_authentication==3">已通过</view>
		
		
		<view class="YG_box_btn" @click="ClickTijiao" v-if="Men.staff_authentication==4">重新提交</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM:getApp().globalData.apiBase,
				value4:'',//姓名
				value5:'',//电话
				Imagessrc1A:'',
				Imagessrc1B:'',
				Men:{
					staff_authentication:1
				}
			}
		},
		onShow() {
			this.MeApi() //个人信息
		},
		methods:{
			MeApi(){
				uni.request({
					url:this.YM + '/api/user/staffAuthenticationInfo',
					method:'POST',
					header:{
						'token':uni.getStorageSync('token')
					},
					success:res=>{
						if (!res.data || res.data.code !== 1 || !res.data.data) {
							uni.showToast({
								title: res.data && res.data.msg ? res.data.msg : '认证信息加载失败',
								icon: 'none'
							})
							return
						}
						this.Men=res.data.data
						console.log(res.data.data,'员工认证信息接口');
						this.value5=this.Men.staff_mobile || ''
						this.value4=this.Men.staff_name || ''
						this.Imagessrc1B=this.Men.staff_work || ''
						this.Imagessrc1A=this.Imagessrc1B ? this.YM + this.Imagessrc1B : ''
					},
				})
			},
			//点击了提交
			ClickTijiao(){
				let data={
					staff_mobile:this.value5,
					staff_name:this.value4,
					staff_work:this.Imagessrc1B
				}
				uni.request({
					url:this.YM + '/api/user/staffAuthentication',
					method:'POST',
					header:{
						'token':uni.getStorageSync('token')
					},
					data,
					success:res=>{
						console.log(res,'员工认证');
						if(res.data.code==1){
							uni.navigateTo({
								url:'/pages/success/successYuanG'
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
				})
			},
			Clickup(){
				uni.chooseImage({
					success: (chooseImageRes) => {
						const tempFilePaths = chooseImageRes.tempFilePaths;
						uni.uploadFile({
							url: this.YM + '/api/common/upload',
							filePath: tempFilePaths[0],
							name: 'file',
							formData: {
								token: uni.getStorageSync('token')
							},
							success: (res) => {
								let Ima = JSON.parse(res.data)
								console.log(Ima, 'Ima-Ima-Ima');
								this.Imagessrc1A = this.YM + Ima.data.url
								this.Imagessrc1B = Ima.data.url
							}
						});
					}
				});
			}
			
		}
	}
</script>

<style>
.YG{
	width: 100vw;
	height: 100vh;
	background-color: #FFFFFF;
}

.YG_BJ{
	width: 100%;
	height: 152rpx;
	/* background-color: rosybrown; */
}
.YG_box{
	width:690rpx;
	height: 130rpx;
	/* background-color: aqua; */
	border-bottom: 1rpx solid #F8F8F8;
	margin: 0rpx auto 0;
	display: flex;
}
.YG_box_L{
	width: 150rpx;
	height: 100%;
	/* background-color: darksalmon; */
	text-indent: 0.3em;
	line-height: 130rpx;
	font-size: 30rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #010101;
}
.YG_box_R{
	width: 500rpx;
	height: 100%;
	/* background-color: gold; */
	display: flex;
	align-items: center;
}
.G{
	display: flex;
	align-items: center;
	justify-content: flex-start;
}
.YG_box_R_up{
	width: 160rpx;
	height: 160rpx;
	background: #F8F8F8;
	border-radius: 10rpx;
	overflow: hidden;
	display: flex;
	align-items: center;
	justify-content: center;
}
.YG_box_R_up_box{
	width: 100rpx;
	height: 100rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-wrap: wrap;
	font-size: 24rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #858896;
}
.YG_box_R_up_box_icon{
	width: 46rpx;
	height: 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}
.YG_box_btn,
.YG_box_btn1{
	width: 556rpx;
	height: 88rpx;
	background: #ED9939;
	border-radius: 44rpx;
	font-size: 30rpx;
	font-family: PingFang SC;
	font-weight: bold;
	color: #FFFFFF;
	text-align: center;
	line-height: 88rpx;
	margin: 30rpx auto 0;
}
.YG_box_btn1{
	opacity: 0.4;
}
.YG_BJ_text{
	width: 690rpx;
	height: 60rpx;
	line-height: 60rpx;
	color: red;
	font-size: 26rpx;
	font-family: PingFang SC;
	font-weight: 500;
	margin: 0 auto;
}
</style>
