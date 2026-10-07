<template>
	<view class="Order">

		<!-- 1 -->
		<view class="Order_DH">
			<view class="Order_DH_1">
				<!-- 订单状态：1待支付  2已支付 3进行中 4还车中 5已完成 6已取消 -->
				<view class="Order_DH_1_box" v-if="detailData.state==1">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/shiz.png" class="imageBFB"></image>
					</view>
					<text>待支付</text>
				</view>

				<view class="Order_DH_1_box" v-if="detailData.state==2">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/shiz.png" class="imageBFB"></image>
					</view>
					<text>已支付</text>
				</view>

				<view class="Order_DH_1_box" v-if="detailData.state==3">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/shiz.png" class="imageBFB"></image>
					</view>
					<text>进行中</text>
				</view>

				<view class="Order_DH_1_box" v-if="detailData.state==4">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/shiz.png" class="imageBFB"></image>
					</view>
					<text>还车中</text>
				</view>

				<view class="Order_DH_1_box" v-if="detailData.state==5">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/YWC.png" class="imageBFB"></image>
					</view>
					<text>已完成</text>
				</view>

				<view class="Order_DH_1_box" v-if="detailData.state==6">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/qx.png" class="imageBFB"></image>
					</view>
					<text>已取消</text>
				</view>

			</view>

			<!-- 已支付 -->
			<view class="Order_DH_2" v-if="detailData.state==2">此订单已支付</view>
			<!-- 还车中 -->
			<view class="Order_DH_2" v-if="detailData.state==4">用户还车请求，请点击还车按钮</view>
			<!-- 进行中 -->
			<view class="Order_DH_2" v-if="detailData.state==3">租赁结束时间后进行门店还车</view>
			<!-- 已取消 -->
			<view class="Order_DH_2" v-if="detailData.state==6">该订单已取消</view>


			<!-- 3 -->
			<view class="Order_DH_3" v-if="detailData.state==4">
				<view class="Order_DH_3_btn2" @click="HuanChe">去还车</view>
			</view>
		</view>

		<!-- 2 -->
		<view class="Order_XX">
			<view class="Order_XX_1">
				<view class="Order_XX_1_tao">
					<view class="Order_XX_1_image">
						<view class="Order_XX_1_image_src" @click="ClickYL">
							<image :src="YM + detailData.service_image" class="imageBFB"></image>
						</view>

						<!-- 租赁方式 1时租 2日租 3月租 4分期 -->
						<view class="Order_XX_1_jiao" v-if="detailData.lease_type==3">月租</view>
						<view class="Order_XX_1_jiao" v-if="detailData.lease_type==2">日租</view>
						<view class="Order_XX_1_jiao" v-if="detailData.lease_type==1">时租</view>


					</view>
					<view class="Order_XX_1_text">
						<view class="Order_XX_1_text_title">
							<text>{{detailData.service_name}}</text>
							<text style="color: #858896;font-size: 28rpx;"></text>
						</view>
						<view class="Order_XX_1_text_text">{{detailData.introductory}}</view>
					</view>
				</view>
			</view>



			<view class="Order_XX_2">
				<view class="Order_XX_2_tao">
					<view class="Order_XX_2_L">
						<view class="Order_XX_2_L_title">开始时间</view>
						<view class="Order_XX_2_L_date">{{detailData.starttime.slice(5)}}</view>
						<!-- slice(5) -->
					</view>
					<view class="Order_XX_2_B">
						<text>{{detailData.lease_duration}}</text>
						<text v-if="detailData.lease_type==1">小时</text>
						<text v-if="detailData.lease_type==2">天</text>
						<text v-if="detailData.lease_type==3">月</text>
					</view>
					<view class="Order_XX_2_L">
						<view class="Order_XX_2_L_title right">结束时间</view>
						<view class="Order_XX_2_L_date right">{{detailData.endtime.slice(5)}}</view>
					</view>
				</view>
			</view>


			<view class="Order_XX_3">
				<view class="Order_XX_3_tao">
					<view class="Order_XX_3_box">取车方式</view>
					<text v-if="detailData.pick_type==1">到店取车</text>
					<text v-else>上门送车</text>
				</view>
				<view class="Order_XX_3_tao" v-if="detailData.pick_type!==1">
					<view class="Order_XX_3_box" style="background-color: #FFF4E7;color: #ED9939;">行程</view>
					<text v-if="detailData.pick_single==1">单程</text>
					<text v-else>双程</text>
				</view>

				<view class="Order_XX_3_taox" v-if="detailData.pick_single!==1 && detailData.pick_type!==1">
					<view class="Order_XX_3_taox_address">
						<view class="Order_XX_3_taox_address_icon">
							<image src="../../static/kongdinwei.png" class="imageBFB"></image>
						</view>
						<text>{{detailData.pick_address}}</text>
					</view>

					<view class="Order_XX_3_taox_info">{{detailData.address_name}} {{detailData.address_phone}}</view>
				</view>
			</view>


			<view class="Order_XX_4">
				<view class="Order_XX_4_tao">
					<view class="Order_XX_4_tao_list">
						<view class="Order_XX_4_tao_list_L">姓名</view>
						<view class="Order_XX_4_tao_list_R">{{detailData.name}}</view>
					</view>

					<view class="Order_XX_4_tao_list">
						<view class="Order_XX_4_tao_list_L">手机号</view>
						<view class="Order_XX_4_tao_list_R">{{detailData.mobile}}</view>
					</view>

					<view class="Order_XX_4_tao_list">
						<view class="Order_XX_4_tao_list_L">身份证号</view>
						<view class="Order_XX_4_tao_list_R">{{detailData.idcard}}</view>
					</view>

					<!-- <view class="Order_XX_4_tao_list">
						<view class="Order_XX_4_tao_list_L">期望取车时间</view>
						<view class="Order_XX_4_tao_list_R">{{detailData.take_back_time}}</view>
					</view> -->

					<view class="erkai_cb" v-for="(item,index) in detailData.guest" :key="index">
						<view class="erkai_cb_one">
							<text>承保人</text>
							<view class="erkai_cb_btn_icon"></view>
						</view>

						<view class="erkai_cb_list">
							<text>姓名</text>
							<text>{{item.name}}</text>
						</view>

						<view class="erkai_cb_list">
							<text>手机号</text>
							<text>{{item.mobile}}</text>
						</view>

						<view class="erkai_cb_list">
							<text>身份证号码</text>
							<text>{{item.idcard}}</text>
						</view>
					</view>

				</view>
			</view>
		</view>

		<!-- 3 -->
		<view class="Order_map">
			<view class="Order_map_L">
				<view class="Order_map_L_icon">
					<image src="../../static/Hwu.png" class="imageBFB"></image>
				</view>
				<view class="Order_map_L_text">
					<view class="Order_map_L_text_title">{{detailData.merchant_name}}</view>
					<view class="Order_map_L_text_date">营业时间: 
					<text v-if="detailData.merchant_business!==null">{{detailData.merchant_business}}</text>
					<text v-else>暂无营业时间</text>
					</view>
					<view class="Order_map_L_text_dz">{{detailData.merchant_address}}</view>
				</view>
			</view>
			<view class="Order_map_R">
				<view class="Order_map_R_map" @click="ClickMap">
					<view class="Order_map_R_map_icon">
						<image src="../../static/map.png" class="imageBFB"></image>
					</view>
					<text>地图</text>
				</view>

				<view class="Order_map_R_map" @click="ClickPhone">
					<view class="Order_map_R_map">
						<view class="Order_map_R_map_icon">
							<image src="../../static/phone.png" class="imageBFB"></image>
						</view>
						<text>电话</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 4 -->
		<view class="Order_order">
			<view class="Order_order_tao">
				<view class="Order_order_title">订单信息</view>

				<view class="Order_order_list top">
					<view class="Order_order_list_L">
						<text>订单编号</text>
					</view>
					<view class="Order_order_list_R">
						<view class="Order_order_list_R_fz" @click="ClickFZ">复制</view>
						<text>{{detailData.number}}</text>
					</view>
				</view>

				<view class="Order_order_list">
					<view class="Order_order_list_L">
						<text>创建时间</text>
					</view>
					<view class="Order_order_list_R">
						<text>{{detailData.createtime}}</text>
					</view>
				</view>


				<view class="Order_order_list">
					<view class="Order_order_list_L">
						<text>租车费用</text>
					</view>
					<view class="Order_order_list_R">
						<text>¥{{detailData.lease_price}}</text>
					</view>
				</view>


				<view class="Order_order_list">
					<view class="Order_order_list_L">
						<text>上门送车</text>
					</view>
					<view class="Order_order_list_R">
						<text>￥{{detailData.pick_price}}</text>
					</view>
				</view>

				<view class="Order_order_list">
					<view class="Order_order_list_L">
						<text>租车押金</text>
					</view>
					<view class="Order_order_list_R">
						<text>￥{{detailData.cash_price}}</text>
					</view>
				</view>


				<view class="Order_order_list">
					<view class="Order_order_list_L">
						<text>服务费用</text>
					</view>
					<view class="Order_order_list_R">
						<text>￥{{detailData.insure_money}}</text>
					</view>
				</view>

				<view class="Order_order_list" style="border-top: 2rpx solid #F5F5F5;">
					<view class="Order_order_list_L">
						<text style="color: #000000;font-weight: 600;font-size: 28rpx;">在线支付</text>
					</view>
					<view class="Order_order_list_R">
						<text style="color: #000000;font-weight: 600;font-size: 28rpx;">￥{{detailData.price}}</text>
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
				GoodsData: '',
				diffDays: 0, //天数依赖
				SheBePrice: '', //租车费用 计算得出
				FenQiData: '',
				order_number: '',
				detailData: ''
			}
		},
		onLoad(options) {
			console.log(options.order_number, '接收的参数');
			this.order_number = options.order_number
			uni.setNavigationBarColor({
				frontColor: "#000000", // 前景颜色，即字体颜色
				backgroundColor: "#ffd0b9", // 背景颜色
			});
			this.MDOXQ()
		},
		methods: {
			HuanChe(item){
				uni.navigateTo({
					url:'/pages/erkaiPages/MDhuanche?number=' + this.detailData.number
				})
			},
			MDOXQ() {
				uni.request({
					url: this.YM + '/api/store/orderDetail',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					data: {
						order_number: this.order_number
					},
					success: res => {
						console.log(res.data.data, '详情');
						this.detailData = res.data.data
					}
				})
			},
			// 预览
			YLimages(itemsrc) {
				var SrcImage = this.YM + itemsrc;
				uni.previewImage({
					urls: [SrcImage], // 将Yitme.content封装在数组中
					success: res => {
						console.log(res, '走进来了 预览');
					},
					longPressActions: {
						itemList: ['发送给朋友', '保存图片', '收藏'],
						success: function(data) {
							console.log('选中了第' + (data.tapIndex + 1) + '个按钮,第' + (data.index + 1) + '张图片');
						},
						fail: (err) => {
							console.log(err.errMsg);
						}
					}
				});
			},
			// 复制
			ClickFZ() {
				wx.setClipboardData({
					data: this.detailData.number,
					success(res) {
						wx.showToast({
							title: '复制成功',
							icon: 'success',
							duration: 2000
						})
					}
				})
			},
			//点击了电话
			ClickPhone() {
				console.log('点击了电话');
				uni.makePhoneCall({
					phoneNumber: this.detailData.merchant_mobile //仅为示例
				});
			},
			//点击了地图
			ClickMap() {
				console.log('点击了地图');
				const All = `${this.detailData.merchant_address}`
				uni.openLocation({
					latitude: Number(this.detailData.lat), // 目标位置的纬度
					longitude: Number(this.detailData.lng), // 目标位置的经度
					name: this.detailData.merchant_name, // 目标位置的名称
					address: All, // 目标位置的地址
					scale: 18 // 地图缩放级别，范围1-28
				});
			},
			//预览图片
			ClickYL() {
				console.log('点击了预览图片');
				var SrcImage = this.YM + this.detailData.service_image;
				uni.previewImage({
					urls: [SrcImage], // 将Yitme.content封装在数组中
					success: res => {
						console.log(res, '走进来了 预览');
					},
					longPressActions: {
						itemList: ['发送给朋友', '保存图片', '收藏'],
						success: function(data) {
							console.log('选中了第' + (data.tapIndex + 1) + '个按钮,第' + (data.index + 1) + '张图片');
						},
						fail: (err) => {
							console.log(err.errMsg);
						}
					}
				});
			},
		}
	}
