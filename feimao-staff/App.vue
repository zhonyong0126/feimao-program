<script>
export default {
  onLaunch: function() {
    console.log('App Launch')
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.redirectTo({ url: '/pages/my/index' })
    }
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style>
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

:root {
  --primary-color: #1890ff;
  --success-color: #52c41a;
  --warning-color: #faad14;
  --error-color: #ff4d4f;
}

.card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.btn-primary {
  background: var(--primary-color);
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx 40rpx;
}

.status-tag {
  display: inline-block;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  font-size: 24rpx;
}

.status-0 { background: #fff7e6; color: #fa8c16; }
.status-1 { background: #e6f7ff; color: #1890ff; }
.status-2 { background: #f6ffed; color: #52c41a; }
.status-3 { background: #f9f0ff; color: #722ed1; }
.status-4 { background: #f6ffed; color: #52c41a; }
.status-5 { background: #fff2f0; color: #ff4d4f; }
</style>
