package bin.mt.plugin;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public interface Constant {
    OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
    String UA = "Mozilla/5.0 (Linux; Android 6.0;)";

    String BAIDU_APPID_PREFERENCE_KEY = "BAIDU_APPID";
    String BAIDU_KEY_PREFERENCE_KEY = "BAIDU_KEY";

    /**
     * 百度翻译默认appid
     */
    String BAIDU_APPID_DEFAULT = "20160303000014473";

    /**
     * 百度翻译默认key
     */
    String BAIDU_KEY_DEFAULT = "71L93pxZqoHdp2dMnN_n";

}
