<template>
	<view class="Datebigbox">
		<view class="Datebigbox_title">选择日期</view>
		<view class="Datebigbox_box">
			<view class="Datebigbox_box_list">
				<view class="Datebigbox_box_list_name">年</view>
				<view class="Datebigbox_box_list_list">
					<picker-view v-if="visible" :indicator-style="indicatorStyle" :value="value" @change="bindChange"
						class="picker-view">
						<picker-view-column class="list1">
							<view class="item" v-for="(item,index) in years" :key="index">{{item}}</view>
						</picker-view-column>
					</picker-view>
				</view>
			</view>
			<view class="Datebigbox_box_list">
				<view class="Datebigbox_box_list_name">月</view>
				<view class="Datebigbox_box_list_list">
					<picker-view v-if="visible" :indicator-style="indicatorStyle" :value="valueDate[0]"
						@change="bindChange($event,1)" class="picker-view">
						<picker-view-column>
							<view class="item" v-for="(item,index) in months" :key="index">{{item}}</view>
						</picker-view-column>
					</picker-view>
				</view>
			</view>
			<view class="Datebigbox_box_list">
				<view class="Datebigbox_box_list_name">日</view>
				<view class="Datebigbox_box_list_list">
					<picker-view v-if="visible" :indicator-style="indicatorStyle" :value="valueDate[1]"
						@change="bindChange($event,2)" class="picker-view">
						<picker-view-column>
							<view class="item" v-for="(item,index) in days" :key="index">{{item}}</view>
						</picker-view-column>
					</picker-view>
				</view>
			</view>
			<view class="Datebigbox_box_list">
				<view class="Datebigbox_box_list_name">时</view>
				<view class="Datebigbox_box_list_list">
					<picker-view v-if="visible" :indicator-style="indicatorStyle" :value="valueDate[2]"
						@change="bindChange($event,3)" class="picker-view">
						<picker-view-column>
							<view class="item" v-for="(item,index) in hours" :key="index">{{item}}</view>
						</picker-view-column>
					</picker-view>
				</view>
			</view>
			<view class="Datebigbox_box_list">
				<view class="Datebigbox_box_list_name">分</view>
				<view class="Datebigbox_box_list_list" style="border: 0;">
					<picker-view v-if="visible" :indicator-style="indicatorStyle" :value="valueDate[3]"
						@change="bindChange($event,4)" class="picker-view">
						<picker-view-column>
							<view class="item" v-for="(item,index) in minutes" :key="index">{{item}}</view>
						</picker-view-column>
					</picker-view>
				</view>
			</view>
		</view>
		<!-- <view class="Datebigbox_text">选择的时间:{{seletedate}}</view> -->
		<view class="Datebigbox_btn">
			<view @click="DateNo">取消</view>
			<view @click="DateYes">确定</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: "Date",
		data() {
			const date = new Date();
			const years = [];
			const year = date.getFullYear();
			const months = [];
			const month = date.getMonth() + 1;
			const days = [];
			const day = date.getDate();
			const hours = [];
			const hour = date.getHours();
			const minutes = [0,10,20,30,40,50];
			const minute = date.getMinutes();
			const seconds = [];
			const second = date.getSeconds();
			years.push(year);
			// for (let i = 1990; i <= date.getFullYear(); i++) {
			//     years.push(year);
			// }
			for (let i = 1; i <= 12; i++) {
				months.push(i);
			}
			for (let i = 1; i <= 31; i++) {
				days.push(i);
			}
			for (let i = 0; i < 24; i++) {
				hours.push(i);
			}
			// for (let i = 0; i < 60; i++) {
			// 	minutes.push(i);
			// 	seconds.push(i);
			// }
			console.log(month, '月');
			console.log(day, '日');
			console.log(hour, '时');
			console.log(minute, '分');
			return {
				title: 'picker-view',
				years,
				year,
				months,
				month,
				days,
				day,
				hours,
				hour,
				minutes,
				minute,
				seconds,
				second,
				visible: true,
				indicatorStyle: `height: 50px;`,
				value: [year],
				valueDate: [
					[month - 1],
					[day - 1],
					[hour]
				],
				seletedate: `${year}年${month}月${day} ${hour}:${0}`,
				HuaDong:1
			};
		},
		methods: {
			// 确定
			DateYes() {  
			    // 获取当前时间  
			    const currentDate = new Date();  
			    
			    // 将当前时间的小时减去一个小时  
			    currentDate.setHours(currentDate.getHours() - 1);  
			    
			    // 创建比较时只考虑年、月、日和小时  
			    const selectedDate = new Date(`${this.year}-${this.month}-${this.day}T${this.hour}:00`);  
			
			    // 判断选择的时间是否小于当前时间（只考虑到小时）  
			    if (selectedDate < currentDate) {  
			        console.log('No');  
					uni.showToast({
						title:'选择时间不能小于当前时间',
						icon:'none',
						duration: 2000
					});
					return
			    } else {  
			        if (this.HuaDong == 1) {  
			            this.seletedate = `${this.year}-${this.month}-${this.day}- ${this.hour}:00`;  
			            console.log('走了1');  
			        } else {  
			            this.seletedate = `${this.year}-${this.month}-${this.day}- ${this.hour}:${this.minute}`;  
			        }  
			        console.log('选择的时间', this.seletedate);  
			        this.$emit('date-selected', this.seletedate);  
			    }  
			
			    this.$emit('dateshow', false);  
			},  
			// DateYes() {
			// 	if(this.HuaDong==1){
			// 		this.seletedate = `${this.year}-${this.month}-${this.day}- ${this.hour}:${0}`
			// 		// this.seletedate = `${this.year}-${this.month}-${this.day} ${this.hour.toString().padStart(2, '0')}:${'0'.padStart(2, '0')}`;
			// 		console.log('走了1');
			// 	}else{
			// 		this.seletedate = `${this.year}-${this.month}-${this.day}- ${this.hour}:${this.minute}`
			// 		// this.seletedate = `${this.year}-${this.month}-${this.day} ${this.hour.toString().padStart(2, '0')}:${this.minute.toString().padStart(2, '0')}`;
			// 	}
			// 	console.log('选择的时间', this.seletedate);
			// 	this.$emit('date-selected', this.seletedate);
			// 	this.$emit('dateshow', false);
				
				
			// },
			
			// 取消
			DateNo() {
				this.$emit('dateshow', false);
			},
			bindChange: function(e, type) {
				console.log(type);
				console.log(e.detail.value[0]);
				if (type == 1) {
					this.month = e.detail.value[0] + 1;
					this.aggregate()
					return
				} else if (type == 2) {
					this.day = e.detail.value[0] + 1;
					this.aggregate()
					return
				} else if (type == 3) {
					this.hour = e.detail.value[0];
					this.aggregate()
					return
				} else if (type == 4) {
					this.minute = e.detail.value[0];
					this.aggregate()
					return
				}
			},
			aggregate() {
				this.HuaDong=2
				this.seletedate = `${this.year}-${this.month}-${this.day} ${this.hour}:${this.minute}0`
				
				console.log(this.seletedate,'666');
				// this.seletedate = `${this.year}-${this.month}-${this.day} ${this.hour.toString().padStart(2, '0')}:${this.minute.toString().padStart(2, '0')}`;
			}
		}
	}
