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

package de.bsvrz.dua.pllogufd.typen;

import java.util.HashMap;
import java.util.Map;

import de.bsvrz.sys.funclib.bitctrl.daf.AbstractDavZustand;

/**
 * Über diese Klasse werden alle im DAV-Enumerationstyp
 * <code>att.ufdsVergleichsOperator</code> beschriebenen Werte zur Verfügung
 * gestellt.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 * @version $Id$
 */
public class UfdsVergleichsOperator extends AbstractDavZustand {

	/**
	 * Unbenutzt, vermeide Warnung.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Der Wertebereich dieses DAV-Enumerationstypen
	 */
	private static final Map<Integer, UfdsVergleichsOperator> WERTE_BEREICH = new HashMap<>();

	/**
	 * Alle wirklichen Enumerationswerte
	 */
	public static final UfdsVergleichsOperator BEDINGUNG_IMMER_FALSCH = new UfdsVergleichsOperator(
			"BedingungImmerFalsch", 0); //$NON-NLS-1$

	public static final UfdsVergleichsOperator BEDINGUNG_IMMER_WAHR = new UfdsVergleichsOperator(
			"BedingungImmerWahr", 1); //$NON-NLS-1$

	public static final UfdsVergleichsOperator KLEINER = new UfdsVergleichsOperator(
			"kleiner", 2); //$NON-NLS-1$

	public static final UfdsVergleichsOperator KLEINER_GLEICH = new UfdsVergleichsOperator(
			"kleinerGleich", 3); //$NON-NLS-1$

	public static final UfdsVergleichsOperator GLEICH = new UfdsVergleichsOperator(
			"gleich", 4); //$NON-NLS-1$

	public static final UfdsVergleichsOperator GROESSER_GLEICH = new UfdsVergleichsOperator(
			"größerGleich", 5); //$NON-NLS-1$

	public static final UfdsVergleichsOperator GROESSER = new UfdsVergleichsOperator(
			"größer", 6); //$NON-NLS-1$

	/**
	 * Standardkonstruktor
	 *
	 * @param name
	 *            der Name des Wertes
	 * @param code
	 *            der Code des Wertes
	 */
	private UfdsVergleichsOperator(final String name, final int code) {
		super(code, name);
		UfdsVergleichsOperator.WERTE_BEREICH.put(code, this);
	}

	/**
	 * Erfragt den Wert dieses DAV-Enumerationstypen mit dem übergebenen Code
	 *
	 * @param code
	 *            der Code des Enumerations-Wertes
	 */
	public static final UfdsVergleichsOperator getZustand(final int code) {
		return UfdsVergleichsOperator.WERTE_BEREICH.get(code);
	}

	/**
	 * Vergleicht zwei Werte mit diesem Operator miteinander
	 *
	 * @param wert1
	 *            Wert Nr.1
	 * @param wert2
	 *            Wert Nr.2
	 * @return das Vergleichsergebnis
	 */
	public final boolean vergleiche(final long wert1, final long wert2) {
		boolean ergebnis = false;

		if (this.equals(UfdsVergleichsOperator.BEDINGUNG_IMMER_FALSCH)) {
			ergebnis = false;
		} else if (this.equals(UfdsVergleichsOperator.BEDINGUNG_IMMER_WAHR)) {
			ergebnis = true;
		} else if (this.equals(UfdsVergleichsOperator.KLEINER)) {
			ergebnis = wert1 < wert2;
		} else if (this.equals(UfdsVergleichsOperator.KLEINER_GLEICH)) {
			ergebnis = wert1 <= wert2;
		} else if (this.equals(UfdsVergleichsOperator.GLEICH)) {
			ergebnis = wert1 == wert2;
		} else if (this.equals(UfdsVergleichsOperator.GROESSER_GLEICH)) {
			ergebnis = wert1 >= wert2;
		} else if (this.equals(UfdsVergleichsOperator.GROESSER)) {
			ergebnis = wert1 > wert2;
		}

		return ergebnis;
	}
}
