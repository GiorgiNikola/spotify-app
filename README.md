# Spotify Clone Application

## 📝 აღწერა
მუსიკის სტრიმინგის პლატფორმა იუზერების ავთენტიფიკაციით, playlist-ების მართვით და რეკომენდაციებით.

## 🛠 ტექნოლოგიები
- **Java 17**
- **Spring Boot 3.2.0**
- **PostgreSQL 14+**
- **JWT Authentication**
- **JavaMailSender**
- **JUnit 5 & Mockito**
- **Maven**

## 📋 წინა პირობები
- JDK 17+
- PostgreSQL 14+
- Maven 3.8+
- Gmail ანგარიში (email verification-სთვის)

## 🚀 დაინსტალირების ინსტრუქცია

### 1. Database Setup
```sql
-- PostgreSQL-ში შექმენი database
CREATE DATABASE spotify_clone;

-- შექმენი user
CREATE USER spotify_user WITH PASSWORD 'spotify_password';

-- მიანიჭე უფლებები
GRANT ALL PRIVILEGES ON DATABASE spotify_clone TO spotify_user;
```

### 2. Gmail App Password Setup
1. გადადი [Google Account Security](https://myaccount.google.com/security)
2. ჩართე 2-Step Verification
3. გადადი "App passwords" სექციაში
4. შექმენი ახალი App Password "Mail" კატეგორიისთვის
5. დააკოპირე გენერირებული პაროლი

### 3. Application Configuration
შექმენი `src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/spotify_clone
spring.datasource.username=spotify_user
spring.datasource.password=spotify_password

# Email (შეცვალე შენი მონაცემებით)
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password_here

# JWT (გენერირე შენი secure key)
jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
jwt.expiration=86400000
```

### 4. Build & Run
```bash
# Clone repository
git clone <repository-url>
cd spotify-clone

# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

აპლიკაცია გაეშვება: `http://localhost:8080`

## 👤 საწყისი Admin-ის შექმნა

Admin-ის შესაქმნელად, პირდაპირ ბაზაში ჩაამატე:
```sql
-- BCrypt encoded "Admin123!" password
INSERT INTO users (username, email, password, first_name, last_name, role, status, created_at, updated_at, is_deleted)
VALUES (
    'admin', 
    'admin@spotify.com', 
    '$2a$10$xqW1hHCnJ7fE8FwV5QW8VuP6YxK8n.vZ2tQ7hP8RmTqN3kJ8nK4Ee', 
    'Admin', 
    'User', 
    'ADMIN', 
    'ACTIVE', 
    NOW(), 
    NOW(), 
    false
);
```

**Admin Credentials:**
- Email: `admin@spotify.com`
- Password: `Admin123!`

## 📚 API Documentation

### 🔐 Authentication Endpoints

#### 1. რეგისტრაცია
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "user@example.com",
  "password": "Password123",
  "firstName": "Test",
  "lastName": "User",
  "role": "LISTENER"  // or "ARTIST"
}
```

#### 2. Email Verification
```http
POST /api/auth/verify-email
Content-Type: application/json

{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

#### 3. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": 1,
  "username": "testuser",
  "role": "LISTENER"
}
```

### 🎵 Music Endpoints

#### მუსიკის ძებნა
```http
GET /api/music/search?query=rock&page=0&size=20
Authorization: Bearer {token}
```

#### მუსიკის მოსმენა (listening history ჩაიწერება)
```http
GET /api/music/{id}
Authorization: Bearer {token}
```

#### მუსიკის ატვირთვა (ARTIST only)
```http
POST /api/music
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "New Song",
  "albumId": 1,  // optional
  "genre": "ROCK",
  "durationSeconds": 240,
  "fileUrl": "http://example.com/song.mp3"
}
```

### 💿 Album Endpoints

#### ალბომის შექმნა (ARTIST only)
```http
POST /api/albums
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "My Album",
  "releaseDate": "2025-01-01",
  "coverImageUrl": "http://example.com/cover.jpg"
}
```

#### ალბომის ნახვა
```http
GET /api/albums/{id}
Authorization: Bearer {token}
```

### 📋 Playlist Endpoints

#### Playlist-ის შექმნა
```http
POST /api/playlists
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "My Playlist",
  "description": "My favorite songs"
}
```

#### ჩემი Playlists
```http
GET /api/playlists/my?page=0&size=20
Authorization: Bearer {token}
```

#### სიმღერის დამატება Playlist-ში
```http
POST /api/playlists/{playlistId}/songs
Authorization: Bearer {token}
Content-Type: application/json

