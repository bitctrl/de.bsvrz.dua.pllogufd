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

import de.bsvrz.sys.funclib.bitctrl.daf.AbstractDavZustand;

import java.util.HashMap;
import java.util.Map;

/**
 * Repräsentiert den DAV-Enumerationstypen
 * <code>att.optionenPlausibilitätsPrüfungLogischVerkehr</code>.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public final class OptionenPlausibilitaetsPruefungLogischUfd extends AbstractDavZustand {

	/**
	 * Der Wertebereich dieses DAV-Enumerationstypen.
	 */
	private static Map<Integer, OptionenPlausibilitaetsPruefungLogischUfd> werteBereich = new HashMap<Integer, OptionenPlausibilitaetsPruefungLogischUfd>();

	/**
	 * Wertebereichsprüfung wird NICHT durchgeführt. Wert wird nicht verändert,
	 * es werden keine Statusflags gesetzt.
	 */
	public static final OptionenPlausibilitaetsPruefungLogischUfd KEINE_PRUEFUNG = new OptionenPlausibilitaetsPruefungLogischUfd(
			"Keine Prüfung", 0);

	/**
	 * Wertebereichsprüfung wird durchgeführt. Bei Messwertüberschreibung wird
	 * der Wert auf den maximalen Wert gesetzt und mit dem Flag MaxWertLogisch
	 * versehen.
	 */
	public static final OptionenPlausibilitaetsPruefungLogischUfd WERT_REDUZIEREN = new OptionenPlausibilitaetsPruefungLogischUfd(
			"Wert reduzieren", 1);

	/**
	 * Wertebereichsprüfung wird durchgeführt. Bei Messwertüberschreitung wird
	 * der wert als Implausibel und MaxWertLogisch gekennzeichnet und auf
	 * fehlerhaft gesetzt. Bei Geschwindigkeitswerten werden alle
	 * Geschwindigkeitswerte gleichzeitig betrachtet. Die Güte der Werte wird um
	 * 20% reduziert. Es wird eine Betriebsmeldugn erzeugt.
	 */
	public static final OptionenPlausibilitaetsPruefungLogischUfd AUF_FEHLERHAFT_SETZEN = new OptionenPlausibilitaetsPruefungLogischUfd(
			"Auf fehlerhaft setzen", 2);

	/**
	 * Standardkonstruktor.
	 * 
	 * @param code
	 *            der Code
	 * @param name
	 *            die Bezeichnung
	 */
	private OptionenPlausibilitaetsPruefungLogischUfd(String name, int code) {
		super(code, name);
		werteBereich.put(code, this);
	}

	/**
	 * Erfragt den Wert dieses DAV-Enumerationstypen mit dem übergebenen Code.
	 * 
	 * @param code
	 *            der Kode des Zustands
	 * @return der Code des Enumerations-Wertes
	 */
	public static OptionenPlausibilitaetsPruefungLogischUfd getZustand(int code) {
		return werteBereich.get(code);
	}
}
