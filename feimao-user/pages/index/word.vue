<template>
	<view class="word">
		<!-- 顶部 -->
		<view class="word_dh">
			<view class="word_dh_src">
				<image src="../../static/wordbj.png" class="imageBFB"></image>
			</view>
			<view class="word_dh_all">
				<!-- 头像 姓名 设置 -->
				<view class="word_dh_all_txs">
					<!-- 头像 -->
					<view class="word_dh_all_txs_t">
						<view class="word_dh_all_txs_tsrc" @click="ClickYl">
							<image src="../../static/word1.png" class="imageBFB" v-if="!Men.avatar"></image>
							<image :src="YM + Men.avatar" class="imageBFB" v-else></image>
						</view>
			<!-- 			<view class="word_dh_all_txs_ttext"
							:class="Men.authentication==1 || !Men.authentication?'word_dh_all_txs_ttext':'word_dh_all_txs_ttext1'"
							@click="ClickSMRZZ">
							<view class="word_dh_all_txs_ttext_icon">
								<image src="../../static/wsm.png" class="imageBFB"></image>
							</view>
							<text v-if="Men.authentication==1 || !Men.authentication">未实名</text>
							<text v-if="Men.authentication==2">审核中</text>
							<text v-if="Men.authentication==4">已拒绝</text>
							<text v-if="Men.authentication==3">已实名</text>
						</view> -->
					</view>

					<!-- 姓名 -->
					<view class="word_dh_all_txs_x" @click="ClickLogin">
						<view class="word_dh_all_txs_xname" v-if="!Men">请登录</view>
						<view class="word_dh_all_txs_xname" v-else>{{Men.nickname}}</view>
						<view class="word_dh_all_txs_xphone" v-if="!Men.mobile">暂未添加手机号</view>
						<view class="word_dh_all_txs_xphone" v-else>{{Men.mobile}}</view>
					</view>

					<!-- 设置 -->
					<view class="word_dh_all_txs_s">
						<view class="word_dh_all_txs_sicon" @click="ClickShezhi">
							<image src="../../static/wordsz.png" class="imageBFB"></image>
						</view>
					</view>
				</view>
			</view>
		</view>
		<!-- 下部 -->
		<view class="word_bto">

			<view class="word_bto_box">
				<view class="word_bto_box_list" v-for="(item,index) in ListData" :key="index" @click="Clicksz(item)">
					<view class="word_bto_box_list_L">
						<view class="word_bto_box_list_L_icon">
							<image :src="item.icon" class="imageBFB"></image>
						</view>
						<text>{{item.title}}</text>
					</view>
					<view class="word_bto_box_list_R">
						<view v-if="item.id==6" style="display: flex;align-items: center;justify-content: flex-end;">
							<text style="color: #2D7AFE;font-size: 28rpx;">{{Phone}}</text>
							<view class="word_bto_box_list_R_phone">
								<image src="../../static/word7x.png" class="imageBFB"></image>
							</view>
						</view>
						<view class="word_bto_box_list_R_icon" v-else>
							<image src="../../static/wordF.png" class="imageBFB"></image>
						</view>
					</view>
				</view>
			</view>

		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				ListData: [{
						title: '进入门店端',
						icon: '../../static/word1x.png',
						id: 1
					},
					{
						title: '酒店入驻',
						icon: '../../static/word2x.png',
						id: 2
					},
					{
						title: '我的地址',
						icon: '../../static/word3x.png',
						id: 3
					},
					{
						title: '关于我们',
						icon: '../../static/word5x.png',
						id: 5
					},
					{
						title: '电话客服',
						icon: '../../static/word6x.png',
						id: 6
					}
				],
				Men: '', //我的信息依赖
				Phone: '114',
				clickTimer: null,

			}
		},
		onShow() {
			if (!uni.getStorageSync('token')) {
				this.MeApi() //个人信息
			} else {
				this.MeApi() //个人信息
			}

			uni.request({
				url: this.YM + '/api/index/config',
				method: 'POST',
				success: res => {
					console.log(res.data.data, '隐私政策');

					const targetData1 = res.data.data.find(item => item.name === "service_mobile");
					if (targetData1) {
						this.Phone = targetData1.value
						console.log(targetData1.value);
						uni.setStorageSync('Phone', this.Phone)
					} else {
						this.Phone = '后台暂未添加，请联系工作人员'
						console.log('未找到匹配的数据');
					}
				}
			})
		},
		methods: {
			onShareAppMessage(res) {
				return {
					title: '安鑫出行',
					path: '/pages/index/word',
					imageUrl: '../../static/FXimgaes.png',
				};
			},
			//分享到朋友圈
			onShareTimeline(res) {
				return {
					title: '安鑫出行',
					path: '/pages/index/word',
					imageUrl: '../../static/FXimgaes.png',
				};
			},
			//预览头像
			ClickYl() {
				var SrcImage = this.YM + this.Men.avatar;
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
						fail: (err) => {
							console.log(err.errMsg);
						}
					}
				});
			},
			MeApi() {
				uni.request({
					url: this.YM + '/api/user/info',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token')
					},
					success: res => {
						this.Men = res.data.data
						console.log(res.data.data, '个人信息');
					},
				})
			},
			//点击跳转登录
			ClickLogin() {
				if (uni.getStorageSync('token')) {
					uni.navigateTo({
						url: '/pages/WordChild/shezhi'
					})
				} else {
					uni.setStorageSync('LoginIF', '01')
					uni.navigateTo({
						url: '/pages/login/login'
					})
				}
			},
			//点击去签到
			ClicmkQD() {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {

					if (!uni.getStorageSync('token')) {
						uni.showToast({
							title: '请登录后操作',
							icon: 'none',
							duration: 2000
						});
						uni.setStorageSync('LoginIF', '01')
						setTimeout(() => {
							uni.navigateTo({
								url: '/pages/login/login'
							})
						}, 2000)
						return false
					} else {
						// uni.navigateTo({
						// 	url: '/pages/IndexChild/QianDao'
						// })
					}
				}, 800); // 设置延迟时间，单位为毫秒
			},
			//进入实名认证
			ClickSMRZZ() {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
					if (!uni.getStorageSync('token')) {
						uni.showToast({
							title: '请登录后操作',
							icon: 'none',
							duration: 2000
						});
						uni.setStorageSync('LoginIF', '01')
						setTimeout(() => {
							uni.navigateTo({
								url: '/pages/login/login'
							})
						}, 2000)
						return false
					} else {
						uni.navigateTo({
							url: '/pages/IndexChild/SMRZ'
						})
					}
				}, 500); // 设置延迟时间，单位为毫秒
			},
			//点击了设置
			ClickShezhi() {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
					if (!uni.getStorageSync('token')) {
						uni.showToast({
							title: '请登录后操作',
							icon: 'none',
							duration: 2000
						});
						uni.setStorageSync('LoginIF', '01')
						setTimeout(() => {
							uni.navigateTo({
								url: '/pages/login/login'
							})
						}, 2000)
						return false
					} else {
						console.log('点击了设置');
						uni.navigateTo({
							url: '/pages/WordChild/shezhi'
						})
					}
				}, 500); // 设置延迟时间，单位为毫秒
			},
			Clicksz(item) {
				if (!uni.getStorageSync('token')) {
					uni.setStorageSync('LoginIF', '04')
					uni.navigateTo({
						url: '/pages/login/login'
					})
					return
				}
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
					console.log('点击了', item);
					if (item.id == 1) {
						// 商家申请状态：1未申请 2待审核 3已通过 4已拒绝
						if (this.Men.merchant_status == 1) {
							uni.navigateTo({
								url: '/pages/erkaiPages/MneDian'
							})
						} else if (this.Men.merchant_status == 2) {
							uni.showToast({
								title: '正在审核中',
								icon: 'none',
								duration: 2000
							});
							return
						} else if (this.Men.merchant_status == 3) {
							if(uni.getStorageSync('MDtoken')){
								uni.navigateTo({
									url:'/pages/erkaiPages/MDindex'
								})
							}else{
								uni.navigateTo({
									url: '/pages/erkaiPages/MDlogin'
								})
							}
							
						} else if (this.Men.merchant_status == 4) {
							uni.navigateTo({
								url: '/pages/erkaiPages/MneDian'
							})
						}
					} else if (item.id == 2) {
						// 酒店申请状态：1未申请 2待审核 3已通过 4已拒绝
						if (this.Men.shop_status == 1) {
							uni.navigateTo({
								url: '/pages/erkaiPages/RuZhu'
							})
						} else if (this.Men.shop_status == 2) {
							uni.showToast({
								title: '正在审核中',
								icon: 'none',
								duration: 2000
							});
							return
						} else if (this.Men.shop_status == 3) {
							
							if(uni.getStorageSync('JDtoken')){
								uni.navigateTo({
									url:'/pages/erkaiPages/JDindex'
								})
							}else{
								uni.navigateTo({
									url: '/pages/erkaiPages/JDlogin'
								})
							}
							
							
						} else if (this.Men.shop_status == 4) {
							uni.navigateTo({
								url: '/pages/erkaiPages/RuZhu'
							})
						}


					} else if (item.id == 3) {
						uni.navigateTo({
							url: '/pages/WordChild/DiZhi'
						})
					} else if (item.id == 5) {
						uni.navigateTo({
							url: '/pages/WordChild/GYWM'
						})
					} else if (item.id == 6) {
						uni.makePhoneCall({
							phoneNumber: this.Phone //仅为示例
						});
					}
				}, 500); // 设置延迟时间，单位为毫秒
			}
		}
	}
