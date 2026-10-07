<template>
	<view class="zaixian">

		<!-- 下拉选择 -->
		<view class="selete">
			<!-- 1 -->
			<view class="selete_text" :class="isA?'selete_text':'selete_textA'" @click="ClickISa">
				<text style="color: #ED9939;" v-if="AQRQ==true">价格</text>
				<text v-else>价格</text>
				<view class="selete_text_icon" v-if="isA==true">
					<image src="../../static/H.png" class="imageBFB"></image>
				</view>
				<view class="selete_text_icon deg" v-if="isA==false">
					<image src="../../static/C.png" class="imageBFB"></image>
				</view>
			</view>
			<view class="selete_price" v-if="isA==false">
					<view class="selete_price_list" v-for="(price,index) in PriceData" :key="index">
						<view class="selete_price_list_left"
							:class="price.items?'selete_price_list_leftA':'selete_price_list_left'"
							@click="ClickPrice(price)">{{price.title}}</view>
						<view class="selete_price_list_right">
							<view class="selete_price_list_right_icon" v-if="price.items==true">
								<image src="../../static/Cdui.png" class="imageBFB"></image>
							</view>
						</view>
					</view>


				<view class="selete_price_btn_box">
					<view class="selete_price_btn_box1" @click="ClickPricecz">重置</view>
					<view class="selete_price_btn_box2" @click="ClickPriceQD">确定</view>
				</view>
			</view>

			<!-- 2 -->
			<view class="selete_text" :class="isB?'selete_text':'selete_textA'" @click="ClickISb">
				<text style="color: #ED9939;" v-if="QRQ==true">品牌</text>
				<text v-else>品牌</text>
				<view class="selete_text_icon" v-if="isB==true">
					<image src="../../static/H.png" class="imageBFB"></image>
				</view>
				<view class="selete_text_icon deg" v-if="isB==false">
					<image src="../../static/C.png" class="imageBFB"></image>
				</view>
			</view>
			<view class="selete_price" v-if="isB==false">
				<view class="selete_price_box">
				<view class="selete_price_list" v-for="(Brand,index) in BrandData" :key="index">
					<view class="selete_price_list_left"
						:class="Brand.status_text!==''?'selete_price_list_leftA':'selete_price_list_left'"
						@click="ClickBrand(Brand)">{{Brand.name}}</view>
					<view class="selete_price_list_right">
						<view class="selete_price_list_right_icon" v-if="Brand.status_text!==''">
							<image src="../../static/Cdui.png" class="imageBFB"></image>
						</view>
					</view>
				</view>
				</view>

				<view class="selete_price_btn_box">
					<view class="selete_price_btn_box1" @click="ClickCZ">重置</view>
					<view class="selete_price_btn_box2" @click="ClickPriceQD">确定</view>
				</view>
			</view>
		</view>


		<!-- 列表 -->
		<view class="zaixian_list">

			<view class="zaixian_list_box" @click="ClickGooDs(item)" v-for="(item,index) in CheA" :key="index">
				<view class="zaixian_list_box_image">
					<image :src="YM + item.index_image" class="imageBFB"></image>
				</view>
				<view class="zaixian_list_box_tbox">
					<view class="zaixian_list_box_tbox_title">{{item.name}}</view>
					<view class="zaixian_list_box_tbox_text">
						<view class="zaixian_list_box_tbox_text_L">
							<text style="font-size: 28rpx;">￥</text>{{item.min_price}} <text
								style="font-size: 28rpx;">起</text>

						</view>
						<view class="zaixian_list_box_tbox_text_R"></view>
					</view>
				</view>
			</view>



		</view>
		<u-empty mode="list" icon="https://cdn.uviewui.com/uview/empty/car.png" v-if="CheA==''">
		</u-empty>

		<!-- //点击商品弹出 选择门店 -->
		<u-popup :show="show" :round="10" mode="bottom" @close="close" @open="open" :closeable='true'>
			<view class="Allup">

				<view class="Allup_box">
					<view class="Allup_box_all">
						<view class="Allup_box_all_icon">
							<image :src="YM + ClickDatas.index_image" class="imageBFB"></image>
						</view>
						<view class="Allup_box_all_text">
							<view class="Allup_box_all_text_T">{{ClickDatas.name}}</view>
							<view class="Allup_box_all_text_B">
								<text style="font-size: 28rpx;">￥</text>
								<text>{{ClickDatas.min_price}}</text>
								<text style="font-size: 28rpx;">起</text>
							</view>
						</view>
					</view>
				</view>

				<view class="Allup_box_list" v-for="(item,index) in DianPuData" :key="index">
					<view class="Allup_box_list_Oen" @click.stop="ClickXQ(item)">
						<view class="Allup_box_list_Oen_icon">
							<image :src="YM + item.merchant.index_image" class="imageBFB"></image>
						</view>
						<view class="Allup_box_list_Oen_text">
							<view class="Allup_box_list_Oen_textT">{{item.merchant.name}}</view>
							<view class="Allup_box_list_Oen_textB">{{item.merchant.address}}</view>
						</view>
					</view>
					<view class="Allup_box_list_Two">
						<view class="Allup_box_list_Two_L" @click.stop="ClickXQ(item)">
							<view class="Allup_box_list_Two_LD" v-if="item.merchant.pick_type==1">到店取车</view>
							<view class="Allup_box_list_Two_LD"
								v-if="item.merchant.pick_type==1 || item.merchant.pick_type==2">送车上门</view>
						</view>
						<view class="Allup_box_list_Two_R" @click.stop="ClickXQ(item)">
							<text>￥{{item.price}}</text>
							<view class="Allup_box_list_Two_R_icon" @click.stop="ClickXQ(item)">
								<image src="../../static/CICON.png" class="imageBFB"></image>
							</view>
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
				isA: true,
				isB: true,
				show: false,
				// 价格的
				PriceData: [{
						title: '全部',
						items: true
					},
					{
						title: '价格从低到高',
						items: false
					},
					{
						title: '价格从高到低',
						items: false
					}
				],
				// 品牌的
				BrandData: [],
				// 点击弹出的店铺数据
				DianPuData: [],
				CheA: '',
				Brand: 0, //选择品牌后的数据依赖
				selteprice: 0,
				pages: 1,
				ClickDatas: '',
				QRQ:false,
				AQRQ:false
			}
		},
		onShow() {
			this.PinPListApi() //品牌列表

			this.CheLiangListApi() //车辆列表

			uni.setStorageSync('current', 1)
			
			
			setInterval(()=>{
				if(this.Brand!==0){
					this.QRQ=true
				}else{
					this.QRQ=false
				}
				if(this.selteprice!==0){
					this.AQRQ=true
				}else{
					this.AQRQ=false
				}
			},500)
		},
		methods: {
			ClickXQ(item) {
				uni.setStorageSync('merchant_id', item.merchant_id)
				uni.redirectTo({
					url: '/pages/IndexChild/XQ'
				})
			},
			// 品牌列表
			PinPListApi() {
				uni.request({
					url: this.YM + '/api/index/brandList',
					method: 'POST',
					success: res => {
						this.BrandData = res.data.data
						console.log(this.BrandData, '品牌列表');
					}
				})
			},
			// 品牌重置
			ClickCZ() {
				this.Brand = 0
				this.CheLiangListApi()

				for (var i = 0; i < this.BrandData.length; i++) {
					this.BrandData[i].status_text = ''
				}

				console.log('点击了重置');

				this.isB = true
				this.QRQ=false
			},
			// 价格重置
			ClickPricecz() {
				this.selteprice = 0
				this.CheLiangListApi()

				for (var i = 0; i < this.PriceData.length; i++) {
					this.PriceData[i].items = false
				}
				this.PriceData[0].items = true

				this.isA = true
				this.AQRQ=false
			},
			// 车辆列表
			CheLiangListApi() {
				let data = {
					sort: this.selteprice, //价格排序0默认  1 从小到大 2从大到小
					brand_id: this.Brand, //0全部
					page: this.pages
				}
				uni.request({
					url: this.YM + '/api/index/bugCarList',
					method: 'POST',
					data,
					success: res => {
						console.log(res.data.data.data, '车辆列表');
						this.CheA = res.data.data.data
					},
				})
			},
			// 租车商家列表
			ZcgoodslistApi() {
				let data = {
					platform_car_id: this.ClickDatas.id
				}
				uni.request({
					url: this.YM + '/api/index/bugMerchantCarList',
					method: 'POST',
					data,
					success: res => {
						this.DianPuData = res.data.data
						console.log(res.data.data, '购车商家列表');
					},
				})
			},
			//点击了商品
			ClickGooDs(item) {

				console.log(item, '点击的商品');

				this.ClickDatas = item

				this.ZcgoodslistApi()

				this.show = true

			},
			open() {
				// console.log('open');
			},
			close() {
				this.show = false
				// console.log('close');
			},
			//点击了价格
			ClickISa() {
				this.isB = true
				this.isA = false
			},
			ClickPrice(price) {
				for (var i = 0; i < this.PriceData.length; i++) {
					this.PriceData[i].items = false
				}
				price.items = true

				this.selteprice = price

				if (price.title == '全部') {
					this.selteprice = 0
				} else if (price.title == '价格从低到高') {
					this.selteprice = 1
				} else if (price.title == '价格从高到低') {
					this.selteprice = 2
				}
			},
			ClickPriceQD() {
				this.isA = true
				this.isB = true


				console.log(this.Brand, '选择的品牌信息');

				this.CheLiangListApi() //调用商品列表
			},
			//点击了品牌
			ClickISb() {
				this.isA = true
				this.isB = false
			},
			ClickBrand(Brand) {
				for (var i = 0; i < this.BrandData.length; i++) {
					this.BrandData[i].status_text = ''
				}
				Brand.status_text = 'true'

				console.log(Brand, 'Brand-Brand-Brand');

				this.Brand = Brand.brand_id
			}
		}
	}
