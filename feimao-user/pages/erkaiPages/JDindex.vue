<template>
	<view class="JD">
		<view class="JD_title">酒店入驻</view>
		<view class="JD_Back" @click="Back">
			<image src="../../static/HeiBack.png" class="imageBFB"></image>
		</view>
		
		<view class="JD_info">
			<view style="display: flex;align-items: center;">
				<view class="JD_info_Src">
					<image :src="YM + JDinfo.avatar" class="imageBFB"></image>
				</view>
				<view class="JD_info_tex">
					<text>{{JDinfo.name}}</text><br />
					<text style="color: #AFAFAF;font-size: 28rpx;font-weight: 500;">ID:{{JDinfo.id}}</text>
				</view>
			</view>
			
			<view class="JD_info_code" @click="CmendianCode"> ▲ 酒店码</view>
		</view>
		
		<view class="JD_price">
			<view class="Img">
				<image src="../../static/JDindexPrice.png" class="imageBFB"></image>
			</view>
			
			<view class="JD_price_box">
				<view class="JD_price_box_L">
					<text style="display: inline-block;margin-top: 20rpx;">余额(元)</text><br />
					<view style="display: flex;align-items: center;margin-top: 20rpx;font-size: 38rpx;">{{JDinfo.money}} <text style="font-size: 26rpx;color: blue;display: inline-block;margin-left: 20rpx;font-weight: 500;" @click="F5price">刷新余额</text></view>
				</view>
				<view class="JD_price_box_R">
					<text style="display: inline-block;margin-top: 20rpx;margin-right: 30rpx;" @click="Ctixian">提现记录</text>
					<view class="JD_price_box_R_btn" @click="Ctxprice">立即提现</view>
				</view>
			</view>
		</view>
		
		<view class="JD_tabs">
			<u-tabs :list="list1" @click="click" :activeStyle="{
			    color: '#303133',
			    fontWeight: 'bold',
			    transform: 'scale(1.05)'
			}" lineColor='#ED9939' :current='current' lineWidth='30'></u-tabs>
		</view>
		
		<view class="JD_list">
			<view class="JD_list_for" v-for="(item,index) in forlist" :key="index">
				<view class="JD_list_for_Src">
					<image :src="YM + item.user_avatar" class="imageBFB"></image>
				</view>
				<view class="JD_list_for_tex">
					<view style="text-align: left;">
						<text style="color: #101010;font-size: 30rpx;font-weight: 600;">{{item.user_name}}</text><br />
						<text style="color: #999999;font-size: 24rpx;display: flex;align-items: center;">
							<text v-if="current==0">下单时间：</text>
							<text>{{item.createtime}}</text>
						</text><br />
						<text style="color: #999999;font-size: 24rpx;" v-if="current==0">编号：{{item.number}}</text>
					</view>
					<text style="color: #ED9939;font-size: 28rpx;font-weight: 600;">￥{{item.price}}</text>
				</view>
			</view>
			
			<view style="width: 100%;height: 200rpx;text-align: center;line-height: 200rpx;font-size: 28rpx;color: #ccc;">没有更多啦~</view>
		</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				list1: [{
					name: '我的订单',
				}, {
					name: '收益明细',
				}],
				current:0,
				YM: getApp().globalData.apiBase,
				JDinfo:'',
				forlist:''
			}
		},
		onShow() {
			this.JDinfoApi()
			this.JDindexApi()
		},
		methods: {
			JDindexApi(){
				uni.request({
					url:this.YM + '/api/shop/index',
					method:'POST',
					header:{
						'token':uni.getStorageSync('JDtoken')
					},
					data:{
						type:this.current + 1, //1我的订单 2收益列表
						page:1,
						list_rows:999999999
					},
					success:res=>{
						console.log(res.data.data.list.data,'首页数据');
						this.forlist=res.data.data.list.data
					},
				})
			},
			F5price(){
				uni.showLoading({
					title: '正在刷新...',
					mask: true
				})
				setTimeout(()=>{
					this.JDinfoApi()
				},800)
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
						this.JDinfo=res.data.data
						uni.hideLoading();
					}
				})
			},
			//立即提现
			Ctxprice(){
				console.log('立即提现');
				uni.navigateTo({
					url:'/pages/erkaiPages/JDtixian?type=' + 1
				})
			},
			//点击了门店码
			CmendianCode(){
				uni.navigateTo({
					url:'/pages/erkaiPages/JDcode?JDcode=' + this.JDinfo.qrimage
				})
			},
			//点击了提现记录
			Ctixian(){
				console.log('点击了提现记录');
				uni.navigateTo({
					url:'/pages/erkaiPages/JDtixianjilu'
				})
			},
			click(item){
				console.log('item', item);
				this.current = item.index
				this.JDindexApi()
			},
			Back(){
				uni.navigateBack()
			}
		}
	}
