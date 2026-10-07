<template>
	<view class="mxi">
		<view class="mxi_box">

			<view class="mxi_box_list" v-for="(item,index) in MxiData" :key="index" v-if="MxiData">
				<view class="mxi_box_list_L">
					<text style="display: inline-block;margin-left: 20rpx;">订单编号:{{item.number}}</text><br />
					<text style="font-size: 26rpx;color: #999999;display: inline-block;margin-top: 15rpx;margin-left: 20rpx;">{{item.createtime}}</text>
				</view>
				<view class="mxi_box_list_R" style="margin-right: 20rpx;">+{{item.price}}</view>
			</view>

		</view>
		
		<view style="width: 100%;height: 150rpx;text-align: center;line-height: 150rpx;font-size: 28rpx;color: #999999;">没有更多啦~</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				MxiData:''
			}
		},
		onShow() {
			uni.request({
				url: this.YM + '/api/store/moneyLog',
				method: 'POST',
				header: {
					'token': uni.getStorageSync('MDtoken')
				},
				data: {
					page: 1,
					list_rows: 999999999,
					token: uni.getStorageSync('MDtoken')
				},
				success: res => {
					console.log(res.data.data.data, '明细');
					this.MxiData=res.data.data.data
				}
			})
		}
	}
</script>

<style>
	.mxi {
		width: 100vw;
		min-height: 100vh;
		float: left;
		background-color: #F5F5F5;
	}

	.mxi_box {
		width: 95%;
		height: auto;
		border-radius: 12rpx;
		background-color: #FFFFFF;
		margin: 20rpx auto 0;
	}

	.mxi_box_list {
		width: 100%;
		height: auto;
		border-bottom: 2rpx solid #F5F5F5;
		padding-top: 20rpx;
		padding-bottom: 20rpx;
		font-size: 28rpx;
		color: #000000;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.mxi_box_list_L {
		width: 80%;
		height: 100%;
	}

	.mxi_box_list_R {
		width: 15%;
		height: 100%;
		text-align: right;
		display: flex;
		align-items: center;
		justify-content: flex-end;
	}
</style>