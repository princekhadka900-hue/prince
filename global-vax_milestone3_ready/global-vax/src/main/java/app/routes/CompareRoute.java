package app.routes;

import com.google.gson.Gson;
import spark.Request; import spark.Response;
import app.db.Db;
import java.sql.*; import java.util.*;

public class CompareRoute {
  private static final Gson gson = new Gson();

  public static Object compare(Request req, Response res) throws Exception {
    String antigen = param(req, "antigen", "MCV2");
    String countries = param(req, "countries", "Nepal,India");
    int start = Integer.parseInt(param(req, "start", "2019"));
    int end   = Integer.parseInt(param(req, "end",   "2024"));

    List<String> countryList = Arrays.stream(countries.split(","))
        .map(String::trim).filter(s -> !s.isEmpty()).toList();

    Map<String,List<Map<String,Object>>> out = new LinkedHashMap<>();
    String placeholders = String.join(",", countryList.stream().map(x -> "?").toList());

    String sql = String.format("""        SELECT country, year, coverage, denominator, revision_year, method_flag
        FROM coverage
        WHERE antigen=? AND country IN (%s) AND year BETWEEN ? AND ?
        ORDER BY country, year
      """, placeholders);

    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      int i = 1;
      ps.setString(i++, antigen);
      for (String ct : countryList) ps.setString(i++, ct);
      ps.setInt(i++, start); ps.setInt(i, end);

      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        String ct = rs.getString("country");
        out.putIfAbsent(ct, new ArrayList<>());
        Map<String,Object> row = new LinkedHashMap<>();
        row.put("year", rs.getInt("year"));
        row.put("coverage", rs.getObject("coverage"));
        row.put("denominator", rs.getObject("denominator"));
        row.put("revision_year", rs.getInt("revision_year"));
        row.put("method_flag", rs.getString("method_flag"));
        out.get(ct).add(row);
      }
    }
    res.type("application/json");
    return gson.toJson(Map.of(
        "antigen", antigen,
        "countries", countryList,
        "start", start,
        "end", end,
        "series", out,
        "source", "WHO/UNICEF (WUENIC)",
        "last_updated", 2025
    ));
  }

  private static String param(Request req, String key, String def) {
    String v = req.queryParams(key);
    return (v == null || v.isBlank()) ? def : v;
  }
}