{
  "musicId": 5
}
```

#### სიმღერის წაშლა Playlist-იდან
```http
DELETE /api/playlists/{playlistId}/songs/{musicId}
Authorization: Bearer {token}
```

### 🎤 Artist & Recommendations

#### არტისტის პროფილი (similar artists-ით)
```http
GET /api/artists/{artistId}
Authorization: Bearer {token}
```

#### რეკომენდირებული Playlists
```http
GET /api/recommendations/playlists
Authorization: Bearer {token}
```

### 👑 Admin Endpoints

#### მომხმარებლების სია
```http
GET /api/admin/users?page=0&size=20&status=ACTIVE
Authorization: Bearer {admin-token}
```

#### იუზერის დაბლოკვა
```http
PUT /api/admin/users/{userId}/block
Authorization: Bearer {admin-token}
```

#### იუზერის წაშლა
```http
DELETE /api/admin/users/{userId}
Authorization: Bearer {admin-token}
```

#### მუსიკის წაშლა
```http
DELETE /api/admin/music/{musicId}
Authorization: Bearer {admin-token}
```

## 🧪 Testing

### Unit Tests-ის გაშვება
```bash
mvn test
```

### Coverage Report-ის გენერირება
```bash
mvn clean test jacoco:report
```

Coverage report იხილე: `target/site/jacoco/index.html`

**მოთხოვნა:** Service კლასების coverage უნდა იყოს მინიმუმ 80%

## 📊 კოდის სტრუქტურა
```
src/main/java/com/spotify/clone/
├── config/              # Security & Application Config
├── controller/          # REST Controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA Entities
├── enums/               # Enumerations
├── exception/           # Custom Exceptions & Handlers
├── repository/          # JPA Repositories
├── security/            # JWT & Security Classes
└── service/             # Business Logic Services

src/test/java/           # Unit Tests
```

## ⏰ Scheduled Tasks

### 1. Weekly Statistics Generation
- **გაშვება:** ყოველ პარასკევს 23:59-ზე
- **ფუნქცია:** აგენერირებს კვირის სტატისტიკას ყველა სიმღერისთვის
- **Testing:** შეცვალე `@Scheduled(fixedRate = 300000)` - ყოველ 5 წუთში

### 2. Expired Verification Codes Cleanup
- **გაშვება:** ყოველ საათში
- **ფუნქცია:** შლის ვერიფიცირებულ იუზერებს 24 საათის შემდეგ

## 🔒 უსაფრთხოება

- პაროლები hash-ირებულია BCrypt-ით
- JWT tokens ვალიდურია 24 საათი
- Email verification სავალდებულოა
- Role-based access control (RBAC)
- Soft delete strategy ყველა ენტიტისთვის

## 🐛 გასატესტი Scenarios

### 1. User Registration Flow
```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123",
    "firstName": "Test",
    "lastName": "User",
    "role": "LISTENER"
  }'

# 2. Check email for verification code

# 3. Verify
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "verificationCode": "123456"
  }'

# 4. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123"
  }'
```

### 2. Artist Music Upload Flow
```bash
# 1. Register as Artist
# 2. Create Album
curl -X POST http://localhost:8080/api/albums \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Album",
    "releaseDate": "2025-01-15"
  }'

# 3. Upload Music
curl -X POST http://localhost:8080/api/music \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Song",
    "albumId": 1,
    "genre": "ROCK",
    "durationSeconds": 240,
    "fileUrl": "http://example.com/song.mp3"
  }'
```

### 3. Playlist Management Flow
```bash
# 1. Create Playlist
curl -X POST http://localhost:8080/api/playlists \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Rock Collection",
    "description": "Best rock songs"
  }'

# 2. Add Song to Playlist
curl -X POST http://localhost:8080/api/playlists/1/songs \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"musicId": 5}'

# 3. View Playlist
curl -X GET http://localhost:8080/api/playlists/1 \
  -H "Authorization: Bearer {token}"
```

### 4. Search & Listen Flow
```bash
# 1. Search Music
curl -X GET "http://localhost:8080/api/music/search?query=rock" \
  -H "Authorization: Bearer {token}"

# 2. Listen to Music (records history)
curl -X GET http://localhost:8080/api/music/5 \
  -H "Authorization: Bearer {token}"

# 3. Get Recommendations
curl -X GET http://localhost:8080/api/recommendations/playlists \
  -H "Authorization: Bearer {token}"
