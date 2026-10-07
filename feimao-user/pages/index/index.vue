<template>
	<view class="Index">
		<!-- 轮播 -->
		<view class="Index_lunb">
			<u-swiper :list="list1" height='270' :circular='true'></u-swiper>
		</view>

		<!-- 下部区域 -->
		<view class="Index_Bto">
			<view class="Index_Bto_bigbox">

				<!-- 搜索区域 -->
				<!-- 	<view class="Index_Bto_bigbox_sos">
					<view class="Index_Bto_bigbox_sos_left">
						<u--input placeholder="搜索车型" border="false" v-model="value1" :clearable='true'></u--input>
					</view>

					<view class="Index_Bto_bigbox_sos_right" @click="ClickSos">
						<view class="Index_Bto_bigbox_sos_right_iocn">
							<image src="../../static/sos.png" class="imageBFB"></image>
						</view>
					</view>
				</view> -->

				<!-- 扫码租车 在线购车 -->
				<view class="Index_Bto_che">
					<view class="Index_Bto_che_left">
						<!-- 背景 -->
						<view class="Index_Bto_che_left_image">
							<image src="../../static/SaoMAc.png" class="imageBFB"></image>
						</view>
						<view class="Index_Bto_che_left_box" @click="ClickSYS">
							<view class="Index_Bto_che_left_box_L">
								<view class="Index_Bto_che_left_box_L_title">扫码租车</view>
								<view class="Index_Bto_che_left_box_L_text">扫码直接下单</view>
							</view>
							<view class="Index_Bto_che_left_box_R"></view>
						</view>

					</view>
					<view class="Index_Bto_che_right">
						<view class="Index_Bto_che_left_image">
							<image src="../../static/ZaiXianc.png" class="imageBFB"></image>
						</view>
						<view class="Index_Bto_che_left_box" @click="HuanChe">
							<view class="Index_Bto_che_left_box_L">
								<view class="Index_Bto_che_left_box_L_title">立即还车</view>
								<view class="Index_Bto_che_left_box_L_text">便捷快速还车</view>
							</view>
							<view class="Index_Bto_che_left_box_R"></view>
						</view>
					</view>
				</view>

				<!-- 地图找车块 -->
				<view class="Index_Bto_Gn">
					<view class="Index_Bto_Gn_box" @click="ClickMapChe">
						<view class="Index_Bto_Gn_box_src">
							<image src="../../static/D.png" class="imageBFB"></image>
						</view>
						<view class="Index_Bto_Gn_box_text">地图找车</view>
					</view>

					<view class="Index_Bto_Gn_box" @click="ClickKsZC">
						<view class="Index_Bto_Gn_box_src">
							<image src="../../static/K.png" class="imageBFB"></image>
						</view>
						<view class="Index_Bto_Gn_box_text">快速租车</view>
					</view>

					<view class="Index_Bto_Gn_box" @click="ClickWXFW(1)">
						<view class="Index_Bto_Gn_box_src">
							<image src="../../static/W.png" class="imageBFB"></image>
						</view>
						<view class="Index_Bto_Gn_box_text">维修服务</view>
					</view>

					<view class="Index_Bto_Gn_box" @click="ClickWXFW(2)">
						<view class="Index_Bto_Gn_box_src" style="width: 80rpx;height: 80rpx;">
							<image src="../../static/S.png" class="imageBFB"></image>
						</view>
						<view class="Index_Bto_Gn_box_text">商务合作</view>
					</view>
				</view>

				<!-- 标签栏 -->
				<view class="Index_Bto_Biao">
					<view class="Index_Bto_Biao_box">
						<view class="Index_Bto_Biao_box_left">
							<image src="../../static/cx1.png" class="imageBFB"></image>
						</view>
						<text>车型齐全</text>
					</view>

					<view class="Index_Bto_Biao_box">
						<view class="Index_Bto_Biao_box_left">
							<image src="../../static/zb1.png" class="imageBFB"></image>
						</view>
						<text>质量保障</text>
					</view>

					<view class="Index_Bto_Biao_box">
						<view class="Index_Bto_Biao_box_left">
							<image src="../../static/sh1.png" class="imageBFB"></image>
						</view>
						<text>送货上门</text>
					</view>
				</view>

				<!-- 门店列表 -->
				<view class="Index_Bto_Title">门店列表</view>
				<view class="Index_Bto_list">

					<view class="Index_Bto_list_for" v-for="(item,index) in IndexData" :key="index"
						@click="ClickList(item)">
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
								<view class="Index_Bto_list_for_top_right3">
									<view class="Index_Bto_list_for_top_right3_L">{{item.address}}</view>
									<view class="Index_Bto_list_for_top_right3_R">{{item.distance}}</view>
								</view>
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
								<view class="Index_Bto_list_for_bto_right_kuai"
									v-if="item.pick_type==2">送车上门</view>
							</view>
						</view>
					</view>
					<u-empty mode="list" icon="https://cdn.uviewui.com/uview/empty/car.png" v-if="IndexData==''">
					</u-empty>

				</view>

			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				value1: '', //搜索
				YM: getApp().globalData.apiBase,
				list1: [],
				IndexData: [],
				clickTimer:null
			}
		},
		// onLoad() {
		// 	// 纬度
		// 	// uni.setStorageSync('lng', '113.66977')
		// 	// 经度
		// 	// uni.setStorageSync('lat', '34.78675')
		// },
		onLoad(options) {
			console.log(options.scene, '接收的参数');
			if(options.scene===undefined){
				uni.setStorageSync('source_type',1)
				uni.removeStorageSync('source_id')
				console.log('普通下单');
			}
			if(options.scene){
				const parts = options.scene.split('-');
				if (parts.length === 2) {
					uni.setStorageSync('source_id',parts[0])
					uni.setStorageSync('source_type',parts[1])
					
					
					this.backPart = parts[1];
					
					uni.setStorageSync('source_type',this.backPart)
					
					
					
					console.log("后面的内容:", this.backPart);
				}
			}
		},
		onShow() {
			let _this=this
			uni.getLocation({
				type: "wgs84", //返回可以用于 wx.openLocation 的经纬度
				success(res) {
					
					// console.log(res, '获取到的经纬度');
					const lat = res.latitude;
					const lng = res.longitude;
					uni.setStorageSync('lat', lat)
					uni.setStorageSync('lng', lng)
					
					_this.MenDInaApi() //门店列表
				},
				fail: (err) => {
					console.log(err,'定位失败')
				}
			});
		},
		created() {
			this.Lunb() //轮播图
		},
		methods: {
			HuanChe(){
				uni.setStorageSync('current',3)
				uni.switchTab({
					url:'/pages/index/order'
				})
			},
			onShareAppMessage(res) {
				return {
					title: '安鑫出行',
					path: '/pages/index/index',
					imageUrl: '../../static/FXimgaes.png',
				};
			},
			//分享到朋友圈
			onShareTimeline(res) {
				return {
					title: '安鑫出行',
					path: '/pages/index/index',
					imageUrl: '../../static/FXimgaes.png',
				};
			},
			//门店列表
			MenDInaApi() {
				let data={
					lng:uni.getStorageSync('lng'),
					lat:uni.getStorageSync('lat'),
					page:1,
					list_rows:999999999
				}
				uni.request({
					url: this.YM + '/api/index/merchantList',
					method: 'POST',
					data,
					success: res => {
						this.IndexData = res.data.data.data
						// console.log(this.IndexData, '门店列表');
					}
				})
			},
			// 轮播图
			Lunb() {
				uni.request({
					url: this.YM + '/api/index/bannerList',
					method: 'POST',
					success: res => {
						// console.log(res.data.data, '轮播图');
						for (var i = 0; i < res.data.data.length; i++) {
							this.list1.push(this.YM + res.data.data[i].image)
						}
						// console.log(this.list1, 'this.list1');
					}
				})
			},
			//点击了门店列表进入详情
			ClickList(item) {
				// console.log(item, '点击的门店');
				uni.setStorageSync('merchant_id', item.merchant_id)
				uni.removeStorageSync('current')
				uni.navigateTo({
					url: '/pages/IndexChild/XQ'
				})
			},
			//点击在线购车
			ClickZaiXian() {
				uni.navigateTo({
					url: '/pages/IndexChild/ZaiXianGC'
				})
			},
			//点击了快速租车
			ClickKsZC() {
				// console.log('快速租车');
				uni.navigateTo({
					url: '/pages/IndexChild/ZuChe'
				})
			},
			//点击维修服务
			ClickWXFW(type) {
				uni.navigateTo({
					url: '/pages/IndexChild/WeiXiuFW?type=' + type
				})
			},
			//签到
			ClickQD() {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
				if (!uni.getStorageSync('token')) {
					uni.showToast({
						title: '请登录后操作',
						icon: 'none',
						duration: 2000
					});
					uni.setStorageSync('LoginIF', '02')
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/login/login'
						})
					}, 2000)
					return false
				} else {
					// console.log('签到');
					// uni.navigateTo({
					// 	url: '/pages/IndexChild/QianDao'
					// })
				}
				}, 500); // 设置延迟时间，单位为毫秒
			},
			//点击了扫码租车
			ClickSYS() {
				uni.scanCode({
					success: function(res) {
						// console.log('条码类型：' + res.scanType);
						// console.log('条码内容：' + res.result);
					}
				});
			},
			//点击了地图找车
			ClickMapChe() {
				uni.navigateTo({
					url: '/pages/IndexChild/MapChe'
				})
			},
			//点击了搜索
			ClickSos() {
				// console.log('输入的搜索值：', this.value1);
			}
		}
	}
