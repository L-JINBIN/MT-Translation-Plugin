package bin.mt.plugin;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 百度翻译Web版
 *
 * @author Bin
 */
public class BaiduWebTranslator {
    private static final OkHttpClient HTTP_CLIENT = GoogleWebTranslator.HTTP_CLIENT;
    private static final String UA = GoogleWebTranslator.UA;

    private static long[] gtk;
    private static String token;
    private static String cookie;

    public static void readState(SharedPreferences preferences) {
        if (!preferences.contains("gtk-0"))
            return;
        gtk = new long[]{preferences.getLong("gtk-0", 0), preferences.getLong("gtk-1", 0)};
        token = preferences.getString("token", "");
        cookie = preferences.getString("cookie", "");
    }

    public static void saveState(SharedPreferences preferences) {
        if (gtk == null)
            return;
        preferences.edit()
                .putLong("gtk-0", gtk[0])
                .putLong("gtk-1", gtk[1])
                .putString("token", token)
                .putString("cookie", cookie)
                .apply();
    }

    public static String translate(String query, String from, String to) throws IOException {
        String result = translateImpl(query, from, to);
        if (result == null) // 可能身份过期
            result = translateImpl(query, from, to);
        if (result == null) // 翻译失败
            throw new IOException("Translation failed");
        return result;
    }

    private static String translateImpl(String text, String from, String to) throws IOException {
        tryInitSync();
        FormBody formBody = new FormBody.Builder(Charset.defaultCharset())
                .add("query", text)
                .add("from", from)
                .add("to", to)
                .add("token", token)
                .add("sign", a(text))
                .build();

        Request request = new Request.Builder()
                .url("https://fanyi.baidu.com/basetrans")
                .header("User-Agent", UA)
                .header("Cookie", cookie)
                .post(formBody)
                .build();

        Response response = HTTP_CLIENT.newCall(request).execute();
        if (response.isSuccessful()) {
            try {
                //noinspection ConstantConditions
                return getResult(response.body().string());
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            throw new IOException("HTTP response code: " + response.code());
        }
    }

    private static String getResult(String string) throws JSONException {
        JSONObject json = new JSONObject(string);
        if (json.getInt("errno") != 0) {
            gtk = null;
            return null;
        }
        JSONArray array = json.getJSONArray("trans");
        StringBuilder sb = new StringBuilder();
        int length = array.length();
        for (int i = 0; i < length; i++) {
            if (i != 0)
                sb.append('\n');
            sb.append(array.getJSONObject(i).getString("dst"));
        }
        return sb.toString();
    }

    private static void tryInitSync() throws IOException {
        if (gtk == null) {
            synchronized (BaiduWebTranslator.class) {
                if (gtk == null) {
                    init();
                }
            }
        }
    }

    private static void init() throws IOException {
        cookie = "BAIDUID=" + generateBaiduId() + ";";
        Request request = new Request.Builder()
                .url("https://fanyi.baidu.com/translate")
                .header("Cookie", cookie)
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        if (response.isSuccessful()) {
            //noinspection ConstantConditions
            String src = response.body().string();
            token = matchToken(src);
            if (token == null) {
                throw new IOException("Parse token failed");
            }
            gtk = matchGTK(src);
            if (gtk == null) {
                throw new IOException("Parse gtk failed");
            }
        } else {
            throw new IOException("HTTP response code: " + response.code());
        }
    }

    private static String generateBaiduId() {
        StringBuilder sb = new StringBuilder(37);
        Random random = new Random();
        int radix = Character.MAX_RADIX;
        for (int i = 0; i < 32; i++) {
            char c = Character.forDigit(random.nextInt(radix), radix);
            sb.append(Character.toUpperCase(c));
        }
        sb.append(":FG=1");
        return sb.toString();
    }

    private static String matchToken(String src) {
        Matcher matcher = Pattern.compile("token\\s*[:=]\\s*['\"](.*?)['\"]",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(src);
        if (matcher.find())
            return matcher.group(1);
        return null;
    }

    private static long[] matchGTK(String src) {
        Matcher matcher = Pattern.compile("gtk\\s*[:=]\\s*['\"]([0-9]+)\\.([0-9]+)['\"]",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(src);
        if (matcher.find()) {
            return new long[]{Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2))};
        }
        return null;
    }

    private static String a(String r) {
        List<String> t = match(r, "[\\uD800\\uDC00-\\uDBFF\\uDFFF]");
        if (t == null) {
            int a = r.length();
            if (a > 30)
                r = "" + r.substring(0, 10)
                        + r.substring((a / 2 - 5), (a / 2 + 5))
                        + r.substring(a - 10);
        } else {
            String[] C = r.split("[\\uD800\\uDC00-\\uDBFF\\uDFFF]");
            int h = 0;
            int f = C.length;
            List<String> u = new ArrayList<>();
            while (f > h) {
                for (char c : C[h].toCharArray()) {
                    u.add(String.valueOf(c));
                }
                if (h != f - 1) {
                    u.add(t.get(h));
                }
                h++;
            }
            int g = u.size();
            if (g > 30) {
                r = join(u.subList(0, 10)) +
                        join(u.subList(g / 2 - 5, g / 2 + 5)) +
                        join(u.subList(g - 10, g));
            }
        }
        return SignUtil.signWeb(r, gtk[0], gtk[1]);
    }


    private static String join(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
        }
        return sb.toString();
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> match(String str, String regex) {
        ArrayList<String> list = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex).matcher(str);
        while (matcher.find()) {
            list.add(matcher.group());
        }
        if (list.isEmpty())
            return null;
        return list;
    }

}
