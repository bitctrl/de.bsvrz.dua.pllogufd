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
public class TestDuAPlLogUFD extends DuAPlLogUfdTestBase {

	private SystemObject _sensor;
	private AttributeGroup _atg;
	private Aspect _aspSend;
	private Aspect _aspReceive;
	private DataDescription _ddIn;
	private DataDescription _ddOut;

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
	public void testDua8485() throws Exception {
		SystemObject[] sensor = {_sensor};
		fakeParamApp.publishParam(_sensor.getPid(), "atg.ufdsGrenzwerteSichtWeite", "{Verhalten:'Wert reduzieren',SWmax:'502'}");
		startTestCase("DUA84.85.csv", sensor, sensor, _ddIn, _ddOut, new ColumnLayout() {
			@Override
			public int getColumnCount(final boolean in) {
				return 1;
			}

			@Override
			public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
				item.getTextValue("Wert").setText(row.get(realCol));
				if(!in) {
					try {
						item.getItem("Status").getItem("PlLogisch").getTextValue("WertMaxLogisch").setText(row.get(realCol + 2));
					}
					catch(NoSuchElementException ignored){
					}
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 3));
					String percent = row.get(realCol + 5);
					if(percent.endsWith("%")) {
						percent = percent.substring(0, percent.length() - 1);
					}
					percent = percent.replace(',', '.');
					item.getItem("Güte").getUnscaledValue("Index").set(Double.parseDouble(percent) * 100);
				}
				else {
					item.getItem("Güte").getUnscaledValue("Index").set(10000);
				}
			}

			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				String s = fullRow.get(12);
				if(s.equals("Ja")) return "[DUA-PP-UGW";
				return null;
			}

			@Override
			public Collection<String> getIgnored() {
				return Arrays.asList("T");
			}
		});

	}
	
	@Test
	public void testDua8485b() throws Exception {
		SystemObject[] sensor = {_sensor};
		fakeParamApp.publishParam(_sensor.getPid(), "atg.ufdsGrenzwerteSichtWeite", "{Verhalten:'Auf fehlerhaft setzen',SWmax:'502'}");
		startTestCase("DUA84.85b.csv", sensor, sensor, _ddIn, _ddOut, new ColumnLayout() {
			@Override
			public int getColumnCount(final boolean in) {
				return 1;
			}

			@Override
			public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
				item.getTextValue("Wert").setText(row.get(realCol));
				if(!in) {
					try{
						item.getItem("Status").getItem("PlLogisch").getTextValue("WertMaxLogisch").setText(row.get(realCol + 2));
					}                                           
				    catch(NoSuchElementException ignored)  {
				    }
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 3));
					String percent = row.get(realCol + 5);
					if(percent.endsWith("%")) {
						percent = percent.substring(0, percent.length() - 1);
					}
					percent = percent.replace(',', '.');
					item.getItem("Güte").getUnscaledValue("Index").set(Double.parseDouble(percent) * 100);
				}
				else {
					item.getItem("Güte").getUnscaledValue("Index").set(10000);
				}
			}

			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				String s = fullRow.get(12);
				if(s.equals("Ja")) return "[DUA-PP-UGW05]";
				return null;
			}

			@Override
			public Collection<String> getIgnored() {
				return Arrays.asList("T");
			}
		});

	}
	
	@Test
	public void testDua8485c() throws Exception {
		SystemObject[] sensor = {_sensor};
		fakeParamApp.publishParam(_sensor.getPid(), "atg.ufdsGrenzwerteSichtWeite", "{Verhalten:'Keine Prüfung',SWmax:'502'}");
		startTestCase("DUA84.85c.csv", sensor, sensor, _ddIn, _ddOut, new ColumnLayout() {
			@Override
			public int getColumnCount(final boolean in) {
				return 1;
			}

			@Override
			public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
				item.getTextValue("Wert").setText(row.get(realCol));
				if(!in) {
					try {
						item.getItem("Status").getItem("PlLogisch").getTextValue("WertMaxLogisch").setText(row.get(realCol + 2));
					}
					catch(NoSuchElementException ignored){
					}
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 3));
					String percent = row.get(realCol + 5);
					if(percent.endsWith("%")) {
						percent = percent.substring(0, percent.length() - 1);
					}
					percent = percent.replace(',', '.');
					item.getItem("Güte").getUnscaledValue("Index").set(Double.parseDouble(percent) * 100);
				}
				else {
					item.getItem("Güte").getUnscaledValue("Index").set(10000);
				}
			}

			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				String s = fullRow.get(12);
				if(s.equals("Ja")) return "[DUA-PP-UGW";
				return null;
			}

			@Override
			public Collection<String> getIgnored() {
				return Arrays.asList("T");
			}
		});

	}
	
	@Test
	public void testDua56NI() throws Exception {
		startDua56Test("NI", "NiederschlagsIntensität", "0,1", "4 Minuten", "größer");
	}

	@Test
	public void testDua56NIOhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("NI", "NiederschlagsIntensität", "0,1", "4 Minuten", "größer");
	}

	
	@Test
	public void testDua56WFD() throws Exception {
		startDua56Test("WFD", "WasserFilmDicke", "0,04", "7 Minuten", "größer");
	}	
	
	@Test
	public void testDua56WFDOhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("WFD", "WasserFilmDicke", "0,04", "7 Minuten", "größer");
	}	

	@Test
	public void testDua56RLF() throws Exception {
		startDua56Test("RLF", "RelativeLuftFeuchte", "100", "3 Minuten", "kleiner");
	}	

	@Test
	public void testDua56RLFOhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("RLF", "RelativeLuftFeuchte", "100", "3 Minuten", "kleiner");
	}	
	
	@Test
	public void testDua56SW() throws Exception {
		startDua56Test("SW", "SichtWeite", "652", "5 Minuten", "kleiner");
	}

	@Test
	public void testDua56SWOhneBetriebsMeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("SW", "SichtWeite", "652", "5 Minuten", "kleiner");
	}
	
	@Test
	public void testDua56HK() throws Exception {
		startDua56Test("HK", "Helligkeit", "-1", "4 Minuten", "BedingungImmerWahr");
	}

	@Test
	public void testDua56HKOhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("HK", "Helligkeit", "-1", "4 Minuten", "BedingungImmerWahr");
	}

	
	@Test
	public void testDua56RS() throws Exception {
		startDua56Test("RS", "RestSalz", "2", "3 Minuten", "größer");
	}

	@Test
	public void testDua56RSOhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("RS", "RestSalz", "2", "3 Minuten", "größer");
	}
	
	@Test
	public void testDua56WGM() throws Exception {
		startDua56Test("WGM", "WindGeschwindigkeitMittelWert", "3,1", "4 Minuten", "größer");
	}	
	
	@Test
	public void testDua56WGMOhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("WGM", "WindGeschwindigkeitMittelWert", "3,1", "4 Minuten", "größer");
	}	

	@Test
	public void testDua56WGS() throws Exception {
		startDua56Test("WGS", "WindGeschwindigkeitSpitzenWert", "3,3", "4 Minuten", "größer");
	}	

	@Test
	public void testDua56WGSOhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("WGS", "WindGeschwindigkeitSpitzenWert", "3,3", "4 Minuten", "größer");
	}	
	
	@Test
	public void testDua56WR() throws Exception {
		startDua56Test("WR", "WindRichtung", "-1", "2 Minuten", "BedingungImmerWahr");
	}

	@Test
	public void testDua56WROhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("WR", "WindRichtung", "-1", "2 Minuten", "BedingungImmerWahr");
	}
	
	@Test
	public void testDua56TT1() throws Exception {
		startDua56Test("TT1", "TemperaturInTiefe1", "nicht ermittelbar", "4 Minuten", "BedingungImmerWahr");
	}	

	@Test
	public void testDua56TT1OhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("TT1", "TemperaturInTiefe1", "nicht ermittelbar", "4 Minuten", "BedingungImmerWahr");
	}	
	
	@Test
	public void testDua56TT3() throws Exception {
		startDua56Test("TT3", "TemperaturInTiefe3", "nicht ermittelbar", "4 Minuten", "BedingungImmerWahr");
	}	

	@Test
	public void testDua56TT3OhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("TT3", "TemperaturInTiefe3", "nicht ermittelbar", "4 Minuten", "BedingungImmerWahr");
	}	
	
	@Test
	public void testDua56GT() throws Exception {
		startDua56Test("GT", "GefrierTemperatur", "-0,2", "5 Minuten", "kleinerGleich");
	}	

	@Test
	public void testDua56GTOhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("GT", "GefrierTemperatur", "-0,2", "5 Minuten", "kleinerGleich");
	}	
		
	@Test
	public void testDua56LT() throws Exception {
		startDua56Test("LT", "LuftTemperatur", "nicht ermittelbar", "5 Minuten", "BedingungImmerWahr");
	}	

	@Test
	public void testDua56LTOhneBetriebsmeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("LT", "LuftTemperatur", "nicht ermittelbar", "5 Minuten", "BedingungImmerWahr");
	}	
	
	@Test
	public void testDua56TPT() throws Exception {
		startDua56Test("TPT", "TaupunktTemperatur", "nicht ermittelbar", "5 Minuten", "BedingungImmerWahr");
	}	

	@Test
	public void testDua56TPTOhneBetriebsMeldungen() throws Exception {
		startDua56TestOhneBetriebsmeldungen("TPT", "TaupunktTemperatur", "nicht ermittelbar", "5 Minuten", "BedingungImmerWahr");
	}	
	
	@Test
	public void testDua56FBT() throws Exception {
		SystemObject sensor = _dataModel.getObject("ufd." + "FBT".toLowerCase());
		SystemObject sensor2 = _dataModel.getObject("ufd." + "NA".toLowerCase());
		AttributeGroup atg = _dataModel.getAttributeGroup("atg.ufds" + "FahrBahnOberFlächenTemperatur");
		AttributeGroup atg2 = _dataModel.getAttributeGroup("atg.ufds" + "NiederschlagsArt");
		DataDescription ddIn = new DataDescription(atg, _aspSend);
		DataDescription ddOut = new DataDescription(atg, _aspReceive);		
		DataDescription ddIn2 = new DataDescription(atg2, _aspSend);
		fakeParamApp.publishParam(sensor.getPid(), "atg.ufdsDifferenzialKontrolle" + "FahrBahnOberFlächenTemperatur", "{Operator:'" + "BedingungImmerWahr" + "'," + "FBT" + "Grenz:'" + "nicht ermittelbar" + "'," + "FBT" + "maxZeit:'" + "4 Minuten" + "'}");
		startTestCase("DUA56" + "FBT" + ".csv", ImmutableList.of(new DuADataIdentification(sensor, ddIn), new DuADataIdentification(sensor2, ddIn2)), ImmutableList.of(new DuADataIdentification(sensor, ddOut)), new Dua56Layout());
	}

	private void startDua56Test(final String type, final String typeLong, final String grenz, final String tGrenz, final String op) throws Exception {
		// Test mit Betriebsmeldugn an
		
		SystemObject[] sensor = {_dataModel.getObject("ufd." + type.toLowerCase())};
		AttributeGroup atg = _dataModel.getAttributeGroup("atg.ufds" + typeLong);
		DataDescription ddIn = new DataDescription(atg, _aspSend);
		DataDescription ddOut = new DataDescription(atg, _aspReceive);
		fakeParamApp.publishParam(sensor[0].getPid(), "atg.ufdsDifferenzialKontrolle" + typeLong, "{Operator:'" + op + "'," + type + "Grenz:'" + grenz + "'," + type + "maxZeit:'" + tGrenz + "'}");
		startTestCase("DUA56" + type + ".csv", sensor, sensor, ddIn, ddOut, new Dua56Layout());
	}

	private void startDua56TestOhneBetriebsmeldungen(final String type, final String typeLong, final String grenz, final String tGrenz, final String op) throws Exception {
		
		SystemObject[] sensor = {_dataModel.getObject("ufd." + type.toLowerCase())};
		AttributeGroup atg = _dataModel.getAttributeGroup("atg.ufds" + typeLong);
		DataDescription ddIn = new DataDescription(atg, _aspSend);
		DataDescription ddOut = new DataDescription(atg, _aspReceive);

		// Test mit Betriebsmeldugn aus

		fakeParamApp.publishParam(sensor[0].getPid(), "atg.ufdsDifferenzialKontrolle" + typeLong, "{Operator:'" + op + "'," + type + "Grenz:'" + grenz + "'," + type + "maxZeit:'" + 0 + " Sekunden'}");
		startTestCase("DUA56" + type + ".csv", sensor, sensor, ddIn, ddOut, new Dua56Layout() {
			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				return null;
			}

			@Override
			public Collection<String> getIgnored() {
				return Arrays.asList("Wert", "Implausibel");
			}
		});
	}

	
	@Test
	public void testDua57LT() throws Exception {
		startDua57Test("LT", "LuftTemperatur", "1,8 °C");
	}

	@Test
	public void testDua57LTOhneBetriebsmeldungen() throws Exception {
		startDua57TestOhneBetriebsmeldungen("LT", "LuftTemperatur", "1,8 °C");
	}
	
	@Test
	public void testDua57FBT() throws Exception {
		startDua57Test("FBT", "FahrBahnOberFlächenTemperatur", "11,0 °C");
	}
	
	@Test
	public void testDua57FBTOhneBetriebsmeldungen() throws Exception {
		startDua57TestOhneBetriebsmeldungen("FBT", "FahrBahnOberFlächenTemperatur", "11,0 °C");
	}

	@Test
	public void testDua57HK() throws Exception {
		startDua57Test("HK", "Helligkeit", "5432 Lx");
	}	
	
	@Test
	public void testDua57HKOhneBetriebsmeldungen() throws Exception {
		startDua57TestOhneBetriebsmeldungen("HK", "Helligkeit", "5432 Lx");
	}	

	@Test
	public void testDua57RLF() throws Exception {
		startDua57Test("RLF", "RelativeLuftFeuchte", "11 %");
	}	
	
	@Test
	public void testDua57RLFOhneBetriebsmeldungen() throws Exception {
		startDua57TestOhneBetriebsmeldungen("RLF", "RelativeLuftFeuchte", "11 %");
	}	

	@Test
	public void testDua57TPT() throws Exception {
		startDua57Test("TPT", "TaupunktTemperatur", "2,4 °C");
	}

	@Test
	public void testDua57TPTOhneBetriebsmeldungen() throws Exception {
		startDua57TestOhneBetriebsmeldungen("TPT", "TaupunktTemperatur", "2,4 °C");
	}
	
	@Test
	public void testDua57TT1() throws Exception {
		startDua57Test("TT1", "TemperaturInTiefe1", "1,7 °C");
	}	
	
	@Test
	public void testDua57TT1OhneBetriebsmeldungen() throws Exception {
		startDua57TestOhneBetriebsmeldungen("TT1", "TemperaturInTiefe1", "1,7 °C");
	}	

	@Test
	public void testDua57TT3() throws Exception {
		startDua57Test("TT3", "TemperaturInTiefe3", "1,7 °C");
	}	
	
	@Test
	public void testDua57TT3OhneBetriebsmeldungen() throws Exception {
		startDua57TestOhneBetriebsmeldungen("TT3", "TemperaturInTiefe3", "1,7 °C");
	}	

	@Test
	public void testDua57WFD() throws Exception {
		startDua57Test("WFD", "WasserFilmDicke", "0,8 mm");
	}
	
	@Test
	public void testDua57WFDOhneBetriebsmeldungen() throws Exception {
		startDua57TestOhneBetriebsmeldungen("WFD", "WasserFilmDicke", "0,8 mm");
	}

	@Test
	public void testDua57WGM() throws Exception {
		startDua57Test("WGM", "WindGeschwindigkeitMittelWert", "13,1");
	}
	
	@Test
	public void testDua57WGMOhneBetriebsmeldungen() throws Exception {
		startDua57TestOhneBetriebsmeldungen("WGM", "WindGeschwindigkeitMittelWert", "13,1");
	}

	@Test
	public void testDua57WGS() throws Exception {
		startDua57Test("WGS", "WindGeschwindigkeitSpitzenWert", "15");
	}

	@Test
	public void testDua57WGSOhneBetriebsmeldungen() throws Exception {
		startDua57TestOhneBetriebsmeldungen("WGS", "WindGeschwindigkeitSpitzenWert", "15");
	}

	private void startDua57Test(final String type, final String typeLong, final String grenz) throws Exception {
		// Test mit Betriebsmeldung an
		
		SystemObject[] sensor = {_dataModel.getObject("ufd." + type.toLowerCase())};
		AttributeGroup atg = _dataModel.getAttributeGroup("atg.ufds" + typeLong);
		DataDescription ddIn = new DataDescription(atg, _aspSend);
		DataDescription ddOut = new DataDescription(atg, _aspReceive);
		fakeParamApp.publishParam(sensor[0].getPid(), "atg.ufdsAnstiegAbstiegKontrolle" + typeLong, "{" + type + "maxDiff:'" + grenz + "'}");
		startTestCase("DUA57" + type + ".csv", sensor, sensor, ddIn, ddOut, new Dua57Layout());
	}

	private void startDua57TestOhneBetriebsmeldungen(final String type, final String typeLong, final String grenz) throws Exception {
		
		SystemObject[] sensor = {_dataModel.getObject("ufd." + type.toLowerCase())};
		AttributeGroup atg = _dataModel.getAttributeGroup("atg.ufds" + typeLong);
		DataDescription ddIn = new DataDescription(atg, _aspSend);
		DataDescription ddOut = new DataDescription(atg, _aspReceive);

		fakeParamApp.publishParam(sensor[0].getPid(), "atg.ufdsAnstiegAbstiegKontrolle" + typeLong, "{" + type + "maxDiff:'" + 0 + "'}");
		startTestCase("DUA57" + type + ".csv", sensor, sensor, ddIn, ddOut, new Dua57Layout(){
			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				return null;
			}

			@Override
			public Collection<String> getIgnored() {
				return Arrays.asList("Wert", "Implausibel");
			}
		});
	}
	
	@Test
	public void testDua5859() throws Exception {
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
		startTestCase("DUA58.59.csv", send, rec, new Dua58Layout());
	}
	
	@Test
	public void testDua5859NoMessage() throws Exception {
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
		                          "{erzeugeBetriebsmeldungMeteorologischeKontrolle:'Nein'," +
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
		startTestCase("DUA58.59.csv", send, rec, new Dua58Layout(){
			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				return null;
			}

			@Override
			public Collection<String> getIgnored() {
				return Arrays.asList("Wert", "Implausibel");
			}
		});
	}
	

	private static class Dua56Layout extends ColumnLayout {
		@Override
		public int getColumnCount(final boolean in) {
			return 1;
		}

		@Override
		public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
			if(item.getAttributeType().getPid().equals("atl.ufdsNiederschlagsArt")){
				item.getUnscaledValue("Wert").set(Integer.parseInt(row.get(realCol+1)));
			}
			else {
				item.getTextValue("Wert").setText(row.get(realCol));
				if(!item.isDefined()){
					item.getUnscaledValue("Wert").set(Integer.parseInt(row.get(realCol)));
				}
			}
			if(!in) {
				item.getItem("Status").getItem("Erfassung").getTextValue("NichtErfasst").setText(row.get(realCol + 2));
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 3));
				String percent = row.get(realCol + 5);
				if(percent.endsWith("%")) {
					percent = percent.substring(0, percent.length() - 1);
				}
				percent = percent.replace(',', '.');
				item.getItem("Güte").getUnscaledValue("Index").set(Double.parseDouble(percent) * 100);
			}
			else {
				item.getItem("Güte").getUnscaledValue("Index").set(10000);
				boolean invalid = row.get(realCol).equals("-1") || row.get(realCol).equals("-1001");
				item.getItem("Status").getItem("Erfassung").getTextValue("NichtErfasst").setText(invalid ? "Ja" : "Nein");
			}
		}

		@Override
		public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
			String s = fullRow.get(10);
			if(s.equals("Ja")) return "[DUA-PP-UDK";
			return null;
		}

		@Override
		public Collection<String> getIgnored() {
			return Arrays.asList("T");
		}
	}
	
	private static class Dua57Layout extends ColumnLayout {
		@Override
		public int getColumnCount(final boolean in) {
			return 1;
		}

		@Override
		public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
			item.getTextValue("Wert").setText(row.get(realCol));
			if(!item.isDefined()){
				item.getUnscaledValue("Wert").set(Integer.parseInt(row.get(realCol)));
			}
			if(!in) {
				item.getItem("Status").getItem("Erfassung").getTextValue("NichtErfasst").setText(row.get(realCol + 2));
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 3));
				String percent = row.get(realCol + 5);
				if(percent.endsWith("%")) {
					percent = percent.substring(0, percent.length() - 1);
				}
				percent = percent.replace(',', '.');
				item.getItem("Güte").getUnscaledValue("Index").set(Double.parseDouble(percent) * 100);
			}
			else {
				item.getItem("Güte").getUnscaledValue("Index").set(10000);
				item.getItem("Status").getItem("Erfassung").getTextValue("NichtErfasst").setText(row.get(realCol + 1));
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 2));
			}
		}

		@Override
		public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
			if(fullRow.get(4).equals("Nein") && fullRow.get(12).equals("Ja")) return "[DUA-PP-UAK";
			return null;
		}

		@Override
		public Collection<String> getIgnored() {
			return Arrays.asList("T");
		}
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
