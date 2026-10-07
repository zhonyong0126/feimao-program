<template>
	<view class="XQ">

		<!-- 顶部 -->
		<view class="XQ_HD">
			<view class="XQ_HD_lunb">
				<u-swiper :list="list1" :height='260'></u-swiper>
			</view>

			<view class="XQ_HD_icon" @click="ClickF">
				<image src="../../static/BaiZ.png" class="imageBFB"></image>
			</view>
		</view>

		<!-- 下部 -->
		<view class="XQ_XB">
			<view class="XQ_XB_box">

				<view class="XQ_XB_box_xx">
					<view class="XQ_XB_box_xx_title">{{XiangQ.name}}</view>
					<view class="XQ_XB_box_xx_kuai">
						<view class="XQ_XB_box_xx_kuai_box" v-if="XiangQ.pick_type==1">到店取车</view>
						<view class="XQ_XB_box_xx_kuai_box" v-if="XiangQ.pick_type==2">到店取车</view>
						<view class="XQ_XB_box_xx_kuai_box" v-if="XiangQ.pick_type==2">送车上门</view>
					</view>

					<view class="XQ_XB_box_z">
						<view class="XQ_XB_box_z_box">
							<view class="XQ_XB_box_z_box_L">
								<view class="XQ_XB_box_z_box_L_1">营业时间: {{XiangQ.business}}</view>
								<view class="XQ_XB_box_z_box_L_2">{{XiangQ.address}}</view>
							</view>
							<view class="XQ_XB_box_z_box_R">
								<view class="XQ_XB_box_z_box_R_map" @click="ClickMap">
									<view class="XQ_XB_box_z_box_R_map_icon">
										<image src="../../static/map.png" class="imageBFB"></image>
									</view>
									<text>地图</text>
								</view>
								<view class="XQ_XB_box_z_box_R_map" @click="ClickPhone">
									<view class="XQ_XB_box_z_box_R_map_icon">
										<image src="../../static/phone.png" class="imageBFB"></image>
									</view>
									<text>电话</text>
								</view>
							</view>
						</view>
					</view>
				</view>
				<view class="XQ_XB_box_xx_shu"></view>

				<!-- tabs -->
				<view class="XQ_XB_box_xx_tabs">
					<u-tabs :list="listtabs" @click="click" :current='current' lineColor='#ED9939' :activeStyle="{
        color: '#303133',
        fontWeight: 'bold',
        transform: 'scale(1.05)'
    }" lineHeight='4' itemStyle="height: 40px;width:70px;"></u-tabs>
				</view>

				<!-- TAB -->
				<view class="XQ_XB_box_xx_TAB" v-if="current==0">
					<view class="XQ_XB_box_xx_TAB_box" v-for="(item, index) in TabList" :key="index"
						:class="{'XQ_XB_box_xx_TAB_box1': currentIndex !== index}" @click="changeTab(index,item)">
						{{item.name}}
					</view>
				</view>

				<!-- 列表 -->
				<view class="XQ_XB_box_xx_list" v-if="current===0">

					<!-- 租车列表 -->
					<view class="BtoList_box" @click="ClickGooDs(item)" v-for="(item,index) in ZuCheData" :key="index">
						<view class="BtoList_box_L">
							<image :src="YM + item.index_image" class="imageBFB"></image>
						</view>
						<view class="BtoList_box_R">
							<view class="BtoList_box_R_oen">{{item.name}}</view>
							<!-- <view class="BtoList_box_R_two">可租车辆<text style="color: red;">18</text>台</view> -->
							<view class="BtoList_box_R_therr">
								<view class="BtoList_box_R_therr_l">
									<text style="font-size: 28rpx;font-weight: 600;">￥</text>
									<text>{{item.min_price}}</text>
									<text style="font-size: 24rpx;font-weight: 600;">起</text>
								</view>
								<view class="BtoList_box_R_therr_r" @click="ClickGooDs(item)">去租赁</view>
							</view>
						</view>
					</view>

					<u-empty mode="list" icon="https://cdn.uviewui.com/uview/empty/car.png" v-if="ZuCheData==''">
					</u-empty>

				</view>

				<!-- 购车列表 -->
				<view class="XQ_XB_box_xx_list1" v-if="current===1">

					<view class="GouCList" @click="ClickGCXQ(item)" v-for="(item,index) in GouCheData" :key="index">
						<view class="GouCList_image">
							<image :src="YM + item.index_image" class="imageBFB"></image>
						</view>
						<view class="GouCList_text">
							<view class="GouCList_text_title">{{item.name}}</view>
							<view class="GouCList_text_Mp">
								<view class="GouCList_text_Mp_Pirce">
									<text style="font-size: 28rpx;">￥</text>
									<text>{{item.price}}</text>
								</view>
								<view class="GouCList_text_Mp_Kc"></view>
							</view>
						</view>
					</view>
					<view v-if="GouCheData==''" style="width: 100%;height: 450rpx;">
						<u-empty mode="list" icon="https://cdn.uviewui.com/uview/empty/car.png">
						</u-empty>
					</view>
				</view>


				<!-- 维修服务 -->
				<view class="XQ_XB_box_xx_list" v-if="current===2">
					<view class="BtoList_box" v-for="(item,index) in ListAr" :key="index">
						<view class="BtoList_box_L">
							<!-- <image :src="YM + item.image" class="imageBFB" mode="aspectFit"></image> -->
							<image :src="YM + item.image" class="imageBFB"></image>

						</view>
						<view class="BtoList_box_R">
							<view class="BtoList_box_R_oen">{{item.name}}</view>
							<view class="BtoList_box_R_two">{{item.introductory}}</view>
							<view class="BtoList_box_R_therr">
								<view class="BtoList_box_R_therr_l">
									<text style="font-size: 28rpx;font-weight: 600;">￥</text>
									<text>{{item.price}}</text>
									<text style="font-size: 24rpx;font-weight: 600;">起</text>
								</view>
								<view class="BtoList_box_R_therr_r" @click="ClickWeixiu(item)">去下单</view>
							</view>
						</view>
					</view>

					<view v-if="ListAr==''" style="width: 100%;height: 450rpx;">
						<u-empty mode="list" icon="https://cdn.uviewui.com/uview/empty/car.png">
						</u-empty>
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
				current: 0,
				currentIndex: 0, // 默认选中第一个
				list1: [],
				// tabs
				listtabs: [{
						name: '租车',
					},
					// {
					// 	name: '购车',
					// }, {
					// 	name: '维修服务'
					// },
				],
				// TAB
				TabList: [],
				XiangQ: {
					name: '',
					business: '',
					address: ''
				},
				ListAr: '', //门店维修数据依赖
				ZuCheData: '',
				GouCheData: '',
				brand: 0,
				clickTimer: null,
				backPart:''
			}
		},
		onShow() {
			this.MenXQ() //门店详情

			this.MenDianWeiXiu() //门店维修服务

			this.current = uni.getStorageSync('current')
			this.current = Number(this.current)

			console.log(this.current, 'this.current');
		},
		beforeDestroy() {
			// uni.removeStorageSync('current')
		},
		onLoad(options) {
			// console.log(options.scene, '接收的参数');
			// if(options.scene===undefined){
			// 	uni.setStorageSync('source_type',1)
			// 	uni.removeStorageSync('source_id')
			// 	console.log('普通下单');
			// }

			const parts = options.scene.split('-');
			if (parts.length === 2) {
				console.log(parts,'parts');
				uni.setStorageSync('source_id',parts[0])
				uni.setStorageSync('source_type',parts[1])
				
				
				uni.setStorageSync('merchant_id',parts[0])
				
				
				// this.backPart = parts[1];
				
				// uni.setStorageSync('source_type',this.backPart)
				
				
				
				console.log("后面的内容:", this.backPart);
			}

		},
		methods: {
			// 门店购车列表
			MenDGouCheApi() {
				let data = {
					merchant_id: this.XiangQ.merchant_id,
					page: 1,
				}
				uni.request({
					url: this.YM + '/api/merchant/buyCarList',
					method: 'POST',
					data,
					success: res => {
						this.GouCheData = res.data.data.data
						console.log(res.data.data.data, '门店购车列表');
					},
				})
			},
			// 门店租车车辆列表
			MenClistApi() {
				let data = {
					merchant_id: this.XiangQ.merchant_id,
					page: 1,
					brand_id: this.brand
				}
				uni.request({
					url: this.YM + '/api/merchant/leaseCarList',
					method: 'POST',
					data,
					success: res => {
						this.ZuCheData = res.data.data.data
						console.log(res.data.data.data, '门店车辆列表');
					},
				})
			},
			// 门店品牌列表
			MenDianListApi() {
				let data = {
					merchant_id: this.XiangQ.merchant_id
				}
				uni.request({
					url: this.YM + '/api/merchant/brandList',
					method: 'POST',
					data,
					success: res => {
						// console.log(res.data.data,'门店品牌列表');
						this.TabList = res.data.data
						let Treacn = {
							name: '全部'
						}
						this.TabList.unshift(Treacn)
						// console.log(this.TabList,'this.TabList');
					},
				})
			},
			//点击了去下单
			ClickWeixiu(item) {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
					uni.setStorageSync('BFC', '3A')
					uni.setStorageSync('weixiu_merchant_maintain_id', item.merchant_maintain_id)
					uni.setStorageSync('weixiudata', item)
					uni.navigateTo({
						url: '/pages/IndexChild/ZuCheXQ'
					})
				}, 500); // 设置延迟时间，单位为毫秒

			},

			// 门店维修服务
			MenDianWeiXiu() {
				let data = {
					merchant_id: uni.getStorageSync('merchant_id')
				}
				uni.request({
					url: this.YM + '/api/merchant/maintainList',
					method: 'POST',
					data,
					success: res => {
						this.ListAr = res.data.data.data

						// 遍历每个对象
						this.ListAr.forEach(obj => {
							// 将image属性按逗号分隔为数组
							let images = obj.image.split(',');

							// 将第一个元素重新赋值给image属性
							obj.image = images[0];

						});

						// console.log(this.ListAr,'门店维修');
					},
				})
			},
			//门店详情
			MenXQ() {
				let data = {
					merchant_id: uni.getStorageSync('merchant_id')
				}

				uni.request({
					url: this.YM + '/api/merchant/details',
					method: 'POST',
					data,
					success: res => {
						this.XiangQ = res.data.data;

						this.MenDianListApi() //门店品牌列表
						this.MenClistApi() //门店租车车辆列表
						this.MenDGouCheApi() //门店购车列表

						console.log(this.XiangQ, '门店详情');

						let images = this.XiangQ.image.split(',');
						console.log(images, 'images');

						Array.prototype.push.apply(this.list1, images.map(image => this.YM + image));

						console.log(this.list1, 'lun');


					},
				})
			},
			//点击列表进入租车详情 租车
			ClickGooDs(item) {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
					uni.setStorageSync('BFC', '1A')
					let Ar = {
						Type: this.XiangQ.pick_type,
					}
					console.log(item, 'item');

					uni.setStorageSync('GouCheData', Ar)
					uni.setStorageSync('merchat_car_id', item.merchant_car_id)
					uni.navigateTo({
						url: '/pages/IndexChild/ZuCheXQ'
					})
				}, 500); // 设置延迟时间，单位为毫秒
			},
			//点击购车进入详情
			ClickGCXQ(item) {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
					console.log(item, 'item');
					uni.setStorageSync('BFC', '2A')
					let Ar = {
						Type: this.XiangQ.pick_type,
					}
					uni.setStorageSync('GouCheData', Ar)
					uni.setStorageSync('merchat_car_id', item.merchat_car_id)
					uni.navigateTo({
						url: '/pages/IndexChild/ZuCheXQ'
					})
				}, 500); // 设置延迟时间，单位为毫秒



			},
			//点击购买服务进入详情
			ClickGMFW() {
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
					uni.setStorageSync('BFC', '3A')
					uni.navigateTo({
						url: '/pages/IndexChild/ZuCheXQ'
					})

				}, 500); // 设置延迟时间，单位为毫秒


			},
			//TAB
			changeTab(index, item) {
				this.currentIndex = index;
				console.log(item, 'item');

				if (item.name == '全部') {
					this.brand = 0
				} else {
					this.brand = item.brand_id
				}
				this.MenClistApi()
			},
			//点击了电话
			ClickPhone() {
				console.log('点击了电话');
				uni.makePhoneCall({
					phoneNumber: this.XiangQ.mobile //仅为示例
				});
			},
			//点击了地图
			ClickMap() {
				console.log('点击了地图');
				const All = `${this.XiangQ.address}`
				uni.openLocation({
					latitude: Number(this.XiangQ.lat), // 目标位置的纬度
					longitude: Number(this.XiangQ.lng), // 目标位置的经度
					name: this.XiangQ.name, // 目标位置的名称
					address: All, // 目标位置的地址
					scale: 18 // 地图缩放级别，范围1-28
				});
			},
			//点击返回上一层
			ClickF() {
				uni.navigateBack()
			},
			click(item) {
				console.log('item', item);
				this.current = item.index
				if (item.name == '租车') {
					uni.setStorageSync('current', 0)
				} else if (item.name == '购车') {
					uni.setStorageSync('current', 1)
				} else if (item.name == '维修服务') {
					uni.setStorageSync('current', 2)
				}

			}
		}
	}
