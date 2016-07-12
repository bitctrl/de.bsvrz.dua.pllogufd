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

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Diese Klasse bietet einen Timer mit anpassbarem Ausführungszzeitpunkt. Sie ist nur eine Hilfsklasse zu {@link ClockScheduler}.
 *
 * @author Kappich Systemberatung
 */
abstract class ClockTimer {

	/**
	 * Nächster Ausführungszzeitpunkt
	 */
	private Instant _targetTime = null;

	/**
	 * Objekt zur Thread-Synchronisation
	 */
	private final Object _lock = new Object();

	/**
	 * Wurde der Thread terminiert?
	 */
	private boolean _terminated = false;

	/**
	 * Standardkonstruktor.
	 * @param clock Zu berwendende Uhr
	 */
	public ClockTimer(WaitableClock clock) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Instant targetTime;
						synchronized(_lock) {
							targetTime = _targetTime;
							if(targetTime == null) {
								_lock.wait();
							}
							else {
								clock.waitUntil(_lock, targetTime);
							}
						}
						if(_terminated) return;
						if(targetTime != null) {
							Duration duration = clock.durationUntil(targetTime);
							if(duration.isNegative() || duration.isZero()) {
								ClockTimer.this.run(targetTime);
								synchronized(_lock){
									if(_targetTime == targetTime) _targetTime = null;
								}
							}
						}
					}
					catch(InterruptedException ignored) {
					}
				}
			}
		}, "ClockTimer").start();
	}

	/**
	 * Diese Methode kann überschrieben werden, mit der Aktion, die ausgeführt werden soll
	 * @param targetTime (Geplanter) Zeitpunkt der Auführung, muss nicht zwingend dem aktuellen Zeitstempel entsprechen (beispielsweise wenn etwas für die Vergangenheit geplant wurde, wird hier der
	 *                   Zeitstempel in der Vergangenheit ausgegeben)   
	 */
	public abstract void run(final Instant targetTime);

	/**
	 * Setzt den nächsten Ausführungstermin
	 * @param instant Zeitpunkt der nächsten Ausführung. Falls der Zeitpunkt vor dem aktuellen Zeitpunkt liegt, wird run() zeitnah ausgeführt.
	 */
	public final void setTargetTime(
			final Instant instant) {
		synchronized(_lock) {
			if(!Objects.equals(this._targetTime, instant)) {
				this._targetTime = instant;
				_lock.notifyAll();
			}
		}
	}

	/** 
	 * Gibt den Zeitpunkt zurück, zu dem der Timer ausgeführt wird
	 * @return den Zeitpunkt, zu dem der Timer ausgeführt wird
	 */
	public final Instant getTargetTime() {
		synchronized(_lock) {
			return this._targetTime;
		}
	}


	public void terminate() {
		synchronized(_lock) {
			_terminated = true;
			_lock.notifyAll();
		}
	}
}
