# Imagem para publicar o sistema (ex.: no Render).
# 1ª etapa: compila o projeto com Maven e Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

# 2ª etapa: só o necessário para rodar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/quarkus-app/ ./
ENV PORT=8080
EXPOSE 8080
# Limita a memória para caber no plano grátis (512 MB)
CMD ["java", "-XX:MaxRAMPercentage=75", "-jar", "quarkus-run.jar"]
