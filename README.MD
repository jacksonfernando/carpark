# Car Park API Application

A Java Spring Boot application that finds the nearest car parks to a user's location with availability information using Singapore government data. Features Redis caching for improved performance, proper database indexing, audit fields, soft delete functionality, and Docker Compose for easy deployment.

## Features

- **Nearest Car Park Search**: Find car parks near a given location with availability information
- **Redis Caching**: 15-minute cache for frequently accessed data to improve performance
- **Database Indexing**: Optimized indexes for location-based queries and availability filtering
- **Audit Fields**: Track creation, updates, and soft deletes with user attribution
- **Soft Delete**: Non-destructive deletion with restore capability
- **Real-time Availability**: Automatic updates from Singapore government API every 15 minutes
- **Coordinate Conversion**: Converts SVY21 coordinates to WGS84 (latitude/longitude) format
- **Pagination Support**: Efficient pagination for large result sets
- **Distance Calculation**: Uses Haversine formula for accurate distance calculations
- **RESTful API**: Clean REST endpoints with proper validation and error handling
- **Docker Support**: Complete containerization with MySQL, Redis, and Spring Boot

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **MySQL 8.0**
- **Redis 7** (for caching)
- **Spring Security**
- **OpenCSV** for CSV data import
- **WebClient** for HTTP API calls
- **Jackson** for JSON processing
- **Docker & Docker Compose**

## Prerequisites

- Java 21 or higher (for local development)
- Docker and Docker Compose (for containerized deployment)
- MySQL 8.0 or higher (for local development)
- Redis 7 or higher (for local development)

## Quick Start with Docker Compose

The easiest way to run the application is using Docker Compose:

```bash
# Clone the repository
git clone <repository-url>
cd SpringBootStarter

# Place your car park CSV file in the data/ directory
# The file should be named carpark-data.csv

# Start all services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f carpark-api
```

The application will be available at `http://localhost:8080`

## Manual Setup Instructions

### 1. Database Setup

Create the MySQL database:
```sql
CREATE DATABASE carpark_db;
CREATE USER 'carpark_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON carpark_db.* TO 'carpark_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Redis Setup

Install and start Redis:
```bash
# macOS
brew install redis
brew services start redis

# Ubuntu/Debian
sudo apt-get install redis-server
sudo systemctl start redis-server
```

**Note**: The application is configured to use Redis on port 7001. If you're using Docker Compose, Redis will be available on port 7001. For local development, make sure your Redis instance is running on port 7001.

### 3. Configuration

Update `src/main/resources/application.properties` with your credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/carpark_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.data.redis.host=127.0.0.1
spring.data.redis.port=7001
```

### 4. Database Schema

The application uses a comprehensive database schema with proper indexing and audit fields:

#### Car Parks Table
- **Primary Key**: `id` (auto-increment)
- **Unique Index**: `car_park_no` (car park identifier)
- **Location Indexes**: `latitude`, `longitude` (for distance queries)
- **Availability Indexes**: `available_lots`, `deleted_at` (for filtering)
- **Audit Fields**: `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`

#### Key Indexes
- `idx_car_park_no`: For fast car park lookups
- `idx_latitude_longitude`: For location-based queries
- `idx_available_lots`: For availability filtering
- `idx_deleted_at`: For soft delete filtering
- `idx_available_car_parks`: Composite index for available, non-deleted car parks

#### Views
- `active_car_parks`: All non-deleted car parks
- `available_car_parks`: Car parks with available lots > 0

### 5. Car Park Data

Place the Singapore car park CSV file in `data/carpark-data.csv` with the following format:
```csv
car_park_no,address,x_coord,y_coord,car_park_type,type_of_parking_system,short_term_parking,free_parking,night_parking,car_park_decks,gantry_height,car_park_basement
A1,BLK 1/2 TANJONG PAGAR PLAZA,26076.1487,48187.2034,SURFACE CAR PARK,COUPON,WHOLE DAY,NO,YES,0,0,0
```

### 5. Build and Run

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Find Nearest Car Parks

**GET** `/api/carparks/nearest`

**Request Body:**
```json
{
  "latitude": 1.234,
  "longitude": 103.456,
  "page": 1,
  "per_page": 10
}
```

**Response:**
```json
[
  {
    "address": "BLK 1/2 TANJONG PAGAR PLAZA",
    "latitude": 1.234,
    "longitude": 103.456,
    "total_lots": 100,
    "available_lots": 50
  }
]
```

### 2. Find Nearest Car Parks (Query Parameters)

**GET** `/api/carparks/nearest/query?latitude=1.234&longitude=103.456&page=1&per_page=10`

