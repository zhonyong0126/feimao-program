<template>
	<view class="shezhi">

		<view class="shezhi_box">

			<view class="shezhi_box_toux" @click="ClickUP">
				<view class="shezhi_box_toux_image">
					<image :src="YM + Imagessrc1B" class="imageBFB"></image>
				</view>
				<view class="shezhi_box_toux_xiangji">
					<image src="../../static/xiangjiH.png" class="imageBFB"></image>
				</view>
			</view>

			<view class="shezhi_box_box">
				<view class="shezhi_box_boxn">
					<view class="shezhi_box_boxn_L">昵称</view>
					<view class="shezhi_box_boxn_R">
						<u--input placeholder="请输入" border="false" v-model="value4"></u--input>
					</view>
				</view>

				<view class="shezhi_box_boxn">
					<view class="shezhi_box_boxn_L">电话</view>
					<view class="shezhi_box_boxn_R">
						<u--input placeholder="请输入" border="false" v-model="value5"></u--input>
					</view>
				</view>
			</view>

		</view>

		<view class="Btn">
			<view class="shezhi_btn1" @click="Clickbc">保存</view>
			<view class="shezhi_btn2" @click="ClickTc">退出登录</view>
		</view>

	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				value4: '',
				value5: '',
				Imagessrc1A: '',
				Imagessrc1B: ''
			}
		},
		onShow() {
			this.MeApi() //个人信息
		},
		methods: {
			MeApi() {
				uni.request({
					url: this.YM + '/api/user/info',
					method: 'POST',
					header:{
						'token': uni.getStorageSync('token')
					},
					success: res => {
						console.log(res.data.data, '个人信息');
						this.value4 = res.data.data.nickname
						this.value5 = res.data.data.mobile
						this.Imagessrc1B = res.data.data.avatar
					},
				})
			},
			ClickUP() {
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
			},
			//点击了保存
			Clickbc() {
				console.log('点击了保存');
				let data = {
					avatar: this.Imagessrc1B,
					nickname: this.value4,
					mobile: this.value5
				}
				uni.request({
					url: this.YM + '/api/user/edit',
					method: 'POST',
					data,
					header:{
						'token': uni.getStorageSync('token'),
					},
					success: res => {
						console.log(res, '个人信息修改');
						if (res.data.code == 1) {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 2000
							});
							setTimeout(()=>{
								uni.switchTab({
									url: '/pages/index/word'
								})
							},1500)
							
						} else {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 2000
							});
							return false
						}
					},
				})
			},
			//点击了退出登录
			ClickTc() {
				console.log('点击了退出登录');
				uni.request({
					url:this.YM + '/api/user/logout',
					method:'POST',
					header:{
						'token':uni.getStorageSync('token')
					},
					success:res=>{
						if(res.data.code==1){
							uni.showToast({
								title:'退出成功',
								icon:'none',
								duration: 1500
							});
							uni.removeStorageSync('token')
							uni.clearStorageSync()
							setTimeout(()=>{
								uni.switchTab({
									url:'/pages/index/word'
								})
							},1500)
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
			}
		}
	}
</script>

<style>
	.shezhi {
		width: 100vw;
		height: 100vh;
		background-color: #F7F7F7;
	}

	.shezhi_box {
		width: 100%;
		height: 550rpx;
		background-color: #FFFFFF;
		float: left;
	}

	.shezhi_box_toux {
		width: 160rpx;
		height: 160rpx;
		/* background-color: green; */
		position: relative;
		margin: 90rpx auto 0;
	}

	.shezhi_box_toux_image {
		width: 160rpx;
		height: 160rpx;
		border-radius: 50%;
		/* background-color: green; */
		position: absolute;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		z-index: 1;
		overflow: hidden;
	}

	.shezhi_box_toux_xiangji {
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		/* background-color: orangered; */
		position: absolute;
		right: 0;
		bottom: 0;
		z-index: 2;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.shezhi_box_box {
		width: 100%;
		height: 220rpx;
		/* background-color: aqua; */
		margin-top: 80rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-wrap: wrap;
	}

	.shezhi_box_boxn {
		border-bottom: 1rpx solid #F8F8F8;
		width: 675rpx;
		height: 110rpx;
		/* background-color: orange; */
		display: flex;

	}

	.shezhi_box_boxn_L {
		width: 150rpx;
		height: 100%;
		text-align: left;
		line-height: 110rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.shezhi_box_boxn_R {
		width: 500rpx;
		height: 100%;
		display: flex;
		align-items: center;
	}

	.shezhi_box_box>view:nth-child(2) {
		border: 0;
		/* background-color: violet; */
	}

	.Btn {
		width: 100%;
		height: 300rpx;
		float: left;
		margin-top: 30rpx;
	}

	.shezhi_btn1,
	.shezhi_btn2 {
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
		margin: 20rpx auto 0;

	}

	.shezhi_btn2 {
		border: 2rpx solid #ED9939;
		color: #ED9939;
		background-color: #FFFFFF;
	}
</style>
