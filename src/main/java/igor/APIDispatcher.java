package igor;

import igor.model.Service;
import igor.util.Operation;
import igor.util.ServiceMessage;
import igor.util.StatusCode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.*;

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
    router.delete("/service")
      .handler(this::deleteServices);
    router.delete("/service/:id")
      .handler(this::deleteService);
    router.put("/service/:id")
      .handler(this::updateService);

    Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("x-requested-with");
    allowedHeaders.add("Access-Control-Allow-Origin");
    allowedHeaders.add("origin");
    allowedHeaders.add("Content-Type");
    allowedHeaders.add("accept");

    Set<HttpMethod> allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.GET);
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.DELETE);
    allowedMethods.add(HttpMethod.OPTIONS);
    allowedMethods.add(HttpMethod.PUT);

    router.route().handler(CorsHandler.create("*")
      .allowedHeaders(allowedHeaders)
      .allowedMethods(allowedMethods));


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

  private void deleteServices(RoutingContext routingContext) {
    JsonArray body = routingContext.getBodyAsJsonArray();
    List<Integer> _ids = body.getList();
    Integer[] ids = _ids.toArray(new Integer[_ids.size()]);

    ServiceMessage message = new ServiceMessage(Operation.DELETE, ids);

    vertx.eventBus().send(ServiceRegistry.ADDRESS, message, reply -> {
      ServiceMessage serviceMessage = (ServiceMessage) reply.result().body();

      if (reply.succeeded()) {
        System.out.println(serviceMessage.getStatusCode());
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
    System.out.println("LIST");
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

  private void response(RoutingContext routingContext, StatusCode statusCode, JsonArray payload) {
    response(routingContext, statusCode, payload.encodePrettily());
  }

  private void response(RoutingContext routingContext, StatusCode statusCode, String payload) {
    routingContext.response()
      .putHeader("content-type", "application/json")
      .putHeader("Access-Control-Allow-Origin", "*")
      .setChunked(true)
      .setStatusCode(statusCode.getValue())
      .end(payload);
  }
}