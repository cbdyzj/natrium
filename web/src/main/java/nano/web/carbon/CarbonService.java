package nano.web.carbon;

import nano.support.Json;
import nano.web.carbon.model.CarbonApp;
import nano.web.carbon.model.CarbonKey;
import nano.web.carbon.model.CarbonPage;
import nano.web.carbon.model.CarbonText;
import nano.web.nano.repository.KeyValueRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CarbonService {

    private static final String CARBON = "carbon";

    private final KeyValueRepository keyValueRepository;

    public CarbonService(KeyValueRepository keyValueRepository) {
        this.keyValueRepository = keyValueRepository;
    }

    public void createApp(@NotNull CarbonApp app) {
        validateApp(app);
        this.keyValueRepository.createKeyValue("%s:%s".formatted(CARBON, app.getId()), Json.encode(app));
    }

    public void updateApp(@NotNull CarbonApp app) {
        validateApp(app);
        this.keyValueRepository.updateKeyValue("%s:%s".formatted(CARBON, app.getId()), Json.encode(app));
    }

    private static void validateApp(@NotNull CarbonApp app) {
        Assert.notNull(app, "app must be not null");
        Assert.notNull(app.getId(), "app id must be not null");
        Assert.notEmpty(app.getLocaleList(), "app locale list must be not empty");
        Assert.notNull(app.getFallbackLocale(), "app fallback locale must be not null");
        //
        var pageList = app.getPageList();
        if (CollectionUtils.isEmpty(pageList)) {
            return;
        }
        for (CarbonPage page : pageList) {
            Assert.notNull(page, "page must be not null");
            Assert.notNull(page.getCode(), "page code must be not null");
            var keyList = page.getKeyList();
            if (CollectionUtils.isEmpty(keyList)) {
                continue;
            }
            for (CarbonKey key : keyList) {
                Assert.notNull(key, "key must be not null");
                Assert.notNull(key.getKey(), "key key must be not null");
                Assert.notNull(key.getPageCode(), "key page code must be not null");
                Assert.notEmpty(key.getOriginal(), "key original must be not empty");
            }
        }
    }

    public @NotNull CarbonText getText(@NotNull String appId, @NotNull String key, @NotNull String locale) {
        var app = this.getApp(appId);
        var pageList = app.getPageList();
        Assert.notEmpty(pageList, "app has no pages");
        var carbonKey = pageList.stream()
                .map(CarbonPage::getKeyList)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(it -> Objects.equals(key, it.getKey()))
                .findFirst()
                .orElse(null);
        Assert.notNull(carbonKey, "key is absent");
        var translation = carbonKey.getTranslation();
        Assert.notEmpty(translation, "translation is empty");
        var textMap = translation.stream()
                .collect(Collectors.toMap(CarbonText::getLocale, Function.identity()));
        var text = textMap.get(locale);
        if (text == null) {
            var fallbackLocale = app.getFallbackLocale();
            text = textMap.get(fallbackLocale);
        }
        Assert.notNull(text, "key text is absent");
        return text;
    }

    public @NotNull CarbonApp getApp(@NotNull String appId) {
        var keyValue = this.keyValueRepository.queryKeyValue("%s:%s".formatted(CARBON, appId));
        Assert.notNull(keyValue, "carbon app record is absent");
        var value = keyValue.getValue();
        Assert.notNull(value, "carbon app value is absent");
        var app = Json.decodeValue(value, CarbonApp.class);
        Assert.notNull(app, "app is absent");
        return app;
    }

}