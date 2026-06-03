package model.environment;

import java.util.Random;

public class Weather {

    private WeatherType currentWeather;
    private int weatherTicks;
    private final Random random;

    private Season currentSeason;
    private int seasonTicks;

    public Weather() {
        this.random = new Random();
        this.currentWeather = WeatherType.SUNNY;
        this.weatherTicks = 0;
        this.currentSeason = Season.SPRING;
        this.seasonTicks = 0;
    }

    public void update() {

        weatherTicks++;
        seasonTicks++;

        if (weatherTicks >= 1800) {

            weatherTicks = 0;

            int roll = random.nextInt(100);

            switch (currentSeason) {

                case SPRING:

                    if (roll < 50)
                        currentWeather = WeatherType.SUNNY;
                    else if (roll < 85)
                        currentWeather = WeatherType.CLOUDY;
                    else
                        currentWeather = WeatherType.RAINY;

                    break;

                case SUMMER:

                    if (roll < 70)
                        currentWeather = WeatherType.SUNNY;
                    else if (roll < 90)
                        currentWeather = WeatherType.CLOUDY;
                    else
                        currentWeather = WeatherType.RAINY;

                    break;

                case AUTUMN:

                    if (roll < 40)
                        currentWeather = WeatherType.SUNNY;
                    else if (roll < 75)
                        currentWeather = WeatherType.CLOUDY;
                    else
                        currentWeather = WeatherType.RAINY;

                    break;

                case WINTER:

                    if (roll < 25)
                        currentWeather = WeatherType.SUNNY;
                    else if (roll < 60)
                        currentWeather = WeatherType.CLOUDY;
                    else
                        currentWeather = WeatherType.SNOW;

                    break;
            }

            System.out.println("Weather changed to: " + currentWeather);
        }

        if(seasonTicks > 7200) { // ~120 giây

            seasonTicks = 0;

            switch(currentSeason) {

                case SPRING:
                    currentSeason = Season.SUMMER;
                    break;

                case SUMMER:
                    currentSeason = Season.AUTUMN;
                    break;

                case AUTUMN:
                    currentSeason = Season.WINTER;
                    break;

                case WINTER:
                    currentSeason = Season.SPRING;
                    break;
            }
            System.out.println(
                    "Season changed to: " + currentSeason
            );
        }
    }

    public double getGrassGrowthMultiplier() {

        switch (currentSeason) {

            case SPRING:
                return 1.5;

            case SUMMER:
                return 1.0;

            case AUTUMN:
                return 0.8;

            case WINTER:
                return 0.4;

            default:
                return 1.0;
        }
    }

    public WeatherType getCurrentWeather() {
        return currentWeather;
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }
}