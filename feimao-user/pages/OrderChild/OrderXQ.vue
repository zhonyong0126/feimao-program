<template>
	<view class="Order">

		<!-- 1 -->
		<view class="Order_DH">
			<view class="Order_DH_1">
<!-- 订单状态：1待支付  2已支付 3进行中 4还车中 5已完成 6已取消 -->
				<!-- 待支付 -->
				<view class="Order_DH_1_box" v-if="GoodsData.state==1">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/shiz.png" class="imageBFB"></image>
					</view>
					<text>待支付</text>
				</view>

				<!-- 已支付 1-->
				<view class="Order_DH_1_box" v-if="GoodsData.state==2" style="height: ;">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/shiz.png" class="imageBFB"></image>
					</view>
					<text>已支付</text>
				</view>

<!-- 1待支付  2已支付 3进行中 4已完成 5已取消 -->
				<!-- 待使用 -->
				<view class="Order_DH_1_box" v-if="GoodsData.state==3">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/shiz.png" class="imageBFB"></image>
					</view>
					<text>进行中</text>
				</view>
				

				<!-- 进行中 -->
				<view class="Order_DH_1_box" v-if="GoodsData.state==4">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/shiz.png" class="imageBFB"></image>
					</view>
					<text>还车中</text>
				</view>

				<!-- 已完成 -->
				<view class="Order_DH_1_box" v-if="GoodsData.state==5">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/YWC.png" class="imageBFB"></image>
					</view>
					<text>已完成</text>
				</view>

				<!-- 已取消 -->
				<view class="Order_DH_1_box" v-if="GoodsData.state==6">
					<view class="Order_DH_1_box_icon">
						<image src="../../static/qx.png" class="imageBFB"></image>
					</view>
					<text>已取消</text>
				</view>

			</view>

			<!-- 2 -->
			<!-- 待支付 -->
			<view class="Order_DH_2" v-if="GoodsData.state==1">您的订单等待支付...</view>
			<!-- 已支付1 -->
			<view class="Order_DH_2" v-if="GoodsData.state==2 && GoodsData.type==1 && GoodsData.lease_type!=4">您的订单已支付，请按时取车
			</view>
			<!-- 已支付2 -->
			<view class="Order_DH_2" v-if="GoodsData.state==2 && GoodsData.type==1 && GoodsData.lease_type==4">分期付款完成后，车辆将归您所有</view>
			<!-- 已支付3 -->
			<view class="Order_DH_2" v-if="GoodsData.state==2 && GoodsData.type==2">购车订单已支付，请按约定方式提车</view>
			<view class="Order_DH_2" v-if="GoodsData.state==2 && GoodsData.type==3">维修订单已支付，请按约定时间接受服务</view>
			<!-- 待使用 -->
			<view class="Order_DH_2" v-if="GoodsData.state==3 && GoodsData.type==3">已成功支付，请前往门店，门店商家为您进行维修</view>
			<!-- 进行中 -->
			<view class="Order_DH_2" v-if="GoodsData.state==3 && GoodsData.type==1">请在租赁结束时间前前往门店还车</view>
			<!-- 已完成1 -->
			<view class="Order_DH_2" v-if="GoodsData.state==4">正在还车中</view>
			<!-- 已完成2 -->
			<view class="Order_DH_2" v-if="GoodsData.state==5">您的订单已完成</view>
			<!-- 已取消 -->
			<view class="Order_DH_2" v-if="GoodsData.state==6">您的订单已关闭</view>


			<!-- 3 -->
			<view class="Order_DH_3">
				<text v-if="GoodsData.state==2" style="color: #ccc;">支付成功...</text>
				<!-- 待支付 -->
				<view class="Order_DH_3_btn1" @click="ClickQXorder" v-if="GoodsData.state==1">取消订单</view>
				<view class="Order_DH_3_btn2" @click="ClickPay" v-if="GoodsData.state==1">立即支付</view>

				<!-- 已支付2 -->
				<view class="Order_DH_3_btn3" @click="ClickFQFK" v-if="GoodsData.state==2 && GoodsData.lease_type==4">
					分期明细</view>
				<view :class="GoodsData.should_price==0?'Order_DH_3_btn4A':'Order_DH_3_btn4'"
					v-if="GoodsData.state==2 && GoodsData.lease_type==4">
					<text v-if="Number(GoodsData.should_price || 0) > 0" @click="ClickPayFenQi">本期需付款
						¥{{GoodsData.should_price}}</text>
					<text v-else style="color: #858896;">本期无需付款</text>
				</view>

				<!-- 进行中 -->
				<view class="Order_DH_3_btn5" @click="ClickHC" v-if="GoodsData.state==3 && GoodsData.type==1 && GoodsData.pick_state==1">去还车
				</view>
				<view class="Order_DH_3_btn51" v-if="GoodsData.state==3 && GoodsData.type==1 && GoodsData.pick_state==2" @click="ClickSH">
					还车审核中</view>
			</view>
		</view>

		<!-- 2 -->
		<view class="Order_XX">
			<view class="Order_XX_1">
				<view class="Order_XX_1_tao">
					<view class="Order_XX_1_image">
						<view class="Order_XX_1_image_src" @click="ClickYL">
							<image :src="YM + GoodsData.service_image" class="imageBFB"></image>
						</view>

						<view class="Order_XX_1_jiao" v-if="GoodsData.lease_type==3">月租</view>
						<view class="Order_XX_1_jiao" v-if="GoodsData.lease_type==2">日租</view>
						<view class="Order_XX_1_jiao" v-if="GoodsData.lease_type==1">时租</view>
						<view class="Order_XX_1_jiao" v-if="GoodsData.lease_type==4">分期</view>


					</view>
					<view class="Order_XX_1_text">
						<view class="Order_XX_1_text_title">
							<text>{{GoodsData.service_name}}</text>
							<text style="color: #858896;font-size: 28rpx;">x{{GoodsData.num}}</text>
						</view>
						<view class="Order_XX_1_text_text">{{GoodsData.introductory}}</view>
					</view>
				</view>
			</view>
			
			

			<view class="Order_XX_2" v-if="GoodsData.type==1">
				<view class="Order_XX_2_tao">
					<view class="Order_XX_2_L">
						<view class="Order_XX_2_L_title">开始时间</view>
						<view class="Order_XX_2_L_date">{{shortTime(GoodsData.starttime_text)}}</view>
					</view>
					<view class="Order_XX_2_B">
					{{GoodsData.lease_duration}}
					<text v-if="GoodsData.lease_type==1">小时</text>
					<text v-if="GoodsData.lease_type==2">天</text>
					<text v-if="GoodsData.lease_type==3">月</text>
					</view>
					<view class="Order_XX_2_L">
						<view class="Order_XX_2_L_title right">结束时间</view>
						<view class="Order_XX_2_L_date right">{{shortTime(GoodsData.endtime_text)}}</view>
					</view>
				</view>
			</view>
			
