# Aegis Delta Engine - Docker Deployment Guide

## 🐳 Docker Files Created

1. **Dockerfile** - Multi-stage build for production
2. **.dockerignore** - Optimizes build context
3. **docker-compose.yml** - Full stack with Kafka
4. **application-docker.yml** - Docker-specific config
5. **render-docker.yaml** - Render.com Docker deployment

## 🚀 Local Docker Development

### Option 1: Docker Compose (Recommended)
```bash
# Start full stack (Aegis + Kafka)
docker-compose up -d

# View logs
docker-compose logs -f aegis-app

# Stop stack
docker-compose down
```

### Option 2: Single Container
```bash
# Build image
docker build -t aegis-delta-engine .

# Run container
docker run -p 8080:8080 aegis-delta-engine
```

## 🌐 Render.com Docker Deployment

### Step 1: Update render.yaml
```bash
# Replace render.yaml with render-docker.yaml
cp render-docker.yaml render.yaml

# Commit changes
git add render.yaml Dockerfile .dockerignore
git commit -m "Add Docker deployment configuration"
git push origin main
```

### Step 2: Deploy on Render
1. Go to [render.com](https://render.com)
2. Connect your GitHub repo
3. Render will auto-detect Docker configuration
4. Deploy automatically

## 🔧 Docker Configuration Details

### Dockerfile Features:
- **Multi-stage build** for smaller images
- **Layer caching** for faster builds
- **Alpine Linux** for minimal footprint
- **Maven wrapper** included

### Docker Compose Stack:
- **Aegis App** on port 8080
- **Kafka** on port 9092
- **Zookeeper** for Kafka coordination
- **Shared network** for communication

### Environment Variables:
- **SPRING_PROFILES_ACTIVE=docker** for Docker config
- **KAFKA_BOOTSTRAP_SERVERS=kafka:29092** for internal networking
- **PORT=8080** for application port

## 🧪 Testing Docker Deployment

### Local Testing:
```bash
# Start stack
docker-compose up -d

# Wait 30 seconds for startup
sleep 30

# Test health endpoint
curl http://localhost:8080/api/v1/health

# Test product ingestion
curl -X POST http://localhost:8080/api/v1/admin/products \
  -H "Content-Type: application/json" \
  -d '{"id":"DOCKER-TEST","name":"Docker Demo","price":25000,"stock":100,"warehouse":"Docker-1","lastUpdated":1714677500000}'

# Check metrics
curl http://localhost:8080/api/v1/metrics
```

### Production Testing (Render):
```bash
# After deployment
curl https://your-app.onrender.com/api/v1/health
curl https://your-app.onrender.com/api/v1/metrics
```

## 📊 Benefits of Docker Deployment

1. **Consistent Environment**: Same config everywhere
2. **Easy Scaling**: Horizontal scaling ready
3. **Isolation**: No dependency conflicts
4. **Portability**: Deploy anywhere
5. **Version Control**: Infrastructure as code

## 🔍 Troubleshooting

### Common Issues:
- **Port conflicts**: Change external port in docker-compose.yml
- **Memory limits**: Adjust JVM options in docker-compose.yml
- **Kafka connection**: Check network configuration
- **Build failures**: Verify Dockerfile syntax

### Debug Commands:
```bash
# View container logs
docker logs aegis-app

# Enter container for debugging
docker exec -it aegis-app sh

# Rebuild without cache
docker-compose build --no-cache
```

## 🎯 Interview Demo Ready

With Docker deployment, you can:
1. **Show containerized architecture**
2. **Demonstrate microservices with Kafka**
3. **Explain infrastructure as code**
4. **Show production-ready deployment**
5. **Discuss scaling strategies**

Perfect for showcasing modern DevOps practices! 🚀
