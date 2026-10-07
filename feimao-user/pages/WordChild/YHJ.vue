<template>
	<view class="YHJ">
		<view class="YHJ_list">
			<view class="QD_Two_list" v-for="(item,index) in YHJdata" :key="index">
				<view class="QD_Two_list_icon">
					<image src="../../static/YHJBJ.png" class="imageBFB"></image>
				</view>

				<view class="QD_Two_list_price">
					<view class="QD_Two_list_price_price">
						<text>{{item.price}}</text>
						<text style="font-size: 26rpx;font-weight: 500;">元</text>
					</view>
					<view class="QD_Two_list_price_text">满减券</view>
				</view>

				<view class="QD_Two_list_right">
					<view class="QD_Two_list_right_L">
						<view class="QD_Two_list_right_L_t">满{{item.price}}减{{item.reduceprice}}</view>
						<view class="QD_Two_list_right_L_b">有效期:{{item.endtime}}</view>
					</view>
					<view class="QD_Two_list_right_R">
						<view class="QD_Two_list_right_R_b1" @click="ClickIndex">去使用</view>
					</view>
				</view>
			</view>
			<u-empty mode="coupon" icon="https://cdn.uviewui.com/uview/empty/car.png" v-if="YHJdata==''">
			</u-empty>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM:getApp().globalData.apiBase,
				YHJdata: [],
			}
		},
		onShow() {
			this.YHJlist()
		},
		methods: {
			formatEndtime(value) {
				const timestamp = Number(value)
				if (!timestamp) return '长期有效'
				const date = new Date(timestamp * 1000)
				const pad = number => String(number).padStart(2, '0')
				return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
			},
			//优惠券列表
			YHJlist() {
				uni.request({
					url: this.YM + '/api/user/couponList',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token')
					},
					success: res => {
						this.YHJdata = res.data.code == 1 && Array.isArray(res.data.data)
							? res.data.data
							: []
						if (res.data.code != 1) {
							uni.showToast({ title: res.data.msg || '优惠券加载失败', icon: 'none' })
							return
						}

						this.YHJdata.forEach(item => {
							item.price = parseFloat(item.price).toFixed(0)
							item.reduceprice = parseFloat(item.reduceprice).toFixed(0)
							item.endtime = this.formatEndtime(item.endtime)
						})
						console.log(this.YHJdata, '优惠券列表');
					},
					fail: () => {
						this.YHJdata = []
						uni.showToast({ title: '优惠券加载失败，请稍后重试', icon: 'none' })
					}
				})
			},
			ClickIndex() {
				uni.switchTab({
					url: '/pages/index/index'
				})
			}
		}
	}
</script>

<style>
	.YHJ {
		width: 100vw;
		min-height: 100vh;
		background-color: #FFFFFF;
		float: left;
	}

	.YHJ_list {
		width: 692rpx;
		height: 800rpx;
		/* background-color: darkseagreen; */
		margin: 20rpx auto 0;
	}

	.QD_Two_list {
		width: 100%;
		height: 164rpx;
		/* background-color: green; */
		position: relative;
		margin-bottom: 20rpx;

	}

	.QD_Two_list_icon {
		width: 100%;
		height: 100%;
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		z-index: 1;
	}

	.QD_Two_list_price {
		width: 150rpx;
		height: 120rpx;
		/* background-color: violet; */
		position: absolute;
		left: 20rpx;
		top: 25rpx;
		z-index: 2;
	}

	.QD_Two_list_price_price {
		width: 100%;
		height: 70rpx;
		text-align: center;
		line-height: 70rpx;
		font-size: 54rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #FF6043;
	}

	.QD_Two_list_price_text {
		width: 100%;
		height: 30rpx;
		text-align: center;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FF6043;
	}

	.QD_Two_list_right {
		width: 480rpx;
		height: 120rpx;
		/* background-color: salmon; */
		position: absolute;
		z-index: 2;
		top: 25rpx;
		right: 20rpx;
		display: flex;
	}

	.QD_Two_list_right_L {
		width: 300rpx;
		height: 100%;
		/* background-color: yellow; */
	}

	.QD_Two_list_right_L_t {
		width: 100%;
		height: 50%;
		font-size: 32rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #101010;
		line-height: 60rpx;
	}

	.QD_Two_list_right_L_b {
		width: 100%;
		height: 50%;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		line-height: 60rpx;
	}

	.QD_Two_list_right_R {
		width: 160rpx;
		height: 100%;
		/* background-color: red; */
		margin-left: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.QD_Two_list_right_R_t {
		width: 100%;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #333333;
	}

	.QD_Two_list_right_R_t_icon {
		width: 25rpx;
		height: 25rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 10rpx;
	}

	.QD_Two_list_right_R_b,
	.QD_Two_list_right_R_b1 {
		width: 120rpx;
		height: 60rpx;
		background: pink;
		box-shadow: 0rpx 3rpx 10rpx 0rpx rgba(255, 71, 35, 0.45);
		border-radius: 30rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FFFFFF;
		text-align: center;
		line-height: 60rpx;
	}

	.QD_Two_list_right_R_b1 {
		background: linear-gradient(-60deg, #FF4522, #FF671D);
	}
</style>
