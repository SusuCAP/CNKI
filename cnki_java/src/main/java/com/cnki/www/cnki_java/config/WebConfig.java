package com.cnki.www.cnki_java.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取绝对路径并标准化
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = "file:" + uploadPath.toString() + "/";
        
        // 配置头像文件访问
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(location)
                .setCachePeriod(3600)
                .resourceChain(true);
                
        // 配置PDF文件访问
        registry.addResourceHandler("/pages/**")
                .addResourceLocations("file:./pages/")
                .setCachePeriod(3600)
                .resourceChain(true);
                
        logger.info("配置静态资源访问 - 头像路径: {}", location);
    }
} 