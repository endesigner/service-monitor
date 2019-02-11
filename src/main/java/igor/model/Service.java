package igor.model;

import igor.util.StatusCode;

public class Service {
  private String url;
  private StatusCode lastStatus;

  public Service(String url, StatusCode status) {
    this.url = url;
    this.lastStatus = status;
  }

  public Service() {}

  public Service(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public StatusCode getLastStatus() {
    return lastStatus;
  }

  public void setStatus(String status) {
    StatusCode result;
    try {
      result = StatusCode.valueOf(status);
    } catch (Exception e) {
      result = StatusCode.FAIL;
    }

    this.lastStatus = result;
  }

  public void setStatus(StatusCode status) {
    this.lastStatus = status;
  }
}
