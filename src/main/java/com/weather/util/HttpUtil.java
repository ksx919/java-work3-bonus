package com.weather.util;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class HttpUtil {

    /**
     * 发送 GET 请求并返回 JSON 响应
     *
     * @param urlStr 请求的 URL（已包含 API Key）
     * @return JSON 响应
     */
    public static JSONObject sendGetRequest(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return null;
            }

            // 检查响应是否为 GZIP 压缩
            InputStream inputStream;
            String contentEncoding = conn.getHeaderField("Content-Encoding");
            if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
                inputStream = new GZIPInputStream(conn.getInputStream());
            } else {
                inputStream = conn.getInputStream();
            }

            // 读取响应内容
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            conn.disconnect();

            // 返回 JSON 响应
            return new JSONObject(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
