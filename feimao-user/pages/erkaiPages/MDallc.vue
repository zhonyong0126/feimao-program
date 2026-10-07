<template>
	<view class="cx">
		<view class="cx_box">
			<view class="cx_box_title">
				<view class="cx_box_title_shu"></view>
				<text>基础设置</text>
			</view>
			<view class="cx_box_list">
				<text>*<text style="color: #000000;">车辆品牌</text></text>
				<view style="display: flex;
				align-items: center;
				justify-content: flex-end;height: 100%;">
					<view class="cx_box_list_input" @click="Spp">
						<u--input placeholder="请选择" border="false" v-model="valueppai" :disabled='true'
							disabledColor='#FFFFFF'></u--input>
					</view>
					<view class="cx_box_list_input_icon">
						<image src="../../static/ALLRIGHT.png" class="imageBFB"></image>
					</view>
				</view>
			</view>

			<view class="cx_box_list">
				<text>*<text style="color: #000000;">平台车辆</text></text>
				<view style="display: flex;
				align-items: center;
				justify-content: flex-end;height: 100%;">
					<view class="cx_box_list_input" @click="Spt">
						<u--input placeholder="请选择" border="false" v-model="valuept" :disabled='true'
							disabledColor='#FFFFFF'></u--input>
					</view>
					<view class="cx_box_list_input_icon">
						<image src="../../static/ALLRIGHT.png" class="imageBFB"></image>
					</view>
				</view>
			</view>

			<view class="cx_box_list">
				<text>*<text style="color: #000000;">库存数量</text></text>
				<view style="display: flex;
				align-items: center;
				justify-content: flex-end;height: 100%;">
					<view class="cx_box_list_input">
						<u--input placeholder="请选择" border="false" v-model="total_stock"></u--input>
					</view>
				</view>
			</view>

			<view class="cx_box_list">
				<text>*<text style="color: #000000;">车型照片</text></text>
				<view></view>
			</view>

			<view style="width: 90%;height: auto;margin: 0 auto 0;display: flex;align-items: center;flex-wrap: wrap;">
				<view class="cx_box_up" v-for="(item,index) in images" :key="index">
					<image :src="YM + item" class="imageBFB" style="border: 8rpx;"></image>
					<view class="cx_box_up_out" @click="OutImgaes(index)">
						<image src="../../static/out1.png" class="imageBFB"></image>
					</view>
				</view>
				<view class="cx_box_up" @click="Up" v-if="images.length<6">+</view>
			</view>
		</view>

		<view class="cx_box">
			<view class="cx_box_title">
				<view class="cx_box_title_shu"></view>
				<text>基础设置</text>
			</view>
			<view class="cx_box_list">
				<text>*<text style="color: #000000;">车辆押金</text></text>
				<view style="display: flex;
				align-items: center;
				justify-content: flex-end;height: 100%;">
					<view class="cx_box_list_input">
						<u--input placeholder="请选择" border="false" v-model="cash_price"></u--input>
					</view>
				</view>
			</view>

	<!-- 		<view class="cx_box_list">
				<text>*<text style="color: #000000;">时租价格</text></text>
				<view style="display: flex;
				align-items: center;
				justify-content: flex-end;height: 100%;">
					<view class="cx_box_list_input">
						<u--input placeholder="请选择" border="false" v-model="hour_price"></u--input>
					</view>
				</view>
			</view> -->

			<view class="cx_box_list">
				<text>*<text style="color: #000000;">日租价格</text></text>
				<view style="display: flex;
				align-items: center;
				justify-content: flex-end;height: 100%;">
					<view class="cx_box_list_input">
						<u--input placeholder="请选择" border="false" v-model="day_price"></u--input>
					</view>
				</view>
			</view>

			<view class="cx_box_list">
				<text>*<text style="color: #000000;">月租价格</text></text>
				<view style="display: flex;
				align-items: center;
				justify-content: flex-end;height: 100%;">
					<view class="cx_box_list_input">
						<u--input placeholder="请选择" border="false" v-model="month_price"></u--input>
					</view>
				</view>
			</view>
		</view>


		<view class="cx_box">
			<view class="cx_box_title">
				<view class="cx_box_title_shu"></view>
				<text>车辆详情</text>
			</view>

			<!-- 	<view class="cx_box_up" style="height: 186rpx;" @click="Upintro">
				<image :src="YM + intro" class="imageBFB" v-if="intro!==''"></image>
				<text v-else>+</text>
			</view> -->

			<view style="width: 90%;height: auto;margin: 0 auto 0;display: flex;align-items: center;flex-wrap: wrap;">
				<view class="cx_box_up" v-for="(item,index) in intro" :key="index">
					<image :src="YM + item" class="imageBFB" style="border: 8rpx;"></image>
					<view class="cx_box_up_out" @click="OutImgaes2X(index)">
						<image src="../../static/out1.png" class="imageBFB"></image>
					</view>
				</view>
				<view class="cx_box_up" @click="Upintro" v-if="intro.length<6">+</view>
			</view>

		</view>

		<view style="width: 100%;height: 220rpx;"></view>
		<view class="cx_btn">
			<view class="cx_btn_text" @click="SJ">{{type==2?'立即上架':'保存编辑'}}</view>
		</view>


		<u-picker :show="show" :columns="columns" @confirm='confirm' @cancel='cancel'></u-picker>
		<u-picker :show="show1" :columns="columns1" @confirm='confirm1' @cancel='cancel'></u-picker>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				show: false,
				columns: [
					[]
				],
				columns1: [
					[]
				],
				show1: false,
				type: 1, //1查看  2添加提交  3编辑提交
				total_stock: '',
				cash_price: '',
				month_price: '',
				day_price: '',
				hour_price: '',
				id: '',
				ptai: {
					brand_list: '',
					platform_car_list: ''
				},
				brand_id: '',
				valueppai: '',
				valuept: '',
				images: [],
				intro: []
			}
		},
		onLoad(options) {
			this.type = options.type
		},
		onShow() {

			if (uni.getStorageSync('allc')) {
				let Data = uni.getStorageSync('allc')

				console.log(Data);

				this.valueppai = Data.brand_name
				this.valuept = Data.name
				this.total_stock = Data.total_stock
				this.cash_price = Data.cash_price
				this.month_price = Data.month_price
				this.day_price = Data.day_price
				this.hour_price = Data.hour_price
				this.id = Data.id
				this.intro = Data.intro
				this.images = Data.images
				this.platform_car_id = Data.platform_car_id

				this.brand_id = Data.brand_id
				this.ptclApi2x()

			}

			this.ptclApi()

		},
		methods: {
			OutImgaes(index) {
				this.images.splice(index, 1); // 移除对应索引的图片
				console.log(this.images, '移除');
			},
			OutImgaes2X(index) {
				this.intro.splice(index, 1); // 移除对应索引的图片
				console.log(this.intro, '移除');
			},
			Up() {
				uni.chooseImage({
					success: (chooseImageRes) => {
						const tempFilePaths = chooseImageRes.tempFilePaths;
						uni.uploadFile({
							url: this.YM + '/api/common/upload',
							filePath: tempFilePaths[0],
							name: 'file',
							formData: {
								token: uni.getStorageSync('MDtoken')
							},
							success: (res) => {
								let Ima = JSON.parse(res.data)
								console.log(Ima, 'Ima-Ima-Ima');
								this.images.push(Ima.data.url)
							}
						});
					}
				});
			},
			Upintro() {
				uni.chooseImage({
					success: (chooseImageRes) => {
						const tempFilePaths = chooseImageRes.tempFilePaths;
						uni.uploadFile({
							url: this.YM + '/api/common/upload',
							filePath: tempFilePaths[0],
							name: 'file',
							formData: {
								token: uni.getStorageSync('MDtoken')
							},
							success: (res) => {
								let Ima = JSON.parse(res.data)
								console.log(Ima, 'Ima-Ima-Ima');
								this.intro.push(Ima.data.url)
							}
						});
					}
				});
			},
			ptclApi2x() {
				uni.request({
					url: this.YM + '/api/store/brandCarList',
					method: 'POST',
					data: {
						brand_id: this.brand_id
					},
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					success: res => {
						console.log(res.data.data, '平台品牌车辆');

						for (var i = 0; i < res.data.data.platform_car_list.length; i++) {
							this.columns1[0].push(res.data.data.platform_car_list[i].name)
						}


						this.$forceUpdate()
					}
				})
			},
			ptclApi() {
				uni.request({
					url: this.YM + '/api/store/brandCarList',
					method: 'POST',
					data: {
						brand_id: this.brand_id
					},
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					success: res => {
						console.log(res.data.data, '平台品牌车辆');

						for (var i = 0; i < res.data.data.brand_list.length; i++) {
							this.columns[0].push(res.data.data.brand_list[i].name)
						}


						for (var i = 0; i < res.data.data.platform_car_list.length; i++) {
							this.columns1[0].push(res.data.data.platform_car_list[i].name)
						}

						this.ptai.brand_list = res.data.data.brand_list
						this.ptai.platform_car_list = res.data.data.platform_car_list
						this.$forceUpdate()
					}
				})
			},
			SJ() {
				let data = {
					type: this.type,
					platform_car_id: this.platform_car_id,
					month_price: this.month_price,
					day_price: this.day_price,
					// hour_price: this.hour_price,
					total_stock: this.total_stock,
					images: this.images.toString(),
					intro: this.intro.toString(),
					cash_price: this.cash_price,
					id: this.id
				}
				uni.request({
					url: this.YM + '/api/store/carEdit',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					data,
					success: res => {
						console.log(res, '上架');
						if (res.data.code == 1) {
							if (this.type == 2) {
								uni.navigateTo({
									url: '/pages/success/successSJ'
								})
							} else {
								uni.showToast({
									title: '编辑成功',
									icon: 'none',
									duration: 2000
								});
								setTimeout(() => {
									uni.navigateBack()
								}, 2000)
							}
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
			Spp() {
				console.log('点击品牌选择');
				this.show = true
			},
			confirm(e) {
				console.log(e.value[0]);

				this.valueppai = e.value[0]
				this.columns1[0] = []
				for (var i = 0; i < this.ptai.brand_list.length; i++) {
					if (this.ptai.brand_list[i].name == e.value[0]) {
						console.log('匹配的', this.ptai.brand_list[i]);
						this.brand_id = this.ptai.brand_list[i].id
						this.ptclApi()
					}
				}

				this.valuept = '' //初始化平台车辆
				this.platform_car_id = ''

				this.show = false
				this.$forceUpdate()
			},
			cancel() {
				this.show = false
				this.show1 = false
			},
			Spt() {
				console.log('点击平台选择');
				this.show1 = true
			},
			confirm1(e) {
				console.log(e.value[0]);
				this.valuept = e.value[0]

				for (var i = 0; i < this.ptai.platform_car_list.length; i++) {
					if (this.ptai.platform_car_list[i].name == e.value[0]) {
						console.log('匹配的', this.ptai.platform_car_list[i]);
						this.platform_car_id = this.ptai.platform_car_list[i].id
					}
				}

				this.show1 = false
			},

		}
	}
</script>

<style>
	.cx {
		width: 100vw;
		min-height: 100vh;
		background-color: #F5F5F5;
		float: left;
	}

	.cx_box {
		width: 694rpx;
		/* height: 640rpx; */
		background: #FFFFFF;
		border-radius: 20rpx;
		margin: 20rpx auto 0;
		padding-top: 32rpx;
		padding-bottom: 25rpx;
	}

	.cx_box_title {
		width: 93%;
		height: 40rpx;
		display: flex;
		align-items: center;
		margin: 0 auto;
		font-family: PingFang SC, PingFang SC;
		font-weight: bold;
		font-size: 30rpx;
		color: #000000;
	}

	.cx_box_title_shu {
		width: 8rpx;
		height: 36rpx;
		background: #ED9939;
		border-radius: 40rpx;
		margin-right: 20rpx;
	}

	.cx_box_list {
		width: 90%;
		height: 100rpx;
		margin: 10rpx auto 0;
		display: flex;
		font-weight: bold;
		font-size: 28rpx;
		color: #FF0000;
		align-items: center;
	}

	.cx_box_list_input {
		width: 450rpx;
		height: 100%;
		border-bottom: 2rpx solid #F5F5F5;
		display: flex;
		align-items: center;
		font-family: PingFang SC, PingFang SC;
		margin-left: 5rpx;
		font-weight: 500;
	}

	.cx_box_list_input_icon {
		width: 32rpx;
		height: 32rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 10rpx;
	}

	.cx_box_up {
		width: 186rpx;
		height: 136rpx;
		border-radius: 8rpx 8rpx 8rpx 8rpx;
		border: 2rpx solid #CBCBCB;
		text-align: center;
		line-height: 136rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 50rpx;
		color: #999999;
		font-weight: 600;
		margin-left: 15rpx;
		margin-top: 15rpx;
		position: relative;
	}

	.cx_box_up_out {
		width: 35rpx;
		height: 35rpx;
		position: absolute;
		top: -15rpx;
		right: -15rpx;
		z-index: 7;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.cx_btn {
		width: 100%;
		height: 180rpx;
		position: fixed;
		left: 0;
		bottom: 0;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #FFFFFF;
		z-index: 9;
	}

	.cx_btn_text {
		width: 632rpx;
		height: 92rpx;
		background: #ED9939;
		border-radius: 54rpx;
		font-family: PingFang SC, PingFang SC;
		font-weight: bold;
		font-size: 28rpx;
		color: #FFFFFF;
		text-align: center;
		line-height: 92rpx;
	}
</style>
