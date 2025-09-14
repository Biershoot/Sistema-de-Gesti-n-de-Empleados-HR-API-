# HR API - Sistema de Gestión de Empleados

## 📋 Descripción
API REST para gestión de recursos humanos desarrollada con **Spring Boot** siguiendo la **Arquitectura Hexagonal (Ports & Adapters)**. Permite gestionar empleados, departamentos y roles con operaciones CRUD completas y funcionalidades específicas como manejo de vacaciones.

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 21
- MySQL 8.0+
- Maven 3.6+

### Configuración en 3 pasos
```bash
# 1. Clonar el repositorio
git clone https://github.com/Biershoot/Sistema_Gestion_Empleados_HR_API.git
cd HR_API

# 2. Asegurar MySQL ejecutándose (usuario: root, password: root)
# 3. Ejecutar (la BD se crea automáticamente)
./mvnw spring-boot:run
```

**¡Listo!** API disponible en `http://localhost:8080`

## 🏗️ Arquitectura Hexagonal

### Estructura de Capas
```
📦 HR API
├── 🎯 Domain (Núcleo de Negocio)
│   ├── 📋 Models: Employee, Department, Role
│   └── 🔌 Repository Interfaces (Puertos)
│
├── 🔧 Application (Casos de Uso)
│   ├── 🏢 Services: Lógica de negocio
│   └── 📄 DTOs: Transferencia de datos
│
└── 🌐 Infrastructure (Adaptadores)
    ├── 🗄️ Persistence: Implementaciones JPA
    ├── 🌍 Controllers: REST API
    └── ⚙️ Config: Configuraciones
```

### Principios Implementados
- **Domain**: Lógica de negocio pura, sin dependencias externas
- **Application**: Orquesta casos de uso, coordina el dominio
- **Infrastructure**: Adaptadores para tecnologías específicas (REST, JPA, etc.)

### Beneficios
- ✅ Lógica de negocio independiente de frameworks
- ✅ Fácil testing con mocks
- ✅ Flexibilidad para cambiar tecnologías
- ✅ Código mantenible y escalable

## 🗂️ Entidades del Dominio

### Employee (Empleado)
```java
- UUID id
- String firstName, lastName, email
- Department department
- Role role  
- LocalDate hireDate
- int vacationDays

// Métodos de negocio
- takeVacation(int days)
- addVacationDays(int days)
```

### Department (Departamento)
```java
- UUID id
- String name
```

### Role (Rol)
```java
- UUID id
- String name
```

## 🔗 API Endpoints

### 👥 Empleados
```http
POST   /api/employees                    # Crear empleado
GET    /api/employees                    # Listar todos
GET    /api/employees/{id}               # Obtener por ID
PUT    /api/employees/{id}               # Actualizar
DELETE /api/employees/{id}               # Eliminar
GET    /api/employees/department/{id}    # Por departamento
GET    /api/employees/role/{id}          # Por rol
PUT    /api/employees/{id}/vacation?days=5   # Tomar vacaciones
PUT    /api/employees/{id}/vacation/add?days=3 # Agregar días
```

### 🏢 Departamentos
```http
POST   /api/departments        # Crear departamento
GET    /api/departments        # Listar todos
GET    /api/departments/{id}   # Obtener por ID
DELETE /api/departments/{id}   # Eliminar
```

### 👔 Roles
```http
POST   /api/roles              # Crear rol
GET    /api/roles              # Listar todos
GET    /api/roles/{id}         # Obtener por ID
DELETE /api/roles/{id}         # Eliminar
```

### Códigos de Respuesta HTTP
| Código | Descripción | Uso |
|--------|-------------|-----|
| `200 OK` | Operación exitosa | GET, PUT exitosos |
| `201 Created` | Recurso creado | POST exitoso |
| `204 No Content` | Sin contenido | DELETE exitoso |
| `400 Bad Request` | Error en petición | Validación fallida |
| `404 Not Found` | Recurso no encontrado | ID inexistente |

### Ejemplos de Uso

#### Crear un Empleado
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ana",
    "lastName": "García",
    "email": "ana.garcia@company.com",
    "departmentId": "550e8400-e29b-41d4-a716-446655440001",
    "roleId": "650e8400-e29b-41d4-a716-446655440001",
    "hireDate": "2024-01-15",
    "vacationDays": 20
  }'
