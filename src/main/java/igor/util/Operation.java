package igor.util;

public enum Operation {
  ADD,
  GET,
  PUT,
  DELETE,

  // Custom operations for internal signaling
  LIST,
  FLUSH
}