package com.cnki.www.cnki_java.dto;

import org.springframework.web.multipart.MultipartFile;

public class RegisterRequest {
    private String username;
    private String password;
    private String nickname;
    private MultipartFile avatar;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    
    public MultipartFile getAvatar() { return avatar; }
    public void setAvatar(MultipartFile avatar) { this.avatar = avatar; }
} 