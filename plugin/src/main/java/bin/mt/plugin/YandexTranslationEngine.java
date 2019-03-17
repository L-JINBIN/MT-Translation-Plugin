package bin.mt.plugin;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.translation.BaseTranslationEngine;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Yandex翻译API版
 *
 * @author Bin
 */
public class YandexTranslationEngine extends BaseTranslationEngine {
    private List<String> sourceLanguages = Arrays.asList("auto",
            "zh", "en", "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "ceb", "cs", "cy",
            "da", "de", "el", "eo", "es", "et", "eu", "fa", "fi", "fr", "ga", "gd", "gl", "gu",
            "he", "hi", "hr", "ht", "hu", "hy", "id", "is", "it", "ja", "jv", "ka", "kk", "km",
            "kn", "ko", "ky", "la", "lb", "lo", "lt", "lv", "mg", "mi", "mk", "ml", "mn", "mr",
            "ms", "mt", "my", "ne", "nl", "no", "pa", "pl", "pt", "ro", "ru", "si", "sk", "sl",
            "sq", "sr", "su", "sv", "sw", "ta", "te", "tg", "th", "tl", "tr", "tt", "uk", "ur",
            "uz", "vi", "xh", "yi");

    private List<String> targetLanguages = Arrays.asList(
            "zh", "en", "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "ceb", "cs", "cy",
            "da", "de", "el", "eo", "es", "et", "eu", "fa", "fi", "fr", "ga", "gd", "gl", "gu",
            "he", "hi", "hr", "ht", "hu", "hy", "id", "is", "it", "ja", "jv", "ka", "kk", "km",
            "kn", "ko", "ky", "la", "lb", "lo", "lt", "lv", "mg", "mi", "mk", "ml", "mn", "mr",
            "ms", "mt", "my", "ne", "nl", "no", "pa", "pl", "pt", "ro", "ru", "si", "sk", "sl",
            "sq", "sr", "su", "sv", "sw", "ta", "te", "tg", "th", "tl", "tr", "tt", "uk", "ur",
            "uz", "vi", "xh", "yi");

    private LocalString string;

    @Override
    protected void init() {
        string = getContext().getAssetLocalString("String");
    }

    @NonNull
    @Override
    public String name() {
        return string.get("yt");
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
    public String getLanguageDisplayName(String language) {
        if (language.equals("he"))
            language = "iw";
        else if (language.equals("jv"))
            language = "jw";
        return super.getLanguageDisplayName(language);
    }

    private String key;

    @Override
    public void onStart() {
        SharedPreferences preferences = getContext().getPreferences();
        key = preferences.getString(Constant.YANDEX_KEY_PREFERENCE_KEY, Constant.YANDEX_KEY_DEFAULT);
        if (key.isEmpty())
            key = Constant.YANDEX_KEY_DEFAULT;
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        String lang;
        if (sourceLanguage.equals("auto"))
            lang = targetLanguage;
        else
            lang = sourceLanguage + '-' + targetLanguage;

        String url = "https://translate.yandex.net/api/v1.5/tr.json/translate"
                + "?key=" + key
                + "&lang=" + lang;

        FormBody formBody = new FormBody.Builder(Charset.defaultCharset())
                .add("text", text)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Response response = GoogleWebTranslator.HTTP_CLIENT.newCall(request).execute();
        if (response.isSuccessful()) {
            try {
                //noinspection ConstantConditions
                return getResult(response.body().string());
            } catch (JSONException e) {
                throw new IOException(e);
            }
        } else {
            throw new IOException("HTTP response code: " + response.code());
        }
    }

    private static String getResult(String string) throws JSONException, IOException {
        JSONObject json = new JSONObject(string);
        int code = json.getInt("code");
        if (code != 200) {
            throw new IOException("Error " + code
                    + ": " + json.getString("message"));
        }
        JSONArray array = json.getJSONArray("text");
        StringBuilder sb = new StringBuilder();
        int length = array.length();
        for (int i = 0; i < length; i++) {
            sb.append(array.getString(i));
        }
        return sb.toString();
    }

}
