<template>
	<view class="Rz">

		<view class="Rz_haerder">
			<image src="../../static/RZBJ.jpg" class="imageBFB"></image>
			<view class="Rz_haerder_titlke">酒店入驻</view>
			<view class="Rz_haerder_Back" @click="Back">
				<image src="../../static/BaiZ.png" class="imageBFB"></image>
			</view>
		</view>

		<view class="Rz_body">
			<!-- 审核失败步骤 -->
			<view class="Rz_body_box" v-if="SH==4">
				<view class="Rz_body_box_icon">
					<image src="../../static/SHSB.png" class="imageBFB"></image>
				</view>
				<view class="Rz_body_box_text">
					<view class="Rz_body_box_text_icon">
						<image src="../../static/redGTH.png" class="imageBFB"></image>
					</view>
					<text>很遗憾，您的申请未通过</text>
				</view>
				<view class="Rz_body_box_text"
					style="color: #999999;font-size: 24rpx;font-weight: 500;margin-top: 10rpx;">{{examineNote || '如有疑问请咨询平台客服'}}</view>
			</view>
			<!-- 审核通过步骤 -->
			<view class="Rz_body_box" v-if="SH==3">
				<view class="Rz_body_box_icon">
					<image src="../../static/SHSHZ.png" class="imageBFB"></image>
				</view>
				<view class="Rz_body_box_text">
					<view class="Rz_body_box_text_icon">
						<image src="../../static/redGX.png" class="imageBFB"></image>
					</view>
					<text>您的酒店入驻申请已通过</text>
				</view>
			</view>
			<!-- 审核中步骤 -->
			<view class="Rz_body_box" v-if="SH==2">
				<view class="Rz_body_box_icon">
					<image src="../../static/SHSHZ.png" class="imageBFB"></image>
				</view>
				<view class="Rz_body_box_text">
					<view class="Rz_body_box_text_icon">
						<image src="../../static/redGX.png" class="imageBFB"></image>
					</view>
					<text>信息已提交，请耐心等待平台审核</text>
				</view>
				<view class="Rz_body_box_text"
					style="color: #999999;font-size: 24rpx;font-weight: 500;margin-top: 10rpx;">如有疑问请咨询平台客服</view>
			</view>
			<view class="Rz_body_box" v-if="SH==1 || SH==4" :style="SH==4 ? 'top:260rpx' : ''">

				<view class="Rz_body_box_title">请填写入驻信息</view>

				<view class="Rz_body_box_list">
					<text>酒店名称</text>
					<view class="Rz_body_box_list_inp">
						<u--input placeholder="请输入" border="false" v-model="name"></u--input>
					</view>
				</view>

				<view class="Rz_body_box_list">
					<text>姓名</text>
					<view class="Rz_body_box_list_inp">
						<u--input placeholder="请输入" border="false" v-model="nickname"></u--input>
					</view>
				</view>

				<view class="Rz_body_box_list">
					<text>电话</text>
					<view class="Rz_body_box_list_inp">
						<u--input placeholder="请输入" border="false" v-model="mobile"></u--input>
					</view>
				</view>

				<view class="Rz_body_box_list">
					<text>地址</text>
					<view class="Rz_body_box_list_inp">
						<u--input placeholder="请输入" border="false" v-model="address" @focus.stop="Map"></u--input>
					</view>
				</view>

				<view class="Rz_body_box_list">
					<text>照片</text>
					<view class="Rz_body_box_list_up">
						<view class="Rz_body_box_list_up_img" v-for="(item,index) in image" :key="index">
							<view class="Rz_body_box_list_up_img_out" @click.stop="Out(item)">
								<image src="../../static/GGB.png" class="imageBFB"></image>
							</view>
							<image :src="YM + item" class="imageBFB" style="border-radius: 16rpx;"></image>
						</view>

						<view class="Rz_body_box_list_up_btn" @click="Up" v-if="image.length<3">+</view>
					</view>
				</view>

				<view class="Rz_body_box_list">
					<text>详细介绍</text>
					<view class="Rz_body_box_list_text">
						<u--textarea v-model="intro" border='none' height='199' placeholder="请输入"
							maxlength='250'></u--textarea>
					</view>
				</view>
			</view>
		</view>

		<view style="width: 100%;height: 320rpx;"></view>

		<view class="Rz_Btn" v-if="SH==1 || SH==4">
			<view class="Rz_Btn_text" @click="Mti">提交</view>
		</view>

	</view>
