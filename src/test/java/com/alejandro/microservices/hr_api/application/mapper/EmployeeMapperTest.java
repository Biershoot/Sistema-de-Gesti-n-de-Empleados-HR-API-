package com.alejandro.microservices.hr_api.application.mapper;

import com.alejandro.microservices.hr_api.application.dto.EmployeeRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.EmployeeResponseDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para EmployeeMapper.
 *
 * Verifica la correcta conversión entre entidades Employee y DTOs,
 * incluyendo manejo de valores nulos y relaciones complejas.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
class EmployeeMapperTest {

    private EmployeeMapper employeeMapper;
    private Employee testEmployee;
    private Department testDepartment;
    private Role testRole;

    @BeforeEach
    void setUp() {
        employeeMapper = new EmployeeMapper();

        // Configurar entidades de prueba con constructores correctos
        testDepartment = new Department(
            UUID.randomUUID(),
            "IT Department"
        );

        testRole = new Role(
            UUID.randomUUID(),
            "ROLE_DEVELOPER"
        );

        testEmployee = new Employee(
            UUID.randomUUID(),
            "John",
            "Doe",
            "john.doe@company.com",
            "hashedPassword123",
            testDepartment,
            testRole,
            LocalDate.of(2020, 1, 15),
            25 // vacation days
        );
    }

    @Test
    @DisplayName("toResponseDTO debe convertir Employee a EmployeeResponseDTO correctamente")
    void toResponseDTO_ShouldConvertEmployeeCorrectly() {
        // ACT
        EmployeeResponseDTO responseDTO = employeeMapper.toResponseDTO(testEmployee);

        // ASSERT
        assertNotNull(responseDTO, "DTO de respuesta no debe ser nulo");
        assertEquals(testEmployee.getId(), responseDTO.id(), "ID debe coincidir");
        assertEquals(testEmployee.getFirstName(), responseDTO.firstName(), "FirstName debe coincidir");
        assertEquals(testEmployee.getLastName(), responseDTO.lastName(), "LastName debe coincidir");
        assertEquals(testEmployee.getEmail(), responseDTO.email(), "Email debe coincidir");
        assertEquals(testEmployee.getHireDate(), responseDTO.hireDate(), "HireDate debe coincidir");
        assertEquals(testEmployee.getVacationDays(), responseDTO.vacationDays(), "VacationDays debe coincidir");
        assertEquals(testDepartment.getName(), responseDTO.department().name(), "Department name debe coincidir");
        assertEquals(testRole.getName(), responseDTO.role().name(), "Role name debe coincidir");
    }

    @Test
    @DisplayName("toResponseDTO debe manejar Employee nulo")
    void toResponseDTO_ShouldHandleNullEmployee() {
        // ACT
        EmployeeResponseDTO responseDTO = employeeMapper.toResponseDTO(null);

        // ASSERT
        assertNull(responseDTO, "DTO debe ser nulo cuando Employee es nulo");
    }

    @Test
    @DisplayName("toResponseDTO debe manejar Employee con Department nulo")
    void toResponseDTO_ShouldHandleEmployeeWithNullDepartment() {
        // ARRANGE
        testEmployee.setDepartment(null);

        // ACT
        EmployeeResponseDTO responseDTO = employeeMapper.toResponseDTO(testEmployee);

        // ASSERT
        assertNotNull(responseDTO, "DTO no debe ser nulo");
        assertNull(responseDTO.department(), "Department debe ser nulo");
        assertEquals(testEmployee.getFirstName(), responseDTO.firstName(), "Otros campos deben estar presentes");
    }

    @Test
    @DisplayName("toResponseDTO debe manejar Employee con Role nulo")
    void toResponseDTO_ShouldHandleEmployeeWithNullRole() {
        // ARRANGE
        testEmployee.setRole(null);

        // ACT
        EmployeeResponseDTO responseDTO = employeeMapper.toResponseDTO(testEmployee);

        // ASSERT
        assertNotNull(responseDTO, "DTO no debe ser nulo");
        assertNull(responseDTO.role(), "Role debe ser nulo");
        assertEquals(testEmployee.getEmail(), responseDTO.email(), "Otros campos deben estar presentes");
    }

