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

package de.bsvrz.dua.pllogufd.testaufab;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.AllgemeinerDatenContainer;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Klasse zum Auslesen von Parametersätzen der Attributgruppen
 * <code>atg.ufdsAnstiegAbstiegKontrolle<b>*</b></code>
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class UniversalAtgUfdsAnstiegAbstiegKontrolle
extends AllgemeinerDatenContainer{
		
	/**
	 * maximale Differenz zwischen zweier aufeinanderfolgender Messwerte
	 */
	private long maxDiff = -1;
	
	/**
	 * zeigt an, ob der Parameter <code>maxDiff</code> selbst einen semantisch
	 * sinnvollen Zustand hat<br>
	 * Also auf keinem der folgenden Zustände steht:<br>
	 * - <code>nicht ermittelbar</code><br>
	 * - <code>fehlerhaft</code>, oder<br>
	 * - <code>nicht ermittelbar/fehlerhaft</code><br>
	 */
	private boolean sinnvoll = true;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param parameter ein Parameterdatensatz der Attributgruppe
	 * <code>atg.ufdsAnstiegAbstiegKontrolle<b>*</b></code>
	 */
	public UniversalAtgUfdsAnstiegAbstiegKontrolle(final ResultData parameter){
		if(parameter == null){
			throw new NullPointerException("Übergebener Parameter ist <<null>>"); //$NON-NLS-1$
		}
		if(parameter.getData() == null){
			throw new NullPointerException("Übergebener Parameter hat keine Daten"); //$NON-NLS-1$
		}
		
		final UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(parameter.getObject());
		
		UmfeldDatenSensorWert wert = new UmfeldDatenSensorWert(datenArt);
		wert.setWert(parameter.getData().getUnscaledValue(datenArt.getAbkuerzung() + "maxDiff").longValue()); //$NON-NLS-1$
		
		this.maxDiff = wert.getWert();
			
		this.sinnvoll = !wert.isFehlerhaft() && !wert.isFehlerhaftBzwNichtErmittelbar() && !wert.isNichtErmittelbar();
	}
	
	
	/**
	 * Erfragt den Vergleichswert für die Anstiegs-Abfall-Kontrolle
	 * 
	 * @return der Vergleichswert für die Anstiegs-Abfall-Kontrolle
	 */
	public final long getMaxDiff(){
		return this.maxDiff;
	}
	
	
	/**
	 * Erfragt, ob der Parameter <code>maxDiff</code> selbst einen semantisch
	 * sinnvollen Zustand hat<br>
	 * Also auf keinem der folgenden Zustände steht:<br>
	 * - <code>nicht ermittelbar</code><br>
	 * - <code>fehlerhaft</code>, oder<br>
	 * - <code>nicht ermittelbar/fehlerhaft</code><br>
	 * 
	 * @return ob der Parameter einen sinnvollen Wert enthält
	 */
	public final boolean isSinnvoll(){
		return this.sinnvoll;
	}
}