```

### 5. Admin Operations
```bash
# 1. Get All Users
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=20" \
  -H "Authorization: Bearer {admin-token}"

# 2. Block User
curl -X PUT http://localhost:8080/api/admin/users/5/block \
  -H "Authorization: Bearer {admin-token}"

# 3. Delete Music
curl -X DELETE http://localhost:8080/api/admin/music/10 \
  -H "Authorization: Bearer {admin-token}"
```

## 🎯 ფიჩრების დამოწმება

### ✅ Authentication & Authorization
- [x] რეგისტრაცია LISTENER/ARTIST როლებით
- [x] Email verification 6-digit კოდით
- [x] JWT-based login
- [x] Role-based access control
- [x] Admin-ის ხელით დამატება ბაზაში

### ✅ User Management (Admin)
- [x] მომხმარებლების სია pagination-ით
- [x] იუზერის ინფორმაციის განახლება
- [x] იუზერის წაშლა (soft delete)
- [x] იუზერის დაბლოკვა/განბლოკვა
- [x] ფილტრაცია status-ის და role-ის მიხედვით

### ✅ Music Management
- [x] მუსიკის ატვირთვა (ARTIST only)
- [x] მუსიკის CRUD ოპერაციები
- [x] მუსიკის ძებნა სახელით/ავტორით (partial match)
- [x] Permission checks (მხოლოდ საკუთარი მუსიკის რედაქტირება)

### ✅ Album Management
- [x] ალბომის შექმნა (ARTIST only)
- [x] ალბომის CRUD ოპერაციები
- [x] მუსიკის დაკავშირება ალბომთან
- [x] Permission checks

### ✅ Playlist Management
- [x] Playlist-ის შექმნა
- [x] Playlist-ის CRUD ოპერაციები
- [x] სიმღერების დამატება/წაშლა
- [x] Permission checks (მხოლოდ საკუთარი playlists)
- [x] System-generated playlists-ის დაცვა

### ✅ Statistics & Recommendations
- [x] Listening history-ს ჩაწერა
- [x] Weekly statistics generation (scheduled job)
- [x] Similar artists-ის გენერირება ჟანრის მიხედვით
- [x] Personalized playlists ტოპ 3 ჟანრისთვის

### ✅ Search Functionality
- [x] Fuzzy search (partial match)
- [x] ძებნა სათაურით
- [x] ძებნა ავტორით
- [x] Pagination support

## 🔄 Git Flow სტრატეგია

### Branch სტრუქტურა
```
main (production)
  │
  └── develop
       ├── feature/user-authentication
       ├── feature/admin-panel
       ├── feature/music-management
       ├── feature/playlist-functionality
       ├── feature/search
       ├── feature/statistics
       └── feature/recommendations
```

### Commit Convention
```
feat: Add user registration endpoint
fix: Fix playlist permission check
test: Add unit tests for UserService
refactor: Optimize music search query
docs: Update README with API examples
chore: Update dependencies
```

### Workflow
1. Feature branch-ის შექმნა develop-იდან
2. ფიჩრის იმპლემენტაცია tests-თან ერთად
3. Coverage 80%+ უზრუნველყოფა
4. Pull Request-ის შექმნა develop-ში
5. Review და merge
6. Release: develop → main

## 📦 Dependencies

### Core Dependencies
```xml
<!-- Spring Boot Web -->
spring-boot-starter-web

<!-- Spring Boot Data JPA -->
spring-boot-starter-data-jpa

<!-- Spring Boot Security -->
spring-boot-starter-security

<!-- Spring Boot Validation -->
spring-boot-starter-validation

<!-- Spring Boot Mail -->
spring-boot-starter-mail

<!-- PostgreSQL Driver -->
postgresql

<!-- JWT -->
jjwt-api, jjwt-impl, jjwt-jackson

<!-- Lombok -->
lombok

