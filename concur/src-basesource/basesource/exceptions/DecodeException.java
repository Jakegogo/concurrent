package basesource.exceptions;

public class DecodeException extends RuntimeException {

	private static final long serialVersionUID = 7375734754832836149L;

	public DecodeException() {
		super();
	}

	public DecodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DecodeException(String message) {
		super(message);
	}

	public DecodeException(Throwable cause) {
		super(cause);
	}

}
