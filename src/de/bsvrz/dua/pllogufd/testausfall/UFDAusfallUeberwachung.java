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

package de.bsvrz.dua.pllogufd.testausfall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.InvalidArgumentException;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.pllogufd.AtgUmfeldDatenSensorWert;
import de.bsvrz.dua.pllogufd.IKontrollProzessListener;
import de.bsvrz.dua.pllogufd.KontrollProzess;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Das Modul Ausfallüberwachung meldet sich auf alle Parameter an und führt
 * mit allen über die Methode aktualisiereDaten(ResultData[] arg0) übergebenen Daten
 * eine Prüfung durch. Die Prüfung überwacht, ob ein Messwert nach Ablauf des
 * dafür vorgesehenen Intervalls übertragen wurde. Der erwartete Meldungszeitpunkt
 * für einen zyklisch gelieferten Messwert ergibt sich aus dem Intervallbeginn 
 * zuzüglich der Erfassungsintervalldauer. Ein nicht übertragener Messwert
 * wird intern als Datensatz mit dem erwarteten Intervallbeginn angelegt, 
 * wobei die Messwerte jeweils auf den Status Nicht erfasst gesetzt werden. 
 * Nach der Prüfung werden die Daten dann an den nächsten Bearbeitungsknoten
 * weitergereicht.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class UFDAusfallUeberwachung
