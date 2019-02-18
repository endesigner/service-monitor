package igor;

import igor.model.Service;
import igor.util.Operation;
import igor.util.ServiceMessage;
import igor.util.StatusCode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;

import java.util.*;
import java.util.UUID;


public class ServiceRegistry extends AbstractVerticle {

  public static final String ADDRESS = "service-registry";

  private HashMap<UUID, Service> registry;

  public ServiceRegistry(JsonArray jsonArray) {
    this.registry = new HashMap<>();

    Service service;
    for (var i = 0; i < jsonArray.size(); i++) {
      service = jsonArray.getJsonObject(i).mapTo(Service.class);
      registry.put(service.getUUID(), service);
    }
  }

  @Override
  public void start(Future<Void> future) {
    vertx.eventBus().consumer(ADDRESS, message -> {
      ServiceMessage serviceMessage = (ServiceMessage) message.body();

      switch (serviceMessage.getOperation()) {
        case LIST:
          listAndRespond(message);
          break;
        case GET:
          getAndRespond(message, serviceMessage.getServiceIds().get(0));
          break;
        case ADD:
          addAndRespond(message, serviceMessage.getServices().get(0));
          break;
        case DELETE:
          deleteAndRespond(message, serviceMessage.getServiceIds());
          break;
        case PUT:
          updateAndRespond(message, serviceMessage.getServices().get(0));
          break;
        default:
          break;
      }
    });
  }

  private void flushToDisc() {
    List<Service> services = new ArrayList<>(registry.values());
    ServiceMessage message = new ServiceMessage(Operation.FLUSH, services);
    vertx.eventBus().send(App.ADDRESS, message, reply -> {
      if (reply.succeeded()) {
        System.out.println(reply.result().body().toString());
      } else {
        System.out.println(reply.cause().getCause());
      }
    });
  }

  private void listAndRespond(Message message) {
    List<Service> registryList = new ArrayList<>(registry.values());
    message.reply(new ServiceMessage(StatusCode.OK, registryList));
  }

  private void getAndRespond(Message message, UUID serviceId) {
    if (!registry.containsKey(serviceId)) {
      message.reply(new ServiceMessage(StatusCode.NOT_FOUND, serviceId));
      return;
    }

    message.reply(new ServiceMessage(StatusCode.OK, serviceId, registry.get(serviceId)));
  }

  private void addAndRespond(Message message, Service service) {
    UUID id = service.getUUID();
    registry.put(id, service);
    flushToDisc();
    message.reply(new ServiceMessage(StatusCode.CREATED, id, service));
  }

  private void deleteAndRespond(Message message, List<UUID> serviceIds) {
    if (serviceIds.size() == 0) {
      message.reply(new ServiceMessage(StatusCode.NOT_FOUND));
      return;
    }

    serviceIds.stream().forEach(serviceId -> {
      if (!registry.containsKey(serviceId)) return;
      registry.remove(serviceId);
    });

    flushToDisc();

    UUID[] ids = serviceIds.toArray(new UUID[serviceIds.size()]);
    message.reply(new ServiceMessage(StatusCode.OK, ids));
  }

  private void updateAndRespond(Message message, Service service) {
    UUID id = service.getUUID();
    if (!registry.containsKey(id)) {
      message.reply(new ServiceMessage(StatusCode.NOT_FOUND, id));
      return;
    }

    registry.put(service.getUUID(), service);
    flushToDisc();
    message.reply(new ServiceMessage(StatusCode.OK, id, service));
  }
}