    @Test
    @DisplayName("toEntity debe convertir EmployeeRequestDTO a Employee correctamente")
    void toEntity_ShouldConvertRequestDTOCorrectly() {
        // ARRANGE
        EmployeeRequestDTO requestDTO = new EmployeeRequestDTO(
            "Jane",
            "Smith",
            "jane.smith@company.com",
            testDepartment.getId(),
            testRole.getId(),
            LocalDate.of(2021, 6, 1),
            25 // vacation days
        );

        // ACT
        Employee employee = employeeMapper.toEntity(requestDTO, testDepartment, testRole);

        // ASSERT
        assertNotNull(employee, "Employee no debe ser nulo");
        assertEquals(requestDTO.firstName(), employee.getFirstName(), "FirstName debe coincidir");
        assertEquals(requestDTO.lastName(), employee.getLastName(), "LastName debe coincidir");
        assertEquals(requestDTO.email(), employee.getEmail(), "Email debe coincidir");
        assertEquals(requestDTO.hireDate(), employee.getHireDate(), "HireDate debe coincidir");
        assertEquals(testDepartment, employee.getDepartment(), "Department debe coincidir");
        assertEquals(testRole, employee.getRole(), "Role debe coincidir");
        assertTrue(employee.isActive(), "Employee debe estar activo por defecto");
        assertNull(employee.getId(), "ID debe ser nulo para nueva entidad");
    }

    @Test
    @DisplayName("toEntity debe manejar EmployeeRequestDTO nulo")
    void toEntity_ShouldHandleNullRequestDTO() {
        // ACT
        Employee employee = employeeMapper.toEntity(null, testDepartment, testRole);

        // ASSERT
        assertNull(employee, "Employee debe ser nulo cuando DTO es nulo");
    }

    @Test
    @DisplayName("toResponseDTOList debe convertir lista de Employees correctamente")
    void toResponseDTOList_ShouldConvertListCorrectly() {
        // ARRANGE
        Employee employee2 = new Employee(
            UUID.randomUUID(),
            "Jane",
            "Smith",
            "jane.smith@company.com",
            "hashedPassword456",
            testDepartment,
            testRole,
            LocalDate.of(2021, 6, 1),
            20 // vacation days
        );

        List<Employee> employees = Arrays.asList(testEmployee, employee2);

        // ACT
        List<EmployeeResponseDTO> responseDTOs = employeeMapper.toResponseDTOList(employees);

        // ASSERT
        assertNotNull(responseDTOs, "Lista de DTOs no debe ser nula");
        assertEquals(2, responseDTOs.size(), "Lista debe tener 2 elementos");

        EmployeeResponseDTO dto1 = responseDTOs.get(0);
        EmployeeResponseDTO dto2 = responseDTOs.get(1);

        assertEquals(testEmployee.getFirstName(), dto1.firstName(), "Primer DTO debe coincidir");
        assertEquals(employee2.getFirstName(), dto2.firstName(), "Segundo DTO debe coincidir");
    }

    @Test
    @DisplayName("toResponseDTOList debe manejar lista vacía")
    void toResponseDTOList_ShouldHandleEmptyList() {
        // ARRANGE
        List<Employee> emptyList = Arrays.asList();

        // ACT
        List<EmployeeResponseDTO> responseDTOs = employeeMapper.toResponseDTOList(emptyList);

        // ASSERT
        assertNotNull(responseDTOs, "Lista no debe ser nula");
        assertTrue(responseDTOs.isEmpty(), "Lista debe estar vacía");
    }

    @Test
    @DisplayName("toResponseDTOList debe manejar lista nula")
    void toResponseDTOList_ShouldHandleNullList() {
        // ACT
        List<EmployeeResponseDTO> responseDTOs = employeeMapper.toResponseDTOList(null);

        // ASSERT
        assertNull(responseDTOs, "Lista debe ser nula cuando entrada es nula");
    }

    @Test
    @DisplayName("updateEntity debe actualizar Employee existente correctamente")
    void updateEntity_ShouldUpdateExistingEmployeeCorrectly() {
        // ARRANGE
        UUID originalId = testEmployee.getId();
        EmployeeRequestDTO updateDTO = new EmployeeRequestDTO(
            "UpdatedJohn",
            "UpdatedDoe",
            "updated.john@company.com",
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.of(2019, 12, 1),
            35 // vacation days
        );
        Department newDepartment = new Department(
            UUID.randomUUID(),
            "HR Department"
        );
        Role newRole = new Role(
            UUID.randomUUID(),
            "ROLE_HR_SPECIALIST"
        );

        // ACT
        employeeMapper.updateEntity(testEmployee, updateDTO, newDepartment, newRole);

        // ASSERT
        assertEquals(originalId, testEmployee.getId(), "ID debe permanecer igual");
        assertEquals("UpdatedJohn", testEmployee.getFirstName(), "FirstName debe ser actualizado");
        assertEquals("UpdatedDoe", testEmployee.getLastName(), "LastName debe ser actualizado");
        assertEquals("updated.john@company.com", testEmployee.getEmail(), "Email debe ser actualizado");
        assertEquals(LocalDate.of(2019, 12, 1), testEmployee.getHireDate(), "HireDate debe ser actualizado");
        assertEquals(newDepartment, testEmployee.getDepartment(), "Department debe ser actualizado");
        assertEquals(newRole, testEmployee.getRole(), "Role debe ser actualizado");
    }

