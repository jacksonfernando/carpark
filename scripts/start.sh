#!/bin/bash

# Car Park API Docker Compose Startup Script

echo "🚗 Starting Car Park API with Docker Compose..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker compose is available
if ! command -v docker compose &> /dev/null; then
    echo "❌ docker compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

# Check if car park data file exists
if [ ! -f "data/carpark-data.csv" ]; then
    echo "⚠️  Warning: data/carpark-data.csv not found."
    echo "   Please place your Singapore car park CSV file in the data/ directory."
    echo "   The application will still start but you'll need to import data manually."
fi

# Build and start services
echo "🔨 Building and starting services..."
docker compose up -d --build

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check service status
echo "📊 Service Status:"
docker compose ps

# Check if application is responding
echo "🔍 Checking application health..."
if curl -f http://localhost:8080/api/carparks/health > /dev/null 2>&1; then
    echo "✅ Application is running successfully!"
    echo "🌐 API is available at: http://localhost:8080"
    echo "📚 API Documentation: http://localhost:8080/api/carparks/health"
else
    echo "⚠️  Application may still be starting up. Check logs with: docker compose logs -f carpark-api"
fi

echo ""
echo "📋 Useful Commands:"
echo "  View logs: docker compose logs -f"
echo "  Stop services: docker compose down"
echo "  Restart services: docker compose restart"
  echo "  Access MySQL: docker compose exec mysql mysql -u carpark_user -p carpark_db"
  echo "  Access Redis: docker compose exec redis redis-cli"
  echo "  Redis is available on localhost:7001"
