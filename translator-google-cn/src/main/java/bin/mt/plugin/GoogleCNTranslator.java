package bin.mt.plugin;

import org.json2.JSONArray;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Bin
 */
public class GoogleCNTranslator {
    /**
     * IP 可通过访问 https://ping.chinaz.com/translate.google.cn 获取
     */
    private static final String IP = "203.208.40.34";
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(TrustAll.SSL_SOCKET_FACTORY, TrustAll.TRUST_ALL_MANAGER)
            .hostnameVerifier(TrustAll.TRUST_ALL_HOSTNAME_VERIFIER)
            .build();

    public static void main(String[] args) throws IOException {
        System.out.println(translate("apple", "en", "zh"));
        System.out.println(translate("测试测试。测试测试。", "auto", "en"));
        HTTP_CLIENT.connectionPool().evictAll(); // exit
    }

    public static String translate(String query, String from, String to) throws IOException {
        String url = "https://" + IP + "/translate_a/single?client=gtx&dt=t" +
                "&sl=" + from +
                "&tl=" + to +
                "&q=" + URLEncoder.encode(query, "UTF-8");
        Request request = new Request.Builder()
                .get()
                .url(url)
                .header("Host", "translate.googleapis.com")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0;)")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        try {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP response code: " + response.code());
            }
            //noinspection ConstantConditions
            return getResult(response.body().string());
        } finally {
            response.close();
        }
    }

    private static String getResult(String string) throws IOException {
        if (!string.startsWith("[[[")) {
            throw new IOException("Parse result failed: " + string);
        }
        return new JSONArray(string).getJSONArray(0).getJSONArray(0).getString(0);
    }

}
