# API REST - Documentación de Endpoints

## 🌐 Base URL
```
http://localhost:8080/api
```

## 📋 Estructura de Respuestas

### ✅ Respuestas Exitosas
```json
{
  "id": "uuid",
  "campo1": "valor",
  "campo2": "valor"
}
```

### ❌ Respuestas de Error
```json
{
  "code": "ERROR_CODE",
  "message": "Descripción del error",
  "timestamp": "2024-01-15T10:30:00"
}
```

### ❌ Errores de Validación
```json
{
  "code": "VALIDATION_ERROR", 
  "message": "Error en la validación de datos",
  "fieldErrors": {
    "campo1": "Mensaje de error específico",
    "campo2": "Otro mensaje de error"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 👥 EMPLEADOS

### Crear Empleado
```http
POST /api/employees
Content-Type: application/json
```

**Request Body:**
```json
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

**Validaciones:**
- `firstName`: Obligatorio, no vacío
- `lastName`: Obligatorio, no vacío  
- `email`: Obligatorio, formato válido
- `departmentId`: Obligatorio, debe existir
- `roleId`: Obligatorio, debe existir
- `hireDate`: Opcional, no puede ser futura
- `vacationDays`: Debe ser >= 0

**Response:** `201 Created`
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

### Listar Todos los Empleados
```http
GET /api/employees
```

**Response:** `200 OK`
```json
[
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
]
```

### Obtener Empleado por ID
```http
GET /api/employees/{id}
```

**Response:** `200 OK` (mismo formato que crear empleado)

**Errores:**
- `400 Bad Request`: ID inválido
- `404 Not Found`: Empleado no encontrado

### Actualizar Empleado
```http
PUT /api/employees/{id}
Content-Type: application/json
```

**Request Body:** (mismo formato que crear empleado)

**Response:** `200 OK` (empleado actualizado)

### Eliminar Empleado
```http
DELETE /api/employees/{id}
```

**Response:** `204 No Content`

### Empleados por Departamento
```http
GET /api/employees/department/{departmentId}
```

**Response:** `200 OK` (array de empleados)

### Empleados por Rol
```http
GET /api/employees/role/{roleId}
```

**Response:** `200 OK` (array de empleados)

### Tomar Vacaciones
```http
PUT /api/employees/{id}/vacation?days=5
```

**Query Parameters:**
- `days`: Número de días a tomar (obligatorio, > 0)

**Response:** `200 OK` (empleado con días actualizados)

**Errores:**
- `400 Bad Request`: Días insuficientes o inválidos

### Agregar Días de Vacaciones
```http
PUT /api/employees/{id}/vacation/add?days=3
```

**Query Parameters:**
- `days`: Número de días a agregar (obligatorio, > 0)

**Response:** `200 OK` (empleado con días actualizados)

---

## 🏢 DEPARTAMENTOS

### Crear Departamento
```http
POST /api/departments
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Marketing"
}
```

**Validaciones:**
- `name`: Obligatorio, no vacío

**Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440004",
  "name": "Marketing"
}
```

### Listar Todos los Departamentos
```http
GET /api/departments
```

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "IT"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440002", 
    "name": "HR"
  }
]
```

### Obtener Departamento por ID
```http
GET /api/departments/{id}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "IT"
}
```

### Eliminar Departamento
```http
DELETE /api/departments/{id}
```

**Response:** `204 No Content`

**Nota:** No se puede eliminar un departamento que tenga empleados asignados.

---

## 👔 ROLES

### Crear Rol
```http
POST /api/roles
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Senior Developer"
}
```

**Validaciones:**
- `name`: Obligatorio, no vacío

**Response:** `201 Created`
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440005",
  "name": "Senior Developer"
}
```

### Listar Todos los Roles
```http
GET /api/roles
```

**Response:** `200 OK`
```json
[
  {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "name": "Developer"
  },
  {
    "id": "650e8400-e29b-41d4-a716-446655440002",
    "name": "Manager"
  }
]
```

### Obtener Rol por ID
```http
GET /api/roles/{id}
```

**Response:** `200 OK`
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440001",
  "name": "Developer"
}
```

### Eliminar Rol
```http
DELETE /api/roles/{id}
```

**Response:** `204 No Content`

**Nota:** No se puede eliminar un rol que tenga empleados asignados.

---

## 🔐 Códigos de Estado HTTP

| Código | Descripción | Cuándo se usa |
|--------|-------------|---------------|
| `200 OK` | Operación exitosa | GET, PUT exitosos |
| `201 Created` | Recurso creado | POST exitoso |
| `204 No Content` | Operación exitosa sin contenido | DELETE exitoso |
| `400 Bad Request` | Error en la petición | Validación fallida, datos inválidos |
| `404 Not Found` | Recurso no encontrado | GET/PUT/DELETE de ID inexistente |
| `500 Internal Server Error` | Error del servidor | Errores inesperados |

---

## 🧪 Ejemplos con cURL

### Crear un empleado
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ana",
    "lastName": "García",
    "email": "ana.garcia@company.com",
    "departmentId": "550e8400-e29b-41d4-a716-446655440002",
    "roleId": "650e8400-e29b-41d4-a716-446655440003",
    "hireDate": "2024-01-20",
    "vacationDays": 20
  }'
```

### Listar empleados
```bash
curl http://localhost:8080/api/employees
```

### Tomar vacaciones
```bash
curl -X PUT "http://localhost:8080/api/employees/750e8400-e29b-41d4-a716-446655440001/vacation?days=3"
```

### Crear departamento
```bash
curl -X POST http://localhost:8080/api/departments \
  -H "Content-Type: application/json" \
  -d '{"name": "Ventas"}'
```

---

## 🔍 Filtros y Búsquedas

### Empleados por Departamento
```bash
curl http://localhost:8080/api/employees/department/550e8400-e29b-41d4-a716-446655440001
```

### Empleados por Rol
```bash
curl http://localhost:8080/api/employees/role/650e8400-e29b-41d4-a716-446655440001
```

---

## 📝 Notas Importantes

1. **CORS**: La API está configurada para aceptar peticiones desde cualquier origen (`*`)

2. **UUIDs**: Todos los IDs son UUIDs válidos. Formato: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`

3. **Fechas**: Formato ISO 8601: `YYYY-MM-DD`

4. **Validaciones**: Los errores de validación incluyen detalles específicos por campo

5. **Transacciones**: Operaciones de escritura son transaccionales

6. **Datos de Ejemplo**: La API incluye datos precargados para pruebas inmediatas
