// eslint-disable-next-line no-unused-vars
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

Vue.config.productionTip = false

// 配置axios
Vue.prototype.$axios = axios

// 添加路由导航日志
router.afterEach((to, from) => {
  console.log(`Navigated from ${from.path} to ${to.path}`);
});

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
