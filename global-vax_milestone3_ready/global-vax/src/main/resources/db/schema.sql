PRAGMA journal_mode=WAL;

CREATE TABLE IF NOT EXISTS indicator_global_latest (
  indicator TEXT PRIMARY KEY,
  value REAL NOT NULL,
  year INTEGER NOT NULL,
  revision_year INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS coverage (
  country TEXT NOT NULL,
  region TEXT,
  antigen TEXT NOT NULL,
  year INTEGER NOT NULL,
  coverage REAL,
  denominator INTEGER,
  method_flag TEXT,
  revision_year INTEGER NOT NULL,
  PRIMARY KEY (country, antigen, year)
);

CREATE INDEX IF NOT EXISTS idx_cov_antigen ON coverage(antigen);
CREATE INDEX IF NOT EXISTS idx_cov_country ON coverage(country);
