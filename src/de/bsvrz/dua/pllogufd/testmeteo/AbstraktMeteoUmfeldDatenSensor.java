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

package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.Collection;
import java.util.TreeSet;

import stauma.dav.configuration.interfaces.AttributeGroup;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.AbstraktUmfeldDatenSensor;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Abstrakte Klasse f�r Umfelddatensensoren, die der Meteorologischen Kontrolle 
 * zugef�hrt werden
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public abstract class AbstraktMeteoUmfeldDatenSensor 
extends AbstraktUmfeldDatenSensor{

	
	/**
	 * {@inheritDoc}
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
			throw new NullPointerException("Parameter k�nnen nicht bestimmt werden," + //$NON-NLS-1$
					" da noch kein Objekt festgelegt ist"); //$NON-NLS-1$
		}

		Collection<AttributeGroup> parameterAtgs = new TreeSet<AttributeGroup>();
		
		final String atgPid = "atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
						UmfeldDatenArt.getUmfeldDatenArtVon(this.objekt).getName();
				
		AttributeGroup atg = VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup(atgPid);

		if(atg != null){
			parameterAtgs.add(atg);
		}else{
			throw new DUAInitialisierungsException("Es konnte keine Parameter-Attributgruppe f�r die " + //$NON-NLS-1$
					"Meteorologische Kontrolle des Objektes " + this.objekt + " bestimmt werden\n" +  //$NON-NLS-1$//$NON-NLS-2$ 
					"Atg-Name: " + atgPid);  //$NON-NLS-1$
		}
		
		return parameterAtgs;
	}
	
}