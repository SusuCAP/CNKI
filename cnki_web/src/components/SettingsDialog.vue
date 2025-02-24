<template>
  <div class="settings-dialog" v-if="visible">
    <div class="dialog-overlay" @click="close"></div>
    <div class="dialog-content">
      <div class="dialog-header">
        <h3>账户设置</h3>
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
          <label>Avatar</label>
          <div class="avatar-upload">
            <div class="avatar-preview">
              <img v-if="imagePreview" :src="imagePreview" alt="Avatar Preview">
              <div v-else class="text-avatar">{{ formData.nickname ? formData.nickname[0].toUpperCase() : 'U' }}</div>
            </div>
            <div class="upload-button-group">
              <input
                type="file"
                ref="fileInput"
                @change="handleFileChange"
                accept="image/*"
                style="display: none"
              >
              <button class="upload-button" @click="triggerFileInput">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                select Image
              </button>
              <div class="file-name" v-if="selectedFileName">{{ selectedFileName }}</div>
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>nickname</label>
          <input type="text" v-model="formData.nickname" placeholder="input nickname">
        </div>

        <div class="form-group">
          <label>account</label>
          <input type="text" v-model="formData.username" placeholder="input account">
        </div>

        <div class="form-group">
          <label>new password</label>
          <input type="password" v-model="formData.password" placeholder="Please leave blank if not modified">
        </div>
      </div>

      <div class="dialog-footer">
        <button class="cancel-button" @click="close">cancel</button>
        <button class="save-button" @click="saveSettings" :disabled="saving">
          {{ saving ? 'saving...' : 'save' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'SettingsDialog',
  props: {
    visible: Boolean,
    currentSettings: Object
  },
  data() {
    return {
      formData: {
        username: '',
        password: '',
        avatar: null,
        nickname: ''
      },
      saving: false,
      imagePreview: null,
      selectedFileName: '',
      selectedFile: null
    }
  },
  watch: {
    visible(newVal) {
      if (newVal && this.currentSettings) {
        this.formData = {
          ...this.currentSettings,
          password: ''
        }
        this.imagePreview = this.currentSettings.avatar
        this.selectedFileName = ''
        this.selectedFile = null
      }
    }
  },
  methods: {
    close() {
      this.$emit('update:visible', false)
    },
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    
    handleFileChange(event) {
      const file = event.target.files[0]
      if (!file) return
      
      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        alert('select Image')
        return
      }
      
      // 验证文件大小 (限制为2MB)
      if (file.size > 2 * 1024 * 1024) {
        alert('image size is large, need < 2MB')
        return
      }

      this.selectedFile = file
      this.selectedFileName = file.name
      
      // 创建预览
      const reader = new FileReader()
      reader.onload = e => {
        this.imagePreview = e.target.result
      }
      reader.readAsDataURL(file)
    },
    async saveSettings() {
      if (!this.formData.username || !this.formData.nickname) {
        alert('account & nickname is not empty!')
        return
      }

      this.saving = true
      try {
        const formData = new FormData()
        formData.append('username', this.currentSettings.username)
        
        if (this.formData.username !== this.currentSettings.username) {
          formData.append('newUsername', this.formData.username)
        }
        
        if (this.formData.password) {
          formData.append('password', this.formData.password)
        }
        
        if (this.formData.nickname !== this.currentSettings.nickname) {
          formData.append('nickname', this.formData.nickname)
        }
        
        if (this.selectedFile) {
          formData.append('avatar', this.selectedFile)
        }

        const response = await axios.put('http://localhost:8080/api/user/update', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })

        if (response.data.code === 200) {
          const avatarUrl = response.data.data?.avatar || this.currentSettings.avatar
          
          this.$emit('settings-updated', {
            username: this.formData.username,
            nickname: this.formData.nickname,
            avatar: avatarUrl
          })
          this.close()
        }
      } catch (error) {
        console.error('updateInfoError:', error)
        alert('update error')
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.settings-dialog {
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

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-preview {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f5f5f5;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.text-avatar {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
}

.cancel-button, .save-button {
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

.save-button {
  background-color: #764ba2;
  color: white;
}

.save-button:hover:not(:disabled) {
  background-color: #663e92;
}

.save-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.upload-button-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  background-color: #f5f5f5;
  border: 1px dashed #ddd;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #666;
  font-size: 0.9rem;
}

.upload-button:hover {
  background-color: #eee;
  border-color: #764ba2;
  color: #764ba2;
}

.file-name {
  font-size: 0.8rem;
  color: #666;
  word-break: break-all;
  padding: 4px 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.avatar-preview {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
}
</style> 