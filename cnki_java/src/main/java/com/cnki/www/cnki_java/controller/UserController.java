package com.cnki.www.cnki_java.controller;

import com.cnki.www.cnki_java.common.ApiResponse;
import com.cnki.www.cnki_java.dto.RegisterRequest;
import com.cnki.www.cnki_java.entity.User;
import com.cnki.www.cnki_java.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.cnki.www.cnki_java.dto.LoginRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cnki.www.cnki_java.dto.UserResponseDTO;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.cnki.www.cnki_java.entity.SearchHistory;
import com.cnki.www.cnki_java.repository.SearchHistoryRepository;
import com.cnki.www.cnki_java.dto.UserUpdateDTO;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${server.base-url}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("创建头像上传目录: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("创建头像上传目录失败", e);
        }
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@ModelAttribute RegisterRequest request) {
        try {
            logger.debug("开始注册流程 - 用户名: {}", request.getUsername());
            
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                logger.warn("用户名已存在: {}", request.getUsername());
                return ApiResponse.error(400, "用户名已存在");
            }

            logger.debug("创建用户对象 - 昵称: {}", request.getNickname());
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setNickname(request.getNickname());

            // 处理头像上传
            if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
                logger.debug("开始处理头像上传 - 文件名: {}", request.getAvatar().getOriginalFilename());
                String fileName = saveFile(request.getAvatar());
                user.setAvatar(fileName);
                logger.info("头像保存成功: {}", fileName);
            } else {
                logger.debug("未上传头像");
            }

            // 保存到数据库
            userRepository.save(user);
            logger.info("用户注册成功 - ID: {}, 用户名: {}", user.getId(), user.getUsername());
            
            return ApiResponse.success("注册成功");
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            return ApiResponse.error(500, "文件上传失败");
        } catch (Exception e) {
            logger.error("注册过程中发生未知错误", e);
            return ApiResponse.error(500, "系统错误");
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        // 获取项目根目录路径
        Path rootPath = Paths.get("").toAbsolutePath();
        Path uploadPath = rootPath.resolve(uploadDir);
        
        logger.info("项目根目录: {}", rootPath);
        logger.info("完整上传路径: {}", uploadPath);
        
        if (!Files.exists(uploadPath)) {
            logger.info("创建上传目录: {}", uploadPath);
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                logger.error("目录创建失败: {}", uploadPath, e);
                throw new IOException("无法创建上传目录");
            }
        }

        // 生成唯一文件名
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + fileExtension;

        // 构建完整保存路径
        Path filePath = uploadPath.resolve(newFileName);
        logger.debug("文件保存路径: {}", filePath.toAbsolutePath());
        
        // 保存文件
        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            logger.error("文件保存失败: {}", filePath, e);
            throw new IOException("文件保存失败");
        }
        
        return newFileName;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request, HttpSession session) {
        logger.debug("开始登录流程 - 用户名: {}", request.getUsername());
        
        // 1. 验证验证码
        String storedCaptcha = (String) session.getAttribute("captcha");
        logger.debug("验证码验证 - SessionID: {}, 存储验证码: {}, 输入验证码: {}", 
            session.getId(), storedCaptcha, request.getCaptcha());
        
        if (storedCaptcha == null) {
            logger.warn("验证码已过期 - 用户名: {}", request.getUsername());
            return ApiResponse.error(400, "验证码已过期");
        }
        if (!storedCaptcha.equalsIgnoreCase(request.getCaptcha())) {
            logger.warn("验证码不匹配 - 输入: {}, 存储: {}", request.getCaptcha(), storedCaptcha);
            return ApiResponse.error(400, "验证码错误");
        }
        
        // 2. 验证用户是否存在
        logger.debug("查询用户: {}", request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
            .orElse(null);
        if (user == null) {
            logger.warn("用户不存在: {}", request.getUsername());
            return ApiResponse.error(400, "账号不存在");
        }
        
        // 3. 验证密码
        logger.debug("验证密码 - 用户ID: {}", user.getId());
        if (!user.getPassword().equals(request.getPassword())) {
            logger.warn("密码错误 - 用户: {}", request.getUsername());
            return ApiResponse.error(400, "密码错误");
        }
        
        // 登录成功处理
        logger.info("用户登录成功 - ID: {}, 用户名: {}", user.getId(), user.getUsername());
        session.removeAttribute("captcha");
        
        // 构建返回数据
        UserResponseDTO userResponse = new UserResponseDTO(user, baseUrl);
        
        return ApiResponse.success(userResponse);
    }

    @GetMapping("/search-history")
    public ApiResponse<?> getSearchHistory(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        List<SearchHistory> history = searchHistoryRepository
            .findByUserOrderBySearchTimeDesc(user);
        
        return ApiResponse.success(history);
    }

    @PutMapping("/api/user/update")
    public ApiResponse<?> updateUserInfo(@ModelAttribute UserUpdateDTO userUpdateDTO) {
        try {
            logger.debug("开始更新用户信息 - 当前用户: {}", userUpdateDTO.getUsername());
            
            // 获取当前用户
            User user = userRepository.findByUsername(userUpdateDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 更新用户名（如果提供了新用户名）
            String newUsername = userUpdateDTO.getNewUsername();
            if (newUsername != null && !newUsername.isEmpty()) {
                // 检查新用户名是否已被使用
                if (!newUsername.equals(userUpdateDTO.getUsername()) && 
                    userRepository.existsByUsername(newUsername)) {
                    logger.warn("用户名已存在: {}", newUsername);
                    return ApiResponse.error(400, "用户名已存在");
                }
                user.setUsername(newUsername);
                logger.info("更新用户名: {} -> {}", userUpdateDTO.getUsername(), newUsername);
            }
            
            // 更新密码
            if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
                user.setPassword(userUpdateDTO.getPassword());
                logger.info("用户密码已更新 - 用户: {}", userUpdateDTO.getUsername());
            }
            
            // 更新昵称
            if (userUpdateDTO.getNickname() != null && !userUpdateDTO.getNickname().isEmpty()) {
                user.setNickname(userUpdateDTO.getNickname());
                logger.info("更新昵称: {}", userUpdateDTO.getNickname());
            }
            
            // 更新头像
            MultipartFile avatar = userUpdateDTO.getAvatar();
            if (avatar != null && !avatar.isEmpty()) {
                logger.debug("开始处理新头像上传");
                
                // 删除旧头像文件
                if (user.getAvatar() != null) {
                    Path oldAvatarPath = Paths.get(uploadDir, user.getAvatar());
                    try {
                        Files.deleteIfExists(oldAvatarPath);
                        logger.debug("已删除旧头像: {}", oldAvatarPath);
                    } catch (IOException e) {
                        logger.warn("删除旧头像失败: {}", oldAvatarPath, e);
                    }
                }
                
                // 保存新头像
                String fileName = saveFile(avatar);
                user.setAvatar(fileName);
                logger.info("新头像已保存: {}", fileName);
            }
            
            // 保存更新
            userRepository.save(user);
            logger.info("用户信息更新成功 - ID: {}", user.getId());
            
            // 构建返回数据
            UserResponseDTO response = new UserResponseDTO(user, baseUrl);
            return ApiResponse.success(response);
            
        } catch (IOException e) {
            logger.error("文件处理失败", e);
            return ApiResponse.error(500, "文件处理失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    @GetMapping("/api/user/info")
    public ApiResponse<?> getUserInfo(@RequestParam String username) {
        try {
            logger.info("接收到获取用户信息请求 - 用户名: {}", username);
            
            // 查找用户
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            logger.debug("找到用户 - ID: {}, 昵称: {}", user.getId(), user.getNickname());
            
            // 构建返回数据
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            
            // 处理头像URL
            String avatarUrl = null;
            if (user.getAvatar() != null) {
                avatarUrl = baseUrl + "/avatars/" + user.getAvatar();
                logger.debug("生成头像URL: {}", avatarUrl);
            }
            userInfo.put("avatar", avatarUrl);
            
            logger.info("成功返回用户信息: {}", userInfo);
            return ApiResponse.success(userInfo);
            
        } catch (Exception e) {
            logger.error("获取用户信息失败 - 用户名: {}, 错误: {}", username, e.getMessage(), e);
            return ApiResponse.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }
} 