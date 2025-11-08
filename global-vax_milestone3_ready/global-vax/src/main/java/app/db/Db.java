package app.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

public class Db {
  private static final String DB_PATH = System.getenv().getOrDefault(
      "DATABASE_URL", "src/main/resources/db/vax.db");

  static {
    try { Class.forName("org.sqlite.JDBC"); } 
    catch (Exception e) { throw new RuntimeException(e); }
  }

  public static Connection get() throws SQLException {
    return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
  }

  public static void init() {
    try (Connection c = get()) {
      runSqlResource(c, "/db/schema.sql");
      runSqlResource(c, "/db/seed_sample.sql");
    } catch (Exception e) {
      throw new RuntimeException("DB init failed", e);
    }
  }

  private static void runSqlResource(Connection c, String path) throws Exception {
    try (var in = Db.class.getResourceAsStream(path)) {
      if (in == null) throw new RuntimeException("Missing resource: " + path);
      String sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
          .lines().collect(Collectors.joining("
"));
      for (String stmt : sql.split(";")) {
        String s = stmt.trim();
        if (!s.isEmpty()) try (Statement st = c.createStatement()) { st.execute(s); }
      }
    }
  }
}
