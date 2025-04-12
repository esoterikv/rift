package dev.esoterik.rift.lock;

public class RetryingException extends RuntimeException {

  private final int retryCount;

  public RetryingException(final int retryCount) {
    super("Retrying for the %d time".formatted(retryCount));
    this.retryCount = retryCount;
  }

  public int getRetryCount() {
    return retryCount;
  }
}
