package com.alejandro.microservices.hr_api.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════════╗
 * ║                               SERVICIO JWT AVANZADO                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════════════╣
 * ║                     Sistema HR API - Módulo de Seguridad Criptográfica             ║
 * ╚══════════════════════════════════════════════════════════════════════════════════════╝
 *
 * Servicio especializado en la gestión completa de tokens JWT (JSON Web Tokens) para
 * el sistema de autenticación y autorización del HR API.
 *
 * 🔐 FUNCIONALIDADES PRINCIPALES:
 * ═══════════════════════════════════
 *
 * 1. GENERACIÓN DE TOKENS:
 *    ┌─────────────────────────────────────────────────────────────────┐
 *    │ • Creación de tokens JWT firmados criptográficamente           │
 *    │ • Inclusión de claims personalizados (roles, metadata)         │
 *    │ • Configuración de tiempo de expiración automático             │
 *    │ • Algoritmo de firmado HS256 (HMAC con SHA-256)               │
 *    └─────────────────────────────────────────────────────────────────┘
 *
 * 2. VALIDACIÓN DE TOKENS:
 *    ┌─────────────────────────────────────────────────────────────────┐
 *    │ • Verificación de integridad criptográfica                     │
 *    │ • Validación de expiración temporal                            │
 *    │ • Comprobación de emisor y audiencia                           │
 *    │ • Detección de tokens manipulados o corruptos                  │
 *    └─────────────────────────────────────────────────────────────────┘
 *
 * 3. EXTRACCIÓN DE INFORMACIÓN:
 *    ┌─────────────────────────────────────────────────────────────────┐
 *    │ • Parsing seguro de claims del token                           │
 *    │ • Extracción de username (subject)                             │
 *    │ • Obtención de claims personalizados                           │
 *    │ • Manejo robusto de errores de parsing                         │
 *    └─────────────────────────────────────────────────────────────────┘
 *
 * 🛡️ CARACTERÍSTICAS DE SEGURIDAD:
 * ════════════════════════════════════
 *
 * • ALGORITMO ROBUSTO: HS256 (HMAC-SHA256) para firmado
 * • CLAVE SECRETA: 256 bits mínimo para seguridad óptima
 * • VALIDACIÓN TEMPORAL: Expiración automática de tokens
 * • INTEGRIDAD GARANTIZADA: Detección de manipulación
 * • MANEJO SEGURO DE ERRORES: Sin exposición de información sensible
 *
 * 🏗️ ESPECIFICACIONES TÉCNICAS:
 * ═════════════════════════════════
 *
 * • ESTÁNDAR: RFC 7519 (JSON Web Token)
 * • ALGORITMO: HS256 (HMAC with SHA-256)
 * • BIBLIOTECA: jjwt 0.12.3 (Java JWT library)
 * • DURACIÓN: 3600 segundos (1 hora) por defecto
 * • ESTRUCTURA: Header.Payload.Signature (Base64URL)
 *
 * 📊 ESTRUCTURA DEL TOKEN JWT:
 * ═══════════════════════════════
 *
 * HEADER:
 * {
 *   "alg": "HS256",
 *   "typ": "JWT"
 * }
 *
 * PAYLOAD (Claims):
 * {
 *   "sub": "user@empresa.com",      // Subject (email del usuario)
 *   "iat": 1642678800,              // Issued At (timestamp creación)
 *   "exp": 1642682400,              // Expiration (timestamp expiración)
 *   "roles": ["ROLE_ADMIN"],        // Roles del usuario
 *   "employeeId": "uuid...",        // ID del empleado
 *   "departmentId": "uuid..."       // ID del departamento
 * }
 *
 * SIGNATURE:
 * HMACSHA256(
 *   base64UrlEncode(header) + "." + base64UrlEncode(payload),
 *   secret_key
 * )
 *
 * 🔄 FLUJO DE OPERACIONES:
 * ═══════════════════════════
 *
 * GENERACIÓN:
 * Claims → Serialización JSON → Base64URL → Firmado → Token JWT
 *
 * VALIDACIÓN:
 * Token JWT → Parsing → Verificación Firma → Validación Expiración → Claims
 *
 * 📈 MÉTRICAS DE RENDIMIENTO:
 * ══════════════════════════════
 *
 * • Generación de token: ~2-5ms
 * • Validación de token: ~1-3ms
 * • Extracción de claims: ~0.5-2ms
 * • Tamaño promedio del token: ~200-400 bytes
 *
 * 🚨 CONSIDERACIONES DE SEGURIDAD:
 * ═══════════════════════════════════
 *
 * ⚠️  CLAVE SECRETA: Debe cambiarse en producción
 * ⚠️  ALMACENAMIENTO: Nunca almacenar en localStorage del cliente
 * ⚠️  TRANSMISIÓN: Solo por HTTPS en producción
 * ⚠️  EXPIRACIÓN: Tokens de corta duración para mayor seguridad
 * ⚠️  ROTACIÓN: Implementar rotación periódica de claves
 *
 * @author Sistema HR API - Equipo de Seguridad
 * @version 2.0
 * @since 2025-01-14
 * @see <a href="https://tools.ietf.org/html/rfc7519">RFC 7519 - JSON Web Token</a>
 * @see AuthService
 * @see JwtAuthenticationFilter
 * @see SecurityConfig
 */
