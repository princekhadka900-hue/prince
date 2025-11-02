package app.routes;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import app.db.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompareRoute {
  private static final Gson gson = new Gson();

  public static Object compare(Request req, Response res) throws Exception {
    String antigen   = param(req, "antigen", "MCV2");
    String countries = param(req, "countries", "Nepal,India");
    int start        = Integer.parseInt(param(req, "start", "2019"));
    int end          = Integer.parseInt(param(req, "end",   "2024"));

    // Build country list (Java 11 friendly)
    List<String> countryList = new ArrayList<String>();
    for (String ct : countries.split(",")) {
      String t = ct.trim();
      if (!t.isEmpty()) countryList.add(t);
    }

    // Build placeholders (?, ?, ?) for IN clause
    StringBuilder ph = new StringBuilder();
    for (int i = 0; i < countryList.size(); i++) {
      if (i > 0) ph.append(",");
      ph.append("?");
    }
    String placeholders = ph.toString();

    String sql =
        "SELECT country, year, coverage, denominator, revision_year, method_flag " +
        "FROM coverage " +
        "WHERE antigen=? AND country IN (" + placeholders + ") AND year BETWEEN ? AND ? " +
        "ORDER BY country, year";

    Map<String, List<Map<String, Object>>> series = new LinkedHashMap<String, List<Map<String, Object>>>();

    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      int i = 1;
      ps.setString(i++, antigen);
      for (String ct : countryList) ps.setString(i++, ct);
      ps.setInt(i++, start);
      ps.setInt(i,   end);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          String ct = rs.getString("country");
          if (!series.containsKey(ct)) series.put(ct, new ArrayList<Map<String, Object>>());

          Map<String, Object> row = new LinkedHashMap<String, Object>();
          row.put("year",          Integer.valueOf(rs.getInt("year")));
          Object cov = rs.getObject("coverage");
          if (cov != null) row.put("coverage", cov);
          Object den = rs.getObject("denominator");
          if (den != null) row.put("denominator", den);
          row.put("revision_year", Integer.valueOf(rs.getInt("revision_year")));
          String mf = rs.getString("method_flag");
          if (mf != null) row.put("method_flag", mf);

          series.get(ct).add(row);
        }
      }
    }

    Map<String, Object> out = new LinkedHashMap<String, Object>();
    out.put("antigen", antigen);
    out.put("countries", countryList);
    out.put("start", Integer.valueOf(start));
    out.put("end", Integer.valueOf(end));
    out.put("series", series);
    out.put("source", "WHO/UNICEF (WUENIC)");
    out.put("last_updated", Integer.valueOf(2025));

    res.type("application/json");
    return gson.toJson(out);
  }

  private static String param(Request req, String key, String def) {
    String v = req.queryParams(key);
    return (v == null || v.trim().isEmpty()) ? def : v;
  }
}
