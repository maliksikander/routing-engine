package com.ef.mediaroutingengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * Custom Configuration for Swagger
     *
     * @return
     */

    @Bean
    public Docket swaggerConfiguration() {

        // return a prepared Docket Instance
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(PathSelectors.any())
                .apis(RequestHandlerSelectors.basePackage("com.ef.spring.controllers"))
                .build()
                .pathMapping("/")
                .apiInfo(apiDetails());
    }

    private ApiInfo apiDetails() {
        return new ApiInfoBuilder().title("Spring Boot Boilerplate Rest API").version("1.0")
                .description("").build();
    }
}