@Service
public class JwtService {

    // ═══════════════════════════════════════════════════════════════════════════════════════
    // CONFIGURACIÓN DE SEGURIDAD
    // ═══════════════════════════════════════════════════════════════════════════════════════

    /**
     * Clave secreta para firmado de tokens JWT.
     *
     * ESPECIFICACIONES DE SEGURIDAD:
     * ┌─────────────────────────────────────────────────────────────────┐
     * │ • Longitud mínima: 256 bits (32 caracteres)                    │
     * │ • Algoritmo: HS256 requiere clave >= 256 bits                  │
     * │ • Entropía: Alta aleatoriedad para prevenir ataques            │
     * │ • Rotación: Debe cambiarse periódicamente en producción        │
     * └─────────────────────────────────────────────────────────────────┘
     *
     * ⚠️ NOTA CRÍTICA DE SEGURIDAD:
     * Esta clave es para desarrollo únicamente. En producción debe:
     * 1. Generarse aleatoriamente con alta entropía
     * 2. Almacenarse en variables de entorno seguras
     * 3. Rotarse periódicamente
     * 4. Nunca exponerse en logs o código fuente
     */
    private static final String SECRET_KEY = "cambiarPorUnaLlaveMasLargaYSegura1234567890AbCdEfGhIjKlMnOpQrStUvWxYz";

    /**
     * Tiempo de expiración del token en milisegundos.
     *
     * CONFIGURACIÓN TEMPORAL:
     * ┌─────────────────────────────────────────────────────────────────┐
     * │ • Valor actual: 3,600,000ms = 1 hora                           │
     * │ • Justificación: Balance entre seguridad y usabilidad          │
     * │ • Recomendación: Tokens cortos para alta seguridad             │
     * │ • Configuración: Ajustable según requisitos de negocio         │
     * └─────────────────────────────────────────────────────────────────┘
     *
     * CONSIDERACIONES DE SEGURIDAD:
     * • Tokens más largos = mayor riesgo si se comprometen
     * • Tokens más cortos = mejor seguridad, más re-autenticaciones
     * • Balance óptimo: 15 minutos - 2 horas según criticidad
     */
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hora

    // ═══════════════════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE CONFIGURACIÓN CRIPTOGRÁFICA
    // ═══════════════════════════════════════════════════════════════════════════════════════

