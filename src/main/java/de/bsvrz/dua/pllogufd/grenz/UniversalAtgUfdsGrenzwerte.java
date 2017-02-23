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

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Klasse zum Auslesen von Parametersätzen der Attributgruppen
 * <code>atg.ufdsGrenzwerte<b>*</b></code>.
 *
 * @author Kappich Systemberatung
 */
public class UniversalAtgUfdsGrenzwerte {

	/**
	 * Max-Grenzwert
	 */
	private final double maxSkaliert;
	/**
	 * Max-Grenzwert unskaliert
	 */
	private final long max; // = -1;

	/**
	 * Min-Grenzwert
	 * 
	 * TODO Wert ist quasi-final, weil es keine Setter gibt
	 */
	private double minSkaliert = Double.NaN;
	/**
	 * Min-Grenzwert unskaliert
	 * 
	 * TODO Wert ist quasi-final, weil es keine Setter gibt
	 */
	private long min = -1;

	/**
	 * Aktuelles Verhalten der Grenzwertprüfung
	 */
	private final OptionenPlausibilitaetsPruefungLogischUfd _verhalten; // =
																		// OptionenPlausibilitaetsPruefungLogischUfd.KEINE_PRUEFUNG;

	/**
	 * zeigt an, ob der Parameter <code>max</code> selbst einen semantisch
	 * sinnvollen Zustand hat<br>
	 * . Also auf keinem der folgenden Zustände steht:<br>
	 * - <code>nicht ermittelbar</code><br>
	 * - <code>fehlerhaft</code>, oder<br>
	 * - <code>nicht ermittelbar/fehlerhaft</code><br>
	 */
	private boolean maxsinnvoll = false;

	/**
	 * zeigt an, ob der Parameter <code>min</code> selbst einen semantisch
	 * sinnvollen Zustand hat<br>
	 * . Also auf keinem der folgenden Zustände steht:<br>
	 * - <code>nicht ermittelbar</code><br>
	 * - <code>fehlerhaft</code>, oder<br>
	 * - <code>nicht ermittelbar/fehlerhaft</code><br>
	 */
	private boolean minsinnvoll = false;

	/**
	 * Standardkonstruktor.
	 *
	 * @param parameter
	 *            ein Parameterdatensatz der Attributgruppe
	 *            <code>atg.ufdsGrenzwerte<b>*</b></code>
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public UniversalAtgUfdsGrenzwerte(final ResultData parameter) throws UmfeldDatenSensorUnbekannteDatenartException {
		if (parameter == null) {
			throw new NullPointerException("Übergebener Parameter ist <<null>>"); 
		}
		if (parameter.getData() == null) {
			throw new NullPointerException(
					"Übergebener Parameter hat keine Daten"); 
		}

		final UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(parameter.getObject());

		final UmfeldDatenSensorWert wert = new UmfeldDatenSensorWert(datenArt);
		wert.setWert(parameter
				.getData()
				.getUnscaledValue(datenArt.getAbkuerzung() + "max").longValue()); 

		this.max = wert.getWert();
		this.maxSkaliert = wert.getSkaliertenWert();
		this.maxsinnvoll = !wert.isFehlerhaft() && !wert.isFehlerhaftBzwNichtErmittelbar()
				&& !wert.isNichtErmittelbar();

		try {
			wert.setWert(parameter.getData().getUnscaledValue(datenArt.getAbkuerzung() + "min").longValue());

			this.min = wert.getWert();
			this.minSkaliert = wert.getSkaliertenWert();

			this.minsinnvoll = !wert.isFehlerhaft() && !wert.isFehlerhaftBzwNichtErmittelbar()
					&& !wert.isNichtErmittelbar();
		} catch (Exception ignored) {
		}

		this._verhalten = OptionenPlausibilitaetsPruefungLogischUfd
				.getZustand(parameter.getData().getUnscaledValue("Verhalten").intValue());
	}

	/**
	 * Gibt den unskalierten Max-Wert zurück
	 * 
	 * @return den Max-Wert
	 */
	public final long getMax() {
		return this.max;
	}

	/**
	 * Gibt den skalierten Max-Wert zurück
	 * 
	 * @return den skalierten Max-Wert
	 */
	public double getMaxSkaliert() {
		return maxSkaliert;
	}

	/**
	 * Erfragt, ob der Parameter <code>max</code> selbst einen semantisch
	 * sinnvollen Zustand hat<br>
	 * . Also auf keinem der folgenden Zustände steht:<br>
	 * - <code>nicht ermittelbar</code><br>
	 * - <code>fehlerhaft</code>, oder<br>
	 * - <code>nicht ermittelbar/fehlerhaft</code><br>
	 *
	 * @return ob der Parameter einen sinnvollen Wert enthält
	 */
	public boolean isMaxSinnvoll() {
		return maxsinnvoll;
	}

	/**
	 * Erfragt, ob der Parameter <code>min</code> selbst einen semantisch
	 * sinnvollen Zustand hat<br>
	 * . Also auf keinem der folgenden Zustände steht:<br>
	 * - <code>nicht ermittelbar</code><br>
	 * - <code>fehlerhaft</code>, oder<br>
	 * - <code>nicht ermittelbar/fehlerhaft</code><br>
	 * 
	 * Falls der Parameter nicht vorhanden ist, wird false zurückgegeben.
	 *
	 * @return ob der Parameter einen sinnvollen Wert enthält
	 */
	public boolean isMinSinnvoll() {
		return minsinnvoll;
	}

	/**
	 * Gibt das Verhalten zurück
	 * 
	 * @return das Verhalten
	 */
	public OptionenPlausibilitaetsPruefungLogischUfd getVerhalten() {
		return _verhalten;
	}

	/**
	 * Gibt den unskalierten Minimalwert zurück
	 * 
	 * @return den Minimalwert
	 */
	public long getMin() {
		return this.min;
	}

	/**
	 * Gibt den skalierten Minimalwert zurück
	 * 
	 * @return den skalierten Minimalwert
	 */
	public double getMinSkaliert() {
		return minSkaliert;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_verhalten == null) ? 0 : _verhalten.hashCode());
		result = prime * result + (int) (max ^ (max >>> 32));
		long temp;
		temp = Double.doubleToLongBits(maxSkaliert);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (min ^ (min >>> 32));
		temp = Double.doubleToLongBits(minSkaliert);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UniversalAtgUfdsGrenzwerte other = (UniversalAtgUfdsGrenzwerte) obj;
		if (_verhalten == null) {
			if (other._verhalten != null)
				return false;
		} else if (!_verhalten.equals(other._verhalten))
			return false;
		if (max != other.max)
			return false;
		if (Double.doubleToLongBits(maxSkaliert) != Double.doubleToLongBits(other.maxSkaliert))
			return false;
		if (min != other.min)
			return false;
		if (Double.doubleToLongBits(minSkaliert) != Double.doubleToLongBits(other.minSkaliert))
			return false;
		return true;
	}
}
