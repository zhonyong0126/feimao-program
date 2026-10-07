<template>
	<view class="MX">

		<view class="MX_box">

			<view class="MX_box_list" v-for="(item,index) in List" :key="index">
				<view class="MX_box_list_L">
					<view class="MX_box_list_L_titile">{{item.memo}}</view>
					<view class="MX_box_list_L_text">{{item.time}}</view>
				</view>
				<view class="MX_box_list_R">{{item.score}}</view>
				<!-- <view class="MX_box_list_R1">{{item.score}}</view> -->
				<!-- <view v-else class="MX_box_list_R">+{{item.score}}</view> -->
			</view>

		</view>

	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				List: []
			}
		},
		onShow() {
			let data = {
				page: 1
			}
			uni.request({
				url: this.YM + '/api/sign/userCouponLogList',
				method: 'POST',
				header:{
					'token': uni.getStorageSync('token'),
				},
				data,
				success: res => {
					this.List = res.data.code == 1 && res.data.data
						? (res.data.data.data || [])
						: []
					if (res.data.code != 1) {
						uni.showToast({ title: res.data.msg || '积分明细加载失败', icon: 'none' })
					}
					console.log(this.List, '积分明细');
					
					for (var i = 0; i < this.List.length; i++) {
						console.log(this.List[i].score,'查找是否存在-号');
					}
				},
				fail: () => {
					this.List = []
					uni.showToast({ title: '积分明细加载失败，请稍后重试', icon: 'none' })
				}
			})
		}
	}
</script>

<style>
	.MX {
		width: 100vw;
		min-height: 100vh;
		background-color: #FFFFFF;
		float: left;
	}

	.MX_box {
		width: 90%;
		height: 100%;
		margin: 25rpx auto 0;
		/* background-color: aquamarine; */
	}

	.MX_box_list {
		width: 100%;
		height: 140rpx;
		/* background-color: violet; */
		border-bottom: 1rpx solid #dedede;
		display: flex;
	}

	.MX_box_list_L {
		width: 90%;
		height: 100%;
		/* background-color: teal; */
	}

	.MX_box_list_L_titile {
		width: 100%;
		height: 50%;
		/* background-color: sandybrown; */
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		line-height: 70rpx;
	}

	.MX_box_list_L_text {
		width: 100%;
		height: 50%;
		/* background-color: bisque; */
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.MX_box_list_R,
	.MX_box_list_R1 {
		width: 10%;
		height: 100%;
		/* background-color: indianred; */
		font-size: 32rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #FF7525;
		text-align: center;
		line-height: 150rpx;
	}

	.MX_box_list_R1 {
		color: #333333;
	}
</style>
