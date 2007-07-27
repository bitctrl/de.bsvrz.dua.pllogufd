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

package de.bsvrz.dua.pllogufd.testmeteo.sw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.ConfigurationObject;
import stauma.dav.configuration.interfaces.ObjectSet;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.testmeteo.AbstraktMeteoMessstelle;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
* Analogon zur <code>SichtweitenTabelle</code> aus der Feinspezifikation mit
* zugehörigen Funktionalitäten. In dieser Klasse wird je eine Messstelle mit
* allen Sensoren, die für das Submodul "Sichtweiten" interessant sind betrachtet.
* Die eigentliche Plausibilisierung wird innerhalb der Super-Klasse <code>{@link AbstraktMeteoMessstelle}</code>
* über die Methode <code>aktualisiereDaten(..)</code> durchgeführt.
* 
* @author BitCtrl Systems GmbH, Thierfelder
* 
*/
public class SichtweitenMessstelle
extends AbstraktMeteoMessstelle{

	/**
	 * Im Submodul Sichtweiten betrachtete Datenarten
	 */
	private static Collection<UmfeldDatenArt> DATEN_ARTEN = new HashSet<UmfeldDatenArt>();
	static{
		DATEN_ARTEN.add(UmfeldDatenArt.SW);
		DATEN_ARTEN.add(UmfeldDatenArt.NS);
		DATEN_ARTEN.add(UmfeldDatenArt.RLF);
	}

	/**
	 * Zuordnung des Systemobjekts eines Umfelddatensensors zu einer
	 * Instanz dieser Klasse
	 */
	private static Map<SystemObject, SichtweitenMessstelle> UFDS_AUF_UFDMS = new
		HashMap<SystemObject, SichtweitenMessstelle>();
			
	/**
	 * letztes Scihtweite-Datum
	 */
	private UmfeldDatenSensorDatum letztesUfdSWDatum = null; 

	/**
	 * letztes Niederschlagsart-Datum
	 */
	private UmfeldDatenSensorDatum letztesUfdNSDatum = null;
	
	/**
	 * letztes Datum der relativen Luftfeuchte
	 */
	private UmfeldDatenSensorDatum letztesUfdRLFDatum = null;
		
	/**
	 * Parameter der Meteorologischen Kontrolle für den 
	 * Sichtweiten-Sensor
	 */
	private SichtweitenParameter parameterSensor = null;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param ufdmsObj das Systemobjekt einer Umfelddaten-Messstelle
	 * @throws DUAInitialisierungsException wenn die Umfelddaten-Messstelle nicht
	 * vollständig initialisiert werden konnte (mit allen Sensoren usw.)
	 */
	private SichtweitenMessstelle(final SystemObject ufdmsObj)
	throws DUAInitialisierungsException{
		super(ufdmsObj);
		if(ufdmsObj instanceof ConfigurationObject){
			ConfigurationObject ufdmsConObj = (ConfigurationObject)ufdmsObj;
			ObjectSet sensorMengeAnMessStelle = ufdmsConObj.getObjectSet("UmfeldDatenSensoren"); //$NON-NLS-1$
			
			if(sensorMengeAnMessStelle != null){
				for(SystemObject betrachtetesObjekt:VERWALTUNG.getSystemObjekte()){
					if(sensorMengeAnMessStelle.getElements().contains(betrachtetesObjekt)){
						UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(betrachtetesObjekt);
						if(datenArt == null){
							throw new DUAInitialisierungsException("Unbekannter Sensor (" +  //$NON-NLS-1$
									betrachtetesObjekt + ") an Messstelle " + ufdmsObj); //$NON-NLS-1$
						}else
						if(DATEN_ARTEN.contains(datenArt)){
							sensorenAnMessStelle.add(betrachtetesObjekt);	
						}
					}
				}
			}
		}else{
			/**
			 * sollte eigentlich nicht vorkommen
			 */
			throw new DUAInitialisierungsException(ufdmsObj +
					" ist kein Konfigurationsobjekt"); //$NON-NLS-1$
		}	
	}
	
	
	/**
	 * Initialisiert die statischen Instanzen dieser Klasse
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @throws DUAInitialisierungsException wenn eine Messstelle nicht instanziiert
	 * werden konnte oder wenn ein Umfelddatensensor mehreren Messstellen zugeordnet
	 * ist
	 */
	public static final void initialisiere(final IVerwaltung verwaltung)
	throws DUAInitialisierungsException{
		setVerwaltungsModul(verwaltung);
		
		for(SystemObject ufdmsObj:verwaltung.getVerbindung().getDataModel().
									getType("typ.umfeldDatenMessStelle").getElements()){ //$NON-NLS-1$
			SichtweitenMessstelle messStelle = new SichtweitenMessstelle(ufdmsObj);
			if(messStelle.getSensoren().isEmpty()){
				LOGGER.config("Umfelddaten-Messstelle " + ufdmsObj + //$NON-NLS-1$ 
						" wird nicht betrachtet"); //$NON-NLS-1$
			}else{
				if(messStelle.getSensoren().size() == DATEN_ARTEN.size()){
					for(SystemObject umfeldDatenSensor:messStelle.getSensoren()){
						if(UFDS_AUF_UFDMS.get(umfeldDatenSensor) != null){
							throw new DUAInitialisierungsException("Der Umfelddatensensor " + umfeldDatenSensor + //$NON-NLS-1$
									" ist gleichzeitig an mehr als einer Messstelle konfiguriert:\n" + //$NON-NLS-1$
									UFDS_AUF_UFDMS.get(umfeldDatenSensor) + " und\n" + messStelle); //$NON-NLS-1$
						}
						messStelle.initialisiereMessStelle();
						UFDS_AUF_UFDMS.put(umfeldDatenSensor, messStelle);
					}					
				}
			}
		}
	}
	
	
	/**
	 * Erfragt die Umfelddaten-Messstelle (dieses Typs),
	 * an der ein bestimmter Sensor konfiguriert ist
	 * 
	 * @param umfeldDatenSensorObj das Systemobjekt eines
	 * Umfelddatensensors
	 * @return die Umfelddaten-Messstelle oder <code>null</code>,
	 * wenn der Sensor nicht betrachtet wird
	 */
	public static final SichtweitenMessstelle getMessStelleVonSensor(
												final SystemObject umfeldDatenSensorObj){
		return UFDS_AUF_UFDMS.get(umfeldDatenSensorObj);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiereMessStelle()
	throws DUAInitialisierungsException{
		SystemObject parameterSensorObj = null;
		
		for(SystemObject sensor:this.getSensoren()){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			if(datenArt.equals(UmfeldDatenArt.SW)){
				parameterSensorObj = sensor;
				break;
			}
		}
		
		if(parameterSensorObj == null){
			throw new DUAInitialisierungsException("An Messstelle " + this +  //$NON-NLS-1$
					" konnte kein Sensor für Sichtweiten identifiziert werden"); //$NON-NLS-1$
		}
		
		this.parameterSensor = new SichtweitenParameter(VERWALTUNG, parameterSensorObj);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultData[] berechneAlleRegeln() {
		regel1();
		return this.getAlleAktuellenWerte();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean bringeDatumInPosition(ResultData umfeldDatum) {
		boolean erfolgreich = false;
		
		if(umfeldDatum.getData() != null){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(umfeldDatum.getObject());
			
			if(datenArt != null && this.isDatumSpeicherbar(umfeldDatum)){
				UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(umfeldDatum);

				erfolgreich = true;
				if(datenArt.equals(UmfeldDatenArt.SW)){
					this.letztesUfdSWDatum = datum;
				}else
				if(datenArt.equals(UmfeldDatenArt.NS)){
					this.letztesUfdNSDatum = datum;
				}else
				if(datenArt.equals(UmfeldDatenArt.RLF)){
					this.letztesUfdRLFDatum = datum;
				}else{
					erfolgreich = false;
				}
				
				if(erfolgreich){
					this.aktuellerZeitstempel = umfeldDatum.getDataTime();
				}
			}else{
				LOGGER.warning("Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
			}
		}
		
		return erfolgreich;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultData[] getAlleAktuellenWerte() {
		List<ResultData> aktuelleWerte = new ArrayList<ResultData>();
		
		if(this.letztesUfdSWDatum != null){
			aktuelleWerte.add(this.letztesUfdSWDatum.getVeraendertesOriginalDatum());
		}
		if(this.letztesUfdNSDatum != null){
			aktuelleWerte.add(this.letztesUfdNSDatum.getVeraendertesOriginalDatum());
		}
		if(this.letztesUfdRLFDatum != null){
			aktuelleWerte.add(this.letztesUfdRLFDatum.getVeraendertesOriginalDatum());
		}
		
		return aktuelleWerte.toArray(new ResultData[0]);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<UmfeldDatenArt> getDatenArten() {
		return DATEN_ARTEN;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loescheAlleWerte() {
		this.letztesUfdSWDatum = null; 
		this.letztesUfdNSDatum = null;
		this.letztesUfdRLFDatum = null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean sindAlleWerteFuerIntervallDa() {
		return this.letztesUfdSWDatum != null &&
			   this.letztesUfdNSDatum != null &&
			   this.letztesUfdRLFDatum != null;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isPufferLeer() {
		return this.letztesUfdSWDatum == null &&
			   this.letztesUfdNSDatum == null &&
			   this.letztesUfdRLFDatum == null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected UmfeldDatenSensorDatum getDatumBereitsInPosition(ResultData umfeldDatum) {
		UmfeldDatenSensorDatum datumInPosition = null;
		
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(umfeldDatum.getObject());
		if(datenArt != null){
			if(datenArt.equals(UmfeldDatenArt.SW)){
				datumInPosition = this.letztesUfdSWDatum;
			}else
			if(datenArt.equals(UmfeldDatenArt.NS)){
				datumInPosition = this.letztesUfdNSDatum;
			}else
			if(datenArt.equals(UmfeldDatenArt.RLF)){
				datumInPosition = this.letztesUfdRLFDatum;
			}
		}else{
			LOGGER.warning("Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
		}
		
		return datumInPosition;
	}
	
	
	/**
	 * Die Regeln aus SE-02.00.00.00.00-AFo-3.4
	 */
	
	/**
 	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (SW <= SWgrenz) <b>und</b> (NS == kein Niederschlag) <b>und</b> (RLF < SWgrenzTrockenRLF)
	 * <b>dann</b> (SW=implausibel)</code>
	 * <br>Die Ergebnisse werden zurück in die lokalen Variablen geschrieben  
	 */
	private final void regel1(){
		if(this.letztesUfdSWDatum != null &&
		   this.letztesUfdNSDatum != null &&
		   this.letztesUfdRLFDatum != null &&
		   this.letztesUfdSWDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdNSDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdRLFDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
			if(this.parameterSensor.isInitialisiert() &&
			   this.parameterSensor.getSWgrenzSW().isOk() &&
			   this.parameterSensor.getSWgrenzTrockenRLF().isOk() &&
			   this.letztesUfdRLFDatum.getWert().isOk() &&
			   this.letztesUfdSWDatum.getWert().isOk()){
				if(this.letztesUfdSWDatum.getWert().getWert() <= this.parameterSensor.getSWgrenzSW().getWert() &&
				   this.letztesUfdNSDatum.getWert().getWert() == 0 &&
				   this.letztesUfdRLFDatum.getWert().getWert() < this.parameterSensor.getSWgrenzTrockenRLF().getWert()){
					this.letztesUfdSWDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					this.letztesUfdSWDatum.getWert().setFehlerhaftAn();
					LOGGER.fine("[SW.R1]Daten geändert:\n" + this.letztesUfdSWDatum.toString()); //$NON-NLS-1$
				}				
			}
		}		
	}

}
