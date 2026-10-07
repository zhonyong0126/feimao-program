<template>
	<view class="list">
			<view class="Index_Bto_list_for" v-for="(item,index) in ListData" :key="index" @click="ClickDp(item)">
				<view class="Index_Bto_list_for_top">
					<view class="Index_Bto_list_for_top_image">
						<image :src="YM + item.avatar" class="imageBFB"></image>
					</view>
					<view class="Index_Bto_list_for_top_right">
						<view class="Index_Bto_list_for_top_right1">
							<text>{{item.name}}</text>
							<text>
								<text style="font-size: 24rpx;color: #E64638;">￥</text>
								<text style="font-size: 40rpx;color: #E64638;">{{item.min_price}}</text>
								<text style="font-size: 24rpx;color: #E64638;font-weight: 500;">起</text>
							</text>
						</view>
		
						<view class="Index_Bto_list_for_top_right2">营业时间: {{item.business}}</view>
						<view class="Index_Bto_list_for_top_right3">{{item.address}}</view>
					</view>
				</view>
				<view class="Index_Bto_list_for_bto">
					<view class="Index_Bto_list_for_bto_left">
						<view class="Index_Bto_list_for_bto_left_icon">
							<image src="../../static/kz1.png" class="imageBFB"></image>
						</view>
						<text>可租车辆:</text>
						<text style="color: black;">{{item.car_num}}</text>
					</view>
					<view class="Index_Bto_list_for_bto_right">
						<view class="Index_Bto_list_for_bto_right_kuai" v-if="item.pick_type==1">到店取车</view>
						<view class="Index_Bto_list_for_bto_right_kuai" v-if="item.pick_type==2">到店取车</view>
						<view class="Index_Bto_list_for_bto_right_kuai" v-if="item.pick_type==2">送车上门</view>
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
				ListData:[{
					index_image:'',
					name:'',
					min_price:'',
					business:'',
					address:'',
					car_num:'',
				}]
			}
		},
		onShow() {
			this.MendiNAPI()
		},
		methods: {
			ClickDp(item){
				console.log(item,'点击的');
				uni.setStorageSync('merchant_id',item.merchant_id)
				uni.redirectTo({
					url:'/pages/IndexChild/XQ'
				})
			},
			//门店列表
			MendiNAPI() {
				uni.request({
					url: this.YM + '/api/index/merchantList',
					method: 'POST',
					data:{
						lng:uni.getStorageSync('lng'),
						lat:uni.getStorageSync('lat'),
						page:1,
						list_rows:999999999
					},
					success: res => {
						console.log(res.data.data.data, '门店列表');
						this.ListData=res.data.data.data
					}
				})
			},
		}
	}
</script>

<style>
.list{
	width: 100vw;
	min-height: 100vh;
	float: left;
}

	.Index_Bto_list_for {
		width:700rpx;
		height: 288rpx;
		border-radius: 20rpx;
		background-color: rgba(255, 255, 255, 1);
		text-align: center;
		box-shadow: 0px 4rpx 12rpx 0px rgba(0, 0, 0, 0.06);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-wrap: wrap;
		margin: 20rpx auto 0;
	}

	.Index_Bto_list_for_top {
		width: 648rpx;
		height: 170rpx;
		display: flex;
		align-items: flex-start;
		border-bottom: 1rpx solid #BBBBBB;
		/* background-color: hotpink; */
	}

	.Index_Bto_list_for_top_image {
		width: 140rpx;
		height: 140rpx;
		border-radius: 8rpx;
		/* background-color: green; */
		overflow: hidden;
		margin-top: 8rpx;
	}

	.Index_Bto_list_for_top_right {
		width: 490rpx;
		height: 140rpx;
		/* background-color: yellow; */
		margin-left: 15rpx;
		margin-top: 8rpx;
	}

	.Index_Bto_list_for_top_right1 {
		width: 100%;
		height: 55rpx;
		/* background-color: darkseagreen; */
		display: flex;
		align-items: center;
		justify-content: space-between;
		color: rgba(0, 0, 0, 1.0);
		font-size: 30rpx;
		font-weight: bold;
	}

	.Index_Bto_list_for_top_right2 {
		width: 100%;
		height: 40rpx;
		color: rgba(28, 37, 61, 1);
		font-size: 26rpx;
		line-height: 40rpx;
		text-align: left;
		font-weight: 600;
	}

	.Index_Bto_list_for_top_right3 {
		width: 100%;
		height: 30rpx;
		line-height: 30rpx;
		color: rgba(133, 136, 150, 1);
		font-size: 24rpx;
		margin-top: 15rpx;
		text-align: left;
	}

	.Index_Bto_list_for_bto {
		width: 648rpx;
		height: 70rpx;
		/* background-color: violet; */
		display: flex;
		align-items: center;
	}

	.Index_Bto_list_for_bto_left,
	.Index_Bto_list_for_bto_right {
		width: 50%;
		height: 100%;
		/* background-color: darksalmon; */
		display: flex;
		align-items: center;
		justify-content: flex-start;
		color: rgba(133, 136, 150, 1);
		font-size: 28rpx;
		font-weight: 600;
	}

	.Index_Bto_list_for_bto_left_icon {
		width: 32rpx;
		height: 42rpx;
		/* background-color: green; */
		margin-right: 15rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}


	.Index_Bto_list_for_bto_right {
		/* background-color: antiquewhite; */
		justify-content: flex-end;
	}

	.Index_Bto_list_for_bto_right_kuai {
		width: 110rpx;
		height: 36rpx;
		line-height: 36rpx;
		border-radius: 6rpx;
		background-color: rgba(255, 255, 255, 1);
		color: rgba(161, 161, 161, 1);
		font-size: 22rpx;
		text-align: center;
		font-family: Arial;
		border: 2rpx solid rgba(213, 213, 213, 1);
		margin-left: 10rpx;
	}

	.Index_Bto_list_btn {
		width: 100%;
		height: 130rpx;
		/* background-color: darksalmon; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-top: 60rpx;
	}

	.Index_Bto_list_btn_A {
		width: 556rpx;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 44rpx;
		background-color: rgba(237, 153, 57, 1);
		color: rgba(33, 250, 237, 1);
		font-size: 28rpx;
		text-align: center;
		font-family: Arial;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Index_Bto_list_btn_A_icon {
		width: 24rpx;
		height: 24rpx;
		/* background-color: green; */
		margin-left: 15rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}
</style>
