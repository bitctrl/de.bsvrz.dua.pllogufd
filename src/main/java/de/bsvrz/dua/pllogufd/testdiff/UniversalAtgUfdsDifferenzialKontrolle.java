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

package de.bsvrz.dua.pllogufd.testdiff;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.pllogufd.typen.UfdsVergleichsOperator;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Klasse zum Auslesen von Parametersätzen der Attributgruppen
 * <code>atg.ufdsDifferenzialKontrolle<b>*</b></code>.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class UniversalAtgUfdsDifferenzialKontrolle {

	/**
	 * zu verwendender Operator zum Vergleich des Messwerts mit dem Grenzwert,
	 * der eingehalten werden muss, damit Differenzialkontrolle durchgeführt
	 * werden darf.
	 */
	private final UfdsVergleichsOperator operator;//  = null;

	/**
	 * Grenzwert, der eingehalten werden muss, damit Differenzialkontrolle
	 * durchgeführt werden darf.
	 */
	private final UmfeldDatenSensorWert grenz; //  = null;

	/**
	 * maximal zulässige Zeitdauer der Ergebniskonstanz.
	 */
	private final  long maxZeit; // = -1;

	/**
	 * Standardkonstruktor.
	 *
	 * @param parameter
	 *            ein Parameterdatensatz der Attributgruppe
	 *            <code>atg.ufdsDifferenzialKontrolle<b>*</b></code>
	 * @throws UmfeldDatenSensorUnbekannteDatenartException 
	 */
	public UniversalAtgUfdsDifferenzialKontrolle(final ResultData parameter) throws UmfeldDatenSensorUnbekannteDatenartException {
		if (parameter == null) {
			throw new NullPointerException("Übergebener Parameter ist <<null>>"); //$NON-NLS-1$
		}
		if (parameter.getData() == null) {
			throw new NullPointerException(
					"Übergebener Parameter hat keine Daten"); //$NON-NLS-1$
		}
		final UmfeldDatenArt datenArt = UmfeldDatenArt
				.getUmfeldDatenArtVon(parameter.getObject());

		final Data.NumberValue oparatorValue = parameter.getData()
				.getUnscaledValue("Operator"); //$NON-NLS-1$
		if (oparatorValue != null) {
			this.operator = UfdsVergleichsOperator.getZustand(oparatorValue
					.intValue());
		} else {
			operator = null;
		}
		
		this.grenz = new UmfeldDatenSensorWert(datenArt);
		this.grenz
				.setWert(parameter
						.getData()
						.getUnscaledValue(datenArt.getAbkuerzung() + "Grenz").longValue()); //$NON-NLS-1$
		this.maxZeit = parameter.getData()
				.getTimeValue(datenArt.getAbkuerzung() + "maxZeit").getMillis(); //$NON-NLS-1$
	}

	/**
	 * Erfragt den zu verwendenden Operator zum Vergleich des Messwerts mit dem
	 * Grenzwert, der eingehalten werden muss, damit Differenzialkontrolle
	 * durchgeführt werden darf.
	 *
	 * @return der zu verwendende Operator
	 */
	public final UfdsVergleichsOperator getOperator() {
		return this.operator;
	}

	/**
	 * Erfragt den Grenzwert, der eingehalten werden muss, damit
	 * Differenzialkontrolle durchgeführt werden darf.
	 *
	 * @return grenz der Grenzwert, der eingehalten werden muss, damit
	 *         Differenzialkontrolle durchgeführt werden darf
	 */
	public final UmfeldDatenSensorWert getGrenz() {
		return this.grenz;
	}

	/**
	 * Erfragt die maximal zulässige Zeitdauer der Ergebniskonstanz.
	 *
	 * @return maxZeit maximal zulässige Zeitdauer der Ergebniskonstanz
	 */
	public final long getMaxZeit() {
		return this.maxZeit;
	}
}
