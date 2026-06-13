package model.environment;

public enum Season {
    SPRING(1.25),
    SUMMER(0.8),
    AUTUMN(1.2),
    WINTER(0.7);

    private double multiReproduction;   // hệ số sinh theo mùa

    Season(double multi) {
        this.multiReproduction = multi;
    }

    public double getMultiReproduction() {
        return this.multiReproduction;
    }

}