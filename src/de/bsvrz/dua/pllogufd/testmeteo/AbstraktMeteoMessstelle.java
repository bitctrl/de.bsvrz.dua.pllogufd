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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.pllogufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;

/**
 * Abstrakte Klasse f�r Umfelddatenmessstellen, f�r die eine meteorologische
 * Kontrolle durchgef�hrt werden soll.
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
	protected Collection<SystemObject> sensorenAnMessStelle = new HashSet<SystemObject>();
	
	/**
	 * letzter Zeitstempel, f�r den Daten aus dieser Messstelle wieder freigegeben wurden
	 * d.h. insbesondere, der Zeitstempel, f�r den bereits versucht wurde alle Regeln
	 * abzuarbeiten
	 */
	protected long letzterBearbeiteterZeitStempel = -1;
	
	
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
	 * @return ob das Umfelddatum in diesem Submodul verarbeitet wird <b>und</b> ob das
	 * �bergebene Umfelddatum nicht schon vorher verarbeitet worden ist (Kontrolle
	 * des Zeitstempel, da die Ausfallkontrolle ein Datum geschickt haben kann, dass
	 * jetzt tats�chlich noch nachkommt)
	 */
	protected abstract boolean isDatenArtRelevantFuerSubModul(final ResultData umfeldDatum);
	
	
	/**
	 * Arbeitet <b>alle</b> Regeln der Reihe nach ab, so die Voraussetzungen zur
	 * Abarbeitung gegeben sind. Die Ergebnisse �berschreiben die Variablen mit
	 * den originalen Werten (lokaler Puffer).
	 * 
	 * @return das Ergebnis des Aufrufs der Methode <code>getAlleAktuellenWerte()</code>
	 */
	protected abstract ResultData[] berechneAlleRegeln();
	
	
	/**
	 * Erfragt, ob alle Werte, die zur Abarbeitung <b>aller</b> Regeln dieses Submoduls
	 * notwendig sind vorliegen <b>und alle</b> f�r das selbe Intervall gelten.
	 *  
	 * @return ob <b>alle</b> Werte f�r <b>ein</b> Intervall vorliegen
	 */
	protected abstract boolean sindAlleWerteFuerIntervallDa();
	
	
	/**
	 * Schreibt ein angekommenes Datum in die Member-Variable in die es geh�rt
	 * 
	 * @param umfeldDatum ein Umfelddatum
	 */
	protected abstract void bringeDatumInPosition(final ResultData umfeldDatum);
	
	
	/**
	 * L�scht alle Member-Variablen mit gespeicherten Umfelddaten im lokalen Puffer
	 */
	protected abstract void loescheAlleWerte();
	
	
	/**
	 * Erfragt alle im Moment gespeicherten Member-Variablen als <code>ResultData</code>-Objekte
	 * 
	 * @return alle im Moment gespeicherten Member-Variablen als <code>ResultData</code>-Objekte
	 */
	protected abstract ResultData[] getAlleAktuellenWerte();
	
	
	/**
	 * Erfragt, ob f�r einen bestimmten Umfelddatensensor bereits ein Datum im lokalen 
	 * Puffer steht und gibt dieses zur�ck. Dabei wird nicht �berpr�ft, ob das eingetroffene
	 * Datum �berhaupt Daten enth�lt.
	 * 
	 * @param umfeldDatum ein Datum eines bestimmten Umfelddatensensors
	 * @return das f�r einen bestimmten Umfelddatensensor bereits im lokalen 
	 * Puffer stehende Datum oder <code>null</code> wenn noch keins im Puffer steht
	 */
	protected abstract UmfeldDatenSensorDatum getDatumBereitsInPosition(final ResultData umfeldDatum);
	
	
	/**
	 * Initialisiert eine Messstelle diesen Typs (Parameteranmeldungen usw.)
	 * 
	 * @throws DUAInitialisierungsException wenn die Initialisierung fehlgeschlagen ist 
	 */
	protected abstract void initialisiereMessStelle()
	throws DUAInitialisierungsException;
	
	
	/**
	 * Setzt den letzten Zeitstempel, f�r den Daten aus dieser Messstelle wieder freigegeben
	 * wurden d.h. insbesondere, der Zeitstempel, f�r den bereits versucht wurde alle Regeln
	 * abzuarbeiten 
	 * 
	 * @param datum letzter Zeitstempel, f�r den Daten aus dieser Messstelle wieder 
	 * freigegeben wurden d.h. insbesondere, der Zeitstempel, f�r den bereits versucht
	 * wurde alle Regeln abzuarbeiten 
	 */
	protected final void setLetztenBerabeitetenZeitstempel(UmfeldDatenSensorDatum datum){
		if(datum != null){
			this.letzterBearbeiteterZeitStempel = datum.getDatenZeit();
		}
	}
	
	
	/**
	 * Aktualisiert diese Messstelle der meteorologischen Kontrolle mit einem neuen
	 * Umfelddatum.  
	 * 
	 * @param umfeldDatum ein aktuelles Umfelddatum
	 * @return die Ergebnisse der �berpr�fung bzw. <code>null</code>, wenn das �bergebene 
	 * Umfelddatum nicht zur Berechnung von Werten gef�hrt hat
	 */
	public final ResultData[] aktualisiereDaten(final ResultData umfeldDatum){
		ResultData[] ergebnisse = null;
		
		if(umfeldDatum != null){
			synchronized (this) {
				
				LOGGER.info(this.getClass().getSimpleName() + " IN: " + umfeldDatum.getObject() + ", " +  //$NON-NLS-1$ //$NON-NLS-2$
						DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(umfeldDatum.getDataTime())));
				
				if(this.isDatenArtRelevantFuerSubModul(umfeldDatum)){
					if(umfeldDatum.getData() == null){
						/**
						 * Keine Daten oder keine Quelle hei�t hier FLUSH
						 */
						Collection<ResultData> ergebnisListe = new ArrayList<ResultData>();
						for(ResultData berechnungsErgebnis:this.berechneAlleRegeln())ergebnisListe.add(berechnungsErgebnis);
						ergebnisListe.add(umfeldDatum);
						ergebnisse = ergebnisListe.toArray(new ResultData[0]);						
						this.loescheAlleWerte();
						this.bringeDatumInPosition(umfeldDatum);
					}else{
						UmfeldDatenSensorDatum datumInPosition = this.getDatumBereitsInPosition(umfeldDatum);
						if(datumInPosition != null){
							if(datumInPosition.getDatenZeit() != umfeldDatum.getDataTime()){					
								/**
								 * Es kann hier davon ausgegangen werden, dass das Intervall,
								 * f�r das noch Daten im Lokalen Puffer stehen abgelaufen ist,
								 * da ein neues Datum (oder keine Quelle, oder...) eingetroffen 
								 * ist
								 */
								ergebnisse = this.berechneAlleRegeln();
								this.loescheAlleWerte();
								this.bringeDatumInPosition(umfeldDatum);
							}else{
								/**
								 * Hier muss davon ausgegangen werden, dass die Ausfallkontrolle
								 * ein Datum erzeugt hat, dass dann doch noch gekommen ist, w�hrend
								 * aber noch nicht alle Daten zur Berechnung aller Regeln vorliegen
								 * Dieses Datum wird in das Modul eingepflegt und das bereits bestehende
								 * freigegeben
								 */
								ergebnisse = new ResultData[]{ datumInPosition.getOriginalDatum() };
								this.bringeDatumInPosition(umfeldDatum);
							}
						}else{
							/**
							 * Es kann hier davon ausgegangen werden, dass noch nicht alle 
							 * Daten f�r das aktuelle Intevall da sind.
							 */
							this.bringeDatumInPosition(umfeldDatum);
							if(this.sindAlleWerteFuerIntervallDa()){
								ergebnisse = this.berechneAlleRegeln();
								this.loescheAlleWerte();
							}
						}
					}
				}else{
					/**
					 * Das Datum interessiert hier nicht und wird 
					 * direkt zur�ckgegeben
					 */
					ergebnisse = new ResultData[]{ umfeldDatum }; 
				}
				
				String log = this.getClass().getSimpleName() + " OUT: "; //$NON-NLS-1$
				if(ergebnisse != null && ergebnisse.length != 0){
					for(ResultData ergebnis:ergebnisse){
						log += "\n  " + ergebnis.getObject() + ", " +  //$NON-NLS-1$ //$NON-NLS-2$
						DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(ergebnis.getDataTime()));
					}
				}else{
					log += "nichts"; //$NON-NLS-1$
				}
				LOGGER.info(log);
				
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
