/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
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
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.pllogufd.testmeteo;

import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;

/**
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public class UFDSWasserFilmDicke extends AbstraktMeteoUmfeldDatenSensor {

	protected UFDSWasserFilmDicke(IVerwaltung verwaltung, SystemObject obj) throws DUAInitialisierungsException {
		super(verwaltung, obj);
		// TODO Automatisch erstellter Konstruktoren-Stub
	}

	public void update(ResultData[] results) {
		// TODO Automatisch erstellter Methoden-Stub

	}

}
