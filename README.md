# Mini Blog Application (Monolithic Architecture)
A personal blog application built with Java & Spring Boot.
Users can publish short moments (posts), follow each other and interact through a secure RESTFul API.
Designed to showcase my backend development skills using modern technologies and best practices.

## Features
- User registration & login (JWT authentication)
- Secure logout with token blacklist (Redis)
- Role-based authorization (User / Admin)
- Create & manage Moments (mini-posts)
- Follow / Unfollow other users
- Custom validation annotations
- AOP-based logging system
- JPA Auditing
- Properly exception handling
- Unit & integration tests
- Swagger Open API documentation for easy API exploration and testing
- PostgreSQL & REDIS (via Docker Compose)

## Postman Collection
You can find the full collection of all API endpoints:
👉 [Postman Collection](./Microblog.postman_collection.json)  
(Import into Postman to test all routes easily)

> Requires: Docker & Java 17

## How to Run

1. Clone the repository:
```bash
git clone https://github.com/timebetov/Spring_Monolith_REST_API_Microblog.git
cd Spring_Monolith_REST_API_Microblog
```
2. Create `.env` file in the project root with the following content:
```.dotenv
DB_HOST=postgres
DB_PORT=5432
DB_NAME=microblog
DB_USER=microroot
DB_USER_PWD=MBR@@t2025

REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=SomePassRedis
REDIS_USER=usergoi
REDIS_USER_PASSWORD=usergoiPWD
```
3. Build and start all services with Docker Compose:
```bash
docker-compose up --build
```
4. The application will be available at http://localhost:8080
5. Open Swagger UI in your browser to explore API endpoints:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)