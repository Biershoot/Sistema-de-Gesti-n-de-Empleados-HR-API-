# HR API - Configuración Automática de Base de Datos

## 🚀 Configuración Automática de Base de Datos MySQL

Tu proyecto ahora está configurado para crear automáticamente la base de datos MySQL `hrdb` durante el proceso de compilación y testing.

### ✅ ¿Qué se configuró?

1. **Plugin SQL Maven** en el `pom.xml` que:
   - Crea automáticamente la base de datos `hrdb`
   - Ejecuta scripts SQL para crear tablas
   - Inserta datos de ejemplo

2. **Scripts SQL** en la carpeta `database/`:
   - `create_database.sql` - Script básico para crear solo la BD
   - `setup_complete_database.sql` - Script completo con tablas y datos

### 🔧 Configuración de Conexión

La aplicación se conecta a MySQL con esta configuración:
- **Host**: localhost:3306
- **Base de datos**: hrdb
- **Usuario**: root
- **Contraseña**: root

### 📋 Prerrequisitos

1. **MySQL Server** debe estar instalado y ejecutándose en tu máquina
2. **Usuario root** con contraseña `root` (o modifica las credenciales en `pom.xml`)
3. **Puerto 3306** disponible

### 🚀 Cómo usar

#### Opción 1: Automático (Recomendado)
La base de datos se crea automáticamente cuando ejecutas:

```bash
# Al ejecutar pruebas
./mvnw test

# Al compilar el proyecto
./mvnw compile

# Al empaquetar
./mvnw package
```

#### Opción 2: Manual
Si quieres crear la base de datos manualmente:

```bash
# Solo crear la base de datos
./mvnw sql:execute@create-database

# Crear tablas e insertar datos
./mvnw sql:execute@setup-database
```

### 📊 Datos de Ejemplo Incluidos

La base de datos se crea con estos datos de prueba:

**Departamentos:**
- IT
- HR  
- Finance
- Marketing

**Roles:**
- Developer
- Manager
- Analyst
- Coordinator

**Empleados:**
- Juan Pérez (IT - Developer)
- María García (HR - Manager)
- Carlos López (IT - Developer)

### 🔄 Modificar Configuración

Si necesitas cambiar la configuración de la base de datos, edita:

1. **Credenciales**: `pom.xml` (sección sql-maven-plugin)
2. **Scripts SQL**: Archivos en la carpeta `database/`
3. **Conexión de la app**: `src/main/resources/application.properties`

### ⚠️ Notas Importantes

- La base de datos se recrea en cada ejecución de pruebas
- Los scripts usan `IF NOT EXISTS` e `INSERT IGNORE` para evitar errores de duplicación
- Si MySQL no está disponible, las pruebas unitarias (con mocks) seguirán funcionando

### 🐛 Solución de Problemas

**Error de conexión:**
- Verifica que MySQL esté ejecutándose
- Confirma usuario/contraseña en el `pom.xml`
- Asegúrate que el puerto 3306 esté disponible

**Error de permisos:**
- El usuario debe tener permisos para crear bases de datos
- Verifica con: `GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost';`
