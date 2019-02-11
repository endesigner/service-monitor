package igor;

import igor.model.Service;
import igor.util.Operation;
import igor.util.ServiceMessage;
import igor.util.ServiceMessageCodec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;

import java.util.List;

public class App extends AbstractVerticle {

  public static final String ADDRESS = "app";

  public static void main(String[] args) {
    new App();
  }

  public App() {
    Vertx.vertx()
      .deployVerticle(this);
  }

  @Override
  public void start(Future<Void> future) {
    // The registry of services
    String jsonFile = config().getString("jsonFile", "services.json");
    vertx.fileSystem().readFile(jsonFile, result -> {
      if (result.succeeded()) {
        Buffer buffer = result.result();
        JsonArray jsonArray = new JsonArray(buffer);
        ServiceRegistry registry = new ServiceRegistry(jsonArray);
        vertx.deployVerticle(registry);
      } else {
        throw new RuntimeException(result.cause());
      }
    });

    // Handle persisting of services to disk
    vertx.eventBus().consumer(ADDRESS, message -> {
      ServiceMessage serviceMessage = (ServiceMessage) message.body();

      if (!serviceMessage.getOperation().equals(Operation.FLUSH)) {
        message.fail(-1, "Here we listen for FLUSHES!");
        return;
      }

      List<Service> serviceList = serviceMessage.getServices();
      JsonArray jsonArray = new JsonArray(serviceList);

      vertx.fileSystem().writeFile(jsonFile, jsonArray.toBuffer(), result -> {
        if (result.succeeded()) {
          message.reply("FLUSHED!");
        } else {
          message.fail(-1, "An error occurred while flushing... ");
        }
      });
    });

    // Status checker
    vertx.deployVerticle(new StatusChecker());

    // API handlers
    Integer port = config().getInteger("http.port", 9090);
    vertx.deployVerticle(new APIDispatcher(port));

    // Register custom codec to be more flexible about what data we can pass around
    vertx.eventBus()
      .registerDefaultCodec(ServiceMessage.class, new ServiceMessageCodec());
  }
}