</template>

<script>
	export default {
		data() {
			return {
				SH: 1,
				isMit: false,
				examineNote: '',
				YM: getApp().globalData.apiBase,
				name: '',
				nickname: '',
				mobile: '',
				address: '',
				intro: '',
				lng: '',
				lat: '',
				image: [],
			}
		},
		onShow() {
			this.loadApplication()
		},
		methods: {
			loadApplication() {
				uni.request({
					url: this.YM + '/api/user/shopApply',
					method: 'POST',
					header: {'token': uni.getStorageSync('token')},
					data: {type: 1},
					success: res => {
						if (!res.data || res.data.code !== 1 || !res.data.data) {
							uni.showToast({title: res.data && res.data.msg ? res.data.msg : '申请信息加载失败', icon: 'none'})
							return
						}
						this.SH = Number(res.data.data.shop_status || 1)
						const shop = res.data.data.shop || {}
						this.name = shop.name || ''
						this.nickname = shop.nickname || ''
						this.mobile = shop.mobile || ''
						this.address = shop.address || ''
						this.intro = shop.intro || ''
						this.examineNote = shop.examine_note || ''
						this.image = Array.isArray(shop.image) ? shop.image.filter(Boolean) : []
					}
				})
			},
			Out(item) {
			    this.image = this.image.filter(img => img !== item);
			},
			Up() {
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
								console.log(Ima, 'Ima-Ima-Ima');
								this.image.push(Ima.data.url)
							},
							fail: () => uni.showToast({title: '图片上传失败', icon: 'none'})
						});
					}
				});
			},
			Map() {
				uni.chooseLocation({
					success: res => {
						this.lat = res.latitude
						this.lng = res.longitude
						this.address = res.name
						console.log(res, '点击得');
					},
					fail: err => {
						console.log(err, 'err-err-err');
						uni.showToast({
							title: '授权失败',
							icon: 'none',
							duration: 1000
						})
					}
				})
			},
			Mti() {
				if (this.isMit) return
				let data = {
					type: 2,
					name: this.name,
					nickname: this.nickname,
					mobile: this.mobile,
					address: this.address,
					image: this.image.toString(),
					intro: this.intro,
					lng: this.lng,
					lat: this.lat
				}
				this.isMit = true
				uni.request({
					url: this.YM + '/api/user/shopApply',
					method: 'POST',
					data,
					header: {
						'token': uni.getStorageSync('token')
					},
					success: res => {
						this.isMit = false
						console.log(res, '门店提交');
						if (res.data.code == 1) {
							uni.showToast({
								title: '申请成功，审核中',
								icon: 'none',
								duration: 2500
							});
							setTimeout(() => {
								uni.navigateBack()
							}, 2500)
						} else {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 2000
							});
							return false
						}
					},
					fail: () => {
						this.isMit = false
						uni.showToast({title: '提交失败，请稍后重试', icon: 'none'})
					}
				})
			},
			Back() {
				uni.navigateBack()
			}
		}
	}
</script>

