<template>
    <div class="background">

        <div v-show="showLoginForm" class="login-container">
            <div class="login-title">
                <div class="login-title-text">{{ showLoginForm ? 'Login' : 'Register' }}</div>
                <div class="login-title-subtext">Numerous related previews available</div>
            </div>

            <div class="login-form">
                <div class="login-form-item">
                    <div class="login-user-name">
                        <div class="login-user-name-label">Username</div>

                        <input class="login-user-name-input" type="text" placeholder="Enter your username"
                            v-model="loginForm.username">

                    </div>

                    <div class="login-password">
                        <div class="login-password-label">Password</div>

                        <input class="login-password-input" type="password" placeholder="Enter your password"
                            v-model="loginForm.password">

                    </div>

                    <div class="captcha">
                        <input class="captcha-input" type="text" placeholder="Enter your captcha"
                            v-model="loginForm.captcha">
                        <img class="captcha-img" @click="getCaptcha" :src="captchaSrc">
                    </div>
                </div>


                <div class="tips">
                    <div class="tips-text" :style="{ visibility: isError === '' ? 'hidden' : 'visible' }">
                        {{ isError }}
                    </div>

                    <div class="forget-button">forget password?</div>
                </div>

                <button class="login-button" @click="login">Login</button>
            </div>

            <div class="register-tips">New User? <span class="register-tips-button"
                    @click="showLoginForm = !showLoginForm">Signup</span></div>

        </div>

        <div v-show="!showLoginForm" class="register-container" :style="{ height: showLoginForm ? '650px' : '780px' }">
            <div class="register-title">
                <div class="register-title-text">Register</div>

            </div>

            <div class="register-form-item">
                <div class="avatar">
                    <input type="file" class="avatar-input" ref="avatarInput" style="display: none;" 
                        @change="chooseAvatar" accept="image/*" />
                    <img :src="avatarPreview" alt="" class="choose-avatar" @click="triggerAvatar" />
                </div>

                <div class="register-nickname">
                    <div class="register-nickname-label">Nickname</div>
                    <input class="register-nickname-input" type="text" placeholder="Enter your nickname"
                        v-model="registerForm.nickname">
                </div>

                <div class="register-user-name">
                    <div class="register-user-name-label">Username</div>

                    <input class="register-user-name-input" type="text" placeholder="Enter your username"
                        v-model="registerForm.username">

                </div>

                <div class="register-password">
                    <div class="register-password-label">Password</div>

                    <input class="register-password-input" type="password" placeholder="Enter your password"
                        v-model="registerForm.password">
                    <input class="register-password-input" type="password" placeholder="Confirm your password"
                        v-model="registerForm.confirmPassword">

                </div>

            </div>

            <button class="register-button" @click="register">Register</button>
            <div class="register-tips">Already have an account? <span class="register-tips-button"
                    @click="showLoginForm = !showLoginForm">Login</span></div>
        </div>

        <div :class="showLoginForm ? 'login-footer' : 'register-footer'" ref="vantaRef" >
            <div class="login-footer-text-title">WELLCOME</div>
            <div class="login-footer-text-subtext">{{ showLoginForm ? 'BACK!' : 'SIGNUP!' }}</div>
        </div>
    </div>
</template>

<script>
import axios from 'axios';
import * as THREE from "three";
import FOG from "vanta/src/vanta.fog";  // 引入FOG效果

export default {
    name: 'UserLogin',
    data() {
        return {
            avatar: null,
            avatarPreview: null,
            isError: '',
            showLoginForm: true,
            captchaSrc: "",

            loginForm: {
                username: "",
                password: "",
                captcha: ""
            },
            registerForm: {
                nickname: "",
                username: "",
                password: "",
                confirmPassword: "",
                avatar: null
            }
        }
    },


    methods: {
        login() {
            axios.post('http://localhost:8080/login', this.loginForm, {
                withCredentials: true
            }).then(res => {
                console.log(res);
                if (res.data.code === 200) {
                    localStorage.setItem('isLoggedIn', true);
                    setTimeout(() => {
                        this.$router.push({
                             name: 'Home',
                            params: {
                                username: res.data.data.username,
                                nickname: res.data.data.nickname,
                                avatarUrl: res.data.data.avatarUrl
                            }
                            });
                    }, 50);
                } else {
                    this.isError = '登录失败';
                }
            }).catch(error => {
                console.error('登录错误:', error);
                this.isError = '网络错误';
            });
        },
        getCaptcha() {
            axios.get('http://localhost:8080/captcha', {
                responseType: 'blob',
                withCredentials: true
            })
            .then(res => {
                let imgUrl = URL.createObjectURL(res.data);
                this.captchaSrc = imgUrl;
            })
            .catch(() => {
                this.isError = '获取验证码失败';
            });
        },


        triggerAvatar() {
            this.$refs.avatarInput.click();
        },
        chooseAvatar(event) {
            const file = event.target.files[0];
            if (file) {
                this.registerForm.avatar = file;
                const reader = new FileReader();
                reader.onload = (e) => {
                    this.avatarPreview = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        },
        async register() {
            // 密码确认校验
            if (this.registerForm.password !== this.registerForm.confirmPassword) {
                this.isError = "两次输入的密码不一致";
                return;
            }

            const formData = new FormData();
            formData.append('nickname', this.registerForm.nickname);
            formData.append('username', this.registerForm.username);
            formData.append('password', this.registerForm.password);
            
            // 添加头像文件
            if (this.$refs.avatarInput.files[0]) {
                formData.append('avatar', this.$refs.avatarInput.files[0]);
            }

            try {
                const response = await axios.post('http://localhost:8080/register', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    },
                    withCredentials: true
                });
                
                if (response.data.code === 200) {
                    alert('注册成功');
                    this.showLoginForm = true; // 切换回登录界面
                    // 清空表单
                    this.registerForm = {
                        nickname: "",
                        username: "",
                        password: "",
                        confirmPassword: "",
                        avatar: null
                    };
                    this.avatarPreview = null;
                } else {
                    this.isError = response.data.message;
                }
            } catch (error) {
                this.isError = error.response?.data?.message || '注册失败';
            }
        },
    },
    mounted() {
        this.getCaptcha(); // 组件挂载时获取验证码
        // 在mounted中初始化VANTA效果
        this.vantaEffect = FOG({
            el: this.$refs.vantaRef,  // 绑定到ref
            THREE: THREE,
            mouseControls: true,
            touchControls: true,
            gyroControls: false,
            minHeight: 780.00,
            minWidth: 500.00,
            highlightColor: 0xf5f5bc,
            blurFactor: 0.90,
            zoom: 1
        });
    },
    beforeDestroy() {
        // 销毁VANTA效果
        if (this.vantaEffect) {
            this.vantaEffect.destroy();
        }
    }
}
</script>

<style scoped src="./css/login.css"></style>