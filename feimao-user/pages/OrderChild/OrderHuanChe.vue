<template>
	<view class="HC">
		
		<view class="HC_title">
			<text style="font-weight: bold;font-size: 30rpx;color: #010101;">车辆图片</text>
			<text>拍照上传，前后左右方位照片各一张  车架号码一张(最多上传四张图片)</text>
		</view>
		
		<view class="HC_imagebox">
			<!-- 图片 -->
			<view class="HC_imagebox_src" v-for="(item,index) in Imagesrc" :key="index">
				<image :src="YM + item" class="imageBFB"></image>
			</view>
			
			<!-- 上传 -->
			<view class="HC_imagebox_up" @click="ClickUp" v-if="Imagesrc.length<4">
				<view class="HC_imagebox_up_box">
					<view class="HC_imagebox_up_box_icon">
						<image src="../../static/scxiangji.png" class="imageBFB"></image>
					</view>
					<text>上传图片</text>
				</view>
			</view>
		</view>
		
		<view class="HC_title marT">
			<text style="font-weight: bold;font-size: 30rpx;color: #010101;">车辆情况描述</text>
		</view>
		
		<view class="HC_input">
			<u--textarea v-model="value5" border='none' count height='153'
				placeholder="请输入"></u--textarea>
		</view>
		
		<view class="HC_btn" @click="Clickbtn">确定还车</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM:getApp().globalData.apiBase,
				Imagesrc:[],
				value5:''
			}
		},
		methods: {
			//确定还车
			Clickbtn(){
				console.log('图片',this.Imagesrc);
				console.log('车辆情况',this.value5);
				if(this.Imagesrc==''){
					uni.showToast({
						title:'请上传车辆图片',
						icon:'none',
						duration: 2000
					});
					return false
				}else{
					
				let data={
					back_image:this.Imagesrc.toString(),
					back_content:this.value5
				}
				uni.request({
					url:this.YM + '/api/order/backCar',
					method:'POST',
					header:{
						'token':uni.getStorageSync('token'),
					},
					data,
					success:res=>{
						console.log(res,'确定换车');
						if(res.data.code==1){
							uni.navigateTo({
								url:'/pages/success/successHuanChe'
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
				}
			},
			//上传图片
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
								let Ima = JSON.parse(res.data)
								console.log(Ima,'Ima');
								this.Imagesrc.push(Ima.data.url)
							}
						});
					}
				});
			},
		}
	}
</script>

<style>
.HC{
	width: 100vw;
	height: 100vh;
	background-color: #FFFFFF;
	float: left;
}

.HC_title{
	width: 680rpx;
	height: 60rpx;
	margin: 20rpx auto 0;
	/* background-color: yellow; */
	font-size: 24rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #ff007f;
	/* display: flex; */
	/* align-items: center; */
}

.HC_imagebox{
	width: 680rpx;
	min-height: 160rpx;
	/* background-color: skyblue; */
	display: flex;
	align-items: center;
	justify-content: flex-start;
	flex-wrap: wrap;
	margin: 20rpx auto 0;
}
.HC_imagebox_src{
	width: 160rpx;
	height: 160rpx;
	background: #F8F8F8;
	border-radius: 10rpx;
	overflow: hidden;
	margin-bottom: 20rpx;
	margin-right: 15rpx;
}

.HC_imagebox_up{
	width: 160rpx;
	height: 160rpx;
	background: #F8F8F8;
	border-radius: 10rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 20rpx;
}
.HC_imagebox_up_box_icon{
	width: 46rpx;
	height: 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 5rpx;
}
.HC_imagebox_up_box{
	width: 100rpx;
	height: 70rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-wrap: wrap;
	font-size: 24rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #858896;
}
.marT{
	margin: 50rpx auto 0;
}
.HC_input{
	width: 680rpx;
	height: 346rpx;
	background: #F8F8F8;
	border-radius: 10rpx;
	margin: 20rpx auto 0;
}

.HC_btn{
	width: 556rpx;
	height: 88rpx;
	background: #ED9939;
	border-radius: 44rpx;
	font-size: 30rpx;
	font-family: PingFang SC;
	font-weight: bold;
	color: #FFFFFF;
	margin: 50rpx auto 0;
	text-align: center;
	line-height: 88rpx;
}
</style>
