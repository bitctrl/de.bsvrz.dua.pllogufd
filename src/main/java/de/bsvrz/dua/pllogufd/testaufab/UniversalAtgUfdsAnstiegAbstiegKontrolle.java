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

package de.bsvrz.dua.pllogufd.testaufab;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.AllgemeinerDatenContainer;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Klasse zum Auslesen von Parametersätzen der Attributgruppen
 * <code>atg.ufdsAnstiegAbstiegKontrolle<b>*</b></code>.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class UniversalAtgUfdsAnstiegAbstiegKontrolle extends
AllgemeinerDatenContainer {

	/**
	 * Skalierter maxDiff-Wert
	 */
	private double _scaledMax = Double.NaN;
	/**
	 * maximale Differenz zwischen zweier aufeinanderfolgender Messwerte.
	 */
	private long maxDiff = -1;

	/**
	 * zeigt an, ob der Parameter <code>maxDiff</code> selbst einen semantisch
	 * sinnvollen Zustand hat<br>
	 * . Also auf keinem der folgenden Zustände steht:<br>
	 * - <code>nicht ermittelbar</code><br>
	 * - <code>fehlerhaft</code>, oder<br>
	 * - <code>nicht ermittelbar/fehlerhaft</code><br>
	 */
	private boolean sinnvoll = true;

	/**
	 * Standardkonstruktor.
	 *
	 * @param parameter
	 *            ein Parameterdatensatz der Attributgruppe
	 *            <code>atg.ufdsAnstiegAbstiegKontrolle<b>*</b></code>
	 * @throws UmfeldDatenSensorUnbekannteDatenartException 
	 */
	public UniversalAtgUfdsAnstiegAbstiegKontrolle(final ResultData parameter) throws UmfeldDatenSensorUnbekannteDatenartException {
		if (parameter == null) {
			throw new NullPointerException("Übergebener Parameter ist <<null>>"); //$NON-NLS-1$
		}
		if (parameter.getData() == null) {
			throw new NullPointerException(
					"Übergebener Parameter hat keine Daten"); //$NON-NLS-1$
		}

		final UmfeldDatenArt datenArt = UmfeldDatenArt
				.getUmfeldDatenArtVon(parameter.getObject());

		final UmfeldDatenSensorWert wert = new UmfeldDatenSensorWert(datenArt);
		wert.setWert(parameter
				.getData()
				.getUnscaledValue(datenArt.getAbkuerzung() + "maxDiff").longValue()); //$NON-NLS-1$

		this.maxDiff = wert.getWert();

		this.sinnvoll = !wert.isFehlerhaft()
				&& !wert.isFehlerhaftBzwNichtErmittelbar()
				&& !wert.isNichtErmittelbar()
				&& wert.getWert() > 0;

		if(sinnvoll) {
			_scaledMax = parameter.getData().getScaledValue(datenArt.getAbkuerzung() + "maxDiff").doubleValue();
		}
	}

	/**
	 * Erfragt den Vergleichswert für die Anstiegs-Abfall-Kontrolle.
	 *
	 * @return der Vergleichswert für die Anstiegs-Abfall-Kontrolle
	 */
	public final long getMaxDiff() {
		return this.maxDiff;
	}

	/**
	 * Gibt den skalierten MaxDiff-Wert zurück (für die Ausgabe in Meldungen u.ä.)
	 * @return Skalierter MaxDiff-Wert
	 */
	public double getScaledMax() {
		return _scaledMax;
	}

	/**
	 * Erfragt, ob der Parameter <code>maxDiff</code> selbst einen semantisch
	 * sinnvollen Zustand hat<br>
	 * . Also auf keinem der folgenden Zustände steht:<br>
	 * - <code>nicht ermittelbar</code><br>
	 * - <code>fehlerhaft</code>, oder<br>
	 * - <code>nicht ermittelbar/fehlerhaft</code><br>
	 *
	 * @return ob der Parameter einen sinnvollen Wert enthält
	 */
	public final boolean isSinnvoll() {
		return this.sinnvoll;
	}
}
