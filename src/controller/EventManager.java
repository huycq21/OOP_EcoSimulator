package controller;

public final class EventManager {

    private EventManager() {}

    public static void animalBorn(String animalName) {
        System.out.println("[BIRTH] " + animalName);
    }

    public static void animalDied(String animalName) {
        System.out.println("[DEATH] " + animalName);
    }

    public static void plantSpawned(String plantName) {
        System.out.println("[PLANT] " + plantName);
    }

    public static void animalHide(String animalName) {
        System.out.println("[HIDE] " + animalName);
    }

    public static void animalLeaveBush(String animalName) {
        System.out.println("[LEAVE BUSH] " + animalName);
    }
}
