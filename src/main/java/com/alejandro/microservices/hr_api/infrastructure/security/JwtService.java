package com.alejandro.microservices.hr_api.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para manejo de tokens JWT.
 *
 * Proporciona funcionalidades completas para:
 * - Generación de tokens JWT con claims personalizados
 * - Validación y verificación de tokens
 * - Extracción de información de tokens
 * - Manejo de expiración y renovación
 *
 * Utiliza la biblioteca JJWT para el manejo seguro de tokens JWT
 * con algoritmo de firma HMAC SHA-256.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@Service
public class JwtService {

    // Clave secreta por defecto (en producción debe estar en variables de entorno)
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    // Tiempo de expiración del token en milisegundos (24 horas por defecto)
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    /**
     * Extrae el nombre de usuario del token JWT.
     *
     * @param token Token JWT
     * @return Nombre de usuario (subject del token)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token JWT.
     *
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim deseado
     * @param <T> Tipo del claim
     * @return Valor del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT con claims adicionales.
     *
     * @param extraClaims Claims adicionales a incluir en el token
     * @param username Nombre de usuario (subject)
     * @return Token JWT generado
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        if (extraClaims == null) {
            extraClaims = new HashMap<>();
        }
        return buildToken(extraClaims, username, jwtExpiration);
    }

    /**
     * Genera un token JWT básico sin claims adicionales.
     *
     * @param username Nombre de usuario
     * @return Token JWT generado
     */
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    /**
     * Valida si un token JWT es válido para un usuario específico.
     *
     * @param token Token JWT a validar
     * @param username Nombre de usuario esperado
     * @return true si el token es válido, false en caso contrario
     */
    public boolean isTokenValid(String token, String username) {
        if (username == null) {
            throw new NullPointerException("Username cannot be null");
        }
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    /**
     * Extrae todos los claims de un token JWT.
     *
     * @param token Token JWT
     * @return Objeto Claims con toda la información del token
     */
    public Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith((javax.crypto.SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     *
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica si un token JWT ha expirado.
     *
     * @param token Token JWT
     * @return true si el token ha expirado, false en caso contrario
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Construye un token JWT con los parámetros especificados.
     *
     * @param extraClaims Claims adicionales
     * @param username Nombre de usuario (subject)
     * @param expiration Tiempo de expiración en milisegundos
     * @return Token JWT construido
     */
    private String buildToken(Map<String, Object> extraClaims, String username, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Obtiene la clave de firma para los tokens JWT.
     *
     * @return Clave de firma
     */
    private javax.crypto.SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Obtiene el tiempo de expiración configurado en segundos.
     *
     * @return Tiempo de expiración en segundos
     */
    public long getExpirationTime() {
        return jwtExpiration / 1000; // Convertir de milisegundos a segundos
    }
}
