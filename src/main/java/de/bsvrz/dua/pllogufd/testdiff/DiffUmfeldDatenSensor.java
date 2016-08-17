/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Pl-Prüfung logisch UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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

package de.bsvrz.dua.pllogufd.testdiff;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.AbstraktUmfeldDatenSensor;
import de.bsvrz.dua.pllogufd.vew.VerwaltungPlPruefungLogischUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.VariableMitKonstanzZaehler;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.funclib.operatingMessage.OperatingMessage;

import java.util.Collection;
import java.util.HashSet;

/**
 * Assoziiert einen Umfelddatensensor mit dessen Parametern und Werten bzgl. der
 * Differenzialkontrolle
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class DiffUmfeldDatenSensor extends AbstraktUmfeldDatenSensor {

	private static final Debug _debug = Debug.getLogger();

	/**
	 * aktueller Wert mit Historie.
	 */
	private VariableMitKonstanzZaehler<Long> wert = null;

	/**
	 * aktuelle Parameter für die Differenzialkontrolle dieses
	 * Umfelddatensensors.
	 */
	private UniversalAtgUfdsDifferenzialKontrolle parameter = null;

	/**
	 * Vorlage für die Betriebsmeldung
	 */
	private static final MessageTemplate TEMPLATE_DIFF = new MessageTemplate(
			MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN,
			MessageTemplate.fixed("Grenzwert für Messwertkonstanz bei Differenzialkontrolle für "),
			MessageTemplate.variable("attr"),
			MessageTemplate.fixed(" an Messstelle "),
			MessageTemplate.object(),
			MessageTemplate.fixed(" überschritten, da "),
			MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". Wert wird auf fehlerhaft gesetzt. "),
			MessageTemplate.ids()
	);

	/**
	 * Niederschlags-Art-Sensor (für den Spezialfall FBT)
	 */
	private SensorNA sensorNA;

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 *            das mit dieser Instanz zu assoziierende Systemobjekt (vom Typ
	 *            <code>typ.umfeldDatenSensor</code>)
	 * @throws DUAInitialisierungsException
	 *             wird weitergereicht
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	protected DiffUmfeldDatenSensor(final IVerwaltung verwaltung,
			final SystemObject obj) throws DUAInitialisierungsException,
			UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, obj);
		final UmfeldDatenArt datenArt = UmfeldDatenArt
				.getUmfeldDatenArtVon(obj);
		if (datenArt == null) {
			throw new UmfeldDatenSensorUnbekannteDatenartException(
					"Datenart von Umfelddatensensor " + obj + 
					" (" + obj.getType()
					+ ") konnte nicht identifiziert werden"); 
		}
		this.wert = new VariableMitKonstanzZaehler<>(datenArt.getName());
		
		if(datenArt.getAbkuerzung().equals("FBT")){
			DUAUmfeldDatenMessStelle messstelle = ((VerwaltungPlPruefungLogischUFD)verwaltung).getMessstelle(objekt);
			if(messstelle != null) {
				DUAUmfeldDatenSensor hauptSensorNS = messstelle.getHauptSensor(UmfeldDatenArt.ns);
				if(hauptSensorNS != null) {
					sensorNA = new SensorNA(hauptSensorNS.getObjekt());
				}
			}
		}
		
		this.init();
	}

	@Override
	protected Collection<AttributeGroup> getParameterAtgs()
			throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		if (this.objekt == null) {
			throw new NullPointerException(
					"Parameter können nicht bestimmt werden," + 
					" da noch kein Objekt festgelegt ist"); 
		}

		final Collection<AttributeGroup> parameterAtgs = new HashSet<AttributeGroup>();

		final String atgPid = "atg.ufdsDifferenzialKontrolle" + UmfeldDatenArt.
				getUmfeldDatenArtVon(this.objekt).getName();
		final AttributeGroup atg = verwaltungsModul
				.getVerbindung().getDataModel().getAttributeGroup(atgPid);

		if (atg != null) {
			parameterAtgs.add(atg);
		} else {
			throw new DUAInitialisierungsException(
					"Es konnte keine Parameter-Attributgruppe für die " + 
							"Differenzialkontrolle des Objektes " + this.objekt
							+ " bestimmt werden\n" + 
							"Atg-Name: " + atgPid); 
		}

		return parameterAtgs;
	}

	/**
	 * Für die empfangenen Daten wird geprüft, ob innerhalb eines definierenten
	 * Zeitraums (parametrierbares Zeitfenster) eine Änderung des Messwerts
	 * vorliegt. Liegt eine Ergebniskonstanz in diesem Zeitfenster vor, so
	 * erfolgt eine Kennzeichnung der Werte als Implausibel und Fehlerhaft.
	 *
	 * @param resultat
	 *            ein Roh-Datum eines Umfelddatensensors
	 * @return das gekennzeichnete Datum oder <code>null</code> wenn das Datum
	 *         plausibel ist
	 */
	public final Data plausibilisiere(final ResultData resultat) {
		Data copy = null;

		if((resultat != null) && (resultat.getData() != null)) {
			if(this.parameter != null) {
				try {
					final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
							resultat);

					final UmfeldDatenArt datenArt = UmfeldDatenArt
							.getUmfeldDatenArtVon(resultat.getObject());

					final long aktuellerWert = datum.getWert().getWert();
					final long t = datum.getT();
					this.wert.aktualisiere(aktuellerWert, t);

					synchronized(this.parameter) {
						boolean vergleichDurchfuehren;
						if(this.parameter.getOperator() != null) {
							vergleichDurchfuehren = this.parameter
									.getOperator()
									.vergleiche(
											datum.getWert(),
											this.parameter.getGrenz()
									);

						}
						else {
							vergleichDurchfuehren = aktuellerWert <= this.parameter
									.getGrenz().getWert();
						}

						long maxZeit = this.parameter.getMaxZeit();
						if(maxZeit <= 0) {
							vergleichDurchfuehren = false;
						}
						if(vergleichDurchfuehren && datenArt.getAbkuerzung().equals("FBT")){
							// Bedingung != Schnee
							if(sensorNA != null){
								String nawert = sensorNA.nawert;
								if(nawert != null && nawert.contains("Schnee")){
									_debug.fine("Differenzialkontrolle für FBT wird nicht durchgeführt, da NA-Sensor Schnee meldet", objekt);
									vergleichDurchfuehren = false;
								}
							}
						}
						if(vergleichDurchfuehren) {
							if(this.wert.getWertIstKonstantSeit() > maxZeit) {
								OperatingMessage message = TEMPLATE_DIFF.newMessage(((VerwaltungPlPruefungLogischUFD)verwaltungsModul).getBetriebsmeldungsObjekt(objekt));
								message.put("attr", datenArt.getName() + " " + datenArt.getAbkuerzung());
								message.add("values", datenArt.getAbkuerzung() + " konstant " + formatDuration(this.wert.getWertIstKonstantSeit()) + " > " + formatDuration(maxZeit) + " maximal");
								switch(datenArt.getAbkuerzung()) {
									case "NI":
										message.addId("[DUA-PP-UDK01]");
										break;
									case "WFD":
										message.addId("[DUA-PP-UDK02]");
										break;
									case "LT":
										message.addId("[DUA-PP-UDK03]");
										break;
									case "RLF":
										message.addId("[DUA-PP-UDK04]");
										break;
									case "SW":
										message.addId("[DUA-PP-UDK05]");
										break;
									case "HK":
										message.addId("[DUA-PP-UDK06]");
										break;
									case "FBT":
										message.addId("[DUA-PP-UDK07]");
										break;
									case "TT1":
										message.addId("[DUA-PP-UDK08]");
										break;
									case "TT3":
										message.addId("[DUA-PP-UDK09]");
										break;
									case "RS":
										message.addId("[DUA-PP-UDK10]");
										break;
									case "GT":
										message.addId("[DUA-PP-UDK11]");
										break;
									case "TPT":
										message.addId("[DUA-PP-UDK12]");
										break;
									case "WGS":
										message.addId("[DUA-PP-UDK13]");
										break;
									case "WGM":
										message.addId("[DUA-PP-UDK14]");
										break;
									case "WR":
										message.addId("[DUA-PP-UDK15]");
										break;
									default:
										message.addId("[DUA-PP-UDK??]");
										break;
								}
								message.send();
								
								datum.getWert().setFehlerhaftAn();
								datum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
								copy = datum.getDatum();
							}
						}
					}
				}
				catch(UmfeldDatenSensorUnbekannteDatenartException ignored) {
				}
			}
			else {
				_debug
						.fine("Fuer Umfelddatensensor " + this + 
								      " wurden noch keine Parameter für die Differenzialkontrolle empfangen"); 
			}
		}

		return copy;

	}

	@Override
	public void update(final ResultData[] resultate) {
		if (resultate != null) {
			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					synchronized (this) {
						try {
							this.parameter = new UniversalAtgUfdsDifferenzialKontrolle(
									resultat);
						} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
							_debug.warning(e.getMessage());
							continue;
						}
						_debug
						.info("Neue Parameter für (" + resultat.getObject() + "):\n"
								+ this.parameter);
					}
				}
			}
		}
	}


	public static String formatDuration(long tmp) {
		long ms = tmp % 1000;
		tmp /= 1000;
		long sec = tmp % 60;
		tmp /= 60;
		long min = tmp % 60;
		tmp /= 60;
		long h = tmp;
		StringBuilder stringBuilder = new StringBuilder();
		if(h >= 1){
			if(h == 1){
				stringBuilder.append("1 Stunde ");
			}
			else {
				stringBuilder.append(h).append(" Stunden ");
			}
		}
		if(min >= 1){
			if(min == 1){
				stringBuilder.append("1 Minute ");
			}
			else {
				stringBuilder.append(min).append(" Minuten ");
			}
		}
		if(sec >= 1){
			if(sec == 1){
				stringBuilder.append("1 Sekunde ");
			}
			else {
				stringBuilder.append(sec).append(" Sekunden ");
			}
		}
		if(ms >= 1){
			if(ms == 1){
				stringBuilder.append("1 Millisekunde ");
			}
			else {
				stringBuilder.append(ms).append(" Millisekunden ");
			}
		}
		stringBuilder.setLength(stringBuilder.length()-1);
		return stringBuilder.toString();
	}

	/** 
	 * Gibt den NS-Sensor zurück zurück
	 * @return den NS-Sensor zurück
	 */
	public SensorNA getSensorNA() {
		return sensorNA;
	}

	/**
	 * Hilfsklasse, die den Aktuellen Niederschlagsart-Wert empfängt
	 */
	static class SensorNA {
		/**
		 * NS-Sensor-Objekt
		 */
		private final SystemObject _systemObject;

		/**
		 * Aktueller Sensorwert oder null falls nicht bestimmbar
		 */
		private String nawert = null;

		/** 
		 * Erstellt einen neuen SensorNA
		 */
		public SensorNA(final SystemObject systemObject) {
			_systemObject = systemObject;
		}

		/** 
		 * Gibt das Objekt zurück
		 * @return das Objekt
		 */
		public SystemObject getSystemObject() {
			return _systemObject;
		}

		/**
		 * Aktualisiert die Daten
		 * @param resultat Neue Daten
		 */
		public void update(final ResultData resultat) {
			if(resultat.hasData()){
				nawert = resultat.getData().getItem("NiederschlagsArt").getTextValue("Wert").getValueText();
			}
			else {
				nawert = null;
			}
			
		}
	}

	@Override
	public String toString() {
		return objekt.toString();
	}
}
