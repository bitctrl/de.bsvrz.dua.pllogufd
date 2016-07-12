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

/**
 * Ein Clock-Objekt, das Methoden bietet, eine beliebige Zeit zu arten oder zu warten bis ein beliebiger Zeitpunkt eingetroffen ist.
 * Diese Klasse kann verwendet werden um für Testfälle, Simulationen u.ä. den Zeitablauf zu beschleunigen oder zu verlangsamen, siehe {@link SimulationClock}
 *
 * @author Kappich Systemberatung
 */
public abstract class WaitableClock extends Clock {

	/**
	 * Tut das gleiche wie {@link Thread#sleep(long)}, verwendet aber diese Uhr um die Wartezeit zu bestimmen. Wenn diese Uhr beispielsweise
	 * um Faktor 10 beschleunigt läuft, dann verringert sich die reale Wartezeit um Faktor 10.
	 * @param millis Millisekunden, fall 0 oder negativ wird nicht gewartet.
	 * @throws InterruptedException
	 */
	public abstract void sleep(long millis) throws InterruptedException;

	/**
	 * Wartet die angegebene Dauer.
	 * @param duration Dauer, fall 0 oder negativ wird nicht gewartet.
	 * @throws InterruptedException
	 */
	public final void sleep(Duration duration) throws InterruptedException {
		sleep(duration.toMillis());
	}

	/**
	 * Wartet, bis die Uhr die angegebene Zeit erreicht. Liegt die angegebene Zeit vor dem aktuellen zeitpunkt wird nicht gewartet.
	 * @param instant Ziel-Zeitpunkt
	 * @throws InterruptedException
	 */
	public final void sleepUntil(Instant instant) throws InterruptedException {
		sleep(durationUntil(instant));
	}

	/**
	 * Berechnet die Dauer bis zum angegebenen Zeitpunkt
	 * @param until Ziel-Zeitpunkt
	 * @return Dauer (kann negativ sein, falls der Ziel-Zeitpunkt in der Vergangenheit liegt)
	 */
	public final Duration durationUntil(final Instant until) {
		return Duration.between(instant(), until);
	}
	
	/**
	 * Tut das gleiche wie obj.{@link #wait(long)}, verwendet aber diese Uhr um die Wartezeit zu bestimmen. Wenn diese Uhr beispielsweise
	 * um Faktor 10 beschleunigt läuft, dann verringert sich die reale Wartezeit um Faktor 10. Im Gegensatz zu {@link #sleep(Duration)} kann
	 * das Warten mit {@link #notifyAll()} unterbrochen werden und es muss auf das Objekt synchronisiert werden.
	 * @param obj Objekt, für das {@link #wait(long)} aufgerufen werden soll
	 * @param millis Millisekunden, fall 0 oder negativ wird nicht gewartet.
	 * @throws InterruptedException
	 * @return Zeit die gewartet wurde, bis der Thread aufgeweckt wurde
	 */
	public abstract Duration wait(Object obj, long millis) throws InterruptedException;

	/**
	 * Wartet die angegebene Dauer, vergleichbar mit obj.{@link #wait(long)}. Im Gegensatz zu {@link #sleep(Duration)} kann
	 * das Warten mit {@link #notifyAll()} unterbrochen werden und es muss auf das Objekt synchronisiert werden.
	 * @param obj  Objekt, für das {@link #wait(long)} aufgerufen werden soll
	 * @param duration Dauer, fall 0 oder negativ wird nicht gewartet.
	 * @return Zeit die gewartet wurde, bis der Thread aufgeweckt wurde
	 * @throws InterruptedException
	 */
	public final Duration wait(Object obj, Duration duration) throws InterruptedException {
		return wait(obj, duration.toMillis());
	}
	
	/**
	 * Wartet biszum angegebenen Zeitpunkt, vergleichbar mit obj.{@link #wait(long)}. Im Gegensatz zu {@link #sleep(Duration)} kann
	 * das Warten mit {@link #notifyAll()} unterbrochen werden und es muss auf das Objekt synchronisiert werden.
	 * @param obj  Objekt, für das {@link #wait(long)} aufgerufen werden soll
	 * @param until Ziel-Zeitpunkt
	 * @return Zeit die gewartet wurde, bis der Thread aufgeweckt wurde
	 * @throws InterruptedException
	 */
	public final Duration waitUntil(Object obj, Instant until) throws InterruptedException {
		return wait(obj, durationUntil(until));
	}
	
	/** 
	 * Gibt die System-Uhr zurück. Diese entspricht der aktuellen Systemzeit und ist damit eine erweiterte Version
	 * von {@link Clock#systemUTC()}.
	 * @return die System-Uhr
	 */
	public static WaitableClock systemClock(){
		return new WaitableSystemClock(Clock.systemUTC());
	}

	/**
	 * Gibt eine simulierte Uhr zurück
	 * @param start Startzeit der Uhr (beim Aufruf dieser Methode)
	 * @param simulationSpeed Geschwindigkeits-Faktor der Uhr. Gibt an, wie schnell die Uhr im Vergleich zur realen
	 *                        Systemzeit läuft. Bspw. Faktor 10: Die Uhr geht 10 Minuten innerhalb einer realen Minute.   
	 * @return Simulationsuhr
	 */
	public static WaitableClock simulationClock(Instant start, double simulationSpeed){
		return new SimulationClock(start, simulationSpeed);
	}
}
