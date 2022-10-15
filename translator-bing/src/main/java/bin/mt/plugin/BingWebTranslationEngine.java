package bin.mt.plugin;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

/**
 * 谷歌翻译Web版
 *
 * @author Bin
 */
public class BingWebTranslationEngine extends BaseTranslationEngine {
    private final List<String> sourceLanguages = Arrays.asList("auto",
            "zh-CN", "zh-TW", "en", "da", "uk", "uz", "ur", "hy", "ru", "bg", "hr", "is",
            "ca", "hu", "af", "kn", "hi", "id", "gu", "kk", "tr", "cy", "bn", "ne", "iw",
            "el", "ku", "de", "it", "lv", "cs", "sk", "sl", "sw", "pa", "ja", "ps", "ka",
            "mi", "fr", "pl", "bs", "fa", "te", "ta", "th", "ht", "ga", "et", "sv", "zu",
            "lt", "ug", "my", "ro", "lo", "fi", "hmn", "nl", "tl", "sm", "pt", "es", "vi",
            "az", "am", "sq", "ar", "ko", "mk", "mr", "ml", "ms", "mt", "km", "so");

    private final List<String> targetLanguages = sourceLanguages.subList(1, sourceLanguages.size());

    private LocalString string;

    @Override
    protected void init() {
        string = getContext().getAssetLocalString("String");
        BingWebTranslator.setDomain(string.get("domain"));
    }

    @NonNull
    @Override
    public String name() {
        return string.get("plugin_name");
    }

    @NonNull
    @Override
    public List<String> loadSourceLanguages() {
        return sourceLanguages;
    }

    @NonNull
    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return targetLanguages;
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        return BingWebTranslator.translate(text, sourceLanguage, targetLanguage);
    }

    @Override
    public void onFinish() {
    }
}
