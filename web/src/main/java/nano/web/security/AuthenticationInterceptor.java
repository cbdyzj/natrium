package nano.web.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;

import static nano.web.security.TokenCode.*;

@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    @NonNull
    private final SecurityService securityService;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            var authorized = handlerMethod.getMethodAnnotation(Authorized.class);
            if (authorized == null) {
                authorized = handlerMethod.getBeanType().getAnnotation(Authorized.class);
            }
            if (authorized != null) {
                var token = (String) request.getAttribute(D_TOKEN);
                this.securityService.checkTokenPrivilege(token, List.of(authorized.value()));
            }
        }
        return true;
    }
}
