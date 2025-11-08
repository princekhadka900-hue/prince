package app;

import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class App {

    static class Coverage {
        String country;
        String indicator; // e.g., MCV2, ZERO_DOSE
        int year;
        Double value;

        public Coverage(String country, String indicator, int year, Double value) {
            this.country = country;
            this.indicator = indicator;
            this.year = year;
            this.value = value;
        }
    }

    private static final List<Coverage> DATA = new ArrayList<>();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        port(getHerokuAssignedPort());
        staticFiles.location("/public");

        // Load demo data from CSV in resources/db/demo_coverage.csv
        loadCsv("/db/demo_coverage.csv");

        // Health
        get("/api/health", (req, res) -> {
            res.type("application/json");
            return "{\"status\":\"ok\"}";
        });

        // List countries
        get("/api/countries", (req, res) -> {
            res.type("application/json");
            Set<String> countries = DATA.stream().map(c -> c.country).collect(Collectors.toCollection(TreeSet::new));
            return gson.toJson(countries);
        });

        // Coverage by indicator and optional filters
        get("/api/coverage", (req, res) -> {
            res.type("application/json");
            String indicator = Optional.ofNullable(req.queryParams("indicator")).orElse("MCV2").toUpperCase();
            String c1 = req.queryParams("country1");
            String c2 = req.queryParams("country2");
            String y1s = req.queryParams("from");
            String y2s = req.queryParams("to");

            Integer y1 = y1s!=null ? Integer.parseInt(y1s) : 2019;
            Integer y2 = y2s!=null ? Integer.parseInt(y2s) : 2024;

            List<Coverage> filtered = DATA.stream()
                .filter(r -> r.indicator.equalsIgnoreCase(indicator))
                .filter(r -> r.year >= y1 && r.year <= y2)
                .filter(r -> (c1==null || r.country.equalsIgnoreCase(c1)) || (c2==null || r.country.equalsIgnoreCase(c2)) || (c1==null && c2==null))
                .collect(Collectors.toList());

            return gson.toJson(filtered);
        });

        // Zero-dose reduction (delta between from and to year)
        get("/api/zeroDoseReduction", (req, res) -> {
            res.type("application/json");
            String y1s = req.queryParams("from");
            String y2s = req.queryParams("to");
            int y1 = y1s!=null ? Integer.parseInt(y1s) : 2019;
            int y2 = y2s!=null ? Integer.parseInt(y2s) : 2024;

            Map<String, Double> byCountryStart = new HashMap<>();
            Map<String, Double> byCountryEnd = new HashMap<>();

            DATA.stream()
                .filter(r -> r.indicator.equalsIgnoreCase("ZERO_DOSE"))
                .forEach(r -> {
                    if (r.year==y1) byCountryStart.put(r.country, r.value);
                    if (r.year==y2) byCountryEnd.put(r.country, r.value);
                });

            // reduction = start - end (positive is reduction)
            Map<String, Double> reduction = new HashMap<>();
            for (String country : byCountryStart.keySet()) {
                if (byCountryEnd.containsKey(country)) {
                    double start = byCountryStart.get(country);
                    double end = byCountryEnd.get(country);
                    reduction.put(country, start - end);
                }
            }
            // Sort desc and return
            List<Map.Entry<String, Double>> sorted = reduction.entrySet().stream()
                    .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                    .collect(Collectors.toList());
            return gson.toJson(sorted);
        });

        // Metadata: indicator definitions and revision year
        get("/api/metadata", (req, res) -> {
            res.type("application/json");
            Map<String,Object> meta = new LinkedHashMap<>();
            meta.put("definitions", Map.of(
                "MCV2", "Measles-containing vaccine second dose (coverage %)",
                "ZERO_DOSE", "Children who have not received any routine vaccine (count/%)"
            ));
            meta.put("revisionYear", 2024);
            meta.put("source", "WHO/UNICEF & Gavi (demo dataset)");
            meta.put("lastUpdate", "2025-10-15");
            return gson.toJson(meta);
        });

        // Simple HTML page routes (optional if serving static files only)
        get("/vaccination-data", (req, res) -> {
            res.type("text/html");
            return new String(Objects.requireNonNull(App.class.getResourceAsStream("/public/vaccination.html")).readAllBytes(), StandardCharsets.UTF_8);
        });
    }

    private static void loadCsv(String resourcePath) {
        try (InputStream is = App.class.getResourceAsStream(resourcePath);
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            // header: country,indicator,year,value
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue;
                String country = parts[0].trim();
                String indicator = parts[1].trim();
                int year = Integer.parseInt(parts[2].trim());
                Double value = parts[3].isEmpty() ? null : Double.parseDouble(parts[3].trim());
                DATA.add(new Coverage(country, indicator, year, value));
            }
        } catch (Exception e) {
            System.err.println("Failed to load CSV: " + e.getMessage());
        }
    }

    private static int getHerokuAssignedPort() {
        String port = System.getenv("PORT");
        if (port != null) {
            return Integer.parseInt(port);
        }
        return 4567;
    }
}
