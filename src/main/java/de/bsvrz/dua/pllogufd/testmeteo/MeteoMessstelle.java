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

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.vew.PllogUfdOptions;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.funclib.operatingMessage.OperatingMessage;

import java.text.NumberFormat;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Eine konkrete Messstelle für die Meteorologische Kontrolle
 *
 * @author Kappich Systemberatung
 */
public class MeteoMessstelle implements ClientReceiverInterface {

	/**
	 * Messstellen-Objekt
	 */
	private final DUAUmfeldDatenMessStelle _messStelle;

	private final PllogUfdOptions options;

	/**
	 * NiederschlagsIntensitäts-Sensor oder null
	 */
	private SystemObject _niSensor = null;
	/**
	 * NiederschlagsArt-Sensor oder null
	 */
	private SystemObject _nsSensor = null;
	/**
	 * WasserFilmDicke-Sensor oder null
	 */
	private SystemObject _wfdSensor = null;
	/**
	 * Fahrbahnoberflächenzustand-Sensor oder null
	 */
	private SystemObject _fbzSensor = null;
	/**
	 * Lufttemperatur-Sensor oder null
	 */
	private SystemObject _ltSensor = null;
	/**
	 * RLF-Sensor oder null
	 */
	private SystemObject _rlfSensor = null;
	/**
	 * Sichtweitensensor oder null
	 */
	private SystemObject _swSensor = null;

	/**
	 * Aktueller NI-Wert
	 */
	private UmfeldDatenSensorWert _niWert = null;
	/**
	 * Aktueller NS-Wert
	 */
	private UmfeldDatenSensorWert _nsWert = null;
	/**
	 * Aktueller WFD-Wert
	 */
	private UmfeldDatenSensorWert _wfdWert = null;
	/**
	 * Aktueller FBZ-Wert
	 */
	private UmfeldDatenSensorWert _fbzWert = null;
	/**
	 * Aktueller LT-Wert
	 */
	private UmfeldDatenSensorWert _ltWert = null;
	/**
	 * Aktueller RLF-Wert
	 */
	private UmfeldDatenSensorWert _rlfWert = null;
	/**
	 * Aktueller SW-Wert
	 */
	private UmfeldDatenSensorWert _swWert = null;

	/**
	 * Aktuelle Datenarten, die implausible Daten liefern
	 */
	private final Set<UmfeldDatenArt> _implausibleDatenArten = new LinkedHashSet<>();
	/**
	 * Aktuelle verletze Bedingungen (für die Ausgabe in der Betriebsmeldung)
	 */
	private final Set<String> _verletzteBedingungen = new LinkedHashSet<>();
	/**
	 * Aktuell vorliegende Betriebsmeldungs-IDs
	 */
	private final Set<String> _ids = new LinkedHashSet<>();

	/**
	 * Aktueller Parameterwert _niGrenzNs
	 */
	private UmfeldDatenSensorWert _niGrenzNs;
	/**
	 * Aktueller Parameterwert _niGrenzWfd
	 */
	private UmfeldDatenSensorWert _niGrenzWfd;
	/**
	 * Aktueller Parameterwert _wfdGrenzTrocken
	 */
	private UmfeldDatenSensorWert _wfdGrenzTrocken;
	/**
	 * Aktueller Parameterwert _ltGrenzRegen
	 */
	private UmfeldDatenSensorWert _ltGrenzRegen;
	/**
	 * Aktueller Parameterwert _ltGrenzSchnee
	 */
	private UmfeldDatenSensorWert _ltGrenzSchnee;
	/**
	 * Aktueller Parameterwert _rlfGrenzTrocken
	 */
	private UmfeldDatenSensorWert _rlfGrenzTrocken;
	/**
	 * Aktueller Parameterwert _rlfGrenzNass
	 */
	private UmfeldDatenSensorWert _rlfGrenzNass;
	/**
	 * Aktueller Parameterwert _swGrenz
	 */
	private UmfeldDatenSensorWert _swGrenz;

	/**
	 * Sollen Betriebsmeldungen versendet werden?
	 */
	private boolean _sendMessage = true;

	/**
	 * Wurden die aktuellen Eingangsdaten bereits geprüft? Verhindert, dass
	 * Meldungen doppelt verschickt werden, obwohl sich nichts ändert.
	 */
	private boolean _geprueft = false;

