package com.example.data_demo_002.common.util.Jwt;


public class UserContext {
    private static final ThreadLocal<Long> userIdTL = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameTL = new ThreadLocal<>();
    public static void setUserId(Long id) { userIdTL.set(id); }
    public static void setUsername(String name) { usernameTL.set(name); }
    public static Long getUserId() { return userIdTL.get(); }
    public static String getUsername() { return usernameTL.get(); }
    public static void clear() { userIdTL.remove(); usernameTL.remove(); }
}