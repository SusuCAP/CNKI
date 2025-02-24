import Vue from 'vue'
import Router from 'vue-router'
import Login from '../components/Login.vue'
import Home from '../views/Home.vue'

Vue.use(Router)

const router = new Router({
  mode: 'history',
  routes: [
    { path: '/', component: Login },
    { path: '/home', component: Home, meta: { requiresAuth: true }, name: 'Home' }
  ]
})

// 添加完整的导航守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫触发:', to.path, '登录状态:', localStorage.getItem('isLoggedIn'));
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 直接检查登录状态（根据实际业务需求）
    if (!localStorage.getItem('isLoggedIn')) {
      next({
        path: '/',
        query: { redirect: to.fullPath }
      });
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router; 