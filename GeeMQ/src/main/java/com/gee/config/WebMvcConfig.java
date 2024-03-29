package com.gee.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
	}
	/**
	 * 配置静态资源文件路径
	 * @param registry
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**/*")
				.addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX+"/static/css/vant/fonts")
				.addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX+"/static/css/element-ui/fonts");
	}
}
