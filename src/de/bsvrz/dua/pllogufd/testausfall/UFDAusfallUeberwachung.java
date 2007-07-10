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
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.pllogufd.IKontrollProzessListener;
import de.bsvrz.dua.pllogufd.KontrollProzess;
import de.bsvrz.dua.pllogufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Das Modul Ausfall�berwachung meldet sich auf alle Parameter an und f�hrt
 * mit allen �ber die Methode aktualisiereDaten(ResultData[] arg0) �bergebenen Daten
 * eine Pr�fung durch. Die Pr�fung �berwacht, ob ein Messwert nach Ablauf des
 * daf�r vorgesehenen Intervalls �bertragen wurde. Der erwartete Meldungszeitpunkt
 * f�r einen zyklisch gelieferten Messwert ergibt sich aus dem Intervallbeginn 
 * zuz�glich der Erfassungsintervalldauer. Ein nicht �bertragener Messwert
 * wird intern als Datensatz mit dem erwarteten Intervallbeginn angelegt, 
 * wobei die Messwerte jeweils auf den Status Nicht erfasst gesetzt werden. 
 * Nach der Pr�fung werden die Daten dann an den n�chsten Bearbeitungsknoten
 * weitergereicht.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class UFDAusfallUeberwachung
extends AbstraktBearbeitungsKnotenAdapter 
implements IKontrollProzessListener<Collection<AusfallUFDSDatum>>,
		   ClientReceiverInterface{
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Eine Map mit allen aktuellen Kontrollzeitpunkten und den zu diesen
	 * Kontrollzeitpunkten zu �berpr�fenden Umfelddatensensoren
	 */
	private SortedMap<Long, Collection<AusfallUFDSDatum>> kontrollZeitpunkte =
					Collections.synchronizedSortedMap(new TreeMap<Long, Collection<AusfallUFDSDatum>>());
	
	/**
	 * Mapt alle betrachteten Umfelddatensensoren auf den aktuell f�r sie
	 * erlaubten maximalen Zeitverzug
	 */
	private Map<SystemObject, Long> sensorWertErfassungVerzug =
					Collections.synchronizedMap(new TreeMap<SystemObject, Long>());

	/**
	 * interner Kontrollprozess
	 */
	private KontrollProzess<Collection<AusfallUFDSDatum>> kontrollProzess = null;
	
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
		this.kontrollProzess = new KontrollProzess<Collection<AusfallUFDSDatum>>();
		this.kontrollProzess.addListener(this);
		
		DataDescription parameterBeschreibung = new DataDescription(
				dieVerwaltung.getVerbindung().getDataModel().getAttributeGroup("atg.ufdsAusfall�berwachung"), //$NON-NLS-1$
				dieVerwaltung.getVerbindung().getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_SOLL),
				(short)0);
		dieVerwaltung.getVerbindung().subscribeReceiver(this, dieVerwaltung.getSystemObjekte(),
				parameterBeschreibung, ReceiveOptions.normal(), ReceiverRole.receiver());
	}


	/**
	 * {@inheritDoc}
	 */
	public synchronized void aktualisiereDaten(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
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
						Long dummy = this.kontrollZeitpunkte.firstKey();
						if(dummy != null){
							fruehesterKontrollZeitpunkt = dummy.longValue();
						}

						if(fruehesterKontrollZeitpunkt > 0){								
							this.kontrollProzess.setNaechstenAufrufZeitpunkt(
									fruehesterKontrollZeitpunkt, this.kontrollZeitpunkte.get(new Long(fruehesterKontrollZeitpunkt)));
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
	 * Datums. Dabei wird zun�chst der momentan noch erwartete Kontrollzeitpunkt dieses
	 * Datums berechnet und dieser dann aus der Liste der Kontrollzeitpunkte entfernt
	 * 
	 * @param resultat ein Roh-Datum eines Umfelddatensensors
	 */
	@SuppressWarnings("null")
	private final void bereinigeKontrollZeitpunkte(final ResultData resultat){
		
		/**
		 * Berechne den wahrscheinlichsten Zeitpunkt, f�r den hier noch auf 
		 * ein Datum dieses Objektes gewartet wird 
		 */
		final long maxZeitVerzug = this.getMaxZeitVerzug(resultat.getObject());
		final UmfeldDatenSensorDatum rohWert = new UmfeldDatenSensorDatum(resultat);
		final Long letzterErwarteterZeitpunkt = resultat.getDataTime() + rohWert.getT() + maxZeitVerzug;
		
		Collection<AusfallUFDSDatum> kontrollObjekte = 
			this.kontrollZeitpunkte.get(letzterErwarteterZeitpunkt);

		AusfallUFDSDatum datum = new AusfallUFDSDatum(resultat);

		/**
		 * Gibt es einen Kontrollzeitpunkt, f�r den das Objekt, des empfangenen Datums
		 * eingeplant sein m�sste
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
				" Kontrollwarteschlange gel�scht werden"); //$NON-NLS-1$
			}
		}
	}
	
	
	/**
	 * Erfragt den maximalen Zeitverzug f�r einen Umfelddatensensor
	 * 
	 * @param obj ein Umfelddatensensor
	 * @return der maximale Zeitverzug f�r den Umfelddatensensor oder
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
	public void trigger(Collection<AusfallUFDSDatum> information) {
		ResultData[] zuSendendeAusfallDaten = null;
		
		synchronized (this) {
			List<ResultData> zuSendendeAusfallDatenMenge = new ArrayList<ResultData>();
			for(AusfallUFDSDatum sensorInformation:information){
				UmfeldDatenSensorDatum wert = new UmfeldDatenSensorDatum(sensorInformation.getDatum());
				wert.setStatusErfassungNichtErfasst(DUAKonstanten.JA);
				wert.getWert().setNichtErmittelbarAn();

				long zeitStempel = wert.getDatenZeit() + wert.getT();

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
	 * Datensatz assoziiert ist, ein neuer Datensatz (sp�testens) erwartet wird
	 * 
	 * @param empfangenesResultat ein empfangener Datensatz  
	 * @return der sp�teste Zeitpunkt des n�chsten Datensatzes oder -1, 
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
