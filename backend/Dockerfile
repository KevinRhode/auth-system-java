# syntax=docker/dockerfile:1

###############################################################################
# Build stage
###############################################################################
# For fully reproducible builds, pin the base image by digest, e.g.:
#   FROM eclipse-temurin:21-jdk-alpine@sha256:<digest>
# Get the current digest with:
#   docker buildx imagetools inspect eclipse-temurin:21-jdk-alpine
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy only the wrapper + manifest first. Dependencies then resolve into their
# own image layer and are re-downloaded only when pom.xml actually changes,
# not on every source edit.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

# Now bring in the sources and build the executable jar.
COPY src ./src
RUN ./mvnw -B -ntp clean package -DskipTests

###############################################################################
# Runtime stage
###############################################################################
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create an unprivileged user; the container never runs as root.
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the jar with non-root ownership.
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

# Let the JVM size its heap from the container's cgroup memory limit instead of
# the host's total RAM (prevents OOM-kills on small instances).
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Liveness probe against the app's own /health endpoint. busybox wget ships in
# the Alpine base, so no extra package is needed. Honors $PORT if the platform
# sets one (Render does), else defaults to 8080.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -q -O - "http://127.0.0.1:${PORT:-8080}/health" > /dev/null 2>&1 || exit 1

# `exec` replaces the shell so the JVM becomes PID 1 and receives SIGTERM
# directly, giving Spring a clean graceful shutdown.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]