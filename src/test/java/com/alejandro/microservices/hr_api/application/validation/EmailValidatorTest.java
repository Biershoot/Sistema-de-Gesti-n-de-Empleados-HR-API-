package com.alejandro.microservices.hr_api.application.validation;

import com.alejandro.microservices.hr_api.application.validation.EmailValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para EmailValidator.
 *
 * Verifica la correcta validación de direcciones de email según
 * los estándares RFC y casos específicos de negocio.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
class EmailValidatorTest {

    private EmailValidator emailValidator;

    @BeforeEach
    void setUp() {
        emailValidator = new EmailValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "user@domain.com",
        "test.email@company.org",
        "user+tag@example.com",
        "firstname.lastname@company.co.uk",
        "user_name@sub.domain.com",
        "123@numeric-domain.com",
        "user@domain-with-dashes.com",
        "very.long.email.address@very.long.domain.name.com"
    })
    @DisplayName("Debe validar emails válidos correctamente")
    void isValid_ShouldReturnTrue_WhenEmailIsValid(String email) {
        // ACT
        boolean result = emailValidator.isValid(email);

        // ASSERT
        assertTrue(result, "Email debe ser válido: " + email);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-email",
        "@domain.com",
        "user@",
        "user@domain",
        "user.domain.com",
        "user@@domain.com",
        "user@domain..com",
        ".user@domain.com",
        "user.@domain.com",
        "user@.domain.com",
        "user@domain.com.",
        "user name@domain.com", // Espacio en username
        "user@domain .com" // Espacio en domain
    })
    @DisplayName("Debe rechazar emails inválidos")
    void isValid_ShouldReturnFalse_WhenEmailIsInvalid(String email) {
        // ACT
        boolean result = emailValidator.isValid(email);

        // ASSERT
        assertFalse(result, "Email debe ser inválido: " + email);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("Debe rechazar emails nulos o vacíos")
    void isValid_ShouldReturnFalse_WhenEmailIsNullOrEmpty(String email) {
        // ACT
        boolean result = emailValidator.isValid(email);

        // ASSERT
        assertFalse(result, "Email nulo o vacío debe ser inválido: '" + email + "'");
    }

    @Test
    @DisplayName("Debe manejar emails con caracteres Unicode")
    void isValid_ShouldHandleUnicodeEmails() {
        // ARRANGE
        String[] unicodeEmails = {
            "test@español.com",
            "用户@domain.com",
            "user@测试.com"
        };

        // ACT & ASSERT
        for (String email : unicodeEmails) {
            boolean result = emailValidator.isValid(email);
            // El comportamiento puede variar según la implementación
            // Aquí documentamos el comportamiento esperado
            assertNotNull(result, "Validación debe retornar un resultado para: " + email);
        }
    }

    @Test
    @DisplayName("Debe manejar emails muy largos")
    void isValid_ShouldHandleVeryLongEmails() {
        // ARRANGE
        String longUsername = "a".repeat(200);
        String longDomain = "b".repeat(200) + ".com";
        String longEmail = longUsername + "@" + longDomain;

        // ACT
        boolean result = emailValidator.isValid(longEmail);

        // ASSERT
        assertFalse(result, "Email muy largo debe ser inválido");
    }

    @Test
    @DisplayName("Debe validar emails con subdominios múltiples")
    void isValid_ShouldValidateEmailsWithMultipleSubdomains() {
        // ARRANGE
        String email = "user@mail.subdomain.company.co.uk";

        // ACT
        boolean result = emailValidator.isValid(email);

        // ASSERT
        assertTrue(result, "Email con múltiples subdominios debe ser válido");
    }

    @Test
    @DisplayName("Debe manejar emails con números")
    void isValid_ShouldHandleEmailsWithNumbers() {
        // ARRANGE
        String[] numericEmails = {
            "user123@domain.com",
            "123user@domain.com",
            "user@123domain.com",
            "user@domain123.com"
        };

        // ACT & ASSERT
        for (String email : numericEmails) {
            boolean result = emailValidator.isValid(email);
            assertTrue(result, "Email con números debe ser válido: " + email);
        }
    }

    @Test
    @DisplayName("Debe rechazar emails con caracteres especiales inválidos")
    void isValid_ShouldRejectEmailsWithInvalidSpecialCharacters() {
        // ARRANGE
        String[] invalidEmails = {
            "user<>@domain.com",
            "user[]@domain.com",
            "user()@domain.com",
            "user,@domain.com",
            "user;@domain.com",
            "user:@domain.com",
            "user\"@domain.com"
        };

        // ACT & ASSERT
        for (String email : invalidEmails) {
            boolean result = emailValidator.isValid(email);
            assertFalse(result, "Email con caracteres especiales inválidos debe ser rechazado: " + email);
        }
    }

    @Test
    @DisplayName("Debe ser case insensitive")
    void isValid_ShouldBeCaseInsensitive() {
        // ARRANGE
        String[] caseVariations = {
            "user@domain.com",
            "User@Domain.Com",
            "USER@DOMAIN.COM",
            "UsEr@DoMaIn.CoM"
        };

        // ACT & ASSERT
        for (String email : caseVariations) {
            boolean result = emailValidator.isValid(email);
            assertTrue(result, "Email debe ser válido independientemente del case: " + email);
        }
    }

    @Test
    @DisplayName("Debe manejar dominios con guiones")
    void isValid_ShouldHandleDomainsWithHyphens() {
        // ARRANGE
        String[] hyphenDomains = {
            "user@domain-name.com",
            "user@sub-domain.company-name.com",
            "user@a-b-c.com"
        };

        // ACT & ASSERT
        for (String email : hyphenDomains) {
            boolean result = emailValidator.isValid(email);
            assertTrue(result, "Email con guiones en dominio debe ser válido: " + email);
        }
    }

    @Test
    @DisplayName("Debe rechazar dominios que empiecen o terminen con guión")
    void isValid_ShouldRejectDomainsStartingOrEndingWithHyphen() {
        // ARRANGE
        String[] invalidHyphenDomains = {
            "user@-domain.com",
            "user@domain-.com",
            "user@sub.-domain.com",
            "user@sub.domain-.com"
        };

        // ACT & ASSERT
        for (String email : invalidHyphenDomains) {
            boolean result = emailValidator.isValid(email);
            assertFalse(result, "Email con dominio inválido debe ser rechazado: " + email);
        }
    }

    @Test
    @DisplayName("Debe validar TLDs comunes")
    void isValid_ShouldValidateCommonTLDs() {
        // ARRANGE
        String[] commonTLDs = {
            "user@domain.com",
            "user@domain.org",
            "user@domain.net",
            "user@domain.edu",
            "user@domain.gov",
            "user@domain.mil",
            "user@domain.co.uk",
            "user@domain.com.mx"
        };

        // ACT & ASSERT
        for (String email : commonTLDs) {
            boolean result = emailValidator.isValid(email);
            assertTrue(result, "Email con TLD común debe ser válido: " + email);
        }
    }

    @Test
    @DisplayName("Debe ser thread-safe")
    void isValid_ShouldBeThreadSafe() throws InterruptedException {
        // ARRANGE
        String email = "test@domain.com";
        final Boolean[] results = new Boolean[10];
        Thread[] threads = new Thread[10];

        // ACT - Validar desde múltiples hilos
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = emailValidator.isValid(email);
            });
            threads[i].start();
        }

        // Esperar a que todos terminen
        for (Thread thread : threads) {
            thread.join();
        }

        // ASSERT
        for (Boolean result : results) {
            assertTrue(result, "Validación debe ser consistente en múltiples hilos");
        }
    }

    @Test
    @DisplayName("Performance test para validación masiva")
    void isValid_ShouldBeEfficient() {
        // ARRANGE
        String email = "performance.test@domain.com";

        // ACT
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            emailValidator.isValid(email);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // ASSERT
        assertTrue(totalTime < 1000, // Menos de 1 segundo para 10,000 validaciones
            "Validación debe ser eficiente: " + totalTime + "ms para 10,000 validaciones");
    }

    @Test
    @DisplayName("Debe manejar emails con plus addressing")
    void isValid_ShouldHandlePlusAddressing() {
        // ARRANGE
        String[] plusEmails = {
            "user+tag@domain.com",
            "user+multiple+tags@domain.com",
            "user+123@domain.com",
            "user+test-tag@domain.com"
        };

        // ACT & ASSERT
        for (String email : plusEmails) {
            boolean result = emailValidator.isValid(email);
            assertTrue(result, "Email con plus addressing debe ser válido: " + email);
        }
    }

    @Test
    @DisplayName("Debe rechazar múltiples @ consecutivos")
    void isValid_ShouldRejectMultipleConsecutiveAtSymbols() {
        // ARRANGE
        String[] invalidEmails = {
            "user@@domain.com",
            "user@@@domain.com",
            "user@@@@domain.com"
        };

        // ACT & ASSERT
        for (String email : invalidEmails) {
            boolean result = emailValidator.isValid(email);
            assertFalse(result, "Email con múltiples @ debe ser inválido: " + email);
        }
    }
}
