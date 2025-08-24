package com.wilddev.betterchatcolours.data;

import java.util.Objects;

public class ColorData {

    private final String name;
    private final String hexCode;
    private final String permission;
    private final String displayName;

    public ColorData(String name, String hexCode, String permission, String displayName) {
        this.name = name;
        this.hexCode = hexCode;
        this.permission = permission;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getHexCode() {
        return hexCode;
    }

    public String getPermission() {
        return permission;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ColorData colorData = (ColorData) o;
        return Objects.equals(name, colorData.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ColorData{" +
                "name='" + name + '\'' +
                ", hexCode='" + hexCode + '\'' +
                ", permission='" + permission + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
