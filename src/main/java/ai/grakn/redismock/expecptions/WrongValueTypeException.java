package ai.grakn.redismock.expecptions;

/**
 * Created by Xiaolu on 2015/4/22.
 */
public class WrongValueTypeException extends RuntimeException {

    public WrongValueTypeException() {
        super();
    }

    public WrongValueTypeException(String message) {
        super(message);
    }
}
