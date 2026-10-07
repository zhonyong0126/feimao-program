<template>
	<view class="gz">
		<view class="gz_title">订单编号</view>
		<view class="gz_input">
			<u--input placeholder="请输入订单编号" border="false" v-model="value4"></u--input>
		</view>
		
		<view class="gz_title">车辆图片</view>
		<view class="I">
			
			<view class="gz_up" @click="ClickUp">
				
				<image :src="Imagessrc1A" class="imageBFB" v-if="Imagessrc1A!==''"></image>
				
				<view class="gz_up_box" v-else>
					<view class="gz_up_box_icon">
						<image src="../../static/scxiangji.png" class="imageBFB"></image>
					</view>
					<text>上报图片</text>
				</view>
			</view>
		</view>
		
		<view class="gz_title">故障内容</view>
		<view class="gz_textinput">
			<u--textarea v-model="value5" border='none' count height='150'
				placeholder="请输入故障内容"></u--textarea>
		</view>
		
		<view class="gz_btn" @click="ClickTj">提交</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM:getApp().globalData.apiBase,
				value4:'',//订单编号
				value5:'',//故障内容
				Imagessrc1:'',//传后台
				Imagessrc1A:'',//回显
				isSubmitting:false
			}
		},
		methods: {
			//提交
			ClickTj(){
				if (this.isSubmitting) return
				if (!this.value4.trim() || !this.value5.trim() || !this.Imagessrc1) {
					uni.showToast({title: '请完整填写订单、图片和故障内容', icon: 'none'})
					return
				}
				let data={
					image:this.Imagessrc1,
					content:this.value5,
					number:this.value4
				}
				this.isSubmitting = true
				uni.request({
					url:this.YM + '/api/user/malfunction',
					method:'POST',
					data,
					header:{
						'token':uni.getStorageSync('token'),
					},
					success:res=>{
						this.isSubmitting = false
						if(res.data.code==1){
							uni.reLaunch({
								url:'/pages/success/successGuz'
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
					fail:()=>{
						this.isSubmitting = false
						uni.showToast({title: '提交失败，请稍后重试', icon: 'none'})
					}
				})
			},
			ClickUp(){
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
								let Ima
								try { Ima = JSON.parse(res.data) } catch (error) {
									uni.showToast({title: '图片上传失败', icon: 'none'})
									return
								}
								if (!Ima || Ima.code !== 1 || !Ima.data || !Ima.data.url) {
									uni.showToast({title: Ima && Ima.msg ? Ima.msg : '图片上传失败', icon: 'none'})
									return
								}
								console.log(Ima,'Ima-Ima-Ima');
								this.Imagessrc1 =Ima.data.url
								this.Imagessrc1A=this.YM + Ima.data.url
								console.log('上传的图片:', this.Imagessrc1);
							},
							fail:()=>uni.showToast({title: '图片上传失败', icon: 'none'})
						});
					}
				});
			}
		}
	}
</script>

<style>
.gz{
	width: 100vw;
	height: 100vh;
	background-color: #FFFFFF;
	float:left;
}
.gz_title{
	width: 700rpx;
	height: 60rpx;
	line-height: 60rpx;
	font-size: 30rpx;
	font-family: PingFang SC;
	font-weight: bold;
	color: #010101;
	margin: 20rpx auto 0;
}
.gz_input{
	width: 700rpx;
	height: 90rpx;
	background: #F8F8F8;
	border-radius: 10rpx;
	margin: 10rpx auto 0;
	display: flex;
	align-items: center;
}
.I{
	width: 700rpx;
	height: 160rpx;
	margin: 10rpx auto 0;
}
.gz_up{
	width: 160rpx;
	height: 160rpx;
	background: #F8F8F8;
	border-radius: 10rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
}
.gz_up_box_icon{
	width: 46rpx;
	height: 40rpx;
	/* background-color: green; */
	display: flex;
	align-items: center;
	justify-content: center;
}
.gz_up_box{
	width: 110rpx;
	height: 80rpx;
	/* background-color: darkseagreen; */
	font-size: 24rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #858896;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-wrap: wrap;
}
.gz_textinput{
	width: 700rpx;
	height: 346rpx;
	background: #F8F8F8;
	border-radius: 10rpx;
	margin: 10rpx auto 0;
}

.gz_btn{
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
	margin: 40rpx auto 0;
}
</style>
