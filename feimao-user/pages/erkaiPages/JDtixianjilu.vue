<template>
	<view class="jilu">
		
		<view class="jilu_for" v-for="(item,index) in list" :key="index">
			<view>
				<text style="color: #000000;font-size: 30rpx;font-weight: 600;">佣金提现</text><br />
				<text style="color: #858896;font-size: 26rpx;">{{item.createtime}}</text>
			</view>
			<text style="font-size: 28rpx;color: #000000;font-weight: 600;">-{{item.amount}}</text>
		</view>
		
		
		<view style="width: 100%;height: 200rpx;text-align: center;line-height: 200rpx;font-size: 28rpx;color: #CCC;">没有更多啦~</view>
		
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
			uni.request({
				url:this.YM + '/api/shop/cashList',
				method:'POST',
				data:{
					page:1,
					list_rows:9999999999
				},
				header:{
					'token':uni.getStorageSync('JDtoken')
				},
				success:res=>{
					console.log(res.data.data.data,'记录');
					this.list=res.data.data.data
				}
			})
		}
	}
</script>

<style>
.jilu{
	width: 100vw;
	min-height: 100vh;
	background-color: #FFFFFF;
	float: left;
}
.jilu_for{
	width: 680rpx;
	height: 120rpx;
	border-bottom: 2rpx solid #BBBBBB;
	margin: 5rpx auto 0;
	display: flex;
	align-items: center;
	justify-content: space-between;
}
</style>
