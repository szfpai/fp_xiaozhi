import RequestService from '../httpRequest';

export default {
  // 获取模版列表
  getTemplateList(data, suc, fail) {
    RequestService.sendRequest()
      .url('/fpsphere/agent/template')
      .method('GET')
      .data(data || {})
      .success(suc)
      .fail(fail)
      .send();
  },
  // 获取模版详情
  getTemplateById(id, suc, fail) {
    RequestService.sendRequest()
      .url(`/fpsphere/agent/template/${id}`)
      .method('GET')
      .success(suc)
      .fail(fail)
      .send();
  },
  // 新增模版
  addTemplate(data, suc, fail) {
    RequestService.sendRequest()
      .url('/fpsphere/agent/addtemplate')
      .method('PUT')
      .data(data)
      .success(suc)
      .fail(fail)
      .send();
  },
  // 更新模版
  updateTemplate(data, suc, fail) {
    RequestService.sendRequest()
      .url('/fpsphere/agent/updatetemplate')
      .method('PUT')
      .data(data)
      .success(suc)
      .fail(fail)
      .send();
  },
  // 删除模版（支持单个）
  deleteTemplate(id, suc, fail) {
    RequestService.sendRequest()
      .url(`/fpsphere/agent/deletetemplate/${id}`)
      .method('DELETE')
      .success(suc)
      .fail(fail)
      .send();
  }
} 