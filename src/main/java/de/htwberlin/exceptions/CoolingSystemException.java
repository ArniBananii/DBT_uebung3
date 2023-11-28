package de.htwberlin.exceptions;

/**
 * @author Ingo Classen
 */
@SuppressWarnings("serial")
public class CoolingSystemException extends RuntimeException {

  /**
   * Erzeugt eine ServiceException.
   */
  public CoolingSystemException() {
  }

  /**
   * Erzeugt eine ServiceException mit einer Nachricht.
   * 
   * @param msg
   *          - die Nachricht
   */
  public CoolingSystemException(String msg) {
    super(msg);
  }

  /**
   * Erzeugt eine ServiceException und verweist auf ein Throwable t.
   * 
   * @param t
   *          - das Throwable.
   */
  public CoolingSystemException(Throwable t) {
    super(t);
  }

}
