FROM eclipse-temurin:25
RUN useradd --system --create-home appuser
EXPOSE 8080
COPY backend/target/restaurantFinder.jar restaurantFinder.jar
USER appuser
ENTRYPOINT ["java", "-jar", "restaurantFinder.jar"]
#npm run build und inhalt von frontend kopieren in static dann bauen mit maven

#docker run -d --name mongodb -p 27017:27017 mongo
#docker build -t mein-backend .
#docker run -d --name backend -p 8080:8080 -e MONGODB_URI=mongodb://host.docker.internal:27017/exampleProjVideo mein-backend
