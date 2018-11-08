package net.exkazuu.gameaiarena.common;

public class StringUtils {
  public static int indexOfAny(String str, char[] targets) {
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      for (char target : targets) {
        if (ch == target) {
          return i;
        }
      }
    }
    return -1;
  }

  public static int lastIndexOfAny(String str, char[] targets) {
    int result = -1;
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      for (char target : targets) {
        if (ch == target) {
          result = i;
          break;
        }
      }
    }
    return result;
  }
}
