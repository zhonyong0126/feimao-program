<template>
	<view class="Jl">
		<view class="Jl_list" v-for="(item,index) in list" :key="index">
			<view class="Jl_list_L">
				<text style="color: #000000;font-size: 32rpx;font-weight: 600;">佣金提现</text><br />
				<text style="color: #999999;font-size: 28rpx;font-weight: 500;">{{item.createtime}}</text>
			</view>
			<view class="Jl_list_R">
				<text style="color: #000000;font-size: 32rpx;font-weight: 600;margin-right: 90rpx;">-{{item.amount}}</text>
				<!-- 状态：1待审核 2已通过 3已拒绝 -->
				<text style="color: #0073FF;font-size: 28rpx;" v-if="item.status==1">审核中</text>
				<text style="color: #999999;font-size: 28rpx;" v-if="item.status==2">已提现</text>
				<text style="color: #FF3415;font-size: 28rpx;" v-if="item.status==3">已拒绝</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				list:''
			}
		},
		onShow() {
			this.MDapi()
		},
		methods: {
			MDapi() {
				uni.request({
					url: this.YM + '/api/store/cashList',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					data: {
						page: 1,
						list_rows: 999999999999999999
					},
					success: res => {
						console.log(res.data.data.data, 'tixiian');
						this.list=res.data.data.data
					}
				})
			}
		}
	}
</script>

<style>
	.Jl {
		width: 100vw;
		min-height: 100vh;
		background-color: #FFFFFF;
		float: left;
	}

	.Jl_list {
		width: 662rpx;
		height: 180rpx;
		border-bottom: 2rpx solid #F5F5F5;
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin: 0 auto;
	}

	.Jl_list_L,
	.Jl_list_R {
		width: 50%;
		height: 80%;
	}

	.Jl_list_L {
		height: 50%;
	}

	.Jl_list_R {
		display: flex;
		align-items: center;
		justify-content: flex-end;
	}
</style>