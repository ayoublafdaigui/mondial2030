# ===================================================================
# Mondial 2030 - Ticket Management System
# Multi-stage Dockerfile for JavaFX Application
# ===================================================================

# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the fat JAR
RUN mvn clean package shade:shade -DskipTests

# ===================================================================
# Stage 2: Runtime with JavaFX support
# ===================================================================
FROM eclipse-temurin:17-jdk-jammy AS runtime

# Install X11 and GUI dependencies for JavaFX
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgtk-3-0 \
    libgdk-pixbuf2.0-0 \
    libpango-1.0-0 \
    libcairo2 \
    libatk1.0-0 \
    libgl1-mesa-glx \
    libgl1-mesa-dri \
    libxtst6 \
    libxrender1 \
    libxi6 \
    libfreetype6 \
    fontconfig \
    fonts-dejavu \
    xauth \
    x11-apps \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables for JavaFX
ENV DISPLAY=:0
ENV _JAVA_OPTIONS="-Djava.awt.headless=false"

# Create app user for security
RUN groupadd -r mondial && useradd -r -g mondial mondial

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/ticket-manager-1.0-SNAPSHOT.jar app.jar

# Change ownership
RUN chown -R mondial:mondial /app

# Switch to non-root user
USER mondial

# Health check (verifies JAR is valid)
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD java -jar app.jar --version || exit 1

# Entry point with GUI support
ENTRYPOINT ["java", "-jar", "app.jar"]

# ===================================================================
# NOTES FOR X11 FORWARDING:
# 
# On Linux Host:
#   xhost +local:docker
#   docker run -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix mondial2030-app
#
# On Windows with VcXsrv:
#   1. Install VcXsrv
#   2. Run XLaunch with "Disable access control" checked
#   3. docker run -e DISPLAY=host.docker.internal:0 mondial2030-app
#
# On macOS with XQuartz:
#   1. Install XQuartz
#   2. xhost +localhost
#   3. docker run -e DISPLAY=host.docker.internal:0 mondial2030-app
# ===================================================================
