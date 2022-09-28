package bin.mt.plugin;

import static bin.mt.plugin.Constant.HTTP_CLIENT;
import static bin.mt.plugin.Constant.UA;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;
import okhttp3.Response;

/**
 * 谷歌翻译Web版
 *
 * @author Bin
 */
@Deprecated
public class GoogleWebTranslator {
    private static long[] tkk;

    public static void readState(SharedPreferences preferences) {
        if (!preferences.contains("tkk-0"))
            return;
        tkk = new long[]{preferences.getLong("tkk-0", 0), preferences.getLong("tkk-1", 0)};
    }

    public static void saveState(SharedPreferences preferences) {
        if (tkk == null)
            return;
        preferences.edit()
                .putLong("tkk-0", tkk[0])
                .putLong("tkk-1", tkk[1])
                .apply();
    }

    public static String translate(String query, String sl, String tl) throws IOException {
        String result = translateImpl(query, sl, tl);
        if (result == null) // 可能身份过期
            result = translateImpl(query, sl, tl);
        if (result == null) // 翻译失败
            throw new IOException("Translation failed");
        return result;
    }

    private static String translateImpl(String query, String sl, String tl) throws IOException {
        tryInitSync();
        String tk = SignUtil.signWeb(query, tkk[0], tkk[1]);

        String url = "https://translate.google.cn/translate_a/single?client=webapp&dt=t" +
                "&sl=" + sl +
                "&tl=" + tl +
                "&tk=" + tk +
                "&q=" + encodeURIComponent(query); // 不能用URLEncoder
        Request request = new Request.Builder()
                .url(url)
                .addHeader("user-agent", UA)
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        if (response.isSuccessful()) {
            try {
                //noinspection ConstantConditions
                return getResult(response.body().string());
            } catch (JSONException e) {
                throw new IOException(e);
            }
        } else if (response.code() == 403) {
            GoogleWebTranslator.tkk = null;
            return null;
        } else {
            throw new IOException("HTTP response code: " + response.code());
        }
    }

    private static String getResult(String string) throws JSONException {
        StringBuilder sb = new StringBuilder();
        JSONArray array = new JSONArray(new JSONTokener(string)).getJSONArray(0);
        for (int i = 0; i < array.length(); i++) {
            sb.append(array.getJSONArray(i).getString(0));
        }
        return sb.toString();
    }

    private static void tryInitSync() throws IOException {
        if (tkk == null) {
            synchronized (GoogleWebTranslator.class) {
                if (tkk == null) {
                    init();
                }
            }
        }
    }

    private static void init() throws IOException {
        Request request = new Request.Builder()
                .url("https://translate.google.cn/")
                .addHeader("user-agent", UA)
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        if (!response.isSuccessful())
            throw new IOException("HTTP response code: " + response.code());
        //noinspection ConstantConditions
        tkk = matchTKK(response.body().string());
        if (tkk == null) {
            throw new IOException("Parse tkk failed");
        }
    }

    private static long[] matchTKK(String src) {
        Matcher matcher = Pattern.compile("tkk\\s*[:=]\\s*['\"]([0-9]+)\\.([0-9]+)['\"]",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(src);
        if (matcher.find()) {
            return new long[]{Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2))};
        }
        return null;
    }

    private static final String HEX = "0123456789ABCDEF";

    private static String encodeURIComponent(String str) {
        if (str == null) return null;

        byte[] bytes = str.getBytes(Charset.defaultCharset());
        StringBuilder builder = new StringBuilder(bytes.length);

        for (byte c : bytes) {
            if (c >= 'a' ? c <= 'z' || c == '~' :
                    c >= 'A' ? c <= 'Z' || c == '_' :
                            c >= '0' ? c <= '9' : c == '-' || c == '.')
                builder.append((char) c);
            else
                builder.append('%')
                        .append(HEX.charAt(c >> 4 & 0xf))
                        .append(HEX.charAt(c & 0xf));
        }

        return builder.toString();
    }
}
