package com.poly.BE_main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Đảm bảo đường dẫn /uploads/** ánh xạ tới thư mục thực trên ổ đĩa
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/"); // Dấu chấm nghĩa là thư mục cùng cấp với project
    }
}
