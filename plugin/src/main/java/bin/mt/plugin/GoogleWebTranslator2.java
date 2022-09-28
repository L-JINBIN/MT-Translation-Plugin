package bin.mt.plugin;

import static bin.mt.plugin.Constant.HTTP_CLIENT;
import static bin.mt.plugin.Constant.UA;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 谷歌翻译Web版（2020.12更新）
 *
 * @author Bin
 */
public class GoogleWebTranslator2 {

    public static String translate(String query, String sl, String tl) throws IOException {
        String url = "https://translate.google.cn/_/TranslateWebserverUi/data/batchexecute";
        String arguments = new JSONArray(Arrays.asList(query, sl, tl)).toString();
        arguments = new JSONArray(Arrays.asList("MkEWBc", "[" + arguments + "]")).toString();
        arguments = "[[" + arguments + "]]";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("user-agent", UA)
                .post(new FormBody.Builder().add("f.req", arguments).build())
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        if (response.isSuccessful()) {
            try {
                //noinspection ConstantConditions
                return getResult(response.body().string());
            } catch (JSONException e) {
                throw new IOException(e);
            }
        } else {
            throw new IOException("HTTP response code: " + response.code());
        }
    }

    private static String getResult(String string) throws IOException, JSONException {
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
