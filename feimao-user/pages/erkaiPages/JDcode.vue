<template>
	<view class="COde">
		<image src="../../static/JDcode.jpg" class="imageBFB"></image>
		
		<view class="COde_Img flex">
			<image :src="YM + code" class="imageBFB" @click="YL"></image>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				code:''
			}
		},
		onLoad(options) {
			console.log(options.JDcode);
			this.code=options.JDcode
		},
		methods: {
			YL(){
				var SrcImage = this.YM + this.code;
				uni.previewImage({
					urls: [SrcImage], // 将Yitme.content封装在数组中
					success: res => {
						console.log(res, '走进来了 预览');
					},
					longPressActions: {
						itemList: ['发送给朋友', '保存图片', '收藏'],
						success: function(data) {
							console.log('选中了第' + (data.tapIndex + 1) + '个按钮,第' + (data.index + 1) + '张图片');
						},
						fail: (err)=> {
							console.log(err.errMsg);
						}
					}
				});
			}
		}
	}
</script>

<style>
.COde{
	width: 100vw;
	height: 100vh;
	position: relative;
}
.COde_Img{
	width: 298rpx;
	height: 298rpx;
	position: absolute;
	left: 50%;
	top: 48%;
	z-index: 999;
	transform: translate(-50%,-50%);
}
</style>
