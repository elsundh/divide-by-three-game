package com.game.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.WebSession;

import com.game.event.Event;
import com.game.model.Game;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Repository
@Slf4j
public class GameRepository {
	private static final String GAME_ID_ATTRIBUTE = "game-id";

	private final Map<String, Game> games = new HashMap<>();

	public void joinGame(WebSession session) {
		final Game game = games.values().stream().filter(Game::canJoin).findAny()
				.orElse(new Game(Sinks.many().replay().latest()));
		game.addPlayer();
		
		log.info("adding new player to game {}", game.getId().toString());
		
		session.getAttributes().put(GAME_ID_ATTRIBUTE, game.getId().toString());
		games.put(game.getId().toString(), game);
	}

	public void play(Integer added, WebSession session) {
		games.get(session.getAttribute(GAME_ID_ATTRIBUTE)).play(added);
	}

	public Flux<Event> getResult(WebSession session) {
		return games.get(session.getAttribute(GAME_ID_ATTRIBUTE)).getResult();
	}

	public void stopGame(WebSession session) {
		if(session.getAttribute(GAME_ID_ATTRIBUTE) == null) {
			return;
		}
		log.info("stopping game {}", session.getAttribute(GAME_ID_ATTRIBUTE).toString());
		games.get(session.getAttribute(GAME_ID_ATTRIBUTE)).clear();
		session.getAttributes().remove(GAME_ID_ATTRIBUTE);
		
	}
	
	@Scheduled(fixedDelayString = "${clean-finished-games-ms:30000}")
	public void cleanGames() {
		log.info("cleaning finished games");
		games.values().removeIf(v -> !v.isPlaying());
	}

}
