<template>
	<view class="Cl">
		<view class="Cl_nav">
			<view class="Cl_nav_title">车辆管理</view>
		</view>

		<view class="Cl_tab">
			<u-tabs :list="list1" @click="click" :activeStyle="{
			    color: '#303133',
			    fontWeight: 'bold',
			    transform: 'scale(1.05)'
			}" lineColor='#ED9939' :current='current' itemStyle="height: 40px;width:120px;"></u-tabs>
		</view>

		<view class="Cl_list">
			<view class="Cl_list_order" v-for="(item,index) in cshaData" :key="index">
				<view class="Cl_list_order_info">
					<view class="Cl_list_order_info_src flex">
						<image :src="YM + item.images[0]" class="imageBFB"></image>
					</view>
					<view class="Cl_list_order_info_tex">
						<text style="display: inline-block;margin-top: 8rpx;">品牌:<text
								style="color: #000000">{{item.brand_name}}</text></text><br />
						<text style="display: inline-block;margin-top: 8rpx;">型号:<text
								style="color: #000000">{{item.specification}}</text></text><br />
						<view class="Cl_list_order_info_src_text">
							<text>车辆名称：</text>
							<view class="Cl_list_order_info_src_text_text">{{item.name}}</view>
						</view>
					</view>
				</view>

				<view class="Cl_list_order_data">
					<view class="Cl_list_order_data_list">
						<view class="Cl_list_order_data_list_for">押金：<text
								style="color: #E64638;">{{item.cash_price}}元</text></view>
						<view class="Cl_list_order_data_list_for">库存：<text
								style="color: #E64638;">{{item.total_stock}}辆</text></view>
						<view class="Cl_list_order_data_list_for">可租：<text
								style="color: #E64638;">{{item.stock}}辆</text></view>
					</view>
					<view class="Cl_list_order_data_list">
						<!-- <view class="Cl_list_order_data_list_for">时租：<text
								style="color: #E64638;">{{item.hour_price}}元</text></view> -->
						<view class="Cl_list_order_data_list_for">日租：<text
								style="color: #E64638;">{{item.day_price}}元</text></view>
						<view class="Cl_list_order_data_list_for">月租：<text
								style="color: #E64638;">{{item.month_price}}元</text></view>
					</view>
				</view>

				<view class="Cl_list_btn">
					<view class="Cl_list_btn_btn" @click="Cxiajia(item,2)" v-if="current==0">下架</view>
					<view class="Cl_list_btn_btn" @click="Cxiajia(item,1)" v-if="current==1">上架</view>
					<view class="Cl_list_btn_btn" style="background-color: #FFE6CB;color: #ED9939;margin-left: 28rpx;" @click="Cbianji(item)">
						编辑</view>
					<view class="Cl_list_btn_btn" style="background-color: #FFD2D2;color: red;margin-left: 28rpx;"
						v-if="current==1" @click="Cdelete(item)">删除</view>
				</view>
			</view>
			
			<view style="width: 100%;height: 150rpx;text-align: center;line-height: 150rpx;font-size: 28rpx;color: #ccc;">没有更多啦~</view>
		</view>


		<u-popup :show="show" :round="10" mode="center" @close="close" @open="open" :safeAreaInsetBottom='false'>
			<view class="xiajia">
				<view class="xiajia_title">提示</view>
				<view class="xiajia_text" v-if="current==0">确定下架该车辆吗？</view>
				<view class="xiajia_text" v-if="current==1">确定上架该车辆吗？</view>
				<view class="xiajia_btn">
					<view @click="show=false">取消</view>
					<view style="background-color: #ED9939;color: #FFFFFF;" @click="CxiajiaApi">确定</view>
				</view>
			</view>
		</u-popup>

		<u-popup :show="show1" :round="10" mode="center" @close="close" @open="open" :safeAreaInsetBottom='false'>
			<view class="xiajia">
				<view class="xiajia_title">提示</view>
				<view class="xiajia_text">确定要删除该车辆吗？</view>
				<view class="xiajia_btn">
					<view @click="show1=false">取消</view>
					<view style="background-color: #ED9939;color: #FFFFFF;" @click="deletechash">确定</view>
				</view>
			</view>
		</u-popup>


		<view class="Yuan" @click="ALLcash">
			<view class="Yuan_text">新增车型</view>
		</view>


		<!-- tabbar -->
		<view class="tabbar">
			<view class="tabbar_tabbar">
				<view class="tabbar_tabbar_list" v-for="(item,index) in TabbarData" :key="index" @click="TabbarC(item)">
					<view class="tabbar_tabbar_list_icon">
						<image :src="item.isA ? item.icon : item.iconI" class="imageBFB"></image>
					</view>
					<view class="tabbar_tabbar_list_text" :style="{color:item.isA?'#ED9939':'#999999'}">{{item.name}}
					</view>
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
				list1: [{
					name: '已上架',
				}, {
					name: '已下架',
				}],
				current: 0,
				TabbarData: [{
						name: '首页',
						icon: '../../static/tabbar/index1.png',
						iconI: '../../static/tabbar/index2.png',
						isA: false
					},
					{
						name: '车辆管理',
						icon: '../../static/tabbar/che1.png',
						iconI: '../../static/tabbar/che2.png',
						isA: true
					},
					{
						name: '门店管理',
						icon: '../../static/tabbar/men1.png',
						iconI: '../../static/tabbar/men2.png',
						isA: false
					}
				],
				show: false,
				show1: false,
				cshaData: '',
				xiajiaID:'',
				xiajiashangjia:'',
				deleteid:''
			}
		},
		onShow() {
			this.CshaList()
		},
		methods: {
			deletechash(){
				uni.request({
					url: this.YM + '/api/store/carDel',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					data: {
						id:this.deleteid,
					},
					success: res => {
						if(res.data.code==1){
							uni.showToast({
								title:res.data.msg,
								icon:'none',
								duration: 2000
							});
							this.CshaList()
							this.show1=false
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
			CxiajiaApi(){
				this.status=this.xiajiashangjia //1上架 2下架
				uni.request({
					url: this.YM + '/api/store/carStatus',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					data: {
						id:this.xiajiaID,
						status:this.status
					},
					success: res => {
						if(res.data.code==1){
							uni.showToast({
								title:res.data.msg,
								icon:'none',
								duration: 2000
							});
							this.CshaList()
							this.show=false
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
			CshaList() {
				uni.request({
					url: this.YM + '/api/store/carList',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					data: {
						page: 1,
						list_rows: 999999999,
						status: this.current + 1
					},
					success: res => {
						console.log(res.data.data.data, '车辆列表');
						this.cshaData = res.data.data.data
					}
				})
			},
			Cbianji(item){
				console.log('点击的车辆',item);
				uni.setStorageSync('allc',item)
				uni.navigateTo({
					url: '/pages/erkaiPages/MDallc?type=' + 3
				})
				
			},
			//新增车辆
			ALLcash() {
				uni.removeStorageSync('allc')
				uni.navigateTo({
					url: '/pages/erkaiPages/MDallc?type=' + 2
				})
			},
			// 删除
			Cdelete(item) {
				this.show1 = true
				this.deleteid=item.id
			},
			//下架
			Cxiajia(item,type) {
				this.xiajiaID=item.id
				this.xiajiashangjia=type
				this.show = true
			},
			// tabs
			click(item) {
				this.pages = 1 //每次进入的时候初始化数据请求的页数
				console.log('item', item);
				this.current = item.index
				this.CshaList()
			},
			TabbarC(item) {
				console.log('点击了', item.name);
				if (item.name == '首页') {
					uni.redirectTo({
						url: '/pages/erkaiPages/MDindex'
					})
				} else if (item.name == '门店管理') {
					uni.redirectTo({
						url: '/pages/erkaiPages/MDguanli'
					})
				}
			},
			open() {},
			close() {
				this.show = false
				this.show1 = false
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

	.tabbar_tabbar {
		width: 100%;
		height: 98rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.tabbar_tabbar_list {
		flex: 3;
		height: 100%;
		float: left;
	}

	.tabbar_tabbar_list_icon {
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin: 10rpx auto 0;
	}

	.tabbar_tabbar_list_text {
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
	.Cl {
		width: 100vw;
		min-height: 100vh;
		background-color: #F5F5F5;
		float: left;
	}

	.Cl_nav {
		width: 100%;
		height: 200rpx;
		background-color: #FFFFFF;
		float: left;
	}

	.Cl_nav_title {
		width: 100%;
		height: 60rpx;
		text-align: center;
		line-height: 60rpx;
		font-weight: 600;
		font-size: 30rpx;
		color: #000000;
		margin-top: 100rpx;
	}

	.Cl_tab {
		width: 100%;
		height: 80rpx;
		background-color: #ffffff;
		float: left;
		display: flex;
		align-items: center;
		justify-content: space-around;

	}

	.Cl_list {
		width: 100%;
		float: left;
	}

	.Cl_list_order {
		width: 694rpx;
		background: #FFFFFF;
		border-radius: 20rpx;
		margin: 20rpx auto 0;
		padding-bottom: 28rpx;
	}

	.Cl_list_order_info {
		width: 644rpx;
		height: 200rpx;
		border-bottom: 2rpx solid #F5F5F5;
		margin: 0 auto;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.Cl_list_order_info_src {
		width: 196rpx;
		height: 144rpx;
		border-radius: 12rpx;
		overflow: hidden;
		margin-right: 14rpx;
	}

	.Cl_list_order_info_tex {
		width: 430rpx;
		height: 144rpx;
		font-size: 28rpx;
		color: #999999;
	}

	.Cl_list_order_info_src_text {
		width: 100%;
		height: auto;
		display: flex;
		align-items: center;
		margin-top: 8rpx;
	}

	.Cl_list_order_info_src_text_text {
		width: 270rpx;
		height: auto;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		color: #000000;
	}

	.Cl_list_order_data {
		width: 644rpx;
		height: 140rpx;
		border-bottom: 2rpx solid #F5F5F5;
		margin: 0 auto;
	}

	.Cl_list_order_data_list {
		width: 100%;
		height: 50%;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.Cl_list_order_data_list_for {
		flex: 3;
		height: 100%;
		display: flex;
		align-items: center;
		color: #000000;
		font-size: 28rpx;
	}

	.Cl_list_btn {
		width: 644rpx;
		height: 60rpx;
		margin: 28rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: flex-end;
	}

	.Cl_list_btn_btn {
		width: 112rpx;
		height: 56rpx;
		background: #F5F5F5;
		border-radius: 60rpx;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 28rpx;
		color: #000000;
		text-align: center;
		line-height: 56rpx;
	}

	.xiajia {
		width: 586rpx;
		height: 352rpx;
		float: left;
	}

	.xiajia_title {
		width: 100%;
		height: auto;
		text-align: center;
		font-family: PingFang SC, PingFang SC;
		font-weight: bold;
		font-size: 32rpx;
		color: #000000;
		margin-top: 32rpx;
	}

	.xiajia_text {
		width: 100%;
		height: auto;
		text-align: center;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 28rpx;
		color: #999999;
		margin-top: 32rpx;
	}

	.xiajia_btn {
		width: 90%;
		height: 80rpx;
		display: flex;
		align-items: center;
		justify-content: space-around;
		margin: 80rpx auto 0;
	}

	.xiajia_btn>view {
		width: 200rpx;
		height: 80rpx;
		background: #F7F7F7;
		border-radius: 60rpx 60rpx 60rpx 60rpx;
		border: 2rpx solid #E8E8E8;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 28rpx;
		color: #000000;
		text-align: center;
		line-height: 80rpx;
	}

	.Yuan {
		width: 120rpx;
		height: 120rpx;
		background: #ED9939;
		box-shadow: 0rpx 4rpx 12rpx 0rpx rgba(255, 151, 0, 0.12);
		border-radius: 50%;
		position: fixed;
		right: 10rpx;
		bottom: 530rpx;
		z-index: 999;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Yuan_text {
		width: 56rpx;
		height: 80rpx;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 28rpx;
		color: #FFFFFF;
		line-height: 40rpx;
		text-align: left;
		font-style: normal;
		text-transform: none;
	}
</style>