<!-- Testing -->
spring-boot-starter-test
spring-security-test
```

## 🐞 ცნობილი Issues და გადაწყვეტები

### Issue 1: Email არ იგზავნება
**გადაწყვეტა:**
1. დარწმუნდი რომ Gmail-ის 2-Step Verification ჩართულია
2. გამოიყენე App Password, არა რეგულარული პაროლი
3. Check firewall settings for SMTP port 587

### Issue 2: JWT Token არ მუშაობს
**გადაწყვეტა:**
1. დარწმუნდი რომ `jwt.secret` არის 256-bit (32+ characters)
2. Check token expiration time
3. Verify Authorization header format: `Bearer {token}`

### Issue 3: Database Connection Error
**გადაწყვეტა:**
1. დარწმუნდი რომ PostgreSQL service გაშვებულია
2. Check database credentials
3. Verify database exists: `psql -U postgres -c "\l"`

## 📈 Performance Considerations

### Database Optimization
- ✅ Indexes დამატებულია frequently queried fields-ზე
- ✅ Pagination გამოყენებულია list endpoints-ზე
- ✅ Lazy loading relationships-ზე
- ✅ Soft delete strategy

### Query Optimization
- ✅ N+1 problem თავიდან არიდება eager loading-ით სადაც საჭიროა
- ✅ JPQL queries optimized
- ✅ Composite indexes critical queries-თვის

## 🔮 სამომავლო გაუმჯობესებები

- [ ] Redis caching frequently accessed data-სთვის
- [ ] File upload support real music files-თვის (Amazon S3)
- [ ] WebSocket support real-time notifications-თვის
- [ ] Advanced search filters (year, duration, etc.)
- [ ] User followers/following system
- [ ] Social features (comments, likes)
- [ ] Payment integration for premium features
- [ ] Mobile API optimization
- [ ] GraphQL support

## 📞 Support

**Issues:** GitHub Issues გამოიყენე bugs და feature requests-თვის

**Documentation:** ეს README შეიცავს ყველა საჭირო ინფორმაციას

## 📄 License

MIT License - თავისუფლად გამოიყენე სასწავლო მიზნებისთვის

---

## 🎓 სასწავლო მიზნები რომელიც მიღწეულია

✅ Spring Boot 3 და Spring Security გამოყენება
✅ JWT Authentication implementation
✅ Email integration JavaMailSender-ით
✅ JPA Relationships და Hibernate
✅ RESTful API design principles
✅ Exception Handling best practices
✅ Unit Testing Mockito-ით (80%+ coverage)
✅ Scheduled Tasks (@Scheduled)
✅ Role-Based Access Control (RBAC)
✅ Soft Delete pattern
✅ DTOs და Entity mapping
✅ Pagination და Sorting
✅ Git Flow methodology
✅ Clean Code principles
✅ SOLID principles

## 🚦 Quick Start Guide

### 5 წუთში გაშვება:

```bash
# 1. Clone & Navigate
git clone <repo-url>
cd spotify-clone

# 2. Database Setup
psql -U postgres
CREATE DATABASE spotify_clone;
CREATE USER spotify_user WITH PASSWORD 'spotify_password';
GRANT ALL PRIVILEGES ON DATABASE spotify_clone TO spotify_user;
\q

# 3. Configure Application
# Edit src/main/resources/application.yaml
# Set your email credentials

# 4. Build & Run
mvn clean install
mvn spring-boot:run

# 5. Create Admin User
psql -U spotify_user -d spotify_clone
-- Paste admin INSERT query from above

# 6. Test
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@spotify.com","password":"Admin123!"}'
```

---

**Made with ❤️ for Learning Spring Boot**

**Version:** 1.0.0  
**Last Updated:** 2025-01-18

---

## 💡 Tips for Developers

### Testing Email Locally
```java
// For local testing, use console output instead of real email
@Profile("dev")
@Service
public class ConsoleEmailService extends EmailService {
    @Override
    public void sendVerificationEmail(String to, String name, String code) {
        System.out.println("=== EMAIL ===");
        System.out.println("To: " + to);
        System.out.println("Code: " + code);
        System.out.println("============");
    }
}
```

### Quick Database Reset
```sql
-- Reset all data
TRUNCATE TABLE 
  listening_history, 
  weekly_statistics, 
  playlist_music, 
  playlists, 
  music, 
  albums, 
  users 
RESTART IDENTITY CASCADE;

-- Re-add admin
-- Use admin INSERT query from above
```

### Postman Collection
Import to Postman for easy testing:
```json
{
  "info": {
    "name": "Spotify Clone API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Register",
          "request": {
            "method": "POST",
            "header": [],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"testuser\",\n  \"email\": \"test@example.com\",\n  \"password\": \"Password123\",\n  \"firstName\": \"Test\",\n  \"lastName\": \"User\",\n  \"role\": \"LISTENER\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "http://localhost:8080/api/auth/register",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "auth", "register"]
            }
          }
        }
      ]
    }
  ]
}
```

## 🎉 დასასრული