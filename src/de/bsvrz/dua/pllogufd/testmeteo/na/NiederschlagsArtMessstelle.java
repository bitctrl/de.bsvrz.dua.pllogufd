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

package de.bsvrz.dua.pllogufd.testmeteo.na;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.testmeteo.AbstraktMeteoMessstelle;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Analogon zur <code>NiederschlagsArtsTabelle</code> aus der Feinspezifikation mit
 * zugehörigen Funktionalitäten. In dieser Klasse wird je eine Messstelle mit
 * allen Sensoren, die für das Submodul "Niederschlagsart" interessant sind betrachtet.
 * Die eigentliche Plausibilisierung wird innerhalb der Super-Klasse <code>{@link AbstraktMeteoMessstelle}</code>
 * über die Methode <code>aktualisiereDaten(..)</code> durchgeführt.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public class NiederschlagsArtMessstelle
extends AbstraktMeteoMessstelle{
	
	/**
	 * Im Submodul Niederschlagsart betrachtete Datenarten
	 */
	private static Collection<UmfeldDatenArt> DATEN_ARTEN = new HashSet<UmfeldDatenArt>();
	static{
		DATEN_ARTEN.add(UmfeldDatenArt.NS);
		DATEN_ARTEN.add(UmfeldDatenArt.NI);
		DATEN_ARTEN.add(UmfeldDatenArt.LT);
		DATEN_ARTEN.add(UmfeldDatenArt.RLF);
	}

	/**
	 * Zuordnung des Systemobjekts eines Umfelddatensensors zu einer
	 * Instanz dieser Klasse
	 */
	private static Map<SystemObject, NiederschlagsArtMessstelle> UFDS_AUF_UFDMS = new
		HashMap<SystemObject, NiederschlagsArtMessstelle>();
			
	/**
	 * letztes Niederschlagsintensitäts-Datum
	 */
	private UmfeldDatenSensorDatum letztesUfdNIDatum = null; 

	/**
	 * letztes Niederschlagsart-Datum
	 */
	private UmfeldDatenSensorDatum letztesUfdNSDatum = null;
	
	/**
	 * letztes Datum der relativen Luftfeuchte
	 */
	private UmfeldDatenSensorDatum letztesUfdRLFDatum = null;
	
	/**
	 * letztes Datum der Lufttemperatur
	 */
	private UmfeldDatenSensorDatum letztesUfdLTDatum = null;
	
	/**
	 * Parameter der Meteorologischen Kontrolle für den 
	 * Niederschlagsart-Sensor
	 */
	private NiederschlagsArtParameter parameterSensor = null;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param ufdmsObj das Systemobjekt einer Umfelddaten-Messstelle
	 * @throws DUAInitialisierungsException wenn die Umfelddaten-Messstelle nicht
	 * vollständig initialisiert werden konnte (mit allen Sensoren usw.)
	 */
	private NiederschlagsArtMessstelle(final SystemObject ufdmsObj)
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
			NiederschlagsArtMessstelle messStelle = new NiederschlagsArtMessstelle(ufdmsObj);
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
	public static final NiederschlagsArtMessstelle getMessStelleVonSensor(
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
			if(datenArt.equals(UmfeldDatenArt.NS)){
				parameterSensorObj = sensor;
				break;
			}
		}
		
		if(parameterSensorObj == null){
			throw new DUAInitialisierungsException("An Messstelle " + this +  //$NON-NLS-1$
					" konnte kein Sensor für Niederschlagsart identifiziert werden"); //$NON-NLS-1$
		}
		
		this.parameterSensor = new NiederschlagsArtParameter(VERWALTUNG, parameterSensorObj);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultData[] berechneAlleRegeln() {
		regel1();
		regel2();
		regel3();
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
			
			if(datenArt != null){
				if(this.isDatumSpeicherbar(umfeldDatum)){
					UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(umfeldDatum);
	
					erfolgreich = true;
					if(datenArt.equals(UmfeldDatenArt.NI)){
						this.letztesUfdNIDatum = datum;
					}else
					if(datenArt.equals(UmfeldDatenArt.NS)){
						this.letztesUfdNSDatum = datum;
					}else
					if(datenArt.equals(UmfeldDatenArt.RLF)){
						this.letztesUfdRLFDatum = datum;
					}else
					if(datenArt.equals(UmfeldDatenArt.LT)){
						this.letztesUfdLTDatum = datum;
					}else{
						erfolgreich = false;
					}
					
					if(erfolgreich){
						this.aktuellerZeitstempel = umfeldDatum.getDataTime();
					}
				}else{
					LOGGER.warning(this.getClass().getSimpleName() + ", Datum nicht speicherbar:\n" + umfeldDatum); //$NON-NLS-1$
				}
			}else{
				LOGGER.warning(this.getClass().getSimpleName() + ", Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
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
		
		if(this.letztesUfdNIDatum != null){
			aktuelleWerte.add(this.letztesUfdNIDatum.getVeraendertesOriginalDatum());
		}
		if(this.letztesUfdNSDatum != null){
			aktuelleWerte.add(this.letztesUfdNSDatum.getVeraendertesOriginalDatum());
		}
		if(this.letztesUfdLTDatum != null){
			aktuelleWerte.add(this.letztesUfdLTDatum.getVeraendertesOriginalDatum());
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
	protected boolean isPufferLeer() {
		return this.letztesUfdNIDatum == null &&
			   this.letztesUfdNSDatum == null &&
			   this.letztesUfdRLFDatum == null &&
			   this.letztesUfdLTDatum == null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loescheAlleWerte() {
		this.letztesUfdNIDatum = null; 
		this.letztesUfdNSDatum = null;
		this.letztesUfdRLFDatum = null;
		this.letztesUfdLTDatum = null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected UmfeldDatenSensorDatum getDatumBereitsInPosition(ResultData umfeldDatum) {
		UmfeldDatenSensorDatum datumInPosition = null;
		
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(umfeldDatum.getObject());
		if(datenArt != null){
			if(datenArt.equals(UmfeldDatenArt.NI)){
				datumInPosition = this.letztesUfdNIDatum;
			}else
			if(datenArt.equals(UmfeldDatenArt.NS)){
				datumInPosition = this.letztesUfdNSDatum;
			}else
			if(datenArt.equals(UmfeldDatenArt.RLF)){
				datumInPosition = this.letztesUfdRLFDatum;
			}else
			if(datenArt.equals(UmfeldDatenArt.LT)){
				datumInPosition = this.letztesUfdLTDatum;
			}
		}else{
			LOGGER.warning(this.getClass().getSimpleName() + ", Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
		}
		
		return datumInPosition;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean sindAlleWerteFuerIntervallDa() {
		return this.letztesUfdNIDatum != null &&
			   this.letztesUfdNSDatum != null &&
			   this.letztesUfdRLFDatum != null &&
			   this.letztesUfdLTDatum != null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<UmfeldDatenArt> getDatenArten() {
		return DATEN_ARTEN;
	}

	
	/**
	 * Die Regeln aus SE-02.00.00.00.00-AFo-3.4
	 */
	
	/**
	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (NS == Regen) <b>und</b> (LT < NSgrenzLT) <b>dann</b> (NS=implausibel)</code>
	 * <br>Die Ergebnisse werden zurück in die lokalen Variablen geschrieben  
	 */
	private final void regel1(){
		if(this.letztesUfdLTDatum != null &&
		   this.letztesUfdNSDatum != null &&
		   this.letztesUfdLTDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdNSDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdLTDatum.getWert().isOk()){
			if(this.letztesUfdNSDatum.getWert().getWert() > 39 &&
			   this.letztesUfdNSDatum.getWert().getWert() < 70){ // Regen
				if(this.parameterSensor.isInitialisiert() && 
				   this.parameterSensor.getNsGrenzLT().isOk()){
					if(this.letztesUfdLTDatum.getWert().getWert() < this.parameterSensor.getNsGrenzLT().getWert()){
						this.letztesUfdNSDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
						this.letztesUfdNSDatum.getWert().setFehlerhaftAn();
						LOGGER.fine("[NS.R1]Daten geändert:\n" + this.letztesUfdNSDatum.toString()); //$NON-NLS-1$
					}
				}
			}
		}
	}
	
	
	/**
 	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (NS == Niederschlag) <b>und</b> (NI > 0) <b>dann</b> (NI=implausibel, NS=implausibel)</code>
	 * <br>Die Ergebnisse werden zurück in die lokalen Variablen geschrieben  
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private final void regel2Alt(){
		if(this.letztesUfdNIDatum != null &&
		   this.letztesUfdNSDatum != null &&
		   this.letztesUfdNIDatum.getDatenZeit() == this.letztesUfdNSDatum.getDatenZeit() &&
		   this.letztesUfdNIDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdNSDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
			
			if(this.letztesUfdNSDatum.getWert().getWert() == 0 && 
			   this.letztesUfdNIDatum.getWert().getWert() > 0){
				this.letztesUfdNIDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				this.letztesUfdNIDatum.getWert().setFehlerhaftAn();
				this.letztesUfdNSDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				this.letztesUfdNSDatum.getWert().setFehlerhaftAn();
				LOGGER.fine("[NS.R2]Daten geändert:\n" + this.letztesUfdNIDatum.toString() + //$NON-NLS-1$ 
						"\n" + this.letztesUfdNSDatum.toString()); //$NON-NLS-1$
			}
		}	
	}

	
	/**
 	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (NS == Niederschlag) <b>und</b> (NI > 0) <b>und</b> RLF == <code>nicht erfasst</code> 
	 * <b>dann</b> (NI=implausibel, NS=implausibel)</code>
	 * <br>Die Ergebnisse werden zurück in die lokalen Variablen geschrieben  
	 */
	private final void regel2(){
		if(this.letztesUfdNIDatum != null &&
		   this.letztesUfdNSDatum != null &&
		   this.letztesUfdRLFDatum != null &&
		   this.letztesUfdNIDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdNSDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdRLFDatum.getStatusErfassungNichtErfasst() == DUAKonstanten.JA){
			
			if(this.letztesUfdNSDatum.getWert().getWert() == 0 && 
			   this.letztesUfdNIDatum.getWert().getWert() > 0){
				this.letztesUfdNIDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				this.letztesUfdNIDatum.getWert().setFehlerhaftAn();
				this.letztesUfdNSDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				this.letztesUfdNSDatum.getWert().setFehlerhaftAn();
				LOGGER.fine("[NS.R2]Daten geändert:\n" + this.letztesUfdNIDatum.toString() + //$NON-NLS-1$ 
						"\n" + this.letztesUfdNSDatum.toString()); //$NON-NLS-1$
			}
		}	
	}

	
	/**
 	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (NS == Niederschlag) <b>und</b> (NI == 0) <b>und</b> (RLF < NSgrenzTrockenRLF)
	 * <b>dann</b> (NS=implausibel)</code>
	 * <br>Die Ergebnisse werden zurück in die lokalen Variablen geschrieben  
	 */
	private final void regel3(){
		if(this.letztesUfdNIDatum != null &&
		   this.letztesUfdNSDatum != null &&
		   this.letztesUfdRLFDatum != null &&
		   this.letztesUfdNIDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdNSDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdRLFDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
			if(this.parameterSensor.isInitialisiert() &&
			   this.parameterSensor.getNsGrenzTrockenRLF().isOk() &&
			   this.letztesUfdRLFDatum.getWert().isOk()){
				if(this.letztesUfdNSDatum.getWert().getWert() > 0 &&
				   this.letztesUfdNIDatum.getWert().getWert() == 0 &&
				   this.letztesUfdRLFDatum.getWert().getWert() < this.parameterSensor.getNsGrenzTrockenRLF().getWert()){
					this.letztesUfdNSDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					this.letztesUfdNSDatum.getWert().setFehlerhaftAn();
					LOGGER.fine("[NS.R3]Daten geändert:\n" + this.letztesUfdNSDatum.toString()); //$NON-NLS-1$
				}				
			}
		}		
	}
}
