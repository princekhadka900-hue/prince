package app;

import static spark.Spark.*;

import app.db.Db;
import app.routes.CompareRoute;
import app.routes.IndicatorsRoute;

public class App {
  public static void main(String[] args) {
    port(8080);
    threadPool(8);

    Db.init();

    staticFiles.location("/public");

    get("/api/health", (req,res)->"ok");
    get("/api/indicators", IndicatorsRoute::list);
    get("/api/compare",    CompareRoute::compare);

    after((req,res) -> res.header("Access-Control-Allow-Origin", "*"));
  }
}
