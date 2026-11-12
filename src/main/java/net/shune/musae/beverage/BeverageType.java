package net.shune.musae.beverage;

public final class BeverageType {
    public final String id;
    public final int nutrition;
    public final float saturation;
    public final int drunkThreshold;

    public BeverageType(String id, int nutrition, float saturation, int drunkThreshold) {
        this.id = id;
        this.nutrition = nutrition;
        this.saturation = saturation;
        this.drunkThreshold = Math.max(1, drunkThreshold);
    }
}
