package igor;

import igor.model.Service;
import igor.util.Operation;
import igor.util.ServiceMessage;
import igor.util.StatusCode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClient;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class StatusChecker extends AbstractVerticle {

  public static final String ADDRESS = "status-checker";

  private WebClient client;

  @Override
  public void start(Future<Void> future) {
    this.client = WebClient.create(vertx);

    vertx.setPeriodic(1000 * 60, handle -> {
      runChecks();
    });
  }

  private void runChecks() {
    vertx.eventBus().send(ServiceRegistry.ADDRESS, new ServiceMessage(Operation.LIST), reply -> {
      if (reply.succeeded()) {
        ServiceMessage message = (ServiceMessage) reply.result().body();
        List<Service> services = message.getServices();
        services.stream()
          .forEach(service -> check(service.getUUID(), service));
      } else {
        throw new RuntimeException("FAILED TO GET A LIST OF SERVICES FROM " + ServiceRegistry.ADDRESS);
      }
    });
  }

  private void check(UUID id, Service service) {
    String url = service.getUrl();

    client.get(80, url, "/").send(response -> {
      if (response.succeeded() && response.result().statusCode() == StatusCode.OK.getValue()) {
        System.out.println(url + " " + response.result().statusCode());
        service.setStatus(StatusCode.UP);
      } else {
        // TODO: Improve error handling
        service.setStatus(StatusCode.DOWN);
      }

      service.setLastChecked(new Date());
      vertx.eventBus().send(ServiceRegistry.ADDRESS, new ServiceMessage(Operation.PUT, id, service));
    });
  }
}
