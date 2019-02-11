package igor;

import igor.model.Service;
import igor.util.Operation;
import igor.util.StatusCode;
import igor.util.ServiceMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;

public class APIDispatcher extends AbstractVerticle {
  private Integer port;

  public APIDispatcher(Integer port) {
    this.port = port;
  }

  @Override
  public void start(Future<Void> future) {
    Router router = Router.router(vertx);
    router.route("/service*")
      .handler(BodyHandler.create()); // Handle reading of the request body
    router.route().failureHandler(failureContext -> {
      failureContext.response()
        .setStatusCode(failureContext.statusCode())
        .end(failureContext.failure().getMessage());
    });
    router.get("/service")
      .handler(this::listServices);
    router.get("/service/:id")
      .handler(this::getServices);
    router.post("/service")
      .handler(this::addService);
    router.delete("/service/:id")
      .handler(this::deleteService);
    router.put("/service/:id")
      .handler(this::updateService);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port, result -> {
        System.out.println(String.format("Listening on port %d", port));

        if (result.succeeded()) {
          future.complete();
        } else {
          future.fail(result.cause());
        }
      });
  }

  private void updateService(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    Integer _id = maybeID(id);
    String body = routingContext.getBodyAsString();

    ServiceMessage message = new ServiceMessage(Operation.PUT, _id, new JsonObject(body).mapTo(Service.class));

    vertx.eventBus().send(ServiceRegistry.ADDRESS, message, reply -> {
      ServiceMessage serviceMessage = (ServiceMessage) reply.result().body();

      if (reply.succeeded()) {
        response(routingContext, serviceMessage.getStatusCode(), serviceMessage.getServices());
      } else {
        routingContext.fail(reply.cause());
      }
    });
  }

  private void deleteService(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    Integer _id = maybeID(id);

    ServiceMessage message = new ServiceMessage(Operation.DELETE, _id);

    vertx.eventBus().send(ServiceRegistry.ADDRESS, message, reply -> {
      ServiceMessage serviceMessage = (ServiceMessage) reply.result().body();

      if (reply.succeeded()) {
        response(routingContext, serviceMessage.getStatusCode());
      } else {
        routingContext.fail(reply.cause());
      }
    });
  }

  private void addService(RoutingContext routingContext) {
    String body = routingContext.getBodyAsString();
    Service service = new JsonObject(body).mapTo(Service.class);

    ServiceMessage message = new ServiceMessage(Operation.ADD, service);

    vertx.eventBus().send(ServiceRegistry.ADDRESS, message, reply -> {
      ServiceMessage serviceMessage = (ServiceMessage) reply.result().body();

      if (reply.succeeded()) {
        response(routingContext, serviceMessage.getStatusCode(), serviceMessage.getServices().get(0));
      } else {
        routingContext.fail(reply.cause());
      }
    });
  }

  private void getServices(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    Integer _id = maybeID(id);

    ServiceMessage message = new ServiceMessage(Operation.GET, _id);

    vertx.eventBus().send(ServiceRegistry.ADDRESS, message, reply -> {
      ServiceMessage serviceMessage = (ServiceMessage) reply.result().body();

      if (reply.succeeded()) {
        response(routingContext, serviceMessage.getStatusCode(), serviceMessage.getServices());
      } else {
        routingContext.fail(reply.cause());
      }
    });
  }

  private void listServices(RoutingContext routingContext) {
    vertx.eventBus().send(ServiceRegistry.ADDRESS, new ServiceMessage(Operation.LIST), reply -> {
      ServiceMessage message = (ServiceMessage) reply.result().body();

      if (reply.succeeded()) {
        response(routingContext, message.getStatusCode(), message.getServices());
      } else {
        routingContext.fail(reply.cause());
      }
    });
  }

  private Integer maybeID(String id) {
    Integer result = -1;
    try { result = Integer.valueOf(id); } catch (Exception e) {}
    return result;
  }

  private void response(RoutingContext routingContext, StatusCode statusCode, List<Service> services) {
    response(routingContext, statusCode, Json.encode(services));
  }

  private void response(RoutingContext routingContext, StatusCode statusCode, Service service) {
    response(routingContext, statusCode, Json.encode(service));
  }

  private void response(RoutingContext routingContext, StatusCode statusCode) {
    response(routingContext, statusCode, new JsonObject());
  }

  private void response(RoutingContext routingContext, StatusCode statusCode, JsonObject payload) {
    response(routingContext, statusCode, payload.encodePrettily());
  }

  private void response(RoutingContext routingContext, StatusCode statusCode, String payload) {
    routingContext.response()
      .putHeader("content-type", "application/json")
      .setStatusCode(statusCode.getValue())
      .end(payload);
  }
}