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

package de.bsvrz.dua.pllogufd;

import stauma.dav.clientside.Data;
import stauma.dav.clientside.ResultData;

/**
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AtgUmfeldDatenSensorWert {

	/**
	 * ein DAV-Datum eines Umfelddatensensors
	 */
	private Data datum = null;
	
	/**
	 * Die Bezeichnung des Datums
	 */
	private String name = null;
		 
	
	public AtgUmfeldDatenSensorWert(final ResultData resultat){
		if(resultat == null){
			throw new NullPointerException("Datensatz ist <<null>>"); //$NON-NLS-1$
		}
		if(resultat.getData() == null){
			throw new NullPointerException("Datensatz enthält keine Daten"); //$NON-NLS-1$")
		}
		this.name = resultat.getObject().getType().getName().substring(4);
		this.datum = resultat.getData().createModifiableCopy();
	}


	/**
	 * @return statusErfassungNichtErfasst
	 */
	public final int getStatusErfassungNichtErfasst() {
		return this.datum.getItem(this.name).getItem("Status"). //$NON-NLS-1$
					getItem("Erfassung").getUnscaledValue("NichtErfasst").intValue();  //$NON-NLS-1$//$NON-NLS-2$
	}


	/**
	 * @param statusErfassungNichtErfasst Festzulegender statusErfassungNichtErfasst
	 */
	public final void setStatusErfassungNichtErfasst(int statusErfassungNichtErfasst) {
		this.datum.getItem(this.name).getItem("Status"). //$NON-NLS-1$
					getItem("Erfassung").getUnscaledValue("NichtErfasst").  //$NON-NLS-1$//$NON-NLS-2$
					set(statusErfassungNichtErfasst);
	}


	/**
	 * @return statusMessWertErsetzungImplausibel
	 */
	public final int getStatusMessWertErsetzungImplausibel() {
		return this.datum.getItem(this.name).getItem("Status"). //$NON-NLS-1$
		getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();  //$NON-NLS-1$//$NON-NLS-2$
	}


	/**
	 * @param statusMessWertErsetzungImplausibel Festzulegender statusMessWertErsetzungImplausibel
	 */
	public final void setStatusMessWertErsetzungImplausibel(
			int statusMessWertErsetzungImplausibel) {
		this.datum.getItem(this.name).getItem("Status"). //$NON-NLS-1$
					getItem("MessWertErsetzung").getUnscaledValue("Implausibel").  //$NON-NLS-1$//$NON-NLS-2$
					set(statusMessWertErsetzungImplausibel);
	}


	/**
	 * @return wert
	 */
	public final long getWert() {
		return this.datum.getItem(this.name).getUnscaledValue("Wert").longValue(); //$NON-NLS-1$
	}


	/**
	 * @param wert Festzulegender wert
	 */
	public final void setWert(long wert) {
		this.datum.getItem(this.name).getUnscaledValue("Wert").set(wert); //$NON-NLS-1$
	}


	/**
	 * @return datum
	 */
	public final Data getDatum() {
		return datum;
	}
		
}
