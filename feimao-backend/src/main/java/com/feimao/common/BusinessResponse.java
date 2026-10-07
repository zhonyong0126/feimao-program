package com.feimao.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 租售业务接口统一响应。 */
@Data
@AllArgsConstructor
public class BusinessResponse<T> {
  private int code;
  private String msg;
  private long time;
  private T data;

  public static <T> BusinessResponse<T> success(T data) {
    return new BusinessResponse<>(1, "操作成功", System.currentTimeMillis() / 1000, data);
  }

  /** Build the exact message/data pair used by the original ThinkPHP API. */
  public static <T> BusinessResponse<T> success(String message, T data) {
    return new BusinessResponse<>(1, message, System.currentTimeMillis() / 1000, data);
  }

  public static BusinessResponse<Void> successMessage(String message) {
    return success(message, null);
  }

  public static <T> BusinessResponse<T> error(String message) {
    return new BusinessResponse<>(0, message, System.currentTimeMillis() / 1000, null);
  }

  public static <T> BusinessResponse<T> error(String message, T data) {
    return new BusinessResponse<>(0, message, System.currentTimeMillis() / 1000, data);
  }
}
