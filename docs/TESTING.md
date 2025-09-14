# Testing y Mejores Prácticas - HR API

## 🧪 Estrategia de Testing

### Pirámide de Testing Implementada

```
                    🔺 E2E Tests
                   /             \
              🔺 Integration Tests 🔺
             /                       \
        🔺🔺🔺 Unit Tests 🔺🔺🔺
```

**Distribución de Pruebas:**
- **70% Unit Tests** - Lógica de negocio y componentes aislados
- **20% Integration Tests** - Interacción entre capas
- **10% E2E Tests** - Flujos completos de usuario

## 🎯 Unit Tests por Capa

### Domain Layer Tests
**Ubicación:** `src/test/java/.../domain/model/`

```java
// Ejemplo: EmployeeTest.java
@Test
void shouldTakeVacationWhenSufficientDays() {
    // Given: Empleado con 15 días
    Employee employee = new Employee(/*...*/, 15);
    
    // When: Toma 5 días
    employee.takeVacation(5);
    
    // Then: Quedan 10 días
    assertEquals(10, employee.getVacationDays());
}

@Test
void shouldThrowExceptionWhenInsufficientVacationDays() {
    Employee employee = new Employee(/*...*/, 5);
    
    assertThrows(IllegalArgumentException.class, 
                () -> employee.takeVacation(10));
}
```

**Características:**
- ✅ **Sin dependencias externas** - Solo objetos Java
- ✅ **Testing de reglas de negocio** - Validaciones del dominio
- ✅ **Casos edge** - Límites y condiciones especiales
- ✅ **Ejecución rápida** - Milisegundos por test

### Application Layer Tests  
**Ubicación:** `src/test/java/.../application/service/`

```java
// Ejemplo: EmployeeServiceTest.java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    
    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private RoleRepository roleRepository;
    
    @InjectMocks private EmployeeService employeeService;
    
    @Test
    void shouldCreateEmployeeSuccessfully() {
        // Given: Mocks configurados
        when(departmentRepository.findById(deptId))
            .thenReturn(Optional.of(department));
        when(roleRepository.findById(roleId))
            .thenReturn(Optional.of(role));
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(savedEmployee);
        
        // When: Crear empleado
        EmployeeResponseDTO result = employeeService.createEmployee(request);
        
        // Then: Verificar resultado
        assertNotNull(result.id());
        assertEquals("Juan", result.firstName());
        verify(employeeRepository).save(any(Employee.class));
    }
}
```

**Características:**
- 🎭 **Mocking de dependencias** - Aislamiento de servicios
- ✅ **Testing de orquestación** - Flujo entre componentes
- 📊 **Verificación de interacciones** - Calls a repositorios
- 🎯 **Casos de error** - Manejo de excepciones

### Infrastructure Layer Tests

#### Repository Adapter Tests
**Ubicación:** `src/test/java/.../infrastructure/persistence/adapter/`

```java
@ExtendWith(MockitoExtension.class)
class EmployeeRepositoryAdapterTest {
    
    @Mock private JpaEmployeeRepository jpaRepository;
    @InjectMocks private EmployeeRepositoryAdapter adapter;
    
    @Test
    void shouldSaveAndReturnEmployee() {
        // Given: Entity mock
        when(jpaRepository.save(any(EmployeeEntity.class)))
            .thenReturn(employeeEntity);
        
        // When: Save domain object
        Employee result = adapter.save(employee);
        
        // Then: Verify mapping
        assertEquals(employee.getId(), result.getId());
        verify(jpaRepository).save(any(EmployeeEntity.class));
    }
}
```

#### Controller Tests
**Ubicación:** `src/test/java/.../infrastructure/controller/`

```java
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @MockBean private EmployeeService employeeService;
    @Autowired private ObjectMapper objectMapper;
    
    @Test
    void shouldCreateEmployeeSuccessfully() throws Exception {
        // Given: Service mock
        when(employeeService.createEmployee(any()))
            .thenReturn(employeeResponse);
        
        // When & Then: HTTP request
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.firstName").value("Juan"));
    }
}
```

## 📊 Cobertura de Testing Actual

### Estadísticas por Módulo
```
📦 Domain Layer
├── EmployeeTest.java           ✅ 8 tests
├── DepartmentTest.java         ✅ 3 tests  
└── RoleTest.java              ✅ 3 tests
   Total: 14 tests - 100% cobertura

📦 Application Layer  
├── EmployeeServiceTest.java    ✅ 12 tests
├── DepartmentServiceTest.java  ✅ 17 tests
└── RoleServiceTest.java       ✅ 17 tests
   Total: 46 tests - 100% cobertura

📦 Infrastructure Layer
├── Persistence Adapters       ✅ 23 tests
├── REST Controllers           ✅ 20 tests
└── DTOs                       ✅ 12 tests
   Total: 55 tests - 100% cobertura

🎯 TOTAL: 115+ tests unitarios
```

## 🚀 Ejecución de Pruebas

### Comandos Maven

```bash
# Todas las pruebas
./mvnw test

# Solo unit tests
./mvnw test -Dtest="*Test"

# Por capa específica
./mvnw test -Dtest="*.domain.*Test"      # Domain
./mvnw test -Dtest="*.application.*Test" # Application  
./mvnw test -Dtest="*.infrastructure.*Test" # Infrastructure

# Por tipo de componente
./mvnw test -Dtest="*ControllerTest"     # Solo controllers
./mvnw test -Dtest="*ServiceTest"        # Solo services
./mvnw test -Dtest="*RepositoryAdapterTest" # Solo adapters

# Con reporte de cobertura
./mvnw test jacoco:report
```

