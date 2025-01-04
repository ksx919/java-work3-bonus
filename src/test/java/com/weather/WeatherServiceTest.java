package com.weather;

import com.weather.service.WeatherService;
import com.weather.util.DBConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherServiceTest {

    private WeatherService weatherService;

    @BeforeEach
    public void setUp() {
        weatherService = new WeatherService();
        clearDatabase(); // 清理数据库，确保测试环境干净
    }

    @Test
    @DisplayName("测试数据库连接")
    public void testDatabaseConnection() {
        try (Connection connection = DBConnection.getConnection()) {
            assertNotNull(connection, "数据库连接失败！");
        } catch (Exception e) {
            fail("数据库连接测试失败，抛出异常：" + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试城市查询并保存到数据库")
    public void testSearchCity() {
        String cityName = "北京";
        weatherService.searchCity(cityName);

        // 验证数据库中是否有数据
        try (Connection connection = DBConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cities WHERE name = '北京'")) {

            assertTrue(rs.next(), "未找到保存的城市信息！");
            assertEquals("北京", rs.getString("name"), "城市名称不匹配！");
            assertEquals("101010100", rs.getString("id"), "城市 ID 不匹配！");
        } catch (SQLException e) {
            fail("验证城市信息时出现错误：" + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试天气查询并保存到数据库")
    public void testQueryThreeDayWeather() {
        String cityName = "北京";
        weatherService.queryThreeDayWeather(cityName);

        // 验证数据库中是否有天气数据
        try (Connection connection = DBConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM weather WHERE city_name = '北京'")) {

            int count = 0;
            while (rs.next()) {
                count++;
                assertEquals("北京", rs.getString("city_name"), "城市名称不匹配！");
                assertNotNull(rs.getString("date"), "天气日期为空！");
                assertNotNull(rs.getDouble("temp_max"), "最高温为空！");
                assertNotNull(rs.getDouble("temp_min"), "最低温为空！");
                assertNotNull(rs.getString("text_day"), "天气状况为空！");
            }
            assertEquals(3, count, "天气数据条目数不正确，应为 3 条！");
        } catch (SQLException e) {
            fail("验证天气信息时出现错误：" + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试保存和读取天气信息")
    public void testSaveAndRetrieveWeather() {
        String cityName = "上海";
        weatherService.queryThreeDayWeather(cityName);

        // 验证数据库中的天气信息
        try (Connection connection = DBConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM weather WHERE city_name = '上海'")) {

            assertTrue(rs.next(), "未找到保存的天气信息！");
            assertEquals("上海", rs.getString("city_name"), "城市名称不匹配！");
            assertNotNull(rs.getString("date"), "天气日期为空！");
            assertNotNull(rs.getDouble("temp_max"), "最高温为空！");
            assertNotNull(rs.getDouble("temp_min"), "最低温为空！");
            assertNotNull(rs.getString("text_day"), "天气状况为空！");
        } catch (SQLException e) {
            fail("验证天气信息时出现错误：" + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试异常处理：无效的城市名称")
    public void testInvalidCityName() {
        String invalidCityName = "不存在的城市";
        weatherService.searchCity(invalidCityName);

        // 验证数据库中没有保存任何数据
        try (Connection connection = DBConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cities WHERE name = '" + invalidCityName + "'")) {

            assertFalse(rs.next(), "不应保存无效城市名称的数据！");
        } catch (SQLException e) {
            fail("验证城市信息时出现错误：" + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试异常处理：API 错误")
    public void testApiErrorHandling() {
        // 模拟 API 错误：通过传入错误的 API Key
        String cityName = "北京";
        String originalApiKey = weatherService.getApiKey();

        weatherService.setApiKey("INVALID_API_KEY"); // 设置无效的 API Key
        weatherService.searchCity(cityName);

        // 验证数据库中没有保存任何数据
        try (Connection connection = DBConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cities WHERE name = '" + cityName + "'")) {

            assertFalse(rs.next(), "API 错误时不应保存任何数据！");
        } catch (SQLException e) {
            fail("验证城市信息时出现错误：" + e.getMessage());
        } finally {
            weatherService.setApiKey(originalApiKey); // 恢复原始 API Key
        }
    }

    /**
     * 清理测试数据库
     */
    private void clearDatabase() {
        try (Connection connection = DBConnection.getConnection();
             Statement stmt = connection.createStatement()) {

            stmt.executeUpdate("DELETE FROM weather");
            stmt.executeUpdate("DELETE FROM cities");
            System.out.println("测试环境已清理：清空 cities 和 weather 表。");

        } catch (SQLException e) {
            System.err.println("清理测试环境时出现错误！");
            e.printStackTrace();
        }
    }
}
