package com.cnki.www.cnki_java.dto;

import com.cnki.www.cnki_java.entity.User;
import org.springframework.beans.factory.annotation.Value;

public class UserResponseDTO {
    private String username;
    private String nickname;
    private String avatarUrl;
    private String status;
    
    @Value("${server.base-url}")
    private String baseUrl;
    
    // 构造函数
    public UserResponseDTO(User user, String baseUrl) {
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.avatarUrl = user.getAvatar() != null ? 
            baseUrl + "/avatars/" + user.getAvatar() : null;
        this.status = "active";
    }
    
    // getters
    public String getUsername() { return username; }
    public String getNickname() { return nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getStatus() { return status; }
}