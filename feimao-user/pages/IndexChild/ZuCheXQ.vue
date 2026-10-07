<template>
	<view class="zcxq">

		<!-- 轮播 -->
		<view class="zcxq_lunb">
			<u-swiper :list="list2" :height='280' :circular='true'></u-swiper>
		</view>

		<!-- 下部 -->
		<view class="zcxq_all">

			<view class="zcxq_all_Fc">
				<view class="zcxq_all_Fc_title">
					<text style="font-size: 26rpx;font-weight: 500;">￥</text>
					<text>{{GouCheData.hour_price}}</text>
					<text style="font-size: 26rpx;font-weight: 500;">元</text>
				</view>

				<view class="zcxq_all_Fc_text">{{GouCheData.car.name}}</view>


				<!-- 商户信息 -->
				<view class="zcxq_all_sh">
					<view class="zcxq_all_xx_cl">商户信息</view>

					<view class="zcxq_all_sh_xx">
						<text style="color: #858896;">营业时间</text>
						<text style="color: black;">{{GouCheData.merchant.business}}</text>
					</view>



					<view class="zcxq_all_sh_xx">
						<text style="color: #858896;">店铺地址</text>
						<text style="color: black;display: inline-block;width: 430rpx;text-align: right;"
							v-if="BFC=='3A'">{{WeiXiuData.address}}</text>
						<text style="color: black;display: inline-block;width: 430rpx;text-align: right;"
							v-if="BFC=='2A'">{{GouCheData.merchant.address}}</text>
						<text style="color: black;display: inline-block;width: 430rpx;text-align: right;"
							v-if="BFC=='1A'">{{GouCheData.merchant.address}}</text>
					</view>
				</view>

				<!-- 车辆详情 -->
				<view class="zcxq_all_clxq">
					<view class="zcxq_all_clxq_icon">
						<image src="../../static/L.png" class="imageBFB"></image>
					</view>
					<view class="zcxq_all_clxq_text">车辆详情</view>
					<view class="zcxq_all_clxq_icon">
						<image src="../../static/R.png" class="imageBFB"></image>
					</view>
				</view>

				<!-- <view class="zcxq_all_image" v-for="(item,index) in GouCheData.intro" :key="index">
				</view> -->
				<image :src="YM + item" mode="widthFix" v-for="(item,index) in GouCheData.intro" :key="index" style="width: 100%;"></image>


				<view style="width: 100%;height: 200rpx;background-color: transparent;"></view>

				<view class="zcxq_all_btn">
					<view class="zcxq_all_btn_text" @click="ClickLGZL">立即租赁</view>
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
				list1: [],
				list2: [],
				BFC: '1A',
				WeiXiuData: '', //维修数据依赖
				GouCheData: '', //购车数据依赖
				R: '',
				clickTimer: null
			}
		},
		onShow() {
			this.BFC = uni.getStorageSync('BFC')
			console.log(this.BFC, 'this.BFC');
			this.XiangQingApi() //详情接口

			this.GoumaiXiangQingApi() //购买详情接口

			// uni.removeStorageSync('current')

		},
		methods: {
			// 购买详情信息
			GoumaiXiangQingApi() {
				let data = {
					merchant_car_id: uni.getStorageSync('merchat_car_id')
				}
				uni.request({
					url: this.YM + '/api/merchant/carDetails',
					method: 'POST',
					data,
					success: res => {
						console.log(res.data.data, '购买详情信息');
						this.GouCheData = res.data.data
						// 将image属性按逗号分隔为数组
						
						
						for (var i = 0; i < res.data.data.images.length; i++) {
							this.list2.push(this.YM + res.data.data.images[i])
						}
						
						// let images = res.data.data.images.split(',');
						// images.forEach(value => {
						// 	this.list2.push(this.YM + value);
						// });
						let A = uni.getStorageSync('GouCheData').Type
						this.R = {
							Type: A,
							item: this.GouCheData
						}
					},
				})
			},
			//维修详情信息
			XiangQingApi() {
				let data = {
					merchant_maintain_id: uni.getStorageSync('weixiu_merchant_maintain_id')
				}
				uni.request({
					url: this.YM + '/api/merchant/maintainDetails',
					method: 'POST',
					data,
					header: {
						'token': uni.getStorageSync('token'),
					},
					success: res => {
						this.WeiXiuData = res.data.data
						console.log(this.WeiXiuData, '详情信息');


						// 将image属性按逗号分隔为数组
						let images = this.WeiXiuData.image.split(',');


						images.forEach(value => {
							this.list1.push(this.YM + value);
						});

						console.log(this.list1, 'this.list1');

					},
				})
			},
			//立即租赁服务
			ClickLGZL() {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
					if (!uni.getStorageSync('token')) {
						uni.setStorageSync('LoginIF', '04')
						uni.navigateTo({
							url: '/pages/login/login'
						})
						return false
					} else {
						uni.setStorageSync('GouCheData', this.R)
						uni.navigateTo({
							url: '/pages/IndexChild/lzTiJIao'
						})
					}
				}, 500); // 设置延迟时间，单位为毫秒

			},
			//购买服务提交
			ClickGMtijiao() {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
					if (!uni.getStorageSync('token')) {
						uni.setStorageSync('LoginIF', '04')
						uni.navigateTo({
							url: '/pages/login/login'
						})
						return false
					} else {
						console.log('购买服务提交');
						uni.setStorageSync('GouCheData', this.R)
						uni.navigateTo({
							url: '/pages/IndexChild/goumaiTiJiao'
						})
					}
				}, 500); // 设置延迟时间，单位为毫秒

			},
			//维修服务提交
			ClickFWtijiao() {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
					if (!uni.getStorageSync('token')) {
						uni.setStorageSync('LoginIF', '04')
						uni.navigateTo({
							url: '/pages/login/login'
						})
						return false
					} else {
						console.log('服务提交');
						uni.navigateTo({
							url: '/pages/IndexChild/weixiuTiJiao'
						})
					}
				}, 500); // 设置延迟时间，单位为毫秒

			}
		}
	}
