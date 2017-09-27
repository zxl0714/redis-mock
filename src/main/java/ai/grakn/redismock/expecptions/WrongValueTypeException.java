package ai.grakn.redismock.expecptions;

/**
 * Created by Xiaolu on 2015/4/22.
 */
public class WrongValueTypeException extends Exception {

    public WrongValueTypeException() {
        super();
    }

    public WrongValueTypeException(String message) {
        super(message);
    }
}
