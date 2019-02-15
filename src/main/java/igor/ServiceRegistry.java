package igor;

import igor.model.Service;
import igor.util.Operation;
import igor.util.ServiceMessage;
import igor.util.StatusCode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class ServiceRegistry extends AbstractVerticle {

  public static final String ADDRESS = "service-registry";

  private HashMap<Integer, Service> registry;

  public ServiceRegistry(JsonArray jsonArray) {
    this.registry = new HashMap<>();

    for (var i = 0; i < jsonArray.size(); i++) {
      registry.put(i, jsonArray.getJsonObject(i).mapTo(Service.class));
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
          List<Integer> serviceIds = serviceMessage.getServiceIds();
          if (serviceIds.size() > 1) {
            deleteAndRespond(message, serviceIds);
          } else {
            deleteAndRespond(message, serviceIds.get(0));
          }
          break;
        case PUT:
          updateAndRespond(message, serviceMessage.getServiceIds().get(0), serviceMessage.getServices().get(0));
          break;
        default:
          break;
      }
    });
  }

  private void rebuildIndexes() {
    HashMap<Integer, Service> newRegistry = new HashMap<>();

    Iterator<Integer> iterator = IntStream.range(0, registry.size()).boxed().iterator();

    registry.values()
      .forEach(service -> newRegistry.put(iterator.next(), service));

    this.registry = newRegistry;
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

  private void getAndRespond(Message message, Integer serviceId) {
    if (!registry.containsKey(serviceId)) {
      message.reply(new ServiceMessage(StatusCode.NOT_FOUND, serviceId));
      return;
    }

    message.reply(new ServiceMessage(StatusCode.OK, serviceId, registry.get(serviceId)));
  }

  private void addAndRespond(Message message, Service service) {
    Integer id = registry.size();
    registry.put(id, service);
    flushToDisc();
    message.reply(new ServiceMessage(StatusCode.CREATED, id, service));
  }

  private void deleteAndRespond(Message message, Integer serviceId) {
    if (!registry.containsKey(serviceId)) {
      message.reply(new ServiceMessage(StatusCode.NOT_FOUND, serviceId));
      return;
    }

    registry.remove(serviceId);
    flushToDisc();
    rebuildIndexes();
    message.reply(new ServiceMessage(StatusCode.NO_CONTENT, serviceId));
  }

  private void deleteAndRespond(Message message, List<Integer> serviceIds) {
    serviceIds.stream().forEach(serviceId -> {
      if (!registry.containsKey(serviceId)) return;
      registry.remove(serviceId);
    });

    flushToDisc();
    rebuildIndexes();

    Integer[] ids = serviceIds.toArray(new Integer[serviceIds.size()]);
    message.reply(new ServiceMessage(StatusCode.OK, ids));
  }

  private void updateAndRespond(Message message, Integer serviceId, Service service) {
    // This is a naive implementation on an update mechanism
    // Update may happen on a service with an index that has been deleted before the update is complete

    if (!registry.containsKey(serviceId)) {
      message.reply(new ServiceMessage(StatusCode.NOT_FOUND, serviceId));
      return;
    }

    registry.put(serviceId, service);
    flushToDisc();
    message.reply(new ServiceMessage(StatusCode.OK, serviceId, service));
  }
}