</script>

<style>
	.zaixian {
		width: 100vw;
		min-height: 100vh;
		background-color: #F7F7F7;
	}

	/* 下拉选择 */
	.selete {
		width: 100%;
		height: 80rpx;
		background-color: #FFFFFF;
		display: flex;
		position: relative;
	}

	.selete_text,
	.selete_textA {
		flex: 2;
		height: 100%;
		/* background-color: violet; */
		color: rgba(16, 16, 16, 1);
		font-size: 26rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-weight: 600;
	}

	.selete_textA {
		color: #ED9939;
		font-weight: 700;
	}

	.selete_text_icon {
		width: 22rpx;
		height: 22rpx;
		/* background-color: green; */
		margin-left: 10rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		text-align: center;
		line-height: 22rpx;
	}

	.deg {
		width: 22rpx;
		height: 22rpx;
		/* background-color: green; */
		margin-left: 10rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		text-align: center;
		line-height: 22rpx;
		transform: rotate(180deg);
	}


	.selete_price {
		width: 100%;
		height: 480rpx;
		background-color: #FFFFFF;
		position: absolute;
		bottom: -480rpx;
		left: 0;
		display: flex;
		align-items: flex-start;
		justify-content: center;
		flex-wrap: wrap;
		overflow: auto;
	}

	.selete_price_box {
		width: 676rpx;
		height: 330rpx;
		/* background-color: salmon; */
		overflow: auto;
	}


	.selete_price_list {
		width: 676rpx;
		height: 80rpx;
		border-bottom: 1rpx solid #D0D0D0;
		display: flex;
	}

	.selete_price_list_left,
	.selete_price_list_leftA {
		width: 70%;
		height: 100%;
		/* background-color: royalblue; */
		color: rgba(133, 136, 150, 1);
		font-size: 28rpx;
		line-height: 80rpx;
	}

	.selete_price_list_leftA {
		color: #ED9939;
	}

	.selete_price_list_right {
		width: 30%;
		height: 100%;
		/* background-color: violet; */
		display: flex;
		align-items: center;
		justify-content: flex-end;
	}

	.selete_price_list_right_icon {
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.selete_price_btn_box {
		width: 676rpx;
		height: 150rpx;
		/* background-color: yellowgreen; */
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.selete_price_btn_box1 {
		width: 228rpx;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 16rpx;
		background-color: rgba(248, 248, 248, 1);
		color: rgba(16, 16, 16, 0.64);
		font-size: 24rpx;
		text-align: center;
		font-family: Arial;
	}

	.selete_price_btn_box2 {
		width: 432rpx;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 16rpx;
		background-color: rgba(237, 153, 57, 1);
		color: rgba(255, 255, 255, 1);
		font-size: 24rpx;
		text-align: center;
		font-family: Arial;
	}



	/* 列表 */
	.zaixian_list {
		width: 100%;
		/* min-height: calc(100vh - 80rpx); */
		background-color: #F7F7F7;
		display: flex;
		align-items: flex-start;
		justify-content: flex-start;
		/* 修改这里 */
		flex-wrap: wrap;
	}

	.zaixian_list_box {
		width: 340rpx;
		height: 490rpx;
		border-radius: 20rpx 20rpx 20rpx 20rpx;
		/* background: #FFFFFF; */
		/* background-color: pink; */
		margin-top: 20rpx;
		justify-items: flex-start;
		margin-left: 22rpx;
		overflow: hidden;
	}

	.zaixian_list_box_image {
		width: 100%;
		height: 340rpx;
		/* background-color: green; */
	}

	.zaixian_list_box_tbox {
		width: 100%;
		height: 150rpx;
		background-color: #FFFFFF;
		float: left;
	}

	.zaixian_list_box_tbox_title {
		width: 85%;
		height: 50rpx;
		line-height: 50rpx;
		font-size: 28rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #101010;
		margin: 15rpx auto 0;
		/* background-color: red; */

	}

	.zaixian_list_box_tbox_text {
		width: 85%;
		height: 60rpx;
		margin: 15rpx auto 0;
		/* background-color: aquamarine; */
		display: flex;
	}

	.zaixian_list_box_tbox_text_L,
	.zaixian_list_box_tbox_text_R {
		width: 50%;
		height: 100%;
		line-height: 60rpx;
		font-size: 38rpx;
		font-family: DIN;
		font-weight: bold;
		color: #E64638;
		display: flex;
	}

	.zaixian_list_box_tbox_text_R {
		font-size: 24rpx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #858896;
		text-align: right;
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
