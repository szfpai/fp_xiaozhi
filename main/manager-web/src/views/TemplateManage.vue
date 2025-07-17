<template>
  <div class="welcome">
    <HeaderBar />
    <div class="operation-bar">
      <h2 class="page-title">角色模版管理</h2>
      <div class="right-operations">
        <el-input placeholder="请输入模版名称查询" v-model="searchName" class="search-input"
          @keyup.enter.native="handleSearch" clearable />
        <el-button class="btn-search" @click="handleSearch">搜索</el-button>
      </div>
    </div>
    <div class="main-wrapper">
      <div class="content-panel">
        <div class="content-area">
          <el-card class="params-card" shadow="never">
            <el-table ref="templateTable" :data="templateList" class="transparent-table" v-loading="loading"
              element-loading-text="拼命加载中" element-loading-spinner="el-icon-loading"
              element-loading-background="rgba(255, 255, 255, 0.7)"
              @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column label="模版名称" prop="agentName" align="center" />
              <el-table-column label="提示词" prop="systemPrompt" align="center" :min-width="220">
                <template slot-scope="scope">
                  <div class="wrap-cell">{{ scope.row.systemPrompt }}</div>
                </template>
              </el-table-column>
              <el-table-column label="语言代码" prop="langCode" align="center" />
              <el-table-column label="语言" prop="language" align="center" />
              <el-table-column label="创建时间" prop="createdAt" align="center">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="更新时间" prop="updatedAt" align="center">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.updatedAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center">
                <template slot-scope="scope">
                  <el-button size="mini" type="text" @click="editTemplate(scope.row)">编辑</el-button>
                  <el-button size="mini" type="text" @click="deleteTemplate(scope.row)">删除</el-button>
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
                  @click="deleteSelectedTemplates">删除</el-button>
              </div>
              <div class="custom-pagination">
                <el-select v-model="pageSize" @change="handlePageSizeChange" class="page-size-select">
                  <el-option v-for="item in pageSizeOptions" :key="item" :label="`${item}条/页`" :value="item" />
                </el-select>
                <button class="pagination-btn" :disabled="currentPage === 1" @click="goFirst">首页</button>
                <button class="pagination-btn" :disabled="currentPage === 1" @click="goPrev">上一页</button>
                <button v-for="page in visiblePages" :key="page" class="pagination-btn"
                  :class="{ active: page === currentPage }" @click="goToPage(page)">{{ page }}</button>
                <button class="pagination-btn" :disabled="currentPage === pageCount" @click="goNext">下一页</button>
                <span class="total-text">共{{ total }}条记录</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="模版名称">
          <el-input v-model="editForm.agentName" />
        </el-form-item>
        <el-form-item label="角色介绍">
          <el-input type="textarea" v-model="editForm.systemPrompt" :rows="6" />
        </el-form-item>
        <el-form-item label="语音活动检测(VAD)">
          <el-select v-model="editForm.vadModelId" placeholder="请选择">
            <el-option v-for="item in vadModelList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="语音识别(ASR)">
          <el-select v-model="editForm.asrModelId" placeholder="请选择">
            <el-option v-for="item in asrModelList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="大语言模型(LLM)">
          <el-select v-model="editForm.llmModelId" placeholder="请选择">
            <el-option v-for="item in llmModelList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="视觉大模型(VLLM)">
          <el-select v-model="editForm.vllmModelId" placeholder="请选择">
            <el-option v-for="item in vllmModelList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="意图识别(Intent)">
          <el-select v-model="editForm.intentModelId" placeholder="请选择">
            <el-option v-for="item in intentModelList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="记忆(Memory)">
          <el-select v-model="editForm.memModelId" placeholder="请选择">
            <el-option v-for="item in memoryModelList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="语音合成(TTS)">
          <el-select v-model="editForm.ttsModelId" @change="handleTtsModelChange" placeholder="请选择">
            <el-option v-for="item in ttsModelList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色音色">
          <el-select v-model="editForm.ttsVoiceId" placeholder="请选择">
            <el-option v-for="item in voiceList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="语言代码">
          <el-select v-model="editForm.langCode" @change="handleLangCodeChange" placeholder="请选择语言代码">
            <el-option label="zh" value="zh" />
            <el-option label="en" value="en" />
          </el-select>
        </el-form-item>
        <el-form-item label="语言">
          <el-select v-model="editForm.language" @change="handleLanguageChange" placeholder="请选择语言">
            <el-option label="中文" value="中文" />
            <el-option label="英文" value="英文" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit">确 定</el-button>
      </div>
    </el-dialog>
    <el-footer>
      <version-footer />
    </el-footer>
  </div>
