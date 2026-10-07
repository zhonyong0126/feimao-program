<template>
	<view class="Sc">
		<view class="Mindex_list" v-for="(item,index) in order" :key="index">
			<view class="Mindex_list_title">
				<view class="Mindex_list_title_L">
					<view class="Mindex_list_title_L_icon">
						<image src="../../static/Hwu.png" class="imageBFB"></image>
					</view>
					<text>{{item.merchant_name}}</text>
				</view>
				<text style="color: #000000;" v-if="item.state==1">待支付</text>
				<text style="color: #000000;" v-if="item.state==2">已支付</text>
				<text style="color: #ED9939;" v-if="item.state==3">进行中</text>
				<text style="color: #ED9939;" v-if="item.state==4">还车中</text>
				<text style="color: #999999;" v-if="item.state==5">已完成</text>
				<text style="color: #999999;" v-if="item.state==6">已取消</text>
			</view>

			<view class="Mindex_list_body">
				<view class="Mindex_list_body_Src flex">
					<image :src="YM + item.service_image" class="imageBFB"></image>
				</view>
				<view class="Mindex_list_body_L">
					<text>{{item.service_name}}</text><br />
					<text style="display: inline-block;margin-top: 12rpx;"><text
							style="color: #999999;">取车方式：</text>{{item.pick_type}}</text><br />
					<text style="display: inline-block;margin-top: 12rpx;"><text
							style="color: #999999;">租赁时间：</text>{{item.lease_duration}}</text>
				</view>
			</view>

			<view class="Mindex_price">
				<text>￥{{item.price}}<text
						style="color: #999999;font-size: 24rpx;display: inline-block;margin-left: 10rpx;">(含押金)</text></text>
				<view class="Mindex_price_btn" @click="LinkMDorderXQ(item)">查看详情</view>
			</view>
		</view>
		
		<view style="width: 100%;height: 150rpx;text-align: center;line-height: 150rpx;font-size: 28rpx;color: #ccc;">没有更多啦~</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				type: null,
				order:'',
				state:'',
				pick_type:''
			}
		},
		onLoad(options) {
			console.log(options.State,'options.State');
			this.type = options.State
			var titleD = ''
			if (this.type == 1) {
				titleD = '确认换车订单'
				this.state=4
				this.MDorderApi()
			} else {
				titleD = '送车上门订单'
				this.state=0
				this.pick_type=3
				this.MDorderApi()
			}
			uni.setNavigationBarTitle({
				title: titleD // 替换为您的标题
			});
		},
		onShow() {
			setTimeout(()=>{
				this.MDorderApi()
			},1000)
		},
		methods: {
			//详情
			LinkMDorderXQ(item) {
				console.log('点击的',item);
				uni.navigateTo({
					url: '/pages/erkaiPages/MDorderXQ?order_number=' + item.number
				})
			},
			MDorderApi(){
				uni.request({
					url:this.YM + '/api/store/orderList',
					method:'POST',
					header:{
						'token':uni.getStorageSync('MDtoken')
					},
					data:{
						page:1,
						list_rows:999999999,
						state:this.state,
						search:'',
						pick_type:this.pick_type
					},
					success:res=>{
						console.log(res.data.data.data,'列表');
						this.order=res.data.data.data
					}
				})
			},

		}
	}
</script>

<style>
	.Sc {
		width: 100vw;
		min-height: 100vh;
		float: left;
		background-color: #F5F5F5;
	}

	.Mindex_list {
		width: 694rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 2rpx 12rpx 0rpx rgba(0, 0, 0, 0.06);
		border-radius: 20rpx;
		margin: 20rpx auto 0;
		padding-top: 15rpx;
		padding-bottom: 25rpx;
	}

	.Mindex_list_title {
		width: 622rpx;
		height: 90rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin: 0 auto;
		border-bottom: 2rpx solid #F5F5F5;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 28rpx;
		color: #000000;
	}

	.Mindex_list_title_L {
		width: 450rpx;
		height: 100%;
		display: flex;
		align-items: center;
	}

	.Mindex_list_title_L_icon {
		width: 32rpx;
		height: 32rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 15rpx;
	}

	.Mindex_list_body {
		width: 622rpx;
		height: 144rpx;
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
	}

	.Mindex_list_body_Src {
		width: 196rpx;
		height: 144rpx;
		border-radius: 12rpx;
		overflow: hidden;
		margin-right: 20rpx;
		position: relative;

	}

	.Mindex_list_body_Src_title {
		width: 60rpx;
		height: 32rpx;
		background: #ED9939;
		border-radius: 12rpx 0rpx 8rpx 0rpx;
		text-align: center;
		line-height: 32rpx;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 22rpx;
		color: #FFFFFF;
		position: absolute;
		left: 0;
		top: 0;
		z-index: 999999;
	}

	.Mindex_list_body_L {
		width: 400rpx;
		height: 144rpx;
		color: #000000;
		font-size: 28rpx;
	}

	.Mindex_price {
		width: 622rpx;
		height: 70rpx;
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-family: Roboto, Roboto;
		font-weight: bold;
		font-size: 36rpx;
		color: #000000;
	}

	.Mindex_price_btn {
		width: 152rpx;
		height: 64rpx;
		border-radius: 32rpx 32rpx 32rpx 32rpx;
		border: 2rpx solid #CCCCCC;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 28rpx;
		color: #666666;
		text-align: center;
		line-height: 64rpx;
	}
</style>