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

package de.bsvrz.dua.pllogufd.testmeteo.ni;

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
import de.bsvrz.dua.pllogufd.UmfeldDatenSensorDatum;
import de.bsvrz.dua.pllogufd.testmeteo.AbstraktMeteoMessstelle;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Analogon zur <code>NiederschlagsIntensitätsTabelle</code> aus der Feinspezifikation mit
 * zugehörigen Funktionalitäten. In dieser Klasse wird je eine Messstelle mit
 * allen Sensoren, die für das Submodul "Niederschlagsintensität" interessant sind betrachtet.
 * Die eigentliche Plausibilisierung wird innerhalb der Super-Klasse <code>{@link AbstraktMeteoMessstelle}</code>
 * über die Methode <code>aktualisiereDaten(..)</code> durchgeführt.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public class NiederschlagsIntensitaetsMessstelle
extends AbstraktMeteoMessstelle{

	/**
	 * Im Submodul Niederschlagsintensität betrachtete Datenarten
	 */
	private static Collection<UmfeldDatenArt> DATEN_ARTEN = new HashSet<UmfeldDatenArt>();
	static{
		DATEN_ARTEN.add(UmfeldDatenArt.NS);
		DATEN_ARTEN.add(UmfeldDatenArt.NI);
		DATEN_ARTEN.add(UmfeldDatenArt.WFD);
		DATEN_ARTEN.add(UmfeldDatenArt.RLF);
	}

	/**
	 * Zuordnung des Systemobjekts eines Umfelddatensensors zu einer
	 * Instanz dieser Klasse
	 */
	private static Map<SystemObject, NiederschlagsIntensitaetsMessstelle> UFDS_AUF_UFDMS = new
		HashMap<SystemObject, NiederschlagsIntensitaetsMessstelle>();
			
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
	 * zählt die Millisekunden, die sich die relative Luftfeuchte
	 * schon unterhalb von <code>NIgrenzTrockenRLF</code> befindet
	 */
	private long rlfUnterNIgrenzTrockenFuerMS = 0;

	/**
	 * letztes Datum der Wasserfilmdicke
	 */
	private UmfeldDatenSensorDatum letztesUfdWFDDatum = null;
	
	/**
	 * Parameter der Meteorologischen Kontrolle für den 
	 * Niederschlagsintensitäts-Sensor
	 */
	private NiederschlagsIntensitaetsParameter parameterSensor = null;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param ufdmsObj das Systemobjekt einer Umfelddaten-Messstelle
	 * @throws DUAInitialisierungsException wenn die Umfelddaten-Messstelle nicht
	 * vollständig initialisiert werden konnte (mit allen Sensoren usw.)
	 */
	private NiederschlagsIntensitaetsMessstelle(final SystemObject ufdmsObj)
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
			NiederschlagsIntensitaetsMessstelle messStelle = new NiederschlagsIntensitaetsMessstelle(ufdmsObj);
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
	public static final NiederschlagsIntensitaetsMessstelle getMessStelleVonSensor(
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
			if(datenArt.equals(UmfeldDatenArt.NI)){
				parameterSensorObj = sensor;
				break;
			}
		}
		
		if(parameterSensorObj == null){
			throw new DUAInitialisierungsException("An Messstelle " + this +  //$NON-NLS-1$
					" konnte kein Sensor für Niederschlagsintensität identifiziert werden"); //$NON-NLS-1$
		}
		
		this.parameterSensor = new NiederschlagsIntensitaetsParameter(VERWALTUNG, parameterSensorObj);
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
	protected void bringeDatumInPosition(ResultData umfeldDatum) {
		if(umfeldDatum.getData() != null){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(umfeldDatum.getObject());
			
			if(datenArt != null){
				UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(umfeldDatum);
				
				LOGGER.info("Speichere: " + datum); //$NON-NLS-1$
				
				if(datenArt.equals(UmfeldDatenArt.NI)){
					this.letztesUfdNIDatum = datum;
				}else
				if(datenArt.equals(UmfeldDatenArt.NS)){
					this.letztesUfdNSDatum = datum;
				}else
				if(datenArt.equals(UmfeldDatenArt.RLF)){
					this.letztesUfdRLFDatum = datum;
					if(datum.getWert().isOk() &&
					   this.parameterSensor.isInitialisiert() && 
					   this.parameterSensor.getNIGrenzTrockenRLF().isOk() &&
					   datum.getWert().getWert() < this.parameterSensor.getNIGrenzTrockenRLF().getWert()){
						this.rlfUnterNIgrenzTrockenFuerMS += datum.getT();
					}else{
						this.rlfUnterNIgrenzTrockenFuerMS = 0;
					}
				}else
				if(datenArt.equals(UmfeldDatenArt.WFD)){
					this.letztesUfdWFDDatum = datum;
				}
			}else{
				LOGGER.error("Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultData[] getAlleAktuellenWerte() {
		List<ResultData> aktuelleWerte = new ArrayList<ResultData>();
		
		if(this.letztesUfdNIDatum != null){
			aktuelleWerte.add(this.letztesUfdNIDatum.getVeraendertesOriginalDatum());
			this.setLetztenBerabeitetenZeitstempel(this.letztesUfdNIDatum);
		}
		if(this.letztesUfdNSDatum != null){
			aktuelleWerte.add(this.letztesUfdNSDatum.getVeraendertesOriginalDatum());
			this.setLetztenBerabeitetenZeitstempel(this.letztesUfdNSDatum);
		}
		if(this.letztesUfdWFDDatum != null){
			aktuelleWerte.add(this.letztesUfdWFDDatum.getVeraendertesOriginalDatum());
			this.setLetztenBerabeitetenZeitstempel(this.letztesUfdWFDDatum);
		}
		if(this.letztesUfdRLFDatum != null){
			aktuelleWerte.add(this.letztesUfdRLFDatum.getVeraendertesOriginalDatum());
			this.setLetztenBerabeitetenZeitstempel(this.letztesUfdRLFDatum);
		}
		
		return aktuelleWerte.toArray(new ResultData[0]);
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
			if(datenArt.equals(UmfeldDatenArt.WFD)){
				datumInPosition = this.letztesUfdWFDDatum;
			}
		}else{
			LOGGER.error("Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
		}
		
		return datumInPosition;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isDatenArtRelevantFuerSubModul(ResultData umfeldDatum) {
		boolean relevant = false;
		
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(umfeldDatum.getObject());
		if(datenArt != null){
			relevant = DATEN_ARTEN.contains(datenArt) && 
			   		   this.letzterBearbeiteterZeitStempel != umfeldDatum.getDataTime();
		}else{
			LOGGER.error("Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
		}
		
		return relevant;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loescheAlleWerte() {
		this.letztesUfdNIDatum = null; 
		this.letztesUfdNSDatum = null;
		this.letztesUfdRLFDatum = null;
		this.letztesUfdWFDDatum = null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean sindAlleWerteFuerIntervallDa() {
		boolean alleWerteFuerIntervallDa = false;
		
		if(this.letztesUfdNIDatum != null &&
		   this.letztesUfdNSDatum != null &&
		   this.letztesUfdRLFDatum != null &&
		   this.letztesUfdWFDDatum != null){
			
			final long datenZeit = this.letztesUfdNIDatum.getDatenZeit();
			if(this.letztesUfdNSDatum.getDatenZeit() == datenZeit &&
			   this.letztesUfdRLFDatum.getDatenZeit() == datenZeit &&
			   this.letztesUfdWFDDatum.getDatenZeit() == datenZeit){
				alleWerteFuerIntervallDa = true;
			}
		}
		
		return alleWerteFuerIntervallDa;
	}

	
	/**
	 * Die Regeln aus SE-02.00.00.00.00-AFo-3.4
	 */
	
	/**
	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (NI > 0) <b>und</b> (NS == kein Niederschlag) <b>dann</b> (NS=implausibel, NI=implausibel)</code>
	 * <br>Die Ergebnisse werden zurück in die lokalen Variablen geschrieben  
	 */
	private final void regel1(){
		if(this.letztesUfdNIDatum != null &&
		   this.letztesUfdNSDatum != null &&
		   this.letztesUfdNIDatum.getDatenZeit() == this.letztesUfdNSDatum.getDatenZeit() &&
		   this.letztesUfdNIDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdNSDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
			if(this.letztesUfdNIDatum.getWert().getWert() > 0 && 
			   this.letztesUfdNSDatum.getWert().getWert() == 0){
				this.letztesUfdNIDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				this.letztesUfdNIDatum.getWert().setFehlerhaftAn();
				this.letztesUfdNSDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				this.letztesUfdNSDatum.getWert().setFehlerhaftAn();
				LOGGER.info("[NI.R1]Daten geändert:\n" + this.letztesUfdNIDatum.toString() + //$NON-NLS-1$ 
						"\n" + this.letztesUfdNSDatum.toString()); //$NON-NLS-1$
			}
		}
	}
	
	
	/**
 	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (NS == Niederschlag) <b>und</b> (NI > NIminNI) <b>und</b> (RLF < NIgrenzTrockenRLF)
	 * <b>dann</b> (NI=implausibel)</code>
	 * <br>Die Ergebnisse werden zurück in die lokalen Variablen geschrieben  
	 */
	private final void regel2(){
		if(this.letztesUfdNIDatum != null &&
		   this.letztesUfdNSDatum != null &&
		   this.letztesUfdRLFDatum != null &&
		   this.letztesUfdNIDatum.getDatenZeit() == this.letztesUfdNSDatum.getDatenZeit() &&
		   this.letztesUfdNIDatum.getDatenZeit() == this.letztesUfdRLFDatum.getDatenZeit() &&
		   this.letztesUfdNIDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdNSDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdRLFDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
			if(this.parameterSensor.isInitialisiert() &&
			   this.parameterSensor.getNIminNI().isOk() &&
			   this.parameterSensor.getNIGrenzTrockenRLF().isOk() &&
			   this.letztesUfdRLFDatum.getWert().isOk()){
				if(this.letztesUfdNSDatum.getWert().getWert() == 0 &&
				   this.letztesUfdNIDatum.getWert().getWert() > this.parameterSensor.getNIminNI().getWert() &&
				   this.letztesUfdRLFDatum.getWert().getWert() < this.parameterSensor.getNIGrenzTrockenRLF().getWert()){
					this.letztesUfdNIDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					this.letztesUfdNIDatum.getWert().setFehlerhaftAn();
					LOGGER.info("[NI.R2]Daten geändert:\n" + this.letztesUfdNIDatum.toString()); //$NON-NLS-1$
				}				
			}
		}		
	}
	
	
	/**
 	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (NI > 0.5) <b>und</b> (WFD == 0) <b>und</b> (RLF > WFDgrenzNassPLF für Zeitraum > WFDminNassRLF)
	 * <b>dann</b> (NI=implausibel)</code>
	 * <br>Die Ergebnisse werden zurück in die lokalen Variablen geschrieben  
	 */
	private final void regel3(){
		if(this.letztesUfdNIDatum != null &&
		   this.letztesUfdWFDDatum != null &&
		   this.letztesUfdRLFDatum != null &&
		   this.letztesUfdNIDatum.getDatenZeit() == this.letztesUfdWFDDatum.getDatenZeit() &&
		   this.letztesUfdNIDatum.getDatenZeit() == this.letztesUfdRLFDatum.getDatenZeit() &&
		   this.letztesUfdNIDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdWFDDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN &&
		   this.letztesUfdRLFDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
			if(this.parameterSensor.isInitialisiert() &&
					
			   this.parameterSensor.getEXTRA().isOk() &&
			   
			   this.parameterSensor.getNIGrenzTrockenRLF().isOk() &&
			   this.letztesUfdRLFDatum.getWert().isOk()){
				if(this.letztesUfdNIDatum.getWert().getWert() > this.parameterSensor.getEXTRA().getWert() &&
				   this.letztesUfdWFDDatum.getWert().getWert() == 0 &&
				   this.rlfUnterNIgrenzTrockenFuerMS > this.parameterSensor.getNIminTrockenRLF()){
					this.letztesUfdNIDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					this.letztesUfdNIDatum.getWert().setFehlerhaftAn();
					LOGGER.info("[NI.R3]Daten geändert:\n" + this.letztesUfdNIDatum.toString()); //$NON-NLS-1$
				}				
			}
		}		
	}
	
}
