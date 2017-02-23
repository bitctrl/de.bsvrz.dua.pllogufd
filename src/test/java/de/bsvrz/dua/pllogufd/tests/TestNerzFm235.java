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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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
import de.bsvrz.dua.pllogufd.clock.SimulationClock;
import de.bsvrz.dua.pllogufd.vew.VerwaltungPlPruefungLogischUFD;
import de.kappich.pat.testumg.util.DavTestUtil;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestNerzFm235 extends DuAPlLogUfdTestBase {

	private SystemObject wfdSensor;
	private AttributeGroup wfdAtg;

	private SystemObject fbzSensor;
	private AttributeGroup fbzAtg;

	private Aspect _aspSend;
	private Aspect _aspReceive;
	private Instant _start;

	@Override
	@Before
	public void setUp() throws Exception {
		_start = Instant.ofEpochMilli(t("14:25:03"));
		VerwaltungPlPruefungLogischUFD.clock = new SimulationClock(_start, 100);
		super.setUp();
		wfdSensor = _dataModel.getObject("ufd.wfd");
		wfdAtg = _dataModel.getAttributeGroup("atg.ufdsWasserFilmDicke");
		fbzSensor = _dataModel.getObject("ufd.fbz");
		fbzAtg = _dataModel.getAttributeGroup("atg.ufdsFahrBahnOberFlächenZustand");
		_aspSend = _dataModel.getAspect("asp.externeErfassung");
		_aspReceive = _dataModel.getAspect("asp.plausibilitätsPrüfungLogisch");
	}
	
	@Override
	protected String[] getUfdArgs() {
		Collection<String> args = new ArrayList<>();
		args.addAll(Arrays.asList(super.getUfdArgs()));
		args.add("-ignoriereRegeln=10,11");
		return args.toArray(new String[args.size()]);
	}

	@Test
	public void testNerzFm235MitRegeln() throws Exception {
		DataDescription wfdSend = new DataDescription(wfdAtg, _aspSend);
		DataDescription fbzSend = new DataDescription(fbzAtg, _aspSend);
		DataDescription wfdReceive = new DataDescription(wfdAtg, _aspReceive);
		DataDescription fbzReceive = new DataDescription(fbzAtg, _aspReceive);

		((SimulationClock)VerwaltungPlPruefungLogischUFD.clock).setInstant(_start);

		DavTestUtil.startRead(wfdSensor, wfdReceive);
		DavTestUtil.startRead(fbzSensor, fbzReceive);

		sendData(new ResultData(wfdSensor, wfdSend, t("14:25:00"), wfd("10.00")), new ResultData(fbzSensor, fbzSend, t("14:25:00"), fbz("trocken"))); 
		sendData(new ResultData(wfdSensor, wfdSend, t("14:26:00"), wfd("0.00")), new ResultData(fbzSensor, fbzSend, t("14:26:00"), fbz("feucht"))); 
		sendData(new ResultData(wfdSensor, wfdSend, t("14:27:00"), wfd("0.00")), new ResultData(fbzSensor, fbzSend, t("14:26:00"), fbz("trocken"))); 
		
		expectWfdData("14:25:00", "10,00");
		expectFbzData("14:25:00", "trocken");
		expectWfdData("14:26:00", "0,00");
		expectFbzData("14:26:00", "feucht");
	}

	private void expectWfdData(final String time, final String value) throws InterruptedException {
		DataDescription ddReceive = new DataDescription(wfdAtg, _aspReceive);
		ResultData data = null;
		do {
			data = DavTestUtil.readData(wfdSensor, ddReceive);
		} while((data != null) && data.getDataTime() > _start.toEpochMilli() + (TimeUnit.DAYS.toMillis(1)));

		Assert.assertNotNull("Datensatz != null", data);
		
		Data d = data.getData();
		Assert.assertNotNull("Daten != null", d);
		System.out.println(Instant.ofEpochMilli(data.getDataTime()) + "; 1; "
				+ d.getItem("WasserFilmDicke").getTextValue("Wert").getValueText() + "; ; " + d.getItem("WasserFilmDicke").getItem("Status").getItem("Erfassung").getTextValue("NichtErfasst").getValueText());
		check("Datenzeit", Instant.ofEpochMilli(t(time)), Instant.ofEpochMilli(data.getDataTime()));
		check("Daten: " + time, value, d.getItem("WasserFilmDicke").getTextValue("Wert").getValueText());
	}

	private void expectFbzData(final String time, final String value) throws InterruptedException {
		DataDescription ddReceive = new DataDescription(fbzAtg, _aspReceive);
		ResultData data = null;
		do {
			data = DavTestUtil.readData(fbzSensor, ddReceive);
		} while((data != null) && data.getDataTime() > _start.toEpochMilli() + (TimeUnit.DAYS.toMillis(1)));

		Assert.assertNotNull("Datensatz != null", data);
		
		Data d = data.getData();
		Assert.assertNotNull("Daten != null", d);
		System.out.println(Instant.ofEpochMilli(data.getDataTime()) + "; 1; "
				+ d.getItem("FahrBahnOberFlächenZustand").getTextValue("Wert").getValueText() + "; ; " + d.getItem("FahrBahnOberFlächenZustand").getItem("Status").getItem("Erfassung").getTextValue("NichtErfasst").getValueText());
		check("Datenzeit", Instant.ofEpochMilli(t(time)), Instant.ofEpochMilli(data.getDataTime()));
		check("Daten: " + time, value, d.getItem("FahrBahnOberFlächenZustand").getTextValue("Wert").getValueText());
	}

	
	private void check(final String text, final Object expected, final Object valueText) {
		boolean equals = expected.equals(valueText)
				|| (expected.equals("-3") && valueText.equals("nicht ermittelbar/fehlerhaft"))
				|| (expected.equals("-1") && valueText.equals("nicht ermittelbar"));
		if(!equals) {
			Assert.fail("Fehler " + text + "\nErwartet : " + expected + "\nIst: " + valueText);
		}
	}

	private Data wfd(final String s) {
		Data data = _connection.createData(wfdAtg);
		resetData(data);
		data.getTimeValue("T").setSeconds(60);
		data.getItem("WasserFilmDicke").getTextValue("Wert").setText(s);
		if(!data.isDefined()){
			data.getItem("WasserFilmDicke").getUnscaledValue("Wert").set(Long.parseLong(s));
		}
		return data;
	}

	private Data fbz(final String s) {
		Data data = _connection.createData(fbzAtg);
		resetData(data);
		data.getTimeValue("T").setSeconds(60);
		data.getItem("FahrBahnOberFlächenZustand").getTextValue("Wert").setText(s);
//		if(!data.isDefined()){
//			data.getItem("FahrBahnOberFlächenZustand").getUnscaledValue("Wert").set(Long.parseLong(s));
//		}
		return data;
	}

	
	private long t(final String s) {
		return LocalTime.parse(s).atDate(LocalDate.of(2000, 1, 1)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
}
