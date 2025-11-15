package br.com.frotasPro.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtDecoder jwtDecoder;
    @Autowired
    public JwtService(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public String extrairLogin(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaims().get("login").toString();
    }

    public String extrairId(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaims().get("id").toString();
    }

    public boolean ehTokenValido(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
