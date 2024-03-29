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
            "74.125.196.113",
            "108.177.97.100",
            "64.233.189.191",
            "142.250.105.103",
            "216.239.32.40",
            "142.250.149.120"
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
                            System.out.println("Success " + s);
                            newIP.compareAndSet(null, s);
                        } else {
                            System.out.println("Fail " + s);
                        }
                    } catch (Exception e) {
                        System.out.println("Error " + s);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            }.start();
        }
        while (true) {
            boolean finished;
            try {
                finished = countDownLatch.await(100, TimeUnit.MICROSECONDS);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ip = newIP.get();
            if (ip != null) {
                return okIP = ip;
            }
            if (finished) {
                throw new RuntimeException("无可用IP");
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
        JSONArray array = new JSONArray(string).getJSONArray(0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            sb.append(array.getJSONArray(i).getString(0));
        }
        return sb.toString();
    }

}