### 3. Get All Available Car Parks

**GET** `/api/carparks/available`

### 4. Import Car Park Data

**POST** `/api/carparks/import`

### 5. Update Car Park Availability

**POST** `/api/carparks/update-availability`

### 6. Health Check

**GET** `/api/carparks/health`

## Caching Strategy

The application uses Redis caching to improve performance:

- **Cache Duration**: 15 minutes (matching the Singapore API update frequency)
- **Cached Data**:
  - Nearest car park search results
  - All available car parks list
  - Distance calculations
- **Cache Keys**: Include coordinates and pagination parameters for precise caching
- **Cache Invalidation**: Automatic TTL-based expiration

### Cache Performance Benefits

- **Reduced Database Queries**: Frequently accessed data is served from cache
- **Faster Response Times**: Cached responses are served in milliseconds
- **Reduced API Load**: Less strain on the Singapore government API
- **Scalability**: Multiple application instances can share the same cache

## Data Import Process

### 1. Initial Data Import

The application includes a data import service that:
- Reads car park data from CSV file
- Converts SVY21 coordinates to WGS84 using OneMap API
- Stores static car park information in the database

### 2. Availability Updates

The application automatically:
- Fetches real-time availability from Singapore government API
- Updates car park availability every 15 minutes
- Handles API rate limiting and error scenarios
- Updates cache when new data is available

## Coordinate System Conversion

The application converts coordinates from Singapore's SVY21 system to WGS84 (latitude/longitude) using the OneMap API:

- **SVY21**: Singapore's local coordinate system
- **WGS84**: Global latitude/longitude system used by GPS devices

## Distance Calculation

Uses the Haversine formula to calculate distances between coordinates:

```java
distance = 6371 * acos(cos(radians(lat1)) * cos(radians(lat2)) *
         cos(radians(lon2) - radians(lon1)) +
         sin(radians(lat1)) * sin(radians(lat2)))
```

## Testing

Run the test suite:

```bash
./gradlew test
```

The application includes unit tests for:
- CarParkService
- Coordinate conversion
- Distance calculations
- API endpoints
- Cache functionality

## Docker Commands

### Development

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Rebuild and restart
docker-compose up -d --build

# Access MySQL
docker-compose exec mysql mysql -u carpark_user -p carpark_db

# Access Redis
docker-compose exec redis redis-cli

# Access application shell
docker-compose exec carpark-api sh
```

### Production

```bash
# Build production image
docker build -t carpark-api:latest .

# Run with production environment
docker-compose -f docker-compose.prod.yml up -d
```

## Example Usage

### Using curl:

```bash
# Find nearest car parks
curl -X GET "http://localhost:8080/api/carparks/nearest" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 1.234,
    "longitude": 103.456,
    "page": 1,
    "per_page": 10
  }'

# Import car park data
curl -X POST "http://localhost:8080/api/carparks/import"

# Update availability
curl -X POST "http://localhost:8080/api/carparks/update-availability"
```

### Using Postman:

Import the provided Postman collection (`API.postman_collection.json`) for easy API testing.

## Error Handling

The application includes comprehensive error handling:

- **400 Bad Request**: Invalid parameters (latitude/longitude out of range)
- **500 Internal Server Error**: Server-side errors
- **Validation Errors**: Detailed validation messages for request parameters
- **Cache Errors**: Graceful fallback when Redis is unavailable

## Performance Considerations

- **Database Indexing**: Optimized queries for location-based searches
- **Redis Caching**: 15-minute cache for frequently accessed data
- **Pagination**: Efficient pagination to handle large datasets
- **Batch Processing**: Car park data import uses batch processing for efficiency
- **Connection Pooling**: Optimized database and Redis connection pools

## Monitoring and Logging

The application includes:
- Structured logging with SLF4J
- Health check endpoints
- Scheduled task monitoring
- Error tracking and reporting
- Cache hit/miss logging
- Performance metrics

## Deployment

### Docker Compose (Recommended)

```bash
# Production deployment
docker-compose -f docker-compose.prod.yml up -d

# Development deployment
docker-compose up -d
```

### Manual Deployment

1. Build the application: `./gradlew build`
2. Set up MySQL database
3. Configure Redis
4. Run the JAR file: `java -jar build/libs/blog-0.0.1-SNAPSHOT.jar`

### Production Considerations

- Use MySQL for production database
- Configure Redis with persistence
- Set up monitoring and alerting
- Implement rate limiting
- Use HTTPS in production
- Configure proper CORS settings
- Set up log aggregation
- Use environment-specific configurations


## License

This project is licensed under the MIT License.

## Support

For issues and questions, please create an issue in the repository.
r
