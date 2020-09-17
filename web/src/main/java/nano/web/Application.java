package nano.web;

import nano.support.configuration.ConditionalOnRabbit;
import nano.web.security.AuthenticationInterceptor;
import nano.web.service.messageing.ExchangeDeclarer;
import nano.web.service.scripting.Scripting;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableAsync
@SpringBootApplication(proxyBeanMethods = false)
public class Application implements ApplicationContextAware, WebMvcConfigurer {

    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * App config vars
     */
    @Bean
    @ConfigurationProperties("nano")
    public ConfigVars configVars() {
        return new ConfigVars();
    }

    /**
     * Cache resources with ETag
     */
    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

    /**
     * Rest template for sending HTTP request
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    /**
     * Declare exchanges on rabbit property set
     */
    @Bean
    @ConditionalOnRabbit
    public ExchangeDeclarer exchangeDeclarer(Environment env) {
        return new ExchangeDeclarer();
    }

    /**
     * Convert message to JSON
     */
    @Bean
    @ConditionalOnRabbit
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 为API增加鉴权
     *
     * @see nano.web.security.Authorized
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        var interceptor = this.applicationContext.getBean(AuthenticationInterceptor.class);
        registry.addInterceptor(interceptor).addPathPatterns("/api/**");
    }

    /**
     * 增加Scripting相关静态资源的content type
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.mediaTypes(Scripting.MEDIA_TYPE);
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
