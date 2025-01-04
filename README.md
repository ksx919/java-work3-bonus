# 天气查询系统

使用maven管理项目依赖并且已完成以下任务

1、创建数据库weather_db，共两张表为cities、weather

cities            id(主码)        name        lon        lat

城市表          城市id        城市名称   经度       纬度

weather     id(主码)    city_id(外键依赖cities(id))        city_name        date        temp_max        temp_min            text_day

天气表        天气id                    城市id                           城市名称        日期            最高温                最低温         天气情况(如多云)

2、使用和风天气所给的API以及获取的APIKEY，来获取json文件（起初未发现有Gzip加密导致浪费好多时间）

3、使用HttpUtil.java文件发送GET请求并获取json响应

4、使用DBConnection.java文件控制数据库的连接

5、使用WeatherService.java文件来查询城市和天气并保存信息到数据库

6、App.java设计控制台提供查询城市信息、查询三天的天气信息两个功能，且数据库会在查询后自动更新。


