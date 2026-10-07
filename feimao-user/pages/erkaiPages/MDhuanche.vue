<template>
	<view class="hc">
		<view class="hc_box">
			<view class="hc_box_title">
				<text>车辆情况</text>
				<view style="display: flex;align-items: center;justify-content: flex-end;">
					<view class="hc_box_title_R" v-for="(item,index) in radioData" :key="index" @click="SEradiui(item)">
						<view class="wxtj_two_xx_box_kuai_B_radio" v-if="item.isA">
							<image src="../../static/ZseletA.png" class="imageBFB"></image>
						</view>
						<view class="wxtj_two_xx_box_kuai_B_radio" v-else>
							<image src="../../static/ZseletB.png" class="imageBFB"></image>
						</view>
						<text :style="{color: item.isA ? 'black' : '#9C9BA2'}">{{item.name}}</text>
					</view>
				</view>
			</view>
			
			<view class="hc_box_title">
				<text>退款金额</text>
				<text></text>
			</view>
			
			<view class="hc_box_input">
				<text>￥</text>
				<u--input border="false" v-model="refund_cash_price"></u--input>
			</view>
			
			<view class="hc_box_title" style="color: #ED9939;">
				<text>注：无损立即退款，有损退款需平台审核</text>
				<text></text>
			</view>
			
		</view>
		
		
		<view class="hc_btn" @click="Ctj">提交</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				order_number:'',
				radioData:[{
					name:'无损',
					isA:true,
					type:1
				},
				{
					name:'有损',
					isA:false,
					type:2
				}],
				return_type:1,
				refund_cash_price:''
			}
		},
		onLoad(options) {
			this.order_number=options.number
		},
		onShow() {
			this.refund_cash_price=uni.getStorageSync('cash_price')
		},
		methods: {
			Ctj(){
				uni.request({
					url:this.YM + '/api/store/backCar',
					method:'POST',
					header:{
						'token':uni.getStorageSync('MDtoken')
					},
					data:{
						order_number:this.order_number,
						refund_cash_price:this.refund_cash_price,
						return_type:this.return_type
					},
					success:res=>{
						console.log(res,'换车');
						if(res.data.code==1){
							this.refund_cash_price=''
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
			},
			SEradiui(item){
				for (var i = 0; i < this.radioData.length; i++) {
					this.radioData[i].isA=false
				}
				item.isA=true
				this.return_type=item.type
			}
		}
	}
</script>

<style>
.hc{
	width: 100vw;
	height: 100vh;
	float: left;background-color: #F5F5F5;
}
.hc_box{
	width: 696rpx;
	border-radius: 20rpx;
	background-color: rgba(255, 255, 255, 1);
	text-align: center;
	margin: 20rpx auto 0;
	padding-top: 10rpx;
	padding-bottom: 20rpx;
}
.hc_box_title{
	width: 90%;
	height: 70rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin: 0 auto;
	color: rgba(16, 16, 16, 1);
	font-size: 28rpx;
	
}
.hc_box_title_R{
	height: 100%;
	display: flex;
	align-items: center;
	justify-content: flex-end;
	margin-left: 15rpx;
}

	.wxtj_two_xx_box_kuai_B_radio {
		width: 36rpx;
		height: 36rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 8rpx;
	}
	
	.hc_box_input{
		width: 90%;
		height: 95rpx;
		border-bottom: 2rpx solid #dfdfdf;
		display: flex;
		align-items: center;
		margin: 15rpx auto 0;
		color: rgba(19, 36, 70, 1);
		font-size: 48rpx;
	}
	
	.hc_btn{
		width: 556rpx;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 44rpx;
		background-color: rgba(237, 153, 57, 1);
		color: rgba(255, 255, 255, 1);
		font-size: 28rpx;
		text-align: center;
		font-family: Arial;
		margin: 30rpx auto 0;
	}
</style>
