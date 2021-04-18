package com.game.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.game.exception.GameException;

@ControllerAdvice
public class GameExceptionHandler {
	
	@ExceptionHandler(GameException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST )
	public String gameException(GameException e) {
		return e.getMessage();
	}

}
