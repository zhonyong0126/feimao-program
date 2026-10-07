<template>
	<view class="Mindex">

		<view class="Mindex_title">
			<view class="JD_Back" @click="Back">
				<image src="../../static/HeiBack.png" class="imageBFB"></image>
			</view>
			首页</view>
		<view class="Mindex_sos">
			<u--input placeholder="请输入手机号查询" prefixIcon="search" prefixIconStyle="font-size: 22px;color: #909399"
				shape="circle" border="none" v-model="search" @confirm='confirminput'></u--input>
		</view>

		<view class="Mindex_info">
			<view class="Mindex_info_L">
				<view class="Mindex_info_L_Src">
					<image :src="YM + MDinfo.user.avatar" class="imageBFB"></image>
				</view>
				<view>
					<text style="color: #000000;font-size: 32rpx;font-weight: 600;">{{MDinfo.user.name}}</text><br />
					<text style="color: #999999;font-size: 28rpx;">ID:{{MDinfo.user.id}}</text>
				</view>
			</view>
			<view></view>
			<view class="Mindex_info_R">
				<view class="Mindex_info_R_images">
					<image src="../../static/MDinfoicon.png" class="imageBFB"></image>
				</view>
				<view class="Mindex_info_R_btn" @click="LinkMDcode">
					<view class="Mindex_info_R_btn_code">
						<image src="../../static/MDmaicon.png" class="imageBFB"></image>
					</view>
					<text>门店码</text>
				</view>
			</view>
		</view>

		<view class="Mindex_data">
			<view class="Mindex_data_one">
				<view style="display: flex;align-items: center;">
					<view
						style="width: 8rpx;height: 36rpx;background-color: #ED9939;border-radius: 40rpx;margin-right: 20rpx;">
					</view>
					<text style="color: #000000;font-size: 32rpx;font-weight: 600;">收益看板</text>
				</view>
				<view style="display: flex;align-items: center;" @click="TiXianjl">
					<text style="color: #ED9939;font-size: 28rpx;">提现记录</text>
					<view class="Mindex_data_one_icon">
						<image src="../../static/orright.png" class="imageBFB"></image>
					</view>
				</view>
			</view>

			<view class="Mindex_data_two">
				<view class="Mindex_data_two_box">
					<view class="Mindex_data_two_box_icon">
						<image src="../../static/MDSY.png" class="imageBFB"></image>
					</view>
					<view @click="LinkMXI">
						<text style="font-size: 40rpx;color: #000000;font-weight: bold;">{{MDinfo.order_money}}</text><br />
						<text style="font-size: 28rpx;color: #999999;">累计收益(元)</text>
					</view>
				</view>
				<view class="Mindex_data_two_box" @click="LinkTiXian">
					<view class="Mindex_data_two_box_icon" style="width: 66rpx;height: 66rpx;">
						<image src="../../static/MDYE.png" class="imageBFB"></image>
					</view>
					<view>
						<text style="font-size: 40rpx;color: #000000;font-weight: bold;">{{MDinfo.user_money}}</text><br />
						<text style="font-size: 28rpx;color: #999999;">账户余额(元)</text>
					</view>
				</view>
			</view>

			<view class="Mindex_data_therr">注：点击余额进行提现申请操作</view>
		</view>

		<view class="Mindex_order">
			<view class="Mindex_order_box" @click="LinkSc(1)">
				<image src="../../static/MDHC.png" class="imageBFB"></image>
			</view>
			<view class="Mindex_order_box" @click="LinkSc(2)">
				<image src="../../static/MDSM.png" class="imageBFB"></image>
			</view>
		</view>

		<view class="Mindex_tab">
			<u-tabs :list="list1" @click="click" :activeStyle="{
			    color: '#303133',
			    fontWeight: 'bold',
			    transform: 'scale(1.05)'
			}" lineColor='#ED9939' :current='current'></u-tabs>
		</view>

		<view class="Mindex_list" v-for="(item,index) in order" :key="index">
			<view class="Mindex_list_title">
				<view class="Mindex_list_title_L">
					<view class="Mindex_list_title_L_icon">
						<image src="../../static/Hwu.png" class="imageBFB"></image>
					</view>
					<text>{{item.merchant_name}}</text>
				</view>
				<!-- 订单状态：1待支付  2已支付 3进行中 4还车中 5已完成 6已取消 -->
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
				<view style="display: flex;align-items: center;justify-content: flex-end;">
					<view class="Mindex_price_btn" @click="LinkMDorderXQ(item)">查看详情</view>
					<view class="Mindex_price_btn1" @click="HuanChe(item)" v-if="item.state==4 && item.pick_state==1">确认还车</view>
				</view>
			</view>
		</view>
		
		<view style="width: 100%;height: 150rpx;text-align: center;line-height: 150rpx;font-size: 28rpx;color: #ccc;">没有更多啦~</view>

		<view style="width: 100%;height: 180rpx;"></view>



		<!-- tabbar -->
		<view class="tabbar">
			<view class="tabbar_tabbar">
				<view class="tabbar_tabbar_list" v-for="(item,index) in TabbarData" :key="index" @click="TabbarC(item)">
					<view class="tabbar_tabbar_list_icon">
						  <image :src="item.isA ? item.icon : item.iconI" class="imageBFB"></image>
					</view>
					<view class="tabbar_tabbar_list_text" :style="{color:item.isA?'#ED9939':'#999999'}">{{item.name}}</view>
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
				TabbarData:[{
					name:'首页',
					icon:'../../static/tabbar/index1.png',
					iconI:'../../static/tabbar/index2.png',
					isA:true
				},
				{
					name:'车辆管理',
					icon:'../../static/tabbar/che1.png',
					iconI:'../../static/tabbar/che2.png',
					isA:false
				},
				{
					name:'门店管理',
					icon:'../../static/tabbar/men1.png',
					iconI:'../../static/tabbar/men2.png',
					isA:false
				}],
				list1: [{
					name: '全部',
				}, {
					name: '已支付',
				}, {
					name: '进行中'
				}, {
					name: '还车中'
				}, {
					name: '已完成'
				}, {
					name: '已取消'
				}],
				current: 0,
				MDinfo:'',
				state:0,//订单状态：0全部  2已支付 3进行中 4还车中 5已完成 6已取消
				order:'',
				search:''
			}
		},
		onShow() {
			this.MDindexApi()
			this.MDorderApi()
		},
		methods: {
			LinkMXI(){
				uni.navigateTo({
					url:'/pages/erkaiPages/Mxi'
				})
			},
			HuanChe(item){
				uni.setStorageSync('cash_price',item.cash_price)
				console.log(item,'item');
				uni.navigateTo({
					url:'/pages/erkaiPages/MDhuanche?number=' + item.number
				})
			},
			Back(){
				uni.navigateBack()
			},
			confirminput(){
				this.MDorderApi()
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
						search:this.search
					},
					success:res=>{
						console.log(res.data.data.data,'列表');
						this.order=res.data.data.data
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
						this.MDinfo=res.data.data
					},
				})
			},
			TabbarC(item){
				console.log('点击了',item.name);
				if(item.name=='车辆管理'){
					uni.redirectTo({
						url:'/pages/erkaiPages/MDcheliang'
					})
				}else if(item.name=='门店管理'){
					uni.redirectTo({
						url:'/pages/erkaiPages/MDguanli'
					})
				}
			},
			//记录
			TiXianjl() {
				uni.navigateTo({
					url: '/pages/erkaiPages/MDtixianJL'
				})
			},
			//提现
			LinkTiXian() {
				uni.navigateTo({
					url: '/pages/erkaiPages/JDtixian?type=' + 2
				})
			},
			//详情
			LinkMDorderXQ(item) {
				console.log('点击的',item);
				uni.navigateTo({
					url: '/pages/erkaiPages/MDorderXQ?order_number=' + item.number
				})
			},
			//送车上门
			LinkSc(type) {
				if(type==1){
					this.current=3
					this.state=4
					this.MDorderApi()
				}else{
					console.log(type);
					uni.navigateTo({
						url: '/pages/erkaiPages/MDsc?State=' + type
					})
				}
				
				
			},
			//门店码
			LinkMDcode() {
				uni.navigateTo({
					url: '/pages/erkaiPages/MDcode?MDcode=' + this.MDinfo.user.qrimage
				})
			},
			click(item) {
				console.log('item', item);
				this.current = item.index
				// 订单状态：0全部  2已支付 3进行中 4还车中 5已完成 6已取消
				if(item.name=='全部'){
					this.state=0
				}else if(item.name=='已支付'){
					this.state=2
				}else if(item.name=='进行中'){
					this.state=3
				}else if(item.name=='还车中'){
					this.state=4
				}else if(item.name=='已完成'){
					this.state=5
				}else if(item.name=='已取消'){
					this.state=6
				}
				this.MDorderApi()
			}
		}
	}
