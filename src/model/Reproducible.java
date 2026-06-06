package model;

public interface Reproducible {

    boolean canReproduce();

    Entity reproduce();
}
