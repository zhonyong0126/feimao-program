<template>
	<view class="YS">
		<view class="YS_box">
			<u-parse :content="content"></u-parse>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				YM:getApp().globalData.apiBase,
				content:''
			}
		},
		onShow() {
			uni.request({
				url:this.YM + '/api/index/config',
				method:'POST',
				success:res=>{
					
					const targetData1 = res.data.data.find(item => item.name === "insure_agreement");
					if (targetData1) {
						this.content = targetData1.value
						console.log(targetData1.value);
					} else {
						this.content = '后台暂未添加，请联系工作人员'
						console.log('未找到匹配的数据');
					}
				}
			})
		}
	}
</script>

<style>
.YS{
	width: 100vw;
	height: 100vh;
	background-color: #FFFFFF;
	float: left;
}
.YS_box{
	width: 90%;
	height: auto;
	margin: 20rpx auto 0;
	font-size: 26rpx;
	font-family: PingFang SC;
	font-weight: 500;
	color: #000000;
}
</style>
