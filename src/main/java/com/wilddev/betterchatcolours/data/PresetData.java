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
    
    /**
     * Get the closing tag for this gradient
     */
    public String getClosingTag() {
        if (colors.isEmpty()) {
            return "";
        }
        
        return colors.size() == 1 ? "</color>" : "</gradient>";
    }
    
    /**
     * Create a preview string with sample text
     */
    public String createPreview(String sampleText) {
        if (colors.isEmpty()) {
            return sampleText;
        }
        
        return getGradientString() + sampleText + getClosingTag();
    }
    
    /**
     * Check if this preset is valid
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && 
               colors != null && !colors.isEmpty() &&
               colors.stream().allMatch(color -> color != null && color.matches("^#[0-9A-Fa-f]{6}$"));
    }
    
    /**
     * Get a description of this preset
     */
    public String getDescription() {
        if (colors.isEmpty()) {
            return "Empty preset";
        }
        
        if (colors.size() == 1) {
            return "Single color: " + colors.get(0);
        }
        
        return String.format("%d-color gradient: %s", colors.size(), String.join(" → ", colors));
    }
    
    /**
     * Create a copy with a new name
     */
    public PresetData copyWithName(String newName) {
        return new PresetData(newName, new java.util.ArrayList<>(colors), createdTime);
    }
    
    /**
     * Create a copy with reordered colors
     */
    public PresetData copyWithColors(List<String> newColors) {
        return new PresetData(name, new java.util.ArrayList<>(newColors), createdTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
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
