package com.weather.service;

import com.weather.util.DBConnection;
import com.weather.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WeatherService {

    private String API_KEY = "API_KEY"; 

    public String getApiKey() {
        return API_KEY;
    }

    public void setApiKey(String apiKey) {
        this.API_KEY = apiKey;
    }
    /**
     * 搜索城市信息，只显示第一个城市
     *
     * @param cityName 城市名称或拼音
     */
    public void searchCity(String cityName) {
        try {
            String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
            String apiUrl = "https://geoapi.qweather.com/v2/city/lookup?location=" + encodedCityName + "&key=" + API_KEY;

            JSONObject response = HttpUtil.sendGetRequest(apiUrl);

            if (response == null || !"200".equals(response.getString("code"))) {
                System.out.println("城市搜索失败，请检查 API Key 或网络连接！");
                return;
            }

            JSONArray locationArray = response.getJSONArray("location");
            if (locationArray.isEmpty()) {
                System.out.println("未找到城市信息，请检查输入！");
                return;
            }

            JSONObject cityInfo = locationArray.getJSONObject(0);
            saveCityToDatabase(cityInfo); // 保存城市信息到数据库

            System.out.println("\n搜索结果：");
            System.out.printf("%-10s%-15s%-10s%-10s%n", "城市名称", "城市ID", "经度", "纬度");
            System.out.printf("%-10s%-15s%-10.2f%-10.2f%n",
                    cityInfo.getString("name"),
                    cityInfo.getString("id"),
                    cityInfo.getDouble("lon"),
                    cityInfo.getDouble("lat"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 查询三天的天气信息，通过城市名称
     *
     * @param cityName 城市名称或拼音
     */
    public void queryThreeDayWeather(String cityName) {
        try {
            String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
            String cityApiUrl = "https://geoapi.qweather.com/v2/city/lookup?location=" + encodedCityName + "&key=" + API_KEY;

            JSONObject cityResponse = HttpUtil.sendGetRequest(cityApiUrl);

            if (cityResponse == null || !"200".equals(cityResponse.getString("code"))) {
                System.out.println("城市搜索失败，请检查城市名称和 API Key！");
                return;
            }

            JSONArray locationArray = cityResponse.getJSONArray("location");
            if (locationArray.isEmpty()) {
                System.out.println("未找到城市信息，请检查输入！");
                return;
            }

            // 默认选择第一个匹配的城市 ID 和城市名称
            JSONObject cityInfo = locationArray.getJSONObject(0);
            String cityId = cityInfo.getString("id");
            String cityNameResolved = cityInfo.getString("name");

            // 保存城市信息到数据库
            saveCityToDatabase(cityInfo);

            String weatherApiUrl = "https://devapi.qweather.com/v7/weather/3d?location=" + cityId + "&key=" + API_KEY;
            JSONObject weatherResponse = HttpUtil.sendGetRequest(weatherApiUrl);

            if (weatherResponse == null || !"200".equals(weatherResponse.getString("code"))) {
                System.out.println("天气查询失败，请检查城市 ID 和 API Key！");
                return;
            }

            JSONArray dailyArray = weatherResponse.getJSONArray("daily");
            saveWeatherToDatabase(cityId, cityNameResolved, dailyArray); // 保存天气信息到数据库

            System.out.printf("\n%s三天的天气预报：%n", cityNameResolved);
            System.out.printf("%-15s%-10s%-10s%-10s%n", "日期", "最高温", "最低温", "天气状况");
            for (int i = 0; i < dailyArray.length(); i++) {
                JSONObject dayWeather = dailyArray.getJSONObject(i);
                System.out.printf("%-15s%-10s%-10s%-10s%n",
                        dayWeather.getString("fxDate"),
                        dayWeather.getString("tempMax") + "°C",
                        dayWeather.getString("tempMin") + "°C",
                        dayWeather.getString("textDay"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 保存城市信息到数据库
     *
     * @param cityInfo 城市信息 JSON 对象
     */
    public void saveCityToDatabase(JSONObject cityInfo) {
        String sql = "INSERT INTO cities (id, name, lon, lat) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), lon = VALUES(lon), lat = VALUES(lat)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cityInfo.getString("id"));
            pstmt.setString(2, cityInfo.getString("name"));
            pstmt.setDouble(3, cityInfo.getDouble("lon"));
            pstmt.setDouble(4, cityInfo.getDouble("lat"));

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("城市信息保存成功！");
            } else {
                System.out.println("城市信息保存失败！");
            }
        } catch (SQLException e) {
            System.err.println("保存城市信息时出现错误！");
            e.printStackTrace();
        }
    }
    /**
     * 保存天气信息到数据库
     *
     * @param cityId   城市 ID
     * @param cityName 城市名称
     * @param dailyArray 天气信息 JSON 数组
     */
    public void saveWeatherToDatabase(String cityId, String cityName, JSONArray dailyArray) {
        String deleteSql = "DELETE FROM weather WHERE city_id = ?";
        String insertSql = "INSERT INTO weather (city_id, city_name, date, temp_max, temp_min, text_day) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // 删除旧数据
            deleteStmt.setString(1, cityId);
            deleteStmt.executeUpdate();

            // 插入新数据
            for (int i = 0; i < dailyArray.length(); i++) {
                JSONObject dayWeather = dailyArray.getJSONObject(i);

                insertStmt.setString(1, cityId);
                insertStmt.setString(2, cityName);
                insertStmt.setString(3, dayWeather.getString("fxDate"));
                insertStmt.setDouble(4, dayWeather.getDouble("tempMax"));
                insertStmt.setDouble(5, dayWeather.getDouble("tempMin"));
                insertStmt.setString(6, dayWeather.getString("textDay"));

                insertStmt.addBatch(); // 批量插入
            }

            int[] rows = insertStmt.executeBatch(); // 执行批量插入
            System.out.println("天气信息保存成功！共插入 " + rows.length + " 条记录。");

        } catch (SQLException e) {
            System.err.println("保存天气信息时出现错误！");
            e.printStackTrace();
        }
    }
}