</script>

<style>
	.word {
		width: 100vw;
		height: 100vh;
		background-color: #FFFFFF;
	}

	.word_dh {
		width: 100%;
		height: 450rpx;
		background-color: #fffdfa;
		position: relative;
	}

	.word_dh_src {
		width: 100%;
		height: 100%;
		/* background-color: yellow; */
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 1;
	}

	.word_dh_all {
		width: 100%;
		height: 100%;
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 2;
	}

	.word_dh_all_txs {
		width: 680rpx;
		height: 170rpx;
		/* background-color: aqua; */
		margin: 220rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.word_dh_all_txs_t {
		width: 140rpx;
		height: 140rpx;
		/* background-color: green; */
		position: relative;
	}

	.word_dh_all_txs_tsrc {
		width: 140rpx;
		height: 140rpx;
		background: #3E3E3E;
		border: 2px solid #FFC03C;
		border-radius: 50%;
		overflow: hidden;
		position: absolute;
		left: 50%;
		top: 50%;
		transform: translate(-50%, -50%);
	}

	.word_dh_all_txs_ttext,
	.word_dh_all_txs_ttext1 {
		width: 112rpx;
		height: 32rpx;
		background: #CFCFCF;
		border-radius: 16rpx;
		text-align: center;
		line-height: 32rpx;
		position: absolute;
		left: 50%;
		bottom: -28rpx;
		transform: translate(-50%);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 22rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FFFFFF;
	}

	.word_dh_all_txs_ttext1 {
		background: #1081FF;
	}

	.word_dh_all_txs_ttext_icon {
		width: 18rpx;
		height: 20rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 10rpx;
	}

	.word_dh_all_txs_x {
		width: 360rpx;
		height: 140rpx;
		/* background-color: yellowgreen; */
		margin-left: 15rpx;
	}

	.word_dh_all_txs_xname {
		width: 100%;
		height: 50%;
		/* background-color: red; */
		font-size: 36rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		line-height: 90rpx;
	}

	.word_dh_all_txs_xphone {
		width: 100%;
		height: 50%;
		/* background-color: skyblue; */
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		line-height: 70rpx;
	}

	.word_dh_all_txs_s {
		width: 150rpx;
		height: 140rpx;
		display: flex;
		align-items: center;
		justify-content: flex-end;
		/* background-color: aliceblue; */
	}

	.word_dh_all_txs_sicon {
		width: 44rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.word_dh_all_Q {
		width: 683rpx;
		height: 140rpx;
		/* background-color: aquamarine; */
		margin: 60rpx auto 0;
	}

	/* 下部 */
	.word_bto {
		width: 100%;
		height: calc(100vh - 620rpx);
		background-color: #FFFFFF;
		float: left;
	}

	.word_bto_box {
		width: 680rpx;
		height: 600rpx;
		/* background-color: yellow; */
		margin: 40rpx auto 0;
	}

	.word_bto_box_list {
		width: 100%;
		height: 60rpx;
		display: flex;
		align-items: center;
		/* background-color: green; */
		margin-bottom: 35rpx;
	}

	.word_bto_box_list_L {
		width: 50%;
		height: 100%;
		/* background-color: red; */
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		display: flex;
		align-items: center;
	}

	.word_bto_box_list_L_icon {
		width: 42rpx;
		height: 42rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 15rpx;
	}

	.word_bto_box_list_R {
		width: 50%;
		height: 100%;
		/* background-color: royalblue; */
		display: flex;
		align-items: center;
		justify-content: flex-end;
	}

	.word_bto_box_list_R_icon {
		width: 12rpx;
		height: 18rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.word_bto_box_list_R_phone {
		width: 25rpx;
		height: 25rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 15rpx;
	}
</style>
