package com.cnki.www.cnki_java.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
public class CaptchaController {
    private static final Logger logger = LoggerFactory.getLogger(CaptchaController.class);

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    /**
     * 生成验证码接口
     * @param request Http请求对象
     * @param response Http响应对象
     * @throws IOException 图片输出异常
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置响应头禁止缓存
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        // 设置响应内容类型为JPEG图片
        response.setContentType("image/jpeg");

        // 生成随机验证码文本
        String capText = defaultKaptcha.createText();
        // 将验证码存入Session（后续验证使用）
        HttpSession session = request.getSession();
        session.setAttribute("captcha", capText);
        
        logger.debug("生成验证码 - SessionID: {}, 验证码: {}", session.getId(), capText);
        
        // 生成验证码图片
        BufferedImage bi = defaultKaptcha.createImage(capText);
        // 将图片写入响应输出流
        ImageIO.write(bi, "jpg", response.getOutputStream());
    }
} 