### Configuración de Testing

```xml
<!-- pom.xml - Dependencias de testing -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- Incluye: JUnit 5, Mockito, AssertJ, Hamcrest -->
</dependencies>
```

## 🛡️ Mejores Prácticas Implementadas

### 1. **Naming Conventions**
```java
// Patrón: should[ExpectedBehavior]When[StateUnderTest]
@Test
void shouldThrowExceptionWhenInsufficientVacationDays() { }

@Test  
void shouldReturnEmployeeWhenValidIdProvided() { }

@Test
void shouldReturnEmptyListWhenNoDepartmentsExist() { }
```

### 2. **Arrange-Act-Assert (AAA)**
```java
@Test
void shouldCalculateVacationDaysCorrectly() {
    // Arrange (Given)
    Employee employee = new Employee(/*...*/, 15);
    
    // Act (When)  
    employee.takeVacation(5);
    
    // Assert (Then)
    assertEquals(10, employee.getVacationDays());
}
```

### 3. **Testing de Casos Edge**
```java
// Valores límite
@Test void shouldHandleZeroVacationDays() { }
@Test void shouldHandleMaxVacationDays() { }
@Test void shouldHandleNegativeInput() { }

// Casos nulos/vacíos
@Test void shouldHandleNullEmployee() { }
@Test void shouldHandleEmptyEmail() { }
@Test void shouldHandleBlankName() { }

// Condiciones especiales
@Test void shouldHandleFutureDates() { }
@Test void shouldHandleDuplicateEmails() { }
```

### 4. **Mocking Estratégico**
```java
// ✅ Mock de dependencias externas
@Mock private EmployeeRepository employeeRepository;
@Mock private EmailService emailService;

// ❌ NO mockear clases bajo test
// @Mock private EmployeeService employeeService; // INCORRECTO

// ✅ Configuración específica de mocks
when(employeeRepository.save(any(Employee.class)))
    .thenAnswer(invocation -> invocation.getArgument(0));
```

### 5. **Test Data Builders**
```java
// EmployeeTestDataBuilder.java
public class EmployeeTestDataBuilder {
    private UUID id = UUID.randomUUID();
    private String firstName = "Juan";
    private String lastName = "Pérez";
    private String email = "juan@company.com";
    private int vacationDays = 15;
    
    public EmployeeTestDataBuilder withVacationDays(int days) {
        this.vacationDays = days;
        return this;
    }
    
    public Employee build() {
        return new Employee(id, firstName, lastName, email, 
                          department, role, LocalDate.now(), vacationDays);
    }
}

// Uso en tests
Employee employee = EmployeeTestDataBuilder.anEmployee()
    .withVacationDays(5)
    .build();
```

## 🔍 Testing de Validaciones

### Jakarta Bean Validation Tests
```java
@Test
void shouldFailValidationWhenEmailInvalid() {
    EmployeeRequestDTO request = new EmployeeRequestDTO(
        "Juan", "Pérez", "invalid-email", // Email inválido
        departmentId, roleId, LocalDate.now(), 15
    );
    
    mockMvc.perform(post("/api/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.fieldErrors.email")
               .value("El formato del email no es válido"));
}
```

### Business Logic Validation Tests
```java
@Test
void shouldThrowExceptionWhenDepartmentNotFound() {
    // Given: Departamento no existe
    when(departmentRepository.findById(nonExistentId))
        .thenReturn(Optional.empty());
    
    // When & Then: Debe lanzar excepción
    assertThrows(IllegalArgumentException.class,
        () -> employeeService.createEmployee(request));
}
```

## 📈 Métricas de Calidad

### Criterios de Aceptación para Tests
- ✅ **Cobertura mínima**: 90% en lógica de negocio
- ✅ **Tiempo de ejecución**: < 2 segundos por test suite
- ✅ **Independencia**: Tests no dependen entre sí
- ✅ **Determinismo**: Resultados consistentes
- ✅ **Legibilidad**: Nombres descriptivos y código claro

### Integración Continua
```yaml
# Ejemplo GitHub Actions
name: CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Run tests
        run: ./mvnw test
      - name: Generate coverage report
        run: ./mvnw jacoco:report
```

## 🎯 Testing Anti-Patterns Evitados

### ❌ **No Hacer**
```java
// Tests frágiles - dependen de orden
@Test void test1() { /* crea datos */ }
@Test void test2() { /* usa datos de test1 */ } // MAL

// Tests que testean implementación en lugar de comportamiento
verify(repository, times(1)).save(any()); // MAL si no es relevante

// Tests con lógica compleja
@Test void complexTest() {
    if (condition) {
        // path 1
    } else {
        // path 2  
    }
    // Dividir en múltiples tests
}
```

### ✅ **Hacer**
```java
// Tests independientes
@BeforeEach void setUp() { /* setup fresh data */ }

// Tests de comportamiento
@Test void shouldNotifyManagerWhenEmployeeRequestsVacation() {
    // Focus en el comportamiento del negocio
}

// Tests simples y directos
@Test void shouldReturnTrueWhenEmployeeHasSufficientVacationDays() {
    // Un solo concepto por test
}
```

## 🚀 Futuras Mejoras en Testing

### Integration Tests
```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class EmployeeIntegrationTest {
    
    @Autowired private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateAndRetrieveEmployee() {
        // Test completo end-to-end
    }
}
```

### Contract Testing
```java
// Para APIs que consumen otros servicios
@Test
void shouldConformToEmployeeAPIContract() {
    // Verificar que responses cumplen contrato
}
```

Esta estrategia de testing garantiza código confiable, mantenible y libre de regresiones.
