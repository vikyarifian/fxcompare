# FX Compare (fxcompare)

Internal enterprise backend service built for the finance and treasury operations team to ingest daily foreign exchange rates from Bank Indonesia (BI) and major commercial banking partners (BCA, Mandiri) into PostgreSQL, enabling automated variance checks against vendor invoices.

## Overview

- **Automated Rate Ingestion:** Scheduled daily job fetching official published rates (`kurs` counter/e-rate) from Bank Indonesia and primary enterprise bank endpoints.
- **Historical FX Queries:** REST API endpoints returning historical rate series filtered by date range and currency pair (e.g., USD/IDR, JPY/IDR, EUR/IDR).
- **Invoice Rate Comparison:** Direct rate comparison API evaluating vendor-billed exchange rates against official treasury rates, returning variance percentage and triggering high-variance threshold flags.
- **Gap Detection & Audit Snapshot:** Automatic detection of missing rate publishing dates (e.g., bank holidays) and snapshot logging for finance audit compliance.

---

## System Architecture & Stack

- **Language & Framework:** Java 21 LTS / Spring Boot 3.3
- **Database:** PostgreSQL 18
- **Build Tool:** Maven
- **Target Deployment:** Ubuntu Server 24.04 LTS (Enterprise On-Premises, Jakarta Datacenter) via Systemd service execution.

![FX Compare Internal UI / API Integration](docs/images/dashboard-placeholder.png)

---

## Prerequisites & Local Setup

### 1. Requirements
- OpenJDK 21 LTS
- Maven 3.9+
- PostgreSQL 18

### 2. Database Initialization
Create the database and application user in local PostgreSQL:
```sql
CREATE DATABASE fxcompare_db;
CREATE USER fxcompare_app WITH ENCRYPTED PASSWORD 'local_secret';
GRANT ALL PRIVILEGES ON DATABASE fxcompare_db TO fxcompare_app;
```

### 3. Application Configuration
Update `src/main/resources/application.yml` or set environment variables:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fxcompare_db
    username: fxcompare_app
    password: local_secret
  jpa:
    hibernate:
      ddl-auto: validate

fxcompare:
  ingestion:
    cron: "0 30 17 * * MON-FRI" # 17:30 WIB post BI publication
    bi-api-url: "https://api.bi.go.id/v1/rates" # Enterprise Gateway
  threshold:
    alert-variance-percent: 1.50 # Flags invoices with >1.5% FX markup
```

### 4. Build and Run
```bash
# Build fat JAR
./mvnw clean package -DskipTests

# Run application
java -jar target/fxcompare-1.2.0.jar
```

---

## Production Deployment (Systemd on Ubuntu Server)

Deploy executable fat JAR to `/opt/fxcompare/app.jar` on corporate Jakarta datacenter instance.

1. Create service unit `/etc/systemd/system/fxcompare.service`:
```ini
[Unit]
Description=FX Compare Rates Service
After=syslog.target network.target postgresql.service

[Service]
User=fxapp
Group=corporate
WorkingDirectory=/opt/fxcompare
ExecStart=/usr/bin/java -Xms512m -Xmx2048m -jar /opt/fxcompare/app.jar
SuccessExitStatus=143
Restart=always
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://10.10.20.15:5432/fxcompare_prod
Environment=SPRING_DATASOURCE_USERNAME=fxcompare_prod_usr
Environment=SPRING_DATASOURCE_PASSWORD=file:/etc/fxcompare/db_pass.txt

[Install]
WantedBy=multi-user.target
```

2. Reload and start systemd daemon:
```bash
sudo systemctl daemon-reload
sudo systemctl enable fxcompare
sudo systemctl start fxcompare
sudo systemctl status fxcompare
```

---

## Operational Code Snapshot

Below is a representative snippet from the rate scheduler workaround implementation:

```java
package com.company.fxcompare.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.company.fxcompare.service.RateIngestionService;

@Component
public class ScheduledIngestionRunner {

    private final RateIngestionService ingestionService;

    public ScheduledIngestionRunner(RateIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    // Workaround for Spring Scheduling lacking dynamic cron evaluation without resetting task context
    @Scheduled(cron = "${fxcompare.ingestion.cron:0 30 17 * * MON-FRI}", zone = "Asia/Jakarta")
    public void executeDailyIngestion() {
        ingestionService.syncDailyBankIndonesiaRates();
        ingestionService.syncCommercialBankRates();
    }
}
```
