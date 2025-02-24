package com.cnki.www.cnki_java.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {
    
    /**
     * 验证码生成器配置
     * @return 配置好的DefaultKaptcha实例
     */
    @Bean
    public DefaultKaptcha producer() {
        Properties properties = new Properties();
        // 无边框
        properties.put("kaptcha.border", "no");
        // 字体颜色
        properties.put("kaptcha.textproducer.font.color", "black");
        // 字符间距
        properties.put("kaptcha.textproducer.char.space", "5");
        // 验证码长度（字符个数）
        properties.put("kaptcha.textproducer.char.length", "4");
        // 图片宽度
        properties.put("kaptcha.image.width", "120");
        // 图片高度
        properties.put("kaptcha.image.height", "50");
        
        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
} 