package com.weather;

import com.weather.service.WeatherService;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        WeatherService weatherService = new WeatherService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== 天气查询系统 =====");
            System.out.println("1. 搜索城市信息");
            System.out.println("2. 查询三天天气预报");
            System.out.println("3. 退出");
            System.out.print("请选择操作：");

            int choice = scanner.nextInt();
            scanner.nextLine(); // 清除换行符

            switch (choice) {
                case 1:
                    System.out.print("请输入城市名称或拼音：");
                    String cityName = scanner.nextLine();
                    weatherService.searchCity(cityName);
                    break;
                case 2:
                    System.out.print("请输入城市名称或拼音：");
                    String weatherCityName = scanner.nextLine();
                    weatherService.queryThreeDayWeather(weatherCityName);
                    break;
                case 3:
                    System.out.println("退出系统！");
                    return;
                default:
                    System.out.println("无效选项，请重新选择！");
            }
        }
    }
}
