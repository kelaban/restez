package errors;

/**
 * This is a marker class for serialization exceptions. Sometimes it
 * when handling api exceptions it is useful to know if the exception
 * came from serialization or something else
 *
 */
public class SerializationException extends BadRequestException {

  public SerializationException(String message) {
    super(message);
  }

  public SerializationException(String message, Throwable cause) {
    super(message, cause);
  }

}
