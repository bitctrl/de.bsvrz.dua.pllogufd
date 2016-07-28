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

package de.bsvrz.dua.pllogufd.grenz;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.AbstraktUmfeldDatenSensor;
import de.bsvrz.dua.pllogufd.vew.VerwaltungPlPruefungLogischUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
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
 * Grenzwertprüfung
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class GrenzUmfeldDatenSensor extends AbstraktUmfeldDatenSensor {

	private static final Debug _debug = Debug.getLogger();
	private final SystemObject _messstelle;

	/**
	 * aktuelle Parameter für die Grenzwertprüfung dieses
	 * Umfelddatensensors.
	 */
	private UniversalAtgUfdsGrenzwerte parameter = null;

	public static final MessageTemplate TEMPLATE_GRENZWERT = new MessageTemplate(
			MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN,
			MessageTemplate.set("attr", " und ", "Attribut ", "Attribute "),
			MessageTemplate.fixed(" durch Grenzwertprüfung auf fehlerhaft gesetzt am UFD-Sensor "),
			MessageTemplate.object(),
			MessageTemplate.fixed(" ("),
			MessageTemplate.variable("messstelle"),
			MessageTemplate.fixed("), da "),
			MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". "),
			MessageTemplate.ids()
	);

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
	protected GrenzUmfeldDatenSensor(final IVerwaltung verwaltung,
			final SystemObject obj) throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, obj);
		super.init();
		_messstelle = ((VerwaltungPlPruefungLogischUFD)verwaltung).getBetriebsmeldungsObjekt(obj);
	}

	@Override
	protected Collection<AttributeGroup> getParameterAtgs()
			throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		if (this.objekt == null) {
			throw new NullPointerException(
					"Parameter können nicht bestimmt werden," + //$NON-NLS-1$
					" da noch kein Objekt festgelegt ist"); //$NON-NLS-1$
		}

		final Collection<AttributeGroup> parameterAtgs = new HashSet<AttributeGroup>();
		
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(this.objekt);
		if (datenArt == null) {
			throw new UmfeldDatenSensorUnbekannteDatenartException(
					"Datenart von Umfelddatensensor " + this.objekt + //$NON-NLS-1$ 
							" (" + objekt.getType()
							+ ") konnte nicht identifiziert werden"); //$NON-NLS-1$
		}

		final String atgPid = "atg.ufdsGrenzwerte" + datenArt.getName();

		final AttributeGroup atg = verwaltungsModul
				.getVerbindung().getDataModel().getAttributeGroup(atgPid);

		if (atg != null) {
			parameterAtgs.add(atg);
		} else {
			_debug.fine(
					"Es konnte keine Parameter-Attributgruppe für die " + //$NON-NLS-1$
							"Grenzwertprüfung des Objektes "//$NON-NLS-1$
							+ this.objekt + " bestimmt werden\n" + //$NON-NLS-1$
							"Atg-Name: " + atgPid); //$NON-NLS-1$
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
	public final Data plausibilisiere(final ResultData resultat) {
		Data copy = null;

		if ((resultat != null) && (resultat.getData() != null)) {
			final UmfeldDatenSensorDatum wert = new UmfeldDatenSensorDatum(
					resultat);
			try {
				final UmfeldDatenArt datenArt = UmfeldDatenArt
						.getUmfeldDatenArtVon(resultat.getObject());
				
				synchronized(this) {
					if(parameter != null){
						UmfeldDatenSensorWert w = wert.getWert();
						long currentValue = w.getWert();
						if(w.isOk()) {
							OperatingMessage message = TEMPLATE_GRENZWERT.newMessage(objekt);
							message.add("attr", datenArt.getAbkuerzung());
							message.put("messstelle", _messstelle);
							switch(datenArt.getAbkuerzung()) {
								case "NI":
									message.addId("[DUA-PP-UGW01]");
									break;
								case "WFD":
									message.addId("[DUA-PP-UGW02]");
									break;
								case "LT":
									message.addId("[DUA-PP-UGW03]");
									break;
								case "FBT":
									message.addId("[DUA-PP-UGW04]");
									break;
								case "SW":
									message.addId("[DUA-PP-UGW05]");
									break;
								case "HK":
									message.addId("[DUA-PP-UGW06]");
									break;
								case "GT":
									message.addId("[DUA-PP-UGW07]");
									break;
								case "TPT":
									message.addId("[DUA-PP-UGW08]");
									break;
								case "TT1":
									message.addId("[DUA-PP-UGW09]");
									break;
								case "TT3":
									message.addId("[DUA-PP-UGW10]");
									break;
								case "WGS":
									message.addId("[DUA-PP-UGW11]");
									break;
								case "WGM":
									message.addId("[DUA-PP-UGW12]");
									break;
								default:
									message.addId("[DUA-PP-UGW??]");
									break;
							}
							if(parameter.isMaxSinnvoll() && currentValue > parameter.getMax()) {
								Data mainitem = wert.getDatum().getItem(datenArt.getName());
								if(parameter.getVerhalten() == OptionenPlausibilitaetsPruefungLogischUfd.WERT_REDUZIEREN) {
									w.setWert(parameter.getMax());
									mainitem.getItem("Status").getItem("PlLogisch").getTextValue("WertMaxLogisch").setText("Ja");
								}
								else if(parameter.getVerhalten() == OptionenPlausibilitaetsPruefungLogischUfd.AUF_FEHLERHAFT_SETZEN){
									mainitem.getItem("Status").getItem("PlLogisch").getTextValue("WertMaxLogisch").setText("Ja");
									wert.setGueteIndex(Math.round(wert.getGueteIndex().getWert() * .8));
									message.add("values", datenArt.getAbkuerzung()
											+ "="
											+ formatValue(w.getSkaliertenWert(), mainitem.getTextValue("Wert").getSuffixText())
											+ " > "
											+ formatValue(parameter.getMaxSkaliert(), mainitem.getTextValue("Wert").getSuffixText())
									);
									
									w.setFehlerhaftAn();
									wert.setStatusMessWertErsetzungImplausibel(1);
									message.send();
								}
							}
							else if(parameter.isMinSinnvoll() && currentValue < parameter.getMin()) {
								Data mainitem = wert.getDatum().getItem(datenArt.getName());
								if(parameter.getVerhalten() == OptionenPlausibilitaetsPruefungLogischUfd.WERT_REDUZIEREN) {
									w.setWert(parameter.getMin());
									mainitem.getItem("Status").getItem("PlLogisch").getTextValue("WertMinLogisch").setText("Ja");
								}
								else if(parameter.getVerhalten() == OptionenPlausibilitaetsPruefungLogischUfd.AUF_FEHLERHAFT_SETZEN){
									mainitem.getItem("Status").getItem("PlLogisch").getTextValue("WertMinLogisch").setText("Ja");
									wert.setGueteIndex(Math.round(wert.getGueteIndex().getWert() * .8));
									message.add("values", datenArt.getAbkuerzung()
											+ "="
											+ formatValue(w.getSkaliertenWert(), mainitem.getTextValue("Wert").getSuffixText())
											+ " < "
											+ formatValue(parameter.getMinSkaliert(), mainitem.getTextValue("Wert").getSuffixText())
									);
									
									w.setFehlerhaftAn();
									wert.setStatusMessWertErsetzungImplausibel(1);
									message.send();
								}
							}
						}
					}
				}
			} catch (final UmfeldDatenSensorUnbekannteDatenartException ex) {
			}
			copy = wert.getDatum();
		}

		return copy;
	}

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
							this.parameter = new UniversalAtgUfdsGrenzwerte(
									resultat);
						} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
							_debug.warning(e.getMessage());
							continue;
						}
						_debug
						.info("Neue Parameter für (" + resultat.getObject() + "):\n" //$NON-NLS-1$ //$NON-NLS-2$
								+ this.parameter);
					}
				}
			}
		}
	}

}
