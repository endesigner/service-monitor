package igor.util;

import igor.model.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServiceMessage {
  private final Operation operation;
  private final StatusCode statusCode;
  private final List<Integer> serviceIds;
  private final List<Service> services;

  public ServiceMessage(StatusCode statusCode, List<Service> serviceList) {
    this(
      null
      , statusCode
      , null
      , serviceList
    );
  }

  public ServiceMessage(StatusCode statusCode, Integer serviceId, Service service) {
    this(
      null
      , statusCode
      , new ArrayList<Integer>() {{ add(serviceId); }}
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

  public ServiceMessage(Operation operation) {
    this(
      operation
      , null
      , null
      , new ArrayList<Service>() {{ add(null); }}
      );
  }

  public ServiceMessage(Operation operation, Integer serviceId) {
    this(operation
      , null
      , new ArrayList<Integer>() {{ add(serviceId); }}
      , new ArrayList<Service>() {{ add(null); }}
      );
  }

  public ServiceMessage(StatusCode statusCode, int serviceId) {
    this(
      null
      , statusCode
      , new ArrayList<Integer>() {{ add(serviceId); }}
      , new ArrayList<Service>() {{ add(null); }}
      );
  }

  public ServiceMessage(Operation operation, int serviceId, Service service) {
    this(operation
      , null
      , new ArrayList<Integer>() {{ add(serviceId); }}
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


  public ServiceMessage(Operation operation, Integer[] serviceIds) {
    this(
      operation
      , null
      , Arrays.asList(serviceIds)
      , null
    );
  }

  public ServiceMessage(Operation operation, StatusCode statusCode, Integer serviceId, Service service) {
    this(
      operation
      , statusCode
      , new ArrayList<Integer>() {{ add(serviceId); }}
      , new ArrayList<Service>() {{ add(service); }}
      );
  }

  public ServiceMessage(Operation operation, StatusCode statusCode, List<Integer> serviceIds, List<Service> services) {
    this.statusCode = statusCode;
    this.serviceIds = serviceIds;
    this.services = services;
    this.operation = operation;
  }

  public StatusCode getStatusCode() {
    return statusCode;
  }

  public List<Integer> getServiceIds() {
    return serviceIds;
  }

  public List<Service> getServices() {
    return services;
  }

  public Operation getOperation() {
    return operation;
  }
}
