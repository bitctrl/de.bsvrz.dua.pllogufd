/**
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.3 Pl-Pr�fung logisch UFD
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
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.pllogufd.testmeteo.ni;

import de.bsvrz.dua.pllogufd.UmfeldDatenSensorWert;
import de.bsvrz.dua.pllogufd.testmeteo.AbstraktMeteoUmfeldDatenSensor;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;

/**
 * Parameter f�r die meteorologische Kontrolle "Niederschlagsintensit�t"
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public class NiederschlagsIntensitaetsParameter
extends	AbstraktMeteoUmfeldDatenSensor {
	
	/**
	 * Wenn NS= 'Niederschlag' und NI = 0 mm/h und RLF < NIgrenzNassRLF, dann NI implausibel
	 */
	private UmfeldDatenSensorWert niGrenzNassRLF = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
	
	/**
	 * Wenn NS = 'kein Niederschlag' und NI > NIminNI und RLF < NIgrenzTrockenRLF, dann NI
	 * implausibel
	 */
	private UmfeldDatenSensorWert niMinNI = new UmfeldDatenSensorWert(UmfeldDatenArt.NI);
	
	/**
	 * Wenn NI > 0,5 mm/h und WDF = 0 mm und RLF < NIgrenzTrockenRLF f�r Zeitraum > NIminTrockenRLF,
	 * dann NI implausibel
	 */
	private UmfeldDatenSensorWert niGrenzTrockenRLF = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
	
	/**
	 * Wenn NI > 0,5 mm/h und WDF = 0 mm und RLF < NIgrenzTrockenRLF f�r Zeitraum > NIminTrockenRLF,
	 * dann NI implausibel
	 */
	private long niMinTrockenRLF = -1;
	

	/**
	 * {@inheritDoc}
	 */
	public NiederschlagsIntensitaetsParameter(IVerwaltung verwaltung, SystemObject obj)
	throws DUAInitialisierungsException {
		super(verwaltung, obj);
	}

	
	/**
	 * Erfragt <code>NIGrenzNassRLF<code>
	 * 
	 * @return NIGrenzNassRLF
	 */
	public final synchronized UmfeldDatenSensorWert getNIGrenzNassRLF() {
		return this.niGrenzNassRLF;
	}


	/**
	 * Erfragt <code>NIminNI<code>
	 * 
	 * @return NIminNI
	 */
	public final synchronized UmfeldDatenSensorWert getNIminNI() {
		return this.niMinNI;
	}


	/**
	 * Erfragt <code>NIGrenzTrockenRLF<code>
	 * 
	 * @return NIGrenzTrockenRLF
	 */
	public final synchronized UmfeldDatenSensorWert getNIGrenzTrockenRLF() {
		return this.niGrenzTrockenRLF;
	}


	/**
	 * Erfragt <code>NIminTrockenRLF<code>
	 * 
	 * @return NIminTrockenRLF
	 */
	public final long getNIminTrockenRLF() {
		return this.niMinTrockenRLF;
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
						this.niGrenzNassRLF.setWert(resultat.getData().getUnscaledValue("NIgrenzNassRLF").longValue()); //$NON-NLS-1$
						this.niMinNI.setWert(resultat.getData().getUnscaledValue("NIminNI").longValue()); //$NON-NLS-1$
						this.niGrenzTrockenRLF.setWert(resultat.getData().getUnscaledValue("NIgrenzTrockenRLF").longValue()); //$NON-NLS-1$
						this.niMinTrockenRLF = resultat.getData().getTimeValue("NIminTrockenRLF").getMillis(); //$NON-NLS-1$						
					}
					this.parameterInitialisiert = true;
				}
			}
		}
	}

}
