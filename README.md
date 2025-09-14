# HR API - Sistema de Gestión de Empleados

## 📋 Descripción
API REST para gestión de recursos humanos desarrollada con **Spring Boot** siguiendo la **Arquitectura Hexagonal (Ports & Adapters)**. Permite gestionar empleados, departamentos y roles con operaciones CRUD completas y funcionalidades específicas como manejo de vacaciones.

## 🏗️ Arquitectura Hexagonal

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

## 🚀 Características Principales

- ✅ **Arquitectura Hexagonal** - Separación clara de responsabilidades
- ✅ **Base de datos automática** - MySQL se configura automáticamente
- ✅ **Validaciones automáticas** - Jakarta Bean Validation
- ✅ **Manejo de errores centralizado** - Respuestas HTTP consistentes
- ✅ **Pruebas unitarias completas** - 100% cobertura de lógica de negocio
- ✅ **CORS habilitado** - Lista para frontends
- ✅ **Datos de ejemplo incluidos** - Para pruebas inmediatas

## 🗂️ Entidades del Dominio

### Employee (Empleado)
```java
- UUID id
- String firstName, lastName, email
- Department department
- Role role  
- LocalDate hireDate
- int vacationDays
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

## 🔗 Endpoints de la API

### 👥 Empleados
```http
POST   /api/employees              # Crear empleado
GET    /api/employees              # Listar todos
GET    /api/employees/{id}         # Obtener por ID
PUT    /api/employees/{id}         # Actualizar
DELETE /api/employees/{id}         # Eliminar
GET    /api/employees/department/{deptId}  # Por departamento
GET    /api/employees/role/{roleId}        # Por rol
PUT    /api/employees/{id}/vacation        # Tomar vacaciones
PUT    /api/employees/{id}/vacation/add    # Agregar días
```

### 🏢 Departamentos
```http
POST   /api/departments            # Crear departamento
GET    /api/departments            # Listar todos
GET    /api/departments/{id}       # Obtener por ID
DELETE /api/departments/{id}       # Eliminar
```

### 👔 Roles
```http
POST   /api/roles                  # Crear rol
GET    /api/roles                  # Listar todos
GET    /api/roles/{id}             # Obtener por ID
DELETE /api/roles/{id}             # Eliminar
```

## 🛠️ Configuración y Ejecución

### Prerrequisitos
- Java 21
- MySQL 8.0+
- Maven 3.6+

### Base de Datos
La base de datos se configura **automáticamente** al ejecutar el proyecto:
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/hrdb
spring.datasource.username=root
spring.datasource.password=root
```

### Ejecutar la Aplicación
```bash
# Clonar y navegar al proyecto
git clone <repository-url>
cd HR_API

# Ejecutar (la BD se crea automáticamente)
./mvnw spring-boot:run
```

### Ejecutar Pruebas
```bash
# Todas las pruebas
./mvnw test

# Solo pruebas unitarias
./mvnw test -Dtest="*Test"

# Solo pruebas de controladores
./mvnw test -Dtest="*ControllerTest"
```

## 📊 Ejemplos de Uso

### Crear un Empleado
```http
POST /api/employees
Content-Type: application/json

{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@company.com",
  "departmentId": "550e8400-e29b-41d4-a716-446655440001",
  "roleId": "650e8400-e29b-41d4-a716-446655440001",
  "hireDate": "2024-01-15",
  "vacationDays": 15
}
```

### Respuesta
```json
{
  "id": "750e8400-e29b-41d4-a716-446655440001",
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@company.com",
  "department": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "IT"
  },
  "role": {
    "id": "650e8400-e29b-41d4-a716-446655440001", 
    "name": "Developer"
  },
  "hireDate": "2024-01-15",
  "vacationDays": 15
}
```

## 🧪 Datos de Ejemplo Incluidos

El proyecto incluye datos de ejemplo que se insertan automáticamente:

### Departamentos
- IT, HR, Finance, Marketing

### Roles  
- Developer, Manager, Analyst, Coordinator

### Empleados
- Juan Pérez (IT - Developer)
- María García (HR - Manager)  
- Carlos López (IT - Developer)

## 🏛️ Estructura del Proyecto

```
src/main/java/com/alejandro/microservices/hr_api/
├── 🎯 domain/
│   ├── model/           # Employee, Department, Role
│   └── repository/      # Interfaces de repositorios
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
```

## 🔍 Validaciones Incluidas

- ✅ **Email válido** - Formato correcto
- ✅ **Campos obligatorios** - No nulos/vacíos
- ✅ **Días de vacaciones** - No negativos
- ✅ **Fechas de contratación** - No futuras
- ✅ **IDs de referencia** - Deben existir

## 🐛 Manejo de Errores

La API proporciona respuestas de error estructuradas:

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

## 📈 Cobertura de Pruebas

- **Modelos de Dominio**: 100%
- **Servicios de Aplicación**: 100%  
- **Adaptadores de Persistencia**: 100%
- **Controladores REST**: 100%
- **Total**: 70+ pruebas unitarias

## 🤝 Contribución

1. Fork del proyecto
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit changes (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push to branch (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.

## 🔗 Enlaces Útiles

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Jakarta Bean Validation](https://jakarta.ee/specifications/bean-validation/)
