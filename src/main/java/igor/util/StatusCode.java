package igor.util;

public enum StatusCode {
  NOT_FOUND(404),
  OK(200),
  CREATED(201),
  NO_CONTENT(204),
  FAIL(0);

  private final int statusCode;

  StatusCode(int i) {
    this.statusCode = i;
  }

  public int getValue() { return statusCode; }
}
