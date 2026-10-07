<template>
	<view class="QD">

		<!-- 顶部 -->
		<view class="QD_Oen">
			<view class="QD_Oen_BJ">
				<image src="../../static/QDBJ.jpg" class="imageBFB"></image>
			</view>

			<view class="QD_Oen_title">签到</view>
			<view class="QD_Oen_F" @click="ClickF">
				<image src="../../static/BaiZ.png" class="imageBFB"></image>
			</view>

			<!-- 签到具体块 -->
			<view class="QD_JT">
				<view class="QD_JT_oen">
					<view class="QD_JT_oen_L">
						<view class="QD_JT_oen_L_icon" @click="ClickYL">
							<image :src="YM + Me.avatar" class="imageBFB"></image>
						</view>
						<text>{{Me.nickname}}</text>
					</view>
					<view class="QD_JT_oen_R">
						<view class="QD_JT_oen_R1" @click="ClickJFMX">
							<view class="QD_JT_oen_R1_t">
								<text>{{Me.score}}</text>
								<view class="QD_JT_oen_R1_t_icon">
									<image src="../../static/Hsanjiao.png" class="imageBFB"></image>
								</view>
							</view>
							<view class="QD_JT_oen_R1_b">我的积分</view>
						</view>
						<view class="QD_JT_oen_R1">
							<view class="QD_JT_oen_R1_t">{{Me.count_sign}}</view>
							<view class="QD_JT_oen_R1_b">累计打卡(天)</view>
						</view>
					</view>
				</view>

				<view class="QD_JT_two">
					<view class="QD_JT_two_t">

						<view class="QD_JT_two_t_box" v-for="(item,index) in Me.date_list" :key="index">
							<view class="QD_JT_two_t_box_icon" v-if="item.state==1">未签到</view>
							<view class="QD_JT_two_t_box_icon" v-if="item.state==2">
								<image src="../../static/qiandaocg.png" class="imageBFB"></image>
							</view>
							<text v-if="item.dayOfWeek==1">周一</text>
							<text v-if="item.dayOfWeek==2">周二</text>
							<text v-if="item.dayOfWeek==3">周三</text>
							<text v-if="item.dayOfWeek==4">周四</text>
							<text v-if="item.dayOfWeek==5">周五</text>
							<text v-if="item.dayOfWeek==6">周六</text>
							<text v-if="item.dayOfWeek==7">周日</text>
						</view>

					</view>

					<view class="QD_JT_two_b" @click="ClickQD" v-if="QD==1">立即签到</view>
					<view class="QD_JT_two_b1" v-if="QD==2">已签到</view>
				</view>
			</view>
		</view>

		<!-- 底部 -->
		<view class="QD_Two">
			<view class="QD_Two_title">兑换优惠券</view>

			<view class="QD_Two_list_box">
				<view class="QD_Two_list" v-for="(item,index) in YHJdata" :key="index">
					<view class="QD_Two_list_icon">
						<image src="../../static/YHJBJ.png" class="imageBFB"></image>
					</view>

					<view class="QD_Two_list_price">
						<view class="QD_Two_list_price_price">
							<text>{{item.price}}</text>
							<text style="font-size: 26rpx;font-weight: 500;">元</text>
						</view>
						<view class="QD_Two_list_price_text">满减券</view>
					</view>

					<view class="QD_Two_list_right">
						<view class="QD_Two_list_right_L">
							<view class="QD_Two_list_right_L_t">满{{item.price}}减{{item.reduceprice}}</view>
							<view class="QD_Two_list_right_L_b">有效期:{{item.endtime}}</view>
						</view>
						<view class="QD_Two_list_right_R">
							<view class="QD_Two_list_right_R_t">
								<view class="QD_Two_list_right_R_t_icon">
									<image src="../../static/jifen.png" class="imageBFB"></image>
								</view>
								<text>{{item.score}}积分</text>
							</view>
							<view class="QD_Two_list_right_R_b1" @click="ClickTS(item)">立即兑</view>
						</view>
					</view>
				</view>

				<u-empty mode="coupon" icon="https://cdn.uviewui.com/uview/empty/car.png" v-if="YHJdata==''">
				</u-empty>
			</view>
		</view>


		<!-- 签到成功弹出 -->
		<view class="qdcg" v-if="show==true">
			<view class="qdcg_icon">
				<image src="../../static/qdcgtctp.png" class="imageBFB"></image>
			</view>

			<view class="qdcg_box">
				<view class="qdcg_box_t">恭喜获得{{signReward}}积分</view>
				<view class="qdcg_box_b">已放入您的积分账户</view>
			</view>
			<!-- 开心收下 -->
			<view class="qdcg_kxsx" @click="ClickKXSX"></view>
		</view>


		<!-- 确定兑换弹出 -->
		<u-popup :show="showDuihuan" :round="10" mode="center" @close="close" @open="open">
			<view class="showDuihuan">
				<view class="showDuihuan_title">确定使用积分兑换优惠券？</view>
				<view class="showDuihuan_text">兑换成功后，可在【我的-优惠券】查看并使用</view>
				<view class="showDuihuan_btn">
					<view class="showDuihuan_btn1" @click="ClickDHQX">取消</view>
					<view class="showDuihuan_btn2" @click="ClickDHQD">确定兑换</view>
				</view>
			</view>
		</u-popup>

		<view class="qdcg_mc" v-if="show==true" @click="Clickoff"></view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				YHJdata: [],
				show: false,
				showDuihuan: false,
				Me: '',
				QD: '',
				yhjid: '',
				signReward: 0,
				isSigning: false,
				isRedeeming: false
			}
		},
		onShow() {
			this.QdaoApiIndex() //签到首页
			this.YHJlist() //优惠券列表
		},
		methods: {
			formatEndtime(value) {
				const timestamp = Number(value)
				if (!timestamp) return '长期有效'
				const date = new Date(timestamp * 1000)
				const pad = number => String(number).padStart(2, '0')
				return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
			},
			//预览头像
			ClickYL() {
				var SrcImage = this.YM + this.Me.avatar;
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
			//优惠券列表
			YHJlist() {
				let listyhj = {
					page: 1,
				}
				uni.request({
					url: this.YM + '/api/sign/couponList',
					method: 'POST',
					data:listyhj,
					success: res => {
						this.YHJdata = res.data.code == 1 && res.data.data
							? (res.data.data.data || [])
							: []

						this.YHJdata.forEach(item => {
							item.price = parseFloat(item.price).toFixed(0);
							item.reduceprice = parseFloat(item.reduceprice).toFixed(0);
							item.endtime = this.formatEndtime(item.endtime)
						});
						console.log(this.YHJdata, '优惠券列表');
					},
				})
			},
			//立即签到首页信息
			QdaoApiIndex() {
				uni.request({
					url: this.YM + '/api/sign/index',
					method: 'POST',
					header:{
						'token': uni.getStorageSync('token')
					},
					success: res => {
						if (res.data.code != 1 || !res.data.data) {
							this.Me = { date_list: [] }
							this.QD = ''
							uni.showToast({ title: res.data.msg || '签到信息加载失败', icon: 'none' })
							return
						}
						this.Me = res.data.data
						this.Me.date_list = (this.Me.date_list || []).map((item, index) => {
							// 添加属性
							item.dayOfWeek = index + 1;
							return item;
						}).sort((a, b) => {
							// 按照顺序排序
							return a.dayOfWeek - b.dayOfWeek;
						});


						let currentDate = new Date().getDate(); // 获取当前天的日期

						let matchingDate = this.Me.date_list.find(item => {
							let itemDate = new Date(item.date).getDate(); // 获取列表项的日期
							return itemDate === currentDate;
						});

						if (matchingDate && matchingDate.state == 1) {
							this.QD = 1
						} else if (matchingDate) {
							this.QD = 2
						} else {
							this.QD = ''
						}
					},
				})
			},
			//点击了我的积分 进入积分明细
			ClickJFMX() {
				console.log('积分明细');
				uni.navigateTo({
					url: '/pages/IndexChild/integralMX'
				})
			},
			// 兑换优惠券弹出
			ClickTS(item) {
				console.log('确定兑换吗');
				this.showDuihuan = true

				this.yhjid = item.coupon_id
			},
			ClickDHQX() {
				this.showDuihuan = false
			},
			ClickDHQD() {
				if (this.isRedeeming) return
				this.showDuihuan = false
				let dasda = {
					coupon_id: this.yhjid
				}
				this.isRedeeming = true
				uni.request({
					url: this.YM + '/api/sign/redeemCoupon',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token')
					},
					data:dasda,
					success: res => {
						this.isRedeeming = false
						console.log(res, '立即兑换');
						if (res.data.code == 1) {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 2000
							});
							this.YHJlist() //优惠券列表
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
						this.isRedeeming = false
						uni.showToast({title: '兑换失败，请稍后重试', icon: 'none'})
					}
				})
			},
			//开心收下
			ClickKXSX() {
				console.log('点击了开心收下');
				this.show = false
			},
			//点击立即签到
			ClickQD() {
				if (this.isSigning) return
				this.isSigning = true
				uni.request({
					url: this.YM + '/api/sign/signIn',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token')
					},
					success: res => {
						this.isSigning = false
						if (res.data.code == 1) {
							this.signReward = Number(res.data.data) || 0
							this.show = true
							this.QdaoApiIndex() //签到首页
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
						this.isSigning = false
						uni.showToast({title: '签到失败，请稍后重试', icon: 'none'})
					}
				})
			},
			Clickoff() {
				this.show = false
			},
			open() {
				// console.log('open');
			},
			close() {
				this.show = false
				this.showDuihuan = false
				// console.log('close');
			},
			//点击返回上一层
			ClickF() {
				uni.navigateBack()
			},
		}
	}
