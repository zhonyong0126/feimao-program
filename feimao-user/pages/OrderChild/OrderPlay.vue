<template>
	<view class="Play">

		<view class="Play_price">
			<view class="Play_price_text">支付金额</view>
			<view class="Play_price_num">
				<text style="font-size: 40rpx;">￥</text>
				<text>{{PlayPrice}}</text>
			</view>
		</view>

		<view class="Play_box">
			<view class="Play_box_box">
				<view class="Play_box_box_L">
					<view class="Play_box_box_L_icon">
						<image src="../../static/weixinlogo.png" class="imageBFB"></image>
					</view>
					<text>微信支付</text>
				</view>
				<view class="Play_box_box_R">
					<radio :value="value" :checked="checked" @click="RadioClick" class="RadioCss" color='#ED9939' />
				</view>
			</view>
		</view>
		
		<view class="Play_btn" @click="ClickPlay">立即支付</view>

	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM:getApp().globalData.apiBase,
				value: '',
				checked: false,
				PlayPrice:0,
				clickTimer: null,
				isPaying: false
			}
		},
		onShow() {
			this.PlayPrice=uni.getStorageSync('PlayPrice')
			this.PlayPrice = parseFloat(this.PlayPrice).toFixed(2);


		},
		methods: {
			//点击立即支付
			ClickPlay(){
				if (this.isPaying) return
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
				if(this.checked==false){
					uni.showToast({
						title:'请选择支付方式',
						icon:'none',
						duration: 2000
					});
					return false
				}else{
					this.isPaying = true
					
				// 1租车 2买车 3维修服务
				if(uni.getStorageSync('PlayNum')=='1'){
					
					let data={
						order_number:uni.getStorageSync('order_number')
					}
					uni.request({
						url:this.YM + '/api/order/againLeaseCarPay',
						method:'POST',
						data,
						header:{
							'token':uni.getStorageSync('token'),
						},
							success:res=>{
								this.isPaying = false
							console.log(res,'租车订单再次支付');
							if(res.data.code==1){
								uni.requestPayment({
									provider: 'wxpay',
									orderInfo: '',
									appId: res.data.data.appId,
									nonceStr: res.data.data.nonceStr, //随机字符串，长度为32个字符以下。
									timeStamp: res.data.data.timeStamp, //时间戳，要字符串类型的
									package: res.data.data.package, //prepay_id 参数值，提交格式如：prepay_id=xx
									signType: res.data.data.signType, //MD5类型
									paySign: res.data.data.paySign, //签名
									success: function(res) {
										uni.navigateTo({
											url:'/pages/success/successPlayCG'
										})
									},
									fail: function(err) {
										uni.showToast({
											title: '支付失败！',
											icon: 'error'
										})
									}
								});
								
							}else{
								uni.showToast({
									title:res.data.msg,
									icon:'none',
									duration: 2000
								});
								return false
							}
						},
						fail: () => {
							this.isPaying = false
							uni.showToast({ title: '支付请求失败，请稍后重试', icon: 'none' })
						}
					})
					
				}else if(uni.getStorageSync('PlayNum')=='2'){
					
					let Maiche={
						order_number:uni.getStorageSync('order_number')
					}
					uni.request({
						url:this.YM + '/api/order/againBuyCarPay',
						method:'POST',
						header:{
							'token':uni.getStorageSync('token'),
						},
						data:Maiche,
						success:res=>{
							this.isPaying = false
							console.log(res,'购车订单再次支付');
							if(res.data.code==1){
								uni.requestPayment({
									provider: 'wxpay',
									orderInfo: '',
									appId: res.data.data.appId,
									nonceStr: res.data.data.nonceStr, //随机字符串，长度为32个字符以下。
									timeStamp: res.data.data.timeStamp, //时间戳，要字符串类型的
									package: res.data.data.package, //prepay_id 参数值，提交格式如：prepay_id=xx
									signType: res.data.data.signType, //MD5类型
									paySign: res.data.data.paySign, //签名
									success: function(res) {
										uni.navigateTo({
											url:'/pages/success/successPlayCG'
										})
									},
									fail: function(err) {
										uni.showToast({
											title: '支付失败！',
											icon: 'error'
										})
									}
								});
							}else{
								uni.showToast({
									title:res.data.msg,
									icon:'none',
									duration: 2000
								});
								return false
							}
						},
						fail: () => {
							this.isPaying = false
							uni.showToast({ title: '支付请求失败，请稍后重试', icon: 'none' })
						}
					})
					
				}else if(uni.getStorageSync('PlayNum')=='3'){
					
					let weixiu={
						order_number:uni.getStorageSync('order_number')
					}
					uni.request({
						url:this.YM + '/api/order/againMaintainPay',
						method:'POST',
						header:{
							'token':uni.getStorageSync('token'),
						},
						data:weixiu,
						success:res=>{
							this.isPaying = false
							console.log(res,'维修订单再次支付');
							if(res.data.code==1){
								uni.requestPayment({
									provider: 'wxpay',
									orderInfo: '',
									appId: res.data.data.appId,
									nonceStr: res.data.data.nonceStr, //随机字符串，长度为32个字符以下。
									timeStamp: res.data.data.timeStamp, //时间戳，要字符串类型的
									package: res.data.data.package, //prepay_id 参数值，提交格式如：prepay_id=xx
									signType: res.data.data.signType, //MD5类型
									paySign: res.data.data.paySign, //签名
									success: function(res) {
										uni.navigateTo({
											url:'/pages/success/successPlayCG'
										})
									},
									fail: function(err) {
										uni.showToast({
											title: '支付失败！',
											icon: 'error'
										})
									}
								});
							}else{
								uni.showToast({
									title:res.data.msg,
									icon:'none',
									duration: 2000
								});
								return false
							}
						},
						fail: () => {
							this.isPaying = false
							uni.showToast({ title: '支付请求失败，请稍后重试', icon: 'none' })
						}
					})
					
				}else if(uni.getStorageSync('PlayNum')=='4'){
					let FQplay={
						order_number:uni.getStorageSync('order_number'),
						// should_id:uni.getStorageSync('should_id')
					}
					uni.request({
						url:this.YM + '/api/order/againLeaseCarPay',
						method:'POST',
						header:{
							'token':uni.getStorageSync('token'),
						},
						data:FQplay,
						success:res=>{
							this.isPaying = false
							console.log(res,'分期付款当前期数');
							if(res.data.code==1){
								uni.requestPayment({
									provider: 'wxpay',
									orderInfo: '',
									appId: res.data.data.appId,
									nonceStr: res.data.data.nonceStr, //随机字符串，长度为32个字符以下。
									timeStamp: res.data.data.timeStamp, //时间戳，要字符串类型的
									package: res.data.data.package, //prepay_id 参数值，提交格式如：prepay_id=xx
									signType: res.data.data.signType, //MD5类型
									paySign: res.data.data.paySign, //签名
									success: function(res) {
										uni.navigateTo({
											url:'/pages/success/successPlayCG'
										})
									},
									fail: function(err) {
										uni.showToast({
											title: '支付失败！',
											icon: 'error'
										})
									}
								});
							}else{
								uni.showToast({
									title: res.data.msg,
									icon: 'error'
								})
								return
							}
						},
						fail: () => {
							this.isPaying = false
							uni.showToast({ title: '支付请求失败，请稍后重试', icon: 'none' })
						}
					})
				}else{
					this.isPaying = false
					uni.showToast({ title: '订单支付类型错误', icon: 'none' })
				}
				}
				}, 500); // 设置延迟时间，单位为毫秒
			},
			RadioClick() {
				this.checked = !this.checked
			}
		}
	}
