package com.team4.monew.util;

import java.util.List;

public class UserActivityListUtils {

  private static final int MAX_SIZE = 10;

  public static <T> void addToLimitedList(List<T> list, T item) {
    list.remove(item);
    list.add(0, item);
    if (list.size() > MAX_SIZE) {
      list.remove(list.size() - 1);
    }
  }
}
