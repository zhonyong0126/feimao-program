<template>
	<view class="all">
		<view class="all_box">
			<view class="all_box_list">
				<view class="all_box_list_L">联系人</view>
				<view class="all_box_list_R">
					<u--input placeholder="请输入" border="false" v-model="value4"></u--input>
				</view>
			</view>

			<view class="all_box_list">
				<view class="all_box_list_L">手机号</view>
				<view class="all_box_list_R">
					<u--input placeholder="请输入" border="false" v-model="value5"></u--input>
				</view>
			</view>

			<view class="all_box_list">
				<view class="all_box_list_L No">所在地区</view>
				<view class="all_box_list_R Right" @click="open">
					<text v-if="str==''">请选择</text>
					<text style="color: black;" v-else>{{str}}</text>
					<view class="all_box_list_R_icon">
						<image src="../../static/wordF.png" class="imageBFB"></image>
					</view>
				</view>
			</view>

			<view class="all_box_list aotua">
				<view class="all_box_list_L No G">详细地址</view>
				<view class="all_box_list_R G"></view>
				<view class="all_box_list_input" @click="SeleteDiZhi">
					<u--textarea v-model="valuetext" border='none' height='100' placeholder="请输入"></u--textarea>
				</view>
			</view>
		</view>


		<!-- 按钮 -->
		<view class="A">
			<view class="Btn" @click="ClickBaoc" v-if="SHo==2">保存</view>
			<view class="Btn" @click="ClickBaoAc" v-if="SHo==1">保存</view>
		</view>

		<cityPicker :column="column" :default-value="defaultValue" :mask-close-able="maskCloseAble" @confirm="confirm"
			@cancel="cancel" :visible="visible" />
	</view>
</template>

<script>
	import cityPicker from '../../uni_modules/components/piaoyi-cityPicker/piaoyi-cityPicker.vue'
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				value4: '', //联系人
				value5: '', //手机号
				valuetext: '', //详细地址
				visible: false,
				maskCloseAble: true,
				str: '',
				defaultValue: '513401',
				column: 3,
				SHo: 2,
				ARA: '',
				lat:'',
				lng:'',
				isSaving: false
			}
		},
		components: {
			cityPicker
		},
		created() {
			if (uni.getStorageSync('DZA') == '001') {
				console.log(uni.getStorageSync('BianJiDZ'));
				let Ar = uni.getStorageSync('BianJiDZ')
				this.ARA = uni.getStorageSync('BianJiDZ')
				this.SHo = 1
				this.value4 = Ar.name
				this.value5 = Ar.mobile
				this.valuetext = Ar.address
				this.str = Ar.administrative
				this.lat = Ar.lat
				this.lng = Ar.lng
			} else {
				console.log('不做操作');
				this.SHo = 2
			}
		},
		methods: {
			SeleteDiZhi() {
				if(this.valuetext==''){
					uni.chooseLocation({
						success: res => {
							this.lat = res.latitude
							this.lng = res.longitude
							this.valuetext = res.name
							console.log(res, '点击得');
						},
						fail: err => {
							console.log(err, 'err-err-err');
							uni.showToast({
								title: '授权失败',
								icon: 'none',
								duration: 1000
							})
						}
					})
				}
				console.log('点击了详细地址');
			},
			ClickBaoc() {
				if (!this.validateAddress() || this.isSaving) return
				console.log('联系人', this.value4);
				console.log('手机号', this.value5);
				console.log('详细地址', this.valuetext);
				console.log('所在地区', this.str);
				let data = {
					mobile: this.value5,
					address: this.valuetext,
					name: this.value4,
					administrative: this.str,
					lat:this.lat,
					lng:this.lng
				}
				this.isSaving = true
				uni.request({
					url: this.YM + '/api/user/addressAdd',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token'),
					},
					data,
					success: res => {
						this.isSaving = false
						console.log(res, '添加地址');
						if (res.data.code == 1) {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 1500
							});
							setTimeout(() => {
								uni.navigateBack()
							}, 1500)
						} else {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 1500
							});
							return false
						}
					},
					fail: () => {
						this.isSaving = false
						uni.showToast({ title: '保存失败，请稍后重试', icon: 'none' })
					}
				})
			},
			//编辑地址
			ClickBaoAc() {
				if (!this.validateAddress() || this.isSaving) return
				let data = {
					address_id: this.ARA.id,
					mobile: this.value5,
					address: this.valuetext,
					name: this.value4,
					administrative: this.str,
					lat: this.lat,
					lng: this.lng
				}
				this.isSaving = true
				uni.request({
					url: this.YM + '/api/user/addressEdit',
					method: 'POST',
					header: {
						'token': uni.getStorageSync('token'),
					},
					data,
					success: res => {
						this.isSaving = false
						console.log(res, '编辑成功');
						if (res.data.code == 1) {
							uni.showToast({
								title: '修改成功',
								icon: 'none',
								duration: 1500
							});
							setTimeout(() => {
								uni.navigateBack()
							}, 1500)
						} else {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 1500
							});
							return false
						}
					},
					fail: () => {
						this.isSaving = false
						uni.showToast({ title: '保存失败，请稍后重试', icon: 'none' })
					}
				})
			},
			validateAddress() {
				if (!this.value4.trim() || !/^1[3-9]\d{9}$/.test(this.value5) ||
					!this.str || !this.valuetext.trim() || !Number(this.lat) || !Number(this.lng)) {
					uni.showToast({ title: '请完整填写联系人、手机号和定位地址', icon: 'none' })
					return false
				}
				return true
			},
			open() {
				this.visible = true
			},
			confirm(val) {
				console.log(val)
				// this.str = JSON.stringify(val)
				this.str = `${val.provinceName}-${val.cityName}-${val.areaName}`
				this.visible = false
			},
			cancel() {
				this.visible = false
			}
		}
	}
</script>

<style>
	.all {
		width: 100vw;
		height: 100vh;
		background-color: #F7F7F7;
	}

	.all_box {
		width: 100%;
		height: auto;
		background-color: #FFFFFF;
		float: left;
		padding-top: 20rpx;
	}

	.all_box_list {
		width: 700rpx;
		height: 110rpx;
		/* background-color: yellow; */
		margin: 0 auto;
		border-bottom: 1rpx solid #EFEFEF;
		display: flex;
	}

	.all_box_list_L {
		width: 150rpx;
		height: 100%;
		line-height: 110rpx;
		/* background-color: red; */
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #1B1B1B;
		letter-spacing: 0.5em;
		text-align: center;
	}

	.No {
		letter-spacing: 0.1em;
	}

	.Right {
		justify-content: space-between;
		font-size: 29rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #c8ccd4;
		text-indent: 0.8em;
	}

	.all_box_list_R {
		width: 550rpx;
		height: 100%;
		/* background-color: darksalmon; */
		display: flex;
		align-items: center;
	}

	.all_box_list_R_icon {
		width: 12rpx;
		height: 23rpx;
		/* background-color: green; */
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.all_box_list_input {
		width: 700rpx;
		height: 150rpx;
		margin: 0 auto;
		/* background-color: violet; */
	}

	.aotua {
		height: 270rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-wrap: wrap;
	}

	.G {
		height: 110rpx;
	}

	.A {
		width: 100%;
		height: 100rpx;
		display: flex;
		align-content: center;
		justify-content: center;
		margin-top: 50rpx;
		float: left;
	}

	.Btn {
		width: 556rpx;
		height: 88rpx;
		background: #ED9939;
		border-radius: 44rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #FFFFFF;
		text-align: center;
		line-height: 88rpx;
	}
</style>
