# AWS Deployment Guide (AWS CDK)

This guide outlines how to deploy the IAM Service to AWS using the **AWS Cloud Development Kit (CDK)**.

## Architecture Overview

The production-ready architecture on AWS consists of:

- **VPC**: A dedicated Virtual Private Cloud with Public and Private subnets across 2 Availability Zones.
- **Database**: **Amazon RDS for PostgreSQL 16** (Multi-AZ) or **Amazon Aurora Serverless v2**.
- **Compute**: **Amazon ECS with AWS Fargate** for both the `iam-service` and `keycloak`.
- **Load Balancing**: **Application Load Balancer (ALB)** to handle ingress traffic and SSL termination.
- **Security**: **AWS Secrets Manager** for database credentials and Keycloak administrative secrets.
- **Container Registry**: **Amazon ECR** for storing Docker images.
- **Observability**: **AWS Distro for OpenTelemetry (ADOT)** collector running as a sidecar or service, exporting to CloudWatch or Amazon Managed Prometheus/Grafana.

## Prerequisites

- **AWS CLI**: Installed and configured ([guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)).
- **Node.js & npm**: Required for CDK.
- **AWS CDK CLI**: `npm install -g aws-cdk`.
- **Docker**: Must be running locally for container bundling during deployment.
- **Java 25 & Maven**: To build the application artifact.

## Deployment Steps

### 1. Build the Application
Ensure the application is compiled and the JAR is ready for containerization:
```bash
./mvnw clean package -DskipTests
```

### 2. Initialize the CDK Project
Create a separate directory for infrastructure:
```bash
mkdir infrastructure && cd infrastructure
cdk init app --language typescript
```

### 3. Define the Infrastructure (Code Example)
In your `lib/infrastructure-stack.ts`, you would define your resources:

```typescript
import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as rds from 'aws-cdk-lib/aws-rds';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ecs_patterns from 'aws-cdk-lib/aws-ecs-patterns';

// ... Inside Stack constructor
const vpc = new ec2.Vpc(this, 'IamVpc', { maxAzs: 2 });

const db = new rds.DatabaseInstance(this, 'IamDatabase', {
  engine: rds.DatabaseInstanceEngine.postgres({ version: rds.PostgresEngineVersion.VER_16 }),
  vpc,
  databaseName: 'iam_db',
  // Use Secrets Manager by default for credentials
});

const cluster = new ecs.Cluster(this, 'IamCluster', { vpc });

// iam-service
const iamService = new ecs_patterns.ApplicationLoadBalancedFargateService(this, 'IamFargateService', {
  cluster,
  taskImageOptions: {
    image: ecs.ContainerImage.fromAsset('../'), // Points to root where Dockerfile/pom.xml are
    environment: {
      SPRING_DATASOURCE_URL: `jdbc:postgresql://${db.dbInstanceEndpointAddress}:5432/iam_db`,
      SPRING_PROFILES_ACTIVE: 'prod',
    },
  },
  publicLoadBalancer: true,
});
```

### 4. Configuration for Production

Update `src/main/resources/application.properties` or use environment variables to override:

| Property | AWS Resource / Value |
| --- | --- |
| `spring.datasource.url` | RDS Endpoint |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | ALB URL for Keycloak |
| `keycloak.auth-server-url` | ALB URL for Keycloak |
| `management.otlp.tracing.endpoint` | ADOT Collector Endpoint |

### 5. Deploy
Bootstrap your AWS environment (if first time):
```bash
cdk bootstrap
```

Deploy the stack:
```bash
cdk deploy
```

## Post-Deployment
1. **Keycloak Setup**: Access the Keycloak ALB URL and configure the `saas-iam` realm, clients, and users as described in the main `README.md`.
2. **Secrets**: Transition hardcoded passwords in environment variables to AWS Secrets Manager references.
3. **Domain & SSL**: Use **AWS Certificate Manager (ACM)** and **Route 53** to attach a custom domain and HTTPS to your load balancer.