</script>

<style>
	.XQ {
		width: 100vw;
		min-height: 100vh;
		/* background-color: pink; */
	}

	/* 顶部 */
	.XQ_HD {
		width: 100%;
		/* height: 516rpx; */
		/* background-color: #ccc; */
		position: relative;
	}

	.XQ_HD_lunb {
		width: 100%;
		height: 100%;
		/* background-color: green; */
	}

	.XQ_HD_icon {
		width: 35rpx;
		height: 42rpx;
		/* background-color: green; */
		position: absolute;
		top: 110rpx;
		left: 30rpx;
		z-index: 2;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	/* 下部 */
	.XQ_XB {
		width: 100%;
		min-height: calc(100vh - 516rpx);
		background-color: #FFFFFF;
		position: relative;
	}

	.XQ_XB_box {
		width: 100%;
		height: 100%;
		border-radius: 32rpx 32rpx 0rpx 0rpx;
		background-color: #FFFFFF;
		position: absolute;
		left: 0;
		top: -50rpx;
	}

	.XQ_XB_box_xx {
		width: 90%;
		height: 300rpx;
		/* background-color: sandybrown; */
		margin: 20rpx auto 0;
	}

	.XQ_XB_box_xx_title {
		width: 100%;
		height: 40rpx;
		line-height: 40rpx;
		font-size: 34rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
	}

	.XQ_XB_box_xx_kuai {
		width: 100%;
		height: 35rpx;
		display: flex;
		align-items: center;
		margin-top: 18rpx;
	}

	.XQ_XB_box_xx_kuai_box {
		width: 104rpx;
		height: 36rpx;
		background: #FFFFFF;
		border: 2rpx solid #D5D5D5;
		border-radius: 6rpx;
		font-size: 22rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #A1A1A1;
		margin-right: 15rpx;
		text-align: center;
		line-height: 36rpx;
	}

	.XQ_XB_box_z {
		width: 100%;
		height: 200rpx;
		/* background-color: pink; */
		float: left;
		display: flex;
		align-items: center;
		justify-content: flex-end;
	}

	.XQ_XB_box_z_box {
		width: 100%;
		height: 170rpx;
		/* background-color: cyan; */
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.XQ_XB_box_z_box_L {
		width: 400rpx;
		height: 100%;
		/* background-color: aquamarine; */
	}

	.XQ_XB_box_z_box_L_1 {
		width: 100%;
		height: 30rpx;
		line-height: 30rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		line-height: 50rpx;
		margin-top: 20rpx;
	}

	.XQ_XB_box_z_box_L_2 {
		width: 100%;
		height: 70rpx;
		margin-top: 26rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.XQ_XB_box_z_box_R {
		width: 200rpx;
		height: 100rpx;
		/* background-color: red; */
		display: flex;
		align-items: center;
		justify-content: space-around;
	}

	.XQ_XB_box_z_box_R_map {
		width: 54rpx;
		height: 100%;
		/* background-color: red; */
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #2A2E79;
	}

	.XQ_XB_box_z_box_R_map_icon {
		width: 54rpx;
		height: 72rpx;
		margin-bottom: 8rpx;
	}

	.XQ_XB_box_xx_shu {
		width: 100%;
		height: 20rpx;
		background-color: #F7F7F7;
		margin-top: 20rpx;
	}

	/* tabs */
	.XQ_XB_box_xx_tabs {
		width: 100%;
		height: 70rpx;
		/* background-color: yellow; */
		display: flex;
		align-items: center;
		justify-content: space-around;
	}

	/* TAB */
	.XQ_XB_box_xx_TAB {
		width: 100%;
		height: 70rpx;
		background-color: #FFFFFF;
		display: flex;
		align-items: center;
		padding-left: 30rpx;
	}

	.XQ_XB_box_xx_TAB_box,
	.XQ_XB_box_xx_TAB_box1 {
		min-width: 96rpx;
		height: 52rpx;
		background: #F9EFE5;
		border: 2rpx solid #FFB867;
		border-radius: 4rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #ED9939;
		padding-left: 8rpx;
		padding-right: 8rpx;
		margin-right: 15rpx;
		text-align: center;
		line-height: 52rpx;
	}

	.XQ_XB_box_xx_TAB_box1 {
		min-width: 98rpx;
		height: 54rpx;
		background: #ECECEC;
		color: #696969;
		border: 0;
	}


	.XQ_XB_box_xx_list {
		width: 100%;
		min-height: 700rpx;
		background-color: #fcfcfc;
		float: left;
	}

	/* 下部列表 */
	.BtoList {
		width: 100%;
		min-height: calc(100vh - 80rpx);
		background-color: #F7F7F7;
		float: left;
	}

	.BtoList_box {
		width: 696rpx;
		height: 220rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 20rpx;
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.BtoList_box_L {
		width: 224rpx;
		height: 164rpx;
		background: #C5C5C5;
		border-radius: 12rpx;
		overflow: hidden;
	}

	.BtoList_box_R {
		width: 415rpx;
		height: 164rpx;
		/* background-color: hotpink; */
		margin-left: 18rpx;
	}

	.BtoList_box_R_oen {
		width: 100%;
		height: 45rpx;
		line-height: 45rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #101010;
		/* background-color: yellow; */
	}

	.BtoList_box_R_two {
		width: 100%;
		height: 40rpx;
		line-height: 50rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		/* background-color: aqua; */
	}

	.BtoList_box_R_therr {
		width: 100%;
		height: 70rpx;
		margin-top: 20rpx;
		/* background-color: orange; */
		display: flex;
		align-items: center;
		justify-content: space-between;

	}

	.BtoList_box_R_therr_l {
		font-size: 40rpx;
		font-family: DIN;
		font-weight: bold;
		color: #E64638;
		display: flex;
		align-items: center;
		height: 56rpx;
	}

	.BtoList_box_R_therr_r {
		width: 136rpx;
		height: 56rpx;
		background: #FFFFFF;
		border: 2rpx solid #ED9939;
		border-radius: 28rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #E78C24;
		line-height: 56rpx;
		text-align: center;
	}


	/* 购车列表 */
	.XQ_XB_box_xx_list1 {
		width: 100%;
		min-height: 700rpx;
		background-color: #fcfcfc;
		float: left;
		display: flex;
		align-items: flex-start;
		justify-content: flex-start;
		flex-wrap: wrap;
		padding-bottom: 40rpx;
	}

	.GouCList {
		width: 340rpx;
		height: 490rpx;
		background: #A0A0A0;
		border-radius: 20rpx 20rpx 20rpx 20rpx;
		margin-top: 20rpx;
		margin-left: 20rpx;
		overflow: hidden;
	}

	.GouCList_image {
		width: 100%;
		height: 340rpx;
		background-color: #C5C5C5;
	}

	.GouCList_text {
		width: 100%;
		height: 150rpx;
		background-color: #FFFFFF;
		float: left;
	}

	.GouCList_text_title {
		width: 90%;
		height: 40rpx;
		line-height: 40rpx;
		margin: 25rpx auto 0;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #101010;
	}

	.GouCList_text_Mp {
		width: 90%;
		height: 65rpx;
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.GouCList_text_Mp_Pirce {
		width: 50%;
		height: 100%;
		text-align: left;
		font-size: 38rpx;
		font-family: DIN;
		font-weight: bold;
		color: #E64638;
	}

	.GouCList_text_Mp_Kc {
		width: 50%;
		height: 100%;
		text-align: right;
		font-size: 24rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.BtoList_box {
		width: 696rpx;
		height: 220rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 20rpx;
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.BtoList_box_L {
		width: 224rpx;
		height: 164rpx;
		background: #C5C5C5;
		border-radius: 12rpx;
		overflow: hidden;
	}

	.BtoList_box_R {
		width: 415rpx;
		height: 164rpx;
		/* background-color: hotpink; */
		margin-left: 18rpx;
	}

	.BtoList_box_R_oen {
		width: 100%;
		height: 45rpx;
		line-height: 45rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #101010;
		/* background-color: yellow; */
	}

	.BtoList_box_R_two {
		width: 100%;
		height: 40rpx;
		line-height: 50rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		/* background-color: aqua; */
	}

	.BtoList_box_R_therr {
		width: 100%;
		height: 70rpx;
		margin-top: 20rpx;
		/* background-color: orange; */
		display: flex;
		align-items: center;
		justify-content: space-between;

	}

	.BtoList_box_R_therr_l {
		font-size: 40rpx;
		font-family: DIN;
		font-weight: bold;
		color: #E64638;
		display: flex;
		align-items: center;
		height: 56rpx;
	}

	.BtoList_box_R_therr_r {
		width: 136rpx;
		height: 56rpx;
		background: #FFFFFF;
		border: 2rpx solid #ED9939;
		border-radius: 28rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #E78C24;
		line-height: 56rpx;
		text-align: center;
	}
</style>
