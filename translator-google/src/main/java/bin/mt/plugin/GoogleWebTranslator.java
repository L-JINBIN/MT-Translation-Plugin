package bin.mt.plugin;

import org.json2.JSONArray;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Bin
 */
public class GoogleWebTranslator {

    public static void main(String[] args) throws IOException {
        System.out.println(translate("apple", "en", "zh"));
        System.out.println(translate("测试测试。测试测试。", "auto", "en"));
    }

    public static String translate(String query, String from, String to) throws IOException {
        String url = "https://translate.google.com/_/TranslateWebserverUi/data/batchexecute";
        String arguments = new JSONArray(Arrays.asList(query, from, to)).toString();
        arguments = new JSONArray(Arrays.asList("MkEWBc", "[" + arguments + "]")).toString();
        arguments = "[[" + arguments + "]]";

        String result = HttpUtils.post(url)
                .formData("f.req", arguments)
                .execute();
        return getResult(result);
    }

    private static String getResult(String string) throws IOException {
        if (!string.startsWith(")]}'\n\n")) {
            throw new IOException("Parse result failed: " + string);
        }
        JSONArray array = new JSONArray(string.substring(6));
        array = array.getJSONArray(0);
        if (!array.getString(1).equals("MkEWBc")) {
            throw new IOException("Parse result failed: " + string);
        }
        array = new JSONArray(array.getString(2));
        array = array.getJSONArray(1).getJSONArray(0).getJSONArray(0).getJSONArray(5);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            sb.append(array.getJSONArray(i).getString(0));
        }
        return sb.toString();
    }

}
