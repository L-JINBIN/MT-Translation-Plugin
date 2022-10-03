package bin.mt.plugin;

import static bin.mt.plugin.Constant.UA;

import org.json2.JSONException;
import org.json2.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpUtils {

    public static Request post(String url) {
        return new Request(url, true);
    }

    public static Request get(String url) {
        return new Request(url, false);
    }

    public static class Request {
        private final String url;
        private final boolean post;
        private final StringBuilder postData;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private String charset = "UTF-8";

        private Request(String url, boolean post) {
            this.url = url;
            this.post = post;
            this.postData = post ? new StringBuilder() : null;
            header("User-Agent", UA);
        }

        public Request setCharset(String charset) {
            this.charset = charset;
            return this;
        }

        public Request header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Request formData(String key, String value) {
            if (postData.length() == 0) {
                headers.put("Content-Type", "application/x-www-form-urlencoded");
            } else {
                postData.append('&');
            }
            try {
                postData.append(key).append('=').append(URLEncoder.encode(value, charset));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public HttpURLConnection createConnection() throws IOException {
            URL mURL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) mURL.openConnection();
            conn.setRequestMethod(post ? "POST" : "GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(post);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
            if (post) {
                OutputStream out = conn.getOutputStream();
                out.write(postData.toString().getBytes(charset));
                out.flush();
                out.close();
            }
            return conn;
        }

        public String execute() throws IOException {
            HttpURLConnection conn = createConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[102400];
                int len;
                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                is.close();
                return baos.toString(charset);
            } else {
                throw new IOException("HTTP response code: " + responseCode);
            }
        }

        public JSONObject executeToJson() throws IOException {
            try {
                return new JSONObject(execute());
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }
    }

}
