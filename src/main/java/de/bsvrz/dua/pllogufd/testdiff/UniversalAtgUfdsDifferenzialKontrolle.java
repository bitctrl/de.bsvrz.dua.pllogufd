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

package de.bsvrz.dua.pllogufd.testdiff;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.pllogufd.typen.UfdsVergleichsOperator;
import de.bsvrz.sys.funclib.bitctrl.dua.AllgemeinerDatenContainer;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Klasse zum Auslesen von Parametersätzen der Attributgruppen
 * <code>atg.ufdsDifferenzialKontrolle<b>*</b></code>.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class UniversalAtgUfdsDifferenzialKontrolle extends
		AllgemeinerDatenContainer {

	/**
	 * zu verwendender Operator zum Vergleich des Messwerts mit dem Grenzwert,
	 * der eingehalten werden muss, damit Differenzialkontrolle durchgeführt
	 * werden darf.
	 */
	private UfdsVergleichsOperator operator = null;

	/**
	 * Grenzwert, der eingehalten werden muss, damit Differenzialkontrolle
	 * durchgeführt werden darf.
	 */
	private UmfeldDatenSensorWert grenz = null;

	/**
	 * maximal zulässige Zeitdauer der Ergebniskonstanz.
	 */
	private long maxZeit = -1;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param parameter
	 *            ein Parameterdatensatz der Attributgruppe
	 *            <code>atg.ufdsDifferenzialKontrolle<b>*</b></code>
	 */
	public UniversalAtgUfdsDifferenzialKontrolle(final ResultData parameter) {
		if (parameter == null) {
			throw new NullPointerException("Übergebener Parameter ist <<null>>"); //$NON-NLS-1$
		}
		if (parameter.getData() == null) {
			throw new NullPointerException(
					"Übergebener Parameter hat keine Daten"); //$NON-NLS-1$
		}
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(parameter
				.getObject());

		Data.NumberValue oparatorValue = parameter.getData().getUnscaledValue(
				"Operator"); //$NON-NLS-1$
		if (oparatorValue != null) {
			this.operator = UfdsVergleichsOperator.getZustand(oparatorValue
					.intValue());
		}
		this.grenz = new UmfeldDatenSensorWert(UmfeldDatenArt
				.getUmfeldDatenArtVon(parameter.getObject()));
		this.grenz.setWert(parameter.getData().getUnscaledValue(
				datenArt.getAbkuerzung() + "Grenz").longValue()); //$NON-NLS-1$
		this.maxZeit = parameter.getData().getTimeValue(
				datenArt.getAbkuerzung() + "maxZeit").getMillis(); //$NON-NLS-1$
	}

	/**
	 * Erfragt den zu verwendenden Operator zum Vergleich des Messwerts mit dem
	 * Grenzwert, der eingehalten werden muss, damit Differenzialkontrolle
	 * durchgeführt werden darf.
	 * 
	 * @return der zu verwendende Operator
	 */
	public final UfdsVergleichsOperator getOpertator() {
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
