package igor;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.TestSuite;
import io.vertx.ext.unit.report.ReportOptions;
import org.junit.Test;

// TODO write async test https://vertx.io/docs/vertx-unit/java/
public class APIDispatcherTest {
  private Vertx vertx;

  @Test
  public void runTest() {
    vertx = Vertx.vertx();
    TestSuite suite = TestSuite.create("Test endpoints");

    suite.test("my_test_case", context -> {
      Async async = context.async();
      HttpClient client = vertx.createHttpClient();
      HttpClientRequest req = client.get(9090, "localhost", "/");
      req.exceptionHandler(err -> context.fail(err.getMessage()));
      req.handler(resp -> {
        System.out.println("HI@@@@@@@");
        context.assertEquals(200, resp.statusCode());
        async.complete();
      });
      req.end();
    });

    suite.run(new TestOptions().addReporter(new ReportOptions().setTo("console")));
  }
}