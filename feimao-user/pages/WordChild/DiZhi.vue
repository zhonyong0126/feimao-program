<template>
	<view class="diz">

		<view class="diz_list_box">
			<view class="diz_list" v-for="(item,index) in List" :key="index">
				<view class="diz_list_L">
					<view class="diz_list_L_title">{{item.name}} {{item.mobile}}</view>
					<view class="diz_list_L_text">{{item.administrative}}-{{item.address}}</view>
				</view>
				<view class="diz_list_R">
					<view class="diz_list_R_B" @click="ClickBianji(item)"><u>编辑</u></view>
					<view class="diz_list_R_S" @click="ClickDelete(item)"><u>删除</u></view>
				</view>
			</view>


		</view>
		<u-empty mode="address" icon="https://cdn.uviewui.com/uview/empty/car.png" v-if="List==''">
		</u-empty>
		<view style="width: 100%;height: 88rpx;background-color: transparent;"></view>
		<view class="diz_btn" @click="ClickAllDz">新增地址</view>

		<!-- 删除地址弹出 -->
		<u-modal :show="show" :title="title" :showCancelButton='true' @confirm='confirm' @cancel='cancel'></u-modal>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				show: false,
				title: '确定删除当前地址？',
				List: [],
				DZID: '',
				isDeleting: false
			}
		},
		onShow() {
			this.dizlistApi()
		},
		methods: {
			// 地址列表
			dizlistApi() {
				uni.request({
					url: this.YM + '/api/user/addressList',
					method: 'POST',
					header:{
						'token': uni.getStorageSync('token')
					},
					success: res => {
						this.List = res.data.code == 1 && res.data.data
							? (res.data.data.data || [])
							: []
						if (res.data.code != 1) {
							uni.showToast({ title: res.data.msg || '地址加载失败', icon: 'none' })
						}
						console.log(this.List, '地址列表');
					},
					fail: () => {
						this.List = []
						uni.showToast({ title: '地址加载失败，请稍后重试', icon: 'none' })
					}
				})
			},
			//点击了新增地址
			ClickAllDz() {
				console.log('新增地址');
				uni.setStorageSync('DZA', '002')
				uni.navigateTo({
					url: '/pages/WordChild/AllDizhi'
				})
			},
			//编辑地址
			ClickBianji(item) {
				uni.setStorageSync('BianJiDZ', item)
				uni.setStorageSync('DZA', '001')
				uni.navigateTo({
					url:'/pages/WordChild/AllDizhi'
				})
			},
			//删除地址
			ClickDelete(item) {
				this.show = true
				this.DZID = item.id
			},
			confirm() {
				if (this.isDeleting) return
				this.show = false
				console.log('点击了确定');
				let data = {
					address_id: this.DZID
				}
				this.isDeleting = true
				uni.request({
					url: this.YM + '/api/user/addressDel',
					method: 'POST',
					header:{
						'token': uni.getStorageSync('token')
					},
					data,
					success: res => {
						this.isDeleting = false
						console.log(res, '删除地址');
						if (res.data.code == 1) {
							uni.showToast({
								title: res.data.msg,
								icon: 'none',
								duration: 2000
							});
							this.dizlistApi()
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
						this.isDeleting = false
						uni.showToast({ title: '删除失败，请稍后重试', icon: 'none' })
					}
				})
			},
			cancel() {
				this.show = false
			}
		}
	}
</script>

<style>
	.diz {
		width: 100vw;
		min-height: 100vh;
		background-color: #F7F7F7;
	}

	.diz_list_box {
		width: 100%;
		height: auto;
		background-color: #FFFFFF;
	}

	.diz_list {
		width: 100%;
		height: 155rpx;
		/* background-color: antiquewhite; */
		border-bottom: 1rpx solid #EEEEEE;
		display: flex;
		justify-content: center;
	}

	.diz_list_L {
		width: 70%;
		height: 100%;
		/* background-color: aqua; */
	}

	.diz_list_L_title {
		width: 100%;
		height: 50rpx;
		/* background-color: violet; */
		line-height: 50rpx;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #010101;
		margin-top: 20rpx;
	}

	.diz_list_L_text {
		width: 100%;
		height: 80rpx;
		/* background-color: antiquewhite; */
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
	}

	.diz_list_R {
		width: 20%;
		height: 100%;
		/* background-color: mediumpurple; */
		display: flex;
	}

	.diz_list_R_B,
	.diz_list_R_S {
		width: 50%;
		height: 50rpx;
		margin-top: 20rpx;
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		text-decoration: underline;
		color: #333333;

	}

	.diz_list_R_S {
		color: red;
	}

	.diz_btn {
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
		position: fixed;
		bottom: 15rpx;
		left: 50%;
		transform: translate(-50%);
	}
</style>
