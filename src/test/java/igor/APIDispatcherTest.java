package igor;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class APIDispatcherTest {
  private Vertx vertx;

  @Before
  public void setup(TestContext testContext) {
    vertx = Vertx.vertx();

    vertx.deployVerticle(
      APIDispatcher.class.getName(),
      testContext.asyncAssertSuccess()
    );
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void whenReceivedResponse_thenSuccess(TestContext testContext) {
    Async async = testContext.async();
    WebClient client = WebClient.create(vertx);
    client.get(9090, "localhost", "/service")
      .send(response -> {
        System.out.println(response.result().body());
        testContext.assertTrue(response.toString().contains("Hello"));
        async.complete();
      });

//    int port = 9090;
//    vertx.createHttpClient()
//      .get(port, "localhost", "/service").
//      , response -> {
//        response.handler(responseBody -> {
//          testContext.assertTrue(responseBody.toString().contains("Hello"));
//          async.complete();
//        });
//      });
  }
}
