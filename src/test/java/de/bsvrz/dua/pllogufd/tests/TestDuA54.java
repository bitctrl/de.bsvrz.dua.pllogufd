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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.clock.SimulationClock;
import de.bsvrz.dua.pllogufd.vew.VerwaltungPlPruefungLogischUFD;
import de.kappich.pat.testumg.util.DavTestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.*;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestDuA54 extends DuAPlLogUfdTestBase {

	private SystemObject _sensor;
	private AttributeGroup _atg;
	private Aspect _aspSend;
	private Aspect _aspReceive;
	private Instant _start;

	@Override
	@Before
	public void setUp() throws Exception {
		_start = Instant.ofEpochMilli(t("14:25:03"));
		VerwaltungPlPruefungLogischUFD.clock = new SimulationClock(_start, 100);
		super.setUp();
		_sensor = _dataModel.getObject("ufd.lt");
		_atg = _dataModel.getAttributeGroup("atg.ufdsLuftTemperatur");
		_aspSend = _dataModel.getAspect("asp.externeErfassung");
		_aspReceive = _dataModel.getAspect("asp.plausibilitätsPrüfungLogisch");
	}

	@Test
	public void testDua54() throws Exception {
		fakeParamApp.publishParam(_sensor.getPid(), "atg.ufdsAusfallÜberwachung", "{maxZeitVerzug:'40 Sekunden'}");
		DataDescription ddSend = new DataDescription(_atg, _aspSend);
		DataDescription ddReceive = new DataDescription(_atg, _aspReceive);

		((SimulationClock)VerwaltungPlPruefungLogischUFD.clock).setInstant(_start);
		
		waitUntil(t("14:25:03")); sendData(new ResultData(_sensor, ddSend, t("14:25:00"), d("6,9"))); DavTestUtil.startRead(_sensor, ddReceive);
		waitUntil(t("14:26:04")); sendData(new ResultData(_sensor, ddSend, t("14:26:00"), d("6,8")));
		waitUntil(t("14:27:02")); sendData(new ResultData(_sensor, ddSend, t("14:27:00"), d("-1003")));
		waitUntil(t("14:28:00")); sendData(new ResultData(_sensor, ddSend, t("14:28:00"), d("6,7")));
		waitUntil(t("14:29:21")); sendData(new ResultData(_sensor, ddSend, t("14:29:00"), d("6,6")));
		waitUntil(t("14:30:39")); sendData(new ResultData(_sensor, ddSend, t("14:30:00"), d("6,7")));
		waitUntil(t("14:31:10")); sendData(new ResultData(_sensor, ddSend, t("14:31:00"), d("6,7")));
		waitUntil(t("14:32:44")); sendData(new ResultData(_sensor, ddSend, t("14:32:00"), d("6,6")));
		waitUntil(t("14:33:03")); sendData(new ResultData(_sensor, ddSend, t("14:33:00"), d("6,6")));
		waitUntil(t("14:34:01")); sendData(new ResultData(_sensor, ddSend, t("14:34:00"), d("6,6")));
		waitUntil(t("14:36:02")); sendData(new ResultData(_sensor, ddSend, t("14:36:00"), d("6,5")));
		waitUntil(t("14:37:03")); sendData(new ResultData(_sensor, ddSend, t("14:37:00"), d("6,4")));
		waitUntil(t("14:37:05")); sendData(new ResultData(_sensor, ddSend, t("14:35:00"), d("6,9")));
		waitUntil(t("14:38:01")); sendData(new ResultData(_sensor, ddSend, t("14:38:00"), d("6,4")));
		waitUntil(t("14:41:02")); sendData(new ResultData(_sensor, ddSend, t("14:41:00"), d("6,2")));

		
		expectData("14:25:00", "6,9", "Nein");
		expectData("14:26:00", "6,8", "Nein");
		expectData("14:27:00", "-1003", "Nein");
		expectData("14:28:00", "6,7", "Nein");
		expectData("14:29:00", "6,6", "Nein");
		expectData("14:30:00", "6,7", "Nein");
		expectData("14:31:00", "6,7", "Nein");
		expectData("14:32:00", "-1001", "Ja");
		expectData("14:33:00", "6,6", "Nein");
		expectData("14:34:00", "6,6", "Nein");
		expectData("14:35:00", "-1001", "Ja");
		expectData("14:36:00", "6,5", "Nein");
		expectData("14:37:00", "6,4", "Nein");
		expectData("14:38:00", "6,4", "Nein");
		expectData("14:39:00", "-1001", "Ja");
		expectData("14:40:00", "-1001", "Ja");
		expectData("14:41:00", "6,2", "Nein");

	}

	private void waitUntil(final long millis) throws InterruptedException {
		VerwaltungPlPruefungLogischUFD.clock.sleepUntil(Instant.ofEpochMilli(millis));
	}

	private void expectData(final String time, final String value, final String nichtErfasst) throws InterruptedException {
		DataDescription ddReceive = new DataDescription(_atg, _aspReceive);
		ResultData data = DavTestUtil.readData(_sensor, ddReceive);
		Data d = data.getData();
		Assert.assertNotNull("Daten != null", d);
		System.out.println(Instant.ofEpochMilli(data.getDataTime()) + "; 1; " + d.getItem("LuftTemperatur").getTextValue("Wert").getValueText() + "; ; " + d.getItem("LuftTemperatur").getItem("Status").getItem("Erfassung").getTextValue("NichtErfasst").getValueText());
		check("Datenzeit", Instant.ofEpochMilli(t(time)), Instant.ofEpochMilli(data.getDataTime()));
		check("Daten", value, d.getItem("LuftTemperatur").getTextValue("Wert").getValueText());
		check("Daten", nichtErfasst, d.getItem("LuftTemperatur").getItem("Status").getItem("Erfassung").getTextValue("NichtErfasst").getValueText());
		
	}

	private void check(final String text, final Object expected, final Object valueText) {
		boolean equals = expected.equals(valueText)
				|| (expected.equals("-1003") && valueText.equals("nicht ermittelbar/fehlerhaft"))
				|| (expected.equals("-1001") && valueText.equals("nicht ermittelbar"));
		if(!equals) {
			Assert.fail("Fehler " + text + "\nErwartet : " + expected + "\nIst: " + valueText);
		}
	}

	private Data d(final String s) {
		Data data = _connection.createData(_atg);
		resetData(data);
		data.getTimeValue("T").setSeconds(60);
		data.getItem("LuftTemperatur").getTextValue("Wert").setText(s);
		if(!data.isDefined()){
			data.getItem("LuftTemperatur").getUnscaledValue("Wert").set(Long.parseLong(s));
		}
		return data;
	}

	private long t(final String s) {
		return LocalTime.parse(s).atDate(LocalDate.of(2000, 1, 1)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
}
