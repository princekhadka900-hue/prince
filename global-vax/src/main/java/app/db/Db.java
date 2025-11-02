package app.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class Db {
  // For local dev we keep a file path. In shaded JAR, resources are read from classpath.
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
      // Seed unconditionally for demo; idempotent statements are fine in this sample
      runSqlResource(c, "/db/seed_sample.sql");
    } catch (Exception e) {
      throw new RuntimeException("DB init failed", e);
    }
  }

  private static void runSqlResource(Connection c, String resourcePath) throws Exception {
    try (InputStream in = Db.class.getResourceAsStream(resourcePath)) {
      if (in == null) throw new RuntimeException("Missing resource: " + resourcePath);
      String sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
          .lines().collect(Collectors.joining("\n"));
      for (String stmt : sql.split(";")) {
        String s = stmt.trim();
        if (!s.isEmpty()) {
          Statement st = c.createStatement();
          try {
            st.execute(s);
          } finally {
            st.close();
          }
        }
      }
    }
  }
}
