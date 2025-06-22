package com.tmv.core.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtTestUtil {
    private static final String SECRET_KEY = "x3YaK87ghFSsgSDFDFGDSeHp6D4bwGGGQwertDDDLkj2o4fkas5egqLJFa7";

    public static String createMockToken(String username, String role) {
           return Jwts.builder()
                   .setSubject(username)
                   .claim("roles", role)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour expiration
                   .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Use your secret key
                   .compact();
       }
   }