</script>

<style>
.JD {
    width: 100vw;
    height: 100vh;
    background: linear-gradient(to bottom, #FFF4D7 20%, #FFFFFF 80%);
    float: left;
	position: relative;
}
.JD_title{
	width: 100%;
	height: 60rpx;
	text-align: center;
	line-height: 60rpx;
	position: absolute;
	left: 0;
	top: 100rpx;
	color: rgba(0, 0, 0, 1);
	font-size: 32rpx;
	font-weight: 600;
	z-index: 1;
}
.JD_Back{
	width: 40rpx;
	height: 40rpx;
	position: absolute;
	left: 35rpx;
	top: 110rpx;
	z-index: 9;
	display: flex;
	align-items: center;
	justify-content: center;
}
.JD_info{
	width: 100%;
	height: 100rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin: 210rpx auto 0;
}
.JD_info_Src{
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	overflow: hidden;
	margin-left: 35rpx;
}
.JD_info_tex{
	width: 300rpx;
	height: 100%;
	color: rgba(0, 0, 0, 1.0);
	font-size: 32rpx;
	font-weight: 600;
	margin-left: 20rpx;
}
.JD_info_code{
	width: 136rpx;
	height: 46rpx;
	line-height: 46rpx;
	opacity: 0.63;
	border-radius: 40rpx 0px 0px 40rpx;
	background-color: rgba(237, 153, 57, 1);
	text-align: center;
	display: flex;
	align-items: center;
	justify-content: center;
	color: rgba(255, 255, 255, 1);
	font-size: 24rpx;
}

.JD_price{
	width: 690rpx;
	height: 220rpx;
	border-radius: 20rpx;
	background: linear-gradient(180deg, rgba(255,255,255,1) 57%,rgba(253,244,235,0) 100%);
	text-align: center;
	box-shadow: 0px 2rpx 12rpx 0px rgba(0, 0, 0, 0.04);
	margin: 44rpx auto 0;
	position: relative;
}
.Img{
	width: 176rpx;
	height: 176rpx;
	position: absolute;
	right: 0;
	bottom: 0;
	z-index: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}
.JD_price_box{
	width: 100%;
	height: 100%;
	position: absolute;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	z-index: 5;
	display: flex;
	align-items: center;
	justify-content: space-between;
}
.JD_price_box_L,
.JD_price_box_R{
	height: 80%;
	font-size: 28rpx;
	color: black;
	font-weight: 600;
	margin-left: 50rpx;
	text-align:left;
}
.JD_price_box_R{
	width: 35%;
	margin-left: 0;
	text-align: right;
	display: flex;
	align-items: center;
	justify-content: flex-end;
	flex-wrap: wrap;
}
.JD_price_box_R_btn{
	width: 200rpx;
	height: 76rpx;
	line-height: 76rpx;
	border-radius: 40rpx;
	background-color: rgba(237, 153, 57, 1);
	text-align: center;
	margin-right: 30rpx;
}

.JD_tabs{
	width: 690rpx;
	height: 80rpx;
	margin: 20rpx auto 0;
}
.JD_list{
	width: 690rpx;
	border-radius: 20rpx;
	background-color: rgba(255, 255, 255, 1);
	text-align: center;
	margin: 15rpx auto 0;
	padding-top: 20rpx;
	padding-bottom: 20rpx;
}
.JD_list_for{
	width: 626rpx;
	height: 145rpx;
	border-bottom: 2rpx solid #eaeaea;
	margin: 10rpx auto 0;
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	padding-top: 10rpx;
	padding-bottom: 10rpx;
}
.JD_list_for:last-child{
	border: 0;
}
.JD_list_for_Src{
	width: 96rpx;
	height: 96rpx;
	border-radius: 50%;
	overflow: hidden;
	margin-right: 10rpx;
	margin-top: 15rpx;
}
.JD_list_for_tex{
	width: 510rpx;
	height: 110rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-top: 15rpx;
}
</style>
