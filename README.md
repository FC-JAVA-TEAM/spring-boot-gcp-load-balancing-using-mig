# Spring Boot Application Deployment with Docker on GCP

This guide provides a complete walkthrough for deploying a Spring Boot application using Docker on Google Cloud Platform (GCP) with load balancing and managed instance groups.

## Table of Contents

1. [Containerize the Application](#1-containerize-the-application)
2. [Create Startup Script](#2-create-startup-script)
3. [GCP Console Setup Steps](#3-gcp-console-setup-steps)
   - [Create Instance Template](#a-create-instance-template)
   - [Create Health Check](#b-create-health-check)
   - [Create Firewall Rules](#c-create-firewall-rules)
   - [Create Managed Instance Group (MIG)](#d-create-managed-instance-group-mig)
   - [Create Load Balancer](#e-create-load-balancer)
4. [Verify Deployment](#4-verify-deployment)
5. [Monitoring and Maintenance](#5-monitoring-and-maintenance)
6. [Available API Endpoints](#available-api-endpoints)

## 1. Containerize the Application

### Create a Dockerfile

Create a `Dockerfile` in your project directory:

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/spring-boot-gcp-load-balancing-using-mig-0.0.1-SNAPSHOT.jar app.jar
ENV ZONE=default-zone
EXPOSE 1212
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build and push the Docker image

```bash
# Build the JAR
mvn clean package

# Configure Docker to use GCR
gcloud auth configure-docker

# Build and tag the Docker image
docker build -t gcr.io/telus-koodo/spring-boot-gcp-load-balancing-using-mig:v1 .

# Push to GCR
docker push gcr.io/telus-koodo/spring-boot-gcp-load-balancing-using-mig:v1
```

## 2. Create Startup Script

Create a startup script (`startup.sh`):

```bash
#!/bin/bash

# Install Docker
apt-get update
apt-get install -y docker.io

# Start Docker service
systemctl start docker
systemctl enable docker

# Configure Docker to use GCR
gcloud auth configure-docker --quiet

# Get instance zone
ZONE=$(curl -H "Metadata-Flavor: Google" http://metadata.google.internal/computeMetadata/v1/instance/zone)
ZONE=${ZONE##*/}

# Pull and run the container
docker pull gcr.io/telus-koodo/spring-boot-gcp-load-balancing-using-mig:v1
docker run -d \
    --name spring-app \
    -p 1212:1212 \
    -e ZONE=$ZONE \
    --restart always \
    gcr.io/telus-koodo/spring-boot-gcp-load-balancing-using-mig:v1
```

## 3. GCP Console Setup Steps

### A. Create Instance Template

1. Go to Compute Engine > Instance Templates > Create Instance Template
2. Configure basic settings:
   - Name: spring-app-template
   - Machine type: e2-small
   - Boot disk: Debian 11
   - Service account: Select one with access to GCR
3. In Management tab:
   - Add startup script (content from startup.sh above)
4. In Networking tab:
   - Network tags: Add spring-app

### B. Create Health Check

1. Go to Compute Engine > Health Checks > Create Health Check
2. Configure:
   - Name: spring-app-health
   - Protocol: HTTP
   - Port: 1212
   - Request path: /api/data/health
   - Check interval: 10 seconds
   - Timeout: 5 seconds
   - Healthy threshold: 2
   - Unhealthy threshold: 3

### C. Create Firewall Rules

1. Go to VPC Network > Firewall > Create Firewall Rule
2. Configure:
   - Name: allow-health-check
   - Direction: Ingress
   - Targets: Specified target tags
   - Target tags: spring-app
   - Source ranges: 130.211.0.0/22,35.191.0.0/16 (Google Health Check ranges)
   - Protocols and ports: tcp:1212

3. Create another firewall rule:
   - Name: allow-http-1212
   - Direction: Ingress
   - Targets: Specified target tags
   - Target tags: spring-app
   - Source ranges: 0.0.0.0/0
   - Protocols and ports: tcp:1212

### D. Create Managed Instance Group (MIG)

1. Go to Compute Engine > Instance Groups > Create Instance Group
2. Configure:
   - Name: spring-app-mig
   - Instance template: Select spring-app-template
   - Location: Select region and zones
   - Minimum instances: 2
   - Maximum instances: 4
   - Autoscaling:
     - CPU utilization target: 60%
   - Health check: Select spring-app-health
   - Initial delay: 300 seconds (5 minutes to allow for Docker setup)

### E. Create Load Balancer

1. Go to Network Services > Load Balancing > Create Load Balancer
2. Choose "HTTP(S) Load Balancing"
3. Configure:
   - Name: spring-app-lb
   - Backend configuration:
     - Create backend service
     - Select your instance group
     - Health check: Use spring-app-health
   - Frontend configuration:
     - Protocol: HTTP
     - Port: 80
     - IP: Create new static IP

## 4. Verify Deployment

1. Wait for instances to be created and become healthy (may take 5-10 minutes)
2. Access your application:
   ```
   http://<LOAD-BALANCER-IP>/api/data/health
   ```

## 5. Monitoring and Maintenance

- Monitor instance health in Instance Groups dashboard
- View logs:
  ```bash
  # SSH into an instance
  docker logs spring-app
  ```
- Update application:
  1. Build new Docker image with new version tag
  2. Push to GCR
  3. Update instance template with new image version
  4. Rolling update MIG

This setup provides:
- Containerized application
- Auto-scaling based on CPU usage
- High availability across multiple zones
- Health monitoring
- Load balancing
- Automated instance recovery
- Rolling updates capability

Remember to clean up resources when not needed to avoid unnecessary charges.

## Available API Endpoints

The application provides the following endpoints for generating random data:

### Health Check
```
GET /api/data/health
Response: "OK"
```

### User Data
```
GET /api/data/user
Response: Single user object with random data

GET /api/data/users
Response: Array of 10 user objects
```

### Product Data
```
GET /api/data/product
Response: Single product object with random data

GET /api/data/products
Response: Array of 10 product objects
```

### Company Data
```
GET /api/data/company
Response: Company object with random data including name, catchphrase, industry, etc.
```

### Address Data
```
GET /api/data/address
Response: Address object with random street, city, state, country, etc.
```

### Bank Account Data
```
GET /api/data/bank
Response: Bank account object with random account details
```

All endpoints return JSON data that can be used for testing and development purposes. The data is randomly generated using the Java Faker library, ensuring unique values for each request.
