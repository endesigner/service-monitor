package igor.util;

import igor.model.Service;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.UUID;

public class ServiceMessageCodec implements MessageCodec<ServiceMessage, ServiceMessage> {
  private static final String STATUS_CODE = "statusCode";
  private static final String SERVICES = "service";
  private static final String SERVICE_IDS = "serviceIds";
  private static final String OPERATION = "operation";

  @Override
  public void encodeToWire(Buffer buffer, ServiceMessage serviceMessage) {
    JsonObject jsonToEncode = new JsonObject();
    jsonToEncode.put(STATUS_CODE, serviceMessage.getStatusCode());
    jsonToEncode.put(SERVICE_IDS, serviceMessage.getServiceIds());
    jsonToEncode.put(SERVICES, serviceMessage.getServices());
    jsonToEncode.put(OPERATION, serviceMessage.getOperation());
    String jsonToString = jsonToEncode.encode();

    int length = jsonToString.getBytes().length;
    // Write data into given buffer
    buffer.appendInt(length);
    buffer.appendString(jsonToString);
  }

  @Override
  public ServiceMessage decodeFromWire(int i, Buffer buffer) {
    // My custom message starting from this *position* of buffer
    int _pos = i;

    // Length of JSON
    int length = buffer.getInt(_pos);

    // Get JSON string by it`s length
    // Jump 4 because getInt() == 4 bytes
    String jsonStr = buffer.getString(_pos+=4, _pos+=length);
    JsonObject contentJson = new JsonObject(jsonStr);

    // Get fields
    StatusCode statusCode = StatusCode.valueOf(contentJson.getString(STATUS_CODE));
    List<UUID> serviceIds = (List<UUID>) contentJson.getJsonArray(SERVICE_IDS).getList();
    List<Service> services = (List<Service>) contentJson.getJsonArray(SERVICES).getList();
    Operation operation = Operation.valueOf(contentJson.getString(OPERATION));

    // We can finally create custom message object
    return new ServiceMessage(operation, statusCode, serviceIds, services);
  }

  @Override
  public ServiceMessage transform(ServiceMessage serviceMessage) {
    return serviceMessage;
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}