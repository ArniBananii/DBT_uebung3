package de.htwberlin.exceptions;

/**
 * @author Ingo Classen
 */
@SuppressWarnings("serial")
public class DataException extends RuntimeException {

  /**
   * Erzeugt eine DataException.
   */
  public DataException() {
  }

  /**
   * Erzeugt eine DataException mit einer Nachricht.
   * 
   * @param msg
   *          - die Nachricht
   */
  public DataException(String msg) {
    super(msg);
  }

  /**
   * Erzeugt eine DataException und verweist auf ein Throwable t.
   * 
   * @param t
   *          - das Throwable.
   */
  public DataException(Throwable t) {
    super(t);
  }

}
