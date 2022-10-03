package bin.mt.plugin;

import org.json2.JSONArray;
import org.json2.JSONObject;

import java.io.IOException;
import java.util.UUID;

public class YandexWebTranslator {

    public static void main(String[] args) throws IOException {
        System.out.println(translate("apple", "en", "zh"));
    }

    public static String translate(String query, String from, String to) throws IOException {
        String lang;
        if (from.equals("auto"))
            lang = to;
        else
            lang = from + '-' + to;
        JSONObject result = HttpUtils.post("https://translate.yandex.net/api/v1/tr.json/translate")
                .formData("srv", "android")
                .formData("ucid", UUID.randomUUID().toString().replace("-", ""))
                .formData("lang", lang)
                .formData("text", query)
                .executeToJson();
        return getResult(result);
    }

    private static String getResult(JSONObject json) throws IOException {
        int code = json.getInt("code");
        if (code != 200) {
            throw new IOException("Error " + code + ": " + json.getString("message"));
        }
        JSONArray array = json.getJSONArray("text");
        StringBuilder sb = new StringBuilder();
        int length = array.length();
        for (int i = 0; i < length; i++) {
            sb.append(array.getString(i));
        }
        return sb.toString();
    }

}
