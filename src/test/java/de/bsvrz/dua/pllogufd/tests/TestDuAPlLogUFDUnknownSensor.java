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

package de.bsvrz.dua.pllogufd.tests;

import de.bsvrz.sys.funclib.operatingMessage.OperatingMessageInterface;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestDuAPlLogUFDUnknownSensor extends DuAPlLogUfdTestBase {

	@Override
	protected String[] getConfigurationAreas() {
		return new String[]{"kb.duaTestUfd", "kb.duaTestUfdUnknown"};
	}


	protected String[] getUfdArgs() {
		return new String[]{"-KonfigurationsBereichsPid=kb.duaTestUfd,kb.duaTestUfdUnknown"};
	}

	@Test
	public void testDuaUnknownSensors() throws Exception {
		final Set<OperatingMessageInterface> messageSet = new HashSet<>();
		// Erwarte Betriebsmeldungen
		for(int i = 0; i < 3; i++){
			messageSet.add(pollMessage());
		}
		Assert.assertNull(pollMessage());
		System.out.println("messageSet = " + messageSet);
		Assert.assertTrue(messageSet.toString().contains("Unbekannter Umfelddatensensor (ufd.sonnenscheindauer2) bei Messstelle (ufdm.2) entdeckt. Sensor wird ignoriert. [DUA-PP-UU01]"));
		Assert.assertTrue(messageSet.toString().contains("Unbekannter Umfelddatensensor (ufd.sonnenscheindauer3) bei Messstelle (ufdm.3) entdeckt. Sensor wird ignoriert. [DUA-PP-UU01]"));
		Assert.assertTrue(messageSet.toString().contains("Unbekannter Umfelddatensensor (ufd.sonnenscheindauer4) entdeckt. Sensor wird ignoriert. [DUA-PP-UU02]"));
	}
}
