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

package de.bsvrz.dua.pllogufd.testausfall;

import stauma.dav.clientside.ResultData;

/**
 * Nach assoziiertem Systemobjekt sortierbares Roh-Datum eines
 * Umfelddatensensors
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AusfallUFDSDatum
implements Comparable<AusfallUFDSDatum>{
	
	/**
	 * der Roh-Datum eines Umfelddatensensors
	 */
	private ResultData resultat = null; 


	/**
	 * Standardkonstruktor
	 * 
	 * @param resultat ein Datum eines Umfelddatensensors.<br><b>ACHTUNG:</b>
	 * Das Datum darf nicht <code>null</code> sein und muss Daten besitzen<br>
	 */
	protected AusfallUFDSDatum(final ResultData resultat) {
		if(resultat == null){
			throw new NullPointerException("Roh-Datum ist <<null>>"); //$NON-NLS-1$
		}
		if(resultat.getData() == null){
			throw new NullPointerException("Roh-Datum hat keine Daten"); //$NON-NLS-1$
		}
		this.resultat = resultat;
	}
	
	
	/**
	 * Erfragt das Roh-Datum eines Umfelddatensensors
	 * 
	 * @return das Roh-Datum eines Umfelddatensensors
	 */
	public final ResultData getDatum() {
		return this.resultat;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ergebnis = false;
		
		if(obj != null && obj instanceof AusfallUFDSDatum){
			AusfallUFDSDatum that = (AusfallUFDSDatum)obj;
			ergebnis = this.getDatum().getObject().equals(that.getDatum().getObject());
		}
		
		return ergebnis;
	}


	/**
	 * {@inheritDoc}
	 */
	public int compareTo(AusfallUFDSDatum that) {
		return new Long(this.getDatum().getObject().getId()).compareTo(
						that.getDatum().getObject().getId());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.resultat.toString();
	}

}