</script>

<style>
	/* tabbar */
	.tabbar {
		width: 100vw;
		height: 166rpx;
		background-color: #FFFFFF;
		position: fixed;
		left: 0;
		bottom: 0;
		z-index: 999999999999;
		border-top: 2rpx solid #f4f4f4;
	}
	.tabbar_tabbar{
		width: 100%;
		height: 98rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	.tabbar_tabbar_list{
		flex: 3;
		height: 100%;
		float: left;
	}
	.tabbar_tabbar_list_icon{
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin: 10rpx auto 0;
	}
	.tabbar_tabbar_list_text{
		width: 100%;
		text-align: center;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 24rpx;
		color: #ED9939;
		height: 36rpx;
		line-height: 36rpx;
	}

	/* ---------------------------------------------------- */
	.Mindex {
		width: 100vw;
		min-height: 100vh;
		float: left;
		background: linear-gradient(to bottom, #FFF4D7 20%, #FFFFFF 80%);
	}

	.Mindex_title {
		width: 100vw;
		height: 60rpx;
		text-align: center;
		line-height: 60rpx;
		font-size: 30rpx;
		font-weight: 600;
		color: #000000;
		margin-top: 100rpx;
		position: relative;
	}
	.JD_Back{
		width: 40rpx;
		height: 40rpx;
		position: absolute;
		left: 35rpx;
		top: 50%;
		transform: translate(0,-50%);
		z-index: 9;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Mindex_sos {
		width: 630rpx;
		height: 68rpx;
		background: #FFFFFF;
		border-radius: 34rpx 34rpx 34rpx 34rpx;
		margin: 30rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: center;
		padding-left: 15rpx;
	}

	.Mindex_info {
		width: 100%;
		height: 188rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		position: relative;
	}

	.Mindex_info_L {
		width: 540rpx;
		height: 120rpx;
		margin-left: 58rpx;
		display: flex;
		align-items: center;
		position: absolute;
		z-index: 9999;
	}

	.Mindex_info_L_Src {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		overflow: hidden;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 20rpx;
	}

	.Mindex_info_R {
		width: 330rpx;
		height: 100%;
		position: relative;
	}

	.Mindex_info_R_images {
		width: 264rpx;
		height: 188rpx;
		position: absolute;
		left: 0;
		top: 50%;
		z-index: 2;
		transform: translate(0, -50%);
	}

	.Mindex_info_R_btn {
		width: 148rpx;
		height: 60rpx;
		background: rgba(242, 182, 113, 0.6);
		border-radius: 40rpx 0rpx 0rpx 40rpx;
		position: absolute;
		right: 0;
		top: 50%;
		z-index: 999;
		transform: translate(0, -50%);
		display: flex;
		align-items: center;
		justify-content: center;
		font-weight: 400;
		font-size: 26rpx;
		color: #FFFFFF;
	}

	.Mindex_info_R_btn_code {
		width: 36rpx;
		height: 36rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 10rpx;
	}

	.Mindex_data {
		width: 694rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 2rpx 12rpx 0rpx rgba(0, 0, 0, 0.06);
		border-radius: 20rpx 20rpx 20rpx 20rpx;
		margin: 28rpx auto 0;
		padding-top: 30rpx;
		padding-bottom: 30rpx;
	}

	.Mindex_data_one {
		width: 90%;
		margin: 0 auto;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.Mindex_data_one_icon {
		width: 36rpx;
		height: 36rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 4rpx;
	}

	.Mindex_data_two {
		width: 90%;
		height: 125rpx;
		margin: 40rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.Mindex_data_two_box {
		width: 50%;
		height: 100%;
		display: flex;
		align-items: center;
		font-family: Roboto, Roboto;
		font-weight: bold;
		font-size: 40rpx;
		color: #000000;
	}

	.Mindex_data_two_box_icon {
		width: 74rpx;
		height: 68rpx;
		margin-right: 20rpx;
	}

	.Mindex_data_therr {
		width: 90%;
		margin: 20rpx auto 0;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 24rpx;
		color: #ED9939;
	}

	.Mindex_order {
		width: 694rpx;
		height: 112rpx;
		margin: 60rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.Mindex_order_box {
		width: 332rpx;
		height: 112rpx;
		border-radius: 20rpx;
		overflow: hidden;
		position: relative;
	}

	.Mindex_tab {
		width: 100%;
		height: 80rpx;
		margin: 40rpx auto 0;
	}

	.Mindex_list {
		width: 694rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 2rpx 12rpx 0rpx rgba(0, 0, 0, 0.06);
		border-radius: 20rpx 20rpx 20rpx 20rpx;
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

	.Mindex_price_btn,
	.Mindex_price_btn1 {
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

	.Mindex_price_btn1 {
		background-color: #ED9939;
		border: 0;
		color: #FFFFFF;
		margin-left: 15rpx;
	}
</style>