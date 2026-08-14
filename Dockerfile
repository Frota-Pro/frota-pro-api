FROM eclipse-temurin:21-jdk-alpine

# Fixa o fuso horário do container/JVM em America/Recife. Sem isso, o
# relógio do container fica em UTC e rotinas como a sincronização
# automática de cargas com o WinThor (LocalDate.now()) calculam "hoje"
# errado a partir das 21h no horário do Brasil.
RUN apk add --no-cache tzdata
ENV TZ=America/Recife
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=America/Recife"

WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
