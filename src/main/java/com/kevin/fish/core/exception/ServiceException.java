package com.kevin.fish.core.exception;

/**
 * 统一异常
 * @author Kevin Yu
 */
public class ServiceException extends RuntimeException {

	private Integer code = 400;

	public Integer getCode() {
		return code;
	}

	public ServiceException() {
	}

	public ServiceException(Throwable e) {
		super(e);
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Integer code, String message) {
		super(message);
		this.code = code;
	}

	public ServiceException(String message, Throwable e) {
		super(message, e);
	}
}
