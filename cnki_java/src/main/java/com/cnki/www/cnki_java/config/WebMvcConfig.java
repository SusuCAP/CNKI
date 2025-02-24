package com.cnki.www.cnki_java.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${pdf.storage.path}")
    private String pdfStoragePath;

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .mediaType("pdf", MediaType.APPLICATION_PDF);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置头像文件访问
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + uploadPath + "/");

        // 配置PDF文件访问
        Path pdfPath = Paths.get(pdfStoragePath).toAbsolutePath().normalize();
        String pdfLocation = "file:" + pdfPath.toString() + "/";
        
        logger.info("PDF文件路径配置: {}", pdfLocation);
        logger.info("PDF目录是否存在: {}", Files.exists(pdfPath));
        logger.info("PDF目录是否可读: {}", Files.isReadable(pdfPath));
        logger.info("PDF目录绝对路径: {}", pdfPath.toAbsolutePath());
        
        try {
            Files.list(pdfPath).forEach(p -> {
                try {
                    logger.info("发现文件: {} (大小: {} bytes, 可读: {}, 是文件: {})", 
                        p.getFileName(), 
                        Files.size(p),
                        Files.isReadable(p),
                        Files.isRegularFile(p)
                    );
                } catch (IOException e) {
                    logger.error("无法获取文件信息: {}", p.getFileName(), e);
                }
            });
        } catch (IOException e) {
            logger.error("无法列出PDF目录内容", e);
        }

        registry.addResourceHandler("/pages/**")
                .addResourceLocations(pdfLocation)
                .setCachePeriod(0);  // 禁用缓存以便于调试
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/pages/**")
                .allowedOrigins("*")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .maxAge(3600);
    }
} 