import { getServiceUrl } from '../api'
import RequestService from '../httpRequest'

// 应用管理相关API
export default {
  // 分页查询应用信息
  getAppList(params, callback, failCallback) {
    const queryString = Object.keys(params)
      .filter(key => params[key] !== null && params[key] !== undefined && params[key] !== '')
      .map(key => `${key}=${encodeURIComponent(params[key])}`)
      .join('&')
    
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/list${queryString ? '?' + queryString : ''}`)
      .method('GET')
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.getAppList(params, callback, failCallback)
        })
      }).send()
  },

  // 根据ID查询应用信息
  getAppById(id, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/${id}`)
      .method('GET')
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.getAppById(id, callback, failCallback)
        })
      }).send()
  },

  // 保存应用信息
  saveApp(data, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app`)
      .method('POST')
      .data(data)
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.saveApp(data, callback, failCallback)
        })
      }).send()
  },

  // 更新应用信息
  updateApp(id, data, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/${id}`)
      .method('PUT')
      .data(data)
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.updateApp(id, data, callback, failCallback)
        })
      }).send()
  },

  // 删除应用（单个）
  deleteApp(id, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/${id}`)
      .method('DELETE')
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.deleteApp(id, callback, failCallback)
        })
      }).send()
  },

  // 批量删除应用
  deleteBatchApp(ids, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/batch`)
      .method('POST')
      .data(ids)
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.deleteBatchApp(ids, callback, failCallback)
        })
      }).send()
  },

  // 获取最新版本应用
  getLatestApp(appType, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/latest/${appType}`)
      .method('GET')
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.getLatestApp(appType, callback, failCallback)
        })
      }).send()
  },

  // 上传应用文件
  uploadAppFile(formData, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/upload`)
      .method('POST')
      .data(formData)
      .header({
        'Content-Type': 'multipart/form-data'
      })
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.uploadAppFile(formData, callback, failCallback)
        })
      }).send()
  },

  // 下载应用文件
  downloadApp(id, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/download/${id}`)
      .method('GET')
      .type('blob')
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.downloadApp(id, callback, failCallback)
        })
      }).send()
  },

  // 生成下载二维码
  generateQRCode(id, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/qrcode/${id}`)
      .method('GET')
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.generateQRCode(id, callback, failCallback)
        })
      }).send()
  },

  // 增加下载次数
  incrementDownloadCount(id, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/download-count/${id}`)
      .method('POST')
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.incrementDownloadCount(id, callback, failCallback)
        })
      }).send()
  },

  // 记录下载日志
  recordDownloadLog(data, callback, failCallback) {
    RequestService.sendRequest()
      .url(`${getServiceUrl()}/app/download-log`)
      .method('POST')
      .data(data)
      .success((res) => {
        RequestService.clearRequestTime()
        callback(res)
      })
      .fail((err) => {
        RequestService.clearRequestTime()
        failCallback(err)
      })
      .networkFail(() => {
        RequestService.reAjaxFun(() => {
          this.recordDownloadLog(data, callback, failCallback)
        })
      }).send()
  }
} 