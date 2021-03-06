package nano.web;

import nano.support.configuration.ConditionalOnRabbit;
import nano.support.mail.MailService;
import nano.support.templating.SugarViewResolver;
import nano.support.validation.ValidateInterceptor;
import nano.support.validation.Validated;
import nano.support.validation.Validator;
import nano.web.messageing.ExchangeDeclarer;
import nano.web.nano.ConfigVars;
import nano.web.security.AuthenticationInterceptor;
import nano.web.security.Token;
import nano.web.security.TokenArgumentResolver;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Application entry of web
 *
 * @author cbdyzj
 * @since 2020.8.16
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication(proxyBeanMethods = false)
public class WebApplication implements ApplicationContextAware, WebMvcConfigurer {

    private ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
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
     * Templating
     *
     * @see nano.support.Sugar#render
     */
    @Bean
    public SugarViewResolver sugarViewResolver() {
        var resolver = new SugarViewResolver();
        resolver.setPrefix("classpath:/templates/");
        return resolver;
    }

    /**
     * Validating  advisor
     *
     * @see Validated
     * @see Validator
     * @see ValidateInterceptor
     */
    @Bean
    public DefaultPointcutAdvisor validatePointcutAdvisor() {
        // advisor
        var advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(AnnotationMatchingPointcut.forMethodAnnotation(Validated.class));
        advisor.setAdvice(new ValidateInterceptor(this.context));
        return advisor;
    }

    @Bean
    @ConditionalOnProperty("spring.mail.host")
    public MailService mailService(JavaMailSender javaMailSender, @Value("${spring.mail.username:}") String fromAddress) {
        var mailService = new MailService();
        mailService.setJavaMailSender(javaMailSender);
        mailService.setFromAddress(fromAddress);
        return mailService;
    }

    /**
     * Declare exchanges on rabbit property set
     */
    @Bean
    @ConditionalOnRabbit
    public ExchangeDeclarer exchangeDeclarer() {
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
     * Add token argument resolver
     *
     * @see Token
     * @see TokenArgumentResolver
     */
    @Override
    public void addArgumentResolvers(@NotNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this.context.getBean(TokenArgumentResolver.class));
    }

    /**
     * Authentication interceptor for API
     *
     * @see nano.web.security.Authorized
     */
    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registry.addInterceptor(this.context.getBean(AuthenticationInterceptor.class)).addPathPatterns("/api/**");
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        this.context = applicationContext;
    }


}
