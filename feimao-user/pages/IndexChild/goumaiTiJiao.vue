<template>
	<view class="goumai">

		<!-- 1 -->
		<view class="goumai_oen">
			<view class="wxtj_oen_box">
				<view class="wxtj_oen_box_image">
					<view class="wxtj_oen_box_image_src">
						<image :src="YM + Datar.item.car.image[0]" class="imageBFB"></image>
					</view>
					<view class="wxtj_oen_box_image_text">
						<view class="wxtj_oen_box_image_text_T">
							<text>{{Datar.item.car.name}}</text>
							<text style="color: #E64638;font-size: 33rpx;"><text
									style="color: #E64638;font-size: 28rpx;">￥</text>{{Datar.item.price}}</text>
						</view>
						<view class="wxtj_oen_box_image_text_B">车辆品牌：{{Datar.item.car.specification}}</view>
					</view>
				</view>
				<view class="wxtj_oen_box_Num">
					<view class="wxtj_oen_box_Num_L">数量</view>
					<view class="wxtj_oen_box_Num_R">
						<view class="Jian" @click="ClickJian" :class="showis==true?'Jia':'Jian'">-</view>
						<view class="Num">{{Num}}</view>
						<view class="Jian" @click="ClickJia" :class="showis==true?'Jian':'Jia'">+</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 2 -->
		<view class="goumai_two">

			<view class="goumai_two_qc">
				<view class="goumai_two_qc_titkle">取车方式</view>
				<view class="goumai_two_qc_box">

					<view :class="item.isA?'goumai_two_qc_box_A':'goumai_two_qc_box_A1'" v-for="(item,index) in OueData"
						:key="index" @click="RadioClick(item)" v-if="Datar.item.merchant.pick_type==1">
						<view class="goumai_two_qc_box_A_L">
							<view class="goumai_two_qc_box_A_L_title">{{item.title}}</view>
							<view class="goumai_two_qc_box_A_L_text">{{item.text}}</view>

						</view>
						<view class="goumai_two_qc_box_A_R">
							<view class="goumai_two_qc_box_A_R_radio">
								<radio :value="value" :checked="item.isA" @click="RadioClick(item)" class="RadioCss"
									color='#ED9939' />
							</view>
						</view>
					</view>

					<view :class="item.isA?'goumai_two_qc_box_A':'goumai_two_qc_box_A1'"
						v-for="(item,index) in OueData1" :key="index" @click="RadioClick(item)"
						v-if="Datar.item.merchant.pick_type==2">
						<view class="goumai_two_qc_box_A_L">
							<view class="goumai_two_qc_box_A_L_title">{{item.title}}</view>
							<view class="goumai_two_qc_box_A_L_text">{{item.text}}</view>

						</view>
						<view class="goumai_two_qc_box_A_R">
							<view class="goumai_two_qc_box_A_R_radio">
								<radio :value="value" :checked="item.isA" @click="RadioClick(item)" class="RadioCss"
									color='#ED9939' />
							</view>
						</view>
					</view>

				</view>

				<view class="goumai_two_qc_dz" v-if="addshow==true" @click="ClickLinkdz">
					<view class="goumai_two_qc_dz_icon">
						<view class="goumai_two_qc_dz_icon_src">
							<image src="../../static/Hdinwei.png" class="imageBFB"></image>
						</view>
					</view>
					<view class="goumai_two_qc_dz_text">
						<view class="goumai_two_qc_dz_text_D" v-if="DIZ">{{DIZ.administrative}}{{DIZ.address}}</view>
						<view class="goumai_two_qc_dz_text_N" v-if="DIZ">{{DIZ.name}} {{DIZ.mobile}}</view>
						<view class="goumai_two_qc_dz_text_D" v-if="!DIZ">请选择地址</view>
						<view class="goumai_two_qc_dz_text_N" v-if="!DIZ">请选择收货人</view>
					</view>
					<view class="goumai_two_qc_dz_Y">
						<image src="../../static/wordF.png" class="imageBFB"></image>
					</view>
				</view>
			</view>

			<view class="wxtj_two_xx">
				<view class="wxtj_two_xx_box">
					<view class="wxtj_two_xx_box_L">开始日期</view>
					<view class="wxtj_two_xx_box_R" @click="ClickDate">
						<text v-if="time==''" style="color: #ccc;">请选择</text>
						<text v-else>{{time}}</text>
						<view class="wxtj_two_xx_box_R_icon">
							<image src="../../static/wordF.png" class="imageBFB"></image>
						</view>
					</view>
				</view>

				<view class="wxtj_two_xx_box">
					<view class="wxtj_two_xx_box_L">姓名</view>
					<view class="wxtj_two_xx_box_R">
						<u--input placeholder="请输入您的姓名" border="false" v-model="valuename"
							inputAlign='right'></u--input>
					</view>
				</view>

				<view class="wxtj_two_xx_box">
					<view class="wxtj_two_xx_box_L">手机号</view>
					<view class="wxtj_two_xx_box_R">
						<u--input placeholder="请输入您的手机号" border="false" v-model="valuephone"
							inputAlign='right'></u--input>
					</view>
				</view>
			</view>

			<view class="wxtj_two_yhj" @click="ClickYHJ">
				<view class="wxtj_two_yhj_L">
					<view class="wxtj_two_yhj_L_icon">
						<image src="../../static/YHJicon.png" class="imageBFB"></image>
					</view>
					<text>优惠券</text>
				</view>
				<view class="wxtj_two_yhj_R">
					<text v-if="TextR==''">暂无可用优惠券</text>
					<text>{{TextR}}</text>
					<view class="wxtj_two_yhj_R_icon">
						<image src="../../static/wordF.png" class="imageBFB"></image>
					</view>
				</view>
			</view>

			<!-- 按钮 -->
			<view class="wxtj_btn">
				<view class="wxtj_btn_L">
					<view class="wxtj_btn_L_Pirce">
						<text style="font-size: 28rpx;">￥</text>
						<text>{{Math.round(PriceZ * 100) / 100}}</text>
					</view>
					<view class="wxtj_btn_L_Mxi" @click='dwadawd'>
						<text>价格明细</text>
						<view class="wxtj_btn_L_Mxi_icon">
							<image src="../../static/MXicon.png" class="imageBFB"></image>
						</view>
					</view>
				</view>
				<view class="wxtj_btn_R" @click="ClickTiJAIo">提交订单</view>
			</view>

		</view>
		<!-- 日期 -->
		<u-picker :show="show" ref="uPicker" :columns="columns" @confirm="confirm" :title='title' @close='close'
			@cancel='cancel'></u-picker>

		<!-- 优惠券弹出 -->
		<u-popup :show="showYHJ" :round="10" mode="bottom" @close="close" @open="open" :closeable='true'>
			<view class="YHJ">
				<view class="YHJ_title">优惠券</view>
				<view class="YHJ_list">
					<view class="QD_Two_list" v-for="(item,index) in YHJdata" :key="index">
						<view class="QD_Two_list_icon">
							<image src="../../static/YHJBJ.png" class="imageBFB"></image>
						</view>

						<view class="QD_Two_list_price">
							<view class="QD_Two_list_price_price">
								<text>{{item.reduceprice}}</text>
								<text style="font-size: 26rpx;font-weight: 500;">元</text>
							</view>
							<view class="QD_Two_list_price_text">满减券</view>
						</view>

						<view class="QD_Two_list_right">
							<view class="QD_Two_list_right_L">
								<view class="QD_Two_list_right_L_t">满{{item.price}}减{{item.reduceprice}}</view>
								<view class="QD_Two_list_right_L_b">{{item.endtime_text}}</view>
							</view>
							<view class="QD_Two_list_right_R">
								<view class="QD_Two_list_right_R_b1" @click="ClickLjD(item)" v-if="Num * GouChedata.item.price>=item.price">使用
								</view>
								<view class="QD_Two_list_right_R_bAR" v-else>无法使用</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</u-popup>
		
		<!-- 明细 -->
		<u-popup :show="showMxi" :round="10" mode="bottom" @close="close" @open="open" :closeable='true'>
			<view class="Mxi">
				<view class="Mxi_titile">价格明细</view>
				<view class="Mxi_num">{{time}}</view>
				<view class="Mxi_text">{{textQC}}</view>
				<view class="Mxi_box">
					<view class="Mxi_box_list">
						<view class="Mxi_box_list_L">在线支付</view>
						<view class="Mxi_box_list_R">
							<text style="font-size: 28rpx;">￥</text>
							<text>{{PriceZ}}</text>
						</view>
					</view>
					<view class="Mxi_box_list">
						<view class="Mxi_box_list_L">车辆总价</view>
						<view class="Mxi_box_list_R">
							<text style="font-size: 28rpx;color: #333333;">￥</text>
							<text style="font-size: 32rpx;color: #333333;">{{PriceZ}}</text>
						</view>
					</view>

					<view class="Mxi_box_list">
						<view class="Mxi_box_list_L">优惠</view>
						<view class="Mxi_box_list_R">
							<text style="color: #FF7359;font-size: 28rpx;font-weight: 500;">
								<text v-if="TextR!==''">减￥{{TextR}}</text>
								<text v-else>暂未选择优惠券</text>
							</text>
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
				showis: null,
				showYHJ: false,
				showMxi:false,
				Num: 1, //数量
				value: '', //N
				valuename: '', //姓名
				valuephone: '', //手机号
				title: '提车时间',
				show: false,
				time: '', //选择的时间 回显
				timestamp: '', //选择的时间 传递后台
				columns: [
					[],
					[]
				],
				addshow: false,
				OueData: [{
					title: '到店取车',
					text: '前往门店自取',
					isA: false,
				}],
				OueData1: [{
						title: '到店取车',
						text: '前往门店自取',
						isA: false,
					},
					{
						title: '送车上门',
						text: '为您送车上门',
						isA: false,
					}
				],
				// 优惠券
				YHJdata: [],
				Datar: '',
				addradw: '',
				DIZ: '',
				TypeSeleye:'',
				GouChedata: '',
				YHJ: '', //使用的优惠券金额
				PriceZ: 0, //总价
				TextR:'',
				clickTimer: null,
				textQC:'请选择',
				isSubmitting: false
			}
		},
		onLoad() {
			uni.setNavigationBarColor({
				frontColor: "#000000", // 前景颜色，即字体颜色
				backgroundColor: "#ffd0b9", // 背景颜色
			});
		},
		created() {
			this.FengZDate()
		},
			onShow() {
			this.Datar = uni.getStorageSync('GouCheData')

			this.Datar.item.car.image = this.Datar.item.car.image.split(',');

			console.log(this.Datar, '携带的数据');

			this.GouChedata = uni.getStorageSync('GouCheData')
			console.log(this.GouChedata,'获取的数据');
			this.PriceZ = this.GouChedata.item.price
			this.DIZ = uni.getStorageSync('addressitem') || ''
			this.addradw = this.DIZ ? `${this.DIZ.administrative}${this.DIZ.address}` : ''
			

			this.YHJLISTAPI()
		},
		methods: {
			dwadawd(){
				this.showMxi=true
			},
			//点击了优惠券的立即兑换
			ClickLjD(item) {
				const orderAmount = Number(this.Num) * Number(this.GouChedata.item.price)
				if (orderAmount < Number(item.price)) {
					uni.showToast({ title: '订单金额未达到优惠券门槛', icon: 'none' })
					return
				}
				console.log('选择的优惠券', item);
				this.YHJ = Number(item.coupon_id)

				this.TextR = Number(item.reduceprice)


				this.PriceZ = Number(this.Num) * Number(this.GouChedata.item.price) - Number(this.TextR)
				this.PriceZ = this.PriceZ.toFixed(2)

				this.showYHJ = false
			},
			// 优惠券列表
			YHJLISTAPI() {
				uni.request({
					url: this.YM + '/api/user/couponList',
					method: 'POST',
					header:{
						'token': uni.getStorageSync('token')
					},
					success: res => {
						this.YHJdata = res.data.code == 1 && Array.isArray(res.data.data)
							? res.data.data
							: []
						if (res.data.code != 1) {
							uni.showToast({ title: res.data.msg || '优惠券加载失败', icon: 'none' })
						}
						// this.YHJdata.forEach(item => {
						// 	item.price = parseFloat(item.price).toFixed(0);
						// 	item.reduceprice = parseFloat(item.reduceprice).toFixed(0);
						// });
						console.log(res.data.data, '优惠券列表');
					},
					fail: () => {
						this.YHJdata = []
						uni.showToast({ title: '优惠券加载失败，请稍后重试', icon: 'none' })
					}
				})
			},
			//跳转选择地址
			ClickLinkdz() {
				console.log('跳转选择地址');
				uni.setStorageSync('Ari', 'Order')
				uni.navigateTo({
					url: '/pages/IndexChild/seleteDZ'
				})
			},
			//点击了优惠券
			ClickYHJ() {
				console.log('点击了优惠券');
				this.showYHJ = true
			},
			RadioClick(item) {
				console.log(item, 'item');
				for (var i = 0; i < this.OueData.length; i++) {
					this.OueData[i].isA = false
				}
				for (var i = 0; i < this.OueData1.length; i++) {
					this.OueData1[i].isA = false
				}
				item.isA = true

				if (item.title == '送车上门') {
					this.addshow = true
					this.TypeSeleye = 3
					this.textQC='同城配送'
					this.title='送车时间'
				} else {
					this.addshow = false
					this.TypeSeleye = 1
					this.textQC='到店取车'
					this.title='提车时间'
				}
			},
			//点击了提交订单
			ClickTiJAIo() {
				if (this.isSubmitting) return
				clearTimeout(this.clickTimer);
				// 设置新的定时器
				this.clickTimer = setTimeout(() => {
				if(this.TypeSeleye==''){
					uni.showToast({
						title: '请选择取车方式',
						icon: 'none',
						duration: 2000
					});
					return false
				}else if (this.timestamp == '') {
					uni.showToast({
						title: '请选择提车时间',
						icon: 'none',
						duration: 2000
					});
					return false
				} else if (this.TypeSeleye == 3 && !this.DIZ) {
					uni.showToast({ title: '请选择送车地址', icon: 'none' })
					return false
				} else if (this.valuename == '') {
					uni.showToast({
						title: '请填写姓名',
						icon: 'none',
						duration: 2000
					});
					return false
				} else if (this.valuephone == '') {
					uni.showToast({
						title: '请填写手机号',
						icon: 'none',
						duration: 2000
					});
					return false
				} else if (!/^1[3-9]\d{9}$/.test(this.valuephone)) {
					uni.showToast({ title: '手机号格式错误', icon: 'none' })
					return false
				}else{
					
				let data = {
					merchant_car_id: uni.getStorageSync('merchat_car_id'),
					coupon_id:this.YHJ,
					num: this.Num,
					take_back_time: this.timestamp,
					name: this.valuename,
					mobile: this.valuephone,
					pick_type: this.TypeSeleye,
					pick_address: this.addradw ,//取车地址
					service_image: this.Datar.item.car.image[0] //主图
				}
				this.isSubmitting = true
				uni.request({
					url: this.YM + '/api/order/buyCarPay',
					method: 'POST',
					data,
					header:{
						'token': uni.getStorageSync('token'),
					},
					success: res => {
						this.isSubmitting = false
						console.log(res, '提交购车订单');
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
										url: '/pages/success/successTiJiao'
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
								icon: 'none',
								duration: 2000
							});
							return false
						}
					},
					fail: () => {
						this.isSubmitting = false
						uni.showToast({ title: '订单提交失败，请稍后重试', icon: 'none' })
					}
				})
				}
				}, 500); // 设置延迟时间，单位为毫秒
			},

			confirm(e) {
				console.log('confirm', e);
				const dateStr = e.value[0].split(' ')[0]; // 提取日期字符串，格式为：10月2日
				const timeStr = e.value[1].replace('点', ''); // 提取时间字符串，格式为：11点
				const month = parseInt(dateStr.split('月')[0]); // 提取月份，转换为数字
				const day = parseInt(dateStr.split('月')[1].replace('日', '')); // 提取日期，转换为数字
				const hour = parseInt(timeStr); // 提取小时，转换为数字

				const selectedDate = new Date();
				selectedDate.setMonth(month - 1); // 月份需要减1，因为月份从0开始计数
				selectedDate.setDate(day);
				selectedDate.setHours(hour);
				selectedDate.setMinutes(0);
				selectedDate.setSeconds(0);
				selectedDate.setMilliseconds(0);

				this.timestamp = Math.floor(selectedDate.getTime() / 1000);
				console.log(this.timestamp, '时间戳');



				const date = e.value[0].split(' ')[0];
				this.time = `${date} ${e.value[1]}`
				console.log(this.time, '选择的时间');
				this.show = false;
			},
			//点击了日期
			ClickDate() {
				console.log('点击了日期');
				this.show = true
			},
			// 封装好的方法 选择时间
			FengZDate() {
				// 获取当前日期
				const currentDate = new Date();
				let currentMonth = currentDate.getMonth() + 1; // 月份从0开始，需要加1
				let currentDay = currentDate.getDate();

				const result = [];

				for (let i = 0; i < 3; i++) {
					const nextMonth = currentMonth + i;
					const nextYear = currentDate.getFullYear();
					const daysInNextMonth = new Date(nextYear, nextMonth, 0).getDate();


					for (let j = currentDay; j <= daysInNextMonth; j++) {
						const nextDate = new Date(nextYear, nextMonth - 1, j);
						const nextMonthStr = nextDate.getMonth() + 1;
						const nextDayStr = nextDate.getDate();
						// const nextWeekday = nextDate.toLocaleDateString('zh-CN', {
						// 	weekday: 'short'
						// });

						// result.push(`${nextMonthStr}月${nextDayStr}日 ${nextWeekday}`);
						result.push(`${nextMonthStr}月${nextDayStr}日`);
					}

					// 重置当前日期为1号
					currentDay = 1;
				}

				this.columns[0] = result
				// console.log(this.columns[0],'this.columns[0]');

				// 0-23点  小时
				for (let i = 0; i <= 23; i++) {
					// console.log(i);
					this.columns[1].push(i + '点')
				}
			},
			close() {
				this.show = false;
				this.showYHJ = false
				this.showMxi=false
			},
			open() {
				// console.log('open');
			},
			cancel() {
				this.show = false;
				
			},
			//点击了减
			ClickJian() {
				console.log('点击了减');
				this.showis = true
				if (this.Num == 1) {
					uni.showToast({
						title: '数量不能小于1',
						icon: 'none',
						duration: 2000
					});
					return false
				} else {
					this.Num--
					this.YHJ = ''
					this.TextR = ''
					this.PriceZ = Number(this.Num) * Number(this.GouChedata.item.price)
				}
			},
			//点击了加
			ClickJia() {
				console.log('点击了加');
				this.showis = false
				this.Num++
				this.YHJ = ''
				this.TextR = ''
				this.PriceZ = Number(this.Num) * Number(this.GouChedata.item.price)
			}
		}
	}
