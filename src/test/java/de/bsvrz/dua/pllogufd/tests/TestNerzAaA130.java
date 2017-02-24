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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.kappich.pat.testumg.util.DavTestUtil;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestNerzAaA130 extends DuAPlLogUfdTestBase {

	private SystemObject ltSensor;
	private AttributeGroup ltAtg;

	private Aspect _aspReceive;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		ltSensor = _dataModel.getObject("ufd.lt");
		ltAtg = _dataModel.getAttributeGroup("atg.ufdsLuftTemperatur");
		_aspReceive = _dataModel.getAspect("asp.plausibilitätsPrüfungLogisch");
	}
	
	@Override
	protected String[] getUfdArgs() {
		Collection<String> args = new ArrayList<>();
		args.addAll(Arrays.asList(super.getUfdArgs()));
		args.add("-initialeAusfallKontrolle=true");
		args.add("-defaultMaxZeitVerzug=10000");
		return args.toArray(new String[args.size()]);
	}

	@Test
	public void testNerzAeA130() throws Exception {
		DataDescription ltReceive = new DataDescription(ltAtg, _aspReceive);
	
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime cal = ZonedDateTime.of(LocalDate.now(), LocalTime.of(0, 0), ZoneId.systemDefault());
		while( cal.isBefore(now)) {
			cal = cal.plus(Duration.ofMinutes(1));
		}

		DavTestUtil.startRead(ltSensor, ltReceive);

		expectLtData(cal, "nicht ermittelbar");
		expectLtData(cal.plus(Duration.ofMinutes(1)), "nicht ermittelbar");
		expectLtData(cal.plus(Duration.ofMinutes(2)), "nicht ermittelbar");
	}

	private void expectLtData(final ZonedDateTime cal, final String value) throws InterruptedException {
		DataDescription ddReceive = new DataDescription(ltAtg, _aspReceive);
		ResultData data = null;
		do {
			data = DavTestUtil.readData(ltSensor, ddReceive, TimeUnit.MINUTES.toMillis(2));
		} while((data != null) && !data.hasData());

		Assert.assertNotNull("Datensatz != null", data);
		
		Data d = data.getData();
		Assert.assertNotNull("Daten != null", d);
		System.out.println(Instant.ofEpochMilli(data.getDataTime()) + "; 1; "
				+ d.getItem("LuftTemperatur").getTextValue("Wert").getValueText() + "; ; " + d.getItem("LuftTemperatur").getItem("Status").getItem("Erfassung").getTextValue("NichtErfasst").getValueText());
		check("Datenzeit", cal.toInstant(), Instant.ofEpochMilli(data.getDataTime()));
		check("Daten: " + cal, value, d.getItem("LuftTemperatur").getTextValue("Wert").getValueText());
	}

	private void check(final String text, final Object expected, final Object valueText) {
		boolean equals = expected.equals(valueText)
				|| (expected.equals("-3") && valueText.equals("nicht ermittelbar/fehlerhaft"))
				|| (expected.equals("-1") && valueText.equals("nicht ermittelbar"));
		if(!equals) {
			Assert.fail("Fehler " + text + "\nErwartet : " + expected + "\nIst: " + valueText);
		}
	}
}
