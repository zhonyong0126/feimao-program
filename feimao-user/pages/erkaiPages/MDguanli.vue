<template>
	<view class="guanli">
		<view class="Cl_nav">
			<view class="Cl_nav_title">门店管理</view>
		</view>

		<view class="guanli_box">

			<view class="guanli_box_box">
				<view class="cx_box_title">
					<view class="cx_box_title_shu"></view>
					<text>基础设置</text>
				</view>

				<view class="cx_box_list" style="align-items: center;justify-content: space-between;">
					<text><text style="color: #000000;">门店设置</text></text>
					<view class="cx_box_list_up" @click="Up">
						<image :src="YM + info.avatar" class="imageBFB"></image>
					</view>
				</view>

				<view class="cx_box_list">
					<text><text style="color: #000000;">门店名称</text></text>
					<view class="cx_box_list_input" style="width: 510rpx;">
						<u--input placeholder="请输入" border="false" v-model="info.name" :disabled='false'
							disabledColor='#FFFFFF' inputAlign='right'></u--input>
					</view>
				</view>

				<view class="cx_box_list">
					<text><text style="color: #000000;">营业时间</text></text>
					<view style="display: flex;
					align-items: center;
					justify-content: flex-end;height: 100%;">
						<view class="cx_box_list_input" @click="focus">
							<u--input placeholder="请选择" border="false" v-model="info.business" :disabled='true'
								disabledColor='#FFFFFF' inputAlign='right'></u--input>
						</view>
						<view class="cx_box_list_input_icon">
							<image src="../../static/ALLRIGHT.png" class="imageBFB"></image>
						</view>
					</view>
				</view>

				<view class="cx_box_list">
					<text><text style="color: #000000;">联系人</text></text>
					<view class="cx_box_list_input" style="width: 533rpx;">
						<u--input placeholder="请输入" border="false" v-model="info.nickname" :disabled='false'
							disabledColor='#FFFFFF' inputAlign='right'></u--input>
					</view>
				</view>

				<view class="cx_box_list">
					<text><text style="color: #000000;">联系电话</text></text>
					<view class="cx_box_list_input" style="width: 510rpx;">
						<u--input placeholder="请输入" border="false" v-model="info.mobile" :disabled='false'
							disabledColor='#FFFFFF' inputAlign='right'></u--input>
					</view>
				</view>
				
				<view class="cx_box_list">
					<text><text style="color: #000000;">店铺区域</text></text>
					<view style="display: flex;
					align-items: center;
					justify-content: flex-end;height: 100%;" @click="Cditu">
						<view class="cx_box_list_input"
							style="display: flex;align-items: center;justify-content: flex-end;color: #666666;">
							<text>{{info.administrative}}</text>
						</view>
					</view>
				</view>

				<view class="cx_box_list">
					<text><text style="color: #000000;">店铺地址</text></text>
					<view style="display: flex;
					align-items: center;
					justify-content: flex-end;height: 100%;" @click="Map">
						<view class="cx_box_list_input"
							style="display: flex;align-items: center;justify-content: flex-end;color: #666666;">
							<view
								style="width: 44rpx;height: 44rpx;display: flex;align-items: center;justify-content: center;">
								<image src="../../static/ordinwei.png" class="imageBFB"></image>
							</view>
							<text>{{info.address}}</text>
						</view>
					</view>
				</view>

				<view class="cx_box_list">
					<text><text style="color: #000000;">门店服务</text></text>
					<view style="display: flex;
					align-items: center;
					justify-content: flex-end;height: 100%;">
						<view class="cx_box_list_input"
							style="display: flex;align-items: center;justify-content: flex-end;color: #666666;">
							<view class="radaidwa" style="margin-right: 15rpx;" v-for="(item,index) in FWdata"
								:key="index">
								<radio :value="value" :checked="item.isA" @click="RadioClick(item)" class="RadioCss"
									color='#ED9939' />
								<text>{{item.name}}</text>
							</view>
						</view>
					</view>
				</view>

				<view class="cx_box_list">
					<text><text style="color: #000000;">门店轮播图</text></text>
					<view></view>
				</view>

				<view style="width:90%;height: auto;display: flex;align-items: center;flex-wrap: wrap;">
					<view class="cx_box_up" v-for="(item,index) in info.image" :key="index">
						<view class="cx_box_up_out" @click="Cout(index)">
							<image src="../../static/out1.png" class="imageBFB"></image>
						</view>
						<image :src="YM + item" class="imageBFB"></image>
					</view>

					<view class="cx_box_up" v-if="info.image.length<6" @click="Upimages">+</view>
				</view>
			</view>


			<view class="guanli_box_box">
				<view class="cx_box_title">
					<view class="cx_box_title_shu"></view>
					<text>门店设置</text>
				</view>

				<view class="cx_box_list">
					<text><text style="color: #000000;">可租总车辆</text></text>
					<view style="display: flex;
					align-items: center;
					justify-content: flex-end;height: 100%;">
						<view class="cx_box_list_input">
							<u--input placeholder="请输入" border="false" v-model="info.car_num" :disabled='false'
								disabledColor='#FFFFFF' inputAlign='right'></u--input>
						</view>

					</view>
				</view>

				<view class="cx_box_list">
					<text><text style="color: #000000;">最低起租价</text></text>
					<view style="display: flex;
					align-items: center;
					justify-content: flex-end;height: 100%;">
						<view class="cx_box_list_input">
							<u--input placeholder="请输入" border="false" v-model="info.min_price" :disabled='false'
								disabledColor='#FFFFFF' inputAlign='right'></u--input>
						</view>

					</view>
				</view>
			</view>

			<view class="btn" @click="Baoc">保存</view>
			<view class="btn1" @click="TC">退出登录</view>

		</view>


		<view style="width: 100%;height: 250rpx;float: left;"></view>

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

		<u-modal :show="show" title="确定退出登录?" :showCancelButton='true' @confirm='confirm' @cancel='cancel'></u-modal>
		<u-picker :show="showdate" ref="uPicker" :columns="columns" @confirm="confirmdate" @cancel='cancel'></u-picker>
		<cityPicker :column="column" :default-value="defaultValue" :mask-close-able="maskCloseAble" @confirm="confirmxe"
			@cancel="cancel" :visible="visible" />
	</view>
