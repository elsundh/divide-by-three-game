package com.game.model;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.game.event.Event;
import com.game.event.JoinEvent;
import com.game.event.LeaveEvent;
import com.game.event.PlayEvent;
import com.game.event.WinEvent;
import com.game.exception.GameException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RequiredArgsConstructor
@Slf4j
public class Game {

	private UUID id = UUID.randomUUID();
	private Integer latestNumber;
	private final Sinks.Many<Event> sink;
	private int playersCount = 0;
	private boolean isPlaying = true;

	public void addPlayer() {
		if (playersCount < 2) {
			playersCount++;
		}

		sink.tryEmitNext(new JoinEvent());
	}

	public boolean canJoin() {
		return playersCount < 2;
	}

	public void play(Integer added) {
		if (latestNumber == null) {

			log.info("new game, generating random number");
			latestNumber = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
			sink.tryEmitNext(new PlayEvent(latestNumber));
			
		} else {
			if (added == null) {
				added = calculateAdded();
			} else {
				if (isInvalidAndDivisibleBy3(added)) {
					throw new GameException("Wrong added number");
				}
			}
			log.info("user added {} to number {}", added, latestNumber);
			Event result;
			latestNumber = (added + latestNumber) / 3;

			if (latestNumber == 1) {
				result = new WinEvent();
			} else {
				result = new PlayEvent(latestNumber, added);
				log.info("publishing divided by 3 {}", result);
			}
			
			sink.tryEmitNext(result);

		}
	}

	private boolean isInvalidAndDivisibleBy3(Integer added) {
		return added > 1 && added < -1 && isNotDivisibleBy3(added);
	}
	private boolean isNotDivisibleBy3(Integer added) {
		return (added + latestNumber) % 3 != 0;
	}

	public void clear() {
		latestNumber = null;
		sink.tryEmitNext(new LeaveEvent());
		sink.tryEmitComplete();
		isPlaying = false;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}

	public Flux<Event> getResult() {
		return sink.asFlux();
	}

	public UUID getId() {
		return this.id;
	}

	private Integer calculateAdded() {
		Integer added;
		added = -1;
		while (added < 1 && isNotDivisibleBy3(added)) {
			added++;
		}
		return added;
	}

}
