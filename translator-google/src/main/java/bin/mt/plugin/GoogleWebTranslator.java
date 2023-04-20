package bin.mt.plugin;

import org.json2.JSONArray;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author Bin
 */
public class GoogleWebTranslator {

    public static void main(String[] args) throws IOException {
        System.out.println(translate("apple. he. she.", "en", "zh"));
        System.out.println(translate("测试测试。测试测试。", "auto", "en"));
    }

    public static String translate(String query, String from, String to) throws IOException {
        String url = "http://translate.googleapis.com/translate_a/single?client=gtx&dt=t" +
                "&sl=" + from +
                "&tl=" + to +
                "&q=" + URLEncoder.encode(query, "UTF-8");
        return getResult(HttpUtils.get(url).execute());
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
