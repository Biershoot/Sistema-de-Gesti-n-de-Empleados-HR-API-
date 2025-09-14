package com.alejandro.microservices.hr_api.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private Employee employee;
    private Department department;
    private Role role;
    private UUID employeeId;
    private UUID departmentId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        department = new Department(departmentId, "IT");
        role = new Role(roleId, "Developer");

        employee = new Employee(
            employeeId,
            "Juan",
            "Pérez",
            "juan.perez@empresa.com",
            department,
            role,
            LocalDate.of(2023, 1, 15),
            25
        );
    }

    @Test
    @DisplayName("Debería crear un empleado con todos los datos correctos")
    void shouldCreateEmployeeWithCorrectData() {
        assertAll(
            () -> assertEquals(employeeId, employee.getId()),
            () -> assertEquals("Juan", employee.getFirstName()),
            () -> assertEquals("Pérez", employee.getLastName()),
            () -> assertEquals("juan.perez@empresa.com", employee.getEmail()),
            () -> assertEquals(department, employee.getDepartment()),
            () -> assertEquals(role, employee.getRole()),
            () -> assertEquals(LocalDate.of(2023, 1, 15), employee.getHireDate()),
            () -> assertEquals(25, employee.getVacationDays())
        );
    }

    @Test
    @DisplayName("Debería tomar días de vacaciones correctamente")
    void shouldTakeVacationDaysSuccessfully() {
        // Given
        int initialVacationDays = employee.getVacationDays();
        int daysToTake = 5;

        // When
        employee.takeVacation(daysToTake);

        // Then
        assertEquals(initialVacationDays - daysToTake, employee.getVacationDays());
    }

    @Test
    @DisplayName("Debería lanzar excepción al tomar días negativos o cero")
    void shouldThrowExceptionWhenTakingNegativeOrZeroDays() {
        assertAll(
            () -> {
                IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> employee.takeVacation(0)
                );
                assertEquals("Los días deben ser positivos", exception.getMessage());
            },
            () -> {
                IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> employee.takeVacation(-5)
                );
                assertEquals("Los días deben ser positivos", exception.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Debería lanzar excepción al tomar más días de vacaciones de los disponibles")
    void shouldThrowExceptionWhenTakingMoreVacationDaysThanAvailable() {
        // Given
        int availableDays = employee.getVacationDays();
        int daysToTake = availableDays + 1;

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employee.takeVacation(daysToTake)
        );

        assertEquals("No tiene suficientes días de vacaciones", exception.getMessage());
        assertEquals(availableDays, employee.getVacationDays()); // Los días no deberían cambiar
    }

    @Test
    @DisplayName("Debería agregar días de vacaciones correctamente")
    void shouldAddVacationDaysSuccessfully() {
        // Given
        int initialVacationDays = employee.getVacationDays();
        int daysToAdd = 10;

        // When
        employee.addVacationDays(daysToAdd);

        // Then
        assertEquals(initialVacationDays + daysToAdd, employee.getVacationDays());
    }

    @Test
    @DisplayName("No debería agregar días de vacaciones si el valor es cero o negativo")
    void shouldNotAddVacationDaysWhenZeroOrNegative() {
        // Given
        int initialVacationDays = employee.getVacationDays();

        // When
        employee.addVacationDays(0);
        employee.addVacationDays(-5);

        // Then
        assertEquals(initialVacationDays, employee.getVacationDays());
    }

    @Test
    @DisplayName("Debería poder tomar exactamente todos los días de vacaciones disponibles")
    void shouldTakeExactlyAllAvailableVacationDays() {
        // Given
        int allVacationDays = employee.getVacationDays();

        // When
        employee.takeVacation(allVacationDays);

        // Then
        assertEquals(0, employee.getVacationDays());
    }

    @Test
    @DisplayName("Debería mantener la integridad después de múltiples operaciones")
    void shouldMaintainIntegrityAfterMultipleOperations() {
        // Given
        int initialDays = employee.getVacationDays();

        // When
        employee.addVacationDays(10);
        employee.takeVacation(5);
        employee.addVacationDays(3);
        employee.takeVacation(2);

        // Then
        int expectedDays = initialDays + 10 - 5 + 3 - 2; // 31
        assertEquals(expectedDays, employee.getVacationDays());
    }
}
