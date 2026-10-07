<template>
	<view class="goumai">

		<!-- 1 -->
		<view class="goumai_oen">
			<!-- 实名认证 -->
			<!-- 		<view class="goumai_oen_smrz" @click="Clicksmrz">
				<view class="goumai_oen_smrz_icon">
					<image src="../../static/Bdp.png" class="imageBFB"></image>
				</view>
				<view class="goumai_oen_smrz_text">为保障您的租车安全，请先进行实名认证</view>
				<view class="goumai_oen_smrz_Y">
					<image src="../../static/wordF.png" class="imageBFB"></image>
				</view>
			</view> -->

			<view class="wxtj_oen_box">
				<view class="wxtj_oen_box_image">
					<view class="wxtj_oen_box_image_src">
						<image :src="YM + ZuLData.item.car.image[0]" class="imageBFB"></image>
					</view>
					<view class="wxtj_oen_box_image_text">
						<view class="wxtj_oen_box_image_text_T">
							<text>{{ZuLData.item.car.name}}</text>
							<text style="color: #E64638;font-size: 33rpx;"><text
									style="color: #E64638;font-size: 28rpx;"></text></text>
						</view>
						<view class="wxtj_oen_box_image_text_B">车辆品牌：{{Odata.merchant_car.brand}}</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 2 -->
		<view class="goumai_two">

			<view class="goumai_two_qc">
				<view class="goumai_two_qc_titkle">取车方式</view>
				<view class="goumai_two_qc_box">

					<!-- Odata.merchant_car.pick_type -->

					<view v-for="(item, index) in OueData1" :key="index"
						:class="item.isA ? 'goumai_two_qc_box_A' : 'goumai_two_qc_box_A1'"
						v-if="!(Odata.merchant_car.pick_type === 1 && index === 1)" @click="RadioClick(item)">
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

				<view class="goumai_two_qc_dz" @click="ClickLinkdz" v-if="pick_type==3">
					<view class="goumai_two_qc_dz_icon">
						<view class="goumai_two_qc_dz_icon_src">
							<image src="../../static/Hdinwei.png" class="imageBFB"></image>
						</view>
					</view>
					<view class="goumai_two_qc_dz_text">
						<view class="goumai_two_qc_dz_text_D" v-if="DIZ!==''">{{DIZ.administrative}}{{DIZ.address}}
						</view>
						<view class="goumai_two_qc_dz_text_N" v-if="DIZ!==''">{{DIZ.name}} {{DIZ.mobile}}</view>
						<view class="goumai_two_qc_dz_text_D" v-if="DIZ==''">请选择地址</view>
						<view class="goumai_two_qc_dz_text_N" v-if="DIZ==''">请选择收货人</view>
					</view>
					<view class="goumai_two_qc_dz_Y">
						<image src="../../static/wordF.png" class="imageBFB"></image>
					</view>
				</view>
			</view>

			<view class="wxtj_two_xx">
				<view class="wxtj_two_xx_box">
					<view class="wxtj_two_xx_box_L" style="width: 100%;">
						<view style="width: 150rpx;height: 100%;display: flex;align-items: center;">租赁方式</view>
						<text style="font-size: 24rpx;color: #ED9939;" v-if="lease_type==3">注：按月收费，最长可选择1个月即30天</text>
						<text style="font-size: 24rpx;color: #ED9939;"
							v-if="lease_type==2">注：至当日24点为一天，可以跨天租赁，最长可选择30天，按天收费</text>
						<text style="font-size: 24rpx;color: #ED9939;" v-if="lease_type==1">注：选择开始时间和结束时间，按小时收费</text>
					</view>
				</view>

				<view class="wxtj_two_xx_box_kuai">
					<view class="wxtj_two_xx_box_kuai_B" v-for="(item,index) in zlData" :key="index"
						@click="Clickselete(item)">
						<view class="wxtj_two_xx_box_kuai_B_radio" v-if="item.isA">
							<image src="../../static/ZseletA.png" class="imageBFB"></image>
						</view>
						<view class="wxtj_two_xx_box_kuai_B_radio" v-else>
							<image src="../../static/ZseletB.png" class="imageBFB"></image>
						</view>
						<text :style="{color: item.isA ? 'black' : '#9C9BA2'}">{{item.title}}</text>
					</view>

				</view>

				<view class="wxtj_two_xx_box" v-if="pick_type==3" style="height: auto;">
					<view class="wxtj_two_xx_box_L" style="width: 25%;">送车上门</view>
					<view class="wxtj_two_xx_box_R"
						style="flex-wrap: wrap;width: 65%;justify-content: flex-end;">
						<view class="wxtj_two_xx_box_kuai_B"
							style="width: 100%;margin-left: 20rpx;justify-content: flex-end;"
							v-for="(item,index) in CseData" :key="index" @click="Cs(item)">
							<view class="wxtj_two_xx_box_kuai_B_radio" v-if="item.isA">
								<image src="../../static/ZseletA.png" class="imageBFB"></image>
							</view>
							<view class="wxtj_two_xx_box_kuai_B_radio" v-else>
								<image src="../../static/ZseletB.png" class="imageBFB"></image>
							</view>
							<!-- <text :style="{color: item.isA ? 'black' : '#9C9BA2'}">{{item.title}}</text> -->
							<view class="wxtj_two_xx_box_kuai_B_text" :style="{color: item.isA ? 'black' : '#9C9BA2'}">
								{{item.title}}</view>
						</view>
					</view>
				</view>

				<view class="wxtj_two_xx_box" style="height: auto;color:orange;font-size: 26rpx;" v-if="pick_type==3">注:{{Odata.service_money}}</view>

				<!-- 月租开始时间 -->
				<view class="wxtj_two_xx_box" @click="startDate">
					<view class="wxtj_two_xx_box_L">租赁开始时间</view>
					<view class="wxtj_two_xx_box_R">
						<text v-if="starttime==''" style="color: #ccc;">请选择</text>
						<text v-else>{{starttime}}</text>
						<view class="wxtj_two_xx_box_R_icon">
							<image src="../../static/wordF.png" class="imageBFB"></image>
						</view>
					</view>
				</view>

				<view class="wxtj_two_xx_box" @click="endDate">
					<view class="wxtj_two_xx_box_L">租赁结束时间</view>
					<view class="wxtj_two_xx_box_R">
						<text v-if="endtime_text==''" style="color: #ccc;">请选择</text>
						<text v-else>{{endtime}}</text>
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

				<view class="wxtj_two_xx_box">
					<view class="wxtj_two_xx_box_L">身份证号</view>
					<view class="wxtj_two_xx_box_R">
						<u--input placeholder="请输入您的身份证号" border="false" v-model="valueid"
							inputAlign='right'></u--input>
					</view>
				</view>

				<!-- <view class="wxtj_two_xx_box" @click="desireDate">
					<view class="wxtj_two_xx_box_L">期望取车时间</view>
					<view class="wxtj_two_xx_box_R" @click="desireDate">
						<text v-if="take_back_time==''" style="color: #ccc;">请选择</text>
						<text v-else>{{take_back_time}}</text>
						<view class="wxtj_two_xx_box_R_icon">
							<image src="../../static/wordF.png" class="imageBFB"></image>
						</view>
					</view>
				</view> -->

				<view class="wxtj_two_xx_box">
					<view class="wxtj_two_xx_box_L">
						<text>租车费用</text>
						<text v-if="zlData[0].isA">/天</text>
						<text v-if="zlData[1].isA">/时</text>
						<text v-if="zlData[2].isA">/月</text>
					</view>
					<view class="wxtj_two_xx_box_R">{{Odata.lease_price}}元</view>
				</view>

				<view class="wxtj_two_xx_box">
					<view class="wxtj_two_xx_box_L">
						<text>服务费用/天/人</text>
					</view>
					<view class="wxtj_two_xx_box_R">{{Odata.insure_price}}元</view>
				</view>
				<view class="erkai_cb" v-for="(item,index) in guest" :key="index" v-if="guest.length!==0">
					<view class="erkai_cb_one">
						<text>承保人</text>
						<view class="erkai_cb_btn_icon" style="margin-right: 0;width: 42rpx;height: 42rpx;"
							@click="CguestDelete(item)">
							<image src="../../static/SC.png" class="imageBFB"></image>
						</view>
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

				<view class="erkai_cb_btn" @click="Ctj">
					<view class="erkai_cb_btn_icon">
						<image src="../../static/TJ.png" class="imageBFB"></image>
					</view>
					添加乘坐人
				</view>
				
				<view class="tekai_tis">提示：姓名、身份证号请再次核对，若信息有误，无法提供保险保障。</view>

			</view>



			<view class="wxtj_two_gs">
				<view class="wxtj_two_gs_icon">
					<radio :value="value" :checked="checked" @click="Radiochecked" class="RadioCss" color='#0D7FFF' />
				</view>
				<text>我已阅读并同意<text style="color: #0D7FFF;"><text @click.stop="ClickXY">《投保授权书》</text><text
							@click.stop="ClickZL">《租赁协议》</text></text></text>
			</view>

			<view style="width: 100%;height: 165rpx;background-color: transparent;"></view>

			<!-- 按钮 -->
			<view class="wxtj_btn">
				<view class="wxtj_btn_L">
					<view class="wxtj_btn_L_Pirce">
						<text style="font-size: 28rpx;">￥</text>
						<text>{{Odata.all_money}}</text>
					</view>
					<view class="wxtj_btn_L_Mxi" @click="ClickMxi">
						<text>价格明细</text>
						<view class="wxtj_btn_L_Mxi_icon">
							<image src="../../static/MXicon.png" class="imageBFB"></image>
						</view>
					</view>
				</view>
				<view class="wxtj_btn_R" @click="Corder">提交订单</view>
			</view>

		</view>






		<Dateassembly @dateshow="getShow" v-if="dateshow" @date-selected="handleDateSelected"></Dateassembly>

		<!-- <u-datetime-picker :show="showDate" v-model="value1" mode="datetime" :formatter="formatter"
			@confirm='confirmDate' @cancel='cancel'></u-datetime-picker> -->







		<u-picker :show="showEndDate" :columns="columns" @confirm='confirmEndDate' @cancel='cancel'></u-picker>
		<u-datetime-picker :show="showDateQi" v-model="value1" mode="datetime" :formatter="formatter"
			@confirm='confirmDateQi' @cancel='cancel' :minDate="MinDate40Hou"></u-datetime-picker>

		<!-- 明细 -->
		<u-popup :show="showMxi" :round="10" mode="bottom" @close="close" @open="open" :closeable='true'>
			<view class="Mxi">
				<view class="Mxi_titile">价格明细</view>
				<view class="Mxi_num">
					{{Odata.params.starttime}} - {{Odata.params.endtime}} 共{{Odata.num}}
					<text v-if="Odata.params.lease_type==1">小时</text>
					<text v-if="Odata.params.lease_type==2">天</text>
					<text v-if="Odata.params.lease_type==3">月</text>
				</view>
				<view class="Mxi_text">
					<text v-if="Odata.pick_type==1">到店取车</text>
					<text v-if="Odata.pick_type==3">上门送车</text>
				</view>
				<view class="Mxi_box">
					<view class="Mxi_box_list">
						<view class="Mxi_box_list_L">在线支付</view>
						<view class="Mxi_box_list_R">
							<text style="font-size: 28rpx;">￥</text>
							<text>{{Odata.all_money}}</text>
						</view>
					</view>
					<view class="Mxi_box_list">
						<view class="Mxi_box_list_L">租车费用</view>
						<view class="Mxi_box_list_R"
							style="display: flex;align-items: center;justify-content: flex-end;">
							<text
								style="color: #ccc;display: inline-block;margin-right: 20rpx;font-size: 26rpx;font-weight: 500;">{{Odata.lease_price}}x{{Odata.num}}</text>

							<text style="font-size: 32rpx;color: #333333;">￥{{Odata.lease_money}}</text>
						</view>
					</view>
					<view class="Mxi_box_list bor">
						<view class="bor_L">租车押金</view>
						<view class="bor_R">¥{{Odata.cash_money}}</view>
					</view>
					<view class="Mxi_box_list">
						<view class="Mxi_box_list_L">服务费用</view>
						<view class="Mxi_box_list_R"
							style="display: flex;align-items: center;justify-content:flex-end;">
							<text
								style="color: #ccc;display: inline-block;margin-right: 20rpx;font-size: 26rpx;font-weight: 500;">{{Odata.insure_price}}x{{Odata.people}}x{{Odata.insure_num}}</text>
							<text style="font-size: 32rpx;color: #333333;">￥{{Odata.insure_money}}</text>
						</view>
					</view>
					<view class="Mxi_box_list" v-if="pick_type==3">
						<view class="Mxi_box_list_L">上门送车</view>
						<view class="Mxi_box_list_R"></view>
					</view>
					<view class="Mxi_box_list bor" v-if="pick_type==3">
						<view class="bor_L" style="display:flex;align-items: center;">
							<text v-if="Odata.pick_single==1">单程</text>
							<text v-else>双程</text>
						</view>
						<view class="bor_R" style="display: flex;align-items:center;justify-content: flex-end;">
							<text style="color: #ccc;display: inline-block;margin-right: 20rpx;">￥{{Odata.pick_price}} x
								<text v-if="Odata.pick_single==1">1</text><text
									v-if="Odata.pick_single==2">2</text></text>
							¥{{Odata.pick_money}}
						</view>
					</view>
				</view>
			</view>
		</u-popup>


		<u-popup :show="show" :round="10" mode="bottom" @close="close" @open="open">
			<view class="CBR">
				<view class="CBR_title">添加乘坐人</view>
				<view class="CBR_out" @click="show=false">
					<image src="../../static/out.png" class="imageBFB"></image>
				</view>
				<view class="erkai_cb_list"
					style="border-bottom: 2rpx solid #ebebeb;height: 100rpx;line-height: 100rpx;margin:10rpx auto 0;">
					<view style="width: 180rpx;">姓名</view>
					<u--input placeholder="请输入" border="false" v-model="val1"></u--input>
				</view>
				<view class="erkai_cb_list"
					style="border-bottom: 2rpx solid #ebebeb;height: 100rpx;line-height: 100rpx;margin:10rpx auto 0;">
					<view style="width: 180rpx;">手机号</view>
					<u--input placeholder="请输入" border="false" v-model="val2"></u--input>
				</view>
				<view class="erkai_cb_list"
					style="border-bottom: 2rpx solid #ebebeb;height: 100rpx;line-height: 100rpx;margin:10rpx auto 0;">
					<view style="width: 180rpx;">身份证号码</view>
					<u--input placeholder="请输入" border="false" v-model="val3"></u--input>
				</view>

				<view class="CBR_btn" @click="guestC">保存</view>
			</view>
		</u-popup>

		<u-popup :show="showcenter" :round="10" mode="center" @close="close" @open="open" :safeAreaInsetBottom='false'>
			<view class="cssshowcenter">
				<view class="cssshowcenter_text" v-html="content"></view>
				<view class="cssshowcenter_btn" @click="showcenter=false">已阅读并同意</view>
				<view class="cssshowcenter_no" @click="showcenter=false">不同意</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
	import Dateassembly from '../../uni_modules/ly-Date/components/ly-Date/ly-Date.vue'
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				columns1: [
					[],
					[]
				],
				columns: [
					[]
				],
				addshow: false,
				OueData1: [{
						title: '到店取车',
						text: '前往门店自取',
						isA: true,
					},
					{
						title: '送车上门',
						text: '为您送车上门',
						isA: false,
					}
				],
				// 租赁
				zlData: [{
						title: '日租',
						isA: true
					},
					// {
					// 	title: '时租',
					// 	isA: false
					// },
					{
						title: '月租',
						isA: false
					},
				],
				CseData: [{
						isA: true,
						title: '单程（送车上门后需至下单门店还车）'
					},
					{
						isA: false,
						title: '双程（送车上门+上门取车）'
					}
				],
				DIZ: '',
				clickTimer: null,
				show: false,
				val1: '',
				val2: '',
				val3: '',
				ZuLData: '', //携带来的商品数据
				pick_type: 1, //取车方式 1到店取车  3上门送车
				lease_type: 2, //租赁方式 1时租 2日租 3月租
				pick_single: 1, //送车上门类型：1单程 2双程
				showDate: false,
				value1: Number(new Date()),
				starttime: '',
				showEndDate: false,
				endtime_text: '',
				endtime: '',
				address_id: '',
				take_back_time: '',
				showDateQi: false,
				Odata: {
					all_money: 0,
					lease_price: 0,
					insure_money: 0
				},
				showMxi: false,
				valuename: '',
				valuephone: '',
				valueid: '',
				guest: [],
				checked: false,
				showcenter: false,
				content: '',
				TYpe: '',
				people: 1,
				MinDate40Hou: null,
				dateshow: false,
				DateType: 1

			}
		},
		components: {
			Dateassembly
		},
		onLoad() {
			uni.setNavigationBarColor({
				frontColor: "#000000", // 前景颜色，即字体颜色
				backgroundColor: "#ffd0b9", // 背景颜色
			});


		},
		created() {
			setTimeout(() => {
				this.PageApi() //租车提交订单页
			}, 200)
			this.showcenter = true
		},
		onShow() {
			// 获取当前时间戳（单位：毫秒）
			const currentTimestamp = Date.now(); // 或者使用 new Date().getTime()  
			// 计算延迟 40 分钟后的时间戳  
			const delayMinutes = 40;
			const delayMilliseconds = delayMinutes * 60 * 1000; // 40 分钟转换为毫秒  
			const futureTimestamp = currentTimestamp + delayMilliseconds;
			// 打印当前时间戳和延迟后的时间戳  
			console.log('当前时间戳:', currentTimestamp);
			console.log('延迟 40 分钟后的时间戳:', futureTimestamp);
			this.MinDate40Hou = futureTimestamp


			this.ZuLData = uni.getStorageSync('GouCheData')
			this.ZuLData.item.car.image = this.ZuLData.item.car.image.split(',');

			if (!uni.getStorageSync('addressitem')) {
				this.DIZ = ''
			} else {
				this.DIZ = uni.getStorageSync('addressitem')
				this.address_id = this.DIZ.id
				this.PageApi()
			}
			console.log(this.DIZ, '地址');


			uni.request({
				url: this.YM + '/api/index/config',
				method: 'POST',
				success: res => {
					console.log(res.data.data, '隐私政策');

					const targetData1 = res.data.data.find(item => item.name === "lease_agreement");
					if (targetData1) {
						this.content = targetData1.value
						// console.log(targetData1.value);
					} else {
						this.content = '后台暂未添加，请联系工作人员'
						// console.log('未找到匹配的数据');
					}
				}
			})
		},
		methods: {
			ClickZL(){
				uni.navigateTo({
					url:'/pages/login/ZL'
				})
			},
			handleDateSelected(date) {
				console.log('父组件接收到的日期:', date);
				if (this.DateType == 1) {
					// 将接收到的日期字符串转换为数组
					let [year, month, day, hour, minute] = date.split('-');
					// 确保小时和分钟小于10时前面补零  
					hour = String(hour).padStart(2, '0');
					minute = String(minute).padStart(2, '0');
					// 格式化为所需的字符串  
					let formattedDate = `${year}/${month}/${day} ${hour}`;
					// 将格式化后的日期赋值给 this.starttime  
					this.starttime = formattedDate;
					console.log('格式化后的日期1x:', this.starttime);


					if (this.lease_type == 3) {
						let startTime = this.starttime
						let startDate = new Date(startTime);
						let endDate = new Date(startDate);
						endDate.setDate(endDate.getDate() + 30);
						endDate.setHours(23);
						endDate.setMinutes(59);
						this.endtime_text = this.formatDate(endDate)
						this.endtime = this.formatDate(endDate)
						console.log(this.endtime, 'this.endtime');
					}


				} else {
					// 将接收到的日期字符串转换为数组
					let [year, month, day, hour, minute] = date.split('-');
					// 确保小时和分钟小于10时前面补零  
					hour = String(hour).padStart(2, '0');
					minute = String(minute).padStart(2, '0');
					// 格式化为所需的字符串  
					let formattedDate = `${year}/${month}/${day} ${hour}`;
					// 将格式化后的日期赋值给 this.starttime  
					this.take_back_time = formattedDate;
					console.log('格式化后的日期2x:', this.take_back_time);
				}
			},
			getShow(value) {
				console.log(value, 'value');
				this.dateshow = value
			},
			Radiochecked() {
				this.checked = !this.checked
			},
			CguestDelete(name) {
				console.log('name', name);
				// 更新 this.guest，把匹配的项删除
				this.guest = this.guest.filter(item => item.name !== name.name);
				console.log('更新后的 guest 列表:', this.guest);
			},
			guestC() {

				if (this.val3.length !== 18) {
					uni.showToast({
						title: '身份证号码格式错误',
						icon: 'none',
						duration: 2000
					});
					return
				}

				if (this.val1 == '') {
					uni.showToast({
						title: '请输入姓名',
						icon: 'none',
						duration: 2000
					});
					return false
				} else if (this.val2 == '') {
					uni.showToast({
						title: '请输入手机号',
						icon: 'none',
						duration: 2000
					});
					return false
				} else if (this.val3 == '') {
					uni.showToast({
						title: '请输入身份证号码',
						icon: 'none',
						duration: 2000
					});
					return false
				}

				let EVE = {
					name: this.val1,
					mobile: this.val2,
					idcard: this.val3
				}
				this.guest.push(EVE)
				console.log('打印乘客数组', this.guest);
				this.show = false

				this.PageApi() //租车提交订单页

				//初始化
				this.val1 = ''
				this.val2 = ''
				this.val3 = ''


			},
			//提交订单
			Corder() {
				if (this.valueid.length !== 18) {
					uni.showToast({
						title: '身份证号码格式错误',
						icon: 'none',
						duration: 2000
					});
					return
				}

				if (this.checked == false) {
					uni.showToast({
						title: '请勾选用户租赁协议',
						icon: 'none',
						duration: 2000
					});
					return
				}
				let _this=this
				let data = {
					merchant_car_id: _this.ZuLData.item.id,
					pick_type: _this.pick_type,
					lease_type: _this.lease_type,
					pick_single: _this.pick_single,
					starttime: _this.starttime,
					endtime: _this.endtime,
					address_id: _this.address_id,
					take_back_time: _this.take_back_time,
					name: _this.valuename,
					mobile: _this.valuephone,
					idcard: _this.valueid,
					guest: _this.guest,
					source_type: uni.getStorageSync('source_type'), //扫码来源类型：1普通下单 2扫门店码下单 3扫酒店码下单
					source_id: uni.getStorageSync('source_id'),
				}
				uni.request({
					url: _this.YM + '/api/order/leaseCarPay',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token')
					},
					data,
					success: res => {
						console.log(res.data.data, '提交订单');
						if (res.data.code == 1) {
							uni.requestPayment({
								provider: 'wxpay',
								orderInfo: '',
								appId: res.data.data.appId,
								nonceStr: res.data.data
									.nonceStr, //随机字符串，长度为32个字符以下。
								timeStamp: res.data.data.timeStamp, //时间戳，要字符串类型的
								package: res.data.data
									.package, //prepay_id 参数值，提交格式如：prepay_id=xx
								signType: res.data.data.signType, //MD5类型
								paySign: res.data.data.paySign, //签名
								success: function(res) {
									uni.setStorageSync('source_type', 1)
									uni.removeStorageSync('source_id')

									if (_this.pick_type == 1) { //上门送车
										uni.navigateTo({
											url: '/pages/success/successPlayCG'
										})
									} else {
										uni.navigateTo({
											url: '/pages/success/successPlayCGSm'
										})
									}


								},
							});
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
			confirmDateQi(e) {
				// 格式化时间戳
				this.take_back_time = this.formatTimestamp(e.value);
				if (this.endtime !== '' && this.starttime !== '') {
					this.PageApi() //租车提交订单页
				}
				this.showDateQi = false
			},
			desireDate() {
				this.DateType = 2
				this.dateshow = true
				// this.showDateQi = true
			},
			confirmEndDate(e) {
				console.log(e.value[0], '选择的结束时间');

				let starttimex = this.starttime;
				let formattedTime = starttimex.replace(/-/g, '/');
				console.log(formattedTime); // 输出: "2024/08/15 10:30"


				if (this.lease_type == 1) { //小时
					this.TYpe = '小时'
					this.endtime_text = e.value[0]
					const hoursString = e.value[0]; // "2小时"
					const hours = parseInt(hoursString); // 提取数字部分，例如：2



					const startTime = new Date(formattedTime); // "2024-08-15 10:30"
					const endTime = new Date(startTime.getTime() + hours * 60 * 60 * 1000); // 添加小时计算
					this.endtime = this.formatDate(endTime);
					console.log(this.endtime, '计算出的结束时间');
				} else if (this.lease_type == 2) {
					if (e.value[0] == '1天') {
						// 假设 this.starttime 是一个字符串
						let startTime = formattedTime
						// 将字符串分割为日期和时间两部分
						let datePart = startTime.split(" ")[0]; // 获取日期部分
						let newTimePart = "23:59"; // 新的时间部分
						// 重新组合成新的字符串
						let newStartTime = datePart + " " + newTimePart;
						console.log(newStartTime); // 输出: "2024-08-15 23:59"
						this.endtime = newStartTime
						this.endtime_text = newStartTime
					} else {
						let startTime = formattedTime; // 使用 this.starttime
						let selectedDays = parseInt(e.value[0]); // 从 '1天' 或 '2天' 中提取数字
						if (isNaN(selectedDays)) {
							console.error("选定的天数无效: " + e.value[0]);
							return; // 处理无效输入
						}
						let startDate = new Date(startTime);
						let endDate = new Date(startDate);
						endDate.setDate(startDate.getDate() + selectedDays); // 增加选择的天数
						endDate.setHours(23, 59, 59, 999); // 设置为当天的23:59:59

						// 在这里减去一天
						endDate.setDate(endDate.getDate() - 1); // 将结束日期减去1天

						let resultTime = this.formatDate3e(endDate);
						console.log(resultTime, 'resultTime'); // 输出计算出的结束时间
						this.endtime = resultTime;
						this.endtime_text = resultTime;
					}
				}
				if (this.endtime !== '' && this.starttime !== '') {
					this.PageApi() //租车提交订单页
				}
				this.showEndDate = false
			},
			formatDate3e(date) {
				let year = date.getFullYear();
				let month = (date.getMonth() + 1).toString().padStart(2, '0'); // 月份从0开始
				let day = date.getDate().toString().padStart(2, '0');
				let hours = date.getHours().toString().padStart(2, '0');
				let minutes = date.getMinutes().toString().padStart(2, '0');
				return `${year}-${month}-${day} ${hours}:${minutes}`;
			},
			formatDate(date) {
				if (this.lease_type == 1) {
					const year = date.getFullYear(); // 获取年份
					const month = String(date.getMonth() + 1).padStart(2, '0'); // 获取月份
					const day = String(date.getDate()).padStart(2, '0'); // 获取日期
					const hours = String(date.getHours()).padStart(2, '0'); // 获取小时
					const minutes = String(date.getMinutes()).padStart(2, '0'); // 获取分钟
					// 返回格式化后的字符串
					return `${year}-${month}-${day} ${hours}:${minutes}`;
				} else if (this.lease_type == 2) {
					const year = date.getFullYear(); // 获取年份
					const month = String(date.getMonth() + 1).padStart(2, '0'); // 获取月份
					const day = String(date.getDate()).padStart(2, '0'); // 获取日期
					const hours = String(date.getHours()).padStart(2, '0'); // 获取小时
					const minutes = String(date.getMinutes()).padStart(2, '0'); // 获取分钟
					// 返回格式化后的字符串
					return `${year}-${month}-${day} ${hours}:${minutes}`;
				} else {
					const year = date.getFullYear(); // 获取年份
					const month = String(date.getMonth() + 1).padStart(2, '0'); // 获取月份
					const day = String(date.getDate()).padStart(2, '0'); // 获取日期
					const hours = String(date.getHours()).padStart(2, '0'); // 获取小时
					const minutes = String(date.getMinutes()).padStart(2, '0'); // 获取分钟
					// 返回格式化后的字符串
					return `${year}-${month}-${day} ${hours}:${minutes}`;
				}

			},
			confirmDate(e) {
				console.log(e, '选择的开始时间');

				// 格式化时间戳
				this.starttime = this.formatTimestamp(e.value);
				this.showDate = false

				if (this.endtime !== '' && this.starttime !== '') {
					this.PageApi() //租车提交订单页
				}


				if (this.lease_type == 3) {
					const timestamp = e.value; // 获取时间戳
					const startTime = new Date(timestamp);
					const endTime = new Date(startTime.getTime() + 30 * 24 * 60 * 60 * 1000);
					this.endtime_text = this.formatDate(endTime);
					this.endtime = this.formatDate(endTime);
					console.log(this.endtime_text, '计算出的结束时间');
				}
			},

			formatTimestamp(timestamp) {
				const date = new Date(timestamp); // 将时间戳转换为Date对象
				const year = date.getFullYear(); // 获取年份
				const month = String(date.getMonth() + 1).padStart(2, '0'); // 获取月份（注意要加1）
				const day = String(date.getDate()).padStart(2, '0'); // 获取日期
				const hours = String(date.getHours()).padStart(2, '0'); // 获取小时
				const minutes = String(date.getMinutes()).padStart(2, '0'); // 获取分钟
				// 返回格式化后的字符串
				return `${year}-${month}-${day} ${hours}:${minutes}`;
			},
			formatter(type, value) {
				if (type === 'year') {
					return `${value}年`;
				}
				if (type === 'month') {
					return `${value}月`;
				}
				if (type === 'day') {
					return `${value}日`;
				}
				if (type === 'hour') {
					return `${value}时`;
				}
				if (type === 'minute') {
					return `${value}分`;
				}
				return value;
			},
			//点击了开始时间
			startDate() {
				console.log('点击了开始时间');
				this.DateType = 1
				this.dateshow = true
			},
			endDate() {
				if (this.starttime == '') {
					uni.showToast({
						title: '请先选择开始时间',
						icon: 'none',
						duration: 2000
					});
					return
				}

				// 租赁方式 1时租 2日租 3月租
				if (this.lease_type == 1) {
					// for (var i = 1; i < 25; i++) {
					// 	this.columns[0].push(`${i}小时`)
					// }
					this.showEndDate = true
				} else if (this.lease_type == 2) {
					for (var i = 1; i < 31; i++) {
						this.columns[0].push(`${i}天`)
					}
					this.showEndDate = true
				}
			},
			formatDateDDDDD(date) {
				const year = date.getFullYear();
				const month = String(date.getMonth() + 1).padStart(2, '0'); // 月份从0开始  
				const day = String(date.getDate()).padStart(2, '0');
				const hours = String(date.getHours()).padStart(2, '0');
				const minutes = String(date.getMinutes()).padStart(2, '0');
				return `${year}/${month}/${day} ${hours}:${minutes}`;
			},
			Cs(item) {
				for (var i = 0; i < this.CseData.length; i++) {
					this.CseData[i].isA = false
				}
				item.isA = true

				if (item.title == '单程（送车上门后需至下单门店还车）') {
					this.pick_single = 1
				} else {
					this.pick_single = 2
				}
				this.PageApi() //租车提交订单页
			},
			PageApi() {

				let people = Number(this.people) + Number(this.guest.length)
				console.log(people, '乘客数量');
				let data = {
					merchant_car_id: this.ZuLData.item.id,
					pick_type: this.pick_type,
					lease_type: this.lease_type,
					pick_single: this.pick_single,
					starttime: this.starttime,
					endtime: this.endtime,
					address_id: this.address_id,
					take_back_time: this.take_back_time,
					people: people
				}
				uni.request({
					url: this.YM + '/api/order/orderPage',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token')
					},
					data,
					success: res => {
						console.log(res, '租车提交订单页');
						if (res.data.code == 1) {
							this.Odata = res.data.data
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
			RadioClick(item) {
				console.log('点击的', item);
				for (var i = 0; i < this.OueData1.length; i++) {
					this.OueData1[i].isA = false
				}
				item.isA = true
				if (item.title == '到店取车') {
					this.pick_type = 1
					this.pick_single = '' //初始化
					this.address_id = ''
				} else {
					this.pick_type = 3
				}

				this.PageApi()
			},
			Clickselete(item) {

				//初始化操作
				this.starttime = ''
				this.endtime = ''
				this.take_back_time = ''
				this.endtime_text = ''


				console.log('点击的', item);
				for (var i = 0; i < this.zlData.length; i++) {
					this.zlData[i].isA = false
				}
				item.isA = true

				if (item.title == '时租') {
					this.lease_type = 1

					// 获取当前时间
					const now = new Date();

					// 获取当前的小时和分钟
					const hours = now.getHours();
					const minutes = now.getMinutes();
					// 计算距离凌晨12点的剩余时间
					const remainingHours = 24 - hours - (minutes > 0 ? 1 : 0);

					console.log(`${remainingHours}`);
					let Num = `${remainingHours}`

					this.columns[0] = []
					for (var i = 1; i < Number(Num) + 1; i++) {
						this.columns[0].push(`${i}小时`)
					}

					console.log(this.columns[0], 'this.columns[0]');

				} else if (item.title == '日租') {
					this.lease_type = 2
					this.columns[0] = []
					for (var i = 1; i < 31; i++) {
						this.columns[0].push(`${i}天`)
					}
				} else {
					this.lease_type = 3
				}
				this.PageApi()

			},
			//点击了添加
			Ctj() {
				console.log('点击了添加');
				this.show = true
			},
			//点击了价格明细
			ClickMxi() {
				if (this.endtime == '' && this.starttime == '') {
					uni.showToast({
						title: '请先选择开始与结束时间',
						icon: 'none',
						duration: 2500
					});
					return
				} else {
					console.log('明细');
					this.showMxi = true
				}

			},
			ClickXY() {
				uni.navigateTo({
					url: '/pages/login/SQ'
				})
			},
			Clickys() {
				uni.navigateTo({
					url: '/pages/login/YS'
				})
			},
			Radiochecked() {
				this.checked = !this.checked
			},
			//跳转实名认证
			Clicksmrz() {
				console.log('跳转实名认证');
				uni.navigateTo({
					url: '/pages/IndexChild/SMRZ'
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
			close() {
				this.show2 = false
				this.showMxi = false
				this.showcenter = false
				this.showDate = false
			},
			open() {},
			cancel() {
				this.show2 = false
				this.showDate = false
				this.showEndDate = false
				this.showDateQi = false
				this.showMxi = false
			},
		}
	}
</script>

<style>
	/* 野马二开css */
	.erkai_cb {
		width: 656rpx;
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
	
	.tekai_tis{
		width: 640rpx;
		height: auto;
		margin: 15rpx auto 0;
		font-size: 26rpx;
		color: orangered;
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

	/* ------------------------------------------------------- */
	.goumai {
		width: 100vw;
		min-height: 100vh;
		background-color: pink;
	}

	/* 1 */
	.goumai_oen {
		width: 100%;
		height: 300rpx;
		background: linear-gradient(0deg, #F7F7F7 0%, #FBEBE0 51%, #FFCFB7 100%);
		float: left;
	}

	.goumai_oen_smrz {
		width: 696rpx;
		height: 68rpx;
		background: #FE7F77;
		border-radius: 16rpx;
		margin: 20rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.goumai_oen_smrz_icon {
		width: 22rpx;
		height: 26rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 15rpx;
		margin-left: 20rpx;
	}

	.goumai_oen_smrz_text {
		width: 600rpx;
		height: 50rpx;
		line-height: 50rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FFFFFF;
	}

	.goumai_oen_smrz_Y {
		width: 12rpx;
		height: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 20rpx;
	}



	.wxtj_oen_box {
		width: 696rpx;
		height: 148rpx;
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

	/* 	.wxtj_oen_box_Num {
		width: 90%;
		height: 70rpx;
		margin: 18rpx auto 0;
		display: flex;
	}

	.wxtj_oen_box_Num_L,
	.wxtj_oen_box_Num_R {
		width: 40%;
		height: 100%;
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
		display: flex;
		align-items: center;
		justify-content: flex-end;
	} */

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
		overflow: auto;
	}

	.wxtj_two_xx {
		width: 696rpx;
		/* height: 332rpx; */
		background: #FFFFFF;
		box-shadow: 0rpx 3rpx 17rpx 0rpx rgba(0, 0, 0, 0.04);
		border-radius: 16rpx;
		margin: 20rpx auto 0;
		padding-top: 10rpx;
		padding-bottom: 20rpx;
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
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		/* background-color: red; */
		display: flex;
		align-items: center;

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

	.wxtj_two_xx_box_kuai {
		width: 90%;
		height: 100rpx;
		/* background-color: red; */
		margin: 10rpx auto 0;
		border-bottom: 1rpx solid #F0F0F0;
		display: flex;
		align-items: center;
		justify-content: flex-start;
	}

	.wxtj_two_xx_box_kuai_B {
		width: 25%;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		/* background-color: skyblue; */
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
	}

	.wxtj_two_xx_box_kuai_B_radio {
		width: 36rpx;
		height: 36rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 8rpx;
	}

	.wxtj_two_xx_box_kuai_B_text {
		width: 370rpx;
		height: auto;
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
		z-index: 5;
	}

	.wxtj_btn_L {
		width: 340rpx;
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
		/* height: 100rpx; */
		/* background-color: violet; */
	}

	.goumai_two_qc_dz_text_D {
		width: 100%;
		/* height: 60%; */
		/* background-color: red; */
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
		line-height: 35rpx;

	}

	.goumai_two_qc_dz_text_N {
		width: 100%;
		/* height: 40%; */
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

	.wxtj_two_gs {
		width: 696rpx;
		height: 40rpx;
		/* background-color: violet; */
		margin: 50rpx auto 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #959595;
	}

	.wxtj_two_gs_icon {
		width: 30rpx;
		height: 30rpx;
		border-radius: 50%;
		/* overflow: hidden; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 15rpx;
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

	/* 分期显示 */
	.FQ {
		width: 100%;
		height: 500rpx;
		/* background-color: #FF; */
		float: left;
		padding-bottom: 50rpx;
	}

	.FQ_title {
		width: 95%;
		height: 70rpx;
		/* background-color:pink; */
		margin: 25rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.FQ_title_btn1 {
		width: 150rpx;
		height: 70rpx;
		text-align: center;
		line-height: 70rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #989898;
	}

	.vaue {
		color: #0261FF;
	}

	.FQ_title_title {
		width: 300rpx;
		height: 70rpx;
		line-height: 70rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #101010;
		text-align: center;
	}

	.FQ_box {
		width: 692rpx;
		height: 80rpx;
		background: #FDEEED;
		border: 2px solid #FCE7E5;
		border-radius: 10rpx;
		margin: 30rpx auto 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
	}

	.FQ_click {
		width: 692rpx;
		height: 350rpx;
		/* background-color: orange; */
		margin: 20rpx auto 0;
	}

	.FQ_click_btn,
	.FQ_click_btnA {
		width: 140rpx;
		height: 60rpx;
		background-color: #ebebeb;
		text-align: center;
		line-height: 60rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		/* color: #101010; */
		color: #989898;
		margin: 15rpx 15rpx 15rpx 15rpx;
		border-radius: 8rpx;
		float: left;
	}

	.FQ_click_btnA {
		background-color: orangered;
		color: black;
	}

	.Mxi {
		width: 100%;
		height: 800rpx;
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
		height: 400rpx;
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

	.bor {
		background: #FFFCF7;
		border: 2px solid #FDF1DE;
		border-radius: 10rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 70rpx;
		line-height: 70rpx;
		width: 99%;
	}

	.bor_L {
		width: 45%;
		height: 100%;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #010101;
	}

	.bor_R {
		width: 45%;
		height: 100%;
		text-align: right;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #333333;
	}

	.cssshowcenter {
		width: 680rpx;
		padding-top: 35rpx;
		padding-bottom: 35rpx;

	}

	.cssshowcenter_text {
		width: 90%;
		height: 500rpx;
		overflow-y: scroll;
		margin: 0 auto;
		font-size: 28rpx;

	}

	.cssshowcenter_btn {
		width: 240rpx;
		height: 70rpx;
		text-align: center;
		line-height: 70rpx;
		color: #FFFFFF;
		background-color: orange;
		border-radius: 8rpx;
		font-size: 28rpx;
		margin: 25rpx auto 0;
	}

	.cssshowcenter_no {
		width: 100%;
		height: 70rpx;
		text-align: center;
		line-height: 70rpx;
		font-size: 26rpx;
		color: #858896;
	}
</style>
