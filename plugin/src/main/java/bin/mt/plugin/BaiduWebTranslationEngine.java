package bin.mt.plugin;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

/**
 * 百度翻译Web版
 *
 * @author Bin
 */
public class BaiduWebTranslationEngine extends BaseTranslationEngine {
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
    private List<String> sourceLanguages = Arrays.asList("auto",
            "zh", "en", "jp", "kor", "fra", "spa", "th", "ara", "ru", "pt", "de", "it", "el", "nl",
            "pl", "bul", "est", "dan", "fin", "cs", "rom", "slo", "swe", "hu", "cht", "vie");

    private List<String> targetLanguages = Arrays.asList(
            "zh", "en", "jp", "kor", "fra", "spa", "th", "ara", "ru", "pt", "de", "it", "el", "nl",
            "pl", "bul", "est", "dan", "fin", "cs", "rom", "slo", "swe", "hu", "cht", "vie");

    // 顺序和sourceLanguages对应
    private List<String> ios_639_1_LanguageCodes = Arrays.asList("auto",
            "zh", "en", "ja", "ko", "fr", "es", "th", "ar", "ru", "pt", "de", "it", "el", "nl",
            "pl", "bg", "et", "da", "fi", "cs", "ro", "sl", "sv", "hu", "zh-TW", "vi");

    private LocalString string;

    @Override
    protected void init() {
        string = getContext().getAssetLocalString("String");
        BaiduWebTranslator.readState(getContext().getPreferences());
    }

    @NonNull
    @Override
    public String name() {
        return string.get("btw");
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

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        return BaiduWebTranslator.translate(text, sourceLanguage, targetLanguage);
    }

    @Override
    public void onFinish() {
        BaiduWebTranslator.saveState(getContext().getPreferences());
    }
}
