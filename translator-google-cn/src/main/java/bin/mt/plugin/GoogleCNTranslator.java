package bin.mt.plugin;

import org.json2.JSONArray;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Bin
 */
public class GoogleCNTranslator {
    private static final String[] IPS = {
            "172.217.212.90",
            "142.250.112.90",
            "142.250.27.90",
            "142.250.1.90",
            "172.217.192.90",
            "142.250.13.90",
            "172.217.219.90"
    };
    private static String okIP;
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .build();

    public static void main(String[] args) throws IOException {
        try {
            System.out.println(translate("apple", "en", "zh"));
            System.out.println(translate("测试测试。测试测试。", "auto", "en"));
        } finally {
            HTTP_CLIENT.connectionPool().evictAll(); // exit
        }
    }

    public static synchronized String getIP() {
        String ip = okIP;
        if (ip != null) {
            return ip;
        }
        final AtomicReference<String> newIP = new AtomicReference<>();
        final CountDownLatch countDownLatch = new CountDownLatch(IPS.length);
        for (final String s : IPS) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        String url = "http://" + s + "/translate_a/single?client=gtx&dt=t&sl=en&tl=zh&q=a";
                        Request request = new Request.Builder()
                                .get()
                                .url(url)
                                .header("Host", "translate.googleapis.com")
                                .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0;)")
                                .build();
                        Response response = HTTP_CLIENT.newCall(request).execute();
                        if (response.isSuccessful()) {
                            newIP.compareAndSet(null, s);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            }.start();
        }
        while (true) {
            try {
                if (countDownLatch.await(100, TimeUnit.MICROSECONDS)) {
                    throw new RuntimeException("无可用IP");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ip = newIP.get();
            if (ip != null) {
                return okIP = ip;
            }
        }
    }

    public static String translate(String query, String from, String to) throws IOException {
        String url = "http://" + getIP() + "/translate_a/single?client=gtx&dt=t" +
                "&sl=" + from +
                "&tl=" + to +
                "&q=" + URLEncoder.encode(query, "UTF-8");
        Request request = new Request.Builder()
                .get()
                .url(url)
                .header("Host", "translate.googleapis.com")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0;)")
                .build();
        Response response;
        try {
            response = HTTP_CLIENT.newCall(request).execute();
        } catch (IOException e) {
            if (e.getMessage().toLowerCase().contains("timeout")) {
                okIP = null;
            }
            throw e;
        }
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
