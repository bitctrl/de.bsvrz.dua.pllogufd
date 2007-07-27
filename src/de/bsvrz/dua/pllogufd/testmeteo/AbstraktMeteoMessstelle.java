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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.pllogufd.UmfeldDatenSensorDatum;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
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
	protected Collection<SystemObject> sensorenAnMessStelle = new HashSet<SystemObject>();
	
	/**
	 * der Zeitstempel aller im Moment gespeicherten Werte
	 */
	protected long aktuellerZeitstempel = -1;
	
	
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
	 * Erfragt die Menge der in dieser Umfelddatenmessstelle verarbeiteten 
	 * Datenarten
	 * 
	 * @return die Menge der in dieser Umfelddatenmessstelle verarbeiteten 
	 * Datenarten
	 */
	protected abstract Collection<UmfeldDatenArt> getDatenArten();
	
	
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
	 * @return ob das Umfelddatum in seine Member-Variable gespeichert werden konnte<br> 
	 * <b>Ein Datum kann nicht gespeichert werden, wenn sein Zeitstempel vom Zeitstempel aller 
	 * anderen gespeicherten Werte differiert.</b>
	 */
	protected abstract boolean bringeDatumInPosition(final ResultData umfeldDatum);
	
	
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
	 * Initialisiert eine Messstelle diesen Typs (Parameteranmeldungen usw.)
	 * 
	 * @throws DUAInitialisierungsException wenn die Initialisierung fehlgeschlagen ist 
	 */
	protected abstract void initialisiereMessStelle()
	throws DUAInitialisierungsException;
	
	
	/**
	 * Erfragt, ob für einen bestimmten Umfelddatensensor bereits ein Datum im lokalen 
	 * Puffer steht und gibt dieses zurück. Dabei wird nicht überprüft, ob das eingetroffene
	 * Datum überhaupt Daten enthält.
	 * 
	 * @param umfeldDatum ein Datum eines bestimmten Umfelddatensensors
	 * @return das für einen bestimmten Umfelddatensensor bereits im lokalen 
	 * Puffer stehende Datum oder <code>null</code> wenn noch keins im Puffer steht
	 */
	protected abstract UmfeldDatenSensorDatum getDatumBereitsInPosition(final ResultData umfeldDatum);

	
	/**
	 * 
	 * @return
	 */
	protected abstract boolean isPufferLeer();
	
	
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
			synchronized (this) {
				
				/** Debug **/
				String zusatzInfo = this.getClass().getSimpleName() + ", Zur Zeit gespeichert: "; //$NON-NLS-1$
				for(ResultData resu:this.getAlleAktuellenWerte())zusatzInfo += "\n" + resu; //$NON-NLS-1$
				LOGGER.info(zusatzInfo);
				
				LOGGER.info(this.getClass().getSimpleName() + " IN: " + umfeldDatum.getObject() + ", " +  //$NON-NLS-1$ //$NON-NLS-2$
						DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(umfeldDatum.getDataTime())) + "\n" + umfeldDatum); //$NON-NLS-1$
				/** Debug **/
				
				if(this.isDatenArtRelevantFuerSubModul(umfeldDatum)){
					if(umfeldDatum.getData() == null){
						
						/**
						 * Keine Daten oder keine Quelle heißt hier: Mache FLUSH
						 * und leitet den übergebenen Datensatz sofort weiter
						 */
						Collection<ResultData> ergebnisListe = new ArrayList<ResultData>();
						for(ResultData berechnungsErgebnis:this.berechneAlleRegeln())ergebnisListe.add(berechnungsErgebnis);
						ergebnisListe.add(umfeldDatum);
						ergebnisse = ergebnisListe.toArray(new ResultData[0]);						
						this.loescheAlleWerte();
						
					}else{
						if(this.isNeuesIntervall(umfeldDatum)){
							
							LOGGER.info("------------- Neues Intervall --------------");
							
							ergebnisse = this.berechneAlleRegeln();
							this.loescheAlleWerte();
							if(!this.bringeDatumInPosition(umfeldDatum)){
								throw new RuntimeException("Hallo"); //$NON-NLS-1$
							}
							
						}else{							
							/**
							 * Es kann hier davon ausgegangen werden, dass noch nicht alle 
							 * Daten für das aktuelle Intevall da sind. Es fehlt mindestens
							 * noch das gerade angekommene Datum.
							 */
							if(this.bringeDatumInPosition(umfeldDatum)){							
								if(this.sindAlleWerteFuerIntervallDa()){
									ergebnisse = this.berechneAlleRegeln();
									this.loescheAlleWerte();
									LOGGER.info("------------- alle Werte berechnet --------------");
								}else{
									LOGGER.info("------------- noch nicht alle Da --------------");
								}
							}else{
								/**
								 * Datum konnte nicht in Position gebracht werden
								 */
								ergebnisse = new ResultData[]{ umfeldDatum };
								throw new RuntimeException("Hallo2");
							}
						}
					}
				}else{
					/**
					 * Das Datum interessiert hier nicht und wird 
					 * direkt zurückgegeben
					 */
					ergebnisse = new ResultData[]{ umfeldDatum }; 
				}
				
				
				/** Debug **/
				zusatzInfo = this.getClass().getSimpleName() + ", Jetzt gespeichert: "; //$NON-NLS-1$
				for(ResultData resu:this.getAlleAktuellenWerte())zusatzInfo += "\n" + resu; //$NON-NLS-1$
				LOGGER.info(zusatzInfo);
				
				String log = this.getClass().getSimpleName() + " OUT: "; //$NON-NLS-1$
				if(ergebnisse != null && ergebnisse.length != 0){
					for(ResultData ergebnis:ergebnisse){
						log += "\n  " + ergebnis.getObject() + ", " +  //$NON-NLS-1$ //$NON-NLS-2$
						DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(ergebnis.getDataTime()));
					}
					log += "\n"; //$NON-NLS-1$
					for(ResultData ergebnis:ergebnisse){
						log += "\n  " + ergebnis; //$NON-NLS-1$
					}
				}else{
					log += "nichts"; //$NON-NLS-1$
				}
				LOGGER.info(log);
				/** Debug **/
				
			}
		}
		
		return ergebnisse;
	}
	
	
	/**
	 * Erfragt, ob das empfangene Umfelddatum zu einem neuen Intervall
	 * gehört.<br>
	 * Dies ist der Fall, wenn der Zeitstempel des gerade empfangenen
	 * Umfelddatums echt größer als <code>aktuellerZeitstempel</code> 
	 * ist und in der für das Datum vorgesehenen Member-Variable bereits
	 * ein Datum steht
	 * 
	 * @param umfeldDatum ein Umfelddatum (muss <code>!= null</code> sein)
	 * @return ob das empfangene Umfelddatum zu einem neuen Intervall
	 * gehört
	 */
	private final boolean isNeuesIntervall(ResultData umfeldDatum){
		return this.getDatumBereitsInPosition(umfeldDatum) != null && 
			   this.aktuellerZeitstempel <= umfeldDatum.getDataTime();
	}	
	
	
	/**
	 * Erfragt, ob ein übergebenes Umfelddatum in diesem Modul speicherbar ist.<br>
	 * Ein Datum ist dann speicherbar, wenn <code>aktuellerZeitstempel == -1</code>
	 * oder <code>aktuellerZeitstempel <= umfeldDatum.getDataTime()</code> oder
	 * der lokale Speicher an sich leer ist
	 * 
	 * @param umfeldDatum ein Umfelddatum (muss <code>!= null</code> sein)
	 * @return ob ein übergebenes Umfelddatum in diesem Modul speicherbar ist
	 */
	protected final boolean isDatumSpeicherbar(ResultData umfeldDatum){
		return this.aktuellerZeitstempel == -1 ||
			   this.aktuellerZeitstempel == umfeldDatum.getDataTime() ||
			   this.isPufferLeer();
	}
	
	
	/**
	 * Erfragt, ob ein Umfelddatum in diesem Submodul innerhalb der Meteorologischen
	 * Kontrolle verarbeitet wird (also insbesondere, ob es hier zwischengespeichert
	 * werden muss)
	 *  
	 * @param umfeldDatum ein Umfelddatum
	 * @return ob das Umfelddatum in diesem Submodul verarbeitet wird
	 */
	private final boolean isDatenArtRelevantFuerSubModul(final ResultData umfeldDatum){
		boolean relevant = false;
		
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(umfeldDatum.getObject());
		if(datenArt != null){
			relevant = this.getDatenArten().contains(datenArt);
		}else{
			LOGGER.error("Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
		}
		
		return relevant;
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
