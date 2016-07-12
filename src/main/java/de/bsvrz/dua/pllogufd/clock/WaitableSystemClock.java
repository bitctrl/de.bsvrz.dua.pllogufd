/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Pl-Prüfung logisch UFD
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.pllogufd.
 * 
 * de.bsvrz.dua.pllogufd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.pllogufd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.pllogufd.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.pllogufd.clock;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Implementierung von {@link WaitableClock} als normale Systemuhr
 *
 * @author Kappich Systemberatung
 */
public final class WaitableSystemClock extends WaitableClock {

	private final Clock _parent;

	WaitableSystemClock(Clock parent) {
		_parent = parent;
	}

	@Override
	public ZoneId getZone() {
		return _parent.getZone();
	}

	@Override
	public Clock withZone(final ZoneId zone) {
		return new WaitableSystemClock(_parent.withZone(zone));
	}

	@Override
	public Instant instant() {
		return _parent.instant();
	}
	
	@Override
	public void sleep(long millis) throws InterruptedException {
		if(millis <= 0) return;
		Thread.sleep(millis);
	}

	@Override
	public Duration wait(final Object obj, final long millis) throws InterruptedException {
		if(millis <= 0) return Duration.ZERO;
		Instant start = instant();
		obj.wait(millis);
		Instant end = instant();
		return Duration.between(start, end);
	}
}
