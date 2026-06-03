package controller;

public final class SimulationTime {
    private static volatile double timeScale = 1.0;

    private SimulationTime() {
    }

    public static double getTimeScale() {
        return timeScale;
    }

    public static void setTimeScale(double value) {
        timeScale = Math.max(0.0, Math.min(8.0, value));
    }
}