</script>

<style>
	.Order {
		width: 100vw;
		min-height: 100vh;
		background: linear-gradient(0deg, #F7F7F7 0%, #FBEBE0 51%, #FFCFB7 100%);
		float: left;
		padding-bottom: 50rpx;
	}

	/* 1 */
	.Order_DH {
		width: 696rpx;
		/* height: 276rpx; */
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 20rpx;
		margin: 20rpx auto 0;
		padding-top: 35rpx;
		padding-bottom: 35rpx;
	}

	.Order_DH_1 {
		width: 100%;
		height: 50rpx;
		/* background-color: red; */
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Order_DH_1_box {
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 32rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
	}

	.Order_DH_1_box_icon {
		width: 30rpx;
		height: 30rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		/* background-color: green; */
		margin-right: 15rpx;
	}

	.Order_DH_2 {
		width: 100%;
		height: 40rpx;
		text-align: center;
		line-height: 40rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		margin-top: 20rpx;
	}

	.Order_DH_3 {
		width: 100%;
		height: 100rpx;
		/* background-color: darkseagreen; */
		margin-top: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		padding-bottom: 30rpx;
	}

	.Order_DH_3_btn1 {
		width: 242rpx;
		height: 84rpx;
		background: #FFFFFF;
		border: 2rpx solid #E6E6E6;
		border-radius: 44rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #333333;
		text-align: center;
		line-height: 84rpx;
		margin-left: 15rpx;
	}

	.Order_DH_3_btn2 {
		width: 240rpx;
		height: 84rpx;
		border-radius: 100rpx 100rpx 100rpx 100rpx;
		border: 2rpx solid #ED9939;
		font-family: PingFang SC, PingFang SC;
		font-weight: bold;
		font-size: 28rpx;
		color: #ED9939;
		text-align: center;
		line-height: 84rpx;
	}

	.Order_DH_3_btn3 {
		width: 242rpx;
		height: 84rpx;
		background: #FFFFFF;
		border: 2rpx solid #E6E6E6;
		border-radius: 44rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #333333;
		text-align: center;
		line-height: 84rpx;
		margin-left: 15rpx;
	}

	.Order_DH_3_btn4,
	.Order_DH_3_btn4A {
		width: 356rpx;
		height: 88rpx;
		background: #E24E11;
		border-radius: 44rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FFFFFF;
		text-align: center;
		line-height: 88rpx;
		margin-right: 8rpx;
	}

	.Order_DH_3_btn4A {
		background-color: #ccc;
	}

	.Order_DH_3_btn5,
	.Order_DH_3_btn51 {
		width: 242rpx;
		height: 84rpx;
		background: #FFFFFF;
		border: 2rpx solid #ED9939;
		border-radius: 44rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #ED9939;
		text-align: center;
		line-height: 84rpx;
	}

	.Order_DH_3_btn51 {
		width: 292rpx;
	}

	/* 取消订单弹出 */
	.QXorder {
		width: 636rpx;
		/* height: 348rpx; */
		background: #FFFFFF;
		border-radius: 16rpx;
		float: left;
	}

	.QXorder_title {
		width: 100%;
		height: 60rpx;
		line-height: 60rpx;
		text-align: center;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #101010;
		margin-top: 35rpx;
	}


	.QXorder_text {
		width: 100%;
		height: 40rpx;
		text-align: center;
		line-height: 40rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858585;
		margin-top: 20rpx;
	}

	.QXorder_btn {
		width: 100%;
		height: 90rpx;
		/* background-color: salmon; */
		display: flex;
		align-items: center;
		justify-content: space-around;
		margin-top: 30rpx;
	}

	.QXorder_btn1,
	.QXorder_btn2 {
		width: 236rpx;
		height: 80rpx;
		background: #F7F7F7;
		border: 1rpx solid #E5E5E5;
		border-radius: 40rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #5A5A5A;
		margin-left: 25rpx;
		text-align: center;
		line-height: 80rpx;
	}

	.QXorder_btn2 {
		width: 236rpx;
		height: 80rpx;
		background: #FB8E1F;
		border-radius: 40rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #FFFFFF;
		margin-left: 0;
		margin-right: 25rpx;
		border: 0;
	}

	/* 分期付款记录弹出 */
	.QXFQ {
		width: 100%;
		min-height: 530rpx;
		max-height: 780rpx;
		background-color: #FFFFFF;
		float: left;
	}

	.QXFQ_text {
		width: 666rpx;
		height: 70rpx;
		text-align: center;
		line-height: 40rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #333333;
		border-bottom: 1rpx solid #F0F0F0;
		margin: 35rpx auto 0;
	}

	.QXFQ_box {
		width: 666rpx;
		height: 430rpx;
		/* background-color: aqua; */
		margin: 30rpx auto 0;
		overflow: auto;
	}

	.QXFQ_box_list {
		width: 100%;
		height: 140rpx;
		/* background-color: skyblue; */
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 20rpx;
	}

	.QXFQ_box_list_L {
		width: 80%;
		height: 100%;
		/* background-color: red; */
	}

	.QXFQ_box_list_L_title {
		width: 100%;
		height: 50%;
		/* background-color: salmon; */
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		line-height: 70rpx;
	}

	.QXFQ_box_list_L_date {
		width: 100%;
		height: 50%;
		/* background-color: mediumblue; */
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.QXFQ_box_list_R {
		width: 20%;
		height: 100%;
		/* background-color: hotpink; */
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 40rpx;
		font-family: DIN;
		font-weight: bold;
		color: #E64638;
	}

	/* 2 */
	.Order_XX {
		width: 696rpx;
		min-height: 402rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 20rpx;
		margin: 20rpx auto 0;
		padding-top: 20rpx;
		padding-bottom: 15rpx;
	}

	.Order_XX_1 {
		width: 650rpx;
		height: 140rpx;
		/* background-color: aquamarine; */
		margin: 0 auto;
		border-bottom: 1rpx solid #F0F0F0;
		display: flex;
		align-items: center;
		justify-content: center;

	}

	.Order_XX_1_tao {
		width: 630rpx;
		height: 140rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.Order_XX_1_image {
		width: 148rpx;
		height: 108rpx;
		background: #C5C5C5;
		border-radius: 12rpx;
		overflow: hidden;
		/* background-color: green; */
		position: relative;
	}

	.Order_XX_1_image_src {
		width: 100%;
		height: 100%;
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		/* background-color: green; */
		z-index: 1;
	}

	.Order_XX_1_jiao {
		width: 60rpx;
		height: 32rpx;
		background: #ED9939;
		opacity: 0.8;
		border-radius: 10rpx 0rpx 10rpx 0rpx;
		font-size: 22rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FFFFFF;
		text-align: center;
		line-height: 32rpx;
		position: absolute;
		left: 0;
		top: 0;
		z-index: 2;
	}

	.Order_XX_1_text {
		width: 460rpx;
		height: 108rpx;
		/* background-color: khaki; */
	}

	.Order_XX_1_text_title {
		width: 100%;
		height: 50%;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		line-height: 54rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.Order_XX_1_text_text {
		width: 100%;
		height: 50%;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		line-height: 54rpx;
	}

	.Order_XX_2 {
		width: 650rpx;
		height: 140rpx;
		border-bottom: 1rpx solid #F0F0F0;
		margin: 0 auto;
		display: flex;
		align-items: center;
		/* background-color: antiquewhite; */
		justify-content: space-between;
	}

	.Order_XX_2_tao {
		width: 630rpx;
		height: 140rpx;
		margin: 0 auto;
		display: flex;
		align-items: center;
		/* background-color: antiquewhite; */
		justify-content: space-between;
	}

	.Order_XX_2_L {
		width: 230rpx;
		height: 100rpx;
		/* background-color: darkcyan; */
	}

	.Order_XX_2_B {
		width: 88rpx;
		height: 40rpx;
		background: #FFF0DF;
		border-radius: 20rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #ffaa7f;
		text-align: center;
		line-height: 40rpx;
	}

	.Order_XX_2_L_title {
		width: 100%;
		height: 40rpx;
		font-size: 24rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		text-align: left;
	}

	.Order_XX_2_L_date {
		width: 100%;
		height: 50rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		text-align: left;
	}

	.right {
		text-align: right;
	}

	.Order_XX_2x {
		width: 650rpx;
		height: 210rpx;
		border-bottom: 1rpx solid #F0F0F0;
		margin: 0 auto;
		/* background-color: antiquewhite; */
	}

	.Order_XX_2x_tao {
		width: 630rpx;
		height: 210rpx;
		/* background-color: salmon; */
		margin: 0 auto;
	}

	.Order_XX_3 {
		width: 650rpx;
		min-height: 105rpx;
		border-bottom: 1rpx solid #F0F0F0;
		margin: 0 auto;
		/* background-color: antiquewhite; */
	}

	.Order_XX_3_tao {
		width: 630rpx;
		height: 75rpx;
		margin: 0 auto;
		display: flex;
		align-items: center;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
	}

	.Order_XX_3_taox {
		width: 630rpx;
		height: 110rpx;
		/* background-color: aqua; */
		margin: 15rpx auto 0;
		display: flex;
		flex-wrap: wrap;
	}

	.Order_XX_3_taox_address {
		width: 100%;
		min-height: 40rpx;
		display: flex;
		align-items: flex-start;
		flex-wrap: wrap;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 26rpx;
		color: #000000;
	}

	.Order_XX_3_taox_address_icon {
		width: 28rpx;
		height: 28rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 8rpx;
	}

	.Order_XX_3_taox_info {
		width: 100%;
		height: 40rpx;
		text-indent: 1.5em;
		font-family: PingFang SC, PingFang SC;
		font-weight: 400;
		font-size: 26rpx;
		color: #999999;
	}

	.Order_XX_3_taox_L {
		width: 15%;
		height: 100%;
		/* background-color: red; */
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.Order_XX_3_taox_R {
		width: 85%;
		height: 100%;
		/* background-color: violet; */
	}

	.Order_XX_3_taox_R1 {
		width: 100%;
		height: 50%;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		text-align: right;
	}

	.Order_XX_3_taox_R2 {
		width: 100%;
		height: 50%;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		text-align: right;
	}

	.Order_XX_3_box {
		width: 124rpx;
		height: 40rpx;
		background: #DDFBEE;
		border-radius: 8rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #388C67;
		text-align: center;
		line-height: 40rpx;
		margin-right: 15rpx;
	}

	.Order_XX_4 {
		width: 650rpx;
		/* background-color: aqua; */
		margin: 0 auto;
		padding-bottom: 20rpx;
	}

	.Order_XX_4_tao {
		width: 630rpx;
		height: 100%;
		margin: 0 auto;
	}

	.Order_XX_4_tao_list {
		width: 100%;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 5rpx;
		margin-top: 5rpx;
	}

	.Order_XX_4_tao_list_L {
		width: 40%;
		height: 100%;
		line-height: 60rpx;
		text-align: left;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.Order_XX_4_tao_list_R {
		width: 60%;
		height: 100%;
		line-height: 60rpx;
		text-align: right;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
	}

	/* 3 */
	.Order_map {
		width: 696rpx;
		height: 180rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 20rpx;
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Order_map_L {
		width: 62%;
		height: 100%;
		/* background-color: red; */
		display: flex;
	}

	.Order_map_L_icon {
		width: 28rpx;
		height: 26rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-top: 38rpx;
		margin-right: 8rpx;
	}

	.Order_map_L_text {
		width: 380rpx;
		height: 120rpx;
		margin-top: 30rpx;
		/* background-color: aqua; */
	}

	.Order_map_L_text_title {
		width: 100%;
		height: 40rpx;
		line-height: 40rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		/* background-color: violet; */
	}

	.Order_map_L_text_date,
	.Order_map_L_text_dz {
		width: 100%;
		height: 30rpx;
		line-height: 30rpx;
		font-size: 24rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #6B6E7D;
		line-height: 50rpx;
		/* margin-top: 10rpx; */
	}

	.Order_map_L_text_dz {
		height: 45rpx;
		margin-top: 8rpx;
		line-height: 27rpx;
	}

	.Order_map_R {
		width: 30%;
		height: 100%;
		/* background-color: hotpink; */
		display: flex;
		align-items: center;
		justify-content: space-around;
	}

	.Order_map_R_map {
		width: 54rpx;
		height: 100rpx;
		/* background-color: blue; */
		display: flex;
		align-items: center;
		justify-content: center;
		flex-wrap: wrap;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #2A2E79;
	}

	.Order_map_R_map_icon {
		width: 54rpx;
		height: 72rpx;
		margin-bottom: 5rpx;
	}

	/* 4 */
	.Order_order {
		width: 696rpx;
		min-height: 404rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 16rpx;
		margin: 20rpx auto 0;
		padding-top: 20rpx;
		padding-bottom: 15rpx;
	}

	.Order_order_tao {
		width: 630rpx;
		/* height: 500rpx; */
		/* background-color: yellow; */
		margin: 0 auto;
	}

	.Order_order_title {
		width: 100%;
		height: 50rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		line-height: 50rpx;
		margin-top: 15rpx;
	}

	.Order_order_list {
		width: 100%;
		height: 70rpx;
		/* background-color: red; */
		margin-bottom: 5rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.top {
		margin-top: 20rpx;
	}

	.Order_order_list_L {
		width: 30%;
		height: 100%;
		/* background-color: yellow; */
		display: flex;
		align-items: center;
		justify-content: flex-start;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.Order_order_list_L_icon {
		width: 72rpx;
		height: 30rpx;
		border: 1rpx solid #FF2D2D;
		border-radius: 4rpx;
		font-size: 22rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: red;
		text-align: center;
		line-height: 30rpx;
		margin-left: 15rpx;
	}

	.Order_order_list_R {
		width: 70%;
		height: 100%;
		/* background-color: skyblue; */
		display: flex;
		align-items: center;
		justify-content: flex-end;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.Order_order_list_R_fz {
		width: 52rpx;
		height: 30rpx;
		background: #DAE7FF;
		border-radius: 4rpx;
		font-size: 22rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #0055ff;
		text-align: center;
		line-height: 30rpx;
		margin-right: 15rpx;
	}

	.Order_order_zaixian {
		width: 650rpx;
		height: 100rpx;
		/* background-color: violet; */
		margin: 15rpx auto 0;
		border-top: 1rpx solid #F0F0F0;
	}

	.Order_order_zaixian_B {
		width: 630rpx;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin: 0 auto;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
	}


	/* 还车照片 */
	.Order_Hc {
		width: 696rpx;
		min-height: 408rpx;
		max-height: 678rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 16rpx;
		margin: 20rpx auto 0;
		padding-top: 20rpx;
		overflow: auto;

	}

	.Order_Hc_title {
		width: 630rpx;
		height: 70rpx;
		/* background-color: aqua; */
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		margin: 0 auto;
	}

	.Order_Hc_image {
		width: 645rpx;
		/* height: 350rpx; */
		/* background-color: aqua; */
		margin: 20rpx auto 0;

	}

	.Order_Hc_image_box {
		float: left;
	}

	.Order_Hc_image_src {
		width: 200rpx;
		height: 200rpx;
		background: #545454;
		border-radius: 16rpx;
		margin-bottom: 15rpx;
		margin-right: 15rpx;
		background-color: green;
		overflow: hidden;
		float: left;
	}

	.Order_Hc_text {
		width: 100%;
		height: 150rpx;
		margin: 0 auto;
		float: left;
	}

	.Order_Hc_text_box {
		width: 650rpx;
		height: 100%;
		border-top: 1rpx solid #F0F0F0;
		/* background-color: aquamarine; */
		margin: 0 auto;
	}

	.Order_Hc_text_box_title {
		width: 100%;
		height: 50rpx;
		line-height: 50rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		margin-top: 20rpx;
	}

	.Order_Hc_text_box_text {
		width: 100%;
		height: 50rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		margin-top: 15rpx;
	}

	/* 野马二开css */
	.erkai_cb {
		width: 640rpx;
		border-radius: 12rpx;
		background-color: rgba(245, 245, 245, 1);
		margin: 18rpx auto 0;
		padding-top: 18rpx;
		padding-bottom: 18rpx;
	}

	.erkai_cb_one {
		width: 90%;
		height: 50rpx;
		margin: 0 auto;
		display: flex;
		align-items: center;
		justify-content: space-between;
		color: rgba(237, 153, 57, 1);
		font-size: 28rpx;
	}

	.erkai_cb_list {
		width: 90%;
		height: 68rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-size: 28rpx;
		color: #000000;
		margin: 2rpx auto 0;
	}

	.erkai_cb_btn {
		width: 640rpx;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 12rpx;
		background-color: rgba(255, 255, 255, 1);
		text-align: center;
		border: 2rpx solid rgba(237, 153, 57, 1);
		display: flex;
		align-items: center;
		justify-content: center;
		color: rgba(237, 153, 57, 1);
		font-size: 28rpx;
		margin: 20rpx auto 0;
	}

	.erkai_cb_btn_icon {
		width: 35rpx;
		height: 35rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 15rpx;
	}

	.CBR {
		width: 100%;
		float: left;
		position: relative;
	}

	.CBR_title {
		width: 100%;
		height: 50rpx;
		text-align: center;
		line-height: 50rpx;
		font-size: 32rpx;
		font-weight: 600;
		margin-top: 25rpx;
	}

	.CBR_out {
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		position: absolute;
		right: 25rpx;
		top: 25rpx;
		z-index: 999;
	}

	.CBR_btn {
		width: 624rpx;
		height: 92rpx;
		line-height: 92rpx;
		border-radius: 46rpx;
		background-color: rgba(237, 153, 57, 1);
		text-align: center;
		color: rgba(255, 255, 255, 1);
		font-size: 28rpx;
		margin: 55rpx auto 0;
	}
</style>
