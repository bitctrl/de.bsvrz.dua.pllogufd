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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.KontrollProzess;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IKontrollProzessListener;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
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
implements IKontrollProzessListener<Long>,
		   ClientReceiverInterface{
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Eine Map mit allen aktuellen Kontrollzeitpunkten und den zu diesen
	 * Kontrollzeitpunkten zu überprüfenden Umfelddatensensoren
	 */
	private SortedMap<Long, Collection<AusfallUFDSDatum>> kontrollZeitpunkte =
					Collections.synchronizedSortedMap(new TreeMap<Long, Collection<AusfallUFDSDatum>>());
	
	/**
	 * Mapt alle betrachteten Umfelddatensensoren auf den aktuell für sie
	 * erlaubten maximalen Zeitverzug
	 */
	private Map<SystemObject, Long> sensorWertErfassungVerzug =
					Collections.synchronizedMap(new TreeMap<SystemObject, Long>());

	/**
	 * interner Kontrollprozess
	 */
	private KontrollProzess<Long> kontrollProzess = null;
	
	/**
	 * speichert pro Umfelddatensensor die letzte empfangene Datenzeit
	 */
	private Map<SystemObject, Long> letzteEmpfangeneDatenZeitProSensor = new HashMap<SystemObject, Long>(); 
	
	
	
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
		this.kontrollProzess = new KontrollProzess<Long>();
		this.kontrollProzess.addListener(this);
		
		DataDescription parameterBeschreibung = new DataDescription(
				dieVerwaltung.getVerbindung().getDataModel().getAttributeGroup("atg.ufdsAusfallÜberwachung"), //$NON-NLS-1$
				dieVerwaltung.getVerbindung().getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_SOLL),
				(short)0);
		dieVerwaltung.getVerbindung().subscribeReceiver(this, dieVerwaltung.getSystemObjekte(),
				parameterBeschreibung, ReceiveOptions.normal(), ReceiverRole.receiver());
		
		for(SystemObject sensor:verwaltung.getSystemObjekte()){
			this.letzteEmpfangeneDatenZeitProSensor.put(sensor, new Long(-1));
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public synchronized void aktualisiereDaten(ResultData[] resultate) {
		if(resultate != null){
			List<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();
			
			for(ResultData resultat:resultate){
				if(resultat != null){
					
					/**
					 * Hier werden die Daten herausgefiltert, die von der Ausfallkontrolle
					 * quasi zu unrecht generiert wurden, da das Datum nur minimal zu spät kam.
					 */
					if(this.letzteEmpfangeneDatenZeitProSensor.get(resultat.getObject()) < resultat.getDataTime()){
						/**
						 * Zeitstempel ist echt neu!
						 */
						weiterzuleitendeResultate.add(resultat);
					}
					this.letzteEmpfangeneDatenZeitProSensor.put(resultat.getObject(), resultat.getDataTime());
					
					
					if(resultat.getData() != null){
											
						this.bereinigeKontrollZeitpunkte(resultat);
						
						long kontrollZeitpunkt = this.getKontrollZeitpunktVon(resultat);
						if(kontrollZeitpunkt > 0){
							Collection<AusfallUFDSDatum> kontrollObjekte =
									this.kontrollZeitpunkte.get(kontrollZeitpunkt);
								
							/**
							 * Kontrolldatum bestimmten
							 */
							AusfallUFDSDatum neuesKontrollObjekt = new AusfallUFDSDatum(resultat);							
							if(kontrollObjekte != null){
								kontrollObjekte.add(neuesKontrollObjekt);
							}else{
								kontrollObjekte = new TreeSet<AusfallUFDSDatum>();
								kontrollObjekte.add(neuesKontrollObjekt);
								this.kontrollZeitpunkte.put(new Long(kontrollZeitpunkt), kontrollObjekte);
							}
						}
	
						long fruehesterKontrollZeitpunkt = -1;
												
						if(!this.kontrollZeitpunkte.isEmpty()){
							fruehesterKontrollZeitpunkt = this.kontrollZeitpunkte.firstKey().longValue();
							
							if(fruehesterKontrollZeitpunkt > 0){
								this.kontrollProzess.setNaechstenAufrufZeitpunkt(fruehesterKontrollZeitpunkt,
																				 new Long(fruehesterKontrollZeitpunkt));
							}else{
								LOGGER.warning("Der momentan aktuellste Kontrollzeitpunkt ist <= 0"); //$NON-NLS-1$
							}
						}else{
							LOGGER.warning("Die Menge der Kontrollzeitpunkte ist leer"); //$NON-NLS-1$
						}
					}
				}
			}
			
			if(this.knoten != null && !weiterzuleitendeResultate.isEmpty()){
				this.knoten.aktualisiereDaten(weiterzuleitendeResultate.toArray(new ResultData[0]));
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
	@SuppressWarnings("null")
	private final void bereinigeKontrollZeitpunkte(final ResultData resultat){
		
		/**
		 * Berechne den wahrscheinlichsten Zeitpunkt, für den hier noch auf 
		 * ein Datum dieses Objektes gewartet wird 
		 */
		final Long letzterErwarteterZeitpunkt = resultat.getDataTime() + 
												new UmfeldDatenSensorDatum(resultat).getT() +
												this.getMaxZeitVerzug(resultat.getObject());
		
		Collection<AusfallUFDSDatum> kontrollObjekte = 
			this.kontrollZeitpunkte.get(letzterErwarteterZeitpunkt);
		
		AusfallUFDSDatum datum = new AusfallUFDSDatum(resultat);

		/**
		 * Gibt es einen Kontrollzeitpunkt, für den das Objekt, des empfangenen Datums
		 * eingeplant sein müsste
		 */
		if(kontrollObjekte != null){
			if(kontrollObjekte.remove(datum)){
				if(kontrollObjekte.isEmpty()){
					this.kontrollZeitpunkte.remove(letzterErwarteterZeitpunkt);
				}
			}else{
				kontrollObjekte = null;
			}
		}

		if(kontrollObjekte == null){
			Long gefundenInKontrollZeitpunkt = new Long(-1);
			for(Long kontrollZeitpunkt:this.kontrollZeitpunkte.keySet()){
				kontrollObjekte = this.kontrollZeitpunkte.get(kontrollZeitpunkt);
				if(kontrollObjekte.remove(datum)){
					gefundenInKontrollZeitpunkt = kontrollZeitpunkt;
					break;
				}
			}

			if(gefundenInKontrollZeitpunkt >= 0){
				if(kontrollObjekte.isEmpty()){
					this.kontrollZeitpunkte.remove(gefundenInKontrollZeitpunkt);
				}					
			}else{
				LOGGER.warning("Datum " + datum + " konnte nicht aus" + //$NON-NLS-1$ //$NON-NLS-2$
				" Kontrollwarteschlange gelöscht werden"); //$NON-NLS-1$
			}
		}
	}
	
	
	/**
	 * Erfragt den maximalen Zeitverzug für einen Umfelddatensensor
	 * 
	 * @param obj ein Umfelddatensensor
	 * @return der maximale Zeitverzug für den Umfelddatensensor oder
	 * -1, wenn dieser nicht ermittelt werden konnte
	 */
	private final long getMaxZeitVerzug(final SystemObject obj){
		long maxZeitVerzug = -1;

		if(obj != null){
			synchronized (this.sensorWertErfassungVerzug) {
				Long dummy = this.sensorWertErfassungVerzug.get(obj);
				if(dummy != null && dummy > 0){
					maxZeitVerzug = dummy;
				}
			}
		}
		
		return maxZeitVerzug;
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void trigger(Long kontrollZeitpunkt) {
		List<ResultData> zuSendendeAusfallDatenMenge = new ArrayList<ResultData>();
		
		synchronized (this.kontrollZeitpunkte) {
			Collection<AusfallUFDSDatum> ausfallDaten = this.kontrollZeitpunkte.get(kontrollZeitpunkt);
			if(ausfallDaten != null){
				for(AusfallUFDSDatum ausfallDatum:ausfallDaten){				
					UmfeldDatenSensorDatum wert = new UmfeldDatenSensorDatum(ausfallDatum.getDatum());
					wert.setStatusErfassungNichtErfasst(DUAKonstanten.JA);
					wert.getWert().setNichtErmittelbarAn();

					long zeitStempel = wert.getDatenZeit() + wert.getT();

					ResultData resultat = new ResultData(ausfallDatum.getDatum().getObject(), 
							ausfallDatum.getDatum().getDataDescription(), zeitStempel, wert.getDatum());

					zuSendendeAusfallDatenMenge.add(resultat);
				}				
			}else{
				LOGGER.warning("Der Kontrollzeitpunkt " +  //$NON-NLS-1$
						DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(kontrollZeitpunkt)) + 
						" wurde inzwischen entfernt"); //$NON-NLS-1$
			}
		}
		
		/**
		 * Sende die ausgefallenen Daten an mich selbst, um
		 * die Liste der Ausfallzeitpunkte zu aktualisieren
		 */
		if(zuSendendeAusfallDatenMenge.size() > 0){
			this.aktualisiereDaten(zuSendendeAusfallDatenMenge.toArray(new ResultData[0]));
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

		long maxZeitVerzug = this.getMaxZeitVerzug(empfangenesResultat.getObject());

		if(maxZeitVerzug >= 0){
			UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(empfangenesResultat);
			kontrollZeitpunkt = empfangenesResultat.getDataTime() + 2 * datum.getT() + maxZeitVerzug;
		}else{
			LOGGER.warning("Es wurden noch keine (sinnvollen) Parameter empfangen: " //$NON-NLS-1$ 
					+ empfangenesResultat.getObject());
		}

		return kontrollZeitpunkt;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
					synchronized (this.sensorWertErfassungVerzug) {
						this.sensorWertErfassungVerzug.put(resultat.getObject(), 
								new Long(resultat.getData().getTimeValue("maxZeitVerzug").getMillis())); //$NON-NLS-1$
						LOGGER.info("Neue Parameter: maxZeitVerzug(" + resultat.getObject() + ") = " +  //$NON-NLS-1$ //$NON-NLS-2$
								resultat.getData().getTimeValue("maxZeitVerzug").getMillis() + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}
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
