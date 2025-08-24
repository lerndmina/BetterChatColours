package io.imadam.betterchatcolours.data;

import java.util.UUID;

public class UserStats {
  private final UUID uuid;
  private final int presetCount;
  private final int maxPresets;
  private final boolean hasActiveGradient;
  private final boolean hasAdminForced;

  public UserStats(UUID uuid, int presetCount, int maxPresets, boolean hasActiveGradient, boolean hasAdminForced) {
    this.uuid = uuid;
    this.presetCount = presetCount;
    this.maxPresets = maxPresets;
    this.hasActiveGradient = hasActiveGradient;
    this.hasAdminForced = hasAdminForced;
  }

  public UUID getUuid() {
    return uuid;
  }

  public int getPresetCount() {
    return presetCount;
  }

  public int getMaxPresets() {
    return maxPresets;
  }

  public boolean hasActiveGradient() {
    return hasActiveGradient;
  }

  public boolean hasAdminForced() {
    return hasAdminForced;
  }

  public boolean canCreateMorePresets() {
    return presetCount < maxPresets;
  }

  public int getRemainingPresets() {
    return Math.max(0, maxPresets - presetCount);
  }

  @Override
  public String toString() {
    return "UserStats{" +
        "uuid=" + uuid +
        ", presetCount=" + presetCount +
        ", maxPresets=" + maxPresets +
        ", hasActiveGradient=" + hasActiveGradient +
        ", hasAdminForced=" + hasAdminForced +
        '}';
  }
}