```

#### Listar Empleados
```bash
curl http://localhost:8080/api/employees
```

#### Tomar Vacaciones
```bash
curl -X PUT "http://localhost:8080/api/employees/{id}/vacation?days=3"
```

## 🗄️ Base de Datos

### Configuración Automática
- **MySQL** se configura automáticamente al ejecutar el proyecto
- **Base de datos**: `hrdb` (se crea automáticamente)
- **Credenciales**: root/root (configurable en `application.properties`)
- **Plugin Maven**: Ejecuta scripts SQL durante compilación

### Esquema de Tablas
```sql
-- departments
CREATE TABLE departments (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- roles  
CREATE TABLE roles (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- employees
CREATE TABLE employees (
    id CHAR(36) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    department_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    hire_date DATE NOT NULL,
    vacation_days INT NOT NULL DEFAULT 0,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Datos de Ejemplo Incluidos
**Departamentos:** IT, HR, Finance, Marketing  
**Roles:** Developer, Manager, Analyst, Coordinator  
**Empleados:** Juan Pérez (IT-Developer), María García (HR-Manager), Carlos López (IT-Developer)

### Configuración Personalizada
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/hrdb
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

## 🔍 Validaciones y Manejo de Errores

### Validaciones Automáticas
- ✅ **Email válido** - Formato correcto (@domain.com)
- ✅ **Campos obligatorios** - firstName, lastName, email no vacíos
- ✅ **Días de vacaciones** - Valor ≥ 0
- ✅ **Fechas de contratación** - No futuras
- ✅ **Referencias** - departmentId y roleId deben existir

### Respuestas de Error Estructuradas
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Error en la validación de datos",
  "fieldErrors": {
    "email": "El formato del email no es válido",
    "firstName": "El nombre no puede estar vacío"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Manejo Centralizado
- **GlobalExceptionHandler** captura y formatea errores
- **Respuestas consistentes** en formato JSON
- **Códigos HTTP apropiados** según el tipo de error

## 🧪 Testing

### Estrategia de Pruebas
```
🔺 E2E Tests (10%)
🔺🔺 Integration Tests (20%)  
🔺🔺🔺 Unit Tests (70%)
```

### Ejecución de Pruebas
```bash
# Todas las pruebas (115+ tests)
./mvnw test

# Por componente específico
./mvnw test -Dtest="*ControllerTest"        # REST Controllers
./mvnw test -Dtest="*ServiceTest"           # Servicios de aplicación
./mvnw test -Dtest="*RepositoryAdapterTest" # Adaptadores de persistencia
./mvnw test -Dtest="*.domain.*Test"         # Entidades de dominio
```

### Cobertura por Capa
- **Domain Models**: 100% - Testing de lógica de negocio pura
- **Application Services**: 100% - Testing con mocks de repositorios  
- **Infrastructure Adapters**: 100% - Testing de mapeo y validaciones
- **REST Controllers**: 100% - Testing de endpoints HTTP

### Tipos de Pruebas Implementadas
1. **Unit Tests**: Lógica de negocio aislada
2. **Integration Tests**: Interacción entre capas
3. **Controller Tests**: Endpoints REST con MockMvc
4. **Repository Tests**: Mapeo entre entidades JPA y dominio

## 🛠️ Configuración Avanzada

### Cambiar Puerto
```properties
server.port=8090
```

### Perfiles de Ambiente
```bash
# Desarrollo
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Producción  
./mvnw spring-boot:run -Dspring.profiles.active=prod

# Testing
./mvnw spring-boot:run -Dspring.profiles.active=test
```

### Variables de Entorno
```bash
export DATABASE_URL=jdbc:mysql://localhost:3306/hrdb
export DB_USERNAME=mi_usuario
export DB_PASSWORD=mi_password
./mvnw spring-boot:run
```

## 📁 Estructura del Proyecto

```
src/main/java/com/alejandro/microservices/hr_api/
├── 🎯 domain/
│   ├── model/           # Employee, Department, Role
│   └── repository/      # Interfaces de repositorios (puertos)
│
├── 🔧 application/
│   ├── service/         # EmployeeService, DepartmentService, RoleService
│   └── dto/             # DTOs de request/response
│
└── 🌐 infrastructure/
    ├── persistence/     # Adaptadores JPA
    │   ├── entity/      # Entidades JPA  
    │   ├── repository/  # Spring Data repositories
    │   └── adapter/     # Implementaciones de puertos
    ├── controller/      # REST Controllers
    └── config/          # Configuraciones Spring

src/test/java/          # Pruebas unitarias organizadas por capa
database/               # Scripts SQL de inicialización
```

## 🐛 Solución de Problemas

| Error | Solución |
|-------|----------|
| "Port 8080 already in use" | `./mvnw spring-boot:run -Dserver.port=8090` |
| "Unknown database 'hrdb'" | `./mvnw sql:execute@create-database` |
| "Access denied for user 'root'" | Verificar credenciales MySQL en `pom.xml` |
| "JVM version not supported" | Instalar Java 21+ |
| "Connection refused" | Verificar que MySQL esté ejecutándose |

### Verificar Configuración
```bash
# Comprobar Java
java -version

# Comprobar MySQL
mysql -u root -p -e "SHOW DATABASES;"

# Ver logs detallados
./mvnw spring-boot:run --debug
```

## 🚀 Características Principales

- ✅ **Arquitectura Hexagonal** - Separación clara de responsabilidades
- ✅ **Base de datos automática** - MySQL se configura automáticamente  
- ✅ **Validaciones automáticas** - Jakarta Bean Validation
- ✅ **Manejo de errores centralizado** - Respuestas HTTP consistentes
- ✅ **115+ pruebas unitarias** - 100% cobertura de lógica de negocio
- ✅ **CORS habilitado** - Listo para frontends
- ✅ **Datos de ejemplo incluidos** - Para pruebas inmediatas
- ✅ **API RESTful** - Siguiendo mejores prácticas
- ✅ **Gestión de vacaciones** - Funcionalidad específica de HR
- ✅ **Documentación completa** - Todo en un lugar

## 🤝 Contribución

1. Fork del proyecto
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit changes (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push to branch (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 🔗 Enlaces Útiles

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Jakarta Bean Validation](https://jakarta.ee/specifications/bean-validation/)
