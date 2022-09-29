package bin.mt.plugin;

import static bin.mt.plugin.Constant.UA;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 百度翻译Web版
 *
 * @author Bin
 */
public class BaiduWebTranslator {
    private static volatile long[] gtk;
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
        if (from.equalsIgnoreCase("auto"))
            from = langDetect(query);
        String result = translateImpl(query, from, to, false);
        if (result == null) // 可能身份过期
            result = translateImpl(query, from, to, true);
        if (result == null) // 翻译失败
            throw new IOException("Translation failed");
        return result;
    }

    private static String langDetect(String query) {
        try {
            return HttpUtils.post("https://fanyi.baidu.com/langdetect")
                    .header("User-Agent", UA)
                    .header("Cookie", cookie)
                    .formData("query", query)
                    .executeToJson()
                    .getString("lan");
        } catch (Exception e) {
            return "auto";
        }
    }

    private static String translateImpl(String text, String from, String to, boolean throwErrno) throws IOException {
        tryInitSync();
        // 这里使用OKHTTP会返回1022错误，原因不清楚
        JSONObject json = HttpUtils.post("https://fanyi.baidu.com/basetrans")
                .header("User-Agent", UA)
                .header("Cookie", cookie)
                .formData("query", text)
                .formData("from", from)
                .formData("to", to)
                .formData("token", token)
                .formData("sign", sign(text))
                .executeToJson();
        return getResult(json, throwErrno);
    }

    private static String getResult(JSONObject json, boolean throwErrno) throws IOException {
        try {
            if (json.getInt("errno") != 0) {
                gtk = null;
                if (throwErrno) {
                    throw new IOException(json.getString("errmsg"));
                } else {
                    return null;
                }
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
        } catch (JSONException e) {
            throw new IOException(e);
        }
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
        String src = HttpUtils.get("https://fanyi.baidu.com/translate")
                .header("User-Agent", UA)
                .header("Cookie", cookie)
                .execute();
        token = matchToken(src);
        if (token == null) {
            throw new IOException("Parse token failed");
        }
        gtk = matchGTK(src);
        if (gtk == null) {
            throw new IOException("Parse gtk failed");
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

    private static final Pattern PATTERN1 = Pattern.compile("[\uD800\uDC00-\uDBFF\uDFFF]");
    private static final Pattern PATTERN2 = Pattern.compile("[\uD800\uDC00-\uDBFF\uDFFF]|.");

    private static String sign(String text) {
        Matcher matcher = PATTERN1.matcher(text);
        if (matcher.find()) {
            if (matcher.replaceAll(".").length() > 30) {
                matcher = PATTERN2.matcher(text);
                List<String> list = new ArrayList<>();
                while (matcher.find()) {
                    list.add(matcher.group());
                }
                StringBuilder sb = new StringBuilder();
                int size = list.size();
                join(sb, list.subList(0, 10));
                join(sb, list.subList((size / 2) - 5, (size / 2) + 5));
                join(sb, list.subList(size - 10, size));
                text = sb.toString();
            }
        } else if (text.length() > 30) {
            int length = text.length();
            text = text.substring(0, 10)
                    + text.substring((length / 2 - 5), (length / 2 + 5))
                    + text.substring(length - 10);
        }
        return SignUtil.signWeb(text, gtk[0], gtk[1]);
    }

    private static void join(StringBuilder sb, List<String> list) {
        for (String s : list) {
            sb.append(s);
        }
    }

}
