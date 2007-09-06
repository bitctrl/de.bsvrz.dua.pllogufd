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

package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.Collection;
import java.util.HashSet;

import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.AbstraktUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Abstrakte Klasse für Umfelddatensensoren, die der Meteorologischen Kontrolle 
 * zugeführt werden
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public abstract class AbstraktMeteoUmfeldDatenSensor 
extends AbstraktUmfeldDatenSensor{

	/**
	 * wurden schon einmal Parameter empfangen
	 */
	protected boolean parameterInitialisiert = false;
	
	

	/**
	 * Standardkonstruktor
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param obj das mit dieser Instanz zu assoziierende Systemobjekt 
	 * (vom Typ <code>typ.umfeldDatenSensor</code>)
	 * @throws DUAInitialisierungsException wird weitergereicht
	 */
	protected AbstraktMeteoUmfeldDatenSensor(IVerwaltung verwaltung, SystemObject obj)
	throws DUAInitialisierungsException {
		super(verwaltung, obj);
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<AttributeGroup> getParameterAtgs()
	throws DUAInitialisierungsException {
		if(this.objekt == null){
			throw new NullPointerException("Parameter können nicht bestimmt werden," + //$NON-NLS-1$
					" da noch kein Objekt festgelegt ist"); //$NON-NLS-1$
		}

		Collection<AttributeGroup> parameterAtgs = new HashSet<AttributeGroup>();
		
		final String atgPid = "atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
						UmfeldDatenArt.getUmfeldDatenArtVon(this.objekt).getName();
				
		AttributeGroup atg = VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup(atgPid);

		if(atg != null){
			parameterAtgs.add(atg);
		}else{
			throw new DUAInitialisierungsException("Es konnte keine Parameter-Attributgruppe für die " + //$NON-NLS-1$
					"Meteorologische Kontrolle des Objektes " + this.objekt + " bestimmt werden\n" +  //$NON-NLS-1$//$NON-NLS-2$ 
					"Atg-Name: " + atgPid);  //$NON-NLS-1$
		}
		
		return parameterAtgs;
	}
	
	
	/**
	 * Erfragt, ob schon einmal Parameter empfangen wurden
	 * 
	 * @return ob schon einmal Parameter empfangen wurden
	 */
	public final boolean isInitialisiert(){
		return this.parameterInitialisiert;
	}
}
