<template>
	<view class="Map">
		<!-- 地图 -->
		<view class="Map_map">
			<map style="width:100%;height:100%;" :latitude="latitude" :longitude="longitude" :scale="scale"
				:markers="markers" @markertap="onMarkerTap">
			</map>
		</view>



		<!-- 信息 -->
		<view class="Map_bot">
			<view class="Map_bot_box">
				<view class="Index_Bto_list_for" @click.stop="Linkx">
					<view class="Index_Bto_list_for_top">
						<view class="Index_Bto_list_for_top_image">
							<image :src="YM + ListData[0].avatar" class="imageBFB"></image>
						</view>
						<view class="Index_Bto_list_for_top_right">
							<view class="Index_Bto_list_for_top_right1">
								<text>{{ListData[0].name}}</text>
								<text>
									<text style="font-size: 24rpx;color: #E64638;">￥</text>
									<text style="font-size: 40rpx;color: #E64638;">{{ListData[0].min_price}}</text>
									<text style="font-size: 24rpx;color: #E64638;font-weight: 500;">起</text>
								</text>
							</view>

							<view class="Index_Bto_list_for_top_right2">营业时间: {{ListData[0].business}}</view>
							<view class="Index_Bto_list_for_top_right3">
								<view class="Index_Bto_list_for_top_right3_L">{{ListData[0].address}}</view>
								<view class="Index_Bto_list_for_top_right3_R">{{ListData[0].distance}}</view>
							</view>
						</view>
					</view>
					<view class="Index_Bto_list_for_bto">
						<view class="Index_Bto_list_for_bto_left">
							<view class="Index_Bto_list_for_bto_left_icon">
								<image src="../../static/kz1.png" class="imageBFB"></image>
							</view>
							<text>可租车辆:</text>
							<text style="color: black;">{{ListData[0].car_num}}</text>
						</view>
						<view class="Index_Bto_list_for_bto_right">
							<view class="Index_Bto_list_for_bto_right_kuai" v-if="ListData[0].pick_type==1">到店取车</view>
							<view class="Index_Bto_list_for_bto_right_kuai" v-if="ListData[0].pick_type==2">到店取车</view>
							<view class="Index_Bto_list_for_bto_right_kuai" v-if="ListData[0].pick_type==2">送车上门</view>
						</view>
					</view>

					<view class="Index_Bto_list_btn">
						<view class="Index_Bto_list_btn_A" @click.stop="ClickGd">
							<text>更多门店</text>
							<view class="Index_Bto_list_btn_A_icon">
								<image src="../../static/bcY.png" class="imageBFB"></image>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM: getApp().globalData.apiBase,
				IndexData: [{
					title: '九九车行',
					price: '120',
					date: '08:00-12:00',
					diz: '郑州市金水区农业路国际企业中心',
					all: 80
				}],
				latitude: 34.7568711, //纬度
				longitude: 113.663221, //经度
				scale: 14, //地图缩放程度
				markers:[],
				ListData:[{
					index_image:'',
					name:'',
					min_price:'',
					business:'',
					address:'',
					car_num:'',
					pick_type:''
				}],
				ListDataA:''
			}
		},
		onShow() {
			this.MendiNAPI()
		},
		methods: {
			Linkx(){
				console.log('点击了');
				uni.setStorageSync('merchant_id',this.ListData[0].merchant_id)
				uni.navigateTo({
					url:'/pages/IndexChild/XQ'
				})
			},
			//更多门店
			ClickGd(){
				console.log('点击了更多门店');
				uni.navigateTo({
					url:'/pages/IndexChild/MenDianList'
				})
			},
			//门店列表
			MendiNAPI() {
				let data={
					lng:uni.getStorageSync('lng'),
					lat:uni.getStorageSync('lat'),
					page:1,
					list_rows:999999999
				}
				uni.request({
					url: this.YM + '/api/index/merchantList',
					method: 'POST',
					data,
					success: res => {
						console.log(res.data.data.data, '门店列表');
						
						this.ListData=res.data.data.data
						this.ListDataA=res.data.data.data
						
						this.markers=[]
						
						this.ListData.forEach(item => {
							this.markers.push({
								id: item.merchant_id, // unique identifier for the marker
								latitude: item.lat,
								longitude: item.lng,
								title: item.name,
								width: 28, // 圆形标点的宽度
								height: 28, // 圆形标点的高度
								borderRadius: 14, // 圆形标点的边框半径，设置为宽度或高度的一半
								borderWidth: 2, // 圆形标点的边框宽度
								borderColor: '#000000', // 圆形标点的边框颜色
								color: '#21FAED', // 圆形标点的填充颜色
								callout: {
									content: item.name, // 弹出气泡中要显示的名称
									color: '#000000',
									fontSize: 14,
									borderRadius: 24,
									bgColor: '#ffffff',
									padding: 8,
									display: 'BYCLICK'
								}
						
							});
						});
						this.latitude = this.ListData[0].lat
						this.longitude = this.ListData[0].lng
					}
				})
			},
			onMarkerTap(e) {
			  const markerId = e.markerId;
			  const clickedMarker = this.markers.find(marker => marker.id === markerId);
			  console.log(clickedMarker.id, '点击的对应id');
			
			  // 使用clickedMarker对象访问点击标记的信息
			  const matchedData = this.ListData.find(item => item.merchant_id === clickedMarker.id);
			  console.log(matchedData, '匹配到的数据');
			
			  // 将匹配到的数据移动到this.ListData的第一个位置
			  const index = this.ListData.indexOf(matchedData);
			  if (index > 0) {
			    this.ListData.splice(index, 1);
			    this.ListData.unshift(matchedData);
			  }
			
			  clickedMarker.callout.display = 'ALWAYS';
			
			  const updatedMarkers = this.markers.map(marker => {
			    if (marker.id === markerId) {
			      return clickedMarker;
			    } else {
			      marker.callout.display = 'BYCLICK';
			      return marker;
			    }
			  });
			
			  this.$set(this, 'markers', updatedMarkers);
			}

		}
	}
