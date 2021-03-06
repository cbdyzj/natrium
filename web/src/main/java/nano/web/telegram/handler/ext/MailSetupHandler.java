package nano.web.telegram.handler.ext;

import nano.support.Onion;
import nano.web.nano.model.Bot;
import nano.web.security.Privilege;
import nano.web.security.UserService;
import nano.web.telegram.BotContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import static nano.support.mail.MailService.EMAIL;

/**
 * Mail setup handler
 */
@Component
public class MailSetupHandler implements Onion.Middleware<BotContext> {

    private final UserService userService;

    public MailSetupHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void via(@NotNull BotContext context, Onion.@NotNull Next next) throws Exception {
        var text = context.text();
        var bot = context.bot();
        if (Bot.NANO.equals(bot.getName()) && isSetMailCommand(text)) {
            this.trySetMailAddress(context);
        } else {
            next.next();
        }
    }

    private void trySetMailAddress(BotContext context) {
        var user = context.getSession().getUser();
        if (!context.userPrivilegeList().contains(Privilege.MAIL)) {
            context.replyMessage("Failed, no mail service permission");
            return;
        }
        var mailAddress = getMailAddress(context.text());
        if (!EMAIL.test(mailAddress)) {
            context.replyMessage("Illegal mail format");
            return;
        }
        user.setEmail(mailAddress);
        this.userService.createOrUpdateUser(user);
        context.replyMessage("Succeed");
    }

    private static boolean isSetMailCommand(String text) {
        int len = "/setmail ".length();
        if (ObjectUtils.isEmpty(text) || text.length() < len) {
            return false;
        }
        return "/setmail ".equalsIgnoreCase(text.substring(0, len));
    }

    private static String getMailAddress(String text) {
        return text.substring("/setmail ".length());
    }
}
