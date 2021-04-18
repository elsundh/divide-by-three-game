package com.game.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;

import com.game.event.Event;
import com.game.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PlayersController {
	
	private final GameRepository game;

	@PostMapping(path = "log-on")
	@ResponseStatus(code = HttpStatus.OK)
	public void registerPlayer(WebSession session) {
		game.joinGame(session);
		log.info("generated session id {}", session.getId());
	}
	
	@PostMapping(path = "log-off")
	@ResponseStatus(code = HttpStatus.OK)
	public void leaveGame(WebSession session) {
		game.stopGame(session);
	}
		
	@PostMapping(path = "/play")
	@ResponseStatus(code =  HttpStatus.CREATED)
	public void play(@RequestParam(required = false) Integer added, WebSession session) {
		log.info("playing with session id {}", session.getId());
		game.play(added, session);
	}
	
	@GetMapping(path = "/game-event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@ResponseBody
	public Flux<Event> getResult(WebSession session) {
		log.info("getting event from session id {}", session.getId());	
		return game.getResult(session);
	}

}
