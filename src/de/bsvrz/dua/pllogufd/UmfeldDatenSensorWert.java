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

import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

/**
 * Schnittstelle zu einem Roh-Sensorwert (dem eigentlichen Wert) eines Umfelddatensensors
 * <b>ohne</b> Plausibilisierungs-Informationen
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class UmfeldDatenSensorWert
implements Comparable<UmfeldDatenSensorWert>{

	/**
	 * der Wert an sich
	 */
	private long wert = 0;
	
	/**
	 * Daten nicht ermittelbar, da bereits Basiswerte fehlerhaft. Wird gesetzt, wenn Daten,
	 * die zur Berechnung dieses Werts notwendig sind, bereits als fehlerhaft gekennzeichnet
	 * sind, oder wenn die Berechnung aus anderen Gründen (z.B. Nenner = 0 in der Berechnungsformel)
	 * nicht möglich war.
	 */
	private boolean fehlerhaftBzwNichtErmittelbar = false;
	
	/**
	 * Daten sind fehlerhaft.
	 * Wird gesetzt, wenn die Daten als fehlerhaft erkannt wurden
	 */
	private boolean fehlerhaft = false;
	
	/**
	 * Daten sind nicht ermittelbar (ist KEIN Fehler). Wird gesetzt, wenn der entsprechende Wert
	 * nicht ermittelbar ist und kein Interpolation sinnvoll möglich ist (z.B. ist die
	 * Geschwindigkeit nicht ermittelbar, wenn kein Fahrzeug erfasst wurde).
	 **/
	private boolean nichtErmittelbar = false;
	
	/**
	 * die Datenart des Wertes
	 */
	private UmfeldDatenArt datenArt = null;
	
	/**
	 * wird von diesem Objekt auf <code>true</code> gesetzt, wenn eine der
	 * Setter-Methoden aufgerufen wurde
	 */
	private boolean veraendert = false;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param datenArt die Datenart des Wertes
	 */
	public UmfeldDatenSensorWert(final UmfeldDatenArt datenArt){
		this.datenArt = datenArt;
	}
	
	
	/**
	 * Erfragt die Skalierung dieses Wertes
	 * 
	 * @return die Skalierung dieses Wertes
	 */
	private final double getWertSkalierung(){
		double skalierung = 1;
		
		if(datenArt.equals(UmfeldDatenArt.TT1) ||
		   datenArt.equals(UmfeldDatenArt.TT2) ||
		   datenArt.equals(UmfeldDatenArt.TT3) ||
		   datenArt.equals(UmfeldDatenArt.TPT) ||
		   datenArt.equals(UmfeldDatenArt.LT) ||
		   datenArt.equals(UmfeldDatenArt.GT) ||
		   datenArt.equals(UmfeldDatenArt.FBT) ||
		   datenArt.equals(UmfeldDatenArt.NI) ||
		   datenArt.equals(UmfeldDatenArt.NM) ||
		   datenArt.equals(UmfeldDatenArt.WFD) ||
		   datenArt.equals(UmfeldDatenArt.WGM) ||
		   datenArt.equals(UmfeldDatenArt.WGS)){
			skalierung = 0.1;
		}
		
		return skalierung;
	}
	
	
	/**
	 * Erfragt den Offset für den Wertestatus dieses UF-Datums in Bezug auf
	 * die normalen Werte:<br>
	 * - <code>nicht ermittelbar = -1</code><br>
	 * - <code>fehlerhaft = -2</code>, oder<br>
	 * - <code>nicht ermittelbar/fehlerhaft = -3</code><br>
	 * 
	 * @return der Offset
	 */
	private final long getWertStatusOffset(){
		long offset = 0;
		
		if(datenArt.equals(UmfeldDatenArt.FBF) ||
		   datenArt.equals(UmfeldDatenArt.HK) ||
		   datenArt.equals(UmfeldDatenArt.NS) ||
		   datenArt.equals(UmfeldDatenArt.NI) ||
		   datenArt.equals(UmfeldDatenArt.NM) ||
		   datenArt.equals(UmfeldDatenArt.RLF) ||
		   datenArt.equals(UmfeldDatenArt.SH) ||
		   datenArt.equals(UmfeldDatenArt.SW) ||
		   datenArt.equals(UmfeldDatenArt.WFD) ||
		   datenArt.equals(UmfeldDatenArt.WR) ||
		   datenArt.equals(UmfeldDatenArt.FBZ) ||
		   datenArt.equals(UmfeldDatenArt.LD) ||
		   datenArt.equals(UmfeldDatenArt.RS) ||
		   datenArt.equals(UmfeldDatenArt.WGM) ||
		   datenArt.equals(UmfeldDatenArt.WGS)){
			offset = 0;
		}else
		if(datenArt.equals(UmfeldDatenArt.TT1) ||
		   datenArt.equals(UmfeldDatenArt.TT2) ||
		   datenArt.equals(UmfeldDatenArt.TT3) ||
		   datenArt.equals(UmfeldDatenArt.TPT) ||
		   datenArt.equals(UmfeldDatenArt.LT) ||
		   datenArt.equals(UmfeldDatenArt.GT) ||
		   datenArt.equals(UmfeldDatenArt.FBT)){
			offset = -1000;
		}else{
			throw new RuntimeException("Das Umfelddatum " + datenArt + //$NON-NLS-1$
					" kann nicht identifiziert werden"); //$NON-NLS-1$
		}
		
		return offset;
	}
	
	
	/**
	 * Zeigt an, ob nach dem letzten Aufruf von <code>setVeraendert(true)</code>
	 * eine Set-Methode aufgerufen wurde
	 * 
	 * @return ob nach dem letzten Aufruf von <code>setVeraendert(true)</code>
	 * eine Set-Methode aufgerufen wurde
	 */
	public final boolean isVeraendert() {
		return veraendert;
	}


	/**
	 * Setzt den Wert <code>veraendert</code>
	 * 
	 * @param veraendert der Wert <code>veraendert</code>
	 */
	public final void setVeraendert(boolean veraendert) {
		this.veraendert = veraendert;
	}


	/**
	 * Erfragt den Wert
	 * 
	 * @return wert der Wert
	 */
	public final long getWert() {
		return wert;
	}


	/**
	 * Setzt den Wert
	 * 
	 * @param wert festzulegender Wert
	 */
	public final void setWert(long wert) {
		this.veraendert = true;
		this.wert = wert;
		this.fehlerhaft = this.wert == getWertStatusOffset() + DUAKonstanten.FEHLERHAFT;
		this.fehlerhaftBzwNichtErmittelbar = this.wert == getWertStatusOffset() + DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT;
		this.nichtErmittelbar = this.wert == getWertStatusOffset() + DUAKonstanten.NICHT_ERMITTELBAR;
	}

	
	/**
	 * Setzt den Wert
	 * 
	 * @param wert festzulegender Wert
	 */
	public final void setSkaliertenWert(double wert) {
		double skalierung = this.getWertSkalierung();
		this.setWert((long)(wert / skalierung));
	}
	
	
	/**
	 * Erfragt den Wert
	 * 
	 * @return wert der Wert
	 */
	public final double getSkaliertenWert() {
		return this.getWert() * this.getWertSkalierung();
	}
	
	
	/**
	 * Setzt das Flag <code>fehlerhaft</code> an
	 */
	public final void setFehlerhaftAn(){
		this.setWert(getWertStatusOffset() + DUAKonstanten.FEHLERHAFT);
	}
	
	
	/**
	 * Erfragt, ob der Wert fehlerhaft ist
	 * 
	 * @return ob der Wert fehlerhaft ist
	 */
	public final boolean isFehlerhaft() {
		return this.fehlerhaft;
	}

	
	
	/**
	 * Setzt das Flag <code>nicht ermittelbar/fehlerhaft</code> an
	 */
	public final void setFehlerhaftBzwNichtErmittelbarAn(){
		this.setWert(getWertStatusOffset() + DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT);
	}


	/**
	 * Erfragt, ob der Wert als nicht ermittelbar gekennzeichnet ist,
	 * da bereits Basiswerte fehlerhaft sind

	 * @return ob der Wert als nicht ermittelbar gekennzeichnet ist,
	 * da bereits Basiswerte fehlerhaft sind
	 */
	public final boolean isFehlerhaftBzwNichtErmittelbar() {
		return this.fehlerhaftBzwNichtErmittelbar;
	}

	
	
	/**
	 * Setzt das Flag <code>nicht ermittelbar</code> an
	 */
	public final void setNichtErmittelbarAn(){
		this.setWert(getWertStatusOffset() + DUAKonstanten.NICHT_ERMITTELBAR);
	}
	
	
	/**
	 * Erfragt, ob der Wert nicht ermittelbar ist (ist KEIN Fehler).
	 * 
	 * @return ob der Wert nicht ermittelbar ist (ist KEIN Fehler).
	 */
	public final boolean isNichtErmittelbar() {
		return this.nichtErmittelbar;
	}

	
	/**
	 * Erfragt, ob dieser Wert wirkliche Daten enthält (im Gegensatz zu blos Statuswerten
	 * )
	 * @return ob dieser Wert wirkliche Daten enthält (im Gegensatz zu blos Statuswerten)
	 */
	public final boolean isOk(){
		return !(this.nichtErmittelbar || this.fehlerhaft || this.fehlerhaftBzwNichtErmittelbar);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String wertStr = new Long(this.wert).toString();
		
		if(this.isFehlerhaft()){
			wertStr = "fehlerhaft"; //$NON-NLS-1$
		}else
		if(this.isFehlerhaftBzwNichtErmittelbar()){
			wertStr = "nicht ermittelbar/fehlerhaft"; //$NON-NLS-1$
		}else
		if(this.isNichtErmittelbar()){
			wertStr = "nicht ermittelbar"; //$NON-NLS-1$
		}
		
		return wertStr;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean gleich = false;
		
		if(obj != null && obj instanceof UmfeldDatenSensorWert){
			UmfeldDatenSensorWert that = (UmfeldDatenSensorWert)obj;
			gleich = this.getWert() == that.getWert();
		}
		
		return gleich;
	}


	/**
	 * {@inheritDoc}
	 */
	public int compareTo(UmfeldDatenSensorWert that) {
		if(that == null){
			throw new NullPointerException("Vergleichswert ist <<null>>"); //$NON-NLS-1$
		}
		return new Long(this.getWert()).compareTo(that.getWert());
	}

}
