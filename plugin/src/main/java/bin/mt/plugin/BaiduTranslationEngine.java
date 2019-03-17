package bin.mt.plugin;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import bin.mt.plugin.api.LocalString;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 百度翻译API版
 *
 * @author Bin
 */
public class BaiduTranslationEngine extends BaiduWebTranslationEngine {
    private LocalString string;

    @Override
    protected void init() {
        string = getContext().getAssetLocalString("String");
    }

    @NonNull
    @Override
    public String name() {
        return string.get("bt");
    }


    private String appid;
    private String key;

    @Override
    public void onStart() {
        // 开始翻译前获取下最新配置值
        SharedPreferences preferences = getContext().getPreferences();
        appid = preferences.getString(Constant.BAIDU_APPID_PREFERENCE_KEY, Constant.BAIDU_APPID_DEFAULT);
        if (appid.isEmpty())
            appid = Constant.BAIDU_APPID_DEFAULT;
        key = preferences.getString(Constant.BAIDU_KEY_PREFERENCE_KEY, Constant.BAIDU_KEY_DEFAULT);
        if (key.isEmpty())
            key = Constant.BAIDU_KEY_DEFAULT;
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        String salt = SignUtil.getRandomText(10);
        String sign = SignUtil.md5((appid + text + salt + key).getBytes("UTF-8"));
        String url = "http://api.fanyi.baidu.com/api/trans/vip/translate"
                + "?q=" + URLEncoder.encode(text, "utf-8")
                + "&from=" + sourceLanguage
                + "&to=" + targetLanguage
                + "&appid=" + appid
                + "&salt=" + salt
                + "&sign=" + sign;
        Request request = new Request.Builder()
                .url(url)
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
        if (json.has("error_code")) {
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


}
