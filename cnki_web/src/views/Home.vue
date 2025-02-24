<template>
  <div class="background">
    <div class="navbar">
      <div class="option-tools">
        <div
          v-for="(option, index) in options"
          :key="index"
          :class="['option-icon', { selected: selectedIndex === index }]"
          @click="selectOption(index)"
        >
          <div v-html="option.icon"></div>
          <div class="recommend-text">{{ option.text }}</div>
        </div>
        <form class="search-bar" @submit.prevent="search">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="28"
            height="28"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="feather feather-search"
          >
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          <input
            v-on:focus="showSearch = true"
            v-model="keyword"
            type="text"
            placeholder="Search..."
            @input="debouncedSearch"
          />
        </form>
      </div>

      <div class="user-menu">
        <div class="user-name-text">{{ greeting }}, {{ nickname }}!</div>
        <div
          class="user-menu-container"
          @mouseenter="showMenu = true"
          @mouseleave="showMenu = false"
        >
          <div class="user-avatar">
            <!-- 头像加载成功时显示图片 -->
            <img
              v-if="userAvatar"
              :src="userAvatar"
              alt="Aatar"
              @error="handleAvatarError"
              class="avatar-image"
            />

            <!-- 头像为空时，显示文字头像 -->
            <div v-else class="text-avatar">
              <div class="text-avatar-inner">
                {{ avatarInitial }}
              </div>
            </div>
          </div>

          <!-- 悬浮菜单 -->
          <transition name="menu-fade">
            <div v-show="showMenu" class="floating-menu">
              <div class="menu-header">
                <div class="user-info">
                  <div class="user-name">{{ nickname }}</div>
                  <div class="user-email">{{ username }}</div>
                </div>
              </div>

              <div class="menu-body">
                <div class="menu-item" @click="showUpload">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-upload"
                  >
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17 8 12 3 7 8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line></svg
                  >upload paper
                </div>
                <div class="menu-item" @click="showSettings">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-settings"
                  >
                    <circle cx="12" cy="12" r="3"></circle>
                    <path
                      d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"
                    ></path>
                  </svg>
                  account settings
                </div>
                <div class="divider"></div>
                <div class="menu-item logout" @click="logout">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-log-out"
                  >
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                    <polyline points="16 17 21 12 16 7"></polyline>
                    <line x1="21" y1="12" x2="9" y2="12"></line>
                  </svg>
                  logout
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <div v-if="showSearch" class="search-content">
      <!-- 搜索加载动画 -->
      <div v-if="searchLoading" class="loading-container">
        <div class="loading-spinner"></div>
        <div class="loading-text">searching...</div>
      </div>

      <div v-else>
        <!-- 搜索历史部分 - 移到上面 -->
        <div class="search-history-section">
          <div class="search-header">
            <div class="search-history-title">#Search History</div>
            <div
              v-if="searchHistory.length > 0"
              class="clear-history"
              @click="clearHistory">
              clearHistory
            </div>
          </div>

          <div v-if="searchHistory.length === 0" class="search-history-null">
            searchHistory is empty!
          </div>

          <div v-else class="search-history">
            <div
              class="search-history-item"
              v-for="(item, index) in searchHistory"
              :key="index"
              @click="useHistorySearch(item)"
            >
              <div class="search-history-item-text">{{ item }}</div>
            </div>
          </div>
        </div>

        <!-- 搜索结果展示 -->
        <div v-if="keyword" class="search-result">
          <div class="result-header">
            <div class="result-title">searchResult</div>
            <div class="result-count">found {{ searchResult.length }} item result</div>
          </div>

          <div v-if="searchResult.length === 0" class="search-empty">
            no search found
          </div>

          <div v-else class="recommend">
            <div
              class="recommend-item"
              v-for="item in searchResult"
              :key="item.paperId"
            >
              <!-- 使用与推荐页相同的卡片结构 -->
              <img :src="item.pdfUrl" alt="pdf" class="pdf-view" />
              <div class="item-text">
                <div class="item-title">{{ item.title }}</div>
                <div class="item-author">{{ item.authors }}</div>
                <div class="item-year">{{ item.year }}</div>
              </div>

              <div class="pdf-button">
                <div class="tools-button">
                  <div
                    :class="['collect-button', { selected: item.isFavorited }]"
                    @click="toggleFavorite(item.paperId)"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="24"
                      height="24"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      class="feather feather-star"
                    >
                      <polygon
                        points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"
                      ></polygon>
                    </svg>
                    <div class="collect-text">
                      {{ item.isFavorited ? "Favorite" : "Favorite" }}
                    </div>
                  </div>
                </div>

                <div class="tools-button">
                  <div class="pdf-download-button" @click="downloadFiles(item.paperId)">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="24"
                      height="24"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      class="feather feather-download"
                    >
                      <path
                        d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"
                      ></path>
                      <polyline points="7 10 12 15 17 10"></polyline>
                      <line x1="12" y1="15" x2="12" y2="3"></line>
                    </svg>
                    <div class="pdf-download-text">download</div>
                  </div>

                  <div
                    class="pdf-view-button"
                    @click="viewPdf(item.pdfUrl, item.paperId)"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="24"
                      height="24"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      class="feather feather-book-open"
                    >
                      <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
                      <path
                        d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"
                      ></path>
                    </svg>
                    <div class="pdf-view-text">view</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="content" v-if="!showSearch">
      <!-- 加载动画 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <div class="loading-text">loading...</div>
      </div>

      <div v-else class="recommend">
        <div class="recommend-item" v-for="item in papers" :key="item.paperId">
          <img :src="item.pdfUrl" alt="pdf" class="pdf-view" />
          <div class="item-text">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-author">{{ item.authors }}</div>
            <div class="item-year">{{ item.year }}</div>
            <div v-if="selectedIndex === 2" class="view-time">
              {{ formatViewTime(item.viewTime) }}
            </div>
            <div v-if="selectedIndex === 3" class="favorite-time">
              {{ formatViewTime(item.favoriteTime) }}
            </div>
          </div>

          <div class="pdf-button">
            <div class="tools-button">
              <div
                :class="['collect-button', { selected: item.isFavorited }]"
                @click="toggleFavorite(item.paperId)"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="feather feather-star"
                >
                  <polygon
                    points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"
                  ></polygon>
                </svg>
                <div class="collect-text">
                  {{ item.isFavorited ? "Favorite" : "Favorite" }}
                </div>
              </div>
            </div>

            <div class="tools-button">
              <div
                class="pdf-download-button"
                @click="downloadFiles(item.paperId)"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="feather feather-download"
                >
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="7 10 12 15 17 10"></polyline>
                  <line x1="12" y1="15" x2="12" y2="3"></line>
                </svg>
                <div class="pdf-download-text">download</div>
              </div>

              <div
                class="pdf-view-button"
                @click="viewPdf(item.pdfUrl, item.paperId)"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="feather feather-book-open"
                >
                  <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
                  <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>
                </svg>
                <div class="pdf-view-text">view</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加设置对话框组件 -->
    <settings-dialog
      :visible.sync="showSettingsDialog"
      :current-settings="currentSettings"
      @settings-updated="handleSettingsUpdated"
    />

    <!-- 添加上传对话框组件 -->
    <upload-dialog
      :visible.sync="showUploadDialog"
      @upload-success="handleUploadSuccess"
    />
  </div>
