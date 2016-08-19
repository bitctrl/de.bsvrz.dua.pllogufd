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

		String[] datenArten = new String[] { "FahrBahnOberFlächenTemperatur", "FahrBahnOberFlächenZustand",
				"GefrierTemperatur", "Helligkeit", "LuftTemperatur", "NiederschlagsArt", "NiederschlagsIntensität",
				"RelativeLuftFeuchte", "RestSalz", "SichtWeite", "Taustoffmenge", "TaupunktTemperatur", "TemperaturInTiefe1",
				"TemperaturInTiefe3", "WasserFilmDicke", "WindRichtung", "WindGeschwindigkeitMittelWert",
				"WindGeschwindigkeitSpitzenWert", "ZeitReserveEisGlätte", "ZeitReserveReifGlätte" };

		Collection<StandardPublikationsZuordnung> zuordnungen = new ArrayList<>();

		for( String datenArt : datenArten) {
			try {
				StandardPublikationsZuordnung zuordung = new StandardPublikationsZuordnung("typ.ufds" + datenArt, "atg.ufds" + datenArt, DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
						DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH);
				zuordnungen.add(zuordung);
			} catch (DUAInitialisierungsException e) {
				LOGGER.fine(e.getLocalizedMessage());
			}
		}

		this.standardAspekte = new StandardAspekteAdapter(
				zuordnungen.toArray(new StandardPublikationsZuordnung[zuordnungen.size()]));

	}

}