<!-- 			<view class="Order_XX_2x" v-if="GoodsData.state==2 && GoodsData.lease_type==4">
				<view class="Order_XX_2x_tao">
					<view class="Order_XX_4_tao_list">
						<view class="Order_XX_4_tao_list_L">租赁时间</view>
						<view class="Order_XX_4_tao_list_R">9月7日</view>
					</view>

					<view class="Order_XX_4_tao_list">
						<view class="Order_XX_4_tao_list_L">分期时间</view>
						<view class="Order_XX_4_tao_list_R">3期</view>
					</view>

					<view class="Order_XX_4_tao_list">
						<view class="Order_XX_4_tao_list_L">每期单价</view>
						<view class="Order_XX_4_tao_list_R">¥499</view>
					</view>
				</view>
			</view> -->

			<view class="Order_XX_3" v-if="GoodsData.type!==3">
				<view class="Order_XX_3_tao">
					<view class="Order_XX_3_box">取车方式</view>
					<text v-if="GoodsData.pick_type==1">到店取车</text>
					<text v-if="GoodsData.pick_type==3">上门送车</text>
				</view>
				<!-- 已支付3 显示 -->
				<view class="Order_XX_3_taox" v-if="GoodsData.pick_type==1">
					<view class="Order_XX_3_taox_L">地址</view>
					<view class="Order_XX_3_taox_R">
						<view class="Order_XX_3_taox_R1">{{GoodsData.merchant_address}}</view>
						<view class="Order_XX_3_taox_R2">{{GoodsData.merchant_name}} {{GoodsData.merchant_mobile}}
						</view>
					</view>
				</view>
				<view class="Order_XX_3_taox" v-if="GoodsData.pick_type==3">
					<view class="Order_XX_3_taox_L">送车地址</view>
					<view class="Order_XX_3_taox_R">
						<view class="Order_XX_3_taox_R1">{{GoodsData.pick_address || '--'}}</view>
					</view>
				</view>
			</view>


			<view class="Order_XX_4">
				<view class="Order_XX_4_tao">
					<view class="Order_XX_4_tao_list">
						<view class="Order_XX_4_tao_list_L">姓名</view>
						<view class="Order_XX_4_tao_list_R">{{GoodsData.name}}</view>
					</view>

					<view class="Order_XX_4_tao_list">
						<view class="Order_XX_4_tao_list_L">手机号</view>
						<view class="Order_XX_4_tao_list_R">{{GoodsData.mobile}}</view>
					</view>

					<!-- <view class="Order_XX_4_tao_list">
						<view class="Order_XX_4_tao_list_L">期望取车时间</view>
						<view class="Order_XX_4_tao_list_R">{{GoodsData.take_back_time}}</view>
					</view> -->
				</view>
			</view>
		</view>

		<!-- 中部增加  还车照片 -->
		<view class="Order_Hc" v-if="GoodsData.pick_state==2">
			<view class="Order_Hc_title">
				<text>还车照片</text>
				<text style="font-size: 26rpx;color: #858896;font-weight: 500;">{{GoodsData.backtime_text}}</text>
			</view>

			<view class="Order_Hc_image">
				<view class="Order_Hc_image_box" v-for="(itemsrc,index) in GoodsData.back_image" :key="index">
					<view class="Order_Hc_image_src">
						<image :src="YM + itemsrc" class="imageBFB" @click="YLimages(itemsrc)"></image>
					</view>


				</view>
			</view>

			<view class="Order_Hc_text">
				<view class="Order_Hc_text_box">
					<view class="Order_Hc_text_box_title">车辆情况描述</view>
					<view class="Order_Hc_text_box_text">{{GoodsData.back_content}}</view>
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
					<view class="Order_map_L_text_title">{{GoodsData.merchant_name}}</view>
					<view class="Order_map_L_text_date">营业时间: {{GoodsData.merchant_business}}</view>
					<view class="Order_map_L_text_dz">{{GoodsData.merchant_address}}</view>
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
						<text>{{GoodsData.number}}</text>
					</view>
				</view>

				<view class="Order_order_list">
					<view class="Order_order_list_L">
						<text>创建时间</text>
					</view>
					<view class="Order_order_list_R">
						<text>{{GoodsData.createtime_text || '--'}}</text>
					</view>
				</view>

				<view class="Order_order_list" v-if="GoodsData.type!==3 && GoodsData.type!==2">
					<view class="Order_order_list_L">
						<text>服务费用</text>
					</view>
					<view class="Order_order_list_R">
						<text>￥{{GoodsData.insure_money}}</text>
					</view>
				</view>

				<view class="Order_order_list" v-if="GoodsData.type!==3 && GoodsData.type!==2">
					<view class="Order_order_list_L">
						<text>租车费用</text>
					</view>
					<view class="Order_order_list_R">
						<text>¥{{GoodsData.lease_money}}</text>
					</view>
				</view>
				
				<view class="Order_order_list" v-if="GoodsData.pick_type==3">
					<view class="Order_order_list_L">
						<text>取车费用</text>
					</view>
					<view class="Order_order_list_R">
						<text>¥{{GoodsData.pick_price}}</text>
					</view>
				</view>

				<view class="Order_order_list" v-if="GoodsData.type!==3 && GoodsData.type!==2">
					<view class="Order_order_list_L">
						<text>租车押金</text>
						<!-- <view class="Order_order_list_L_icon">待退还</view> -->
					</view>
					<view class="Order_order_list_R">
						<text>¥{{GoodsData.cash_price}}</text>
					</view>
				</view>

				<view class="Order_order_list" v-if="GoodsData.type==2 || GoodsData.type==3">
					<view class="Order_order_list_L">
						<text>数量</text>
					</view>
					<view class="Order_order_list_R">
						<text>x{{GoodsData.num}}</text>
					</view>
				</view>

				<view class="Order_order_list" v-if="GoodsData.type==2">
					<view class="Order_order_list_L">
						<text>购车费用</text>
					</view>
					<view class="Order_order_list_R">
						<text>￥{{GoodsData.price}}</text>
					</view>
				</view>

				<view class="Order_order_list" v-if="GoodsData.type==3">
					<view class="Order_order_list_L">
						<text>维修费用</text>
					</view>
					<view class="Order_order_list_R">
						<text>￥{{GoodsData.price}}</text>
					</view>
				</view>

				<view class="Order_order_list" v-if="Number(GoodsData.discounts_price || 0) > 0">
					<view class="Order_order_list_L">
						<text>优惠</text>
					</view>
					<view class="Order_order_list_R">
						<text style="color:red;">减¥{{GoodsData.discounts_price}}</text>
					</view>
				</view>

			</view>
			<view class="Order_order_zaixian">
				<view class="Order_order_zaixian_B">
					<text>在线支付</text>
					<text v-if="GoodsData.pay_price!==null">￥{{money(GoodsData.pay_price)}}</text>
					<text v-else>暂未支付 ￥{{SheBePrice}}</text>
				</view>
			</view>

		</view>



		<!-- 取消订单弹出 -->
		<u-popup :show="show" mode="center" @close="close" @open="open" :round="10">
			<view class="QXorder">
				<view class="QXorder_title">取消订单</view>
				<view class="QXorder_text">确定取消订单？取消后订单将关闭</view>
				<view class="QXorder_btn">
					<view class="QXorder_btn1" @click="ClickZBQX">暂不取消</view>
					<view class="QXorder_btn2" @click="ClickQDQX">确定取消</view>
				</view>
			</view>
		</u-popup>
		<!-- 分期付款记录弹出 -->
		<u-popup :show="show1" mode="bottom" @close="close" @open="open" :round="10" :closeable='true'>
			<view class="QXFQ">
				<view class="QXorder_title">分期付款记录</view>
				<view class="QXFQ_text">租期{{GoodsData.lease_duration}}，每期¥{{GoodsData.lease_price}}</view>
				<view class="QXFQ_box">
					<view class="QXFQ_box_list" v-for="(item,index) in FenQiData" :key="index">
						<view class="QXFQ_box_list_L">
							<view class="QXFQ_box_list_L_title">{{item.num}}期费用
								<text
									style="font-size: 24rpx;color: orangered;margin-left: 15rpx;display: inline-block;"
									v-if="item.state==1">待还款</text>
								<text style="font-size: 24rpx;color: green;margin-left: 15rpx;display: inline-block;"
									v-if="item.state==2">已还款</text>
								<text style="font-size: 24rpx;color: red;margin-left: 15rpx;display: inline-block;"
									v-if="item.state==3">已逾期</text>
							</view>
							<view class="QXFQ_box_list_L_date" v-if="item.paytime_text!==''">{{item.paytime_text}}
							</view>
							<view class="QXFQ_box_list_L_date" v-else>暂未还款</view>
						</view>
						<view class="QXFQ_box_list_R">
							<text>￥{{item.price}}</text>
						</view>
					</view>

				</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				show: false,
				show1: false,
				cancelling: false,
				installmentPaying: false,
				GoodsData: '',
				diffDays: 0, //天数依赖
				SheBePrice: '', //租车费用 计算得出
				FenQiData: []
			}
		},
		onLoad() {
			uni.setNavigationBarColor({
				frontColor: "#000000", // 前景颜色，即字体颜色
				backgroundColor: "#ffd0b9", // 背景颜色
			});
		},
		onShow() {
			this.XQapi()
		},
		methods: {
			money(value) {
				return Number(value || 0).toFixed(2)
			},
			shortTime(value) {
				return typeof value === 'string' && value.length > 5 ? value.slice(5) : '--'
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
			ClickSH() {
				uni.showToast({
					title: '正在审核中，请等待...',
					icon: 'none',
					duration: 2000
				});
			},
			// 复制
			ClickFZ() {
				wx.setClipboardData({
					data: this.GoodsData.number,
					success(res) {
						wx.showToast({
							title: '复制成功',
							icon: 'success',
							duration: 2000
						})
					}
				})
			},
			// 分期订单分期明细
			FenApi() {
				uni.request({
					url: this.YM + '/api/order/installmentDetail',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token')
					},
					data: {
						order_number: this.GoodsData.number
					},
					success: res => {
						this.FenQiData = res.data.code == 1 && Array.isArray(res.data.data) ? res.data.data : []
						const unpaid = this.FenQiData.find(item => Number(item.state) === 1)
						this.$set(this.GoodsData, 'should_id', unpaid ? unpaid.id : 0)
						this.$set(this.GoodsData, 'should_price', unpaid ? unpaid.price : 0)
					},
					fail: () => {
						this.FenQiData = []
						this.$set(this.GoodsData, 'should_id', 0)
						this.$set(this.GoodsData, 'should_price', 0)
						uni.showToast({ title: '分期明细加载失败', icon: 'none' })
					}
				})
			},
			//详情接口
			XQapi() {
				let data = {
					order_number: uni.getStorageSync('order_number')
				}
				uni.request({
					url: this.YM + '/api/order/detail',
					method: 'POST',
					data,
					header:{
						'token': uni.getStorageSync('token'),
					},
					success: res => {
						if (res.data.code != 1 || !res.data.data) {
							uni.showToast({
								title: res.data.msg || '订单详情加载失败',
								icon: 'none'
							})
							return
						}
						this.GoodsData = res.data.data;
						//支付金额减掉押金，等到设备金额 租赁费用
						this.SheBePrice = this.money(Math.max(0, Number(this.GoodsData.price || 0) - Number(this.GoodsData.discounts_price || 0)))

						if (this.GoodsData.starttime_text && this.GoodsData.endtime_text) {
							let starttime = new Date(this.GoodsData.starttime_text)
							let endtime = new Date(this.GoodsData.endtime_text)
							if (this.GoodsData.lease_type == 3 || this.GoodsData.lease_type == 2 || this.GoodsData
								.lease_type == 4) {
								let startYear = starttime.getFullYear();
								let startMonth = starttime.getMonth() + 1;
								let startDate = starttime.getDate();
								let endYear = endtime.getFullYear();
								let endMonth = endtime.getMonth() + 1;
								let endDate = endtime.getDate();

								let startTimeStr = startYear + '-' + (startMonth < 10 ? '0' + startMonth :
									startMonth) + '-' + (startDate < 10 ? '0' + startDate : startDate);
								let endTimeStr = endYear + '-' + (endMonth < 10 ? '0' + endMonth : endMonth) +
									'-' + (endDate < 10 ? '0' + endDate : endDate);

								this.GoodsData.starttime_text = startTimeStr
								this.GoodsData.endtime_text = endTimeStr
							}

							let timeDiff = endtime.getTime() - starttime.getTime()
							this.diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1
						}
						if (this.GoodsData.lease_type == 4) {
							this.FenApi()
						}
						this.GoodsData.back_image = this.GoodsData.back_image
							? this.GoodsData.back_image.split(',').map(image => image.trim()).filter(Boolean)
							: []
					},
				})
			},
			//点击了电话
			ClickPhone() {
				if (!this.GoodsData.merchant_mobile) {
					uni.showToast({ title: '门店电话暂未配置', icon: 'none' })
					return
				}
				uni.makePhoneCall({
					phoneNumber: String(this.GoodsData.merchant_mobile)
				});
			},
			//点击了地图
			ClickMap() {
				const latitude = Number(this.GoodsData.lat)
				const longitude = Number(this.GoodsData.lng)
				if (!latitude || !longitude) {
					uni.showToast({ title: '门店位置暂未配置', icon: 'none' })
					return
				}
				const All = `${this.GoodsData.merchant_address}`
				uni.openLocation({
					latitude,
					longitude,
					name: this.GoodsData.merchant_name, // 目标位置的名称
					address: All, // 目标位置的地址
					scale: 18 // 地图缩放级别，范围1-28
				});
			},
			//预览图片
			ClickYL() {
				console.log('点击了预览图片');
				var SrcImage = this.YM + this.GoodsData.service_image;
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
			//去还车
			ClickHC() {
				uni.navigateTo({
					url: '/pages/OrderChild/OrderHuanChe'
				})
			},
			//分期付款记录
			ClickFQFK() {
				console.log('分期付款记录');
				this.show1 = true
			},
			//分期付款
			ClickPayFenQi() {
				if (this.installmentPaying) return
				if (!this.GoodsData.should_id) {
					uni.showToast({ title: '当前没有待支付分期', icon: 'none' })
					return
				}
				this.installmentPaying = true
				let data = {
					order_number: this.GoodsData.number,
					should_id: this.GoodsData.should_id
				}
				uni.request({
					url: this.YM + '/api/order/installment',
					method: 'POST',
					header:{
						'token': uni.getStorageSync('token'),
					},
					data,
					success: res => {
						console.log(res, '分期付款当前期数');
						if (res.data.code == 1) {
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
										url: '/pages/success/successPlayCG'
									})
								},
								fail: function(err) {
									uni.showToast({
										title: '支付失败！',
										icon: 'error'
									})
								},
								complete: () => {
									this.installmentPaying = false
								}
							});
						} else {
							this.installmentPaying = false
							uni.showToast({
								title: res.data.msg,
								icon: 'error'
							})
							return
						}
					},
					fail: () => {
						this.installmentPaying = false
						uni.showToast({ title: '支付请求失败，请重试', icon: 'none' })
					},
				})
			},
			//点击了立即支付
			ClickPay() {
				console.log('立即支付');
				// 1租车 2买车 3维修服务
				if (this.GoodsData.type == 1) {
					let data = {
						order_number: this.GoodsData.number
					}
					uni.request({
						url: this.YM + '/api/order/againLeaseCarPay',
						method: 'POST',
						header:{
							'token': uni.getStorageSync('token'),
						},
						data,
						success: res => {
							console.log(res, '租车订单再次支付');
							if (res.data.code == 1) {
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
											url: '/pages/success/successPlayCG'
										})
									},
									fail: function(err) {
										uni.showToast({
											title: '支付失败！',
											icon: 'error'
										})
									}
								});

							} else {
								uni.showToast({
									title: res.data.msg,
									icon: 'none',
									duration: 2000
								});
								return false
							}
						},
					})
				} else if (this.GoodsData.type == 2) {
					let Maiche = {
						order_number: this.GoodsData.number
					}
					uni.request({
						url: this.YM + '/api/order/againBuyCarPay',
						method: 'POST',
						header:{
							'token': uni.getStorageSync('token'),
						},
						data:Maiche,
						success: res => {
							console.log(res, '购车订单再次支付');
							if (res.data.code == 1) {
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
											url: '/pages/success/successPlayCG'
										})
									},
									fail: function(err) {
										uni.showToast({
											title: '支付失败！',
											icon: 'error'
										})
									}
								});
							} else {
								uni.showToast({
									title: res.data.msg,
									icon: 'none',
									duration: 2000
								});
								return false
							}
						},
					})
				} else if (this.GoodsData.type == 3) {
					let weixiu = {
						order_number: this.GoodsData.number
					}
					uni.request({
						url: this.YM + '/api/order/againMaintainPay',
						method: 'POST',
						header:{
							'token': uni.getStorageSync('token'),
						},
						data:weixiu,
						success: res => {
							console.log(res, '维修订单再次支付');
							if (res.data.code == 1) {
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
											url: '/pages/success/successPlayCG'
										})
									},
									fail: function(err) {
										uni.showToast({
											title: '支付失败！',
											icon: 'error'
										})
									}
								});
							} else {
								uni.showToast({
									title: res.data.msg,
									icon: 'none',
									duration: 2000
								});
								return false
							}
						},
					})
				} else if (this.GoodsData.lease_type == 4) {
					let FQplay = {
						order_number: this.GoodsData.number
						// should_id:uni.getStorageSync('should_id')
					}
					uni.request({
						url: this.YM + '/api/order/againLeaseCarPay',
						method: 'POST',
						header:{
							'token': uni.getStorageSync('token'),
						},
						data:FQplay,
						success: res => {
							console.log(res, '分期付款当前期数');
							if (res.data.code == 1) {
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
											url: '/pages/success/successPlayCG'
										})
									},
									fail: function(err) {
										uni.showToast({
											title: '支付失败！',
											icon: 'error'
										})
									}
								});
							} else {
								uni.showToast({
									title: res.data.msg,
									icon: 'error'
								})
								return
							}
						},
					})
				}
				// let Price=this.GoodsData.price + Number(this.GoodsData.cash_price)
				// uni.setStorageSync('PlayPrice',Price)
				// uni.setStorageSync('order_number',this.GoodsData.number)

				// uni.navigateTo({
				// 	url: '/pages/OrderChild/OrderPlay'
				// })
			},
			//点击了取消订单
			ClickQXorder() {
				console.log('取消订单');
				this.show = true

			},
			ClickQDQX() {
				if (this.cancelling) return
				this.show = false
				this.cancelling = true
				let XQ = {
					order_number: this.GoodsData.number
				}
				uni.request({
					url: this.YM + '/api/order/cancel',
					method: 'POST',
					header:{
						'token': uni.getStorageSync('token'),
					},
					data:XQ,
					success: res => {
						this.cancelling = false
						console.log(res, '取消订单');
						if (res.data.code == 1) {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 2000
							});
							this.XQapi()
						} else {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 2000
							});
							return false
						}
					},
					fail: () => {
						this.cancelling = false
						uni.showToast({
							title: '取消失败，请重试',
							icon: 'none'
						})
					},
				})
			},
			ClickZBQX() {
				this.show = false
			},
			open() {
				// console.log('open');
			},
			close() {
				this.show = false
				this.show1 = false
				// console.log('close');
			}
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
		padding-top: 50rpx;
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
		/* height: 100rpx; */
		/* background-color: darkseagreen; */
		margin-top: 20rpx;
		display: flex;
		align-items: center;
		justify-content: space-around;
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
		margin-right: 5rpx;
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
	.right{
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
		height: 105rpx;
		margin: 0 auto;
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
	}

	.Order_XX_3_taox {
		width: 630rpx;
		height: 110rpx;
		/* background-color: aqua; */
		margin: 0 auto;
		display: flex;
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
		height: 60%;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		text-align: right;
	}

	.Order_XX_3_taox_R2 {
		width: 100%;
		height: 40%;
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
		color: #55ff7f;
		text-align: center;
		line-height: 40rpx;
	}

	.Order_XX_4 {
		width: 650rpx;
		height: 200rpx;
		/* background-color: aqua; */
		margin: 0 auto;
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
</style>
