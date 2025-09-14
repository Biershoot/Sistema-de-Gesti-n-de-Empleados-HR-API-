management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

Esta configuración garantiza que la base de datos esté siempre lista para usar sin intervención manual.
# Configuración y Base de Datos - HR API

## 🗄️ Configuración de Base de Datos

### Configuración Automática
La aplicación está configurada para crear automáticamente la base de datos MySQL durante el proceso de compilación y testing.

### Archivo de Configuración
```properties
# src/main/resources/application.properties
spring.application.name=HR_API
spring.datasource.url=jdbc:mysql://localhost:3306/hrdb
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Plugin Maven SQL
El proyecto incluye el plugin `sql-maven-plugin` que:
- Crea automáticamente la base de datos `hrdb`
- Ejecuta scripts SQL de inicialización
- Se ejecuta durante la fase `process-test-resources`

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>sql-maven-plugin</artifactId>
    <version>3.0.0</version>
    <!-- Configuración para conexión y scripts -->
</plugin>
```

## 📊 Esquema de Base de Datos

### Estructura de Tablas

```sql
-- Tabla departments
CREATE TABLE departments (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Tabla roles  
CREATE TABLE roles (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Tabla employees
CREATE TABLE employees (
    id CHAR(36) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    department_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    hire_date DATE NOT NULL,
    vacation_days INT NOT NULL DEFAULT 0,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);
```

### Diagrama de Relaciones
```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│ departments │       │  employees  │       │    roles    │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │◄─────┐│ id (PK)     │┌─────►│ id (PK)     │
│ name        │      ││ first_name  ││      │ name        │
└─────────────┘      ││ last_name   ││      └─────────────┘
                     ││ email       ││
                     ││ dept_id(FK) │┘
                     ││ role_id(FK) │
                     ││ hire_date   │
                     ││ vacation_days│
                     │└─────────────┘
                     └──────────────────
```

## 🚀 Ejecución y Configuración

### Prerrequisitos
1. **MySQL Server 8.0+** ejecutándose en localhost:3306
2. **Usuario root** con contraseña `root`
3. **Java 21**
4. **Maven 3.6+**

### Configuración Automática
```bash
# La base de datos se crea automáticamente al ejecutar:
./mvnw test        # Durante las pruebas
./mvnw compile     # Durante la compilación
./mvnw spring-boot:run  # Al ejecutar la aplicación
```

### Configuración Manual (si es necesario)
```bash
# Solo crear la base de datos
./mvnw sql:execute@create-database

# Crear tablas e insertar datos
./mvnw sql:execute@setup-database
```

## 📝 Scripts SQL Incluidos

### create_database.sql
```sql
-- Script básico para crear solo la base de datos
CREATE DATABASE IF NOT EXISTS hrdb 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### setup_complete_database.sql
```sql
-- Script completo que incluye:
-- 1. Creación de tablas
-- 2. Inserción de datos de ejemplo
-- 3. Configuración de constraints

USE hrdb;

-- Crear todas las tablas con relaciones
-- Insertar datos de ejemplo predefinidos
```

## 🎯 Datos de Ejemplo Incluidos

### Departamentos Predefinidos
```sql
INSERT IGNORE INTO departments (id, name) VALUES 
('550e8400-e29b-41d4-a716-446655440001', 'IT'),
('550e8400-e29b-41d4-a716-446655440002', 'HR'),
('550e8400-e29b-41d4-a716-446655440003', 'Finance'),
('550e8400-e29b-41d4-a716-446655440004', 'Marketing');
```

### Roles Predefinidos
```sql
INSERT IGNORE INTO roles (id, name) VALUES 
('650e8400-e29b-41d4-a716-446655440001', 'Developer'),
('650e8400-e29b-41d4-a716-446655440002', 'Manager'),
('650e8400-e29b-41d4-a716-446655440003', 'Analyst'),
('650e8400-e29b-41d4-a716-446655440004', 'Coordinator');
```

### Empleados de Ejemplo
```sql
INSERT IGNORE INTO employees VALUES 
('750e8400-e29b-41d4-a716-446655440001', 'Juan', 'Pérez', 'juan.perez@company.com', 
 '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '2023-01-15', 15),
('750e8400-e29b-41d4-a716-446655440002', 'María', 'García', 'maria.garcia@company.com',
 '550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440002', '2023-02-01', 20),
('750e8400-e29b-41d4-a716-446655440003', 'Carlos', 'López', 'carlos.lopez@company.com',
 '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '2023-03-10', 12);
```

## ⚙️ Configuraciones Adicionales

### JPA/Hibernate
```properties
# Configuraciones de JPA incluidas
spring.jpa.hibernate.ddl-auto=update    # Actualiza schema automáticamente
spring.jpa.show-sql=true                # Muestra queries SQL en logs
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Profiles de Configuración

#### application-dev.properties (Desarrollo)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hrdb_dev
spring.jpa.show-sql=true
logging.level.com.alejandro.microservices.hr_api=DEBUG
```

#### application-prod.properties (Producción)
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate
```

#### application-test.properties (Testing)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

## 🔧 Personalización de Configuración

### Cambiar Credenciales de BD
Editar `pom.xml` en la sección del plugin SQL:
```xml
<configuration>
    <url>jdbc:mysql://localhost:3306</url>
    <username>tu_usuario</username>
    <password>tu_password</password>
</configuration>
```

### Cambiar Puerto de la Aplicación
```properties
server.port=8090
```

### Configurar Pool de Conexiones
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

## 🔍 Verificación de Configuración

### Comprobar Conexión a BD
```sql
-- Conectar a MySQL y verificar
mysql -u root -p
SHOW DATABASES LIKE 'hrdb';
USE hrdb;
SHOW TABLES;
SELECT COUNT(*) FROM employees;
```

### Logs de Aplicación
```bash
# Ver logs de conexión
./mvnw spring-boot:run | grep "database\|connection\|jpa"
```

## 🐛 Solución de Problemas Comunes

### Error: "Unknown database 'hrdb'"
**Solución:** 
```bash
./mvnw sql:execute@create-database
```

### Error: "Access denied for user 'root'"
**Solución:** Verificar credenciales en `pom.xml` y `application.properties`

### Error: "Table doesn't exist"
**Solución:** 
```bash
./mvnw sql:execute@setup-database
```

### Puerto 3306 en uso
**Solución:** Cambiar puerto en `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3307/hrdb
```

## 📊 Monitoreo y Métricas

### Actuator (si se incluye)
```properties
