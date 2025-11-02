DELETE FROM indicator_global_latest;
INSERT INTO indicator_global_latest(indicator,value,year,revision_year) VALUES
('DTP3', 84.0, 2024, 2025),
('MCV2', 71.0, 2024, 2025),
('ZERO_DOSE', 14700000, 2024, 2025);

DELETE FROM coverage;
INSERT INTO coverage(country,region,antigen,year,coverage,denominator,method_flag,revision_year) VALUES
('Nepal','South Asia','MCV2',2019,77,700000,'observed',2025),
('Nepal','South Asia','MCV2',2024,85,720000,'observed',2025),
('India','South Asia','MCV2',2019,71,27000000,'modelled',2025),
('India','South Asia','MCV2',2024,83,28000000,'modelled',2025);