    @Test
    @DisplayName("Mapper debe manejar caracteres especiales en nombres")
    void mapper_ShouldHandleSpecialCharactersInNames() {
        // ARRANGE
        testEmployee.setFirstName("José María");
        testEmployee.setLastName("García-López");
        testEmployee.setEmail("jose.garcia@compañía.com");

        // ACT
        EmployeeResponseDTO responseDTO = employeeMapper.toResponseDTO(testEmployee);

        // ASSERT
        assertEquals("José María", responseDTO.firstName(), "Debe manejar caracteres especiales en firstName");
        assertEquals("García-López", responseDTO.lastName(), "Debe manejar caracteres especiales en lastName");
        assertEquals("jose.garcia@compañía.com", responseDTO.email(), "Debe manejar caracteres especiales en email");
    }

    @Test
    @DisplayName("Mapper debe manejar fechas límite")
    void mapper_ShouldHandleBoundaryDates() {
        // ARRANGE
        LocalDate minDate = LocalDate.of(1900, 1, 1);
        LocalDate maxDate = LocalDate.of(2099, 12, 31);

        testEmployee.setHireDate(minDate);
        EmployeeResponseDTO responseDTO1 = employeeMapper.toResponseDTO(testEmployee);

        testEmployee.setHireDate(maxDate);
        EmployeeResponseDTO responseDTO2 = employeeMapper.toResponseDTO(testEmployee);

        // ASSERT
        assertEquals(minDate, responseDTO1.hireDate(), "Debe manejar fecha mínima");
        assertEquals(maxDate, responseDTO2.hireDate(), "Debe manejar fecha máxima");
    }

    @Test
    @DisplayName("Mapper debe ser thread-safe")
    void mapper_ShouldBeThreadSafe() throws InterruptedException {
        // ARRANGE
        final EmployeeResponseDTO[] results = new EmployeeResponseDTO[10];
        Thread[] threads = new Thread[10];

        // ACT - Mapear desde múltiples hilos
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = employeeMapper.toResponseDTO(testEmployee);
            });
            threads[i].start();
        }

        // Esperar a que todos terminen
        for (Thread thread : threads) {
            thread.join();
        }

        // ASSERT
        for (EmployeeResponseDTO result : results) {
            assertNotNull(result, "Resultado no debe ser nulo");
            assertEquals(testEmployee.getFirstName(), result.firstName(),
                "FirstName debe ser consistente en múltiples hilos");
        }
    }

    @Test
    @DisplayName("Mapper debe mantener inmutabilidad de entidades originales")
    void mapper_ShouldMaintainImmutabilityOfOriginalEntities() {
        // ARRANGE
        String originalFirstName = testEmployee.getFirstName();
        String originalLastName = testEmployee.getLastName();
        LocalDate originalHireDate = testEmployee.getHireDate();

        // ACT
        EmployeeResponseDTO responseDTO = employeeMapper.toResponseDTO(testEmployee);

        // ASSERT - Los records son inmutables, no se pueden modificar
        assertEquals(originalFirstName, testEmployee.getFirstName(),
            "FirstName original no debe cambiar");
        assertEquals(originalLastName, testEmployee.getLastName(),
            "LastName original no debe cambiar");
        assertEquals(originalHireDate, testEmployee.getHireDate(),
            "HireDate original no debe cambiar");
        
        // Verificar que el DTO mantiene los valores originales
        assertEquals(originalFirstName, responseDTO.firstName(),
            "DTO debe mantener el firstName original");
        assertEquals(originalLastName, responseDTO.lastName(),
            "DTO debe mantener el lastName original");
        assertEquals(originalHireDate, responseDTO.hireDate(),
            "DTO debe mantener el hireDate original");
    }

    @Test
    @DisplayName("Performance test para mapeo masivo")
    void mapper_ShouldBeEfficient() {
        // ARRANGE
        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Employee emp = new Employee(
                UUID.randomUUID(),
                "Employee" + i,
                "Test" + i,
                "emp" + i + "@test.com",
                "hashedPassword" + i,
                testDepartment,
                testRole,
                LocalDate.now(),
                20 // vacation days
            );
            employees.add(emp);
        }

        // ACT
        long startTime = System.currentTimeMillis();
        List<EmployeeResponseDTO> responseDTOs = employeeMapper.toResponseDTOList(employees);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // ASSERT
        assertEquals(1000, responseDTOs.size(), "Debe mapear todos los empleados");
        assertTrue(totalTime < 1000, // Menos de 1 segundo para 1000 conversiones
            "Mapeo debe ser eficiente: " + totalTime + "ms para 1000 conversiones");
    }
}