</script>

<style>
	.zcxq {
		width: 100vw;
		height: 100vh;
		/* background-color: skyblue; */
	}

	/* 轮播 */
	.zcxq_lunb {
		width: 100%;
		/* height: 550rpx; */
		/* background-color: green; */
	}

	/* 下部 */
	.zcxq_all {
		width: 100%;
		min-height: calc(100vh - 550rpx);
		/* background-color: aquamarine; */
		position: relative;
	}

	.zcxq_all_Fc {
		width: 100%;
		height: 100%;
		border-radius: 32rpx;
		background-color: #F7F7F7;
		position: absolute;
		left: 0;
		top: -50rpx;
		float: left;
	}

	.zcxq_all_Fc_title {
		width: 93%;
		height: 60rpx;
		line-height: 60rpx;
		font-size: 50rpx;
		font-family: DIN;
		font-weight: bold;
		color: #E64638;
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
	}

	.zcxq_all_Fc_text {
		width: 93%;
		height: 60rpx;
		line-height: 60rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		margin: 15rpx auto 0;
	}

	.zcxq_all_xx {
		width: 93%;
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 20rpx;
		margin: 20rpx auto 0;
		padding-bottom: 15rpx;
	}

	.zcxq_all_xx_cl {
		width: 90%;
		height: 70rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		line-height: 90rpx;
		margin: 15rpx auto 0;
		/* background-color: yellow; */
	}

	.zcxq_all_xx_pp {
		width: 90%;
		height: 70rpx;
		/* background-color: violet; */
		margin: 0 auto;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.zcxq_all_xx_pp_L {
		width: 50%;
		height: 100%;
		line-height: 70rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}


	.zcxq_all_sh {
		width: 93%;
		/* height: 432rpx; */
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 20rpx;
		margin: 24rpx auto 0;
		padding-bottom: 30rpx;
	}

	.zcxq_all_sh_xx {
		width: 90%;
		min-height: 50rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin: 20rpx auto 0;
		/* background-color: red; */
	}

	.zcxq_all_sh_xx>text {
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
	}

	.zcxq_all_clxq {
		width: 93%;
		height: 50rpx;
		/* background-color: aqua; */
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.zcxq_all_clxq_icon {
		width: 98rpx;
		height: 8rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.zcxq_all_clxq_text {
		width: 150rpx;
		height: 27rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #1C253D;
		text-align: center;
		line-height: 27rpx;
	}

	.zcxq_all_btn {
		width: 100%;
		height: 158rpx;
		background-color: #FFFFFF;
		display: flex;
		align-items: center;
		justify-content: center;
		position: fixed;
		bottom: 0;
		left: 0;
	}

	.zcxq_all_btn_text {
		width: 668rpx;
		height: 104rpx;
		background: #FF7A00;
		border-radius: 52rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #FFFFFF;
		text-align: center;
		line-height: 104rpx;
	}

	.zcxq_all_image {
		width: 100%;
		height: 300rpx;
		/* background-color: aqua; */
	}
</style>
