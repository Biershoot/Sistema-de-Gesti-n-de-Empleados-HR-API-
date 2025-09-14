# 🚀 Guía de Inicio Rápido - HR API

## ⚡ Configuración en 5 Minutos

### 1. Prerrequisitos
```bash
# Verificar versiones instaladas
java -version    # Necesitas Java 21+
mysql --version  # Necesitas MySQL 8.0+
mvn -version     # Maven 3.6+ (o usar ./mvnw)
```

### 2. Clonar y Configurar
```bash
# Clonar el repositorio
git clone https://github.com/Biershoot/Sistema_Gestion_Empleados_HR_API.git
cd HR_API

# Verificar que MySQL esté ejecutándose
sudo systemctl status mysql  # Linux
brew services list mysql     # macOS
# En Windows: Services → MySQL80
```

### 3. Ejecutar la Aplicación
```bash
# La base de datos se crea automáticamente
./mvnw spring-boot:run
```

**¡Eso es todo!** La aplicación estará ejecutándose en `http://localhost:8080`

---

## 🎯 Primeras Pruebas

### Verificar que está funcionando
```bash
# Listar empleados (incluye datos de ejemplo)
curl http://localhost:8080/api/employees

# Listar departamentos
curl http://localhost:8080/api/departments

# Listar roles
curl http://localhost:8080/api/roles
```

### Crear tu primer empleado
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

---

## 📋 Datos de Ejemplo Incluidos

Al iniciar la aplicación, encontrarás estos datos precargados:

### 🏢 Departamentos
- **IT** (`550e8400-e29b-41d4-a716-446655440001`)
- **HR** (`550e8400-e29b-41d4-a716-446655440002`)
- **Finance** (`550e8400-e29b-41d4-a716-446655440003`)
- **Marketing** (`550e8400-e29b-41d4-a716-446655440004`)

### 👔 Roles
- **Developer** (`650e8400-e29b-41d4-a716-446655440001`)
- **Manager** (`650e8400-e29b-41d4-a716-446655440002`)
- **Analyst** (`650e8400-e29b-41d4-a716-446655440003`)
- **Coordinator** (`650e8400-e29b-41d4-a716-446655440004`)

### 👥 Empleados
- **Juan Pérez** - IT Developer (15 días vacaciones)
- **María García** - HR Manager (20 días vacaciones)
- **Carlos López** - IT Developer (12 días vacaciones)

---

## 🧪 Ejecutar Pruebas

```bash
# Todas las pruebas (115+ tests)
./mvnw test

# Solo pruebas rápidas (unit tests)
./mvnw test -Dtest="*Test" -DexcludedGroups="integration"

# Ver reporte en navegador
./mvnw test jacoco:report
open target/site/jacoco/index.html
```

---

## 🌐 Endpoints Principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/employees` | Listar todos los empleados |
| `POST` | `/api/employees` | Crear nuevo empleado |
| `GET` | `/api/employees/{id}` | Obtener empleado específico |
| `PUT` | `/api/employees/{id}` | Actualizar empleado |
| `DELETE` | `/api/employees/{id}` | Eliminar empleado |
| `GET` | `/api/departments` | Listar departamentos |
| `GET` | `/api/roles` | Listar roles |

---

## 🔧 Configuración Personalizada

### Cambiar Puerto
```bash
# En application.properties
server.port=8090

# O como variable de entorno
SERVER_PORT=8090 ./mvnw spring-boot:run
```

### Cambiar Base de Datos
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/mi_bd
spring.datasource.username=mi_usuario
spring.datasource.password=mi_password
```

---

## 🐛 Solución de Problemas Rápida

### "Port 8080 already in use"
```bash
# Cambiar puerto
./mvnw spring-boot:run -Dserver.port=8090
```

### "Unknown database 'hrdb'"
```bash
# Crear base de datos manualmente
./mvnw sql:execute@create-database
```

### "Access denied for user 'root'"
```bash
# Verificar MySQL
mysql -u root -p
# Cambiar credenciales en pom.xml si es necesario
```

### Aplicación no inicia
```bash
# Ver logs detallados
./mvnw spring-boot:run --debug

# Verificar Java version
java -version  # Debe ser 21+
```

---

## 📚 Siguiente Paso

Una vez que tengas la aplicación corriendo:

1. **Lee la documentación completa** en `/docs/`
2. **Explora la API** con Postman o curl
3. **Revisa el código** siguiendo la arquitectura hexagonal
4. **Ejecuta las pruebas** para entender el comportamiento
5. **Personaliza** según tus necesidades

### Documentación Adicional
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) - Arquitectura hexagonal detallada
- [`docs/API.md`](docs/API.md) - Documentación completa de endpoints
- [`docs/DATABASE.md`](docs/DATABASE.md) - Configuración de base de datos
- [`docs/TESTING.md`](docs/TESTING.md) - Estrategia de testing

---

## 🎉 ¡Felicidades!

Tienes una **API REST completa** con:
- ✅ Arquitectura hexagonal
- ✅ Base de datos automática
- ✅ Validaciones incluidas
- ✅ 115+ pruebas unitarias
- ✅ Datos de ejemplo
- ✅ Documentación completa

**¡Empieza a desarrollar!** 🚀
