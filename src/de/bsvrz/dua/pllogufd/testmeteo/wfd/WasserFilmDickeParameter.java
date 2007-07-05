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

package de.bsvrz.dua.pllogufd.testmeteo.wfd;

import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.UmfeldDatenSensorWert;
import de.bsvrz.dua.pllogufd.testmeteo.AbstraktMeteoUmfeldDatenSensor;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Parameter für die meteorologische Kontrolle "Wasserfilmdicke"
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public class WasserFilmDickeParameter
extends	AbstraktMeteoUmfeldDatenSensor {

	/**
	 * Wenn NI > 0,5 mm/h und WDF = 0 mm und RLF > WFDgrenzNassRLF für Zeitraum > WDFminNassRLF, dann WFD implausibel
	 */
	private UmfeldDatenSensorWert wfdGrenzNassRLF = new UmfeldDatenSensorWert(UmfeldDatenArt.WFD);
	
	/**
	 * Wenn NI > 0,5 mm/h und WDF = 0 mm und RLF > WFDgrenzNassRLF für Zeitraum > WDFminNassRLF, dann WFD implausibel
	 */
	private long wfdMinNassRLF = -1;
	

	/**
	 * {@inheritDoc}
	 */
	public WasserFilmDickeParameter(IVerwaltung verwaltung, SystemObject obj)
	throws DUAInitialisierungsException {
		super(verwaltung, obj);
	}


	/**
	 * Erfragt <code>WFDgrenzNassRLF<code>
	 * 
	 * @return WFDgrenzNassRLF
	 */
	public final synchronized UmfeldDatenSensorWert getWFDgrenzNassRLF() {
		return this.wfdGrenzNassRLF;
	}


	/**
	 * Erfragt <code>WFDminNassRLF<code>
	 * 
	 * @return WFDminNassRLF
	 */
	public final long getWFDminNassRLF() {
		return this.wfdMinNassRLF;
	}

	
	/**
	 * TODO
	 * @return
	 */
	public final synchronized UmfeldDatenSensorWert getEXTRA(){
		return new UmfeldDatenSensorWert(UmfeldDatenArt.FBF);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
					synchronized (this) {
						this.wfdGrenzNassRLF.setWert(resultat.getData().getUnscaledValue("WFDgrenzNassRLF").longValue()); //$NON-NLS-1$
						this.wfdMinNassRLF = resultat.getData().getTimeValue("WFDminNassRLF").getMillis(); //$NON-NLS-1$						
					}
					this.parameterInitialisiert = true;
				}
			}
		}
	}

}
