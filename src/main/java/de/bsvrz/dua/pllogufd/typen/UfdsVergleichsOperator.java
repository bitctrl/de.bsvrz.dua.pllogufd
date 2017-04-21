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

package de.bsvrz.dua.pllogufd.typen;

import de.bsvrz.sys.funclib.bitctrl.daf.AbstractDavZustand;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;

import java.util.HashMap;
import java.util.Map;

/**
 * Über diese Klasse werden alle im DAV-Enumerationstyp
 * <code>att.ufdsVergleichsOperator</code> beschriebenen Werte zur Verfügung
 * gestellt.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class UfdsVergleichsOperator extends AbstractDavZustand {

	/**
	 * Der Wertebereich dieses DAV-Enumerationstypen
	 */
	private static final Map<Integer, UfdsVergleichsOperator> WERTE_BEREICH = new HashMap<Integer, UfdsVergleichsOperator>();

	/**
	 * Alle wirklichen Enumerationswerte
	 */
	public static final UfdsVergleichsOperator BEDINGUNG_IMMER_FALSCH = new UfdsVergleichsOperator(
			"BedingungImmerFalsch", 0);

	public static final UfdsVergleichsOperator BEDINGUNG_IMMER_WAHR = new UfdsVergleichsOperator("BedingungImmerWahr",
			1);

	public static final UfdsVergleichsOperator KLEINER = new UfdsVergleichsOperator("kleiner", 2);

	public static final UfdsVergleichsOperator KLEINER_GLEICH = new UfdsVergleichsOperator("kleinerGleich", 3);

	public static final UfdsVergleichsOperator GLEICH = new UfdsVergleichsOperator("gleich", 4);

	public static final UfdsVergleichsOperator GROESSER_GLEICH = new UfdsVergleichsOperator("größerGleich", 5);

	public static final UfdsVergleichsOperator GROESSER = new UfdsVergleichsOperator("größer", 6);

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
	 * @return UfdsVergleichsOperator
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
	public final boolean vergleiche(final UmfeldDatenSensorWert wert1, final UmfeldDatenSensorWert wert2) {
		boolean ergebnis = false;

		if (this.equals(UfdsVergleichsOperator.BEDINGUNG_IMMER_FALSCH)) {
			ergebnis = false;
		} else if (this.equals(UfdsVergleichsOperator.BEDINGUNG_IMMER_WAHR)) {
			ergebnis = true;
		} else {
			if (!wert1.isOk() || !wert2.isOk())
				return false;

			if (this.equals(UfdsVergleichsOperator.KLEINER)) {
				ergebnis = wert1.getWert() < wert2.getWert();
			} else if (this.equals(UfdsVergleichsOperator.KLEINER_GLEICH)) {
				ergebnis = wert1.getWert() <= wert2.getWert();
			} else if (this.equals(UfdsVergleichsOperator.GLEICH)) {
				ergebnis = wert1.getWert() == wert2.getWert();
			} else if (this.equals(UfdsVergleichsOperator.GROESSER_GLEICH)) {
				ergebnis = wert1.getWert() >= wert2.getWert();
			} else if (this.equals(UfdsVergleichsOperator.GROESSER)) {
				ergebnis = wert1.getWert() > wert2.getWert();
			}
		}

		return ergebnis;
	}
}
