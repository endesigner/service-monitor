package igor.util;

import igor.model.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ServiceMessage {
  private final Operation operation;
  private final StatusCode statusCode;
  private final List<UUID> serviceIds;
  private final List<Service> services;

  public ServiceMessage(StatusCode statusCode, List<Service> serviceList) {
    this(
      null
      , statusCode
      , null
      , serviceList
    );
  }

  public ServiceMessage(StatusCode statusCode, UUID serviceId, Service service) {
    this(
      null
      , statusCode
      , new ArrayList<UUID>() {{ add(serviceId); }}
      , new ArrayList<Service>() {{ add(service); }}
      );
  }

  public ServiceMessage(Operation operation, Service service) {
    this(
      operation
      , null
      , null
      , new ArrayList<Service>() {{ add(service); }}
    );
  }

  public ServiceMessage(StatusCode statusCode) {
    this(
      null
      , statusCode
      , null
      , new ArrayList<Service>() {{ add(null); }}
    );
  }

  public ServiceMessage(Operation operation) {
    this(
      operation
      , null
      , null
      , new ArrayList<Service>() {{ add(null); }}
      );
  }

  public ServiceMessage(Operation operation, UUID serviceId) {
    this(operation
      , null
      , new ArrayList<UUID>() {{ add(serviceId); }}
      , new ArrayList<Service>() {{ add(null); }}
      );
  }

  public ServiceMessage(StatusCode statusCode, UUID serviceId) {
    this(
      null
      , statusCode
      , new ArrayList<UUID>() {{ add(serviceId); }}
      , new ArrayList<Service>() {{ add(null); }}
      );
  }

  public ServiceMessage(Operation operation, UUID serviceId, Service service) {
    this(operation
      , null
      , new ArrayList<UUID>() {{ add(serviceId); }}
      , new ArrayList<Service>() {{ add(service); }}
      );
  }

  public ServiceMessage(Operation operation, List<Service> services) {
    this(
      operation
      , null
      , null
      , services
    );
  }

  public ServiceMessage(StatusCode statusCode, UUID[] serviceIds) {
    this(
      null
      , statusCode
      , Arrays.asList(serviceIds)
      , null
    );
  }

  public ServiceMessage(Operation operation, UUID[] serviceIds) {
    this(
      operation
      , null
      , Arrays.asList(serviceIds)
      , null
    );
  }

  public ServiceMessage(Operation operation, StatusCode statusCode, UUID serviceId, Service service) {
    this(
      operation
      , statusCode
      , new ArrayList<UUID>() {{ add(serviceId); }}
      , new ArrayList<Service>() {{ add(service); }}
      );
  }

  public ServiceMessage(Operation operation, StatusCode statusCode, List<UUID> serviceIds, List<Service> services) {
    this.statusCode = statusCode;
    this.serviceIds = serviceIds;
    this.services = services;
    this.operation = operation;
  }

  public StatusCode getStatusCode() {
    return statusCode;
  }

  public List<UUID> getServiceIds() {
    return serviceIds;
  }

  public List<Service> getServices() {
    return services;
  }

  public Operation getOperation() {
    return operation;
  }
}
