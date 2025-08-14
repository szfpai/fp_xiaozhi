<template>
  <div class="welcome">
    <HeaderBar />

    <div class="operation-bar">
      <h2 class="page-title">应用管理</h2>
      <div class="right-operations">
        <el-input placeholder="请输入应用名称查询" v-model="searchForm.appName" class="search-input"
          @keyup.enter.native="handleSearch" clearable />
        <el-button class="btn-search" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <div class="main-wrapper">
      <div class="content-panel">
        <div class="content-area">
          <el-card class="params-card" shadow="never">
            <el-table ref="appTable" :data="appList" class="transparent-table" v-loading="loading"
              element-loading-text="拼命加载中" element-loading-spinner="el-icon-loading"
              element-loading-background="rgba(255, 255, 255, 0.7)"
              :header-cell-class-name="headerCellClassName">
              <el-table-column label="选择" align="center" width="120">
                <template slot-scope="scope">
                  <el-checkbox v-model="scope.row.selected"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column label="应用名称" prop="appName" align="center"></el-table-column>
              <el-table-column label="应用类型" prop="appType" align="center">
                <template slot-scope="scope">
                  <el-tag :type="getAppTypeTagType(scope.row.appType)">
                    {{ getAppTypeName(scope.row.appType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="版本号" prop="version" align="center"></el-table-column>
              <el-table-column label="版本码" prop="versionCode" align="center"></el-table-column>
              <el-table-column label="文件大小" prop="fileSize" align="center">
                <template slot-scope="scope">
                  {{ formatFileSize(scope.row.fileSize) }}
                </template>
              </el-table-column>
              <el-table-column label="下载次数" prop="downloadCount" align="center"></el-table-column>
              <el-table-column label="状态" prop="isActive" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.isActive ? 'success' : 'info'">
                    {{ scope.row.isActive ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="强制更新" prop="isForceUpdate" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.isForceUpdate ? 'warning' : 'info'">
                    {{ scope.row.isForceUpdate ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" prop="createDate" align="center">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.createDate) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center">
                <template slot-scope="scope">
                  <el-button size="mini" type="text" @click="handleEdit(scope.row)">编辑</el-button>
                  <el-button size="mini" type="text" @click="handleDownload(scope.row)">下载</el-button>
                  <el-button size="mini" type="text" @click="handleQRCode(scope.row)">二维码</el-button>
                  <el-button size="mini" type="text" @click="handleDelete(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="table_bottom">
              <div class="ctrl_btn">
                <el-button size="mini" type="primary" class="select-all-btn" @click="handleSelectAll">
                  {{ isAllSelected ? '取消全选' : '全选' }}
                </el-button>
                <el-button size="mini" type="success" @click="showAddDialog"
                  style="background: #5bc98c;border: None;">新增</el-button>
                <el-button size="mini" type="danger" icon="el-icon-delete"
                  @click="deleteSelectedApps">删除</el-button>
              </div>
              <div class="custom-pagination">
                <el-select v-model="pagination.limit" @change="handlePageSizeChange" class="page-size-select">
                  <el-option v-for="item in pageSizeOptions" :key="item" :label="`${item}条/页`"
                    :value="item">
                  </el-option>
                </el-select>
                <button class="pagination-btn" :disabled="pagination.page === 1" @click="goFirst">首页</button>
                <button class="pagination-btn" :disabled="pagination.page === 1" @click="goPrev">上一页</button>
                <button v-for="page in visiblePages" :key="page" class="pagination-btn"
                  :class="{ active: page === pagination.page }" @click="goToPage(page)">
                  {{ page }}
                </button>
                <button class="pagination-btn" :disabled="pagination.page === pageCount" @click="goNext">下一页</button>
                <span class="total-text">共{{ pagination.total }}条记录</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 二维码弹窗 -->
    <QRCodeDialog 
      :visible.sync="qrCodeDialogVisible"
      :app-info="selectedAppInfo"
      @download="handleDownload"
    />
    
    <!-- 添加/编辑应用对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="appForm" :model="appForm" :rules="appRules" label-width="100px">
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="appForm.appName" placeholder="请输入应用名称" />
        </el-form-item>
        <el-form-item label="应用类型" prop="appType">
          <el-select v-model="appForm.appType" placeholder="请选择应用类型" style="width: 100%">
            <el-option label="Android" value="android" />
            <el-option label="iOS" value="ios" />
            <el-option label="Windows" value="windows" />
            <el-option label="macOS" value="macos" />
            <el-option label="Linux" value="linux" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="appForm.version" placeholder="请输入版本号" />
        </el-form-item>
        <el-form-item label="版本码" prop="versionCode">
          <el-input-number v-model="appForm.versionCode" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="应用文件" prop="file" v-if="!isEdit">
          <el-upload
            ref="upload"
            :auto-upload="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            :file-list="fileList"
            accept=".apk,.ipa,.exe,.dmg,.deb,.rpm"
            drag
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div class="el-upload__tip" slot="tip">
              支持APK、IPA、EXE、DMG、DEB、RPM等格式，文件大小不超过200MB
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="应用描述" prop="description">
          <el-input
            v-model="appForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入应用描述"
          />
        </el-form-item>
        <el-form-item label="更新日志" prop="changelog">
          <el-input
            v-model="appForm.changelog"
            type="textarea"
            :rows="3"
            placeholder="请输入更新日志"
          />
        </el-form-item>
        <el-form-item label="强制更新">
          <el-switch v-model="appForm.isForceUpdate" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="appForm.isActive" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="appForm.sort" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </div>
    </el-dialog>

    <el-footer>
      <version-footer />
    </el-footer>
  </div>
</template>

<script>
import appApi from '@/apis/module/app'
import QRCodeDialog from '@/components/QRCodeDialog.vue'
import HeaderBar from '@/components/HeaderBar.vue'
import VersionFooter from '@/components/VersionFooter.vue'
import { formatDate, formatFileSize } from '@/utils/format'

export default {
  name: 'AppManagement',
  components: {
    QRCodeDialog,
    HeaderBar,
    VersionFooter
  },
  data() {
    return {
      // 搜索表单
      searchForm: {
        appName: '',
        appType: '',
        isActive: ''
      },
      // 应用列表
      appList: [],
      // 分页信息
      pagination: {
        page: 1,
        limit: 10,
        total: 0
      },
      pageSizeOptions: [10, 20, 50, 100],
      // 加载状态
      loading: false,
      // 对话框相关
      dialogVisible: false,
      isEdit: false,
      dialogTitle: '添加应用',
      // 应用表单
      appForm: {
        appName: '',
        appType: '',
        version: '',
        versionCode: 1,
        description: '',
        changelog: '',
        isForceUpdate: false,
        isActive: true,
        sort: 0
      },
      // 表单验证规则
      appRules: {
        appName: [
          { required: true, message: '请输入应用名称', trigger: 'blur' }
        ],
        appType: [
          { required: true, message: '请选择应用类型', trigger: 'change' }
        ],
        version: [
          { required: true, message: '请输入版本号', trigger: 'blur' }
        ],
        versionCode: [
          { required: true, message: '请输入版本码', trigger: 'blur' }
        ]
      },
      // 文件上传相关
      fileList: [],
      selectedFile: null,
      // 提交状态
      submitLoading: false,
      // 选中的行
      selectedRows: [],
      // 二维码弹窗相关
      qrCodeDialogVisible: false,
      selectedAppInfo: {}
    }
  },
  created() {
    this.fetchAppList()
  },
  computed: {
    pageCount() {
      return Math.ceil(this.pagination.total / this.pagination.limit)
    },
    visiblePages() {
      const pages = []
      const maxVisible = 3
      let start = Math.max(1, this.pagination.page - 1)
      let end = Math.min(this.pageCount, start + maxVisible - 1)

      if (end - start + 1 < maxVisible) {
        start = Math.max(1, end - maxVisible + 1)
      }

      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      return pages
    },
    isAllSelected() {
      return this.appList.length > 0 && this.appList.every(item => item.selected)
    }
  },
  methods: {
    // 获取应用列表
    fetchAppList() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        limit: this.pagination.limit,
        ...this.searchForm
      }
      
      appApi.getAppList(params, 
        (response) => {
          // 成功回调
          if (response.data && response.data.code === 0) {
            this.appList = (response.data.data.list || []).map(item => ({
              ...item,
              selected: false
            }))
            this.pagination.total = response.data.data.total || 0
          } else {
            this.$message.error(response.data?.msg || '获取应用列表失败')
          }
          this.loading = false
        },
        (error) => {
          // 失败回调
          console.error('获取应用列表失败:', error)
          this.$message.error('获取应用列表失败')
          this.loading = false
        }
      )
    },

    // 搜索
    handleSearch() {
      this.pagination.page = 1
      this.fetchAppList()
    },

    // 重置搜索
    handleReset() {
      this.searchForm = {
        appName: '',
        appType: '',
        isActive: ''
      }
      this.pagination.page = 1
      this.fetchAppList()
    },

    // 显示添加对话框
    showAddDialog() {
      this.isEdit = false
      this.dialogTitle = '添加应用'
      this.dialogVisible = true
      this.resetForm()
    },

    // 编辑应用
    handleEdit(row) {
      this.isEdit = true
      this.dialogTitle = '编辑应用'
      this.dialogVisible = true
      this.appForm = { ...row }
    },

    // 删除应用
    handleDelete(row) {
      this.$confirm('确定要删除该应用吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        appApi.deleteApp(row.id, 
          (response) => {
            // 成功回调
            if (response.data && response.data.code === 0) {
              this.$message.success('删除成功')
              this.fetchAppList()
            } else {
              this.$message.error(response.data?.msg || '删除失败')
            }
          },
          (error) => {
            // 失败回调
            console.error('删除应用失败:', error)
            this.$message.error('删除应用失败')
          }
        )
      })
    },

    // 下载应用
    handleDownload(row) {
      appApi.downloadApp(row.id, 
        (response) => {
          // 成功回调
          try {
            const blob = new Blob([response.data])
            const url = window.URL.createObjectURL(blob)
            const link = document.createElement('a')
            link.href = url
            link.download = row.fileName || `${row.appName}.apk`
            document.body.appendChild(link)
            link.click()
            document.body.removeChild(link)
            window.URL.revokeObjectURL(url)
            
            // 增加下载次数
            appApi.incrementDownloadCount(row.id, 
              () => console.log('下载次数增加成功'),
              (error) => console.error('增加下载次数失败:', error)
            )
          } catch (error) {
            console.error('下载处理失败:', error)
            this.$message.error('下载处理失败')
          }
        },
        (error) => {
          // 失败回调
          console.error('下载应用失败:', error)
          this.$message.error('下载应用失败')
        }
      )
    },

    // 生成二维码
    handleQRCode(row) {
      this.selectedAppInfo = row
      this.qrCodeDialogVisible = true
    },

    // 全选/取消全选
    handleSelectAll() {
      const newState = !this.isAllSelected
      this.appList.forEach(item => {
        item.selected = newState
      })
    },

    // 删除选中的应用
    deleteSelectedApps() {
      const selectedIds = this.appList.filter(item => item.selected).map(item => item.id)
      if (selectedIds.length === 0) {
        this.$message.warning('请选择要删除的应用')
        return
      }

      this.$confirm(`确定要删除选中的 ${selectedIds.length} 个应用吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        appApi.deleteBatchApp(selectedIds, 
          (response) => {
            if (response.data && response.data.code === 0) {
              this.$message.success('删除成功')
              this.fetchAppList()
            } else {
              this.$message.error(response.data?.msg || '删除失败')
            }
          },
          (error) => {
            console.error('删除应用失败:', error)
            this.$message.error('删除应用失败')
          }
        )
      })
    },

    // 分页相关方法
    handlePageSizeChange(val) {
      this.pagination.limit = val
      this.pagination.page = 1
      this.fetchAppList()
    },

    goFirst() {
      this.pagination.page = 1
      this.fetchAppList()
    },

    goPrev() {
      if (this.pagination.page > 1) {
        this.pagination.page--
        this.fetchAppList()
      }
    },

    goNext() {
      if (this.pagination.page < this.pageCount) {
        this.pagination.page++
        this.fetchAppList()
      }
    },

    goToPage(page) {
      this.pagination.page = page
      this.fetchAppList()
    },

    // 文件上传相关
    handleFileChange(file) {
      this.selectedFile = file.raw
    },

    beforeUpload(file) {
      // 验证文件大小（限制为200MB）
      if (file.size > 200 * 1024 * 1024) {
        this.$message.error('文件大小不能超过200MB')
        return false
      }
      
      // 验证文件类型
      const allowedTypes = ['.apk', '.ipa', '.exe', '.dmg', '.deb', '.rpm']
      const fileName = file.name.toLowerCase()
      const isValidType = allowedTypes.some(type => fileName.endsWith(type))
      if (!isValidType) {
        this.$message.error('不支持的文件类型，请选择APK、IPA、EXE、DMG、DEB、RPM格式的文件')
        return false
      }
      
      return false // 阻止自动上传
    },

    // 提交表单
    handleSubmit() {
      this.$refs.appForm.validate((valid) => {
        if (!valid) return
        
        if (!this.isEdit && !this.selectedFile) {
          this.$message.error('请选择应用文件')
          return
        }

        this.submitLoading = true
        
        if (this.isEdit) {
          // 编辑应用
          appApi.updateApp(this.appForm, 
            (response) => {
              // 成功回调
              if (response.data && response.data.code === 0) {
                this.$message.success('更新成功')
                this.dialogVisible = false
                this.fetchAppList()
              } else {
                this.$message.error(response.data?.msg || '更新失败')
              }
              this.submitLoading = false
            },
            (error) => {
              // 失败回调
              console.error('操作失败:', error)
              this.$message.error('操作失败')
              this.submitLoading = false
            }
          )
        } else {
          // 添加应用
          const formData = new FormData()
          formData.append('file', this.selectedFile)
          formData.append('appName', this.appForm.appName)
          formData.append('appType', this.appForm.appType)
          formData.append('version', this.appForm.version)
          formData.append('versionCode', this.appForm.versionCode)
          formData.append('description', this.appForm.description)
          formData.append('changelog', this.appForm.changelog)
          formData.append('isForceUpdate', this.appForm.isForceUpdate)

          appApi.uploadAppFile(formData, 
            (response) => {
              // 成功回调
              if (response.data && response.data.code === 0) {
                this.$message.success('添加成功')
                this.dialogVisible = false
                this.fetchAppList()
              } else {
                this.$message.error(response.data?.msg || '添加失败')
              }
              this.submitLoading = false
            },
            (error) => {
              // 失败回调
              console.error('操作失败:', error)
              this.$message.error('操作失败')
              this.submitLoading = false
            }
          )
        }
      })
    },

    // 关闭对话框
    handleDialogClose() {
      this.resetForm()
    },

    // 重置表单
    resetForm() {
      this.appForm = {
        appName: '',
        appType: '',
        version: '',
        versionCode: 1,
        description: '',
        changelog: '',
        isForceUpdate: false,
        isActive: true,
        sort: 0
      }
      this.selectedFile = null
      this.fileList = []
      this.$nextTick(() => {
        this.$refs.appForm && this.$refs.appForm.clearValidate()
      })
    },

    // 工具方法
    getAppTypeName(appType) {
      const typeMap = {
        'android': 'Android',
        'ios': 'iOS',
        'windows': 'Windows',
        'macos': 'macOS',
        'linux': 'Linux'
      }
      return typeMap[appType] || appType
    },

    getAppTypeTagType(appType) {
      const typeMap = {
        'android': 'success',
        'ios': 'primary',
        'windows': 'warning',
        'macos': 'info',
        'linux': 'danger'
      }
      return typeMap[appType] || 'info'
    },

    formatDate,
    formatFileSize,

    headerCellClassName() {
      return 'header-cell'
    }
  }
}
</script>

<style lang="scss" scoped>
.welcome {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: white;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;

  .page-title {
    margin: 0;
    color: #303133;
    font-size: 24px;
    font-weight: 600;
  }

  .right-operations {
    display: flex;
    align-items: center;
    gap: 15px;

    .search-input {
      width: 300px;

      :deep(.el-input__inner) {
        border-radius: 20px;
        border: 2px solid #e4e7ed;
        padding-left: 20px;
        height: 40px;
        line-height: 40px;
        transition: all 0.3s ease;

        &:focus {
          border-color: #5f70f3;
          box-shadow: 0 0 0 2px rgba(95, 112, 243, 0.2);
        }
      }
    }

    .btn-search {
      background: linear-gradient(135deg, #5f70f3 0%, #6d7cf5 100%);
      border: none;
      border-radius: 20px;
      padding: 12px 24px;
      font-weight: 600;
      transition: all 0.3s ease;

      &:hover {
        background: linear-gradient(135deg, #4a5bd9 0%, #5f70f3 100%);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(95, 112, 243, 0.4);
      }
    }
  }
}

.main-wrapper {
  flex: 1;
  padding: 0 40px 40px;
}

.content-panel {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.content-area {
  padding: 30px;
}

.params-card {
  border: none;
  box-shadow: none;
}

.table_bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
  padding: 20px 0;
  border-top: 1px solid #e4e7ed;
}

.ctrl_btn {
  display: flex;
  gap: 15px;

  .select-all-btn {
    background: linear-gradient(135deg, #5f70f3 0%, #6d7cf5 100%);
    border: none;
    border-radius: 6px;
    padding: 8px 16px;
    font-weight: 500;
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #4a5bd9 0%, #5f70f3 100%);
      transform: translateY(-1px);
    }
  }

  .el-button--success {
    background: linear-gradient(135deg, #5bc98c 0%, #67d89a 100%);
    border: none;
    border-radius: 6px;
    padding: 8px 16px;
    font-weight: 500;
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #4ab87b 0%, #5bc98c 100%);
      transform: translateY(-1px);
    }
  }

  .el-button--danger {
    background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
    border: none;
    border-radius: 6px;
    padding: 8px 16px;
    font-weight: 500;
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #e45656 0%, #f56c6c 100%);
      transform: translateY(-1px);
    }
  }
}

.custom-pagination {
  display: flex;
  align-items: center;
  gap: 10px;

  .pagination-btn {
    background: white;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    padding: 6px 12px;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 14px;
    color: #606266;

    &:hover:not(:disabled) {
      border-color: #5f70f3;
      color: #5f70f3;
      background: #f8f9ff;
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.5;
    }

    &.active {
      background: #5f70f3;
      border-color: #5f70f3;
      color: white;
    }
  }
}

.total-text {
  margin-left: 10px;
  color: #606266;
  font-size: 14px;
}

.page-size-select {
  width: 100px;
  margin-right: 10px;

  :deep(.el-input__inner) {
    height: 32px;
    line-height: 32px;
    border-radius: 4px;
    border: 1px solid #e4e7ed;
    background: #dee7ff;
    color: #606266;
    font-size: 14px;
  }

  :deep(.el-input__suffix) {
    right: 6px;
    width: 15px;
    height: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    top: 6px;
    border-radius: 4px;
  }

  :deep(.el-input__suffix-inner) {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
  }

  :deep(.el-icon-arrow-up:before) {
    content: "";
    display: inline-block;
    border-left: 6px solid transparent;
    border-right: 6px solid transparent;
    border-top: 9px solid #606266;
    position: relative;
    transform: rotate(0deg);
    transition: transform 0.3s;
  }
}

:deep(.transparent-table) {
  background: white;
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;

  .el-table__body-wrapper {
    flex: 1;
    overflow-y: auto;
    max-height: none !important;
  }

  .el-table__header-wrapper {
    flex-shrink: 0;
  }

  .el-table__header th {
    background: white !important;
    color: black;
    font-weight: 600;
    height: 40px;
    padding: 8px 0;
    font-size: 14px;
    border-bottom: 1px solid #e4e7ed;
  }

  .el-table__body tr {
    background-color: white;

    td {
      border-top: 1px solid rgba(0, 0, 0, 0.04);
      border-bottom: 1px solid rgba(0, 0, 0, 0.04);
      padding: 8px 0;
      height: 40px;
      color: #606266;
      font-size: 14px;
    }
  }

  .el-table__row:hover>td {
    background-color: #f5f7fa !important;
  }

  &::before {
    display: none;
  }
}

:deep(.el-table .el-button--text) {
  color: #7079aa !important;
}

:deep(.el-table .el-button--text:hover) {
  color: #5a64b5 !important;
}

:deep(.el-checkbox__inner) {
  background-color: #eeeeee !important;
  border-color: #cccccc !important;
}

:deep(.el-checkbox__inner:hover) {
  border-color: #cccccc !important;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #5f70f3 !important;
  border-color: #5f70f3 !important;
}

:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(2px);
}

:deep(.el-loading-spinner .path) {
  stroke: #6b8cff;
}

.el-table {
  --table-max-height: calc(100vh - 40vh);
  max-height: var(--table-max-height);

  .el-table__body-wrapper {
    max-height: calc(var(--table-max-height) - 40px);
  }
}

@media (min-width: 1144px) {
  .table_bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 40px;
  }

  :deep(.transparent-table) {
    .el-table__body tr {
      td {
        padding-top: 16px;
        padding-bottom: 16px;
      }

      &+tr {
        margin-top: 10px;
      }
    }
  }
}
</style> 