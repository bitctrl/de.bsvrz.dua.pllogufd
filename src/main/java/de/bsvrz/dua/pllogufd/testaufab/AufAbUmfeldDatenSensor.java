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

package de.bsvrz.dua.pllogufd.testaufab;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.AbstraktUmfeldDatenSensor;
import de.bsvrz.dua.pllogufd.vew.VerwaltungPlPruefungLogischUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.funclib.operatingMessage.OperatingMessage;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;

/**
 * Assoziiert einen Umfelddatensensor mit dessen Parametern und Werten bzgl. der
 * Anstieg-Abfall-Kontrolle
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class AufAbUmfeldDatenSensor extends AbstraktUmfeldDatenSensor {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Verwaltungsmodul
	 */
	private final VerwaltungPlPruefungLogischUFD _verwaltung;

	/**
	 * aktuelle Parameter für die Anstieg-Abfall-Kontrolle dieses
	 * Umfelddatensensors.
	 */
	private UniversalAtgUfdsAnstiegAbstiegKontrolle parameter = null;

	/**
	 * letztes für diesen Umfelddatensensor empfangenes Datum.
	 */
	private UmfeldDatenSensorDatum letzterWert = null;

	/**
	 * Betriebsmeldungs-Template
	 */
	private static final MessageTemplate TEMPLATE = new MessageTemplate(MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN,
			MessageTemplate.fixed("Grenzwert für Messwertkonstanz bei Anstieg-Abfall-Kontrolle für "),
			MessageTemplate.variable("attr"), MessageTemplate.fixed(" an Messstelle "), MessageTemplate.object(),
			MessageTemplate.fixed(" überschritten, da Differenz "), MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". Wert wird auf fehlerhaft gesetzt. "), MessageTemplate.ids());

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 *            das Sensor-Objekt
	 * @throws DUAInitialisierungsException
	 *             wenn die Instaziierung fehlschlägt
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	protected AufAbUmfeldDatenSensor(final IVerwaltung verwaltung, final SystemObject obj)
			throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, obj);
		_verwaltung = (VerwaltungPlPruefungLogischUFD) verwaltung;
		super.init();
	}

	@Override
	protected Collection<AttributeGroup> getParameterAtgs()
			throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		if (this.objekt == null) {
			throw new NullPointerException(
					"Parameter können nicht bestimmt werden," + " da noch kein Objekt festgelegt ist");
		}

		final Collection<AttributeGroup> parameterAtgs = new HashSet<AttributeGroup>();

		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(this.objekt);
		if (datenArt == null) {
			throw new UmfeldDatenSensorUnbekannteDatenartException("Datenart von Umfelddatensensor " + this.objekt
					+ " (" + objekt.getType() + ") konnte nicht identifiziert werden");
		}

		final String atgPid = "atg.ufdsAnstiegAbstiegKontrolle" + datenArt.getName();

		final AttributeGroup atg = verwaltungsModul.getVerbindung().getDataModel().getAttributeGroup(atgPid);

		if (atg != null) {
			parameterAtgs.add(atg);
		} else {
			throw new DUAInitialisierungsException(
					"Es konnte keine Parameter-Attributgruppe für die " + "Anstieg-Abfall-Kontrolle des Objektes "
							+ this.objekt + " bestimmt werden\n" + "Atg-Name: " + atgPid);
		}

		return parameterAtgs;
	}

	/**
	 * Hier findet die Prüfung eines Datums statt. Diese findet nur für den Fall
	 * statt, dass das empfangene Datum weder als Implausibel, Fehlerhaft noch
	 * Nicht ermittelbar gekennzeichnet ist. Das empfangene Datum wird
	 * gespeichert
	 *
	 * @param resultat
	 *            ein Roh-Datum eines Umfelddatensensors
	 * @return das gekennzeichnete Datum oder <code>null</code> wenn das Datum
	 *         plausibel ist
	 */
	public synchronized final Data plausibilisiere(final ResultData resultat) {
		Data copy = null;

		if ((resultat != null) && (resultat.getData() != null)) {
			final UmfeldDatenSensorDatum wert = new UmfeldDatenSensorDatum(resultat);

			final UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(resultat.getObject());

				if ((this.letzterWert != null) && !this.letzterWert.getWert().isFehlerhaft()
						&& !this.letzterWert.getWert().isFehlerhaftBzwNichtErmittelbar()
						&& !this.letzterWert.getWert().isNichtErmittelbar()
						&& (this.letzterWert.getStatusMessWertErsetzungImplausibel() != DUAKonstanten.JA)) {

					if (!wert.getWert().isFehlerhaft() && !wert.getWert().isFehlerhaftBzwNichtErmittelbar()
							&& !wert.getWert().isNichtErmittelbar()
							&& (wert.getStatusMessWertErsetzungImplausibel() != DUAKonstanten.JA)) {

						if (this.parameter != null) {
							if (this.parameter.isSinnvoll()) {
								long diff = Math.abs(wert.getWert().getWert() - this.letzterWert.getWert().getWert());

								double diffScaled = Math.abs(wert.getWert().getSkaliertenWert()
										- this.letzterWert.getWert().getSkaliertenWert());
								final boolean fehler = diff > this.parameter.getMaxDiff();
								if (fehler) {
									Data mainitem = wert.getDatum().getItem(datenArt.getName());
									OperatingMessage message = TEMPLATE
											.newMessage(_verwaltung.getBetriebsmeldungsObjekt(objekt));
									message.put("attr", datenArt.getName() + " " + datenArt.getAbkuerzung());
									message.add("values",
											datenArt.getAbkuerzung() + " = "
													+ formatValue(diffScaled,
															mainitem.getTextValue("Wert").getSuffixText())
													+ " > " + formatValue(parameter.getScaledMax(),
															mainitem.getTextValue("Wert").getSuffixText()));
									switch (datenArt.getAbkuerzung()) {
									case "WFD":
										message.addId("[DUA-PP-UAK01]");
										break;
									case "LT":
										message.addId("[DUA-PP-UAK02]");
										break;
									case "RLF":
										message.addId("[DUA-PP-UAK03]");
										break;
									case "HK":
										message.addId("[DUA-PP-UAK04]");
										break;
									case "FBT":
										message.addId("[DUA-PP-UAK05]");
										break;
									case "TT1":
										message.addId("[DUA-PP-UAK06]");
										break;
									case "TT3":
										message.addId("[DUA-PP-UAK07]");
										break;
									case "TPT":
										message.addId("[DUA-PP-UAK08]");
										break;
									case "WGS":
										message.addId("[DUA-PP-UAK09]");
										break;
									case "WGM":
										message.addId("[DUA-PP-UAK10]");
										break;
									default:
										message.addId("[DUA-PP-UAK??]");
										break;
									}
									message.send();

									final UmfeldDatenSensorDatum neuerWert = new UmfeldDatenSensorDatum(resultat);
									neuerWert.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
									neuerWert.getWert().setFehlerhaftAn();
									copy = neuerWert.getDatum();
								}
							}
						} else {
							LOGGER.fine("Fuer Umfelddatensensor " + this
									+ " wurden noch keine Parameter für die Anstieg-Abfall-Kontrolle empfangen");
						}
					}
				}
				this.letzterWert = wert;
			} catch (UmfeldDatenSensorUnbekannteDatenartException ignored) {

			}
		}

		return copy;
	}

	/**
	 * Formatiert einen Wert
	 * 
	 * @param w
	 *            Wert
	 * @param suffixText
	 *            Einheit
	 * @return Formatierte Zahl
	 */
	private static String formatValue(final double w, final String suffixText) {
		NumberFormat numberInstance = NumberFormat.getNumberInstance();
		numberInstance.setGroupingUsed(false);
		return numberInstance.format(w) + " " + suffixText;
	}

	@Override
	public void update(final ResultData[] resultate) {
		if (resultate != null) {
			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					synchronized (this) {
						try {
							this.parameter = new UniversalAtgUfdsAnstiegAbstiegKontrolle(resultat);
						} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
							LOGGER.warning(e.getMessage());
							continue;
						}
						LOGGER.info("Neue Parameter für (" + resultat.getObject() + "):\n" + this.parameter);
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return objekt.toString();
	}
}
