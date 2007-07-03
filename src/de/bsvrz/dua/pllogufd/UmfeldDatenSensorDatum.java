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
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;

/**
 * Schnittstelle zu einem Roh-Sensorwert eines Umfelddatensensors
 * <b>mit</b> Plausibilisierungs-Informationen
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class UmfeldDatenSensorDatum {

	/**
	 * die Art des Umfelddatums
	 */
	private UmfeldDatenArt datenArt = null;
	
	/**
	 * ein DAV-Datum eines Umfelddatensensors
	 */
	private Data datum = null;
	
	/**
	 * Indiziert, ob es sich bei diesem Datum schon um eine modifizierbare Kopie handelt
	 */
	private boolean copy = false;
	
	/**
	 * der eigentliche Wert des Umfelddatensensors (ohne Plausibilisierungs-Informationen)
	 */
	private UmfeldDatenSensorWert wert = null;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param resultat ein Roh-Sensorwert eines Umfelddatensensors
	 */
	public UmfeldDatenSensorDatum(final ResultData resultat){
		if(resultat == null){
			throw new NullPointerException("Datensatz ist <<null>>"); //$NON-NLS-1$
		}
		if(resultat.getData() == null){
			throw new NullPointerException("Datensatz enthält keine Daten"); //$NON-NLS-1$")
		}
		
		this.datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(resultat.getObject());
		
		if(datenArt == null){
			throw new NullPointerException("Datenart konnte nicht identifiziert werden:\n" +  //$NON-NLS-1$
					resultat);
		}
		
		this.wert = new UmfeldDatenSensorWert(datenArt);
		this.wert.setWert(this.datum.getItem(this.datenArt.getName()).getUnscaledValue("Wert").longValue()); //$NON-NLS-1$
		this.wert.setVeraendert(false);
		
		this.datum = resultat.getData();
	}

	
	/**
	 * Erstellt eine Kopie des hier verarbeiteten Datums
	 */
	private final void erstelleKopie(){
		if(!this.copy){
			this.copy = true;
			this.datum = this.datum.createModifiableCopy();
		}
	}
	

	/**
	 * Erfragt das Erfassungsintervall dieses Datums
	 * 
	 * @return das Erfassungsintervall dieses Datums
	 */
	public final long getT(){
		return this.datum.getTimeValue("T").getMillis(); //$NON-NLS-1$
	}
	
	
	/**
	 * Erfragt den Wert <code>Status.Erfassung.NichtErfasst</code>
	 * 
	 * @return der Wert <code>Status.Erfassung.NichtErfasst</code>
	 */
	public final int getStatusErfassungNichtErfasst() {
		return this.datum.getItem(this.datenArt.getName()).getItem("Status"). //$NON-NLS-1$
					getItem("Erfassung").getUnscaledValue("NichtErfasst").intValue();  //$NON-NLS-1$//$NON-NLS-2$
	}


	/**
	 * Setzt den Wert <code>Status.Erfassung.NichtErfasst</code>
	 * 
	 * @param der Wert <code>Status.Erfassung.NichtErfasst</code>
	 */
	public final void setStatusErfassungNichtErfasst(int statusErfassungNichtErfasst) {
		this.erstelleKopie();
		this.datum.getItem(this.datenArt.getName()).getItem("Status"). //$NON-NLS-1$
					getItem("Erfassung").getUnscaledValue("NichtErfasst").  //$NON-NLS-1$//$NON-NLS-2$
					set(statusErfassungNichtErfasst);
	}


	/**
	 * Erfragt den Wert <code>Status.MessWertErsetzung.Implausibel</code>
	 * 
	 * @return der Wert <code>Status.MessWertErsetzung.Implausibel</code>
	 */
	public final int getStatusMessWertErsetzungImplausibel() {
		return this.datum.getItem(this.datenArt.getName()).getItem("Status"). //$NON-NLS-1$
		getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();  //$NON-NLS-1$//$NON-NLS-2$
	}


	/**
	 * Setzt den Wert <code>Status.MessWertErsetzung.Implausibel</code>
	 * 
	 * @param der Wert <code>Status.MessWertErsetzung.Implausibel</code>
	 */
	public final void setStatusMessWertErsetzungImplausibel(
								int statusMessWertErsetzungImplausibel) {
		this.erstelleKopie();
		this.datum.getItem(this.datenArt.getName()).getItem("Status"). //$NON-NLS-1$
					getItem("MessWertErsetzung").getUnscaledValue("Implausibel").  //$NON-NLS-1$//$NON-NLS-2$
					set(statusMessWertErsetzungImplausibel);
	}


	/**
	 * Erfragt den Wert selbst
	 * 
	 * @return der Sensor-Messwert 
	 */
	public final UmfeldDatenSensorWert getWert() {
		return this.wert;
	}


	/**
	 * Erfragt das mit dem aktuellen Zustand dieses Objektes assoziierte DAV-Datum
	 *  
	 * @return das mit dem aktuellen Zustand dieses Objektes assoziierte DAV-Datum
	 */
	public final Data getDatum() {
		if(this.wert.isVeraendert()){
			this.erstelleKopie();
			this.datum.getItem(this.datenArt.getName()).getUnscaledValue("Wert").set(this.wert.getWert()); //$NON-NLS-1$
		}
		return this.datum;
	}

}
