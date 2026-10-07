<template>
	<view class="tixian">
		
		<view class="tixian_box">
			<view class="tixian_box_title">提现金额</view>
			<view class="tixian_box_input">
				<view style="display: flex;align-items: center;">
					<text style="font-size: 32rpx;font-weight: 600;">￥</text>
					<u--input border="false" v-model="valuenumber"></u--input>
				</view>
				<text style="font-size: 26rpx;color: #006BFF;" @click="Allprice">全部提现</text>
			</view>
			<view class="tixian_box_text">提交申请后3个工作日，平台将会打款到您的微信账户</view>
		</view>
		<view class="tixian_btn" @click="Ctj">提交</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				valuenumber:'',
				JDmoney:0,
				MDmoney:0,
				type:null
			}
		},

		onLoad(options) {
			this.type=options.type
			
			console.log(this.type,'type');
			
			this.JDinfoApi()
		},
		methods: {
			Ctj(){
				let data={
					amount:this.valuenumber
				}
				if(this.type==1){
					uni.request({
						url:this.YM + '/api/shop/cash',
						method:'POST',
						header:{
							'token':uni.getStorageSync('JDtoken')
						},
						data,
						success:res=>{
							console.log(res,'提现');
							if(res.data.code==1){
								uni.showToast({
									title:res.data.msg,
									icon:'none',
									duration: 2000
								});
								setTimeout(()=>{
									uni.navigateBack()
								},2000)
							}else{
								uni.showToast({
									title:res.data.msg,
									icon:'none',
									duration: 2000
								});
								return false
							}
						}
					})
				}else{
					uni.request({
						url:this.YM + '/api/store/cash',
						method:'POST',
						header:{
							'token':uni.getStorageSync('MDtoken')
						},
						data,
						success:res=>{
							console.log(res,'提现');
							if(res.data.code==1){
								uni.showToast({
									title:res.data.msg,
									icon:'none',
									duration: 2000
								});
								setTimeout(()=>{
									uni.navigateBack()
								},2000)
							}else{
								uni.showToast({
									title:res.data.msg,
									icon:'none',
									duration: 2000
								});
								return false
							}
						}
					})
				}
			},
			Allprice(){
				if(this.type==1){
					this.valuenumber=this.JDmoney
				}else{
					this.valuenumber=this.MDmoney
				}
			},
			JDinfoApi(){
				uni.request({
					url:this.YM + '/api/shop/edit',
					method:'POST',
					header:{
						'token':uni.getStorageSync('JDtoken')
					},
					data:{
						type:1,
					},
					success:res=>{
						console.log(res.data.data,'酒店信息');
						this.JDmoney=res.data.data.money
						
						if(this.type==2){
							this.valuenumber=Number(res.data.data.money)
							console.log('执行了');
						}
					}
				})
			},
			MDindexApi(){
				uni.request({
					url:this.YM + '/api/store/index',
					method:'POST',
					header:{
						'token':uni.getStorageSync('MDtoken')
					},
					success:res=>{
						console.log(res.data.data,'首页数据');
						this.MDmoney=res.data.data.user_money
					},
				})
			},
		}
	}
</script>

<style>
.tixian{
	width: 100vw;
	height: 100vh;
	float: left;
}
.tixian_box{
	width: 696rpx;
	border-radius: 20rpx;
	background-color: rgba(255, 255, 255, 1);
	text-align: center;
	margin: 20rpx auto 0;
	padding-top: 20rpx;
	padding-bottom: 20rpx;
}
.tixian_box_title{
	width: 92%;
	height: 40rpx;
	line-height: 40rpx;
	color: rgba(0, 0, 0, 1.0);
	font-size: 30rpx;
	margin: 0 auto;
	text-align: left;
}
.tixian_box_input{
	width: 92%;
	height: 90rpx;
	border-bottom: 2rpx solid #dedede;
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin: 35rpx auto 0;
}
.tixian_box_text{
	width: 92%;
	height: 40rpx;
	display: flex;
	align-items: center;
	margin: 15rpx auto 0;
	color: rgba(255, 63, 63, 1);
	font-size: 24rpx;
}
.tixian_btn{
	width: 556rpx;
	height: 88rpx;
	line-height: 88rpx;
	border-radius: 44rpx;
	background-color: rgba(237, 153, 57, 1);
	color: rgba(255, 255, 255, 1);
	font-size: 28rpx;
	text-align: center;
	font-family: Arial;
	margin: 25rpx auto 0;
}
</style>
