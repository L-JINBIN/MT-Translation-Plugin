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
public class GoogleWebTranslationEngine extends BaseTranslationEngine {
    private final List<String> sourceLanguages = Arrays.asList("auto",
            "zh", "zh-TW", "en", "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca",
            "ceb", "co", "cs", "cy", "da", "de", "el", "eo", "es", "et", "eu", "fa",
            "fi", "fr", "fy", "ga", "gd", "gl", "gu", "ha", "haw", "hi", "hmn", "hr",
            "ht", "hu", "hy", "id", "ig", "is", "it", "iw", "ja", "jw", "ka", "kk",
            "km", "kn", "ko", "ku", "ky", "la", "lb", "lo", "lt", "lv", "mg", "mi",
            "mk", "ml", "mn", "mr", "ms", "mt", "my", "ne", "nl", "no", "ny", "pa",
            "pl", "ps", "pt", "ro", "ru", "sd", "si", "sk", "sl", "sm", "sn", "so",
            "sq", "sr", "st", "su", "sv", "sw", "ta", "te", "tg", "th", "tl", "tr",
            "uk", "ur", "uz", "vi", "xh", "yi", "yo", "zu");

    private final List<String> targetLanguages = Arrays.asList(
            "zh", "zh-TW", "en", "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca",
            "ceb", "co", "cs", "cy", "da", "de", "el", "eo", "es", "et", "eu", "fa",
            "fi", "fr", "fy", "ga", "gd", "gl", "gu", "ha", "haw", "hi", "hmn", "hr",
            "ht", "hu", "hy", "id", "ig", "is", "it", "iw", "ja", "jw", "ka", "kk",
            "km", "kn", "ko", "ku", "ky", "la", "lb", "lo", "lt", "lv", "mg", "mi",
            "mk", "ml", "mn", "mr", "ms", "mt", "my", "ne", "nl", "no", "ny", "pa",
            "pl", "ps", "pt", "ro", "ru", "sd", "si", "sk", "sl", "sm", "sn", "so",
            "sq", "sr", "st", "su", "sv", "sw", "ta", "te", "tg", "th", "tl", "tr",
            "uk", "ur", "uz", "vi", "xh", "yi", "yo", "zu");

    private LocalString string;

    @Override
    protected void init() {
        string = getContext().getAssetLocalString("String");
    }

    @NonNull
    @Override
    public String name() {
        return string.get("gtw");
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
        return GoogleWebTranslator2.translate(text, sourceLanguage, targetLanguage);
    }

    @Override
    public void onFinish() {
    }
}
