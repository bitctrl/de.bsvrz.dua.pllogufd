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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Implementierung von {@link WaitableClock} als simulierte Uhr, die einen
 * beliebigen Startzeitpunkt hat und auch schneller oder langsamer gehen kann,
 * als die reale Systemzeit.
 *
 * @author Kappich Systemberatung
 */
public final class SimulationClock extends WaitableClock {

	private final double _simulationSpeed;
	private final ZoneId _zone;
	private Instant _startSimulation;
	private Instant _startRealTime;

	/**
	 * Erstellt eine neue SimulationClock
	 * 
	 * @param startState
	 *            Startzeitpunkt der Uhr
	 * @param simulationSpeed
	 *            Simulationsgeschwindigkeit (1 = reale Geschwindigkeit, 10 =
	 *            die Uhr geht 10 mal schneller, als die reale Zeit, usw.)
	 */
	public SimulationClock(Instant startState, double simulationSpeed) {
		this(startState, simulationSpeed, ZoneId.systemDefault(), Instant.now());
	}

	/**
	 * Interner Hilfskonstruktor für die {@link #withZone(ZoneId)}-Methode.
	 * 
	 * @param startSimulation
	 *            der Startzeitpunkt der Simulation
	 * @param simulationSpeed
	 *            die Simulationsgeschwindigkeit
	 * @param zone
	 *            die Id der Zeitzone
	 * @param startRealTime
	 *            der Startzeitpunkt in Realzeit
	 * 
	 */
	private SimulationClock(final Instant startSimulation, double simulationSpeed, final ZoneId zone,
			final Instant startRealTime) {
		Objects.requireNonNull(startSimulation, "startSimulation == null");
		if (simulationSpeed <= 0)
			throw new IllegalArgumentException("simulationSpeed");
		_startSimulation = startSimulation;
		_simulationSpeed = simulationSpeed;
		_zone = zone;
		_startRealTime = startRealTime;
	}

	/**
	 * Verstellt den aktuellen Zeitpunkt. Dies hat keinen Einfluss auf aktuell
	 * laufende Wartedauern.
	 * 
	 * @param start
	 *            Neue aktuelle Uhrzeit.
	 */
	public void setInstant(final Instant start) {
		_startSimulation = start;
		_startRealTime = Instant.now();
	}

	@Override
	public void sleep(long millis) throws InterruptedException {
		millis = (long) (millis / _simulationSpeed);
		if (millis <= 0)
			return;
		Thread.sleep(millis);
	}

	@Override
	public Duration wait(final Object obj, long millis) throws InterruptedException {
		millis = (long) (millis / _simulationSpeed);
		if (millis <= 0)
			return Duration.ZERO;
		Instant start = instant();
		obj.wait(millis);
		Instant end = instant();
		return Duration.between(start, end);
	}

	@Override
	public ZoneId getZone() {
		return _zone;
	}

	@Override
	public Clock withZone(final ZoneId zone) {
		return new SimulationClock(_startSimulation, _simulationSpeed, zone, _startRealTime);
	}

	@Override
	public Instant instant() {
		Duration realTime = Duration.between(_startRealTime, Instant.now());
		long realSeconds = realTime.getSeconds();
		int realNano = realTime.getNano();
		BigInteger simulationSeconds = BigInteger.valueOf(realSeconds);
		BigInteger simulationNanos = BigInteger.valueOf(realNano);
		BigInteger million = BigInteger.valueOf(1_000_000_000);
		BigInteger bigNanos = simulationSeconds.multiply(million).add(simulationNanos);
		BigDecimal bigDecimal = BigDecimal.valueOf(_simulationSpeed).multiply(new BigDecimal(bigNanos));
		BigInteger[] divMod = bigDecimal.toBigInteger().divideAndRemainder(million);
		return _startSimulation.plus(Duration.ofSeconds(divMod[0].longValueExact(), divMod[1].intValueExact()));
	}
}
