package igor.model;

import igor.util.StatusCode;

import java.util.Date;
import java.util.UUID;

public class Service {
  private UUID uuid;
  private String name;
  private String url;
  private StatusCode lastStatus;
  private Date lastChecked;

  public Service(String name, String url, StatusCode status, Date date) {
    this.name = name;
    this.url = url;
    this.lastStatus = status;
    this.lastChecked = date;
  }

  public Service() {
    this.uuid = UUID.randomUUID();
  }

  public Service(String url) {
    this.url = url;
  }

  public Date getLastChecked() { return lastChecked; }
  public String getName() { return name; }
  public String getUrl() {
    return url;
  }
  public UUID getUUID() {return uuid; }
  public StatusCode getLastStatus() {return lastStatus; }

  public void setUrl(String url) { this.url = url; }
  public void setStatus(StatusCode status) { this.lastStatus = status; }
  public void setLastChecked(Date date) { this.lastChecked = date; }
  public void setUUID(UUID id) { this.uuid = id; }

  public void setStatus(String status) {
    StatusCode result;
    try {
      result = StatusCode.valueOf(status);
    } catch (Exception e) {
      result = StatusCode.FAIL;
    }

    this.lastStatus = result;
  }

}
