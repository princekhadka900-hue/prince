package app.routes;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import app.db.Db;
import java.sql.*;
import java.util.*;

public class IndicatorsRoute {
  private static final Gson gson = new Gson();

  public static Object list(Request req, Response res) throws Exception {
    String[] keys = {"DTP3","MCV2","ZERO_DOSE"};
    Map<String,Object> out = new LinkedHashMap<>();
    try (Connection c = Db.get()) {
      for (String k : keys) {
        try (PreparedStatement ps = c.prepareStatement(
            "SELECT value, year, revision_year FROM indicator_global_latest WHERE indicator=?")) {
          ps.setString(1, k);
          try (ResultSet rs = ps.executeQuery()) {
            Map<String,Object> o = new LinkedHashMap<>();
            if (rs.next()) {
              o.put("value", rs.getDouble("value"));
              o.put("year", rs.getInt("year"));
              o.put("revision_year", rs.getInt("revision_year"));
            } else {
              o.put("value", null); o.put("year", null); o.put("revision_year", null);
            }
            o.put("indicator", k);
            o.put("source", "WHO/UNICEF (WUENIC)");
            out.put(k, o);
          }
        }
      }
    }
    res.type("application/json");
    return gson.toJson(out);
  }
}