extends AbstraktBearbeitungsKnotenAdapter 
implements IKontrollProzessListener<Collection<UmfeldDatenSensorDatum>>,
		   ClientReceiverInterface{
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Eine Map mit allen aktuellen Kontrollzeitpunkten und den zu diesen
	 * Kontrollzeitpunkten zu überprüfenden Umfelddatensensoren
	 */
	private SortedMap<Long, Collection<UmfeldDatenSensorDatum>> kontrollZeitpunkte =
					Collections.synchronizedSortedMap(new TreeMap<Long, Collection<UmfeldDatenSensorDatum>>());
	
	/**
	 * Mapt alle betrachteten Umfelddatensensoren auf den aktuell für sie
	 * erlaubten maximalen Zeitverzug
	 */
	private Map<SystemObject, Long> sensorWertErfassungVerzug =
					Collections.synchronizedMap(new TreeMap<SystemObject, Long>());

	/**
	 * interner Kontrollprozess
	 */
	private KontrollProzess<Collection<UmfeldDatenSensorDatum>> kontrollProzess = null;
	
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung) throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
		this.kontrollProzess = new KontrollProzess<Collection<UmfeldDatenSensorDatum>>();
		this.kontrollProzess.addListener(this);
		
		/**
		 * solange es keine Parameter gibt
		 */
		for(SystemObject obj:dieVerwaltung.getSystemObjekte()){
			this.sensorWertErfassungVerzug.put(obj, new Long(1000L));
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
					long kontrollZeitpunkt = this.getKontrollZeitpunktVon(resultat);
					
					if(kontrollZeitpunkt > 0){
						synchronized (this.kontrollZeitpunkte) {
							Collection<UmfeldDatenSensorDatum> kontrollObjekte =
								this.kontrollZeitpunkte.get(kontrollZeitpunkte);
							
							/**
							 * Kontrolldatum bestimmten
							 */
							UmfeldDatenSensorDatum kontrollDatum = null;							
							if(kontrollObjekte != null){
								synchronized (kontrollObjekte) {
									kontrollObjekte.add(kontrollDatum);
								}
							}else{
								synchronized (this) {
									kontrollObjekte = new TreeSet<UmfeldDatenSensorDatum>();
									kontrollObjekte.add(kontrollDatum);
									this.kontrollZeitpunkte.put(new Long(kontrollZeitpunkt), kontrollObjekte);
								}
							}
						}							
					}
					
					this.bereinigeKontrollZeitpunkte(resultat);

					synchronized (this.kontrollZeitpunkte) {
						long fruehesterKontrollZeitpunkt = -1;

						Long dummy = this.kontrollZeitpunkte.firstKey();
						if(dummy != null){
							fruehesterKontrollZeitpunkt = dummy.longValue();
						}

						if(fruehesterKontrollZeitpunkt > 0){
							try {
								this.kontrollProzess.setNaechstenAufrufZeitpunkt(
										fruehesterKontrollZeitpunkt, this.kontrollZeitpunkte.get(new Long(fruehesterKontrollZeitpunkt)));
							} catch (InvalidArgumentException e) {
								LOGGER.error(Konstante.LEERSTRING, e);
							}	
						}
					}
				}
			}
			
			if(this.knoten != null){
				this.knoten.aktualisiereDaten(resultate);
			}
		}
	}
	
	
	/**
	 * Bereinigt die Liste der Kontrollzeitpunkte anhand des gerade eingetroffenen
	 * Datums. Dabei wird zunächst der momentan noch erwartete Kontrollzeitpunkt dieses
	 * Datums berechnet und dieser dann aus der Liste der Kontrollzeitpunkte entfernt
	 * 
	 * @param resultat ein Roh-Datum eines Umfelddatensensors
	 */
	private final void bereinigeKontrollZeitpunkte(final ResultData resultat){
		synchronized (this.kontrollZeitpunkte) {
			long maxZeitVerzug = this.getMaxZeitVerzug(resultat.getObject());
			long letzterErwarteterZeitpunkt = resultat.getDataTime() + maxZeitVerzug;

			Collection<UmfeldDatenSensorDatum> kontrollZeitpunkt = 
						this.kontrollZeitpunkte.get(new Long(letzterErwarteterZeitpunkt));
			
			UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(resultat);
			if(kontrollZeitpunkt == null || !kontrollZeitpunkt.remove(datum)){
				boolean gefunden = false;
				for(Collection<UmfeldDatenSensorDatum> kontrollZeitpunkt1:this.kontrollZeitpunkte.values()){
					gefunden = kontrollZeitpunkt1.remove(datum);
					if( gefunden ) break;
				}
				
				if(!gefunden){
					LOGGER.warning("Datum " + datum + " konnte nicht aus" + //$NON-NLS-1$ //$NON-NLS-2$
							" Kontrollwarteschlange gelöscht werden"); //$NON-NLS-1$
				}
			}
		}
	}
	
	
	/**
	 * Erfragt den maximalen Zeitverzug für einen Umfelddatensensor
	 * 
	 * @param obj ein Umfelddatensensor
	 * @return der maximale Zeitverzug für den Umfelddatensensor
	 */
	private final long getMaxZeitVerzug(final SystemObject obj){
		long maxZeitVerzug = -1;
		
		synchronized (this.sensorWertErfassungVerzug) {
			if(obj != null){
				Long dummy = this.sensorWertErfassungVerzug.get(obj);
				if(dummy != null && dummy >= 0){
					maxZeitVerzug = dummy;
				}
			}
		}
		
		return maxZeitVerzug;
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void trigger(Collection<UmfeldDatenSensorDatum> information) {
		ResultData[] zuSendendeAusfallDaten = null;
		
		synchronized (this) {
			List<ResultData> zuSendendeAusfallDatenMenge = new ArrayList<ResultData>();
			for(UmfeldDatenSensorDatum sensorInformation:information){
				AtgUmfeldDatenSensorWert wert = new AtgUmfeldDatenSensorWert(sensorInformation.getDatum());
				wert.setStatusErfassungNichtErfasst(DUAKonstanten.JA);
			
				long zeitStempel = sensorInformation.getDatum().getDataTime() + 
							sensorInformation.getDatum().getData().getTimeValue("T").getMillis(); //$NON-NLS-1$
				ResultData resultat = new ResultData(sensorInformation.getDatum().getObject(), 
						sensorInformation.getDatum().getDataDescription(), zeitStempel, wert.getDatum());
				zuSendendeAusfallDatenMenge.add(resultat);
			}						
			
			zuSendendeAusfallDaten = zuSendendeAusfallDatenMenge.toArray(new ResultData[0]);
		}
		
		/**
		 * Sende die ausgefallenen Daten an mich selbst, 
		 * um die Liste der Ausfallzeitpunkte zu aktualisieren
		 */
		if(zuSendendeAusfallDaten != null){
			this.aktualisiereDaten(zuSendendeAusfallDaten);
		}
	}

	
	/**
	 * Erfragt den Zeitpunkt, zu dem von dem Objekt, das mit diesem 
	 * Datensatz assoziiert ist, ein neuer Datensatz (spätestens) erwartet wird
	 * 
	 * @param empfangenesResultat ein empfangener Datensatz  
	 * @return der späteste Zeitpunkt des nächsten Datensatzes oder -1, 
	 * wenn dieser nicht sinnvoll bestimmt werden konnte (wenn z.B. keine
	 * Parameter vorliegen)
	 */
	private final long getKontrollZeitpunktVon(final ResultData empfangenesResultat){
		long kontrollZeitpunkt = -1;

		if(empfangenesResultat != null && empfangenesResultat.getData() != null){

			long maxZeitVerzug = this.getMaxZeitVerzug(empfangenesResultat.getObject());

			if(maxZeitVerzug >= 0){
				kontrollZeitpunkt = empfangenesResultat.getDataTime() + 
									2 * empfangenesResultat.getData().getTimeValue("T").getMillis() +  //$NON-NLS-1$
									maxZeitVerzug;
				if(kontrollZeitpunkt < System.currentTimeMillis() + 100){
					kontrollZeitpunkt = -1;
					LOGGER.error("Kontrollzeitpunkt liegt zu nah in der Zukunft:\n" + empfangenesResultat); //$NON-NLS-1$
				}
			}else{
				LOGGER.warning("Es wurden noch keine (sinnvollen) Parameter empfangen: " //$NON-NLS-1$ 
					+ empfangenesResultat.getObject());
			}
		}		

		return kontrollZeitpunkt;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] results) {
		// TODO: Parameter abholen
	}


	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return null;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// hier wird nicht publiziert
	}

}
