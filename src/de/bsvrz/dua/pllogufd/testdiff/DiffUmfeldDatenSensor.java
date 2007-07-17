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

package de.bsvrz.dua.pllogufd.testdiff;

import java.util.Collection;
import java.util.HashSet;

import stauma.dav.clientside.Data;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.AttributeGroup;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.AbstraktUmfeldDatenSensor;
import de.bsvrz.dua.pllogufd.UmfeldDatenSensorDatum;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.VariableMitKonstanzZaehler;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Assoziiert einen Umfelddatensensor mit dessen Parametern und Werten
 * bzgl. der Differenzialkontrolle
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class DiffUmfeldDatenSensor
extends AbstraktUmfeldDatenSensor{
	
	/**
	 * aktueller Wert mit Historie
	 */
	private VariableMitKonstanzZaehler<Long> wert = null;
	
	/**
	 * aktuelle Parameter für die Differenzialkontrolle dieses Umfelddatensensors
	 */
	private UniversalAtgUfdsDifferenzialKontrolle parameter = null;

	
	/**
	 * {@inheritDoc}
	 */
	protected DiffUmfeldDatenSensor(IVerwaltung verwaltung, SystemObject obj)
	throws DUAInitialisierungsException{
		super(verwaltung, obj);
		this.wert = new VariableMitKonstanzZaehler<Long>(UmfeldDatenArt.getUmfeldDatenArtVon(obj).getName());
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<AttributeGroup> getParameterAtgs()
	throws DUAInitialisierungsException{
		if(this.objekt == null){
			throw new NullPointerException("Parameter können nicht bestimmt werden," + //$NON-NLS-1$
					" da noch kein Objekt festgelegt ist"); //$NON-NLS-1$
		}

		Collection<AttributeGroup> parameterAtgs = new HashSet<AttributeGroup>();
		
		final String atgPid = "atg.ufdsDifferenzialKontrolle" + UmfeldDatenArt. //$NON-NLS-1$
										getUmfeldDatenArtVon(this.objekt).getName();
		AttributeGroup atg = VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup(atgPid);

		if(atg != null){
			parameterAtgs.add(atg);
		}else{
			throw new DUAInitialisierungsException("Es konnte keine Parameter-Attributgruppe für die " + //$NON-NLS-1$
					"Differenzialkontrolle des Objektes " + this.objekt + " bestimmt werden\n" +  //$NON-NLS-1$//$NON-NLS-2$ 
					"Atg-Name: " + atgPid);  //$NON-NLS-1$
		}
		
		return parameterAtgs;
	}
	
	
	/**
	 * Für die empfangenen Daten wird geprüft, ob innerhalb eines definierenten
	 * Zeitraums (parametrierbares Zeitfenster) eine Änderung des Messwerts vorliegt.
	 * Liegt eine Ergebniskonstanz in diesem Zeitfenster vor, so erfolgt eine
	 * Kennzeichnung der Werte als Implausibel und Fehlerhaft. 
	 * 
	 * @param originalDatum ein Roh-Datum eines Umfelddatensensors
	 * @return das gekennzeichnete Datum oder <code>null</code> wenn das Datum plausibel ist
	 */
	public final Data plausibilisiere(final ResultData resultat){
		Data copy = null;

		if(resultat != null && resultat.getData() != null){
			if(this.parameter != null){
				
				UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(resultat); 
				final long aktuellerWert = datum.getWert().getWert();
				final long T = datum.getT();
				this.wert.aktualisiere(aktuellerWert, T);

				synchronized (this.parameter) {
					if(!this.parameter.getGrenz().isFehlerhaft() && 
					   !this.parameter.getGrenz().isFehlerhaftBzwNichtErmittelbar() &&
					   !this.parameter.getGrenz().isNichtErmittelbar()){
						boolean vergleichDurchfuehren = false;
						if(this.parameter.getOpertator() != null){
							vergleichDurchfuehren = this.parameter.getOpertator().vergleiche(
									aktuellerWert, this.parameter.getGrenz().getWert()) && datum.getWert().isOk();
							
						}else{
							vergleichDurchfuehren = aktuellerWert <= this.parameter.getGrenz().getWert();
						}
	
						if(vergleichDurchfuehren){
							if(this.wert.getWertIstKonstantSeit() > this.parameter.getMaxZeit()){
								datum.getWert().setFehlerhaftAn();
								datum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
								copy = datum.getDatum();
							}
						}
					}else{
						LOGGER.warning("Die Differenzialkontrolle für den Umfelddatensensor " + //$NON-NLS-1$
								this.objekt + " kann nicht durchgeführt werden, da der Parameter " + //$NON-NLS-1$
								UmfeldDatenArt.getUmfeldDatenArtVon(this.objekt).getAbkuerzung() + 
								"Grenz=" + this.parameter.getGrenz()); //$NON-NLS-1$
					}					
				}
			}else{
				LOGGER.warning("Fuer Umfelddatensensor " + this +  //$NON-NLS-1$
				" wurden noch keine Parameter für die Differenzialkontrolle empfangen"); //$NON-NLS-1$
			}
		}
		
		return copy;

	}

	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
					synchronized (this) {
						this.parameter = new UniversalAtgUfdsDifferenzialKontrolle(resultat);
					}
				}
			}
		}
	}

}