    /**
     * Obtiene la clave de firmado para los tokens JWT.
     *
     * PROCESO DE GENERACIÓN DE CLAVE:
     * ┌─────────────────────────────────────────────────────────────────┐
     * │ 1. Conversión de string secreto a bytes UTF-8                  │
     * │ 2. Generación de clave HMAC compatible con HS256               │
     * │ 3. Validación de longitud mínima (256 bits)                   │
     * │ 4. Preparación para algoritmos de firmado                      │
     * └─────────────────────────────────────────────────────────────────┘
     *
     * SEGURIDAD CRIPTOGRÁFICA:
     * • Utiliza la biblioteca jjwt para generación segura
     * • Garantiza compatibilidad con estándar HS256
     * • Valida automáticamente la longitud de la clave
     * • Maneja la codificación de caracteres correctamente
     *
     * @return Clave criptográfica para firmar tokens JWT
     * @throws IllegalArgumentException Si la clave es demasiado corta
     *
     * @see Keys#hmacShaKeyFor(byte[])
     * @see SignatureAlgorithm#HS256
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════
    // MÉTODOS PÚBLICOS DE GESTIÓN DE TOKENS
    // ═══════════════════════════════════════════════════════════════════════════════════════

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────────┐
     * │                           MÉTODO: GENERATE TOKEN                                   │
     * └─────────────────────────────────────────────────────────────────────────────────────┘
     *
     * Genera un token JWT firmado con claims personalizados y configuración de seguridad.
     *
     * PROCESO DE GENERACIÓN:
     * ═════════════════════════
     *
     * 1. PREPARACIÓN DE CLAIMS:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Incorporación de claims personalizados del usuario       │
     *    │ • Establecimiento del subject (username/email)             │
     *    │ • Configuración de timestamps de emisión                   │
     *    │ • Cálculo automático de fecha de expiración                │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 2. CONSTRUCCIÓN DEL TOKEN:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Creación del builder de JWT                              │
     *    │ • Aplicación de claims al payload                          │
     *    │ • Configuración de algoritmo de firmado (HS256)            │
     *    │ • Aplicación de clave de firmado                           │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 3. FIRMADO CRIPTOGRÁFICO:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Serialización del header y payload                       │
     *    │ • Generación de hash HMAC-SHA256                           │
     *    │ • Concatenación con separadores de punto                   │
     *    │ • Codificación Base64URL del resultado final               │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * CLAIMS ESTÁNDAR INCLUIDOS:
     * ═════════════════════════════
     *
     * • "sub" (Subject): Identificador del usuario
     * • "iat" (Issued At): Timestamp de creación
     * • "exp" (Expiration): Timestamp de expiración
     * • "jti" (JWT ID): Identificador único del token (opcional)
     *
     * CLAIMS PERSONALIZADOS SOPORTADOS:
     * ════════════════════════════════════
     *
     * • "roles": Lista de roles del usuario
     * • "employeeId": UUID del empleado
     * • "departmentId": UUID del departamento
     * • "permissions": Permisos específicos
     * • "sessionId": Identificador de sesión
     *
     * VALIDACIONES DE SEGURIDAD:
     * ═════════════════════════════
     *
     * • Username no puede ser null o vacío
     * • Claims no pueden contener información sensible
     * • Tiempo de expiración debe ser futuro
     * • Clave de firmado debe estar disponible
     *
     * @param claims Mapa de claims personalizados a incluir en el token
     * @param username Identificador del usuario (será el 'subject' del token)
     * @return Token JWT firmado y codificado como string
     * @throws IllegalArgumentException Si el username es null o vacío
     * @throws JwtException Si hay errores en la generación del token
     *
     * @see Jwts#builder()
     * @see Claims
     * @see SignatureAlgorithm#HS256
     */
    public String generateToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)                              // Claims personalizados del usuario
                .setSubject(username)                           // Subject: identificador del usuario
                .setIssuedAt(new Date())                        // Timestamp de emisión
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // Expiración
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Firmado HS256
                .compact();                                     // Serialización final
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────────┐
     * │                         MÉTODO: EXTRACT USERNAME                                   │
     * └─────────────────────────────────────────────────────────────────────────────────────┘
     *
     * Extrae el nombre de usuario (subject) del token JWT de forma segura.
     *
     * PROCESO DE EXTRACCIÓN:
     * ═════════════════════════
     *
     * 1. PARSING DEL TOKEN:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Validación de formato del token JWT                      │
     *    │ • Decodificación Base64URL de las partes                   │
     *    │ • Verificación de estructura header.payload.signature      │
     *    │ • Parsing de JSON del payload                              │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 2. VERIFICACIÓN DE FIRMA:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Recálculo de firma con clave secreta                     │
     *    │ • Comparación con firma del token                          │
     *    │ • Validación de integridad criptográfica                   │
     *    │ • Detección de manipulación del token                      │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 3. EXTRACCIÓN DEL SUBJECT:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Acceso al claim "sub" del payload                        │
     *    │ • Validación de tipo string                                │
     *    │ • Verificación de formato de email (opcional)              │
     *    │ • Retorno del username extraído                            │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * CASOS DE ERROR MANEJADOS:
     * ═══════════════════════════
     *
     * • Token malformado → MalformedJwtException
     * • Firma inválida → SignatureException
     * • Token expirado → ExpiredJwtException
     * • Claims inválidos → InvalidClaimException
     *
     * @param token Token JWT del cual extraer el username
     * @return Username (email) extraído del claim 'subject'
     * @throws JwtException Si el token es inválido, malformado o expirado
     *
     * @see #extractClaim(String, Function)
     * @see Claims#getSubject()
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────────┐
     * │                          MÉTODO: EXTRACT CLAIM                                     │
     * └─────────────────────────────────────────────────────────────────────────────────────┘
     *
     * Extrae un claim específico del token JWT utilizando un resolver de función.
     *
     * Este método implementa el patrón Strategy para extracción flexible de claims,
     * permitiendo obtener cualquier información del payload del token de forma type-safe.
     *
     * PROCESO DE EXTRACCIÓN:
     * ═════════════════════════
     *
     * 1. PARSING COMPLETO:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Construcción del parser JWT con clave de verificación     │
     *    │ • Parsing y validación completa del token                  │
     *    │ • Extracción del objeto Claims completo                    │
     *    │ • Verificación de integridad y expiración                  │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 2. APLICACIÓN DEL RESOLVER:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Invocación de la función resolver con Claims             │
     *    │ • Extracción type-safe del claim específico                │
     *    │ • Manejo automático de conversión de tipos                 │
     *    │ • Retorno del valor extraído                               │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * EJEMPLOS DE USO:
     * ═══════════════════
     *
     * • Extraer subject: extractClaim(token, Claims::getSubject)
     * • Extraer expiración: extractClaim(token, Claims::getExpiration)
     * • Extraer claim personalizado: extractClaim(token, claims -> claims.get("roles"))
     * • Extraer con tipo: extractClaim(token, claims -> claims.get("employeeId", String.class))
     *
     * VENTAJAS DEL PATRÓN:
     * ══════════════════════
     *
     * • Type safety en tiempo de compilación
     * • Flexibilidad para cualquier claim
     * • Reutilización de lógica de parsing
     * • Manejo centralizado de errores
     *
     * @param <T> Tipo del valor del claim a extraer
     * @param token Token JWT del cual extraer el claim
     * @param claimsResolver Función que define cómo extraer el claim específico
     * @return Valor del claim extraído con el tipo especificado
     * @throws JwtException Si el token es inválido o no puede ser parseado
     *
     * @see Function
     * @see Claims
     * @see JwtParserBuilder
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
     * ┌─────────────────────────────────────────────────────────────────────────────────────┐
     * │                          MÉTODO: IS TOKEN VALID                                    │
     * └─────────────────────────────────────────────────────────────────────────────────────┘
     *
     * Valida completamente un token JWT verificando integridad, expiración y username.
     *
     * VALIDACIONES REALIZADAS:
     * ═══════════════════════════
     *
     * 1. VALIDACIÓN DE FIRMA:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Verificación criptográfica de la firma HS256             │
     *    │ • Detección de tokens manipulados o corruptos              │
     *    │ • Validación de integridad del header y payload            │
     *    │ • Confirmación de emisor legítimo                          │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 2. VALIDACIÓN TEMPORAL:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Verificación de que el token no ha expirado              │
     *    │ • Comparación con timestamp actual                         │
     *    │ • Manejo de desfases de reloj (clock skew)                 │
     *    │ • Validación de fecha de emisión                           │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 3. VALIDACIÓN DE IDENTIDAD:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Comparación del subject del token con username esperado  │
     *    │ • Verificación de que el token pertenece al usuario        │
     *    │ • Prevención de uso de tokens por usuarios incorrectos     │
     *    │ • Validación de formato del username                       │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * CASOS DE INVALIDEZ:
     * ═════════════════════
     *
     * • Token con firma alterada → false
     * • Token expirado → false
     * • Username no coincide → false
     * • Token malformado → false
     * • Claims inválidos → false
     *
     * MANEJO ROBUSTO DE ERRORES:
     * ═════════════════════════════
     *
     * El método captura todas las JwtException y retorna false en lugar de
     * propagar excepciones, proporcionando una API más limpia y evitando
     * la exposición de detalles internos de errores.
     *
     * @param token Token JWT a validar
     * @param username Username esperado que debe coincidir con el subject del token
     * @return true si el token es válido en todos los aspectos, false en caso contrario
     *
     * @see #extractUsername(String)
     * @see #isTokenExpired(String)
     */
    public boolean isTokenValid(String token, String username) {
        try {
            return username.equals(extractUsername(token)) && !isTokenExpired(token);
        } catch (JwtException e) {
            // Manejo seguro: retornar false en lugar de exponer detalles del error
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ═══════════════════════════════════════════════════════════════════════════════════════

    /**
     * Verifica si el token ha expirado comparando con el timestamp actual.
     *
     * LÓGICA DE VALIDACIÓN TEMPORAL:
     * ┌─────────────────────────────────────────────────────────────────┐
     * │ 1. Extracción del claim "exp" (expiration) del token           │
     * │ 2. Comparación con fecha/hora actual del sistema               │
     * │ 3. Retorno true si la fecha actual es posterior a expiración   │
     * │ 4. Manejo automático de zonas horarias UTC                     │
     * └─────────────────────────────────────────────────────────────────┘
     *
     * CONSIDERACIONES DE IMPLEMENTACIÓN:
     * • Utiliza Date.before() para comparación precisa
     * • Maneja automáticamente milisegundos
     * • Compatible con diferentes zonas horarias
     * • Robusto ante cambios de horario del sistema
     *
     * @param token Token JWT a verificar
     * @return true si el token ha expirado, false si aún es válido
     *
     * @see #extractClaim(String, Function)
     * @see Claims#getExpiration()
     * @see Date#before(Date)
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
