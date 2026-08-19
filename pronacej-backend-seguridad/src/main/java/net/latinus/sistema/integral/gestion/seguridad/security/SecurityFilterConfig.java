package net.latinus.sistema.integral.gestion.seguridad.security;

import org.apache.catalina.filters.AddDefaultCharsetFilter;
import org.apache.catalina.filters.HttpHeaderSecurityFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityFilterConfig {

    @Bean
    public FilterRegistrationBean<AddDefaultCharsetFilter> charsetFilter() {
        FilterRegistrationBean<AddDefaultCharsetFilter> registration = new FilterRegistrationBean<>();
        AddDefaultCharsetFilter filter = new AddDefaultCharsetFilter();
        filter.setEncoding("UTF-8");

        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName("SecurityHeadersFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<HttpHeaderSecurityFilter> httpHeaderSecurityFilter() {
        FilterRegistrationBean<HttpHeaderSecurityFilter> registration = new FilterRegistrationBean<>();
        HttpHeaderSecurityFilter filter = new HttpHeaderSecurityFilter();
        filter.setAntiClickJackingEnabled(true);
        filter.setAntiClickJackingOption("DENY");
        filter.setXssProtectionEnabled(true);

        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName("HttpHeaderSecurityFilter");
        registration.setOrder(2);
        return registration;
    }
}
