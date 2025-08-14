<template>
  <el-dialog
    title="应用下载二维码"
    :visible.sync="visible"
    width="400px"
    center
    @close="handleClose"
  >
    <div class="qrcode-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <i class="el-icon-loading"></i>
        <p>正在生成二维码...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <i class="el-icon-warning"></i>
        <p>{{ error }}</p>
        <el-button type="primary" size="small" @click="retryGenerate">
          重试
        </el-button>
      </div>
      
              <!-- 成功状态：显示二维码和下载信息 -->
        <div v-else-if="qrCodeData" class="success-container">
          <!-- 二维码图片 -->
          <div class="qrcode-image">
            <img 
              :src="qrCodeData.imageUrl" 
              alt="下载二维码" 
              v-if="qrCodeData.imageUrl"
              class="qrcode-img"
              @load="handleImageLoad"
              @error="handleImageError"
            />
            <div v-else class="qrcode-placeholder">
              <i class="el-icon-picture-outline"></i>
              <p>二维码图片生成中...</p>
              <small v-if="qrCodeData.imageUrl">图片URL: {{ qrCodeData.imageUrl }}</small>
            <small v-if="qrCodeData.message">状态: {{ qrCodeData.message }}</small>
            </div>
          </div>
        
        <!-- 下载链接信息 -->
        <div class="download-info">
          <h4>应用信息</h4>
          <div class="info-item">
            <span class="label">应用名称：</span>
            <span class="value">{{ appInfo.appName }}</span>
          </div>
          <div class="info-item">
            <span class="label">版本号：</span>
            <span class="value">{{ appInfo.version }}</span>
          </div>
          <div class="info-item">
            <span class="label">下载链接：</span>
            <span class="value download-link">{{ qrCodeData.downloadUrl }}</span>
          </div>
          
          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="small" 
              @click="copyDownloadUrl"
              :disabled="!qrCodeData.downloadUrl"
            >
              <i class="el-icon-document-copy"></i> 复制链接
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="downloadApp"
              :disabled="!qrCodeData.downloadUrl"
            >
              <i class="el-icon-download"></i> 直接下载
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import appApi from '@/apis/module/app'

export default {
  name: 'QRCodeDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    appInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      loading: false,
      error: null,
      qrCodeData: null
    }
  },
  watch: {
    visible(newVal) {
      if (newVal && this.appInfo.id) {
        this.generateQRCode()
      }
    }
  },
  methods: {
    // 生成二维码
    generateQRCode() {
      console.log('开始生成二维码，应用ID:', this.appInfo.id)
      this.loading = true
      this.error = null
      this.qrCodeData = null
      
      // 调用后端API生成二维码
      appApi.generateQRCode(this.appInfo.id, 
        (response) => {
          // 成功回调
          console.log('二维码生成成功，响应:', response)
          
          // 处理响应数据
          let responseData = response.data
          console.log('原始响应数据:', responseData)
          
          // 如果响应是字符串，尝试解析为JSON
          if (typeof responseData === 'string') {
            try {
              responseData = JSON.parse(responseData)
              console.log('解析后的响应数据:', responseData)
            } catch (e) {
              console.error('解析响应数据失败:', e)
              responseData = {}
            }
          }
          
          // 检查新的JSON格式
          if (responseData && responseData.code === 0 && responseData.data) {
            // 新格式：{code: 0, data: {downloadUrl, imageUrl}, message}
            this.qrCodeData = {
              downloadUrl: responseData.data.downloadUrl || '',
              imageUrl: responseData.data.imageUrl || null,
              message: responseData.message || ''
            }
          } else if (responseData && responseData.downloadUrl) {
            // 旧格式兼容
            this.qrCodeData = {
              downloadUrl: responseData.downloadUrl || '',
              imageUrl: responseData.imageUrl || null,
              message: responseData.message || ''
            }
          } else {
            console.error('响应数据格式不正确:', responseData)
            this.error = '响应数据格式不正确'
          }
          
          console.log('最终设置的二维码数据:', this.qrCodeData)
          
          this.loading = false
        },
        (error) => {
          // 失败回调
          console.error('生成二维码失败:', error)
          this.error = '生成二维码失败，请重试'
          this.loading = false
        }
      )
    },
    
    // 复制下载链接
    copyDownloadUrl() {
      if (this.qrCodeData && this.qrCodeData.downloadUrl) {
        navigator.clipboard.writeText(this.qrCodeData.downloadUrl)
          .then(() => {
            this.$message.success('下载链接已复制到剪贴板')
          })
          .catch(() => {
            // 降级方案
            const textArea = document.createElement('textarea')
            textArea.value = this.qrCodeData.downloadUrl
            document.body.appendChild(textArea)
            textArea.select()
            document.execCommand('copy')
            document.body.removeChild(textArea)
            this.$message.success('下载链接已复制到剪贴板')
          })
      }
    },
    
    // 直接下载应用
    downloadApp() {
      if (this.qrCodeData && this.qrCodeData.downloadUrl) {
        // 触发父组件的下载方法
        this.$emit('download', this.appInfo)
      }
    },
    
    // 重试生成
    retryGenerate() {
      this.generateQRCode()
    },
    
    // 处理图片加载成功
    handleImageLoad() {
      console.log('二维码图片加载成功:', this.qrCodeData.imageUrl)
    },
    
    // 处理图片加载错误
    handleImageError() {
      console.error('二维码图片加载失败:', this.qrCodeData.imageUrl)
      this.$message.warning('二维码图片加载失败，请检查网络连接')
    },
    
    // 关闭弹窗
    handleClose() {
      this.$emit('update:visible', false)
      this.resetState()
    },
    
    // 重置状态
    resetState() {
      this.loading = false
      this.error = null
      this.qrCodeData = null
    }
  }
}
</script>

<style scoped>
.qrcode-container {
  text-align: center;
  padding: 20px 0;
}

.success-container {
  text-align: center;
}

.qrcode-image {
  margin-bottom: 20px;
}

.qrcode-img {
  width: 200px;
  height: 200px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.qrcode-placeholder {
  width: 200px;
  height: 200px;
  margin: 0 auto;
  border: 2px dashed #e4e7ed;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.qrcode-placeholder i {
  font-size: 48px;
  margin-bottom: 10px;
}

.download-info {
  text-align: left;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 4px;
}

.download-info h4 {
  margin: 0 0 15px 0;
  color: #303133;
  text-align: center;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.info-item .label {
  font-weight: bold;
  color: #606266;
  min-width: 80px;
}

.info-item .value {
  color: #303133;
  word-break: break-all;
}

.download-link {
  color: #409eff;
  cursor: pointer;
}

.download-link:hover {
  text-decoration: underline;
}

.action-buttons {
  margin-top: 20px;
  text-align: center;
}

.action-buttons .el-button {
  margin: 0 5px;
}

.loading-container {
  padding: 40px 0;
  color: #909399;
}

.loading-container i {
  font-size: 32px;
  margin-bottom: 10px;
}

.error-container {
  padding: 40px 0;
  color: #f56c6c;
}

.error-container i {
  font-size: 32px;
  margin-bottom: 10px;
}

.dialog-footer {
  text-align: center;
}
</style> 