package com.game.api.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayEvent extends Event {
	
	private Integer added;
	private Integer resultingNumber;
	
	public PlayEvent() {
		super("play");
	}
	
	public PlayEvent(Integer resultingNumber) {
		super("play");
		this.resultingNumber = resultingNumber;
	}
	
	public PlayEvent(Integer resultingNumber, Integer added) {
		super("play");
		this.resultingNumber = resultingNumber;
		this.added = added;
	}

}
