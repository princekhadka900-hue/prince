# Global Vaccination Initiatives (COSC2803) — Milestone 2

Work-in-progress Java web app implementing **Level 1 (GREEN)** and **Level 2 (ORANGE)** tasks:
- GREEN: Core tiles (DTP3, MCV2, Zero-dose) with **latest year + revision year**, visible **source**.
- ORANGE: Country comparison by **antigen + year range**, with **definitions/method notes** and export placeholder.

## Tech
- Java 21, Maven, SparkJava, SQLite (JDBC)
- Runs in **VS Code** or **GitHub Codespaces** (course supported)

## Run (local or Codespaces)
```bash
mvn clean package
java -jar target/global-vax-1.0.0.jar
# open http://localhost:8080
```
### API
- `GET /api/health` → `ok`
- `GET /api/indicators` → latest global tiles (DTP3/MCV2/Zero-dose)
- `GET /api/compare?antigen=MCV2&countries=Nepal,India&start=2019&end=2024` → series JSON

## Data
- Uses demo `schema.sql` + `seed_sample.sql` (small synthetic sample).
- Real WUENIC snapshot to be integrated for M3 (DB < 100 MB).
- **Source must appear on every chart/export**: WHO/UNICEF (WUENIC).

## Notes
- Local DB file `src/main/resources/db/vax.db` is **ignored** by Git.
- Ensure **Last updated** and **revision year** are shown with each view.
- See `src/main/resources/public/index.html` for UI and definitions toggle.

## Team
- Names: _TBD_
- Classroom repo: _TBD_
