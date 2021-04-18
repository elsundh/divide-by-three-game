package com.game;

import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.game.api.PlayersController;
import com.game.api.event.JoinEvent;
import com.game.api.event.LeaveEvent;
import com.game.api.event.PlayEvent;
import com.game.config.SessionConfig;
import com.game.repository.GameRepository;

import reactor.core.publisher.Flux;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PlayersController.class)
@Import({ GameRepository.class, SessionConfig.class })
class ITTest {

	@Autowired
	private WebTestClient webClient;

	@BeforeEach
	public void setUp() {
		webClient = webClient.mutate().responseTimeout(Duration.ofMillis(600000)).build();
	}

	@Test
	void playTestEvents() throws JsonParseException, JsonMappingException, IOException {
		// log on
		FluxExchangeResult<String> logOnResult = webClient.post().uri("/log-on").exchange().expectStatus().isOk()
				.returnResult(String.class);

		final String sessionId = logOnResult.getResponseHeaders().get("Set-Cookie").get(0).split(";")[0]
				.replace("SESSION=", "");
		
		FluxExchangeResult<String> secondPlayerLogOn = webClient.post().uri("/log-on").exchange().expectStatus().isOk()
				.returnResult(String.class);
		
		final String secondPlayerSessionId = secondPlayerLogOn.getResponseHeaders().get("Set-Cookie").get(0).split(";")[0]
				.replace("SESSION=", "");
		// test event
		Flux<JoinEvent> joinEvent = webClient.get().uri("/game-event").cookie("SESSION", sessionId)
				.accept(MediaType.TEXT_EVENT_STREAM).exchange().returnResult(JoinEvent.class).getResponseBody();

		Assertions.assertEquals("join", joinEvent.blockFirst().getType());

		// test play
		webClient.post().uri("/play").cookie("SESSION", sessionId).exchange().returnResult(PlayEvent.class)
				.getResponseBody();

		Flux<PlayEvent> playEvent = webClient.get().uri("/game-event").cookie("SESSION", sessionId).exchange()
				.returnResult(PlayEvent.class).getResponseBody();

		Integer number = playEvent.blockFirst().getResultingNumber();

		// test play result is divided by 3
		webClient.post().uri("/play").cookie("SESSION", secondPlayerSessionId).exchange().expectStatus().isCreated();

		Flux<PlayEvent> playEvent2 = webClient.get().uri("/game-event").cookie("SESSION", sessionId).exchange().returnResult(PlayEvent.class)
				.getResponseBody();

		PlayEvent secondGameResult = playEvent2.blockFirst();

		int added;
		if (Math.addExact(number, 1) % 3 == 0) {
			added = 1;
		} else if ((number - 1) % 3 == 0) {
			added = -1;
		} else {
			added = 0;
		}

		Assertions.assertEquals((number + added) / 3, secondGameResult.getResultingNumber());

		// test log off
		webClient.post().uri("/log-off").cookie("SESSION", sessionId).exchange().expectStatus().isOk();

		Flux<LeaveEvent> leaveEvent = webClient.get().uri("/game-event").cookie("SESSION", secondPlayerSessionId).exchange()
				.returnResult(LeaveEvent.class).getResponseBody();

		Assertions.assertEquals("leave", leaveEvent.blockFirst().getType());

	}

}
