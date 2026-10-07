package com.feimao.service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import org.springframework.stereotype.Service;

@Service
public class ImageCaptchaService {
  private static final String CHARACTERS = "23456789";
  private static final int[][] SEGMENTS = {
    {1, 1, 1, 1, 1, 1, 0}, {0, 1, 1, 0, 0, 0, 0}, {1, 1, 0, 1, 1, 0, 1},
    {1, 1, 1, 1, 0, 0, 1}, {0, 1, 1, 0, 0, 1, 1}, {1, 0, 1, 1, 0, 1, 1},
    {1, 0, 1, 1, 1, 1, 1}, {1, 1, 1, 0, 0, 0, 0}, {1, 1, 1, 1, 1, 1, 1},
    {1, 1, 1, 1, 0, 1, 1}
  };
  private final SecureRandom random = new SecureRandom();
  private final Map<String, Entry> entries = new ConcurrentHashMap<>();

  public byte[] create(String id) {
    if (id == null || !id.matches("[A-Za-z0-9_-]{1,64}"))
      throw new IllegalArgumentException("验证码标识错误");
    removeExpired();
    StringBuilder code = new StringBuilder(4);
    for (int i = 0; i < 4; i++) code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    entries.put(id, new Entry(code.toString(), now() + 300));
    int width = 350, height = 150;
    byte[] pixels = new byte[width * height * 3];
    for (int i = 0; i < pixels.length; i += 3) {
      pixels[i] = (byte) 245;
      pixels[i + 1] = (byte) 248;
      pixels[i + 2] = (byte) 250;
    }
    for (int i = 0; i < 12; i++)
      line(
          pixels,
          width,
          height,
          random.nextInt(width),
          random.nextInt(height),
          random.nextInt(width),
          random.nextInt(height),
          color(120, 210),
          1);
    for (int i = 0; i < code.length(); i++)
      drawDigit(pixels, width, height, code.charAt(i) - '0', 40 + i * 75, 25, color(20, 140));
    return png(pixels, width, height);
  }

  private static void drawDigit(
      byte[] pixels, int width, int height, int digit, int x, int y, int color) {
    int[][] lines = {
      {x + 8, y, x + 48, y}, {x + 52, y + 5, x + 52, y + 42},
      {x + 52, y + 52, x + 52, y + 89}, {x + 8, y + 94, x + 48, y + 94},
      {x + 4, y + 52, x + 4, y + 89}, {x + 4, y + 5, x + 4, y + 42},
      {x + 8, y + 47, x + 48, y + 47}
    };
    for (int segment = 0; segment < 7; segment++) {
      if (SEGMENTS[digit][segment] == 1) {
        int[] line = lines[segment];
        line(pixels, width, height, line[0], line[1], line[2], line[3], color, 7);
      }
    }
  }

  private static void line(
      byte[] pixels,
      int width,
      int height,
      int x0,
      int y0,
      int x1,
      int y1,
      int color,
      int thickness) {
    int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
    int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
    int error = dx + dy;
    while (true) {
      for (int ox = -thickness / 2; ox <= thickness / 2; ox++)
        for (int oy = -thickness / 2; oy <= thickness / 2; oy++)
          pixel(pixels, width, height, x0 + ox, y0 + oy, color);
      if (x0 == x1 && y0 == y1) break;
      int doubled = 2 * error;
      if (doubled >= dy) {
        error += dy;
        x0 += sx;
      }
      if (doubled <= dx) {
        error += dx;
        y0 += sy;
      }
    }
  }

  private static void pixel(byte[] pixels, int width, int height, int x, int y, int color) {
    if (x < 0 || x >= width || y < 0 || y >= height) return;
    int offset = (y * width + x) * 3;
    pixels[offset] = (byte) (color >> 16);
    pixels[offset + 1] = (byte) (color >> 8);
    pixels[offset + 2] = (byte) color;
  }

  private static byte[] png(byte[] pixels, int width, int height) {
    try {
      ByteArrayOutputStream raw = new ByteArrayOutputStream();
      for (int y = 0; y < height; y++) {
        raw.write(0);
        raw.write(pixels, y * width * 3, width * 3);
      }
      ByteArrayOutputStream compressed = new ByteArrayOutputStream();
      try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
        deflater.write(raw.toByteArray());
      }
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      DataOutputStream output = new DataOutputStream(result);
      output.write(new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10});
      ByteArrayOutputStream header = new ByteArrayOutputStream();
      DataOutputStream headerData = new DataOutputStream(header);
      headerData.writeInt(width);
      headerData.writeInt(height);
      headerData.write(new byte[] {8, 2, 0, 0, 0});
      chunk(output, "IHDR", header.toByteArray());
      chunk(output, "IDAT", compressed.toByteArray());
      chunk(output, "IEND", new byte[0]);
      return result.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException("验证码生成失败", exception);
    }
  }

  private static void chunk(DataOutputStream output, String type, byte[] data) throws IOException {
    byte[] typeBytes = type.getBytes("US-ASCII");
    output.writeInt(data.length);
    output.write(typeBytes);
    output.write(data);
    CRC32 crc = new CRC32();
    crc.update(typeBytes);
    crc.update(data);
    output.writeInt((int) crc.getValue());
  }

  public boolean verify(String id, String answer) {
    if (id == null || answer == null) return false;
    Entry entry = entries.remove(id);
    return entry != null && entry.expiresAt >= now() && entry.code.equalsIgnoreCase(answer.trim());
  }

  private int color(int minimum, int maximum) {
    int range = maximum - minimum;
    int red = minimum + random.nextInt(range);
    int green = minimum + random.nextInt(range);
    int blue = minimum + random.nextInt(range);
    return (red << 16) | (green << 8) | blue;
  }

  private void removeExpired() {
    long current = now();
    entries.entrySet().removeIf(entry -> entry.getValue().expiresAt < current);
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private static final class Entry {
    private final String code;
    private final long expiresAt;

    private Entry(String code, long expiresAt) {
      this.code = code;
      this.expiresAt = expiresAt;
    }
  }
}
