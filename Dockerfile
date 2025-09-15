# Dockerfile para HR Management API
# Utiliza OpenJDK 21 como imagen base
FROM openjdk:21-jdk-slim

# Información del mantenedor
LABEL maintainer="Alejandro Arango Calderón <alejandro@example.com>"
LABEL description="API REST para gestión de recursos humanos"
LABEL version="1.0"

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Hacer el wrapper de Maven ejecutable
RUN chmod +x mvnw

# Descargar dependencias (para aprovechar la cache de Docker)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer puerto de la aplicación
EXPOSE 8080

# Configurar variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/HR_API-0.0.1-SNAPSHOT.jar"]

# Configurar healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
