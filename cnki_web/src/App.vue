<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
export default {
  data() {
    return {
      isLoggedIn: localStorage.getItem('isLoggedIn') || false
    }
  },
  watch: {
    $route(to) {
      if (to.meta.requiresAuth && !this.isLoggedIn) {
        this.$router.push('/');
      }
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

body, html {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
} 
</style>