</script>

<style>
	.Play {
		width: 100vw;
		height: 100vh;
		background-color:#FFFFFF;
		float: left;
	}

	.Play_price {
		width: 300rpx;
		/* height: 200rpx; */
		/* background-color: aqua; */
		margin: 100rpx auto 0;
	}

	.Play_price_text {
		width: 100%;
		height: 50rpx;
		line-height: 50rpx;
		text-align: center;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #101010;
	}

	.Play_price_num {
		width: 100%;
		height: 60rpx;
		margin-top: 15rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 66rpx;
		font-family: DIN;
		font-weight: bold;
		color: #101010;
	}

	.Play_box {
		width: 676rpx;
		height: 210rpx;
		/* background-color: aquamarine; */
		border-top: 1rpx solid #E1E1E1;
		display: flex;
		align-items: center;
		justify-content: center;
		margin: 60rpx auto 0;
	}

	.Play_box_box {
		width: 100%;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		/* background-color: yellow; */
	}

	.Play_box_box_L {
		width: 400rpx;
		height: 100%;
		/* background-color: greenyellow; */
		display: flex;
		align-items: center;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
	}

	.Play_box_box_L_icon {
		width: 46rpx;
		height: 40rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 15rpx;
	}

	.Play_box_box_R {
		width: 40rpx;
		height: 40rpx;
		/* background-color: green; */
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.RadioCss {
		transform: scale(0.9)
	}
	.Play_btn{
		width: 556rpx;
		height: 88rpx;
		background: #ED9939;
		border-radius: 44rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #FFFFFF;
		text-align: center;
		line-height: 88rpx;
		margin: 20rpx auto 0;
	}
</style>
