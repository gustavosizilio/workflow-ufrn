package org.domain.exception;

import java.util.List;

public class ValidationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> errors;
	public ValidationException(List<String> errors) {
		this.errors = errors;
	}
	public List<String> getErrors() {
		return errors;
	}
}
