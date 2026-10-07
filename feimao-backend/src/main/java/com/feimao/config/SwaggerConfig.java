package com.feimao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("飞猫程序API文档")
                        .description("飞猫物流调度平台接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("飞猫科技")));
    }
}
