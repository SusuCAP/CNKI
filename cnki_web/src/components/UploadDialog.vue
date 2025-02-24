<template>
  <div class="upload-dialog" v-if="visible">
    <div class="dialog-overlay" @click="close"></div>
    <div class="dialog-content">
      <div class="dialog-header">
        <h3>upload Paper</h3>
        <div class="close-button" @click="close">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </div>
      </div>

      <div class="dialog-body">
        <div class="form-group">
          <label>PDF File</label>
          <div class="file-upload">
            <input
              type="file"
              ref="fileInput"
              @change="handleFileChange"
              accept="application/pdf"
              style="display: none"
            >
            <button class="upload-button" @click="triggerFileInput">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              select paper file
            </button>
            <div class="file-info" v-if="selectedFile">
              <div class="file-name">{{ selectedFile.name }}</div>
              <div class="file-size">{{ formatFileSize(selectedFile.size) }}</div>
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>Paper title</label>
          <input type="text" v-model="formData.title" placeholder="Please enter the title of the paper">
        </div>

        <div class="form-group">
          <label>authors</label>
          <input type="text" v-model="formData.authors" placeholder="multiple authors separated by commas">
        </div>

        <div class="form-group">
          <label>publication year</label>
          <input type="number" v-model="formData.year" placeholder="Please enter the year of publication">
        </div>
      </div>

      <div class="dialog-footer">
        <button class="cancel-button" @click="close">cancel</button>
        <button class="upload-submit-button" @click="submitUpload" :disabled="uploading || !canSubmit">
          {{ uploading ? 'uploading...' : 'upload' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'UploadDialog',
  props: {
    visible: Boolean
  },
  data() {
    return {
      formData: {
        title: '',
        authors: '',
        year: new Date().getFullYear()
      },
      selectedFile: null,
      uploading: false
    }
  },
  computed: {
    canSubmit() {
      return this.selectedFile && 
             this.formData.title.trim() && 
             this.formData.authors.trim() && 
             this.formData.year
    }
  },
  methods: {
    close() {
      this.$emit('update:visible', false)
      this.resetForm()
    },

    resetForm() {
      this.formData = {
        title: '',
        authors: '',
        year: new Date().getFullYear()
      }
      this.selectedFile = null
    },

    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    
    handleFileChange(event) {
      const file = event.target.files[0]
      if (!file) return
      
      if (file.type !== 'application/pdf') {
        alert('请选择PDF文件')
        return
      }
      
      // 限制文件大小为20MB
      if (file.size > 20 * 1024 * 1024) {
        alert('文件大小不能超过20MB')
        return
      }

      this.selectedFile = file
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    async submitUpload() {
      if (!this.canSubmit) return

      this.uploading = true
      try {
        const formData = new FormData()
        formData.append('file', this.selectedFile)
        formData.append('title', this.formData.title)
        formData.append('authors', this.formData.authors)
        formData.append('year', this.formData.year)

        const response = await axios.post('http://localhost:8080/api/papers/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })

        if (response.data.code === 200) {
          this.$emit('upload-success', response.data.data)
          this.close()
        }
      } catch (error) {
        console.error('上传失败:', error)
        if (error.response?.data?.message) {
          alert(error.response.data.message)
        } else {
          alert('上传失败，请重试')
        }
      } finally {
        this.uploading = false
      }
    }
  }
}
</script>

<style scoped>
.upload-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
}

.dialog-content {
  position: relative;
  width: 400px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  z-index: 1001;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dialog-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.close-button {
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.close-button:hover {
  background-color: #f5f5f5;
}

.form-group {
  margin-bottom: 20px;
  box-sizing: border-box;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 0.9rem;
}

.form-group input {
  width: 380px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #764ba2;
  box-shadow: 0 0 0 2px rgba(118, 75, 162, 0.1);
}

.file-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background-color: #f5f5f5;
  border: 2px dashed #ddd;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  color: #666;
  font-size: 0.9rem;
}

.upload-button:hover {
  background-color: #eee;
  border-color: #764ba2;
  color: #764ba2;
}

.file-info {
  background-color: #f5f5f5;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 0.9rem;
}

.file-name {
  color: #333;
  word-break: break-all;
  margin-bottom: 4px;
}

.file-size {
  color: #666;
  font-size: 0.8rem;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
}

.cancel-button, .upload-submit-button {
  padding: 8px 20px;
  border-radius: 6px;
  border: none;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-button {
  background-color: #f5f5f5;
  color: #666;
}

.cancel-button:hover {
  background-color: #eee;
}

.upload-submit-button {
  background-color: #764ba2;
  color: white;
}

.upload-submit-button:hover:not(:disabled) {
  background-color: #663e92;
}

.upload-submit-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style> 