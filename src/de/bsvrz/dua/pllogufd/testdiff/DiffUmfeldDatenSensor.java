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

package de.bsvrz.dua.pllogufd.testdiff;

import java.util.Collection;
import java.util.TreeSet;

import stauma.dav.clientside.Data;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.AttributeGroup;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.AbstraktUmfeldDatenSensor;
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
	 * L�nge des Pr�fixes innnerhalb der Typ-Pid bis zum
	 * eigentlichen Namen des Umfelddatensensors
	 */
	private static final int PRAEFIX_LEN = "typ.ufds".length(); //$NON-NLS-1$
	
	/**
	 * aktueller Wert mit Historie
	 */
	private VariableMitKonstanzZaehler<Long> wert = null;
	
	/**
	 * aktuelle Parameter f�r die Differenzialkontrolle dieses Umfelddatensensors
	 */
	private UniversalAtgUfdsDifferenzialKontrolle parameter = null;

	
	/**
	 * {@inheritDoc}
	 */
	protected DiffUmfeldDatenSensor(IVerwaltung verwaltung, SystemObject obj)
	throws DUAInitialisierungsException{
		super(verwaltung, obj);
		final String ufdTyp = this.objekt.getType().getPid().substring(PRAEFIX_LEN);
		this.wert = new VariableMitKonstanzZaehler<Long>(ufdTyp);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<AttributeGroup> getParameterAtgs()
	throws DUAInitialisierungsException{
		if(this.objekt == null){
			throw new NullPointerException("Parameter k�nnen nicht bestimmt werden," + //$NON-NLS-1$
					" da noch kein Objekt festgelegt ist"); //$NON-NLS-1$
		}

		Collection<AttributeGroup> parameterAtgs = new TreeSet<AttributeGroup>();
		
		final String ufdTyp = this.objekt.getType().getPid().substring(PRAEFIX_LEN);
		final String atgPid = "atg.ufdsDifferenzialKontrolle" + ufdTyp; //$NON-NLS-1$
		AttributeGroup atg = VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup(atgPid);

		if(atg != null){
			parameterAtgs.add(atg);
		}else{
			throw new DUAInitialisierungsException("Es konnte keine Parameter-Attributgruppe f�r die " + //$NON-NLS-1$
					"Differenzialkontrolle des Objektes " + this.objekt + " bestimmt werden\n" +  //$NON-NLS-1$//$NON-NLS-2$ 
					"Atg-Name: " + atgPid);  //$NON-NLS-1$
		}
		
		return parameterAtgs;
	}
	
	
	/**
	 * F�r die empfangenen Daten wird gepr�ft, ob innerhalb eines definierenten
	 * Zeitraums (parametrierbares Zeitfenster) eine �nderung des Messwerts vorliegt.
	 * Liegt eine Ergebniskonstanz in diesem Zeitfenster vor, so erfolgt eine
	 * Kennzeichnung der Werte als Implausibel und Fehlerhaft. 
	 * 
	 * @param originalDatum ein Roh-Datum eines Umfelddatensensors
	 * @return das gekennzeichnete Datum oder <code>null</code> wenn das Datum plausibel ist
	 */
	public final Data plausibilisiere(final ResultData resultat){
		Data copy = null;

		if(resultat != null && resultat.getData() != null){
			Data data = resultat.getData();

			if(this.parameter != null){
				final long aktuellerWert = data.getItem(this.wert.getName()).getUnscaledValue("Wert").longValue(); //$NON-NLS-1$
				final long T = data.getTimeValue("T").getMillis(); //$NON-NLS-1$
				this.wert.aktualisiere(aktuellerWert, T);

				synchronized (this.parameter) {
					boolean vergleichDurchfuehren = false;
					if(this.parameter.getOpertator() != null){
						vergleichDurchfuehren = this.parameter.getOpertator().vergleiche(
								aktuellerWert, this.parameter.getGrenz());
					}else{
						vergleichDurchfuehren = aktuellerWert <= this.parameter.getGrenz();
					}

					if(vergleichDurchfuehren){
						if(this.wert.getWertIstKonstantSeit() > this.parameter.getMaxZeit()){
							copy = VERWALTUNG.getVerbindung().createData(resultat.getDataDescription().getAttributeGroup());
							data.getItem(wert.getName()).getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$			
							data.getItem(wert.getName()).getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$
							getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$									
						}
					}
				}
			}else{
				LOGGER.warning("Fuer Umfelddatensensor " + this +  //$NON-NLS-1$
				" wurden noch keine Parameter f�r die Differenzialkontrolle empfangen"); //$NON-NLS-1$
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