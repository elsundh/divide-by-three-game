package com.game.model;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.game.event.Event;
import com.game.event.JoinEvent;
import com.game.event.LeaveEvent;
import com.game.event.PlayEvent;
import com.game.event.WinEvent;

import reactor.core.publisher.Sinks;

class DivideByThreeGameFunctionalityTests {

	private Game game = new Game(Sinks.many().replay().latest());

	@Test
	void joinGame() {
		game.addPlayer();
		Event event = game.getResult().blockFirst();
		Assertions.assertTrue(event instanceof JoinEvent);
	}

	@Test
	void play() throws JsonParseException, JsonMappingException, IOException {

		game.play(1);

		Event event = game.getResult().blockFirst();

		Assertions.assertTrue(event instanceof PlayEvent);

		final Integer number = ((PlayEvent) event).getResultingNumber();

		int added;
		if ((number + 1) % 3 == 0) {
			added = 1;
		} else if ((number - 1) % 3 == 0) {
			added = -1;
		} else {
			added = 0;
		}

		game.play(added);

		PlayEvent dividedNumber = (PlayEvent) game.getResult().blockFirst();
		Assertions.assertEquals((number + 1) / 3, dividedNumber.getResultingNumber());
	}
	
	@Test
	void winGame() {
		ReflectionTestUtils.setField(game, "latestNumber", 3);
		game.play(0);
		
		Event event = game.getResult().blockFirst();
		Assertions.assertTrue(event instanceof WinEvent);
	}
	
	@Test
	void leaveGame() {
		game.clear();
		Event event = game.getResult().blockFirst();
		Assertions.assertTrue(event instanceof LeaveEvent);
	}

}