</template>

<script>
	import cityPicker from '../../uni_modules/piaoyi-cityPicker/components/piaoyi-cityPicker/piaoyi-cityPicker.vue'
	export default {
		data() {
			return {
				columns: [
					['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00', '07:00', '08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00', '23:00'],
					['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00', '07:00', '08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00', '23:00'],
				],
				show: false,
				YM: getApp().globalData.apiBase,
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
						isA: false
					},
					{
						name: '门店管理',
						icon: '../../static/tabbar/men1.png',
						iconI: '../../static/tabbar/men2.png',
						isA: true
					}
				],
				FWdata: [{
						name: '到店取车',
						isA: false
					},
					{
						name: '送车上门',
						isA: false
					}
				],
				type: 1, //请求类型：1查看 2修改提交
				info: {
					name: '',
					avatar: '',
					nickname: '',
					mobile: '',
					image: [],
					business: '',
					address: '',
					lat: '',
					lng: '',
					pick_type: '',
					car_num: '',
					min_price: '',
					administrative:''
				},
				showdate: false,
				visible: false,
				maskCloseAble: true,
				str: '',
				defaultValue: '',
				column: 3
			}
		},
		components: {
			cityPicker
		},
		created() {
			this.InfoApi()
		},
		methods: {
			Cout(index){
				 this.info.image.splice(index, 1);
			},
			confirmxe(val) {
				console.log(val)
				this.info.administrative = val.name
				this.visible = false
			},
			Cditu() {
				console.log('Cditu');
				this.visible = true
			},
			confirmdate(e) {
				console.log(e, '选择的时间');
				this.info.business=`${e.value[0]} - ${e.value[1]}`
				console.log(this.info.business);
				this.showdate=false
			},
			focus() {
				console.log('focus');
				this.showdate = true
			},
			Map() {
				uni.chooseLocation({
					success: res => {
						this.info.lat = res.latitude
						this.info.lng = res.longitude
						this.info.address = res.name
						console.log(res, '点击得');
					},
					fail: err => {
						console.log(err, 'err-err-err');
						wx.showToast({
							title: '授权失败',
							icon: 'none',
							duration: 1000
						})
					}
				})
			},
			Upimages() {
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
								this.info.image.push(Ima.data.url)
							}
						});
					}
				});
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
								this.info.avatar = Ima.data.url
							}
						});
					}
				});
			},
			Baoc() {
				this.info.type = 2
				this.info.image = this.info.image.toString()

				if (this.FWdata.every(item => item.isA)) {
					this.info.pick_type = 2
				} else {
					this.info.pick_type = 1
				}


				uni.request({
					url: this.YM + '/api/store/edit',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					data: this.info,
					success: res => {
						if (res.data.code == 1) {
							uni.showToast({
								title: '',
								icon: 'success',
								duration: 2000
							});
							this.info.image=[] //先初始化 然后thus.InfoApi会再次渲染内容
							this.InfoApi()
						}
					}
				})
			},
			InfoApi() {
				uni.request({
					url: this.YM + '/api/store/edit',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					data: {
						type: this.type
					},
					success: res => {
						console.log(res.data.data, '门店信息');
						this.info = res.data.data

						if (res.data.data.pick_type == 1) {
							this.FWdata[0].isA = true
						} else if (res.data.data.pick_type == 2) {
							for (var i = 0; i < this.FWdata.length; i++) {
								this.FWdata[i].isA = true
							}
						}

					}
				})
			},
			confirm() {
				uni.request({
					url: this.YM + '/api/store/logout',
					header: {
						'token': uni.getStorageSync('MDtoken')
					},
					method: 'POST',
					success: res => {
						console.log(res, '退出');
						if (res.data.code == 1) {
							this.show = false
							uni.removeStorageSync('MDtoken')
							uni.switchTab({
								url:'/pages/index/word'
							})
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
			cancel() {
				this.showdate=false
				this.show = false
				this.visible = false
			},
			TC() {
				this.show = true
			},
			RadioClick(item) {
				item.isA = !item.isA
			},
			TabbarC(item) {
				console.log('点击了', item.name);
				if (item.name == '首页') {
					uni.redirectTo({
						url: '/pages/erkaiPages/MDindex'
					})
				} else if (item.name == '车辆管理') {
					uni.redirectTo({
						url: '/pages/erkaiPages/MDcheliang'
					})
				}
			},
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
	.guanli {
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

	.guanli_box {
		width: 100%;
		height: auto;
		float: left;
	}

	.guanli_box_box {
		width: 694rpx;
		background: #FFFFFF;
		border-radius: 20rpx;
		margin: 20rpx auto 0;
		padding-top: 20rpx;
		padding-bottom: 20rpx;
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

	.cx_box_list_up {
		width: 96rpx;
		height: 96rpx;
		border-radius: 12rpx;
		overflow: hidden;
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

	.radaidwa {
		width: auto;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-weight: 400;
		font-size: 28rpx;
		color: #666666;
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
		margin-top: 10rpx;
		margin-left: 20rpx;
		position: relative;
	}

	.cx_box_up_out {
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		position: absolute;
		right: -20rpx;
		top: -20rpx;
		z-index: 5;
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

	.RadioCss {
		transform: scale(0.8)
	}

	.btn,
	.btn1 {
		width: 632rpx;
		height: 88rpx;
		background: #ED9939;
		border-radius: 100rpx;
		font-family: PingFang SC, PingFang SC;
		font-weight: bold;
		font-size: 28rpx;
		color: #FFFFFF;
		text-align: center;
		line-height: 88rpx;
		margin: 30rpx auto 0;
	}

	.btn1 {
		border: 2rpx solid #ED9939;
		color: #ED9939;
		background-color: #FFFFFF;
		margin: 42rpx auto 0;
	}
</style>
