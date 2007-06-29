/**
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.3 Pl-Pr�fung logisch UFD
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.pllogufd.testmeteo;

import stauma.dav.clientside.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;

/**
 * Diese Klasse hat die Aufgabe vergleichbare oder meteorologisch sich
 * beeinflussende Messgr��en zueinander in Beziehung zu setzen, wenn diese
 * in den vorangegangenen Einzelpr�fungen nicht als Implausibel gekennzeichnet
 * wurden. Wird ein Messwert �ber die Meteorologische Kontrolle als nicht
 * plausibel erkannt, so wird der entsprechende Wert auf Fehlerhaft
 * und Implausibel gesetzt
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public class MeteorologischeKontrolle extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Standardkonstruktor
	 * 
	 * @param stdAspekte Informationen zu den
	 * Standardpublikationsaspekten f�r diese
	 * Instanz des Moduls Pl-Pr�fung formal
	 */
	public MeteorologischeKontrolle(final IStandardAspekte stdAspekte){
		if(stdAspekte != null){
			this.standardAspekte = stdAspekte;
		}
	}
	
	public void aktualisiereDaten(ResultData[] resultate) {
		// TODO Automatisch erstellter Methoden-Stub

	}

	public ModulTyp getModulTyp() {
		// TODO Automatisch erstellter Methoden-Stub
		return null;
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Automatisch erstellter Methoden-Stub

	}

}
