package nano.web;

import nano.web.security.AuthenticationInterceptor;
import nano.web.security.SecurityService;
import nano.web.service.scripting.ScriptResourceTransformer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.*;

import java.time.Duration;

@EnableAsync
@SpringBootApplication
public class Application implements ApplicationContextAware, WebMvcConfigurer {

    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    @ConfigurationProperties("nano")
    public ConfigVars configVars() {
        return new ConfigVars();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        var ctx = this.applicationContext;
        var securityService = ctx.getBean(SecurityService.class);
        var interceptor = new AuthenticationInterceptor(securityService);
        var telegramApi = "/api/telegram/**";
        var telegramWebhookApi = "/api/telegram/webhook/*";
        // Telegram API interceptor, exclude Telegram webhook API
        registry.addInterceptor(interceptor).addPathPatterns(telegramApi).excludePathPatterns(telegramWebhookApi);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // https://html.spec.whatwg.org/multipage/scripting.html#scriptingLanguages
        var javascript = MediaType.parseMediaType("text/javascript");
        configurer.mediaType("mjs", javascript)
                .mediaType("jsx", javascript)
                .mediaType("ts", javascript)
                .mediaType("tsx", javascript);
    }

    /**
     * @see WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#addResourceHandlers
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var ctx = this.applicationContext;
        var resourceProperties = ctx.getBean(ResourceProperties.class);
        Duration cachePeriod = resourceProperties.getCache().getPeriod();
        CacheControl cacheControl = resourceProperties.getCache().getCachecontrol().toHttpCacheControl();
        var registration = registry.addResourceHandler("/**/*.mjs", "/**/*.jsx", "/**/*.ts", "/**/*.tsx")
                .addResourceLocations(resourceProperties.getStaticLocations())
                .setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl);
        var resourceChain = registration.resourceChain(true);
        resourceChain.addTransformer(ctx.getBean(ScriptResourceTransformer.class));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private static Integer getSeconds(Duration cachePeriod) {
        return (cachePeriod != null) ? (int) cachePeriod.getSeconds() : null;
    }


}