</script>

<style>
	.QD {
		width: 100vw;
		min-height: 100vh;
		background-color: #F7F7F7;
	}

	/* 顶部 */
	.QD_Oen {
		width: 100%;
		height: 840rpx;
		background-color: palevioletred;
		position: relative;
	}

	.QD_Oen_BJ {
		width: 100%;
		height: 100%;
		position: absolute;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		/* background-color: aquamarine; */
		z-index: 0;
	}

	.QD_Oen_title {
		width: 100%;
		height: 60rpx;
		position: absolute;
		top: 100rpx;
		left: 0;
		text-align: center;
		line-height: 60rpx;
		font-size: 32rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #FFFFFF;
		z-index: 1;
		/* background-color: darkblue; */
	}

	.QD_Oen_F {
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

	.QD_JT {
		width: 650rpx;
		height: 350rpx;
		position: absolute;
		top: 430rpx;
		left: 50%;
		transform: translate(-50%);
		/* background-color: red; */
		float: left;
	}

	.QD_JT_oen {
		width: 100%;
		height: 110rpx;
		border-bottom: 1rpx solid #F0F0F0;
		/* background-color: aqua; */
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
	}

	.QD_JT_oen_L {
		width: 200rpx;
		height: 60rpx;
		/* background-color: yellow; */
		margin-top: 15rpx;
		display: flex;
		align-items: center;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
	}

	.QD_JT_oen_L_icon {
		width: 60rpx;
		height: 60rpx;
		/* background-color: green; */
		overflow: hidden;
		border-radius: 50%;
		margin-right: 15rpx;
	}

	.QD_JT_oen_R {
		width: 350rpx;
		height: 80rpx;
		/* background-color: skyblue; */
		margin-top: 5rpx;
		display: flex;
		justify-content: flex-end;
	}

	.QD_JT_oen_R1 {
		width: 40%;
		height: 100%;
		/* background-color: darkseagreen; */
	}

	.QD_JT_oen_R1_t {
		width: 100%;
		height: 50%;
		font-size: 36rpx;
		font-family: DIN;
		font-weight: bold;
		color: #EB4E71;
		/* background-color: yellow; */
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.QD_JT_oen_R1_t_icon {
		width: 10rpx;
		height: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 10rpx;
	}

	.QD_JT_oen_R1_b {
		width: 100%;
		height: 50%;
		/* background-color: yellow; */
		font-size: 22rpx;
		font-family: PingFang SC;
		color: #333333;
		text-align: center;
		font-weight: 600;
	}

	.QD_JT_two {
		width: 100%;
		height: 250rpx;
		/* background-color: khaki; */
		float: left;
	}

	.QD_JT_two_t {
		width: 100%;
		height: 95rpx;
		/* background-color: greenyellow; */
		margin-top: 20rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.QD_JT_two_t_box {
		width: 66rpx;
		height: 100%;
		/* background-color: red; */
		display: flex;
		align-items: center;
		justify-content: center;
		flex-wrap: wrap;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.QD_JT_two_t_box_icon {
		width: 66rpx;
		height: 66rpx;
		background-color: #F9EDE1;
		border-radius: 50%;
		overflow: hidden;
		margin-bottom: 8rpx;
		font-size: 20rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #E4B5BA;
		text-align: center;
		line-height: 66rpx;
	}

	.QD_JT_two_b,
	.QD_JT_two_b1 {
		width: 400rpx;
		height: 88rpx;
		background: #FE423D;
		border-radius: 44rpx;
		margin: 40rpx auto 0;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FFFFFF;
		text-align: center;
		line-height: 88rpx;
	}

	.QD_JT_two_b1 {
		background: #FE423D;
		color: #FFFFFF;
		opacity: 0.5;
	}

	/* 底部 */
	.QD_Two {
		width: 100%;
		min-height: calc(100vh - 840rpx);
		background-color: #FFF2E7;
		position: relative;
	}

	.QD_Two_title {
		width: 685rpx;
		height: 60rpx;
		line-height: 60rpx;
		font-size: 32rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #101010;
		margin: 15rpx auto 0;
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
		width: 150rpx;
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

	.QD_Two_list_right_R_t_icon {
		width: 25rpx;
		height: 25rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 10rpx;
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

	/* 签到成功弹出 */
	.qdcg {
		width: 552rpx;
		height: 766rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
		position: fixed;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		z-index: 9999;
	}

	.qdcg_icon {
		width: 100%;
		height: 100%;
		/* background-color: salmon; */
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 1;
	}

	.qdcg_box {
		width: 341rpx;
		height: 151rpx;
		position: absolute;
		top: 55%;
		left: 50%;
		transform: translate(-50%, -50%);
		/* background-color: blue; */
		z-index: 3;
	}

	.qdcg_mc {
		width: 100vw;
		height: 100vh;
		background-color: rgba(0, 0, 0, 0.5);
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 5;
	}

	.qdcg_box_t {
		width: 100%;
		height: 50%;
		font-size: 36rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: red;
		text-align: center;
		line-height: 90rpx;

	}

	.qdcg_box_b {
		width: 100%;
		height: 50%;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		text-align: center;
	}

	.qdcg_kxsx {
		width: 400rpx;
		height: 126rpx;
		/* background-color: salmon; */
		position: absolute;
		left: 50%;
		top: 81%;
		transform: translate(-50%, -50%);
		z-index: 3;
	}

	/* 确定兑换弹出 */
	.showDuihuan {
		width: 636rpx;
		/* height: 338rpx; */
		background: #FFFFFF;
		border-radius: 20rpx;
		float: left;
		padding-top: 20rpx;
		padding-bottom: 15rpx;
	}

	.showDuihuan_title {
		width: 100%;
		height: 75rpx;
		line-height: 75rpx;
		text-align: center;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #101010;
		/* background-color: darksalmon; */
		margin-top: 30rpx;
	}

	.showDuihuan_text {
		width: 100%;
		height: 40rpx;
		line-height: 40rpx;
		font-size: 24rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858585;
		text-align: center;
		margin-top: 5rpx;
	}

	.showDuihuan_btn {
		width: 100%;
		height: 100rpx;
		/* background-color: darkseagreen; */
		margin-top: 25rpx;
		display: flex;
		align-items: center;
		justify-content: space-around;
	}

	.showDuihuan_btn1,
	.showDuihuan_btn2 {
		width: 236rpx;
		height: 80rpx;
		background: #F7F7F7;
		border: 1rpx solid #E5E5E5;
		border-radius: 40rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #5A5A5A;
		text-align: center;
		line-height: 80rpx;
		margin-left: 25rpx;
	}

	.showDuihuan_btn2 {
		background: #ED9939;
		color: #FFFFFF;
		border: 0;
		margin-left: 0;
		margin-right: 25rpx;
	}
</style>
