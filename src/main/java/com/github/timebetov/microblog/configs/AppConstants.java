package com.github.timebetov.microblog.configs;

public class AppConstants {

    private AppConstants() {}

    public static final String JWT_SECRET_KEY = "app.jwt.secretKey";
    public static final String JWT_EXPIRATION_IN_MS = "app.jwt.expiration";
    public static final String JWT_ISSUER = "app.jwt.issuer";
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_BLACKLISTED = "blacklisted";
}