<style>
	.Rz {
		width: 100vw;
		min-height: 100vh;
		float: left;
		background-color: #f5f5f5;
	}

	.Rz_haerder {
		width: 100%;
		height: 440rpx;
		position: relative;
	}

	.Rz_haerder_titlke {
		width: 100%;
		height: 60rpx;
		text-align: center;
		line-height: 60rpx;
		font-size: 32rpx;
		font-weight: 600;
		color: #FFFFFF;
		position: absolute;
		left: 0;
		top: 100rpx;
		z-index: 2;
	}

	.Rz_haerder_Back {
		width: 40rpx;
		height: 40rpx;
		position: absolute;
		left: 30rpx;
		top: 110rpx;
		z-index: 9999;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Rz_body {
		width: 696rpx;
		height: 1400rpx;
		text-align: center;
		margin: 0 auto;
		position: relative;
	}

	.Rz_body_box {
		width: 100%;
		border-radius: 16rpx;
		background-color: rgba(255, 255, 255, 1);
		text-align: center;
		position: absolute;
		top: -45rpx;
		left: 0;
		z-index: 5;
		float: left;
		padding-bottom: 30rpx;

	}

	.Rz_body_box_title {
		width: 100%;
		height: 70rpx;
		text-align: center;
		line-height: 70rpx;
		font-size: 30rpx;
		font-weight: 600;
		margin-top: 20rpx;
		color: #000000;
	}

	.Rz_body_box_list {
		width: 632rpx;
		margin: 30rpx auto 0;
		display: flex;
		align-items: flex-start;
		flex-wrap: wrap;
		justify-content: flex-start;
		font-size: 28rpx;
		color: #000000;
		font-weight: 600;
	}

	.Rz_body_box_list_inp {
		width: 632rpx;
		height: 100rpx;
		border-radius: 16rpx;
		background-color: rgba(245, 245, 245, 1);
		text-align: center;
		margin-top: 10rpx;
		display: flex;
		align-items: center;
		font-weight: 500;

	}

	.Rz_body_box_list_up {
		width: 100%;
		height: 170rpx;
		display: flex;
		align-items: center;
		margin-top: 20rpx;
	}

	.Rz_body_box_list_up_btn {
		width: 160rpx;
		height: 160rpx;
		line-height: 160rpx;
		border-radius: 16rpx;
		text-align: center;
		background-color: rgba(255, 255, 255, 1);
		text-align: center;
		border: 2rpx solid rgba(187, 187, 187, 1);
		font-size: 40rpx;
		font-weight: 600;
		color: #666666;
	}

	.Rz_body_box_list_up_img {
		width: 160rpx;
		height: 160rpx;
		border-radius: 16rpx;
		position: relative;
		margin-right: 36rpx;
		border: 2rpx solid #f5f5f5;
	}

	.Rz_body_box_list_up_img_out {
		width: 32rpx;
		height: 32rpx;
		position: absolute;
		right: -12rpx;
		top: -12rpx;
		z-index: 999;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Rz_body_box_list_text {
		width: 100%;
		border-radius: 16rpx;
		background-color: rgba(245, 245, 245, 1);
		overflow: hidden;
		margin-top: 10rpx;
		display: flex;
		align-items: center;
		justify-content: flex-start;
		text-align: left;
	}

	.Rz_Btn {
		width: 100%;
		height: 140rpx;
		position: fixed;
		left: 0;
		bottom: 0;
		z-index: 9999;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #FFFFFF;
	}

	.Rz_Btn_text {
		width: 606rpx;
		height: 100rpx;
		line-height: 100rpx;
		border-radius: 50rpx;
		background-color: rgba(237, 153, 57, 1);
		text-align: center;
		color: rgba(255, 255, 255, 1);
		font-size: 28rpx;
	}

	.Rz_body_box_icon {
		width: 308rpx;
		height: 208rpx;
		margin: 80rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Rz_body_box_text {
		width: 100%;
		height: 40rpx;
		margin-top: 20rpx;
		color: rgba(16, 16, 16, 1);
		font-size: 28rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Rz_body_box_text_icon {
		width: 33rpx;
		height: 33rpx;
		margin-right: 15rpx;
	}
</style>
