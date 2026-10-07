<template>
	<view class="order">
		<!-- tabs -->
		<view class="order_tabs">
			<u-tabs :list="list1" @click="click" :activeStyle="{
            color: '#303133',
            fontWeight: 'bold',
            transform: 'scale(1.05)'
        }" lineColor='#ED9939' :current='current'></u-tabs>
		</view>
		<!-- 订单类型筛选 -->
		<view class="order_Tab">
			<view class="order_Tab_text" :class="tab.isS?'order_Tab_text':'order_Tab_textA'"
				v-for="(tab,index) in list2" :key="index" @click="TabClick(tab)">{{tab.title}}</view>
		</view>

		<!-- 列表 -->
		<view class="order_list_box">

			<view class="order_list" v-for="(item,index) in OrderData" :key="index">
				<view class="order_list_top">
					<view class="order_list_top_left">
						<view class="order_list_top_left_icon">
							<image src="../../static/orderhw.png" class="imageBFB"></image>
						</view>
						<text>{{item.merchant_name}}</text>
					</view>
					<!-- 订单状态：1待支付  2已支付 3进行中 4还车中 5已完成 6已取消 -->
					<text v-if="item.state==1">待支付</text>
					<text style="color: #333333;" v-if="item.state==2">已支付</text>
					<text style="color: #ED9939;" v-if="item.state==3">进行中</text>
					<text style="color: #ED9939;" v-if="item.state==4">还车中</text>
					<text style="color: #333333;" v-if="item.state==5">已完成</text>
					<text style="color: #333333;" v-if="item.state==6">已取消</text>


				</view>
				<view class="order_list_bto">
					<view class="order_list_bto_left">
						<view class="order_list_bto_left_image">
							<image :src="YM + item.service_image" class="imageBFB"></image>
						</view>
						<view class="order_list_bto_left_jiao" v-if="item.lease_type==3">
							<text v-if="item.lease_type==3">月租</text>
						</view>
						<view class="order_list_bto_left_jiao" v-if="item.lease_type==2">
							<text v-if="item.lease_type==2">日租</text>
						</view>
						<view class="order_list_bto_left_jiao" v-if="item.lease_type==4">
							<text v-if="item.lease_type==4">分期</text>
						</view>
						<view class="order_list_bto_left_jiao" v-if="item.lease_type==1">
							<text v-if="item.lease_type==1">时租</text>
						</view>
					</view>
					<view class="order_list_bto_right">
						<view class="order_list_bto_right1">{{item.service_name}}</view>
						<view class="order_list_bto_right2">取车方式: <text style="color: black;"
								v-if="item.pick_type==3">送车上门</text>
							<text style="color: black;" v-if="item.pick_type==1">到店取车</text>
						</view>
						<view class="order_list_bto_right3" v-if="item.lease_type==4">
							<text>期望送达时间: <text style="color: black;">{{item.take_back_time}}</text></text>
							<text>x{{item.num}}</text>
						</view>

						<view class="order_list_bto_right3" v-if="item.lease_type!==4">
							<text v-if="item.lease_type==1">租赁时间: <text
									style="color: black;">{{item.lease_duration}}小时</text></text>
							<text v-if="item.lease_type==2">租赁时间: <text
									style="color: black;">{{item.lease_duration}}天</text></text>
							<text v-if="item.lease_type==3">租赁时间: <text
									style="color: black;">{{item.lease_duration}}月</text></text>
						</view>
					</view>
				</view>
				<view class="order_list_diz" v-if="item.state==3 || item.state==4">
					<text>还车地址:<text style="color: #000000;">{{item.merchant_address}}</text></text>
				</view>

				<view class="order_list_btn">
					<view class="order_list_btn_left">
						<text style="font-size: 28rpx;">￥</text>
						<text>{{payable(item).toFixed(2)}}</text>
						<text style="color: #858896;font-size: 24rpx;" v-if="item.lease_type!==4">（含押金）</text>
						<text style="color:red;font-size: 24rpx;"
							v-if="item.lease_type==4">（租期{{item.lease_duration}}）</text>
					</view>
					<view class="order_list_btn_right">
						<view class="order_list_btn_right_btn1" @click="ClickCKXQ(item)">查看详情</view>
						<view class="order_list_btn_right_btn2" @click="ClickPlay(item)" v-if="item.state==1">去支付</view>
						<view class="order_list_btn_right_btn2 or" v-if="item.state==3 && item.pick_state==1"
							@click="ClickHC(item)">去还车</view>
						<view class="order_list_btn_right_btn21 or" v-if="item.state==3 && item.pick_state==2"
							@click="ClickSH">还车审核中</view>
					</view>
				</view>
			</view>
			<u-empty mode="list" icon="https://cdn.uviewui.com/uview/empty/car.png" v-if="OrderData==''">
			</u-empty>
		</view>

		<u-modal :show="show" title="确定还车吗?" content='确认还车后需等待商家确认收车才能退还押金，一般3-5分钟，如长时间未收到押金，请在订单详情页致电商家' @confirm='hcconfirm' @cancel='cancel' :showCancelButton='true'></u-modal>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				list1: [{
						name: '全部',
					}, {
						name: '待支付',
					}, {
						name: '已支付'
					}, {
						name: '进行中'
					},
					{
						name: '还车中'
					}, {
						name: '已完成'
					}, {
						name: '已取消'
					}
				],
				list2: [{
						title: '全部',
						isS: true
					},
					{
						title: '租车订单',
						isS: false
					},
					{
						title: '购车订单',
						isS: false
					},
					{
						title: '维修订单',
						isS: false
					}
				],
				current: 0,
				state: 0, //<!-- 订单状态：1待支付  2已支付 3进行中 4还车中 5已完成 6已取消 -->
				type: 0, //1租车 2买车 3维修服务
				pages: 1,
				OrderData: [],
				order_number: '',
				show: false,
				isLoading: false,
				hasMore: true,
				requestVersion: 0
			}
		},
		onShow() {
			if (uni.getStorageSync('current')) {
				this.current = uni.getStorageSync('current')
				this.state = 3


				setTimeout(() => {
					uni.removeStorageSync('current')
				}, 800)
			}



			this.pages = 1 //每次进入的时候初始化数据请求的页数
			if (!uni.getStorageSync('token')) {
				uni.showToast({
					title: '请登录后操作',
					icon: 'none',
					duration: 2000
				});
				return false
			} else {
				this.OrderListApi()
			}
		},
		onReachBottom() {
			if (this.isLoading || !this.hasMore) return
			const nextPage = this.pages + 1
			const version = this.requestVersion
			this.isLoading = true
			// 开始发起请求
			let data = {
				state: this.state, //1待支付  2已支付 3进行中 4已完成 5已取消
				type: this.type, //1租车 2买车 3维修服务
				page: nextPage
			}
			uni.request({
				url: this.YM + '/api/order/list',
				method: 'POST',
				header: {
					'token': uni.getStorageSync('token'),
				},
				data,
				success: (res) => {
					if (version != this.requestVersion) return
					this.isLoading = false
					if (res.data.code != 1 || !res.data.data) {
						uni.showToast({ title: res.data.msg || '订单加载失败', icon: 'none' })
						return
					}
					const rows = Array.isArray(res.data.data.data) ? res.data.data.data : []
					this.pages = nextPage
					this.OrderData = this.OrderData.concat(rows)
					this.hasMore = Number(res.data.data.current_page) < Number(res.data.data.last_page)

					// for (var i = 0; i < this.OrderData.length; i++) {
					// 	this.OrderData[i].price = Number(this.OrderData[i].price) + Number(this.OrderData[
					// 		i].cash_price)
					// }


					// this.OrderData.take_back_time
				},
				fail: () => {
					if (version != this.requestVersion) return
					this.isLoading = false
					uni.showToast({ title: '订单加载失败，请稍后重试', icon: 'none' })
				}
			});
		},
		methods: {
			payable(item) {
				return Math.max(0, Number(item.price || 0) - Number(item.discounts_price || 0))
			},
			cancel() {
				this.show = false
			},
			ClickSH() {
				uni.showToast({
					title: '正在审核中，请等待...',
					icon: 'none',
					duration: 2000
				});
			},
			hcconfirm() {
				let data = {
					order_number: this.order_number,
				}
				uni.request({
					url: this.YM + '/api/order/backCar',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token'),
					},
					data,
					success: res => {
						console.log(res, '确定换车');
						if (res.data.code == 1) {
							this.current = 4
							this.state = 4
							this.show = false
							this.OrderListApi()
						} else {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 2000
							});
							return false
						}
					}
				})
			},
			//去还车
			ClickHC(item) {
				this.show = true
				uni.setStorageSync('order_number', item.number)
				this.order_number = item.number
				// uni.navigateTo({
				// 	url:'/pages/OrderChild/OrderHuanChe'
				// })
			},
			//查看详情
			ClickCKXQ(item) {
				console.log(item, '查看详情');
				uni.setStorageSync('order_number', item.number)
				uni.navigateTo({
					url: '/pages/OrderChild/OrderXQ'
				})
			},
			// 订单列表
			OrderListApi() {
				const version = ++this.requestVersion
				this.pages = 1
				this.isLoading = true
				this.hasMore = true
				let data = {
					state: this.state, //1待支付  2已支付 3进行中 4已完成 5已取消
					type: this.type, //1租车 2买车 3维修服务
					page: this.pages
				}
				uni.request({
					url: this.YM + '/api/order/list',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token'),
					},
					data,
					success: res => {
						if (version != this.requestVersion) return
						this.isLoading = false
						if (res.data.code != 1 || !res.data.data) {
							this.OrderData = []
							uni.showToast({ title: res.data.msg || '订单加载失败', icon: 'none' })
							return
						}
						this.OrderData = Array.isArray(res.data.data.data) ? res.data.data.data : []
						this.hasMore = Number(res.data.data.current_page) < Number(res.data.data.last_page)

						// for (var i = 0; i < this.OrderData.length; i++) {
						// 	this.OrderData[i].price = Number(this.OrderData[i].price) + Number(this.OrderData[
						// 		i].cash_price)
						// }
					},
					fail: () => {
						if (version != this.requestVersion) return
						this.isLoading = false
						this.OrderData = []
						uni.showToast({ title: '订单加载失败，请稍后重试', icon: 'none' })
					}
				})
			},
			//跳转支付
			ClickPlay(item) {
				console.log(item, '去支付');
				// 1租车 2买车 3维修服务
				if (item.type == 1 && item.lease_type == 4) {
					uni.setStorageSync('PlayNum', '1')
					uni.setStorageSync('PlayPrice', this.payable(item))
					uni.setStorageSync('order_number', item.number)
				} else if (item.type == 1) {
					uni.setStorageSync('PlayNum', '1')
					// let Price=item.price + Number(item.cash_price)//修改前
					// let Price=Number(item.lease_price) + Number(item.cash_price)//修改后
					let Price = this.payable(item)
					uni.setStorageSync('PlayPrice', Price)
					uni.setStorageSync('order_number', item.number)
				} else if (item.type == 2) {
					uni.setStorageSync('PlayNum', '2')
					// let Price=item.price + Number(item.cash_price)//修改前
					// let Price=Number(item.lease_price) + Number(item.cash_price)//修改后
					let Price = this.payable(item)
					uni.setStorageSync('PlayPrice', Price)
					uni.setStorageSync('order_number', item.number)
				} else if (item.type == 3) {
					uni.setStorageSync('PlayNum', '3')
					// let Price=item.price + Number(item.cash_price)//修改前
					// let Price=Number(item.lease_price) + Number(item.cash_price)//修改后
					let Price = this.payable(item)
					uni.setStorageSync('PlayPrice', Price)
					uni.setStorageSync('order_number', item.number)
				}


				uni.navigateTo({
					url: '/pages/OrderChild/OrderPlay'
				})
			},
			// Tab标签
			TabClick(tab) {
				this.pages = 1 //每次进入的时候初始化数据请求的页数
				for (var i = 0; i < this.list2.length; i++) {
					this.list2[i].isS = false
				}
				tab.isS = true
				console.log(tab, 'tab');

				if (tab.title == '全部') {
					this.type = 0
				} else if (tab.title == '租车订单') {
					this.type = 1
				} else if (tab.title == '购车订单') {
					this.type = 2
				} else if (tab.title == '维修订单') {
					this.type = 3
				}
				if (!uni.getStorageSync('token')) {
					uni.showToast({
						title: '请登录后操作',
						icon: 'none',
						duration: 2000
					});
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/login/login'
						})
					}, 2000)
					return false
				} else {
					this.OrderListApi()
				}
			},
			onShareAppMessage(res) {
				return {
					title: '安鑫出行',
					path: '/pages/index/order',
					imageUrl: '../../static/FXimgaes.png',
				};
			},
			//分享到朋友圈
			onShareTimeline(res) {
				return {
					title: '安鑫出行',
					path: '/pages/index/order',
					imageUrl: '../../static/FXimgaes.png',
				};
			},
			// tabs
			click(item) {
				this.pages = 1 //每次进入的时候初始化数据请求的页数
				console.log('item', item);
				this.current = item.index
				// state:0,//1待支付  2已支付 3进行中 4已完成 5已取消
				if (item.name == '全部') {
					this.state = 0
				} else if (item.name == '待支付') {
					this.state = 1
				} else if (item.name == '已支付') {
					this.state = 2
				} else if (item.name == '进行中') {
					this.state = 3
				} else if (item.name == '还车中') {
					this.state = 4
				} else if (item.name == '已完成') {
					this.state = 5
				} else if (item.name == '已取消') {
					this.state = 6
				}
				this.OrderListApi()
			}
		}
	}
