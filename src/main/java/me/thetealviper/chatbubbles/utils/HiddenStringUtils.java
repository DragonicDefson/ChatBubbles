/*     */ package me.thetealviper.chatbubbles.utils;
/*     */
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import org.bukkit.ChatColor;
/*     */
/*     */ public class HiddenStringUtils
/*     */ {
/*  10 */   private static final String SEQUENCE_HEADER = ChatColor.RESET.toString() + ChatColor.UNDERLINE + ChatColor.RESET;
/*  11 */   private static final String SEQUENCE_FOOTER = ChatColor.RESET.toString() + ChatColor.ITALIC + ChatColor.RESET;
/*     */ 
/*     */   
/*     */   public static String encodeString(String hiddenString) {
/*  15 */     return quote(stringToColors(hiddenString));
/*     */   }
/*     */   
/*     */   public static boolean hasHiddenString(String input) {
/*  19 */     if (input == null) return false;
/*  21 */     return (input.contains(SEQUENCE_HEADER) && input.contains(SEQUENCE_FOOTER));
/*     */   }
/*     */   
/*     */   public static String extractHiddenString(String input) {
/*  25 */     return colorsToString(extract(input));
/*     */   }
/*     */ 
/*     */   
/*     */   public static String replaceHiddenString(String input, String hiddenString) {
/*  30 */     if (input == null) return null;
/*     */     
/*  32 */     int start = input.indexOf(SEQUENCE_HEADER);
/*  33 */     int end = input.indexOf(SEQUENCE_FOOTER);
/*     */     
/*  35 */     if (start < 0 || end < 0) {
/*  36 */       return null;
/*     */     }
/*  39 */     return input.substring(0, start + SEQUENCE_HEADER.length()) + stringToColors(hiddenString) + input.substring(end);
/*     */   }
/*     */   private static String quote(String input) {
/*  46 */     if (input == null) return null; 
/*  47 */     return SEQUENCE_HEADER + input + SEQUENCE_FOOTER;
/*     */   }
/*     */   private static String extract(String input) {
/*  51 */     if (input == null) return null;
/*     */     
/*  53 */     int start = input.indexOf(SEQUENCE_HEADER);
/*  54 */     int end = input.indexOf(SEQUENCE_FOOTER);
/*     */     
/*  56 */     if (start < 0 || end < 0) {
/*  57 */       return null;
/*     */     }
/*     */     
/*  60 */     return input.substring(start + SEQUENCE_HEADER.length(), end);
/*     */   }
/*     */   
/*     */   private static String stringToColors(String normal) {
/*  64 */     if (normal == null) return null;
/*     */     
/*  66 */     byte[] bytes = normal.getBytes(StandardCharsets.UTF_8);
/*  67 */     char[] chars = new char[bytes.length * 4];
/*     */     
/*  69 */     for (int i = 0; i < bytes.length; i++) {
/*  70 */       char[] hex = byteToHex(bytes[i]);
/*  71 */       chars[i * 4] = '§';
/*  72 */       chars[i * 4 + 1] = hex[0];
/*  73 */       chars[i * 4 + 2] = '§';
/*  74 */       chars[i * 4 + 3] = hex[1];
/*     */     } 
/*     */     
/*  77 */     return new String(chars);
/*     */   }
/*     */   
/*     */   private static String colorsToString(String colors) {
/*  81 */     if (colors == null) return null;
/*     */     
/*  83 */     colors = colors.toLowerCase().replace("§", "");
/*     */     
/*  85 */     if (colors.length() % 2 != 0) {
/*  86 */       colors = colors.substring(0, colors.length() / 2 * 2);
/*     */     }
/*     */     
/*  89 */     char[] chars = colors.toCharArray();
/*  90 */     byte[] bytes = new byte[chars.length / 2];
/*     */     
/*  92 */     for (int i = 0; i < chars.length; i += 2) {
/*  93 */       bytes[i / 2] = hexToByte(chars[i], chars[i + 1]);
/*     */     }
/*  96 */     return new String(bytes, StandardCharsets.UTF_8);
/*     */   }
/*     */   
/*     */   private static int hexToUnsignedInt(char c) {
/* 100 */     if (c >= '0' && c <= '9')
/* 101 */       return c - 48; 
/* 102 */     if (c >= 'a' && c <= 'f') {
/* 103 */       return c - 87;
/*     */     }
/* 105 */     throw new IllegalArgumentException("Invalid hex char: out of range");
/*     */   }
/*     */ 
/*     */   
/*     */   private static char unsignedIntToHex(int i) {
/* 110 */     if (i >= 0 && i <= 9)
/* 111 */       return (char)(i + 48); 
/* 112 */     if (i >= 10 && i <= 15) {
/* 113 */       return (char)(i + 87);
/*     */     }
/* 115 */     throw new IllegalArgumentException("Invalid hex int: out of range");
/*     */   }
/*     */   private static byte hexToByte(char hex1, char hex0) {
/* 120 */     return (byte)((hexToUnsignedInt(hex1) << 4 | hexToUnsignedInt(hex0)) + -128);
/*     */   }
/*     */   
/*     */   private static char[] byteToHex(byte b) {
/* 124 */     int unsignedByte = b - -128;
/* 125 */     return new char[] { unsignedIntToHex(unsignedByte >> 4 & 0xF), unsignedIntToHex(unsignedByte & 0xF) };
/*     */   }
/*     */ }


/* Location:              C:\Users\Bradley Methorst\Desktop\chatbubbles.jar!\me\TheTealViper\chatbubble\\utils\HiddenStringUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */