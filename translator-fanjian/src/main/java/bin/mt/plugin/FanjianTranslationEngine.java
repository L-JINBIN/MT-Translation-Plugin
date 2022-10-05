package bin.mt.plugin;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

/**
 * 繁简互转
 *
 * @author Bin
 */
public class FanjianTranslationEngine extends BaseTranslationEngine {
    private final List<String> sourceLanguages = Arrays.asList("zh-CN", "zh-TW");
    private final List<String> targetLanguages1 = Collections.singletonList("zh-TW");
    private final List<String> targetLanguages2 = Collections.singletonList("zh-CN");

    private LocalString string;

    public FanjianTranslationEngine() {
        super(new ConfigurationBuilder()
                .setTargetLanguageMutable(true)
                .setDisableAutoHideLanguage(true)
                .build()
        );
    }

    @Override
    protected void init() {
        string = getContext().getAssetLocalString("String");
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
        return sourceLanguage.equals("zh-CN") ? targetLanguages1 : targetLanguages2;
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        return FanjianTranslator.translate(text, sourceLanguage, targetLanguage);
    }

}
