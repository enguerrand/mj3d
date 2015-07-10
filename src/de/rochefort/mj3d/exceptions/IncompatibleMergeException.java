package de.rochefort.mj3d.exceptions;

public class IncompatibleMergeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public IncompatibleMergeException() {
		super();
	}

	public IncompatibleMergeException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IncompatibleMergeException(String message, Throwable cause) {
		super(message, cause);
	}

	public IncompatibleMergeException(String message) {
		super(message);
	}

	public IncompatibleMergeException(Throwable cause) {
		super(cause);
	}
	

}
