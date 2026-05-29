package model.environment;

import java.util.Random;

public class Weather {

    private WeatherType currentWeather;
    private int weatherTicks;
    private final Random random;

    public Weather() {
        this.random = new Random();
        this.currentWeather = WeatherType.SUNNY;
        this.weatherTicks = 0;
    }

    public void update() {

        weatherTicks++;

        if (weatherTicks >= 3000) { // đổi thời tiết khoảng 50 giây
            weatherTicks = 0;

            WeatherType[] values = WeatherType.values();
            currentWeather = values[random.nextInt(values.length)];

            System.out.println("Weather changed to: " + currentWeather);
        }
    }

    public WeatherType getCurrentWeather() {
        return currentWeather;
    }
}