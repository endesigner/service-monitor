package igor;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.TestSuite;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

// TODO write async test https://vertx.io/docs/vertx-unit/java/
public class APIDispatcherTest {
  private Vertx vertx;

  @Test
  public void runTest() {
    TestSuite suite = TestSuite.create("the_test_suite");
    suite.test("my_test_case", context -> {
      String s = "value";
      context.assertEquals("value", s);
    });
    suite.run();
  }
}