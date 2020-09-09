package nano.web.telegram.handler;

import lombok.extern.slf4j.Slf4j;
import nano.support.Json;
import nano.support.Onion;
import nano.web.telegram.BotContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogHandler implements Onion.Middleware<BotContext> {

    @Override
    public void via(BotContext context, Onion.Next next) throws Exception {
        var parameters = context.getParameters();
        log.info("parameters: {}", Json.encode(parameters));
        // next
        next.next();
    }
}