</script>

<style lang="scss">
	$zindex: 9;
	$left: 0;
	$bottom: 0;

	.Datebigbox {
		width: 100%;
		height: 850rpx;
		position: fixed;
		left: $left;
		bottom: $bottom;
		z-index: $zindex;
		border-radius: 40rpx 40rpx 0 0;
		background-color: #FFFFFF;
		box-shadow: 0px 4rpx 12rpx 0px rgba(0, 0, 0, 0.06);

		.Datebigbox_title {
			width: 100%;
			text-align: center;
			margin-top: 50rpx;
			font-size: 30rpx;
			font-weight: 700;
			color: #000000;
		}

		.Datebigbox_box {
			width: 95%;
			height: 500rpx;
			margin: 40rpx auto 0;
			display: flex;
			align-items: flex-start;
			justify-content: space-between;

			.Datebigbox_box_list {
				flex: 5;
				height: 100%;

				.Datebigbox_box_list_name {
					width: 100%;
					height: 15%;
					text-align: center;
					line-height: 80rpx;
					font-size: 28rpx;
					color: #000000;
					font-weight: 600;
				}

				.Datebigbox_box_list_list {
					width: 98%;
					height: 85%;
					border-right: 2rpx solid #e1e1e1;
				}
			}
		}

		.Datebigbox_text {
			width: 100%;
			height: 50rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 30rpx;
			color: #bababa;
			margin-top: 20rpx;
		}

		.Datebigbox_btn {
			width: 100%;
			height: 88rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-top: 20rpx;
		}

		.Datebigbox_btn>view:nth-child(1),
		.Datebigbox_btn>view:nth-child(2) {
			width: 282rpx;
			height: 80rpx;
			border-radius: 40rpx;
			text-align: center;
			line-height: 80rpx;
			background-color: transparent;
			border: 2rpx solid #ccc;
			margin-right: 20rpx;
		}

		.Datebigbox_btn>view:nth-child(2) {
			background-color: #0f3772;
			border: 0;
			margin-left: 20rpx;
			color: #FFFFFF;
		}
	}

	.picker-view {
		width: 100%;
		height: 100%;
		background-color: transparent !important;
	}

	.item {
		line-height: 100rpx;
		text-align: center;
		background-color: #eaf1fd;
		color: #0f3772;
		font-weight: 600;
	}
</style>