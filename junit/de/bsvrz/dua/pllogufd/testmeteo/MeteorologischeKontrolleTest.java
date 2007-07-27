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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.TestUtensilien;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Super-Klasse f�r alle Tests der Meteorologischen Kontrolle. Sendet Standardparameter
 * und meldet sich als Empf�nger auf alle Umfelddaten unter dem Aspekt
 * <code>asp.plausibilit�tsPr�fungLogisch</code> an
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MeteorologischeKontrolleTest 
implements ClientSenderInterface, ClientReceiverInterface{

	/**
	 * Debug-Ausgaben �ber Standardausgabe
	 */
	protected static final boolean DEBUG = false;

	/**
	 * Sollen die <code>Assert</code>-Statements ausgef�hrt werden?
	 */
	protected static final boolean TEST_AN = true;
	
	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Zum Ordnen der Systemobjekte nach ihrem Namen
	 */
	private static final Comparator<SystemObject> C = 
		new Comparator<SystemObject>(){
			public int compare(SystemObject so1, SystemObject so2) {
				return so1.getName().compareTo(so2.getName());
			}		
		};
	
	/**
	 * Datenverteiler-Verbindung
	 */
	protected ClientDavInterface dav = null;

	/**
	 * alle NS-Sensoren
	 */
	protected SortedSet<SystemObject> nsSensoren = new TreeSet<SystemObject>(C);

	/**
	 * alle NI-Sensoren
	 */
	protected SortedSet<SystemObject> niSensoren = new TreeSet<SystemObject>(C);

	/**
	 * alle LT-Sensoren
	 */
	protected SortedSet<SystemObject> ltSensoren = new TreeSet<SystemObject>(C);

	/**
	 * alle RLF-Sensoren
	 */
	protected SortedSet<SystemObject> rlfSensoren = new TreeSet<SystemObject>(C);

	/**
	 * alle WFD-Sensoren
	 */
	protected SortedSet<SystemObject> wfdSensoren = new TreeSet<SystemObject>(C);
	
	/**
	 * alle SW-Sensoren
	 */
	protected SortedSet<SystemObject> swSensoren = new TreeSet<SystemObject>(C);
	
	/**
	 * alle FBZ-Sensoren
	 */
	protected SortedSet<SystemObject> fbzSensoren = new TreeSet<SystemObject>(C);
	
	/**
	 * alle Sensoren, f�r die innerhalb dieses Tests nur Werte mit dem
	 * Status "fehlerhaft" gesendet werden m�ssen, um das Ergebnis nicht zu beeinflussen
	 */
	protected HashSet<SystemObject> restSensoren = new HashSet<SystemObject>();
	
	/**
	 * letzter Ist-Ergebnis-Wert von einem Sensor
	 */
	protected Map<SystemObject, MeteoErgebnis> ergebnisIst = new HashMap<SystemObject, MeteoErgebnis>();
	

	/**
	 * Standardkonstruktor
	 * 
	 * @throws Exception leitet die Ausnahmen weiter
	 */
	public MeteorologischeKontrolleTest()
	throws Exception{
		this.dav = DAVTest.getDav(PlPruefungLogischUFDTest.CON_DATA);
		PlPruefungLogischUFDTest.initialisiere();
		PlPruefungLogischUFDTest.SENDER.setMeteoKontrolle(true);
		
		/**
		 * Setzte Ausfall�berwachung f�r alle Sensoren AUS
		 * Differentialkontrolle auf einen harmlosen Wert
		 * Anstieg-Abfall-Kontrolle aus
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			PlPruefungLogischUFDTest.SENDER.setMaxAusfallFuerSensor(sensor, -1);
			if(!UmfeldDatenArt.getUmfeldDatenArtVon(sensor).equals(UmfeldDatenArt.FBZ)){
				PlPruefungLogischUFDTest.SENDER.setDiffPara(sensor, 5, Konstante.STUNDE_IN_MS);
			}
			if(!UmfeldDatenArt.getUmfeldDatenArtVon(sensor).equals(UmfeldDatenArt.FBZ)){
				PlPruefungLogischUFDTest.SENDER.setAnAbPara(sensor, 5);
			}
		}
										
		/**
		 * Anmeldung auf alle Daten die aus der Applikation Pl-Pr�fung logisch UFD kommen
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			DataDescription datenBeschreibung = new DataDescription(
					dav.getDataModel().getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel().getAspect("asp.plausibilit�tsPr�fungLogisch"), //$NON-NLS-1$
					(short)0);
			dav.subscribeReceiver(this, sensor, datenBeschreibung,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
		}
				
		/**
		 * Initialisiere alle Objektmengen
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			if(datenArt.equals(UmfeldDatenArt.NI)){
				this.niSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.NS)){
				this.nsSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.WFD)){
				this.wfdSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.SW)){
				this.swSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.FBZ)){
				this.fbzSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.RLF)){
				this.rlfSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.LT)){
				this.ltSensoren.add(sensor);
			}				
		}
		
		/**
		 * Warte eine Sekunde bis alle Anmeldungen durch sind
		 */
		Pause.warte(1000L);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		// 	
	}
	

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return false;
	}
	
	
	/**
	 * Sendet einen Sensorwert
	 * 
	 * @param sensor der Umfelddatensensor
	 * @param wert der zu sendende Wert
	 * @param datenZeitStempel der Datenzeitstempel
	 */
	public final void sendeDatum(final SystemObject sensor, double wert, long datenZeitStempel){
		UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(TestUtensilien.getExterneErfassungDatum(sensor));
		datum.setT(PlPruefungLogischUFDTest.STANDARD_T);
		datum.getWert().setSkaliertenWert(wert);
		ResultData resultat = new ResultData(sensor, datum.getOriginalDatum().getDataDescription(), datenZeitStempel, datum.getDatum());
		try {
			PlPruefungLogischUFDTest.SENDER.sende(resultat);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(Konstante.LEERSTRING, e);
		}			
	}

	
	/**
	 * Sendet einen Sensorwert
	 * 
	 * @param sensor eine Menge von Umfelddatensensoren
	 * @param wert der zu sendende Wert
	 * @param datenZeitStempel der Datenzeitstempel
	 */
	public final void sendeDaten(final Collection<SystemObject> sensoren, double wert, long datenZeitStempel){
		for(SystemObject sensor:sensoren){
			this.sendeDatum(sensor, wert, datenZeitStempel);
		}
	}

	
	/**
	 * Sendet einen Sensorwert mit der Kennzeichnung <code>fehlerhaft</code>
	 * 
	 * @param sensor eine Menge von Umfelddatensensoren
	 * @param datenZeitStempel der Datenzeitstempel
	 */
	public final void sendeFehlerhaftDaten(final Collection<SystemObject> sensoren, long datenZeitStempel){
		for(SystemObject sensor:sensoren){
			UmfeldDatenSensorWert wert = new UmfeldDatenSensorWert(UmfeldDatenArt.getUmfeldDatenArtVon(sensor));
			wert.setFehlerhaftAn();
			this.sendeDatum(sensor, wert.getWert(), datenZeitStempel);
		}
	}

	
	
	/**
	 * Sendet einen Sensorwert
	 * 
	 * @param sensor der Umfelddatensensor
	 * @param wert der zu sendende Wert
	 * @param datenZeitStempel der Datenzeitstempel
	 */
	public final void sendeDatum(final SystemObject sensor, long wert, long datenZeitStempel){
		UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(TestUtensilien.getExterneErfassungDatum(sensor));
		datum.setT(PlPruefungLogischUFDTest.STANDARD_T);
		datum.getWert().setWert(wert);
		ResultData resultat = new ResultData(sensor, datum.getOriginalDatum().getDataDescription(), datenZeitStempel, datum.getDatum());
		if(DEBUG)System.out.println(TestUtensilien.jzt() + ", Sende: " + resultat); //$NON-NLS-1$
		try {
			PlPruefungLogischUFDTest.SENDER.sende(resultat);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(Konstante.LEERSTRING, e);
		}
	}

	
	/**
	 * Sendet einen bzw. mehrere Sensorwert
	 * 
	 * @param sensor eine Menge von Umfelddatensensoren
	 * @param wert der zu sendende Wert
	 * @param datenZeitStempel der Datenzeitstempel
	 */
	public final void sendeDaten(final Collection<SystemObject> sensoren, long wert, long datenZeitStempel){
		for(SystemObject sensor:sensoren){
			this.sendeDatum(sensor, wert, datenZeitStempel);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
					UmfeldDatenSensorDatum ufdDatum = new UmfeldDatenSensorDatum(resultat);
					boolean implausibel = ufdDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA;
					MeteoErgebnis ergebnis = new MeteoErgebnis(resultat.getObject(), resultat.getDataTime(), implausibel);
					this.ergebnisIst.put(resultat.getObject(), ergebnis);
					
					if(DEBUG){
						System.out.println(TestUtensilien.jzt() + ", Empfange(" + ergebnis + "): " + resultat); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}
	}
	
	
	/**
	 * Senset ein fehlerhaftes Datum und wartet dann f�nf intervalle (Reset)
	 * 
	 * @return ein Zeitstempel, an dem f�r <b>alle</b> Sensoren sicher 
	 * ein Wert vorliegt, der <code>nicht erfasst</code> ist 
	 */
	protected final long getTestBeginnIntervall(){
		long intervall = TestUtensilien.getBeginAktuellerSekunde();
		
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenSensorWert wert = new UmfeldDatenSensorWert(UmfeldDatenArt.getUmfeldDatenArtVon(sensor));
			wert.setFehlerhaftAn();
			this.sendeDatum(sensor, wert.getWert(), intervall);
		}

		return intervall + PlPruefungLogischUFDTest.STANDARD_T * 5;
	}
	
}
