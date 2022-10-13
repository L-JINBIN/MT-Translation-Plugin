package bin.mt.plugin;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

/**
 * 百度翻译API版
 *
 * @author Bin
 */
public class BaiduTranslationEngine extends BaseTranslationEngine {
    /* http://api.fanyi.baidu.com/api/trans/product/apidoc
           auto    自动检测
           zh      中文
           en      英语
           jp      日语
           kor     韩语
           fra     法语
           spa     西班牙语
           th      泰语
           ara     阿拉伯语
           ru      俄语
           pt      葡萄牙语
           de      德语
           it      意大利语
           el      希腊语
           nl      荷兰语
           pl      波兰语
           bul     保加利亚语
           est     爱沙尼亚语
           dan     丹麦语
           fin     芬兰语
           cs      捷克语
           rom     罗马尼亚语
           slo     斯洛文尼亚语
           swe     瑞典语
           hu      匈牙利语
           cht     繁体中文
           vie     越南语
         */
    private final List<String> sourceLanguages = Arrays.asList("auto",
            "zh", "en", "jp", "kor", "fra", "spa", "th", "ara", "ru", "pt", "de", "it", "el", "nl",
            "pl", "bul", "est", "dan", "fin", "cs", "rom", "slo", "swe", "hu", "cht", "vie");

    private final List<String> targetLanguages = Arrays.asList(
            "zh", "en", "jp", "kor", "fra", "spa", "th", "ara", "ru", "pt", "de", "it", "el", "nl",
            "pl", "bul", "est", "dan", "fin", "cs", "rom", "slo", "swe", "hu", "cht", "vie");

    // 顺序和sourceLanguages对应
    private final List<String> ios_639_1_LanguageCodes = Arrays.asList("auto",
            "zh", "en", "ja", "ko", "fr", "es", "th", "ar", "ru", "pt", "de", "it", "el", "nl",
            "pl", "bg", "et", "da", "fi", "cs", "ro", "sl", "sv", "hu", "zh-TW", "vi");

    private LocalString string;

    private int minTranslationTime;

    private static long lastTranslationTime;

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
    public List<String> loadTargetLanguages(String s) {
        return targetLanguages;
    }

    private Map<String, String> baidu2ios_639_1;

    @NonNull
    @Override
    public String getLanguageDisplayName(String language) {
        // 这边将百度翻译语言代码转化为谷歌翻译语言代码即可
        if (baidu2ios_639_1 == null) {
            baidu2ios_639_1 = new HashMap<>();
            for (int i = 0; i < sourceLanguages.size(); i++) {
                baidu2ios_639_1.put(sourceLanguages.get(i), ios_639_1_LanguageCodes.get(i));
            }
        }
        String code = baidu2ios_639_1.get(language);
        return super.getLanguageDisplayName(code);
    }

    private String appid;
    private String key;

    @Override
    public void onStart() {
        // 开始翻译前获取下最新配置值
        SharedPreferences preferences = getContext().getPreferences();
        appid = preferences.getString(BaiduConstant.BAIDU_APPID_PREFERENCE_KEY, BaiduConstant.BAIDU_APPID_DEFAULT);
        if (appid.isEmpty())
            appid = BaiduConstant.BAIDU_APPID_DEFAULT;
        key = preferences.getString(BaiduConstant.BAIDU_KEY_PREFERENCE_KEY, BaiduConstant.BAIDU_KEY_DEFAULT);
        if (key.isEmpty())
            key = BaiduConstant.BAIDU_KEY_DEFAULT;
        int qps;
        try {
            qps = Integer.parseInt(preferences.getString(BaiduConstant.BAIDU_TRANSLATION_QPS, "1"));
            if (qps <= 0) {
                qps = 1;
            }
        } catch (Exception e) {
            qps = 1;
        }
        minTranslationTime = (int) Math.ceil(1000f / qps);
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        String salt = getRandomText(10);
        //noinspection CharsetObjectCanBeUsed
        String sign = md5((appid + text + salt + key).getBytes("UTF-8"));
        String url = "http://api.fanyi.baidu.com/api/trans/vip/translate"
                + "?q=" + URLEncoder.encode(text, "utf-8")
                + "&from=" + sourceLanguage
                + "&to=" + targetLanguage
                + "&appid=" + appid
                + "&salt=" + salt
                + "&sign=" + sign;
        // 跟上次翻译时间间隔至少minTranslationTime
        long diff = lastTranslationTime + minTranslationTime - System.currentTimeMillis();
        if (diff > 0) {
            try {
                Thread.sleep(diff);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String result = getResult(HttpUtils.get(url).executeToJson());
        lastTranslationTime = System.currentTimeMillis(); // 如果异常则不更新时间
        return result;
    }

    private String getResult(JSONObject json) throws JSONException, IOException {
        if (json.has("error_code")) {
            if (json.getString("error_code").equals("54004")) {
                throw new IOException(this.string.get("bt_please_recharge"));
            }
            throw new IOException("Error " + json.getString("error_code")
                    + ": " + json.getString("error_msg"));
        }
        JSONArray array = json.getJSONArray("trans_result");
        StringBuilder sb = new StringBuilder();
        int length = array.length();
        for (int i = 0; i < length; i++) {
            if (i != 0)
                sb.append('\n');
            sb.append(array.getJSONObject(i).getString("dst"));
        }
        return sb.toString();
    }

    public static String getRandomText(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static String toHex(byte[] digest) {
        String hex = "0123456789abcdef";
        char[] str = new char[digest.length * 2];
        int k = 0;
        for (byte b : digest) {
            str[k++] = hex.charAt(b >>> 4 & 0xf);
            str[k++] = hex.charAt(b & 0xf);
        }
        return new String(str);
    }

    private static String md5(byte[] data) {
        try {
            return toHex(MessageDigest.getInstance("MD5").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