	/**
	 * Logger
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Template für Betriebsmeldungstext
	 */
	private final MessageTemplate MESSAGE_TEMPLATE = new MessageTemplate(MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN, MessageTemplate.set("attr", ", ", "Messwert ", "Messwerte "),
			MessageTemplate.fixed(" bei meteorologischer Kontrolle an Messstelle "), MessageTemplate.object(),
			MessageTemplate.fixed(" auf fehlerhaft gesetzt, da "), MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". "), MessageTemplate.ids());

	/**
	 * Erstellt eine neue MeteoMessstelle
	 * 
	 * @param connection
	 *            Verbindung
	 * @param messStelle
	 *            Messstellen-Objekt
	 */
	public MeteoMessstelle(final ClientDavInterface connection, final DUAUmfeldDatenMessStelle messStelle,
			PllogUfdOptions options) {
		_messStelle = messStelle;
		this.options = options;
		_niSensor = getObjekt(messStelle, UmfeldDatenArt.ni);
		_nsSensor = getObjekt(messStelle, UmfeldDatenArt.ns);
		_wfdSensor = getObjekt(messStelle, UmfeldDatenArt.wfd);
		_fbzSensor = getObjekt(messStelle, UmfeldDatenArt.fbz);
		_ltSensor = getObjekt(messStelle, UmfeldDatenArt.lt);
		_rlfSensor = getObjekt(messStelle, UmfeldDatenArt.rlf);
		_swSensor = getObjekt(messStelle, UmfeldDatenArt.sw);
		DataModel dataModel = connection.getDataModel();
		String atgPid = "atg.ufdmsParameterMeteorologischeKontrolle";
		AttributeGroup attributeGroup = dataModel.getAttributeGroup(atgPid);
		Aspect aspect = dataModel.getAspect("asp.parameterSoll");

		if (attributeGroup == null) {
			_debug.fine("Es konnte keine Parameter-Attributgruppe für die " + "Meteorologische Kontrolle des Objektes "
					+ messStelle + " bestimmt werden\n" + "Atg-Name: " + atgPid);
			return;
		}

		connection.subscribeReceiver(this, messStelle.getObjekt(), new DataDescription(attributeGroup, aspect),
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	protected SystemObject getObjekt(final DUAUmfeldDatenMessStelle messStelle, final UmfeldDatenArt datenArt) {
		DUAUmfeldDatenSensor hauptSensor = messStelle.getHauptSensor(datenArt);
		if (hauptSensor == null)
			return null;
		return hauptSensor.getObjekt();
	}

	public void updateData(final ResultData resultData) {
		UmfeldDatenSensorWert data;
		if (resultData.hasData()) {
			data = new UmfeldDatenSensorDatum(resultData).getWert();
		} else {
			data = null;
		}

		if (resultData.getObject().equals(_niSensor)) {
			_niWert = data;
		} else if (resultData.getObject().equals(_nsSensor)) {
			_nsWert = data;
		} else if (resultData.getObject().equals(_wfdSensor)) {
			_wfdWert = data;
		} else if (resultData.getObject().equals(_fbzSensor)) {
			_fbzWert = data;
		} else if (resultData.getObject().equals(_ltSensor)) {
			_ltWert = data;
		} else if (resultData.getObject().equals(_rlfSensor)) {
			_rlfWert = data;
		} else if (resultData.getObject().equals(_swSensor)) {
			_swWert = data;
		}
		_geprueft = false;
	}

	public ResultData plausibilisiere(final ResultData resultData) {
		if (!resultData.hasData()) {
			return resultData;
		} else {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(resultData.getObject());
			} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
				return resultData;
			}

			pruefe();

			if (_implausibleDatenArten.contains(datenArt)) {
				UmfeldDatenSensorDatum umfeldDatenSensorDatum = new UmfeldDatenSensorDatum(resultData);
				umfeldDatenSensorDatum.getWert().setFehlerhaftAn();
				umfeldDatenSensorDatum.getDatum(); // Workaround fehlende
													// Aktualisierung
				umfeldDatenSensorDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				return umfeldDatenSensorDatum.getVeraendertesOriginalDatum();
			}
			return resultData;
		}
	}

	static int run = 1;

	private void pruefe() {
		if (_geprueft)
			return;
		_geprueft = true;
		_verletzteBedingungen.clear();
		_implausibleDatenArten.clear();
		_ids.clear();

		// Prüfung 1
		if (isOk(_nsWert) && keinNiederschlag() && isOk(_niWert) && isOk(_niGrenzNs)
				&& _niWert.getWert() > _niGrenzNs.getWert() && isOk(_rlfWert) && isOk(_rlfGrenzNass)
				&& _rlfWert.getWert() > _rlfGrenzNass.getWert()) {
			_implausibleDatenArten.add(UmfeldDatenArt.ns);
			_verletzteBedingungen.add("NS=Kein Niederschlag, " + "NI=" + formatWert(_niWert) + " mm/h > "
					+ formatWert(_niGrenzNs) + " mm/h, " + "RLF=" + formatWert(_rlfWert) + " % rF > "
					+ formatWert(_rlfGrenzNass) + " % rF");
			_ids.add("[DUA-PP-MK01]");
		}

		// Prüfung 2
		if (isOk(_nsWert) && niederschlag() && isOk(_niWert) && _niWert.getWert() == 0 && isOk(_rlfWert)
				&& isOk(_rlfGrenzTrocken) && _rlfWert.getWert() < _rlfGrenzTrocken.getWert()) {
			_implausibleDatenArten.add(UmfeldDatenArt.ns);
			_verletzteBedingungen.add("NS=Niederschlag, " + "NI=" + formatWert(_niWert) + " mm/h, " + "RLF="
					+ formatWert(_rlfWert) + " % rF < " + formatWert(_rlfGrenzTrocken) + " % rF");
			_ids.add("[DUA-PP-MK02]");
		}

		// Prüfung 3
		if (isOk(_nsWert) && keinNiederschlag() && isOk(_niWert) && isOk(_niGrenzNs)
				&& _niWert.getWert() > _niGrenzNs.getWert() && isOk(_rlfWert) && isOk(_rlfGrenzTrocken)
				&& _rlfWert.getWert() < _rlfGrenzTrocken.getWert()) {
			_implausibleDatenArten.add(UmfeldDatenArt.ni);
			_verletzteBedingungen.add("NS=Kein Niederschlag, " + "NI=" + formatWert(_niWert) + " mm/h > "
					+ formatWert(_niGrenzNs) + " mm/h, " + "RLF=" + formatWert(_rlfWert) + " % rF < "
					+ formatWert(_rlfGrenzTrocken) + " % rF");
			_ids.add("[DUA-PP-MK03]");
		}

		// Prüfung 4
		if (isOk(_nsWert) && niederschlag() && isOk(_niWert) && _niWert.getWert() == 0 && isOk(_rlfWert)
				&& isOk(_rlfGrenzNass) && _rlfWert.getWert() > _rlfGrenzNass.getWert()) {
			_implausibleDatenArten.add(UmfeldDatenArt.ni);
			_verletzteBedingungen.add("NS=Niederschlag, " + "NI=" + formatWert(_niWert) + " mm/h, " + "RLF="
					+ formatWert(_rlfWert) + " % rF > " + formatWert(_rlfGrenzNass) + " % rF");
			_ids.add("[DUA-PP-MK04]");
		}

		// Prüfung 5
		if (isOk(_niWert) && isOk(_niGrenzWfd) && _niWert.getWert() > _niGrenzWfd.getWert() && isOk(_wfdWert)
				&& isOk(_wfdGrenzTrocken) && _wfdWert.getWert() <= _wfdGrenzTrocken.getWert() && isOk(_rlfWert)
				&& isOk(_rlfGrenzTrocken) && _rlfWert.getWert() < _rlfGrenzTrocken.getWert()) {
			_implausibleDatenArten.add(UmfeldDatenArt.ni);
			_verletzteBedingungen.add("NI=" + formatWert(_niWert) + " mm/h, " + "WFD=" + formatWert(_wfdWert)
					+ " mm <= " + formatWert(_wfdGrenzTrocken) + " mm, " + "RLF=" + formatWert(_rlfWert) + " % rF < "
					+ formatWert(_rlfGrenzTrocken) + " % rF");
			_ids.add("[DUA-PP-MK05]");
		}

		// Prüfung 6
		if (isOk(_niWert) && isOk(_niGrenzWfd) && _niWert.getWert() > _niGrenzWfd.getWert() && isOk(_wfdWert)
				&& isOk(_wfdGrenzTrocken) && _wfdWert.getWert() <= _wfdGrenzTrocken.getWert() && isOk(_rlfWert)
				&& isOk(_rlfGrenzNass) && _rlfWert.getWert() > _rlfGrenzNass.getWert()) {
			_implausibleDatenArten.add(UmfeldDatenArt.wfd);
			_verletzteBedingungen.add("NI=" + formatWert(_niWert) + " mm/h, " + "WFD=" + formatWert(_wfdWert)
					+ " mm <= " + formatWert(_wfdGrenzTrocken) + " mm, " + "RLF=" + formatWert(_rlfWert) + " % rF > "
					+ formatWert(_rlfGrenzNass) + " % rF");
			_ids.add("[DUA-PP-MK06]");
		}

		// Prüfung 7
		if (isOk(_swWert) && isOk(_swGrenz) && _swWert.getWert() <= _swGrenz.getWert() && isOk(_nsWert)
				&& keinNiederschlag() && isOk(_rlfWert) && isOk(_rlfGrenzTrocken)
				&& _rlfWert.getWert() < _rlfGrenzTrocken.getWert()) {
			_implausibleDatenArten.add(UmfeldDatenArt.sw);
			_verletzteBedingungen.add(
					"SW=" + formatWert(_swWert) + " m <= " + formatWert(_swGrenz) + " m, " + "NS=Kein Niederschlag, "
							+ "RLF=" + formatWert(_rlfWert) + " % rF < " + formatWert(_rlfGrenzTrocken) + " % rF");
			_ids.add("[DUA-PP-MK07]");
		}

		// Prüfung 8
		if (isOk(_nsWert) && regen() && isOk(_ltWert) && isOk(_ltGrenzRegen)
				&& _ltWert.getWert() < _ltGrenzRegen.getWert()) {
			_implausibleDatenArten.add(UmfeldDatenArt.ns);
			_verletzteBedingungen
					.add("NS=Regen, " + "LT=" + formatWert(_ltWert) + " °C < " + formatWert(_ltGrenzRegen) + " °C");
			_ids.add("[DUA-PP-MK08]");
		}

		// Prüfung 9
		if (isOk(_nsWert) && schnee() && isOk(_ltWert) && isOk(_ltGrenzSchnee)
				&& _ltWert.getWert() > _ltGrenzSchnee.getWert()) {
			_implausibleDatenArten.add(UmfeldDatenArt.ns);
			_verletzteBedingungen
					.add("NS=Schnee, " + "LT=" + formatWert(_ltWert) + " °C > " + formatWert(_ltGrenzSchnee) + " °C");
			_ids.add("[DUA-PP-MK09]");
		}

		// Auswahl des Grenzwertes für Regeln 10 und 11 (NERZ-ÄA104)
		if (options.isUseWfdTrockenGrenzwert() && isOk(_wfdGrenzTrocken)) {
			double usedWfdGrenzwert = _wfdGrenzTrocken.getSkaliertenWert();

			// Prüfung 10
			if (isOk(_wfdWert) && _wfdWert.getSkaliertenWert() > usedWfdGrenzwert && isOk(_fbzWert)
					&& _fbzWert.getWert() == 0) {
				_implausibleDatenArten.add(UmfeldDatenArt.wfd);
				_implausibleDatenArten.add(UmfeldDatenArt.fbz);
				_verletzteBedingungen.add("WFD=" + formatWert(_wfdWert) + " mm > " + formatWert(_wfdGrenzTrocken) + " mm, " + "FBZ=Trocken");
				_ids.add("[DUA-PP-MK10]");
			}

			// Prüfung 11
			if (isOk(_wfdWert) && _wfdWert.getSkaliertenWert() < usedWfdGrenzwert && isOk(_fbzWert)
					&& _fbzWert.getWert() != 0) {
				_implausibleDatenArten.add(UmfeldDatenArt.wfd);
				_implausibleDatenArten.add(UmfeldDatenArt.fbz);
				_verletzteBedingungen.add("WFD=" + formatWert(_wfdWert) + " mm, " + "FBZ=Nass");
				_ids.add("[DUA-PP-MK11]");
			}
		} else {
			// Prüfung 10
			if (isOk(_wfdWert) && _wfdWert.getSkaliertenWert() > 0 && isOk(_fbzWert)
					&& _fbzWert.getWert() == 0) {
				_implausibleDatenArten.add(UmfeldDatenArt.wfd);
				_implausibleDatenArten.add(UmfeldDatenArt.fbz);
				_verletzteBedingungen.add("WFD=" + formatWert(_wfdWert) + " mm > 0,0 mm, " + "FBZ=Trocken");
				_ids.add("[DUA-PP-MK10]");
			}

			// Prüfung 11
			if (isOk(_wfdWert) && _wfdWert.getSkaliertenWert() == 0 && isOk(_fbzWert)
					&& _fbzWert.getWert() != 0) {
				_implausibleDatenArten.add(UmfeldDatenArt.wfd);
				_implausibleDatenArten.add(UmfeldDatenArt.fbz);
				_verletzteBedingungen.add("WFD=" + formatWert(_wfdWert) + " mm, " + "FBZ=Nass");
				_ids.add("[DUA-PP-MK11]");
			}

		}


		boolean rlfUndef = !isOk(_rlfWert) || (isOk(_rlfGrenzNass) && isOk(_rlfGrenzTrocken)
				&& _rlfWert.getWert() >= _rlfGrenzTrocken.getWert() && _rlfWert.getWert() <= _rlfGrenzNass.getWert());

		// Prüfung 12
		if (isOk(_nsWert) && keinNiederschlag() && isOk(_niWert) && isOk(_niGrenzNs)
				&& _niWert.getWert() > _niGrenzNs.getWert() && rlfUndef) {
			_implausibleDatenArten.add(UmfeldDatenArt.ns);
			_implausibleDatenArten.add(UmfeldDatenArt.ni);
			_verletzteBedingungen.add("NS=Kein Niederschlag, " + "NI=" + formatWert(_niWert) + " mm/h > "
					+ formatWert(_niGrenzNs) + " mm/h");
			_ids.add("[DUA-PP-MK12]");
		}

		// Prüfung 13
		if (isOk(_nsWert) && niederschlag() && isOk(_niWert) && _niWert.getWert() == 0 && rlfUndef) {
			_implausibleDatenArten.add(UmfeldDatenArt.ns);
			_implausibleDatenArten.add(UmfeldDatenArt.ni);
			_verletzteBedingungen.add("NS=Niederschlag, " + "NI=" + formatWert(_niWert) + " mm/h");
			_ids.add("[DUA-PP-MK13]");
		}

		if (!_ids.isEmpty()) {
			OperatingMessage message = MESSAGE_TEMPLATE.newMessage(_messStelle.getObjekt());
			for (String id : _ids) {
				message.addId(id);
			}
			for (UmfeldDatenArt art : _implausibleDatenArten) {
				message.add("attr", art.getName() + " " + art.getAbkuerzung());
			}
			for (String s : _verletzteBedingungen) {
				message.add("values", s);
			}
			if (_sendMessage) {
				message.send();
			} else {
				_debug.info(message.toString());
			}
		}
	}

	private boolean keinNiederschlag() {
		return _nsWert.getWert() == 0;
	}

	private boolean niederschlag() {
		return _nsWert.getWert() >= 50 && _nsWert.getWert() <= 69;
	}

	private boolean regen() {
		return (_nsWert.getWert() >= 40 && _nsWert.getWert() <= 69)
				|| (_nsWert.getWert() >= 80 && _nsWert.getWert() <= 84);
	}

	private boolean schnee() {
		return (_nsWert.getWert() >= 70 && _nsWert.getWert() <= 78)
				|| (_nsWert.getWert() >= 85 && _nsWert.getWert() <= 87);
	}

	private String formatWert(final UmfeldDatenSensorWert wert) {
		NumberFormat numberInstance = NumberFormat.getNumberInstance();
		numberInstance.setGroupingUsed(false);
		return numberInstance.format(wert.getSkaliertenWert());
	}

	private boolean isOk(final UmfeldDatenSensorWert wert) {
		return wert != null && wert.isOk();
	}

	@Override
	public void update(final ResultData[] results) {
		for (ResultData result : results) {
			if (result.hasData()) {
				Data data = result.getData();
				_sendMessage = data.getTextValue("erzeugeBetriebsmeldungMeteorologischeKontrolle").getValueText()
						.equals("Ja");
				_niGrenzNs = get(data, "NIgrenzNS", UmfeldDatenArt.ni);
				_niGrenzWfd = get(data, "NIgrenzWFD", UmfeldDatenArt.ni);
				_wfdGrenzTrocken = get(data, "WFDgrenzTrocken", UmfeldDatenArt.wfd);
				_ltGrenzRegen = get(data, "LTgrenzRegen", UmfeldDatenArt.lt);
				_ltGrenzSchnee = get(data, "LTgrenzSchnee", UmfeldDatenArt.lt);
				_rlfGrenzTrocken = get(data, "RLFgrenzTrocken", UmfeldDatenArt.rlf);
				_rlfGrenzNass = get(data, "RLFgrenzNass", UmfeldDatenArt.rlf);
				_swGrenz = get(data, "SWgrenz", UmfeldDatenArt.sw);
			}
		}
	}

	private static UmfeldDatenSensorWert get(final Data data, final String name, final UmfeldDatenArt art) {
		UmfeldDatenSensorWert wert = new UmfeldDatenSensorWert(art);
		wert.setWert(data.getUnscaledValue(name).longValue());
		return wert;
	}
}
