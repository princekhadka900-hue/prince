# Global Vaccination Initiatives – Milestone 3

This repository contains a minimal but complete Java (Spark) web application for COSC2803 Milestone 3:
- Full website code with HTML/CSS/JS
- Java API endpoints (MCV2 coverage, zero-dose reduction, metadata)
- Demo data (CSV) mimicking WHO/UNICEF indicators
- Usability testing materials (docs/)

## Quick Start

```bash
# Build
mvn -q -DskipTests package

# Run
java -jar target/global-vax-1.0.0.jar

# App will start at http://localhost:4567
```

### Endpoints
- `/` – Home
- `/vaccination.html` – Vaccination Data UI
- `/api/countries` – list of countries
- `/api/coverage?indicator=MCV2&country1=Australia&country2=Nepal&from=2019&to=2024`
- `/api/zeroDoseReduction?from=2019&to=2024`
- `/api/metadata` – definitions, revision year, source, last update

### Data
`src/main/resources/db/demo_coverage.csv`

### Notes
This uses CSV-backed demo data for easy marking. You can later swap to JDBC/H2 by loading from a table.
