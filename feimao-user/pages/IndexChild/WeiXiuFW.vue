<template>
	<view class="weixiu">

		<view class="weixiu_src">
			<image src="../../static/WXICON.png" class="imageBFB" v-if="type==1"></image>
			<image src="../../static/WXWS.png" class="imageBFB" v-if="type==2"></image>
		</view>

		<view class="weixiu_phone" v-if="type==1" @click="cPHONE">维修电话：{{PHONE1}}</view>
		<view class="weixiu_phone" v-if="type==2" @click="cPHONE1">咨询电话：{{PHONE2}}</view>

	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				type: 1,
				PHONE1:'',
				PHONE2:''
			}
		},
		onLoad(options) {
			this.type = options.type
			let title = ''
			if (options.type == 1) {
				title = '维修服务'
			} else {
				title = '商务合作'
			}
			uni.setNavigationBarTitle({
				title: title
			});

			uni.setNavigationBarColor({
				frontColor: '#000000', // 前景色，包括按钮颜色
				backgroundColor: '#F7F7F7', // 背景色
			});

		},
		onShow() {
			uni.request({
				url:this.YM + '/api/index/config',
				method:'POST',
				success:res=>{
					
					const targetData1 = res.data.data.find(item => item.name === "repair_phone");
					if (targetData1) {
						this.PHONE1 = targetData1.value
					}
					
					
					const targetData2 = res.data.data.find(item => item.name === "service_mobile");
					if (targetData2) {
						this.PHONE2 = targetData2.value
					}
				}
			})
		},
		methods: {
cPHONE(){
	uni.makePhoneCall({
		phoneNumber:this.PHONE1
	})
},
cPHONE1(){
	uni.makePhoneCall({
		phoneNumber:this.PHONE2
	})
}
		}
	}
</script>

<style>
	.weixiu {
		width: 100vw;
		min-height: 100vh;
		background-color: #F7F7F7;
		float: left;
	}

	.weixiu_src {
		width: 300rpx;
		height: 300rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin: 50rpx auto 0;
	}

	.weixiu_phone {
		width: 100%;
		height: 70rpx;
		text-align: center;
		line-height: 70rpx;
		color: rgba(0, 0, 0, 1);
		font-size: 32rpx;
		font-weight: 600;
		margin: 30rpx auto 0;
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

	/* //点击商品弹出 */
	.Allup {
		width: 100%;
		/* height: 600rpx; */
		/* background-color: pink; */
		float: left;
	}

	.Allup_box {
		width: 100%;
		height: 180rpx;
		/* background-color: greenyellow; */
		margin-top: 30rpx;
		border-bottom: 1rpx solid #E9E9E9;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Allup_box_all {
		width: 90%;
		height: 140rpx;
		/* background-color: darkseagreen; */
		display: flex;
		align-items: center;
	}

	.Allup_box_all_icon {
		width: 140rpx;
		height: 140rpx;
		background: #808080;
		border-radius: 20rpx;
		overflow: hidden;
		background-color: green;
	}

	.Allup_box_all_text {
		width: 400rpx;
		height: 140rpx;
		/* background-color: cyan; */
		margin-left: 20rpx;
	}

	.Allup_box_all_text_T,
	.Allup_box_all_text_B {
		width: 100%;
		height: 50%;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #101010;
		line-height: 80rpx;
	}

	.Allup_box_all_text_B {
		font-size: 38rpx;
		font-family: DIN;
		font-weight: bold;
		color: #E64638;
		line-height: 50rpx;
	}

	.Allup_box_list {
		width: 100%;
		height: 220rpx;
		border-bottom: 1rpx solid #E9E9E9;
		/* background-color: violet; */
	}

	.Allup_box_list_Oen {
		width: 90%;
		height: 108rpx;
		/* background-color: green; */
		margin: 20rpx auto 0;
		display: flex;
	}

	.Allup_box_list_Oen_icon {
		width: 108rpx;
		height: 108rpx;
		background: #878787;
		border-radius: 10rpx;
		overflow: hidden;
		/* background-color: green; */
	}

	.Allup_box_list_Oen_text {
		width: 500rpx;
		height: 108rpx;
		margin-left: 20rpx;
	}

	.Allup_box_list_Oen_textT,
	.Allup_box_list_Oen_textB {
		width: 100%;
		height: 50%;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #101010;
		line-height: 64rpx;
	}

	.Allup_box_list_Oen_textB {
		font-size: 26rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #797876;
		line-height: 44rpx;
	}

	.Allup_box_list_Two {
		width: 90%;
		height: 80rpx;
		/* background-color: yellow; */
		margin: 15rpx auto 0;
		display: flex;
	}

	.Allup_box_list_Two_L,
	.Allup_box_list_Two_R {
		width: 50%;
		height: 100%;
		/* background-color: darkseagreen; */
		display: flex;
		align-items: center;
	}

	.Allup_box_list_Two_R {
		/* background-color: khaki; */
		display: flex;
		align-items: center;
		justify-content: flex-end;
		font-size: 30rpx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #E64638;
	}

	.Allup_box_list_Two_LD {
		width: 104rpx;
		height: 36rpx;
		background: #FFFFFF;
		border: 2rpx solid #D5D5D5;
		border-radius: 6rpx;
		font-size: 22rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #A1A1A1;
		margin-right: 20rpx;
		text-align: center;
		line-height: 36rpx;
	}

	.Allup_box_list_Two_R_icon {
		width: 34rpx;
		height: 34rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		/* background-color: green; */
		margin-left: 15rpx;
	}
</style>
