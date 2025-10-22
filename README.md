# 🎵 Spotify Clone API

Complete REST API for a Spotify-like music streaming application with role-based access control, playlist management, and personalized recommendations.

## 📋 Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Testing Instructions](#testing-instructions)
- [API Documentation](#api-documentation)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)

## ✨ Features

### User Management
- ✅ User registration with email verification (6-digit code)
- ✅ JWT-based authentication
- ✅ Role-based access control (LISTENER, ARTIST, ADMIN)
- ✅ Account status management (PENDING, ACTIVE, BLOCKED)

### Music Management
- ✅ Upload music tracks (ARTIST only)
- ✅ Search music by title or artist (partial matching)
- ✅ Genre classification
- ✅ Listening history tracking

### Album Management
- ✅ Create and manage albums (ARTIST only)
- ✅ Associate tracks with albums
- ✅ View albums with all tracks

### Playlist Management
- ✅ Create personal playlists
- ✅ Add/remove songs from playlists
- ✅ System-generated recommendation playlists
- ✅ Protected system playlists

### Recommendations
- ✅ Artist profile with similar artists (genre-based)
- ✅ Personalized playlist generation based on listening history
- ✅ Top 3 genre-based recommendations

### Statistics
- ✅ Weekly listening statistics
- ✅ Unique listener tracking
- ✅ Automated weekly reports (scheduled)

### Admin Features
- ✅ User management (view, update, block, unblock, delete)
- ✅ Music and album moderation
- ✅ Filter users by role/status

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **Email**: JavaMailSender (Gmail SMTP)
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit 5 + Mockito
- **Build Tool**: Maven/Gradle

## 📦 Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6+ or Gradle 7+
- Gmail account for email verification (with App Password)

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd spotify-clone-api
```

### 2. Create PostgreSQL Database
```sql
CREATE DATABASE spotify_clone;
```

### 3. Configure Application Properties

Create `application.yml` or `application.properties`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/spotify_clone
    username: your_db_username
    password: your_db_password
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_gmail_app_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

jwt:
  secret: your_secret_key_here_min_256_bits
  expiration: 86400000  # 24 hours

server:
  port: 8080
```

### 4. Setup Gmail App Password

1. Go to Google Account Settings
2. Security → 2-Step Verification → App passwords
3. Generate app password for "Mail"
4. Use this password in `application.yml`

### 5. Build and Run

**Using Maven:**
```bash
mvn clean install
mvn spring-boot:run
```

**Using Gradle:**
```bash
./gradlew clean build
./gradlew bootRun
```

### 6. Create Admin User (Manual)

Connect to PostgreSQL and run:
```sql
INSERT INTO users (username, email, password, first_name, last_name, role, status, is_deleted, created_at, updated_at)
VALUES (
    'admin',
    'admin@spotify.com',
    '$2a$10$encoded_password_here',  -- Use BCrypt encoder
    'Admin',
    'User',
    'ADMIN',
    'ACTIVE',
    false,
    NOW(),
    NOW()
);
```

Or encode password in Java:
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String encodedPassword = encoder.encode("admin123");
```

## 🧪 Testing Instructions

### Automated Tests

Run unit tests:
```bash
mvn test
# or
./gradlew test
```

Check code coverage (minimum 80% for service classes):
```bash
mvn jacoco:report
# Report: target/site/jacoco/index.html
```

### Manual Testing with Swagger

1. **Access Swagger UI**
   ```
   http://localhost:8080/swagger-ui.html
   ```

2. **Test Authentication Flow**

   a) **Register as LISTENER**:
    - POST `/api/auth/register`
    - Body:
   ```json
   {
     "username": "john_listener",
     "email": "john@example.com",
     "password": "SecurePass123",
     "firstName": "John",
     "lastName": "Doe",
     "role": "LISTENER"
   }
   ```

   b) **Check Email** for 6-digit verification code

   c) **Verify Email**:
    - POST `/api/auth/verify-email`
    - Body:
   ```json
   {
     "email": "john@example.com",
     "verificationCode": "123456"
   }
   ```

   d) **Login**:
    - POST `/api/auth/login`
    - Body:
   ```json
   {
     "email": "john@example.com",
     "password": "SecurePass123"
   }
   ```
    - Copy the JWT token from response

   e) **Authorize in Swagger**:
    - Click "Authorize" button (top right)
    - Enter: `Bearer <your_token>`
    - Click "Authorize"

3. **Test LISTENER Features**

    - GET `/api/music/search?query=rock` - Search music
    - GET `/api/music/{id}` - Get music (records listening history)
    - POST `/api/playlists` - Create playlist
    - POST `/api/playlists/{playlistId}/songs/{musicId}` - Add song
    - POST `/api/recommendations/generate-playlists` - Generate recommendations

4. **Test ARTIST Features**

   a) Register as ARTIST (repeat step 2 with `"role": "ARTIST"`)

   b) Test artist endpoints:
    - POST `/api/albums` - Create album
    - POST `/api/music` - Upload music
    - GET `/api/albums/artist/{artistId}` - View your albums
    - GET `/api/recommendations/artists/{artistId}` - View similar artists

5. **Test ADMIN Features**

   Login as admin, then:
    - GET `/api/admin/users` - View all users
    - GET `/api/admin/users/role/ARTIST` - Filter by role
    - POST `/api/admin/users/{id}/block` - Block user
    - DELETE `/api/admin/music/{id}` - Delete any music
    - DELETE `/api/admin/albums/{id}` - Delete any album

### Testing Search Functionality

Test partial matching:
```
GET /api/music/search?query=fra
```
Should return songs with "afraid", "fragment", "Franco", etc.

### Testing Weekly Statistics

The scheduled job runs every Friday at 23:59. For testing:

1. Change `@Scheduled` annotation in `StatisticsService`:
   ```java
   @Scheduled(fixedRate = 60000)  // Every 1 minute
   ```

2. Add some listening history by getting music
3. Wait for schedule to run
4. Check `weekly_statistics` table

## 📖 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/api-docs
```

### Main Endpoints

| Category | Method | Endpoint | Auth Required | Role |
|----------|--------|----------|---------------|------|
| **Auth** | POST | `/auth/register` | No | - |
| | POST | `/auth/verify-email` | No | - |
| | POST | `/auth/login` | No | - |
| **Music** | POST | `/music` | Yes | ARTIST |
| | GET | `/music/{id}` | Optional | - |
| | GET | `/music/search` | No | - |
| | PUT | `/music/{id}` | Yes | ARTIST (own) |
| | DELETE | `/music/{id}` | Yes | ARTIST (own) |
| **Albums** | POST | `/albums` | Yes | ARTIST |
| | GET | `/albums/{id}` | No | - |
| | GET | `/albums/artist/{artistId}` | No | - |
| | PUT | `/albums/{id}` | Yes | ARTIST (own) |
| | DELETE | `/albums/{id}` | Yes | ARTIST (own) |
| **Playlists** | POST | `/playlists` | Yes | ALL |
| | GET | `/playlists/my` | Yes | ALL |
| | POST | `/playlists/{id}/songs/{musicId}` | Yes | Owner |
| | DELETE | `/playlists/{id}/songs/{musicId}` | Yes | Owner |
| **Recommendations** | GET | `/recommendations/artists/{id}` | No | - |
| | POST | `/recommendations/generate-playlists` | Yes | ALL |
| **Admin** | GET | `/admin/users` | Yes | ADMIN |
| | PUT | `/admin/users/{id}` | Yes | ADMIN |
| | POST | `/admin/users/{id}/block` | Yes | ADMIN |
| | DELETE | `/admin/music/{id}` | Yes | ADMIN |

## 🏗 Architecture

### Design Patterns Used
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic separation
- **DTO Pattern**: Data transfer objects for API
- **Builder Pattern**: Entity construction (Lombok)
- **Strategy Pattern**: Role-based authorization

### Security
- JWT token-based authentication
- BCrypt password encryption
- Role-based method security
- Soft delete pattern for data integrity

### Scheduling
- Weekly statistics generation (Fridays 23:59)
- Expired verification code cleanup (hourly)

## 💾 Database Schema

### Core Tables
- `users` - User accounts with roles and status
- `music` - Music tracks
- `albums` - Music albums
- `playlists` - User playlists
- `playlist_music` - Many-to-many playlist-music relation
- `listening_history` - User listening records
- `weekly_statistics` - Aggregated weekly stats

### Key Relationships
- User → Music (Artist uploads)
- User → Album (Artist creates)
- User → Playlist (User owns)
- User → ListeningHistory (User listens)
- Music → Album (Track belongs to)
- Playlist ↔ Music (Many-to-many)

## 📁 Project Structure

```
src/main/java/com/spotifyapp/
├── config/
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller/
│   ├── AuthController.java
│   ├── MusicController.java
│   ├── AlbumController.java
│   ├── PlaylistController.java
│   ├── RecommendationController.java
│   └── AdminController.java
├── dto/
│   ├── auth/
│   ├── music/
│   ├── album/
│   ├── playlist/
│   ├── user/
│   └── artist/
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── custom exceptions...
├── model/
│   ├── entity/
│   └── enums/
├── repository/
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthenticationFilter.java
│   ├── UserDetailsImpl.java
│   └── UserDetailsServiceImpl.java
├── service/
│   ├── AuthService.java
│   ├── MusicService.java
│   ├── AlbumService.java
│   ├── PlaylistService.java
│   ├── RecommendationService.java
│   ├── UserService.java
│   ├── EmailService.java
│   └── StatisticsService.java
└── SpotifyAppApplication.java

src/test/java/com/spotifyapp/service/
├── AuthServiceTest.java
├── MusicServiceTest.java
├── AlbumServiceTest.java
├── PlaylistServiceTest.java
├── RecommendationServiceTest.java
├── UserServiceTest.java
├── EmailServiceTest.java
└── StatisticsServiceTest.java
```

## 🔍 Key Features Explained

### Email Verification
- 6-digit code generated on registration
- 15-minute expiration
- Sent via Gmail SMTP
- Required before login

### Search Functionality
Partial matching example:
```sql
SELECT * FROM music 
WHERE LOWER(title) LIKE LOWER('%fra%') 
   OR LOWER(artist_name) LIKE LOWER('%fra%')
```

### Recommendation System

**Similar Artists:**
- Based on shared genres
- Sorted by number of common genres
- Limited to top 10

**Personalized Playlists:**
- Analyzes last 3 months listening history
- Identifies top 3 genres
- Creates playlist for each genre with 20 songs
- Replaces old system-generated playlists

### Statistics Tracking
- Records every music fetch as a "listen"
- Aggregates weekly (Monday-Sunday)
- Counts total listens and unique listeners
- Runs automatically via scheduled task

## 🐛 Troubleshooting

### Email Not Sending
- Check Gmail App Password is correct
- Enable 2-Step Verification in Google Account
- Verify SMTP settings in application.yml

### JWT Token Issues
- Ensure secret key is at least 256 bits
- Check token expiration time
- Verify "Bearer " prefix in Authorization header

### Database Connection Issues
- Verify PostgreSQL is running
- Check database credentials
- Ensure database exists

### Tests Failing
- Run `mvn clean test` to clear cache
- Check H2 in-memory database configuration for tests
- Verify mock setups in test classes

## 📝 Git Workflow

This project follows Git Flow:
- `main` - Production-ready code
- `develop` - Integration branch
- `feature/*` - Feature branches
- `hotfix/*` - Urgent fixes

## 📊 Code Coverage

Target: **80%+ coverage for service classes**

Generate report:
```bash
mvn clean test jacoco:report
```

View report: `target/site/jacoco/index.html`

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- Your Name - Initial work

## 🙏 Acknowledgments

- Spring Boot Documentation
- Spotify API for inspiration
- OpenAPI/Swagger for documentation