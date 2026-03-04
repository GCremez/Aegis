# Aegis Delta Engine - Deployment Guide

## Option 1: Render.com (Recommended)

### Step 1: Prepare for Deployment
```bash
# 1. Create render.yaml
cat > render.yaml << EOF
services:
  - type: web
    name: aegis-delta-engine
    env: java
    buildCommand: mvn clean package -DskipTests
    startCommand: java -jar target/Aegis-0.0.1-SNAPSHOT.jar
    envVars:
      - key: JAVA_VERSION
        value: 21
      - key: PORT
        value: 10000
EOF

# 2. Add to git
git add render.yaml
git commit -m "Add Render deployment config"
git push origin main
```

### Step 2: Deploy on Render
1. Go to [render.com](https://render.com)
2. Sign up with GitHub
3. Click "New" → "Web Service"
4. Connect your GitHub repo
5. Render will auto-detect render.yaml
6. Click "Create Web Service"
7. Wait for deployment (2-3 minutes)
8. Your app will be live at `https://your-app.onrender.com`

## Option 2: Heroku

### Step 1: Install Heroku CLI
```bash
# Windows
winget install Heroku.Heroku

# Mac
brew install heroku/brew/heroku

# Login
heroku login
```

### Step 2: Create Heroku App
```bash
# Create app
heroku create aegis-delta-engine

# Add Java buildpack
heroku buildpacks:add heroku/java

# Deploy
git push heroku main

# Open app
heroku open
```

### Step 3: Verify Deployment
```bash
# Check logs
heroku logs --tail

# Test API
curl https://your-app.herokuapp.com/api/v1/health
```

## Option 3: Railway.app

### Step 1: Prepare Dockerfile
```bash
cat > Dockerfile << EOF
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/Aegis-0.0.1-SNAPSHOT.jar"]
EOF

git add Dockerfile
git commit -m "Add Dockerfile"
git push origin main
```

### Step 2: Deploy on Railway
1. Go to [railway.app](https://railway.app)
2. Connect GitHub account
3. Click "New Project"
4. Select your repo
5. Railway will auto-detect Dockerfile
6. Deploy automatically
7. Get your URL from dashboard

## Option 4: Vercel (Serverless)

### Step 1: Create vercel.json
```bash
cat > vercel.json << EOF
{
  "version": 2,
  "builds": [
    {
      "src": "target/Aegis-0.0.1-SNAPSHOT.jar",
      "use": "@vercel/java"
    }
  ],
  "routes": [
    {
      "src": "/(.*)",
      "dest": "target/Aegis-0.0.1-SNAPSHOT.jar"
    }
  ]
}
EOF
```

### Step 2: Deploy
```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
vercel --prod
```

## Environment Variables for All Platforms

Add these to your hosting platform:

```bash
# For Kafka (if you have a broker)
KAFKA_BOOTSTRAP_SERVERS=your-kafka-broker:9092
KAFKA_TOPIC=delta-events

# For application
SERVER_PORT=8080
JAVA_OPTS="-Xmx512m -Xms256m"
```

## Testing Your Deployment

After deployment, test these endpoints:

```bash
# Health check
curl https://your-domain.com/api/v1/health

# Metrics
curl https://your-domain.com/api/v1/metrics

# Test product ingestion
curl -X POST https://your-domain.com/api/v1/admin/products \
  -H "Content-Type: application/json" \
  -d '{"id":"DEMO-001","name":"Demo Product","price":25000,"stock":100,"warehouse":"Demo-1","lastUpdated":1714677500000}'
```

## Interview Ready URLs

Once deployed, you'll have:
- **Health Endpoint**: `https://your-domain.com/api/v1/health`
- **Metrics Dashboard**: `https://your-domain.com/api/v1/metrics`
- **API Documentation**: Show the live endpoints
- **Live Demo**: Real-time processing demonstration

## Pro Tips for Interview

1. **Test Before Interview**: Verify all endpoints work
2. **Have Backup**: Keep local version ready
3. **Prepare Demo**: Know your test commands
4. **Explain Architecture**: Be ready to discuss design choices
5. **Performance Metrics**: Show throughput and efficiency

## Troubleshooting

### Common Issues:
- **Build Failures**: Check Java version compatibility
- **Port Issues**: Ensure platform uses correct port
- **Memory Issues**: Adjust JVM settings
- **Network Issues**: Check firewall/CORS settings

### Quick Fixes:
```bash
# For memory issues
export JAVA_OPTS="-Xmx1g -Xms512m"

# For port issues
export PORT=8080

# For build issues
mvn clean package -DskipTests
```
