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

import com.google.common.collect.ImmutableList;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.tests.ColumnLayout;
import de.bsvrz.dua.tests.DuADataIdentification;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestWfdFbzMitGrenzen extends DuAPlLogUfdTestBase {

	private SystemObject _sensor;
	private AttributeGroup _atg;
	private Aspect _aspSend;
	private Aspect _aspReceive;
	private DataDescription _ddIn;
	private DataDescription _ddOut;

	@Override
	protected String[] getUfdArgs() {
		return new String[]{"-KonfigurationsBereichsPid=kb.duaTestUfd", "-useWfdTrockenGrenzwert=true"};
	}
	
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		_sensor = _dataModel.getObject("ufd.sw");
		_atg = _dataModel.getAttributeGroup("atg.ufdsSichtWeite");
		_aspSend = _dataModel.getAspect("asp.externeErfassung");
		_aspReceive = _dataModel.getAspect("asp.plausibilitätsPrüfungLogisch");
		_ddIn = new DataDescription(_atg, _aspSend);
		_ddOut = new DataDescription(_atg, _aspReceive);
	}

	@Test
	public void testWfdFbzMitGrenzwert() throws Exception {
		SystemObject ni = _dataModel.getObject("ufd.ni");
		SystemObject ns = _dataModel.getObject("ufd.na");
		SystemObject wfd = _dataModel.getObject("ufd.wfd");
		SystemObject fbz = _dataModel.getObject("ufd.fbz");
		SystemObject lt = _dataModel.getObject("ufd.lt");
		SystemObject rlf = _dataModel.getObject("ufd.rlf");
		SystemObject sw = _dataModel.getObject("ufd.sw");
		SystemObject messstelle = _dataModel.getObject("ufdm.1");
		AttributeGroup atgni = _dataModel.getAttributeGroup("atg.ufds" + "NiederschlagsIntensität");
		AttributeGroup atgns = _dataModel.getAttributeGroup("atg.ufds" + "NiederschlagsArt");
		AttributeGroup atgwfd = _dataModel.getAttributeGroup("atg.ufds" + "WasserFilmDicke");
		AttributeGroup atgfbz = _dataModel.getAttributeGroup("atg.ufds" + "FahrBahnOberFlächenZustand");
		AttributeGroup atglt = _dataModel.getAttributeGroup("atg.ufds" + "LuftTemperatur");
		AttributeGroup atgrlf = _dataModel.getAttributeGroup("atg.ufds" + "RelativeLuftFeuchte");
		AttributeGroup atgsw = _dataModel.getAttributeGroup("atg.ufds" + "SichtWeite");
		DataDescription ddniSend = new DataDescription(atgni, _aspSend);
		DataDescription ddnsSend = new DataDescription(atgns, _aspSend);
		DataDescription ddwfdSend = new DataDescription(atgwfd, _aspSend);
		DataDescription ddfbzSend = new DataDescription(atgfbz, _aspSend);
		DataDescription ddltSend = new DataDescription(atglt, _aspSend);
		DataDescription ddrlfSend = new DataDescription(atgrlf, _aspSend);
		DataDescription ddswSend = new DataDescription(atgsw, _aspSend);
		DataDescription ddniReceive = new DataDescription(atgni, _aspReceive);
		DataDescription ddnsReceive = new DataDescription(atgns, _aspReceive);
		DataDescription ddwfdReceive = new DataDescription(atgwfd, _aspReceive);
		DataDescription ddfbzReceive = new DataDescription(atgfbz, _aspReceive);
		DataDescription ddltReceive = new DataDescription(atglt, _aspReceive);
		DataDescription ddrlfReceive = new DataDescription(atgrlf, _aspReceive);
		DataDescription ddswReceive = new DataDescription(atgsw, _aspReceive);
		fakeParamApp.publishParam(messstelle.getPid(), "atg.ufdmsParameterMeteorologischeKontrolle",
		                          "{erzeugeBetriebsmeldungMeteorologischeKontrolle:'Ja'," +
				                          "NIgrenzNS:'0,2 mm/h'," +
				                          "NIgrenzWFD:'0,4mm/h'," +
				                          "WFDgrenzTrocken:'0,02 mm/h'," +
				                          "LTgrenzRegen:'-4,5 °C'," +
				                          "LTgrenzSchnee:'4,5 °C'," +
				                          "RLFgrenzTrocken:'58 %'," +
				                          "RLFgrenzNass:'80 %'," +
				                          "SWgrenz:'555 mm/h'}");
		ImmutableList<DuADataIdentification> send = ImmutableList.of(
				new DuADataIdentification(ni, ddniSend),
				new DuADataIdentification(ns, ddnsSend),
				new DuADataIdentification(wfd, ddwfdSend),
				new DuADataIdentification(fbz, ddfbzSend),
				new DuADataIdentification(lt, ddltSend),
				new DuADataIdentification(rlf, ddrlfSend),
				new DuADataIdentification(sw, ddswSend)
		);
		ImmutableList<DuADataIdentification> rec = ImmutableList.of(
				new DuADataIdentification(ni, ddniReceive),
				new DuADataIdentification(ns, ddnsReceive),
				new DuADataIdentification(wfd, ddwfdReceive),
				new DuADataIdentification(fbz, ddfbzReceive),
				new DuADataIdentification(lt, ddltReceive),
				new DuADataIdentification(rlf, ddrlfReceive),
				new DuADataIdentification(sw, ddswReceive)
		);
		startTestCase("WFD_FBZ_mit_Grenzwerten.csv", send, rec, new Dua58Layout());
	}
	
	private class Dua58Layout extends ColumnLayout{
		@Override
		public int getColumnCount(final boolean in) {
			return 1;     
		}

		@Override
		public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
			String pid = item.getAttributeType().getPid();
			switch(pid) {
				case "atl.ufdsNiederschlagsIntensität":
					set(item, row, realCol);
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 1));
					break;	
				case "atl.ufdsNiederschlagsArt":
					set(item, row, realCol);
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 1));
					break;	
				case "atl.ufdsWasserFilmDicke":
					set(item, row, realCol);
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 1));
					break;	
				case "atl.ufdsFahrBahnOberFlächenZustand":
					set(item, row, realCol);
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 1));
					break;	
				case "atl.ufdsLuftTemperatur":
					set(item, row, realCol);
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 1));
					break;	
				case "atl.ufdsRelativeLuftFeuchte":
					set(item, row, realCol);
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 1));
					break;	
				case "atl.ufdsSichtWeite":
					set(item, row, realCol);
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 1));
					break;
			}
		}

		private void set(final Data item, final List<String> row, final int realCol) {
			try {
				item.getTextValue("Wert").setText(row.get(realCol));
			}
			catch(Exception e) {
				item.getUnscaledValue("Wert").set(Double.parseDouble(row.get(realCol)));
			}
			if(!item.isDefined()){
				item.getUnscaledValue("Wert").set(Double.parseDouble(row.get(realCol)));
			}
		}

		@Override
		public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
			if(fullRow.get(39).equals("X")) return "[DUA-PP-MK01]";
			if(fullRow.get(40).equals("X")) return "[DUA-PP-MK02]";
			if(fullRow.get(41).equals("X")) return "[DUA-PP-MK03]";
			if(fullRow.get(42).equals("X")) return "[DUA-PP-MK04]";
			if(fullRow.get(43).equals("X")) return "[DUA-PP-MK05]";
			if(fullRow.get(44).equals("X")) return "[DUA-PP-MK06]";
			if(fullRow.get(45).equals("X")) return "[DUA-PP-MK07]";
			if(fullRow.get(46).equals("X")) return "[DUA-PP-MK08]";
			if(fullRow.get(47).equals("X")) return "[DUA-PP-MK09]";
			if(fullRow.get(48).equals("X")) return "[DUA-PP-MK10]";
			if(fullRow.get(49).equals("X")) return "[DUA-PP-MK11]";
			if(fullRow.get(50).equals("X")) return "[DUA-PP-MK12]";
			if(fullRow.get(51).equals("X")) return "[DUA-PP-MK13]";
			return null;
		}
	}

}
