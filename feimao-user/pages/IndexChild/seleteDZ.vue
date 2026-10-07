<template>
	<view class="selete">
		
		<view class="selete_box" v-for="(item,index) in AndList" :key="index">
			<view class="selete_box_L">
				<radio :value="value" :checked="item.isA" @click="RadioClick(item)" class="RadioCss" color='#ED9939' />
			</view>
			<view class="selete_box_R">
				<view class="selete_box_R_t">{{item.administrative}}{{item.address}}</view>
				<view class="selete_box_R_b">{{item.name}} {{item.mobile}}</view>
			</view>
		</view>
		
		<view style="width: 100%;height: 88rpx;background-color: transparent;"></view>
		<!-- 新增地址按钮 -->
		<view class="selete_btn" @click="Alladdress">新增地址</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM:getApp().globalData.apiBase,
				value:'',
				AndList:[],
				Ari:''
			}
		},
		onShow() {
			this.Ari=uni.getStorageSync('Ari')
			console.log(this.Ari);
			
			this.dizlistApi()
		},
		beforeDestroy() {
			uni.removeStorageSync('Ari')
		},
		methods: {
			Alladdress(){
				uni.navigateTo({
					url:'/pages/WordChild/AllDizhi'
				})
			},
			dizlistApi() {
				uni.request({
					url: this.YM + '/api/user/addressList',
					method: 'POST',
					header:{
						'token': uni.getStorageSync('token')
					},
					success: res => {
						this.AndList = res.data.code == 1 && res.data.data
							? (res.data.data.data || [])
							: []
						if (res.data.code != 1) {
							uni.showToast({ title: res.data.msg || '地址加载失败', icon: 'none' })
						}
						console.log(this.AndList, '地址列表');
					},
					fail: () => {
						this.AndList = []
						uni.showToast({ title: '地址加载失败，请稍后重试', icon: 'none' })
					}
				})
			},
			RadioClick(item){
				console.log(item,'item');
				uni.setStorageSync('addressitem',item)
				uni.navigateBack({ delta: 1 })
			}
		}
	}
</script>

<style>
.selete{
	width: 100vw;
	min-height: 100vh;
	background-color: #FFFFFF;
	float: left;
	padding-top: 15rpx;
}

.selete_box{
	width: 700rpx;
	height: 120rpx;
	/* background-color: yellow; */
	margin: 20rpx auto 0;
	border-bottom: 1rpx solid #F7F7F7;
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
}
.selete_box_L{
	width: 36rpx;
	height: 36rpx;
	/* background-color: yellow; */
	display: flex;
	align-items: center;
	justify-content: center;
}
.selete_box_R{
	width: 650rpx;
	height: 120rpx;
	/* background-color: skyblue; */
}
.selete_box_R_t{
	width: 100%;
	height: 65%;
	/* background-color: yellow; */
	font-size: 30rpx;
	font-family: PingFang SC;
	font-weight: bold;
	color: #010101;
}
.selete_box_R_b{
	width: 100%;
	height: 35%;
	font-size: 28rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #858896;
	margin-top: 5rpx;
	/* background-color: salmon; */
}
.RadioCss {
		transform: scale(0.8)
	}
	.selete_btn{
		width: 556rpx;
		height: 88rpx;
		background: #ED9939;
		border-radius: 44rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FFFFFF;
		position: fixed;
		left: 50%;
		bottom: 30rpx;
		transform: translate(-50%);
		text-align: center;
		line-height: 88rpx;
	}
</style>
