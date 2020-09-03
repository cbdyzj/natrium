package nano.telegram.handler.text;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nano.service.WikiService;
import nano.support.Onion;
import nano.telegram.BotContext;
import nano.telegram.BotUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WikiHandler implements Onion.Middleware<BotContext> {

    private static final String COMMAND = "wiki";

    // wikipedia language list
    private static final List<String> LANGUAGE_LIST = List.of("zh", "en", "ja");

    @NonNull
    private final WikiService wikiService;

    public void via(BotContext context, Onion.Next next) throws Exception {
        var text = context.text();

        var content = BotUtils.parseCommand(COMMAND, text);
        if (StringUtils.isEmpty(content)) {
            next.next();
            return;
        }

        var extract = this.fetchExtract(content);
        context.sendMessage(extract);
    }

    private String fetchExtract(String title) {
        for (var language : LANGUAGE_LIST) {
            var extract = this.wikiService.getWikiExtract(title, language);
            if (extract != null) {
                return extract;
            }
        }
        return "nano没有找到：" + title;
    }

}