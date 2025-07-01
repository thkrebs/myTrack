   package com.tmv.core.security;

   import jakarta.servlet.http.HttpServletRequest;
   import org.springframework.stereotype.Component;

   @Component
   public class JwtRequestResolver {

       public String resolveToken(HttpServletRequest request) {
           // JWT-Token aus dem "Authorization"-Header extrahieren
           String authHeader = request.getHeader("Authorization");

           if (authHeader != null && authHeader.startsWith("Bearer ")) {
               return authHeader.substring(7); // Entferne 'Bearer ' Präfix
           }

           return null; // Kein Token gefunden
       }
   }