</script>

<style>
	.order {
		width: 100vw;
		min-height: 100vh;
		/* background-color: skyblue; */

	}

	/* tabs */
	.order_tabs {
		width: 100%;
		height: 80rpx;
		background-color: #ffffff;
	}

	/* Tab */
	.order_Tab {
		width: 696rpx;
		height: 85rpx;
		/* background-color: aqua; */
		margin: 15rpx auto 0;
		display: flex;
		align-items: center;
	}

	.order_Tab_text,
	.order_Tab_textA {
		width: 120rpx;
		height: 52rpx;
		line-height: 52rpx;
		border-radius: 4rpx;
		background-color: rgba(237, 153, 57, 1);
		color: rgba(255, 255, 255, 1);
		font-size: 28rpx;
		text-align: center;
		font-family: Arial;
		padding-left: 10rpx;
		padding-right: 10rpx;
		margin-right: 15rpx;
	}

	.order_Tab_textA {
		background-color: rgba(236, 236, 236, 1);
		color: rgba(105, 105, 105, 1);
	}

	/* 列表 */
	.order_list_box {
		width: 696rpx;
		/* height: 900rpx; */
		/* background-color: salmon; */
		margin: 15rpx auto 0;
		padding-bottom: 40rpx;
	}

	.order_list {
		width: 100%;
		/* height: 440rpx; */
		background-color: #FFFFFF;
		border-radius: 20rpx;
		/* border-bottom: 20rpx; */
		display: flex;
		align-items: center;
		justify-content: center;
		flex-wrap: wrap;
		margin-bottom: 20rpx;
		padding-bottom: 20rpx;
		padding-top: 10rpx;
	}

	.order_list_top {
		width: 648rpx;
		height: 80rpx;
		border-bottom: 1rpx solid #BBBBBB;
		/* background-color: palegreen; */
		display: flex;
		align-items: center;
		justify-content: space-between;
		color: rgba(255, 0, 1, 1);
		font-size: 28rpx;
		font-weight: 700;
	}

	.order_list_top_left {
		width: 50%;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: flex-start;
		color: rgba(0, 0, 0, 1.0);
		font-size: 26rpx;
		font-weight: 600;
	}

	.order_list_top_left_icon {
		width: 28rpx;
		height: 28rpx;
		/* background-color: green; */
		margin-right: 10rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.order_list_bto {
		width: 648rpx;
		height: 184rpx;
		/* background-color: aqua; */
		display: flex;
		align-items: center;
	}

	.order_list_bto_left {
		width: 196rpx;
		height: 144rpx;
		border-radius: 8rpx;
		overflow: hidden;
		/* background-color: green; */
		position: relative;
	}

	.order_list_bto_left_image {
		width: 100%;
		height: 100%;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 1;
	}

	.order_list_bto_left_jiao {
		min-width: 60rpx;
		height: 32rpx;
		line-height: 32rpx;
		border-radius: 8rpx 0px 8rpx 0px;
		background-color: rgba(237, 153, 57, 0.87);
		color: rgba(255, 255, 255, 1);
		font-size: 22rpx;
		text-align: center;
		font-family: Arial;
		position: absolute;
		left: 0;
		top: 0;
		z-index: 2;
	}

	.order_list_bto_right {
		width: 430rpx;
		height: 144rpx;
		/* background-color: yellow; */
		margin-left: 15rpx;
	}

	.order_list_bto_right1 {
		width: 100%;
		height: 40rpx;
		color: rgba(0, 0, 0, 1.0);
		font-size: 28rpx;
	}

	.order_list_bto_right2 {
		width: 100%;
		height: 35rpx;
		color: rgba(133, 136, 150, 1);
		font-size: 26rpx;
		margin-top: 15rpx;
	}

	.order_list_bto_right3 {
		width: 100%;
		height: 30rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		color: rgba(133, 136, 150, 1);
		font-size: 26rpx;
		margin-top: 8rpx;
	}

	.order_list_btn {
		width: 648rpx;
		height: 75rpx;
		/* background-color: violet; */
		display: flex;
		align-items: center;
		justify-content: space-between;


	}

	.order_list_btn_left,
	.order_list_btn_right {
		width: 50%;
		height: 100%;
		/* background-color: antiquewhite; */
		display: flex;
		align-content: center;
		color: rgba(0, 0, 0, 1.0);
		font-size: 36rpx;
		font-weight: bold;
		align-items: center;
	}

	.order_list_btn_right {
		/* background-color: yellow; */
		justify-content: flex-end;
	}

	.order_list_btn_right_btn1,
	.order_list_btn_right_btn2,
	.order_list_btn_right_btn21 {
		width: 152rpx;
		height: 64rpx;
		line-height: 64rpx;
		border-radius: 62rpx;
		background-color: rgba(255, 255, 255, 1);
		color: rgba(51, 51, 51, 1);
		font-size: 26rpx;
		text-align: center;
		font-family: Arial;
		border: 2rpx solid rgba(227, 227, 227, 1);
	}

	.order_list_btn_right_btn2,
	.order_list_btn_right_btn21 {
		background-color: rgba(226, 78, 17, 1);
		color: rgba(255, 255, 255, 1);
		margin-left: 15rpx;
		border: 0;
	}

	.order_list_btn_right_btn21 {
		width: 200rpx;
	}

	.or {
		background: #ED9939;
		color: #FFFFFF;
	}

	.order_list_diz {
		width: 648rpx;
		/* height: 70rpx; */
		/* line-height: 70rpx; */
		color: #858896;
		font-size: 28rpx;
		display: flex;
		align-items: center;
		margin: 10rpx auto 0;
	}
</style>
