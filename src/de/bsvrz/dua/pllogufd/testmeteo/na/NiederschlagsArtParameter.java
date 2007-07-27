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

package de.bsvrz.dua.pllogufd.testmeteo.na;

import de.bsvrz.dua.pllogufd.testmeteo.AbstraktMeteoUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;

/**
 * Parameter für die meteorologische Kontrolle "Niederschlagsart"
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public class NiederschlagsArtParameter
extends AbstraktMeteoUmfeldDatenSensor {
	
	/**
	 * Wenn NS = 'Regen' und LT < NSGrenzLT, dann NS implausibel
	 */
	private UmfeldDatenSensorWert nsGrenzLT = new UmfeldDatenSensorWert(UmfeldDatenArt.LT);
	
	/**
	 * Wenn NS = 'Niederschlag' und NI = 0 mm/h und RLF < NSGrenzTrockenRLF,
	 * dann NS implausibel
	 */
	private UmfeldDatenSensorWert nsGrenzTrockenRLF = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
	
	/**
	 * Wenn NS='kein Niederschlag' und NI >NSminNI und RLF>NSGrenzNassRLF,
	 * dann NS implausibel
	 */
	private UmfeldDatenSensorWert nsMinNI = new UmfeldDatenSensorWert(UmfeldDatenArt.NI);
	
	/**
	 * Wenn NI > 0,5 mm/h und WDF = 0 mm und RLF < NIgrenzTrockenRLF für Zeitraum > NIminTrockenRLF,
	 * dann NI implausibel
	 */
	private UmfeldDatenSensorWert nsGrenzRLF = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
	

	/**
	 * {@inheritDoc}
	 */
	public NiederschlagsArtParameter(IVerwaltung verwaltung, SystemObject obj)
	throws DUAInitialisierungsException {
		super(verwaltung, obj);
	}

	
	/**
	 * Erfragt <code>NSGrenzLT<code>
	 * 
	 * @return NSGrenzLT
	 */
	public synchronized final UmfeldDatenSensorWert getNsGrenzLT() {
		return this.nsGrenzLT;
	}


	/**
	 * Erfragt <code>NSGrenzRLF<code>
	 * 
	 * @return NSGrenzRLF
	 */
	public synchronized final UmfeldDatenSensorWert getNsGrenzRLF() {
		return this.nsGrenzRLF;
	}


	/**
	 * Erfragt <code>NSGrenzTrockenRLF<code>
	 * 
	 * @return NSGrenzTrockenRLF
	 */
	public synchronized final UmfeldDatenSensorWert getNsGrenzTrockenRLF() {
		return this.nsGrenzTrockenRLF;
	}


	/**
	 * Erfragt <code>NSminNI<code>
	 * 
	 * @return NSminNI
	 */
	public synchronized final UmfeldDatenSensorWert getNsMinNI() {
		return this.nsMinNI;
	}


	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
					synchronized(this){
						this.nsGrenzLT.setWert(resultat.getData().getUnscaledValue("NSGrenzLT").longValue()); //$NON-NLS-1$
						this.nsGrenzTrockenRLF.setWert(resultat.getData().getUnscaledValue("NSGrenzTrockenRLF").longValue()); //$NON-NLS-1$
						this.nsMinNI.setWert(resultat.getData().getUnscaledValue("NSminNI").longValue()); //$NON-NLS-1$
						this.nsGrenzRLF.setWert(resultat.getData().getUnscaledValue("NSGrenzRLF").longValue()); //$NON-NLS-1$
					}
					this.parameterInitialisiert = true;
					LOGGER.info("Neue Parameter für (" + resultat.getObject() + "):\n" //$NON-NLS-1$ //$NON-NLS-2$
							+ this);
				}
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "nsGrenzLT = " + nsGrenzLT + //$NON-NLS-1$
				"\nnsGrenzTrockenRLF = " + nsGrenzTrockenRLF +//$NON-NLS-1$
				"\nnsMinNI = " + nsMinNI + //$NON-NLS-1$
				"\nnsGrenzRLF = " + nsGrenzRLF;//$NON-NLS-1$
	}	
	
}
