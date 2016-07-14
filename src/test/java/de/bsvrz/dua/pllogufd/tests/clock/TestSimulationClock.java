/*
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.pllogufd.tests.
 * 
 * de.bsvrz.dua.pllogufd.tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.pllogufd.tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.pllogufd.tests.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.pllogufd.tests.clock;

import de.bsvrz.dua.pllogufd.clock.SimulationClock;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestSimulationClock {

	@Test
	public void testSimple() throws Exception {
		SimulationClock simulationClock = new SimulationClock(LocalDateTime.of(2000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), 100);
		System.out.println("start = " + simulationClock.instant());
		simulationClock.sleep(Duration.ofMinutes(10));
		System.out.println("end = " + simulationClock.instant());
		simulationClock.sleep(Duration.ofMinutes(5));
		System.out.println("end = " + simulationClock.instant());
	}
}