</script>

<style>
	.Map {
		width: 100vw;
		height: 100vh;
		background-color: rgba(255, 255, 255, 1);
	}

	/* 地图 */
	.Map_map {
		width: 100%;
		height: 1030rpx;
		background-color: rgba(255, 255, 255, 1);
	}

	/* 信息 */
	.Map_bot {
		width: 100%;
		/* height: 464rpx; */
		background-color: rgba(255, 255, 255, 1);
		position: relative;
	}

	.Map_bot_box {
		width: 100%;
		height: 404rpx;
		border-radius: 28rpx;
		background-color: rgba(255, 255, 255, 1);
		position: absolute;
		top: -40rpx;
		left: 0;
		padding-top: 20rpx;
		padding-bottom: 20rpx;
	}

	.Index_Bto_list_for {
		width: 100%;
		height: 288rpx;
		border-radius: 20rpx;
		background-color: rgba(255, 255, 255, 1);
		text-align: center;
		box-shadow: 0px 4rpx 12rpx 0px rgba(0, 0, 0, 0.06);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-wrap: wrap;
		margin-bottom: 20rpx;
	}

	.Index_Bto_list_for_top {
		width: 648rpx;
		height: 170rpx;
		display: flex;
		align-items: flex-start;
		border-bottom: 1rpx solid #BBBBBB;
		/* background-color: hotpink; */
	}

	.Index_Bto_list_for_top_image {
		width: 140rpx;
		height: 140rpx;
		border-radius: 8rpx;
		/* background-color: green; */
		overflow: hidden;
		margin-top: 8rpx;
	}

	.Index_Bto_list_for_top_right {
		width: 490rpx;
		height: 140rpx;
		/* background-color: yellow; */
		margin-left: 15rpx;
		margin-top: 8rpx;
	}

	.Index_Bto_list_for_top_right1 {
		width: 100%;
		height: 55rpx;
		/* background-color: darkseagreen; */
		display: flex;
		align-items: center;
		justify-content: space-between;
		color: rgba(0, 0, 0, 1.0);
		font-size: 30rpx;
		font-weight: bold;
	}

	.Index_Bto_list_for_top_right2 {
		width: 100%;
		height: 40rpx;
		color: rgba(28, 37, 61, 1);
		font-size: 26rpx;
		line-height: 40rpx;
		text-align: left;
		font-weight: 600;
	}

	.Index_Bto_list_for_top_right3 {
		width: 100%;
		height: 30rpx;
		line-height: 30rpx;
		color: rgba(133, 136, 150, 1);
		font-size: 24rpx;
		margin-top: 15rpx;
		text-align: left;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	
	.Index_Bto_list_for_top_right3_L{
		width: 70%;
		height: 100%;
		white-space: nowrap;
		/* 禁止换行 */
		overflow: hidden;
		/* 溢出部分隐藏 */
		text-overflow: ellipsis;
		/* 超出部分显示省略号 */
	}
	.Index_Bto_list_for_top_right3_R{
		min-width: 78rpx;
		height: 40rpx;
		line-height: 40rpx;
		border-radius: 6rpx;
		background-color: rgba(255, 245, 230, 1);
		text-align: center;
		color: #ED9939 ;
	}

	.Index_Bto_list_for_bto {
		width: 648rpx;
		height: 70rpx;
		/* background-color: violet; */
		display: flex;
		align-items: center;
	}

	.Index_Bto_list_for_bto_left,
	.Index_Bto_list_for_bto_right {
		width: 50%;
		height: 100%;
		/* background-color: darksalmon; */
		display: flex;
		align-items: center;
		justify-content: flex-start;
		color: rgba(133, 136, 150, 1);
		font-size: 28rpx;
		font-weight: 600;
	}

	.Index_Bto_list_for_bto_left_icon {
		width: 32rpx;
		height: 42rpx;
		/* background-color: green; */
		margin-right: 15rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}


	.Index_Bto_list_for_bto_right {
		/* background-color: antiquewhite; */
		justify-content: flex-end;
	}

	.Index_Bto_list_for_bto_right_kuai {
		width: 110rpx;
		height: 36rpx;
		line-height: 36rpx;
		border-radius: 6rpx;
		background-color: rgba(255, 255, 255, 1);
		color: rgba(161, 161, 161, 1);
		font-size: 22rpx;
		text-align: center;
		font-family: Arial;
		border: 2rpx solid rgba(213, 213, 213, 1);
		margin-left: 10rpx;
	}

	.Index_Bto_list_btn {
		width: 100%;
		height: 130rpx;
		/* background-color: darksalmon; */
		display: flex;
		align-items: center;
		justify-content: center;
		margin-top: 60rpx;
	}

	.Index_Bto_list_btn_A {
		width: 556rpx;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 44rpx;
		background-color: rgba(237, 153, 57, 1);
		color: rgba(255, 255, 255, 1.0);
		font-size: 28rpx;
		text-align: center;
		font-family: Arial;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.Index_Bto_list_btn_A_icon {
		width: 24rpx;
		height: 24rpx;
		/* background-color: green; */
		margin-left: 15rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}
</style>
