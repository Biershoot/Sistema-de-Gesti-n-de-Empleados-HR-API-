package com.alejandro.microservices.hr_api.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para gestión de tokens JWT.
 *
 * Responsabilidades:
 * - Generar tokens JWT con claims personalizados
 * - Validar tokens y extraer información
 * - Verificar expiración de tokens
 *
 * Seguridad:
 * - Utiliza algoritmo HS256 para firmado
 * - Tokens válidos por 1 hora
 * - Clave secreta debe ser cambiada en producción
 */
@Service
public class JwtService {

    private static final String SECRET_KEY = "cambiarPorUnaLlaveMasLargaYSegura1234567890AbCdEfGhIjKlMnOpQrStUvWxYz";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hora

    /**
     * Obtiene la clave de firmado para los tokens JWT.
     *
     * @return Clave criptográfica para firmar tokens
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Genera un token JWT con claims personalizados.
     *
     * @param claims Claims adicionales a incluir en el token
     * @param username Nombre de usuario (subject del token)
     * @return Token JWT firmado
     */
    public String generateToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     *
     * @param token Token JWT
     * @return Nombre de usuario extraído del token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token JWT.
     *
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim deseado
     * @return Valor del claim extraído
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    /**
     * Valida si el token es válido para el usuario especificado.
     *
     * @param token Token JWT a validar
     * @param username Nombre de usuario esperado
     * @return true si el token es válido, false en caso contrario
     */
    public boolean isTokenValid(String token, String username) {
        try {
            return username.equals(extractUsername(token)) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Verifica si el token ha expirado.
     *
     * @param token Token JWT a verificar
     * @return true si el token ha expirado, false en caso contrario
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
