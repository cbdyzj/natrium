package nano.web.telegram;

import nano.web.nano.model.Bot;
import nano.web.nano.ConfigVars;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @see <a href="https://core.telegram.org/bots/api">Telegram Bot API</a>
 */
@Service
public class TelegramService {

    private static final ParameterizedTypeReference<Map<String, ?>> STRING_OBJECT_MAP_TYPE = new ParameterizedTypeReference<>() {
    };

    private static final String TELEGRAM_API = "https://api.telegram.org/bot%s/%s";
    private static final String TELEGRAM_FILE_API = "https://api.telegram.org/file/bot%s/%s";

    private final RestTemplate restTemplate;

    private final ConfigVars configVars;

    public TelegramService(@NotNull RestTemplate restTemplate, @NotNull ConfigVars configVars) {
        Assert.notNull(restTemplate, "restTemplate must be not null");
        Assert.notNull(configVars, "configVars must be not null");
        this.restTemplate = restTemplate;
        this.configVars = configVars;
    }

    public Map<String, ?> setWebhook() {
        var apiKey = this.configVars.getNanoApiKey();
        var nanoApi = this.configVars.getNanoApi();
        var result = new HashMap<String, Object>();
        for (var bot : this.configVars.getBots().values()) {
            String botName = bot.getName();
            var endpoint = "/api/telegram/webhook/%s/%s".formatted(botName, apiKey);
            var url = createUrl(endpoint, nanoApi);
            var r = this.call(bot, "setWebhook", Map.of("url", url));
            result.put(botName, r);
        }
        return result;
    }

    public Map<String, ?> sendMessage(@NotNull Bot bot, Map<String, ?> payload) {
        var text = (String) payload.get("text");
        // Text of the message to be sent, 1-4096 characters after entities parsing
        Assert.notNull(text, "Text is missing");
        Assert.isTrue(text.length() <= 4096, "Text length too long");
        return this.call(bot, "sendMessage", payload);
    }

    public Map<String, ?> replyPhoto(@NotNull Bot bot, @NotNull Number chatId, @NotNull Number replyToMessageId, @NotNull Resource photo) {
        var payload = Map.of(
                "chat_id", chatId,
                "reply_to_message_id", replyToMessageId,
                "photo", photo
        );
        return this.callPostForm(bot, "sendPhoto", payload);
    }

    public Map<String, ?> getFile(@NotNull Bot bot, @NotNull String fileId) {
        var payload = Map.of("file_id", fileId);
        return this.call(bot, "getFile", payload);
    }

    public Path downloadFile(@NotNull Bot bot, @NotNull String filePath) {
        var fileUrl = getFileUrl(bot, filePath);
        return this.restTemplate.execute(fileUrl, HttpMethod.GET, null, response -> {
            var tempFile = Files.createTempFile("download", "tmp");
            tempFile.toFile().deleteOnExit();
            var is = response.getBody();
            Assert.notNull(is, "response input stream require non null");
            var os = Files.newOutputStream(tempFile);
            try (is; os) {
                is.transferTo(os);
            }
            return tempFile;
        });
    }

    /**
     * Telegram API caller, Call POST JSON
     */
    public Map<String, ?> call(@NotNull Bot bot, @NotNull String method, @NotNull Map<String, ?> payload) {
        var telegramApi = getTelegramApi(bot, method);
        var url = URI.create(telegramApi);
        var request = RequestEntity.post(url).body(payload);
        var response = this.restTemplate.exchange(request, STRING_OBJECT_MAP_TYPE);
        return response.getBody();
    }

    /**
     * Telegram API caller, Call POST Form
     */
    public Map<String, ?> callPostForm(@NotNull Bot bot, @NotNull String method, @NotNull Map<String, ?> payload) {
        var telegramApi = getTelegramApi(bot, method);
        var url = URI.create(telegramApi);
        var formData = new LinkedMultiValueMap<String, Object>();
        payload.forEach(formData::add);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        var request = RequestEntity.post(url).headers(headers).body(formData);
        var response = this.restTemplate.exchange(request, STRING_OBJECT_MAP_TYPE);
        return response.getBody();
    }

    public static String getFileUrl(@NotNull Bot bot, @NotNull String filePath) {
        var token = bot.getToken();
        return TELEGRAM_FILE_API.formatted(token, filePath);
    }

    public static String getTelegramApi(@NotNull Bot bot, @NotNull String method) {
        var token = bot.getToken();
        return TELEGRAM_API.formatted(token, method);
    }

    private static String createUrl(String spec, String context) {
        try {
            return new URL(new URL(context), spec).toString();
        } catch (MalformedURLException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
