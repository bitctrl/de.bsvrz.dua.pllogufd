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

package de.bsvrz.dua.pllogufd.testmeteo;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoParameter.MeteoParameterType;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule1;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule10;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule11;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule12;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule13;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule2;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule3;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule4;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule5;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule6;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule7;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule8;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule9;
import de.bsvrz.dua.pllogufd.vew.PllogUfdOptions;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.funclib.operatingMessage.OperatingMessage;

/**
 * Eine konkrete Messstelle für die Meteorologische Kontrolle
 *
 * @author Kappich Systemberatung
 */
public class MeteoMessstelle {

	/** Template für Betriebsmeldungstext. */
	private final MessageTemplate MESSAGE_TEMPLATE = new MessageTemplate(MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN, MessageTemplate.set("attr", ", ", "Messwert ", "Messwerte "),
			MessageTemplate.fixed(" bei meteorologischer Kontrolle an Messstelle "), MessageTemplate.object(),
			MessageTemplate.fixed(" auf fehlerhaft gesetzt, da "), MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". "), MessageTemplate.ids());

	private long dataTime;
	private Map<SystemObject, UmfeldDatenArt> sensoren = new LinkedHashMap<>();
	private final Map<UmfeldDatenArt, ResultData> sensorWerte = new LinkedHashMap<>();
	private final Set<UmfeldDatenArt> publiziert = new LinkedHashSet<>();
	private List<MeteoRule> rules = new ArrayList<>();
	private List<MeteoRule> checkedRules = new ArrayList<>();

	private final DUAUmfeldDatenMessStelle messStelle;
	private final MeteoParameter parameter;

	private PllogUfdOptions options;

	/**
	 * Logger
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Erstellt eine neue MeteoMessstelle
	 * 
	 * @param connection
	 *            Verbindung
	 * @param messStelle
	 *            Messstellen-Objekt
	 */
	public MeteoMessstelle(final ClientDavInterface connection, final DUAUmfeldDatenMessStelle messStelle, PllogUfdOptions options) {
		this.options = options;
		this.parameter = new MeteoParameter(connection, messStelle);
		this.messStelle = messStelle;
		initSensor(messStelle, UmfeldDatenArt.ni);
		initSensor(messStelle, UmfeldDatenArt.ns);
		initSensor(messStelle, UmfeldDatenArt.wfd);
		initSensor(messStelle, UmfeldDatenArt.fbz);
		initSensor(messStelle, UmfeldDatenArt.lt);
		initSensor(messStelle, UmfeldDatenArt.rlf);
		initSensor(messStelle, UmfeldDatenArt.sw);

		initRules();
	}

	private void initSensor(final DUAUmfeldDatenMessStelle messStelle, final UmfeldDatenArt datenArt) {
		DUAUmfeldDatenSensor hauptSensor = messStelle.getHauptSensor(datenArt);
		if (hauptSensor == null) {
			return;
		}

		SystemObject sensor = hauptSensor.getObjekt();
		if (sensor != null) {
			sensoren.put(sensor, datenArt);
		}
	}

	private void initRules() {
		initRule(new MeteoRule1());
		initRule(new MeteoRule2());
		initRule(new MeteoRule3());
		initRule(new MeteoRule4());
		initRule(new MeteoRule5());
		initRule(new MeteoRule6());
		initRule(new MeteoRule7());
		initRule(new MeteoRule8());
		initRule(new MeteoRule9());
		initRule(new MeteoRule10());
		initRule(new MeteoRule11());
		initRule(new MeteoRule12());
		initRule(new MeteoRule13());
	}

	private void initRule(MeteoRule rule) {
		if (rule.isValidFor(sensoren.values())) {
			rules.add(rule);
		}
	}

	public Collection<ResultData> updateData(final ResultData resultData) {

		Collection<ResultData> resultList = new ArrayList<>();

		if (!sensoren.containsKey(resultData.getObject())) {
			resultList.add(resultData);
			return resultList;
		}

		ResultData data;
		if (resultData.hasData()) {
			data = resultData;
		} else {
			data = null;
		}

		long timeStamp = resultData.getDataTime();
		resultList.addAll(setData(resultData.getObject(), timeStamp, data));
		return resultList;
	}

	public MeteoParameter getParameter() {
		return parameter;
	}

	private Collection<ResultData> setData(SystemObject sensorObject, long timeStamp, ResultData data) {

		Collection<ResultData> resultList = new ArrayList<>();

		if (data == null) {
			return resultList;
		}

		if (dataTime != 0) {
			if (timeStamp < dataTime) {
				return resultList;
			}
			if (timeStamp > dataTime) {
				checkRules(resultList, true);
				reset();
			}
		}

		dataTime = timeStamp;

		UmfeldDatenArt umfeldDatenArt = sensoren.get(sensorObject);
		if (data != null) {
			sensorWerte.put(umfeldDatenArt, data);
		}

		checkRules(resultList, false);

		if (data != null) {
			if (!publiziert.contains(umfeldDatenArt)) {
				boolean publication = true;
				for (MeteoRule rule : rules) {
					if (rule.getResultTypes().contains(umfeldDatenArt)) {
						publication = false;
						break;
					}
				}
				if (publication) {
					publiziert.add(umfeldDatenArt);
					resultList.add(data);
				}
			}
		}

		return resultList;
	}

	private void checkRules(Collection<ResultData> resultList, boolean force) {

		Set<MeteoRuleCondition> verletzteBedingungen = new LinkedHashSet<>();
		Set<UmfeldDatenArt> implausibleDatenArten = new LinkedHashSet<>();
		Set<UmfeldDatenArt> plausibleDatenArten = new LinkedHashSet<>();
		Set<String> ids = new LinkedHashSet<>();

		for (MeteoRule rule : rules) {
			if (checkedRules.contains(rule)) {
				continue;
			}

			if (!force) {
				if (!rule.isEvaluableFor(this)) {
					continue;
				}

				Set<MeteoRule> connectedRules = new LinkedHashSet<>(rules);
				connectedRules.removeAll(checkedRules);
				connectedRules.remove(rule);
				boolean checkable = true;
				for (MeteoRule connectedRule : connectedRules) {
					if (!connectedRule.isEvaluableFor(this)) {
						checkable = false;
						break;
					}
				}

				if (!checkable) {
					continue;
				}
			}
			plausibleDatenArten.addAll(rule.pruefe(this, verletzteBedingungen, implausibleDatenArten, ids, options));
			checkedRules.add(rule);
		}

		plausibleDatenArten.removeAll(implausibleDatenArten);

		for (Entry<UmfeldDatenArt, ResultData> entry : sensorWerte.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			if (!publiziert.contains(entry.getKey())) {
				if (plausibleDatenArten.contains(entry.getKey())) {
					resultList.add(entry.getValue());
				}
				if (implausibleDatenArten.contains(entry.getKey())) {
					UmfeldDatenSensorDatum umfeldDatenSensorDatum = new UmfeldDatenSensorDatum(entry.getValue());
					umfeldDatenSensorDatum.getWert().setFehlerhaftAn();
					umfeldDatenSensorDatum.getDatum(); // Workaround fehlende
														// Aktualisierung
					umfeldDatenSensorDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					resultList.add(umfeldDatenSensorDatum.getVeraendertesOriginalDatum());
				}
			}
		}
		publiziert.addAll(plausibleDatenArten);
		publiziert.addAll(implausibleDatenArten);

		if (!ids.isEmpty()) {
			OperatingMessage message = MESSAGE_TEMPLATE.newMessage(messStelle.getObjekt());
			for (String id : ids) {
				message.addId(id);
			}
			for (UmfeldDatenArt art : implausibleDatenArten) {
				message.add("attr", art.getName() + " " + art.getAbkuerzung());
			}
			for (MeteoRuleCondition condition : verletzteBedingungen) {
				message.add("values", formatCondition(condition));
			}
			if (parameter.isSendMessage()) {
				message.send();
			} else {
				_debug.info(message.toString());
			}
		}
	}

	private Object formatCondition(MeteoRuleCondition condition) {
		String result = condition.getTemplateStr();
		List<Object> arguments = condition.getArguments();

		for (int idx = 0; idx < arguments.size(); idx++) {
			String pattern = "\\{" + idx + "\\}";
			UmfeldDatenSensorWert wert = null;
			Object argument = arguments.get(idx);
			if (argument instanceof MeteoParameterType) {
				wert = parameter.getParameter((MeteoParameterType) argument);
			} else if (argument instanceof UmfeldDatenArt) {
				wert = getData((UmfeldDatenArt) argument);
			}

			if (wert == null) {
				result = result.replaceAll(pattern, "(null)");
			} else {
				NumberFormat numberInstance = NumberFormat.getNumberInstance();
				numberInstance.setGroupingUsed(false);
				result = result.replaceAll(pattern, numberInstance.format(wert.getSkaliertenWert()));
			}
		}

		return result;
	}

	public void reset() {
		sensorWerte.clear();
		checkedRules.clear();
		publiziert.clear();
	}

	public boolean hasData(UmfeldDatenArt art) {
		return sensorWerte.containsKey(art);
	}

	public long getDataTime() {
		return dataTime;
	}

	public UmfeldDatenSensorWert getData(UmfeldDatenArt datenArt) {
		ResultData resultData = sensorWerte.get(datenArt);
		if ((resultData == null) || !resultData.hasData()) {
			return null;
		}

		UmfeldDatenSensorDatum wert = new UmfeldDatenSensorDatum(resultData);
		return wert.getWert();
	}

	/* Hilfsfunktionen für die Niederschlagsart. */

	public boolean keinNiederschlag() {
		UmfeldDatenSensorWert data = getData(UmfeldDatenArt.ns);
		return (data != null) && data.isOk() && data.getWert() == 0;
	}

	public boolean niederschlag() {
		UmfeldDatenSensorWert data = getData(UmfeldDatenArt.ns);
		return (data != null) && data.isOk() && data.getWert() >= 50 && data.getWert() <= 69;
	}

	public boolean regen() {
		UmfeldDatenSensorWert data = getData(UmfeldDatenArt.ns);
		return (data != null) && data.isOk()
				&& ((data.getWert() >= 40 && data.getWert() <= 69) || (data.getWert() >= 80 && data.getWert() <= 84));
	}

	public boolean schnee() {
		UmfeldDatenSensorWert data = getData(UmfeldDatenArt.ns);
		return (data != null) && data.isOk()
				&& ((data.getWert() >= 70 && data.getWert() <= 78) || (data.getWert() >= 85 && data.getWert() <= 87));
	}

	/* Hilfsfunktionen für den Fahrbahnzustand. */

	public boolean fbzTrocken() {
		UmfeldDatenSensorWert fbzWert = getData(UmfeldDatenArt.fbz);
		if ((fbzWert == null) || !fbzWert.isOk()) {
			return false;
		}

		return fbzWert.getWert() == 0;
	}

	public boolean fbzNass() {
		UmfeldDatenSensorWert fbzWert = getData(UmfeldDatenArt.fbz);
		if ((fbzWert == null) || !fbzWert.isOk()) {
			return false;
		}

		return fbzWert.getWert() != 0;
	}

	
	/* Hilfsfunktionen für die Niederschlagsintensität. */
	public boolean niGroesserGrenzNs() {
		UmfeldDatenSensorWert niWert = getData(UmfeldDatenArt.ni);
		if ((niWert == null) || !niWert.isOk()) {
			return false;
		}

		UmfeldDatenSensorWert niGrenzNs = parameter.getParameter(MeteoParameterType.NI_GRENZ_NS);
		if ((niGrenzNs == null) || !niGrenzNs.isOk()) {
			return false;
		}

		return niWert.getWert() > niGrenzNs.getWert();
	}

	public boolean niGroesserGrenzWfd() {
		UmfeldDatenSensorWert niWert = getData(UmfeldDatenArt.ni);
		if ((niWert == null) || !niWert.isOk()) {
			return false;
		}

		UmfeldDatenSensorWert niGrenzWfd = parameter.getParameter(MeteoParameterType.NI_GRENZ_WFD);
		if ((niGrenzWfd == null) || !niGrenzWfd.isOk()) {
			return false;
		}

		return niWert.getWert() > niGrenzWfd.getWert();
	}

	public boolean niIsNull() {
		UmfeldDatenSensorWert niWert = getData(UmfeldDatenArt.ni);
		if ((niWert == null) || !niWert.isOk()) {
			return false;
		}

		return niWert.getWert() == 0;
	}

	/* Hilfsfunktionen für die Restluftfeuchte. */
	public boolean rlfGroesserNass() {
		UmfeldDatenSensorWert rlfWert = getData(UmfeldDatenArt.rlf);
		if ((rlfWert == null) || !rlfWert.isOk()) {
			return false;
		}

		UmfeldDatenSensorWert rlfGrenzNass = parameter.getParameter(MeteoParameterType.RLF_GRENZ_NASS);
		if ((rlfGrenzNass == null) || !rlfGrenzNass.isOk()) {
			return false;
		}

		return rlfWert.getWert() > rlfGrenzNass.getWert();
	}

	public boolean rlfKleinerTrocken() {
		UmfeldDatenSensorWert rlfWert = getData(UmfeldDatenArt.rlf);
		if ((rlfWert == null) || !rlfWert.isOk()) {
			return false;
		}

		UmfeldDatenSensorWert rlfGrenzTrocken = parameter.getParameter(MeteoParameterType.RLF_GRENZ_TROCKEN);
		if ((rlfGrenzTrocken == null) || !rlfGrenzTrocken.isOk()) {
			return false;
		}

		return rlfWert.getWert() < rlfGrenzTrocken.getWert();
	}

	public boolean rlfUndef() {
		UmfeldDatenSensorWert rlfWert = getData(UmfeldDatenArt.rlf);
		if (rlfWert == null) {
			return false;
		}
		return !rlfWert.isOk();
	}

	public boolean rlfZwischenTrockenUndNass() {
		UmfeldDatenSensorWert rlfWert = getData(UmfeldDatenArt.rlf);
		if ((rlfWert == null) || !rlfWert.isOk()) {
			return false;
		}

		UmfeldDatenSensorWert rlfGrenzTrocken = parameter.getParameter(MeteoParameterType.RLF_GRENZ_TROCKEN);
		if ((rlfGrenzTrocken == null) || !rlfGrenzTrocken.isOk()) {
			return false;
		}

		UmfeldDatenSensorWert rlfGrenzNass = parameter.getParameter(MeteoParameterType.RLF_GRENZ_NASS);
		if ((rlfGrenzNass == null) || !rlfGrenzNass.isOk()) {
			return false;
		}

		return (rlfWert.getWert() >= rlfGrenzTrocken.getWert()) && (rlfWert.getWert() <= rlfGrenzNass.getWert());
	}

	/* Hilfsfunktionen für die Wasserfilmdicke. */
	public boolean wfdKleinerGleichTrocken() {
		UmfeldDatenSensorWert wfdWert = getData(UmfeldDatenArt.wfd);
		if ((wfdWert == null) || !wfdWert.isOk()) {
			return false;
		}

		UmfeldDatenSensorWert wfdGrenzTrocken = parameter.getParameter(MeteoParameterType.WFD_GRENZ_TROCKEN);
		if ((wfdGrenzTrocken == null) || !wfdGrenzTrocken.isOk()) {
			return false;
		}

		return wfdWert.getWert() <= wfdGrenzTrocken.getWert();
	}

	public boolean wfdGroesserNull() {
		UmfeldDatenSensorWert wfdWert = getData(UmfeldDatenArt.wfd);
		if ((wfdWert == null) || !wfdWert.isOk()) {
			return false;
		}

		return wfdWert.getWert() > 0;
	}

	public boolean wfdIsNull() {
		UmfeldDatenSensorWert wfdWert = getData(UmfeldDatenArt.wfd);
		if ((wfdWert == null) || !wfdWert.isOk()) {
			return false;
		}

		return wfdWert.getWert() == 0;
	}

	/* Hilfsfunktionen für die Sichtweite. */
	public boolean swKleinerGleichGrenze() {
		UmfeldDatenSensorWert swWert = getData(UmfeldDatenArt.sw);
		if ((swWert == null) || !swWert.isOk()) {
			return false;
		}

		UmfeldDatenSensorWert swGrenz = parameter.getParameter(MeteoParameterType.SW_GRENZ);
		if ((swGrenz == null) || !swGrenz.isOk()) {
			return false;
		}

		return swWert.getWert() <= swGrenz.getWert();
	}

	/* Hilfsfunktionen für die LuftTemperatur. */
	public boolean ltKleinerGrenzRegen() {
		UmfeldDatenSensorWert ltWert = getData(UmfeldDatenArt.lt);
		if ((ltWert == null) || !ltWert.isOk()) {
			return false;
		}

		UmfeldDatenSensorWert ltGrenzRegen = parameter.getParameter(MeteoParameterType.LT_GRENZ_REGEN);
		if ((ltGrenzRegen == null) || !ltGrenzRegen.isOk()) {
			return false;
		}

		return ltWert.getWert() < ltGrenzRegen.getWert();
	}

	public boolean ltGroesserGrenzSchnee() {
		UmfeldDatenSensorWert ltWert = getData(UmfeldDatenArt.lt);
		if ((ltWert == null) || !ltWert.isOk()) {
			return false;
		}

		UmfeldDatenSensorWert ltGrenzSchnee = parameter.getParameter(MeteoParameterType.LT_GRENZ_SCHNEE);
		if ((ltGrenzSchnee == null) || !ltGrenzSchnee.isOk()) {
			return false;
		}

		return ltWert.getWert() > ltGrenzSchnee.getWert();
	}

}