</script>

<style>
	.Index {
		width: 100vw;
		min-height: 100vh;
		background-color: #ccc;
	}

	/* 轮播 */
	.Index_lunb {
		width: 100%;
		/* height: 556rpx; */
		background-color: #ccc;
	}

	/* 下部区域 */
	.Index_Bto {
		width: 100%;
		/* height: 500rpx; */
		/* background: linear-gradient(182.86deg, rgba(255,255,255,1) 2.38%,rgba(248,249,252,1) 42.48%); */
		position: relative;
	}

	.Index_Bto_bigbox {
		width: 100%;
		min-height: calc(100vh - 446rpx);
		background: linear-gradient(182.86deg, rgba(255, 255, 255, 1) 2.38%, rgba(248, 249, 252, 1) 42.48%);
		position: absolute;
		top: -20rpx;
		left: 0;
		border-radius: 40rpx 40rpx 0px 0px;
		float: left;
	}

	/* 搜索区域 */
	.Index_Bto_bigbox_sos {
		width: 700rpx;
		height: 84rpx;
		border-radius: 72rpx;
		background-color: rgba(255, 255, 255, 1);
		border: 2rpx solid rgba(255, 220, 180, 1);
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.Index_Bto_bigbox_sos_left {
		width: 535rpx;
		height: 64rpx;
		/* background-color: salmon; */
		margin-left: 30rpx;
		display: flex;
		align-items: center;
	}

	.Index_Bto_bigbox_sos_right {
		width: 104rpx;
		height: 64rpx;
		border-radius: 60rpx;
		background-color: rgba(237, 153, 57, 1);
		text-align: center;
		margin-right: 12rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Index_Bto_bigbox_sos_right_iocn {
		width: 36rpx;
		height: 36rpx;
		/* background-color: green; */
		text-align: center;
		line-height: 36rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	/* 扫码租车区 */
	.Index_Bto_che {
		width: 700rpx;
		height: 188rpx;
		/* background-color: red; */
		margin: 40rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.Index_Bto_che_left,
	.Index_Bto_che_right {
		width: 336rpx;
		height: 188rpx;
		border-radius: 28rpx;
		/* background-color: green; */
		overflow: hidden;
		position: relative;
	}

	.Index_Bto_che_right {
		/* background-color: cyan; */
	}

	.Index_Bto_che_left_image {
		width: 100%;
		height: 100%;
		/* background-color: violet; */
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 1;
	}

	.Index_Bto_che_left_box {
		width: 100%;
		height: 100%;
		/* background-color: aqua; */
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 2;
		display: flex;
		align-items: flex-end;
		justify-content: center
	}

	.Index_Bto_che_left_box_L {
		width: 180rpx;
		height: 100%;
		/* background-color: red; */
		margin-left: 30rpx;
	}

	.Index_Bto_che_left_box_R {
		width: 138rpx;
		height: 162rpx;
		border-radius: 0px 0px 28rpx 0px;
		/* background-color: green; */
	}

	.Index_Bto_che_left_box_L_title {
		width: 100%;
		height: 45rpx;
		text-align: left;
		color: rgba(41, 30, 18, 1);
		font-size: 32rpx;
		font-weight: bold;
		margin-top: 30rpx;
	}

	.Index_Bto_che_left_box_L_text {
		width: 100%;
		height: 30rpx;
		text-align: left;
		color: rgba(51, 51, 51, 1);
		font-size: 24rpx;
	}

	/* 地图找车块 */
	.Index_Bto_Gn {
		width: 700rpx;
		height: 160rpx;
		/* background-color: green; */
		margin: 30rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-around;
	}

	.Index_Bto_Gn_box {
		width: 110rpx;
		height: 100%;
		/* background-color: yellow; */
		display: flex;
		align-items: center;
		justify-content: center;
		flex-wrap: wrap;
	}

	.Index_Bto_Gn_box_src {
		width: 95rpx;
		height: 95rpx;
		/* background-color: green; */
	}

	.Index_Bto_Gn_box_text {
		width: 100%;
		height: 45rpx;
		text-align: center;
		color: rgba(0, 0, 0, 1.0);
		font-size: 26rpx;
		margin-top: 10rpx;
		font-weight: 700;
	}

	/* 标签栏 */
	.Index_Bto_Biao {
		width: 700rpx;
		height: 68rpx;
		border-radius: 12rpx;
		background-color: #F2F2F2;
		display: flex;
		align-items: center;
		margin: 20rpx auto 0;
	}

	.Index_Bto_Biao_box {
		flex: 3;
		height: 100%;
		/* background-color: yellowgreen; */
		display: flex;
		align-items: center;
		justify-content: center;
		color: rgba(154, 154, 154, 1);
		font-size: 24rpx;
	}

	.Index_Bto_Biao_box_left {
		width: 28rpx;
		height: 28rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 10rpx;
	}

	/* 门店列表 */
	.Index_Bto_Title {
		width: 700rpx;
		height: 70rpx;
		line-height: 70rpx;
		font-weight: bold;
		margin: 35rpx auto 0;
		color: rgba(0, 0, 0, 1.0);
		font-size: 32rpx;
	}

	.Index_Bto_list {
		width: 700rpx;
		/* height: 700rpx; */
		/* background-color: violet; */
		margin: 30rpx auto 0;
		padding-bottom: 30rpx;
	}

	.Index_Bto_list_for {
		width: 100%;
		height: 288rpx;
		border-radius: 20rpx;
		background-color: rgba(255, 255, 255, 1);
		text-align: center;
		box-shadow: 0px 4rpx 12rpx 0px rgba(0, 0, 0, 0.06);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-wrap: wrap;
		margin-bottom: 20rpx;
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
		display: flex;
		align-items: center;
		justify-content: space-between;
		
	}
	
	.Index_Bto_list_for_top_right3_L{
		width: 70%;
		height: 100%;
		white-space: nowrap;
		/* 禁止换行 */
		overflow: hidden;
		/* 溢出部分隐藏 */
		text-overflow: ellipsis;
		/* 超出部分显示省略号 */
	}
	.Index_Bto_list_for_top_right3_R{
		min-width: 78rpx;
		height: 40rpx;
		line-height: 40rpx;
		border-radius: 6rpx;
		background-color: rgba(255, 245, 230, 1);
		text-align: center;
		color: #ED9939 ;
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
		height: 45rpx;
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
</style>