</template>

<script>
import HeaderBar from '@/components/HeaderBar.vue';
import VersionFooter from '@/components/VersionFooter.vue';
import templateApi from '@/apis/module/template';
import modelApi from '@/apis/module/model';
import { formatDate } from '@/utils/format';

export default {
  components: { HeaderBar, VersionFooter },
  data() {
    return {
      searchName: '',
      loading: false,
      templateList: [],
      currentPage: 1,
      pageSize: 10,
      pageSizeOptions: [10, 20, 50, 100],
      total: 0,
      dialogVisible: false,
      dialogTitle: '编辑模版',
      editForm: {},
      selectedTemplates: [],
      isAllSelected: false,
      ttsModelList: [],
      voiceList: [],
      vadModelList: [],
      asrModelList: [],
      llmModelList: [],
      vllmModelList: [],
      intentModelList: [],
      memoryModelList: [],
    };
  },
  computed: {
    pageCount() {
      return Math.ceil(this.total / this.pageSize);
    },
    visiblePages() {
      const pages = [];
      const maxVisible = 3;
      let start = Math.max(1, this.currentPage - 1);
      let end = Math.min(this.pageCount, start + maxVisible - 1);
      if (end - start + 1 < maxVisible) {
        start = Math.max(1, end - maxVisible + 1);
      }
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
      return pages;
    },
  },
  methods: {
    formatDate,
    handleSelectionChange(val) {
      this.selectedTemplates = val;
      this.isAllSelected = this.selectedTemplates.length === this.templateList.length && this.templateList.length > 0;
    },
    handleSelectAll() {
      if (this.isAllSelected) {
        this.$refs.templateTable.clearSelection();
      } else {
        this.templateList.forEach(row => {
          this.$refs.templateTable.toggleRowSelection(row, true);
        });
      }
      this.isAllSelected = !this.isAllSelected;
    },
    showAddDialog() {
      this.editForm = {
        agentName: '',
        systemPrompt: '',
        langCode: '',
        language: '',
        ttsModelId: '',
        ttsVoiceId: '',
        vadModelId: '',
        asrModelId: '',
        llmModelId: '',
        vllmModelId: '',
        intentModelId: '',
        memModelId: '',
      };
      this.dialogTitle = '新增模版';
      this.dialogVisible = true;
    },
    async deleteSelectedTemplates() {
      if (this.selectedTemplates.length === 0) {
        this.$message.warning('请先选择要删除的模版');
        return;
      }
      this.$confirm(`确定要删除选中的${this.selectedTemplates.length}个模版吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        let successCount = 0;
        let failCount = 0;
        for (const item of this.selectedTemplates) {
          await new Promise((resolve) => {
            templateApi.deleteTemplate(item.id, res => {
              if (res.data.code === 0) {
                successCount++;
              } else {
                failCount++;
              }
              resolve();
            }, () => {
              failCount++;
              resolve();
            });
          });
        }
        this.$message.success(`成功删除${successCount}个，失败${failCount}个`);
        this.fetchTemplateList();
      }).catch(() => {});
    },
    handlePageSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      this.fetchTemplateList();
    },
    handleSearch() {
      this.currentPage = 1;
      this.fetchTemplateList();
    },
    goFirst() {
      this.currentPage = 1;
      this.fetchTemplateList();
    },
    goPrev() {
      if (this.currentPage > 1) {
        this.currentPage--;
        this.fetchTemplateList();
      }
    },
    goNext() {
      if (this.currentPage < this.pageCount) {
        this.currentPage++;
        this.fetchTemplateList();
      }
    },
    goToPage(page) {
      this.currentPage = page;
      this.fetchTemplateList();
    },
    async fetchTemplateList() {
      this.loading = true;
      templateApi.getTemplateList({}, res => {
        if (res.data.code === 0) {
          let data = res.data.data;
          if (this.searchName) {
            data = data.filter(item => item.agentName && item.agentName.includes(this.searchName));
          }
          this.total = data.length;
          // 分页
          const start = (this.currentPage - 1) * this.pageSize;
          const end = start + this.pageSize;
          this.templateList = data.slice(start, end);
        } else {
          this.templateList = [];
          this.total = 0;
        }
        this.loading = false;
      }, err => {
        this.templateList = [];
        this.total = 0;
        this.loading = false;
      });
    },
    getTemplateDetail(id) {
      return new Promise((resolve, reject) => {
        templateApi.getTemplateById(id, res => {
          if (res.data.code === 0) {
            resolve(res.data.data);
          } else {
            this.$message.error(res.data.msg || '获取模版详情失败');
            reject();
          }
        }, () => {
          this.$message.error('获取模版详情失败');
          reject();
        });
      });
    },
    async editTemplate(row) {
      // 先加载所有下拉
      await Promise.all([
        this.fetchTtsModelList(),
        this.fetchModelList('VAD', 'vadModelList'),
        this.fetchModelList('ASR', 'asrModelList'),
        this.fetchModelList('LLM', 'llmModelList'),
        this.fetchModelList('VLLM', 'vllmModelList'),
        this.fetchModelList('Intent', 'intentModelList'),
        this.fetchModelList('Memory', 'memoryModelList'),
      ]);
      // 再拉详情
      const detail = await this.getTemplateDetail(row.id);
      this.editForm = { ...detail };
      // 类型转换和有效性校验
      const idKeyMap = [
        { key: 'ttsModelId', list: this.ttsModelList },
        { key: 'vadModelId', list: this.vadModelList },
        { key: 'asrModelId', list: this.asrModelList },
        { key: 'llmModelId', list: this.llmModelList },
        { key: 'vllmModelId', list: this.vllmModelList },
        { key: 'intentModelId', list: this.intentModelList },
        { key: 'memModelId', list: this.memoryModelList },
      ];
      idKeyMap.forEach(({ key, list }) => {
        if (this.editForm[key]) {
          this.editForm[key] = String(this.editForm[key]);
          if (!list.find(item => String(item.value) === this.editForm[key])) {
            this.editForm[key] = '';
          }
        }
      });
      // TTS模型ID存在时，自动拉取音色
      if (this.editForm.ttsModelId) {
        this.fetchVoiceList(this.editForm.ttsModelId);
      }
      this.dialogTitle = '编辑模版';
      this.dialogVisible = true;
    },
    async submitEdit() {
      // 判空逻辑
      if (!this.editForm.agentName || this.editForm.agentName.trim() === '') {
        this.$message.error('请输入模版名称');
        return;
      }
      if (!this.editForm.systemPrompt || this.editForm.systemPrompt.trim() === '') {
        this.$message.error('请输入角色介绍');
        return;
      }
      if (!this.editForm.langCode || this.editForm.langCode.trim() === '') {
        this.$message.error('请选择语言代码');
        return;
      }
      if (!this.editForm.language || this.editForm.language.trim() === '') {
        this.$message.error('请选择语言');
        return;
      }
      if (!this.editForm.ttsModelId || this.editForm.ttsModelId.trim() === '') {
        this.$message.error('请选择语音合成模型');
        return;
      }
      if (!this.editForm.ttsVoiceId || this.editForm.ttsVoiceId.trim() === '') {
        this.$message.error('请选择角色音色');
        return;
      }
      if (!this.editForm.vadModelId || this.editForm.vadModelId.trim() === '') {
        this.$message.error('请选择语音活动检测模型');
        return;
      }
      if (!this.editForm.asrModelId || this.editForm.asrModelId.trim() === '') {
        this.$message.error('请选择语音识别模型');
        return;
      }
      if (!this.editForm.llmModelId || this.editForm.llmModelId.trim() === '') {
        this.$message.error('请选择大语言模型');
        return;
      }
      if (!this.editForm.vllmModelId || this.editForm.vllmModelId.trim() === '') {
        this.$message.error('请选择视觉大模型');
        return;
      }
      if (!this.editForm.intentModelId || this.editForm.intentModelId.trim() === '') {
        this.$message.error('请选择意图识别模型');
        return;
      }
      if (!this.editForm.memModelId || this.editForm.memModelId.trim() === '') {
        this.$message.error('请选择记忆模型');
        return;
      }

      if (this.dialogTitle === '新增模版') {
        templateApi.addTemplate(this.editForm, res => {
          if (res.data.code === 0) {
            this.$message.success('保存成功');
            this.dialogVisible = false;
            this.fetchTemplateList();
          } else {
            this.$message.error(res.data.msg || '保存失败');
          }
        }, () => {
          this.$message.error('保存失败');
        });
      } else {
        // 编辑模版
        templateApi.updateTemplate(this.editForm, res => {
          if (res.data.code === 0) {
            this.$message.success('保存成功');
            this.dialogVisible = false;
            this.fetchTemplateList();
          } else {
            this.$message.error(res.data.msg || '保存失败');
          }
        }, () => {
          this.$message.error('保存失败');
        });
      }
    },
    async deleteTemplate(row) {
      this.$confirm('确定要删除该模版吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        templateApi.deleteTemplate(row.id, res => {
          if (res.data.code === 0) {
            this.$message.success('删除成功');
            this.fetchTemplateList();
          } else {
            this.$message.error(res.data.msg || '删除失败');
          }
        }, () => {
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },
    handleLangCodeChange(val) {
      if (val === 'zh') {
        this.editForm.language = '中文';
      } else if (val === 'en') {
        this.editForm.language = '英文';
      }
    },
    handleLanguageChange(val) {
      if (val === '中文') {
        this.editForm.langCode = 'zh';
      } else if (val === '英文') {
        this.editForm.langCode = 'en';
      }
    },
    fetchTtsModelList() {
      return new Promise((resolve) => {
        modelApi.getModelNames('TTS', '', (res) => {
          if (res.data.code === 0) {
            this.ttsModelList = res.data.data.map(item => ({
              label: item.modelName,
              value: String(item.id)
            }));
          } else {
            this.ttsModelList = [];
          }
          resolve();
        });
      });
    },
    fetchVoiceList(modelId) {
      if (!modelId) {
        this.voiceList = [];
        return;
      }
      modelApi.getModelVoices(modelId, '', (res) => {
        if (res.data.code === 0) {
          this.voiceList = res.data.data.map(item => ({
            label: item.voiceName,
            value: item.voiceId
          }));
        } else {
          this.voiceList = [];
        }
      });
    },
    handleTtsModelChange(val) {
      this.editForm.ttsModelId = val;
      this.fetchVoiceList(val);
    },
    async fetchModelList(type, listKey) {
      return new Promise((resolve) => {
        modelApi.getModelNames(type, '', (res) => {
          if (res.data.code === 0) {
            this[listKey] = res.data.data.map(item => ({
              label: item.modelName,
              value: String(item.id)
            }));
          }
          resolve();
        });
      });
    }
  },
  watch: {
    'editForm.ttsModelId'(val) {
      if (val) this.fetchVoiceList(val);
    }
  },
  created() {
    this.fetchTemplateList();
    this.fetchTtsModelList();
    this.fetchModelList('VAD', 'vadModelList');
    this.fetchModelList('ASR', 'asrModelList');
    this.fetchModelList('LLM', 'llmModelList');
    this.fetchModelList('VLLM', 'vllmModelList');
    this.fetchModelList('Intent', 'intentModelList');
    this.fetchModelList('Memory', 'memoryModelList');
  }
}
</script>

<style scoped>
.welcome {
  min-height: 100vh;
  background: #f5f7fa;
}
.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0 10px 0;
}
.page-title {
  font-size: 22px;
  font-weight: bold;
}
.right-operations {
  display: flex;
  align-items: center;
}
.search-input {
  width: 220px;
  margin-right: 10px;
}
.table_bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.ctrl_btn {
  display: flex;
  align-items: center;
  gap: 12px;
}
.custom-pagination {
  display: flex;
  align-items: center;
}
.page-size-select {
  width: 100px;
  margin-right: 10px;
}
.pagination-btn {
  margin: 0 2px;
  padding: 2px 8px;
  border: 1px solid #dcdfe6;
  background: #fff;
  cursor: pointer;
}
.pagination-btn.active {
  background: #409EFF;
  color: #fff;
}
.total-text {
  margin-left: 10px;
  color: #888;
}
.wrap-cell {
  white-space: pre-line;
  word-break: break-all;
  text-align: left;
  line-height: 1.6;
  padding: 4px 0;
}
</style> 