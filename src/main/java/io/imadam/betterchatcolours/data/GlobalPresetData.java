package io.imadam.betterchatcolours.data;

import java.util.List;

public class GlobalPresetData {
  private String name;
  private List<String> colors;
  private String permission;

  public GlobalPresetData(String name, List<String> colors, String permission) {
    this.name = name;
    this.colors = colors;
    this.permission = permission;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getColors() {
    return colors;
  }

  public void setColors(List<String> colors) {
    this.colors = colors;
  }

  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public String getGradientTag() {
    if (colors == null || colors.isEmpty()) {
      return "";
    }

    if (colors.size() == 1) {
      return "<color:" + colors.get(0) + ">";
    }

    StringBuilder gradient = new StringBuilder("<gradient:");
    for (int i = 0; i < colors.size(); i++) {
      if (i > 0)
        gradient.append(":");
      gradient.append(colors.get(i));
    }
    gradient.append(">");

    return gradient.toString();
  }

  public String getClosingTag() {
    if (colors == null || colors.isEmpty()) {
      return "";
    }

    if (colors.size() == 1) {
      return "</color>";
    }

    return "</gradient>";
  }

  @Override
  public String toString() {
    return "GlobalPresetData{" +
        "name='" + name + '\'' +
        ", colors=" + colors +
        ", permission='" + permission + '\'' +
        '}';
  }
}
