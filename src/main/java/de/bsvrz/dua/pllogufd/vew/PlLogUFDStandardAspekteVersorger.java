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

package de.bsvrz.dua.pllogufd.vew;

import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.StandardAspekteVersorger;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Diese Klasse repräsentiert die Versorgung des Moduls Pl-Prüfung logisch UFD (innerhalb der SWE Pl-Prüfung logisch UFD) mit Standard-Publikationsinformationen
 * (Zuordnung von Objekt-Datenbeschreibung-Kombination zu Standard- Publikationsaspekt).
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class PlLogUFDStandardAspekteVersorger extends StandardAspekteVersorger {

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @throws DUAInitialisierungsException wird weitergereicht
	 */
	public PlLogUFDStandardAspekteVersorger(final IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		super(verwaltung);
	}

	@Override
	protected void init() throws DUAInitialisierungsException {

		this.standardAspekte = new StandardAspekteAdapter(
				new StandardPublikationsZuordnung[]{
						new StandardPublikationsZuordnung(
								"typ.ufdsFahrBahnOberFlächenTemperatur", //$NON-NLS-1$
								"atg.ufdsFahrBahnOberFlächenTemperatur", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsFahrBahnOberFlächenZustand", //$NON-NLS-1$
								"atg.ufdsFahrBahnOberFlächenZustand", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsGefrierTemperatur", //$NON-NLS-1$
								"atg.ufdsGefrierTemperatur", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsHelligkeit", //$NON-NLS-1$
								"atg.ufdsHelligkeit", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsLuftTemperatur", //$NON-NLS-1$
								"atg.ufdsLuftTemperatur", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsNiederschlagsArt", //$NON-NLS-1$
								"atg.ufdsNiederschlagsArt", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsNiederschlagsIntensität", //$NON-NLS-1$
								"atg.ufdsNiederschlagsIntensität", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsRelativeLuftFeuchte", //$NON-NLS-1$
								"atg.ufdsRelativeLuftFeuchte", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsRestSalz", //$NON-NLS-1$
								"atg.ufdsRestSalz", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsSichtWeite", //$NON-NLS-1$
								"atg.ufdsSichtWeite", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsTaupunktTemperatur", //$NON-NLS-1$
								"atg.ufdsTaupunktTemperatur", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsTemperaturInTiefe1", //$NON-NLS-1$
								"atg.ufdsTemperaturInTiefe1", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsTemperaturInTiefe3", //$NON-NLS-1$
								"atg.ufdsTemperaturInTiefe3", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsWasserFilmDicke", //$NON-NLS-1$
								"atg.ufdsWasserFilmDicke", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsWindRichtung", //$NON-NLS-1$
								"atg.ufdsWindRichtung", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsWindGeschwindigkeitMittelWert", //$NON-NLS-1$
								"atg.ufdsWindGeschwindigkeitMittelWert", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
						new StandardPublikationsZuordnung(
								"typ.ufdsWindGeschwindigkeitSpitzenWert", //$NON-NLS-1$
								"atg.ufdsWindGeschwindigkeitSpitzenWert", //$NON-NLS-1$
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH
						),
				});

	}

}