</template>

<script>
import axios from "axios";
import { debounce } from "lodash";
import SettingsDialog from "../components/SettingsDialog.vue";
import UploadDialog from "../components/UploadDialog.vue";

export default {
  name: "HomePage",
  components: {
    SettingsDialog,
    UploadDialog,
  },
  data() {
    return {
      keyword: "",
      username: "",
      nickname: "",
      userAvatar: null,
      avatarLoadError: false,
      searchValue: "",
      selectedIndex: 0,
      papers: [], // 当前显示的论文列表
      options: [
        {
          text: "recommend",
          icon: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path></svg>`,
        },
        {
          text: "newest",
          icon: `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle></svg>`,
        },
        {
          text: "History",
          icon: `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-clock"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>`,
        },
        {
          text: "collect",
          icon: `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-star"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>`,
        },
      ],
      searchHistory: JSON.parse(localStorage.getItem("searchHistory") || "[]"),
      searchResult: [],
      showMenu: false,
      showSearch: false,
      unreadCount: 3, // 模拟未读消息
      debouncedSearch: null,
      loading: false,
      searchLoading: false,
      showSettingsDialog: false,
      currentSettings: null,
      showUploadDialog: false,
    };
  },
  computed: {
    greeting() {
      const hour = new Date().getHours();
      if (hour < 5) {
        return "Night Owl";
      } else if (hour < 12) {
        return "Morning";
      } else if (hour < 18) {
        return "Afternoon";
      } else {
        return "Evening";
      }
    },
    avatarInitial() {
      if (this.nickname) {
        return this.nickname.charAt(0).toUpperCase();
      }
      return this.username.charAt(0).toUpperCase();
    },
  },
  methods: {
    async getLatest() {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/papers/latest`,
          {
            params: {
              username: this.username,
            },
          }
        );
        this.papers = response.data.data;
      } catch (error) {
        console.error("获取最新论文失败", error);
      }
    },

    async getRecommend() {
      console.log("推荐获取", this.username);
      try {
        const response = await axios.get(
          `http://localhost:8080/api/papers/recommend`,
          {
            params: {
              username: this.username,
              limit: 100,
            },
          }
        );
        this.papers = response.data.data;
      } catch (error) {
        console.error("获取推荐失败", error);
      }
    },

    async handleSearch() {
      if (!this.keyword.trim()) return;

      this.searchLoading = true;
      try {
        const response = await axios.get(
          `http://localhost:8080/api/papers/search`,
          {
            params: {
              keyword: this.keyword,
              username: this.username,
            },
          }
        );

        this.searchResult = response.data.data;

        // 将搜索关键词添加到历史记录
        if (!this.searchHistory.includes(this.keyword)) {
          this.searchHistory.unshift(this.keyword);
          // 限制历史记录数量
          if (this.searchHistory.length > 10) {
            this.searchHistory.pop();
          }
          localStorage.setItem(
            "searchHistory",
            JSON.stringify(this.searchHistory)
          );
        }
      } catch (error) {
        console.error("搜索失败", error);
      } finally {
        this.searchLoading = false;
      }
    },

    async selectOption(index) {
      this.showSearch = false;
      this.loading = true;
      this.selectedIndex = index;
      try {
        switch (index) {
          case 0:
            await this.getRecommend();
            break;
          case 1:
            await this.getLatest();
            break;
          case 2:
            await this.getHistory();
            break;
          case 3:
            await this.getFavorites();
            break;
        }
      } finally {
        this.loading = false;
      }
    },

    handleAvatarError() {
      this.avatarLoadError = true;
      this.userAvatar = null; // 清空错误头像
    },
    showProfile() {
      console.log("打开个人资料");
    },
    showNotifications() {
      console.log("查看通知");
    },
    showSettings() {
      this.currentSettings = {
        username: this.username,
        nickname: this.nickname,
        avatar: this.userAvatar,
      };
      this.showSettingsDialog = true;
    },
    logout() {
      localStorage.removeItem("isLoggedIn");
      this.$router.push("/");
    },
    async toggleFavorite(paperId) {
      try {
        const paper = [...this.papers, ...this.searchResult].find(
          (p) => p.paperId === paperId
        );
        if (!paper) return;

        if (paper.isFavorited) {
          // 取消收藏
          const response = await axios.delete(
            `http://localhost:8080/api/papers/favorite`,
            {
              params: {
                username: this.username,
                paperId: paperId,
              },
            }
          );
          if (response.data.code === 200) {
            this.updatePaperInAllLists(response.data.data);
            // 如果在收藏列表页面，立即移除该论文
            if (this.selectedIndex === 3) {
              this.papers = this.papers.filter((p) => p.paperId !== paperId);
            }
          }
        } else {
          // 添加收藏
          const response = await axios.post(
            `http://localhost:8080/api/papers/favorite`,
            null,
            {
              params: {
                username: this.username,
                paperId: paperId,
              },
            }
          );
          if (response.data.code === 200) {
            this.updatePaperInAllLists(response.data.data);
          }
        }
      } catch (error) {
        console.error("收藏操作失败", error);
      }
    },

    updatePaperInAllLists(paperData) {
      // 更新推荐/历史列表
      const paperIndex = this.papers.findIndex(
        (p) => p.paperId === paperData.paperId
      );
      if (paperIndex !== -1) {
        this.$set(this.papers, paperIndex, {
          ...this.papers[paperIndex],
          isFavorited: paperData.isFavorited,
        });
      }

      // 更新搜索结果列表
      const searchIndex = this.searchResult.findIndex(
        (p) => p.paperId === paperData.paperId
      );
      if (searchIndex !== -1) {
        this.$set(this.searchResult, searchIndex, {
          ...this.searchResult[searchIndex],
          isFavorited: paperData.isFavorited,
        });
      }
    },

    viewPdf(pdfUrl, paperId) {
      console.log("查看pdf", paperId);
      window.open(pdfUrl, "_blank");
      console.log("查看pdf", this.username, paperId);
      axios
        .post(
          `http://localhost:8080/api/papers/view?username=${this.username}&paperId=${paperId}`
        )
        .then((response) => {
          console.log(response);
        })
        .catch((error) => {
          console.error("查看pdf失败", error);
        });
    },

    async getHistory() {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/papers/view-history`,
          {
            params: {
              username: this.username,
            },
          }
        );
        this.papers = response.data.data;
      } catch (error) {
        console.error("获取浏览历史失败", error);
      }
    },

    formatViewTime(timeString) {
      if (!timeString) return "";
      const viewTime = new Date(timeString);
      const now = new Date();
      const diff = now - viewTime;

      // 小于1小时
      if (diff < 3600000) {
        const minutes = Math.floor(diff / 60000);
        return `${minutes} 分钟前`;
      }
      // 小于24小时
      if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000);
        return `${hours} 小时前`;
      }
      // 小于7天
      if (diff < 604800000) {
        const days = Math.floor(diff / 86400000);
        return `${days} 天前`;
      }
      // 其他情况显示具体日期
      return viewTime.toLocaleDateString();
    },

    async getFavorites() {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/papers/favorites`,
          {
            params: {
              username: this.username,
            },
          }
        );
        this.papers = response.data.data;
      } catch (error) {
        console.error("获取收藏列表失败", error);
      }
    },

    useHistorySearch(keyword) {
      this.keyword = keyword;
      this.handleSearch();
    },

    clearHistory() {
      this.searchHistory = [];
      localStorage.removeItem("searchHistory");
    },

    handleSettingsUpdated(newSettings) {
      // 更新本地状态
      this.username = newSettings.username;
      this.nickname = newSettings.nickname;
      this.userAvatar = newSettings.avatar;
      this.avatarLoadError = false; // 重置头像加载错误状态

      // 如果头像加载失败，会触发 handleAvatarError
      const img = new Image();
      img.onerror = () => {
        this.handleAvatarError();
      };
      img.src = newSettings.avatar;
      this.getUserInfo(this.username);
      // 可以添加提示
      alert("设置已更新");
    },

    showUpload() {
      this.showUploadDialog = true;
    },

    handleUploadSuccess(paperData) {
      // 可以根据需要刷新论文列表
      this.selectOption(this.selectedIndex);
      // 可以将新上传的论文添加到当前列表
      if (this.selectedIndex === 1) {
        // 如果在"最新"标签页
        this.papers.unshift(paperData);
      }
      // 显示成功提示
      alert("论文上传成功");
    },

    downloadFiles(paperId) {
      console.log(paperId);
      window.open(`http://localhost:8080/api/papers/download/${paperId}`);
    },

    getUserInfo(token) {
      const username = token;
      console.log(token, "token");
      axios
        .get("http://localhost:8080/api/user/info", {
          params: {
            username: username,
          },
        })
        .then((response) => {
          console.log("获取用户信息成功:", response);
          this.username = response.data.data.username;
          this.userAvatar = response.data.data.avatar;
          this.nickname = response.data.data.nickname;
          this.selectOption(0); // 默认加载推荐
        })
        .catch((error) => {
          console.error("获取用户信息失败:", error);
        });
    },
  },
  mounted() {
    this.getUserInfo(String(this.$route.params.username));
  },
  created() {
    this.debouncedSearch = debounce(this.handleSearch, 1000);
  },
};
</script>

<style scoped src="../components/css/Home.css"></style>