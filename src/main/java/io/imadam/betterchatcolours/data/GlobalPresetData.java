package io.imadam.betterchatcolours.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Enhanced preset data that supports global presets, permissions, and admin
 * publishing
 */
public class GlobalPresetData extends PresetData {

  private UUID owner;
  private boolean isGlobal;
  private String permission;
  private boolean isPublished;
  private long lastModified;
  private boolean isDefault;

  public GlobalPresetData(String name, List<String> colors) {
    super(name, colors);
    this.owner = null;
    this.isGlobal = false;
    this.permission = generateDefaultPermission(name);
    this.isPublished = false;
    this.lastModified = getCreatedTime();
    this.isDefault = false;
  }

  public GlobalPresetData(String name, List<String> colors, UUID owner) {
    super(name, colors);
    this.owner = owner;
    this.isGlobal = false;
    this.permission = generateDefaultPermission(name);
    this.isPublished = false;
    this.lastModified = getCreatedTime();
    this.isDefault = false;
  }

  // New constructor for global presets with permission and default flag
  public GlobalPresetData(String name, List<String> colors, String permission, long createdTime, boolean isDefault) {
    super(name, colors, createdTime);
    this.owner = null;
    this.isGlobal = true;
    this.permission = permission != null ? permission : generateDefaultPermission(name);
    this.isPublished = true;
    this.lastModified = createdTime;
    this.isDefault = isDefault;
  }

  public GlobalPresetData(String name, List<String> colors, UUID owner, boolean isGlobal, String permission,
      boolean isPublished) {
    super(name, colors);
    this.owner = owner;
    this.isGlobal = isGlobal;
    this.permission = permission != null ? permission : generateDefaultPermission(name);
    this.isPublished = isPublished;
    this.lastModified = getCreatedTime();
    this.isDefault = false;
  }

  // Full constructor for deserialization
  public GlobalPresetData(String name, List<String> colors, long createdTime, UUID owner, boolean isGlobal,
      String permission, boolean isPublished, long lastModified) {
    super(name, colors, createdTime);
    this.owner = owner;
    this.isGlobal = isGlobal;
    this.permission = permission != null ? permission : generateDefaultPermission(name);
    this.isPublished = isPublished;
    this.lastModified = lastModified;
    this.isDefault = false;
  }

  private String generateDefaultPermission(String name) {
    return "chatcolors.preset." + name.toLowerCase().replaceAll("[^a-z0-9]", "_");
  }

  // Getters
  public UUID getOwner() {
    return owner;
  }

  public boolean isGlobal() {
    return isGlobal;
  }

  public String getPermission() {
    return permission;
  }

  public String getRequiredPermission() {
    return permission;
  }

  public boolean isPublished() {
    return isPublished;
  }

  public long getLastModified() {
    return lastModified;
  }

  public boolean isDefault() {
    return isDefault;
  }

  // Setters
  public void setOwner(UUID owner) {
    this.owner = owner;
    updateLastModified();
  }

  public void setGlobal(boolean global) {
    this.isGlobal = global;
    updateLastModified();
  }

  public void setPermission(String permission) {
    this.permission = permission != null ? permission : generateDefaultPermission(getName());
    updateLastModified();
  }

  public void setPublished(boolean published) {
    this.isPublished = published;
    updateLastModified();
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
    updateLastModified();
  }

  public void updateLastModified() {
    this.lastModified = System.currentTimeMillis();
  }

  // Utility methods
  public boolean isSingleColor() {
    return getColors() != null && getColors().size() == 1;
  }

  public boolean isOwnedBy(UUID playerId) {
    return owner != null && owner.equals(playerId);
  }

  public boolean canBeUsedBy(UUID playerId) {
    if (isOwnedBy(playerId)) {
      return true;
    }
    return isGlobal && isPublished;
  }

  public boolean isAvailableToPublic() {
    return isGlobal && isPublished;
  }

  /**
   * Create a preview string with the preset's own colors
   */
  public String createColoredPreview(String sampleText) {
    if (getColors().isEmpty()) {
      return sampleText;
    }

    return getGradientString() + sampleText + getClosingTag();
  }

  /**
   * Get preset type description
   */
  public String getTypeDescription() {
    if (isGlobal && isPublished) {
      return "Global (Published)";
    } else if (isGlobal) {
      return "Global (Unpublished)";
    } else {
      return "Personal";
    }
  }

  /**
   * Create a copy as a GlobalPresetData
   */
  public GlobalPresetData copyAsGlobal(String newName) {
    GlobalPresetData copy = new GlobalPresetData(
        newName,
        new java.util.ArrayList<>(getColors()),
        owner,
        isGlobal,
        permission,
        isPublished);
    copy.isDefault = this.isDefault;
    return copy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;
    GlobalPresetData that = (GlobalPresetData) o;
    return Objects.equals(owner, that.owner) && Objects.equals(getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), owner);
  }

  @Override
  public String toString() {
    return "GlobalPresetData{" +
        "name='" + getName() + '\'' +
        ", colors=" + getColors() +
        ", owner=" + owner +
        ", isGlobal=" + isGlobal +
        ", permission='" + permission + '\'' +
        ", isPublished=" + isPublished +
        ", isDefault=" + isDefault +
        ", createdTime=" + getCreatedTime() +
        ", lastModified=" + lastModified +
        '}';
  }
}