</script>

<style>
	.goumai {
		width: 100vw;
		height: 100vh;
		background-color: pink;
	}

	/* 1 */
	.goumai_oen {
		width: 100%;
		height: 300rpx;
		background: linear-gradient(0deg, #F7F7F7 0%, #FBEBE0 51%, #FFCFB7 100%);
		float: left;
	}

	.wxtj_oen_box {
		width: 696rpx;
		height: 228rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 16rpx;
		margin: 30rpx auto 0;
		padding-top: 20rpx;
	}

	.wxtj_oen_box_image {
		width: 90%;
		height: 120rpx;
		/* background-color: yellow; */
		margin: 0 auto;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.wxtj_oen_box_image_src {
		width: 148rpx;
		height: 108rpx;
		background: #C5C5C5;
		border-radius: 12rpx;
		overflow: hidden;
	}

	.wxtj_oen_box_image_text {
		width: 450rpx;
		height: 108rpx;
		/* background-color: antiquewhite; */
		margin-left: 15rpx;
	}

	.wxtj_oen_box_image_text_T {
		width: 100%;
		height: 50%;
		/* background-color: yellow; */
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.wxtj_oen_box_image_text_B {
		width: 100%;
		height: 50%;
		/* background-color: skyblue; */
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.wxtj_oen_box_Num {
		width: 90%;
		height: 70rpx;
		/* background-color: salmon; */
		margin: 18rpx auto 0;
		display: flex;
	}

	.wxtj_oen_box_Num_L,
	.wxtj_oen_box_Num_R {
		width: 40%;
		height: 100%;
		/* background-color: floralwhite; */
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		line-height: 70rpx;
		text-indent: 0.2em;
	}

	.wxtj_oen_box_Num_R {
		width: 60%;
		height: 100%;
		/* background-color: skyblue; */
		display: flex;
		align-items: center;
		justify-content: flex-end;
	}

	.Jian,
	.Jia {
		width: 44rpx;
		height: 44rpx;
		background: #FFFFFF;
		border: 2rpx solid #CFCFCF;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #A9A9A9;
		text-align: center;
		line-height: 44rpx;
	}

	.Jia {
		background: #FFE8CD;
		border: 2rpx solid #ED9939;
		color: #ED9939;
	}

	.Num {
		min-width: 60rpx;
		height: 44rpx;
		text-align: center;
		line-height: 44rpx;
		font-size: 30rpx;
		font-family: DIN;
		font-weight: bold;
		color: #333333;
	}

	/* 2 */
	.goumai_two {
		width: 100%;
		min-height: calc(100vh - 300rpx);
		background-color: #F7F7F7;
		float: left;
	}

	.wxtj_two_xx {
		width: 696rpx;
		height: 332rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 16rpx;
		margin: 20rpx auto 0;
		padding-top: 10rpx;
	}

	.wxtj_two_xx_box {
		width: 90%;
		height: 80rpx;
		/* background-color: cornflowerblue; */
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.wxtj_two_xx_box_L {
		width: 40%;
		height: 100%;
		line-height: 80rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		/* background-color: red; */
	}

	.wxtj_two_xx_box_R {
		width: 60%;
		height: 100%;
		/* background-color: darkseagreen; */
		display: flex;
		align-items: center;
		justify-content: flex-end;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #333333;
	}

	.wxtj_two_xx_box_R_icon {
		width: 12rpx;
		height: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 10rpx;
		margin-right: 10rpx;
	}

	.wxtj_two_yhj {
		width: 696rpx;
		height: 100rpx;
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin: 30rpx auto 0;
	}

	.wxtj_two_yhj_L,
	.wxtj_two_yhj_R {
		width: 45%;
		height: 100%;
		/* background-color: red; */
		display: flex;
		align-items: center;
		justify-content: flex-start;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
	}

	.wxtj_two_yhj_R {
		/* background-color: hotpink; */
		justify-content: flex-end;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #9C9BA2;
	}

	.wxtj_two_yhj_L_icon {
		width: 35rpx;
		height: 34rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 15rpx;
	}

	.wxtj_two_yhj_R_icon {
		width: 12rpx;
		height: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 10rpx;
	}


	.wxtj_btn {
		width: 100%;
		height: 142rpx;
		position: fixed;
		bottom: 0;
		left: 0;
		background-color: #FFFFFF;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.wxtj_btn_L {
		width: 300rpx;
		height: 88rpx;
		display: flex;
		align-items: center;
		margin-left: 51rpx;
		/* background-color: skyblue; */
	}

	.wxtj_btn_L_Pirce {
		/* width: 150rpx; */
		height: 100%;
		font-size: 46rpx;
		font-family: DIN;
		font-weight: bold;
		line-height: 88rpx;
		color: #E64638;
	}

	.wxtj_btn_L_Mxi {
		width: 150rpx;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #A1A1A1;
	}

	.wxtj_btn_L_Mxi_icon {
		width: 18rpx;
		height: 12rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 10rpx;
	}

	.wxtj_btn_R {
		width: 295rpx;
		height: 88rpx;
		background: #E24E11;
		border-radius: 44rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #FFFFFF;
		margin-right: 46rpx;
		text-align: center;
		line-height: 88rpx;
	}


	.goumai_two_qc {
		width: 696rpx;
		/* height: 260rpx; */
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 16rpx;
		margin: 20rpx auto 0;
		padding-bottom: 20rpx;
	}

	.goumai_two_qc_titkle {
		width: 90%;
		height: 85rpx;
		line-height: 85rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		margin: 0 auto;
		/* background-color: yellow; */
	}

	.goumai_two_qc_box {
		width: 90%;
		height: 150rpx;
		/* background-color: sandybrown; */
		margin: 8rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.goumai_two_qc_box_A,
	.goumai_two_qc_box_A1 {
		width: 300rpx;
		height: 150rpx;
		background: #FFFCF9;
		border: 2rpx solid #ED9939;
		border-radius: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.goumai_two_qc_box_A1 {
		background: #FFFFFF;
		border: 2rpx solid #DDDDDE;
	}

	.goumai_two_qc_box_A_L {
		width: 200rpx;
		height: 100rpx;
		/* background-color: thistle; */
		margin-right: 18rpx;
	}

	.goumai_two_qc_box_A_L_title {
		width: 100%;
		height: 50%;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
	}

	.goumai_two_qc_box_A_L_text {
		width: 100%;
		height: 50%;
		font-size: 24rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.goumai_two_qc_box_A_R {
		width: 32rpx;
		height: 115rpx;
		display: flex;
		align-items: flex-end;
		justify-content: center;
	}

	.goumai_two_qc_box_A_R_radio {
		width: 32rpx;
		height: 32rpx;
		border-radius: 50%;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.RadioCss {
		transform: scale(0.8)
	}

	.goumai_two_qc_dz {
		width: 90%;
		height: 140rpx;
		/* background-color: greenyellow; */
		margin: 20rpx auto 0;
		border-top: 1rpx solid #F0F0F0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.goumai_two_qc_dz_icon {
		width: 30rpx;
		height: 80rpx;
		display: flex;
		align-items: flex-start;
		justify-content: center;
		/* background-color: yellow; */

	}

	.goumai_two_qc_dz_icon_src {
		width: 24rpx;
		height: 28rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.goumai_two_qc_dz_text {
		width: 570rpx;
		height: 100rpx;
		/* background-color: violet; */
	}

	.goumai_two_qc_dz_text_D {
		width: 100%;
		height: 60%;
		/* background-color: red; */
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		line-height: 50rpx;

	}

	.goumai_two_qc_dz_text_N {
		width: 100%;
		height: 40%;
		/* background-color: violet; */
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.goumai_two_qc_dz_Y {
		width: 12rpx;
		height: 18rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
	}

	/* 优惠券 */
	.YHJ {
		width: 100%;
		height: 600rpx;
		/* background-color: mediumpurple; */
		float: left;
		padding-bottom: 40rpx;
	}

	.YHJ_title {
		width: 100%;
		height: 60rpx;
		line-height: 60rpx;
		text-align: center;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #101010;
		margin-top: 20rpx;
	}

	.YHJ_list {
		width: 692rpx;
		/* height: 164rpx; */
		/* background-color: aqua; */
		/* overflow: hidden; */
		margin: 40rpx auto 0;
	}

	.YHJ_list_box {
		width: 100%;
		height: 164rpx;
		overflow: hidden;
		margin-bottom: 20rpx;
	}

	.QD_Two_list_box {
		width: 692rpx;
		/* height: 400rpx; */
		/* background-color: green; */
		margin: 15rpx auto 0;
		padding-bottom: 40rpx;
	}

	.QD_Two_list {
		width: 100%;
		height: 164rpx;
		/* background-color: green; */
		position: relative;
		margin-bottom: 20rpx;

	}

	.QD_Two_list_icon {
		width: 100%;
		height: 100%;
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		z-index: 1;
	}

	.QD_Two_list_price {
		width: 120rpx;
		height: 120rpx;
		/* background-color: violet; */
		position: absolute;
		left: 20rpx;
		top: 25rpx;
		z-index: 2;
	}

	.QD_Two_list_price_price {
		width: 100%;
		height: 70rpx;
		text-align: center;
		line-height: 70rpx;
		font-size: 54rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #FF6043;
	}

	.QD_Two_list_price_text {
		width: 100%;
		height: 30rpx;
		text-align: center;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FF6043;
	}

	.QD_Two_list_right {
		width: 480rpx;
		height: 120rpx;
		/* background-color: salmon; */
		position: absolute;
		z-index: 2;
		top: 25rpx;
		right: 20rpx;
		display: flex;
	}

	.QD_Two_list_right_L {
		width: 300rpx;
		height: 100%;
		/* background-color: yellow; */
	}

	.QD_Two_list_right_L_t {
		width: 100%;
		height: 50%;
		font-size: 32rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #101010;
		line-height: 60rpx;
	}

	.QD_Two_list_right_L_b {
		width: 100%;
		height: 50%;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		line-height: 60rpx;
	}

	.QD_Two_list_right_R {
		width: 160rpx;
		height: 100%;
		/* background-color: red; */
		margin-left: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.QD_Two_list_right_R_t {
		width: 100%;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #333333;
	}


	.QD_Two_list_right_R_b,
	.QD_Two_list_right_R_b1 {
		width: 160rpx;
		height: 60rpx;
		background: pink;
		box-shadow: 0rpx 3rpx 10rpx 0rpx rgba(255, 71, 35, 0.45);
		border-radius: 30rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FFFFFF;
		text-align: center;
		line-height: 60rpx;
	}

	.QD_Two_list_right_R_b1 {
		background: linear-gradient(-60deg, #FF4522, #FF671D);
	}
	.QD_Two_list_right_R_bAR {
		width: 200rpx;
		height: 60rpx;
		background-color: #C5C5C5;
		border-radius: 30rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FFFFFF;
		text-align: center;
		line-height: 60rpx;
	}
	.Mxi {
		width: 100%;
		height: 550rpx;
		/* background-color: #; */
		float: left;
	}
	
	.Mxi_titile {
		width: 100%;
		height: 70rpx;
		margin: 20rpx auto 0;
		text-align: center;
		line-height: 70rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #101010;
	}
	
	.Mxi_num {
		width: 100%;
		height: 50rpx;
		/* background-color: violet; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-top: 15rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #333333;
	}
	
	.Mxi_text {
		width: 100%;
		height: 50rpx;
		/* background-color: violet; */
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #333333;
	}
	
	.Mxi_box {
		width: 666rpx;
		height: 300rpx;
		/* background-color: cyan; */
		border-top: 2rpx solid #F0F0F0;
		margin: 30rpx auto 0;
	}
	
	.Mxi_box_list {
		width: 100%;
		height: 100rpx;
		/* background-color: yellow; */
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	
	.Mxi_box_list_L {
		width: 50%;
		height: 100%;
		/* background-color: skyblue; */
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		line-height: 100rpx;
	}
	
	.Mxi_box_list_R {
		width: 50%;
		height: 100%;
		/* background-color: mediumpurple; */
		display: flex;
		align-items: center;
		justify-content: flex-end;
		font-size: 40rpx;
		font-family: DIN;
		font-weight: bold;
		color: #E64638;
	}
</style>
