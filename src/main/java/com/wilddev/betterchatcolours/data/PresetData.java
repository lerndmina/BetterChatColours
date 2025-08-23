package com.wilddev.betterchatcolours.data;

import java.util.List;
import java.util.Objects;

public class PresetData {
    
    private final String name;
    private final List<String> colors;
    private final long createdTime;
    
    public PresetData(String name, List<String> colors) {
        this.name = name;
        this.colors = colors;
        this.createdTime = System.currentTimeMillis();
    }
    
    public PresetData(String name, List<String> colors, long createdTime) {
        this.name = name;
        this.colors = colors;
        this.createdTime = createdTime;
    }
    
    public String getName() {
        return name;
    }
    
    public List<String> getColors() {
        return colors;
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public String getGradientString() {
        if (colors.isEmpty()) {
            return "";
        }
        
        if (colors.size() == 1) {
            return "<color:" + colors.get(0) + ">";
        }
        
        return "<gradient:" + String.join(":", colors) + ">";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PresetData that = (PresetData) o;
        return Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public String toString() {
        return "PresetData{" +
                "name='" + name + '\'' +
                ", colors=" + colors +
                ", createdTime=" + createdTime +
                '}';
    }
}
