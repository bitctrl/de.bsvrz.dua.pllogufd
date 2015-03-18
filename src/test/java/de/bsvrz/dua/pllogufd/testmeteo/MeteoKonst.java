/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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

import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;

/**
 * Standardwerte für meteorologische Pl-Prüfung.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public final class MeteoKonst {

	/**
	 * Parameter NSGrenzLT.
	 */
	public static final long NS_GRENZ_LT = -5;

	/**
	 * Parameter NSGrenzTrockenRLF.
	 */
	public static final long NS_GRENZ_TROCKEN_RLF = 70;

	/**
	 * Parameter NSminNI.
	 */
	public static final double NS_MIN_NI = 0.1;

	/**
	 * Parameter NSGrenzRLF.
	 */
	public static final long NS_GRENZ_RLF = 78;

	/**
	 * Parameter NIgrenzNassNI.
	 */
	public static final double NI_GRENZ_NASS_NI = 0.5;

	/**
	 * Parameter NIgrenzNassRLF.
	 */
	public static final long NI_GRENZ_NASS_RLF = 78;

	/**
	 * Parameter NIminNI.
	 */
	public static final double NI_MIN_NI = 0.1;

	/**
	 * Parameter NIgrenzTrockenRLF.
	 */
	public static final long NI_GRENZ_TROCKEN_RLF = 70;

	/**
	 * Parameter NIminTrockenRLF.
	 */
	public static final long NI_MIN_TROCKEN_RLF = PlPruefungLogischUFDTest.STANDARD_T * 3;

	/**
	 * Parameter WFDgrenzNassNI.
	 */
	public static final double WFD_GRENZ_NASS_NI = 0.5;

	/**
	 * Parameter WFDgrenzNassRLF.
	 */
	public static final long WFD_GRENZ_NASS_RLF = 78;

	/**
	 * Parameter WDFminNassRLF.
	 */
	public static final long WDF_MIN_NASS_RLF = PlPruefungLogischUFDTest.STANDARD_T * 3;

	/**
	 * Parameter SWgrenzTrockenRLF.
	 */
	public static final long SW_GRENZ_TROCKEN_RLF = 70L;

	/**
	 * Parameter SWgrenzSW.
	 */
	public static final long SW_GRENZ_SW = 500;

	/**
	 * Standardkonstruktor.
	 */
	private MeteoKonst() {

	}

}
