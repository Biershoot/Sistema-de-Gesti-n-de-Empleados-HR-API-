# Arquitectura Hexagonal - HR API

## 🎯 Principios de la Arquitectura Hexagonal

La **Arquitectura Hexagonal** (también conocida como Ports & Adapters) organiza el código en capas concéntricas donde el dominio de negocio está en el centro, completamente aislado de las dependencias externas.

## 📐 Estructura de Capas

```
     🌐 Infrastructure (Adaptadores)
    ┌─────────────────────────────────────┐
    │  Controllers  │  Persistence  │ Config │
    │               │               │        │
    └─────────────┬─────────────────┬────────┘
                  │                 │
     🔧 Application (Casos de Uso)  │
    ┌─────────────────────────────────┐
    │      Services & DTOs            │
    └─────────────┬───────────────────┘
                  │
     🎯 Domain (Núcleo de Negocio)
    ┌─────────────────────────────────┐
    │  Models  │  Repository Ports    │
    │          │  (Interfaces)        │
    └─────────────────────────────────┘
```

## 🎯 Domain Layer (Núcleo de Negocio)

### 📋 Models (Entidades de Dominio)
Representan los conceptos fundamentales del negocio.

```java
// Ejemplo: Employee.java
public class Employee {
    private UUID id;
    private String firstName, lastName, email;
    private Department department;
    private Role role;
    private LocalDate hireDate;
    private int vacationDays;
    
    // Métodos de negocio puros
    public void takeVacation(int days) {
        if (days > vacationDays) {
            throw new IllegalArgumentException("No tiene suficientes días");
        }
        this.vacationDays -= days;
    }
}
```

**Características importantes:**
- ✅ **Sin dependencias externas** - Solo Java puro
- ✅ **Lógica de negocio** - Reglas del dominio encapsuladas
- ✅ **Inmutabilidad** - Estado protegido
- ✅ **Validaciones de dominio** - En los métodos de negocio

### 🔌 Repository Ports (Interfaces)
Definen contratos para acceso a datos sin especificar implementación.

```java
// Ejemplo: EmployeeRepository.java
public interface EmployeeRepository {
    Employee save(Employee employee);
    Optional<Employee> findById(UUID id);
    List<Employee> findAll();
    void deleteById(UUID id);
    // Métodos específicos del dominio
    List<Employee> findByDepartmentId(UUID departmentId);
}
```

**Por qué interfaces en el dominio:**
- 🎯 **Inversión de dependencias** - Domain no depende de Infrastructure
- 🔄 **Flexibilidad** - Cambiar implementación sin afectar lógica
- 🧪 **Testabilidad** - Fácil mocking para pruebas

## 🔧 Application Layer (Casos de Uso)

### 🏢 Services (Servicios de Aplicación)
Orquestan las operaciones del dominio y coordinan entre diferentes agregados.

```java
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        // 1. Validar datos de entrada
        validateEmployeeRequest(request);
        
        // 2. Obtener dependencias del dominio
        Department department = departmentRepository.findById(request.departmentId())
            .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
        
        // 3. Crear entidad de dominio
        Employee employee = new Employee(/*...*/);
        
        // 4. Persistir usando puerto
        Employee saved = employeeRepository.save(employee);
        
        // 5. Convertir a DTO de respuesta
        return mapToResponseDTO(saved);
    }
}
```

**Responsabilidades:**
- 🎭 **Orquestación** - Coordina operaciones del dominio
- ✅ **Validación** - Datos de entrada y reglas de negocio
- 🔄 **Transacciones** - Manejo de transacciones de base de datos
- 📤 **Transformación** - Convierte entre DTOs y entidades

### 📄 DTOs (Data Transfer Objects)
Objetos para transferir datos entre capas sin exponer el dominio.

```java
// Request DTO con validaciones
public record EmployeeRequestDTO(
    @NotBlank(message = "El nombre no puede estar vacío")
    String firstName,
    
    @Email(message = "El formato del email no es válido")
    String email,
    
    @NotNull(message = "El ID del departamento no puede ser nulo")
    UUID departmentId
    // ...
) {}

// Response DTO con estructura completa
public record EmployeeResponseDTO(
    UUID id,
    String firstName,
    String lastName,
    String email,
    DepartmentDTO department,
    RoleDTO role,
    LocalDate hireDate,
    int vacationDays
) {}
```

## 🌐 Infrastructure Layer (Adaptadores)

### 🗄️ Persistence (Adaptadores de Salida)
Implementan los puertos del dominio usando tecnologías específicas.

```java
@Repository
public class EmployeeRepositoryAdapter implements EmployeeRepository {
    private final JpaEmployeeRepository jpaRepository;
    
    @Override
    public Employee save(Employee employee) {
        EmployeeEntity entity = mapToEntity(employee);
        EmployeeEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }
    
    // Mapeo entre entidades JPA y modelos de dominio
    private Employee mapToDomain(EmployeeEntity entity) {
        return new Employee(
            entity.getId(),
            entity.getFirstName(),
            // ...
        );
    }
}
```

### 🌍 Controllers (Adaptadores de Entrada)
Exponen la API REST y manejan protocolo HTTP.

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(
            @Valid @RequestBody EmployeeRequestDTO request) {
        EmployeeResponseDTO response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

## 🔄 Flujo de Datos Completo

```
1. HTTP Request → Controller (Infrastructure)
2. Controller → Service (Application)  
3. Service → Repository Port (Domain Interface)
4. Repository Adapter → JPA Entity (Infrastructure)
5. Database → JPA Entity → Domain Model
6. Domain Model → DTO → HTTP Response
```

## ✅ Beneficios de Esta Arquitectura

### 🎯 **Independencia del Dominio**
- El núcleo de negocio no conoce la base de datos, framework web, etc.
- Cambios en infraestructura no afectan las reglas de negocio

### 🧪 **Testabilidad**
- Fácil testing unitario con mocks
- Tests del dominio sin dependencias externas

### 🔄 **Flexibilidad**
- Cambiar de MySQL a PostgreSQL sin tocar lógica
- Cambiar de REST a GraphQL sin afectar servicios

### 📐 **Mantenibilidad**
- Responsabilidades claramente separadas
- Código organizado y predecible

## 🧪 Testing por Capas

### Domain Tests
```java
@Test
void shouldTakeVacationWhenSufficientDays() {
    Employee employee = new Employee(/*15 vacation days*/);
    employee.takeVacation(5);
    assertEquals(10, employee.getVacationDays());
}
```

### Application Tests  
```java
@Test
void shouldCreateEmployeeSuccessfully() {
    // Given: mocks de repositorios
    when(departmentRepository.findById(deptId)).thenReturn(Optional.of(department));
    
    // When: llamar servicio
    EmployeeResponseDTO result = employeeService.createEmployee(request);
    
    // Then: verificar resultado
    assertNotNull(result.id());
}
```

### Infrastructure Tests
```java
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Test
    void shouldReturnCreatedEmployee() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated());
    }
}
```

## 🏗️ Principios SOLID Aplicados

- **S** - Single Responsibility: Cada clase tiene una responsabilidad específica
- **O** - Open/Closed: Abierto para extensión, cerrado para modificación  
- **L** - Liskov Substitution: Los adaptadores pueden sustituir las interfaces
- **I** - Interface Segregation: Interfaces específicas por funcionalidad
- **D** - Dependency Inversion: Domain no depende de Infrastructure

Esta arquitectura hace que el código sea mantenible, testeable y adaptable a cambios futuros.
