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
import java.util.TreeSet;

import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;

/**
 * Abstrakte Klasse für Umfelddatenmessstellen, für die eine meteorologische
 * Kontrolle durchgeführt werden soll.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public abstract class AbstraktMeteoMessstelle 
extends AbstractSystemObjekt{
	
	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();

	/**
	 * Verbindung zum Verwaltungsmodul
	 */
	protected static IVerwaltung VERWALTUNG = null;
	
	/**
	 * die Systemobjekte aller Umfelddatensensoren, die an dieser Messstelle
	 * konfiguriert sind (und in diesem Submodul betrachtet werden)
	 */
	protected Collection<SystemObject> sensorenAnMessStelle = new TreeSet<SystemObject>();
	
	
	/**
	 * {@inheritDoc}
	 */
	protected AbstraktMeteoMessstelle(SystemObject obj) {
		super(obj);
	}

	
	/**
	 * Setzt die Verbindung zum Verwaltungsmodul
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 */
	protected static final void setVerwaltungsModul(final IVerwaltung verwaltung){
		if(VERWALTUNG == null){
			VERWALTUNG = verwaltung;
		}
	}
	
	
	/**
	 * Erfragt, ob ein Umfelddatum in diesem Submodul innerhalb der Meteorologischen
	 * Kontrolle verarbeitet wird (also insbesondere, ob es hier zwischengespeichert
	 * werden muss)
	 *  
	 * @param umfeldDatum ein Umfelddatum
	 * @return ob das Umfelddatum in diesem Submodul verarbeitet wird
	 */
	protected abstract boolean isDatenArtRelevantFuerSubModul(final ResultData umfeldDatum);
	
	
	/**
	 * Arbeitet <b>alle</b> Regeln der Reihe nach ab, so die Voraussetzungen zur
	 * Abarbeitung gegeben sind. Die Ergebnisse überschreiben die Variablen mit
	 * den originalen Werten (lokaler Puffer).
	 * 
	 * @return das Ergebnis des Aufrufs der Methode <code>getAlleAktuellenWerte()</code>
	 */
	protected abstract ResultData[] berechneAlleRegeln();
	
	
	/**
	 * Erfragt, ob alle Werte, die zur Abarbeitung <b>aller</b> Regeln dieses Submoduls
	 * notwendig sind vorliegen <b>und alle</b> für das selbe Intervall gelten.
	 *  
	 * @return ob <b>alle</b> Werte für <b>ein</b> Intervall vorliegen
	 */
	protected abstract boolean sindAlleWerteFuerIntervallDa();
	
	
	/**
	 * Schreibt ein angekommenes Datum in die Member-Variable in die es gehört
	 * 
	 * @param umfeldDatum ein Umfelddatum
	 */
	protected abstract void bringeDatumInPosition(final ResultData umfeldDatum);
	
	
	/**
	 * Löscht alle Member-Variablen mit gespeicherten Umfelddaten im lokalen Puffer
	 */
	protected abstract void loescheAlleWerte();
	
	
	/**
	 * Erfragt alle im Moment gespeicherten Member-Variablen als <code>ResultData</code>-Objekte
	 * 
	 * @return alle im Moment gespeicherten Member-Variablen als <code>ResultData</code>-Objekte
	 */
	protected abstract ResultData[] getAlleAktuellenWerte();
	
	
	/**
	 * Erfragt, ob für einen bestimmten Umfelddatensensor bereits ein Datum im lokalen 
	 * Puffer steht 
	 * 
	 * @param umfeldDatum ein Datum eines bestimmten Umfelddatensensors
	 * @return ob für einen bestimmten Umfelddatensensor bereits ein Datum im lokalen 
	 * Puffer steht
	 */
	protected abstract boolean isBereitsDatumInPosition(final ResultData umfeldDatum);
	
	
	/**
	 * Initialisiert eine Messstelle diesen Typs (Parameteranmeldungen usw.)
	 * 
	 * @throws DUAInitialisierungsException wenn die Initialisierung fehlgeschlagen ist 
	 */
	protected abstract void initialisiereMessStelle()
	throws DUAInitialisierungsException;
	
	
	/**
	 * Aktualisiert diese Messstelle der meteorologischen Kontrolle mit einem neuen
	 * Umfelddatum.  
	 * 
	 * @param umfeldDatum ein aktuelles Umfelddatum
	 * @return die Ergebnisse der Überprüfung bzw. <code>null</code>, wenn das übergebene 
	 * Umfelddatum nicht zur Berechnung von Werten geführt hat
	 */
	public final ResultData[] aktualisiereDaten(final ResultData umfeldDatum){
		ResultData[] ergebnisse = null;
		
		if(umfeldDatum != null){
			if(this.isDatenArtRelevantFuerSubModul(umfeldDatum)){
				if(this.isBereitsDatumInPosition(umfeldDatum)){
					/**
					 * Es kann hier davon ausgegangen werden, dass das Intervall,
					 * für das noch Daten im Lokalen Puffer stehen abgelaufen ist
					 */
					ergebnisse = this.berechneAlleRegeln();
					this.loescheAlleWerte();
					this.bringeDatumInPosition(umfeldDatum);
				}else{
					/**
					 * Es kann hier davon ausgegangen werden, dass noch nicht alle 
					 * Daten für das aktuelle Intevall da sind.
					 */
					this.bringeDatumInPosition(umfeldDatum);
					if(this.sindAlleWerteFuerIntervallDa()){
						ergebnisse = this.berechneAlleRegeln();
						this.loescheAlleWerte();
					}
				}
			}else{
				/**
				 * Das Datum interessiert hier nicht und wird 
				 * direkt zurückgegeben
				 */
				ergebnisse = new ResultData[]{ umfeldDatum }; 
			}
		}
		
		return ergebnisse;
	}
		
	
	/**
	 * Erfragt die Systemobjekte aller Umfelddatensensoren, die an dieser Messstelle
	 * konfiguriert sind (und in diesem Submodul betrachtet werden)
	 * 
	 * @return eine Menge von Umfelddatensensoren (Systemobjekte)
	 */
	public final Collection<SystemObject> getSensoren(){
		return this.sensorenAnMessStelle;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public SystemObjektTyp getTyp() {
		return new SystemObjektTyp(){

			public Class<? extends SystemObjekt> getKlasse() {
				return AbstraktMeteoMessstelle.class;
			}

			public String getPid() {
				return getSystemObject().getType().getPid();
			}
			
		};
	}
	
}
