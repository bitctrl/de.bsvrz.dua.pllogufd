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

import java.util.ArrayList;
import java.util.Collection;

import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.StandardAspekteVersorger;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Diese Klasse repräsentiert die Versorgung des Moduls Pl-Prüfung logisch UFD
 * (innerhalb der SWE Pl-Prüfung logisch UFD) mit
 * Standard-Publikationsinformationen (Zuordnung von
 * Objekt-Datenbeschreibung-Kombination zu Standard- Publikationsaspekt).
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class PlLogUFDStandardAspekteVersorger extends StandardAspekteVersorger {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @throws DUAInitialisierungsException
	 *             wird weitergereicht
	 */
	public PlLogUFDStandardAspekteVersorger(final IVerwaltung verwaltung) throws DUAInitialisierungsException {
		super(verwaltung);
	}

	@Override
	protected void init() throws DUAInitialisierungsException {

		this.standardAspekte = new StandardAspekteAdapter(new StandardPublikationsZuordnung[] {
				new StandardPublikationsZuordnung("typ.ufdsFahrBahnOberFlächenTemperatur",
						"atg.ufdsFahrBahnOberFlächenTemperatur", DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
						DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsFahrBahnOberFlächenZustand",
						"atg.ufdsFahrBahnOberFlächenZustand", DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
						DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsGefrierTemperatur", "atg.ufdsGefrierTemperatur",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsHelligkeit", "atg.ufdsHelligkeit",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsLuftTemperatur", "atg.ufdsLuftTemperatur",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsNiederschlagsArt", "atg.ufdsNiederschlagsArt",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsNiederschlagsIntensität", "atg.ufdsNiederschlagsIntensität",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsRelativeLuftFeuchte", "atg.ufdsRelativeLuftFeuchte",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsRestSalz", "atg.ufdsRestSalz",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsSichtWeite", "atg.ufdsSichtWeite",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsTaupunktTemperatur", "atg.ufdsTaupunktTemperatur",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsTemperaturInTiefe1", "atg.ufdsTemperaturInTiefe1",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsTemperaturInTiefe3", "atg.ufdsTemperaturInTiefe3",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsWasserFilmDicke", "atg.ufdsWasserFilmDicke",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsWindRichtung", "atg.ufdsWindRichtung",
						DUAKonstanten.ASP_EXTERNE_ERFASSUNG, DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsWindGeschwindigkeitMittelWert",
						"atg.ufdsWindGeschwindigkeitMittelWert", DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
						DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				new StandardPublikationsZuordnung("typ.ufdsWindGeschwindigkeitSpitzenWert",
						"atg.ufdsWindGeschwindigkeitSpitzenWert", DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
						DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH), });

	}

}
