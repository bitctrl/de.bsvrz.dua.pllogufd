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

package de.bsvrz.dua.pllogufd.testmeteo.wfd;

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
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Analogon zur <code>WasserfilmDickeTabelle</code> aus der Feinspezifikation
 * mit zugehörigen Funktionalitäten. In dieser Klasse wird je eine Messstelle
 * mit allen Sensoren, die für das Submodul "WasserfilmDicke" interessant sind
 * betrachtet. Die eigentliche Plausibilisierung wird innerhalb der Super-Klasse
 * <code>{@link AbstraktMeteoMessstelle}</code> über die Methode
 * <code>aktualisiereDaten(..)</code> durchgeführt.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public final class WasserfilmDickeMessstelle extends AbstraktMeteoMessstelle {

	/**
	 * Im Submodul WasserfilmDicke betrachtete Datenarten.
	 */
	private static Collection<UmfeldDatenArt> datenArten = new HashSet<UmfeldDatenArt>();
	static {
		datenArten.add(UmfeldDatenArt.fbz);
		datenArten.add(UmfeldDatenArt.ni);
		datenArten.add(UmfeldDatenArt.wfd);
		datenArten.add(UmfeldDatenArt.rlf);
	}

	/**
	 * Zuordnung des Systemobjekts eines Umfelddatensensors zu einer Instanz
	 * dieser Klasse.
	 */
	private static Map<SystemObject, WasserfilmDickeMessstelle> ufdsAufUfdMs = new HashMap<SystemObject, WasserfilmDickeMessstelle>();

	/**
	 * letztes Niederschlagsintensitäts-Datum.
	 */
	private UmfeldDatenSensorDatum letztesUfdNIDatum = null;

	/**
	 * letztes Niederschlagsart-Datum.
	 */
	private UmfeldDatenSensorDatum letztesUfdFBZDatum = null;

	/**
	 * letztes Datum der relativen Luftfeuchte.
	 */
	private UmfeldDatenSensorDatum letztesUfdRLFDatum = null;

	/**
	 * zählt die Millisekunden, die sich die relative Luftfeuchte schon
	 * unterhalb von <code>WFDgrenzNassRLF</code> befindet.
	 */
	private long rlfUeberWfdgrenzNassFuerMS = 0;

	/**
	 * letztes Datum der Wasserfilmdicke.
	 */
	private UmfeldDatenSensorDatum letztesUfdWFDDatum = null;

	/**
	 * Parameter der Meteorologischen Kontrolle für den Wasserfilmdicke-Sensor.
	 */
	private WasserFilmDickeParameter parameterSensor = null;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param ufdmsObj
	 *            das Systemobjekt einer Umfelddaten-Messstelle
	 * @throws DUAInitialisierungsException
	 *             wenn die Umfelddaten-Messstelle nicht vollständig
	 *             initialisiert werden konnte (mit allen Sensoren usw.)
	 */
	private WasserfilmDickeMessstelle(final SystemObject ufdmsObj)
			throws DUAInitialisierungsException {
		super(ufdmsObj);
		if (ufdmsObj instanceof ConfigurationObject) {
			ConfigurationObject ufdmsConObj = (ConfigurationObject) ufdmsObj;
			ObjectSet sensorMengeAnMessStelle = ufdmsConObj
					.getObjectSet("UmfeldDatenSensoren"); //$NON-NLS-1$

			if (sensorMengeAnMessStelle != null) {
				for (SystemObject betrachtetesObjekt : verwaltung
						.getSystemObjekte()) {
					if (betrachtetesObjekt.isValid()) {
						if (sensorMengeAnMessStelle.getElements().contains(
								betrachtetesObjekt)) {
							UmfeldDatenArt datenArt = UmfeldDatenArt
									.getUmfeldDatenArtVon(betrachtetesObjekt);
							if (datenArt == null) {
								throw new DUAInitialisierungsException(
										"Unbekannter Sensor (" + //$NON-NLS-1$
												betrachtetesObjekt
												+ ") an Messstelle " + ufdmsObj); //$NON-NLS-1$
							} else if (datenArten.contains(datenArt)) {
								sensorenAnMessStelle.add(betrachtetesObjekt);
							}
						}
					}
				}
			}
		} else {
			/**
			 * sollte eigentlich nicht vorkommen
			 */
			throw new DUAInitialisierungsException(ufdmsObj
					+ " ist kein Konfigurationsobjekt"); //$NON-NLS-1$
		}
	}

	/**
	 * Initialisiert die statischen Instanzen dieser Klasse.
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @throws DUAInitialisierungsException
	 *             wenn eine Messstelle nicht instanziiert werden konnte oder
	 *             wenn ein Umfelddatensensor mehreren Messstellen zugeordnet
	 *             ist
	 */
	public static void initialisiere(final IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		setVerwaltungsModul(verwaltung);

		for (SystemObject ufdmsObj : verwaltung.getVerbindung().getDataModel()
				.getType("typ.umfeldDatenMessStelle").getElements()) { //$NON-NLS-1$
			if (ufdmsObj.isValid()) {
				WasserfilmDickeMessstelle messStelle = new WasserfilmDickeMessstelle(
						ufdmsObj);
				if (messStelle.getSensoren().isEmpty()) {
					Debug.getLogger().config("Umfelddaten-Messstelle " + ufdmsObj + //$NON-NLS-1$ 
							" wird nicht betrachtet"); //$NON-NLS-1$
				} else {
					if (messStelle.getSensoren().size() == datenArten.size()) {
						for (SystemObject umfeldDatenSensor : messStelle
								.getSensoren()) {
							if (ufdsAufUfdMs.get(umfeldDatenSensor) != null) {
								throw new DUAInitialisierungsException(
										"Der Umfelddatensensor " + umfeldDatenSensor + //$NON-NLS-1$
												" ist gleichzeitig an mehr als einer Messstelle konfiguriert:\n" //$NON-NLS-1$
												+ 
												ufdsAufUfdMs
														.get(umfeldDatenSensor)
												+ " und\n" + messStelle); //$NON-NLS-1$
							}
							messStelle.initialisiereMessStelle();
							ufdsAufUfdMs.put(umfeldDatenSensor, messStelle);
						}
					}
				}
			}
		}
	}

	/**
	 * Erfragt die Umfelddaten-Messstelle (dieses Typs), an der ein bestimmter
	 * Sensor konfiguriert ist.
	 * 
	 * @param umfeldDatenSensorObj
	 *            das Systemobjekt eines Umfelddatensensors
	 * @return die Umfelddaten-Messstelle oder <code>null</code>, wenn der
	 *         Sensor nicht betrachtet wird
	 */
	public static WasserfilmDickeMessstelle getMessStelleVonSensor(
			final SystemObject umfeldDatenSensorObj) {
		return ufdsAufUfdMs.get(umfeldDatenSensorObj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiereMessStelle()
			throws DUAInitialisierungsException {
		SystemObject parameterSensorObj = null;

		for (SystemObject sensor : this.getSensoren()) {
			UmfeldDatenArt datenArt = UmfeldDatenArt
					.getUmfeldDatenArtVon(sensor);
			if (datenArt.equals(UmfeldDatenArt.wfd)) {
				parameterSensorObj = sensor;
				break;
			}
		}

		if (parameterSensorObj == null) {
			throw new DUAInitialisierungsException("An Messstelle " + this + //$NON-NLS-1$
					" konnte kein Sensor für Wasserfilmdicke identifiziert werden"); //$NON-NLS-1$
		}

		this.parameterSensor = new WasserFilmDickeParameter(verwaltung,
				parameterSensorObj);
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

		if (umfeldDatum.getData() != null) {
			UmfeldDatenArt datenArt = UmfeldDatenArt
					.getUmfeldDatenArtVon(umfeldDatum.getObject());

			if (datenArt != null) {
				if (this.isDatumSpeicherbar(umfeldDatum)) {
					UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
							umfeldDatum);

					erfolgreich = true;
					if (datenArt.equals(UmfeldDatenArt.ni)) {
						this.letztesUfdNIDatum = datum;
					} else if (datenArt.equals(UmfeldDatenArt.fbz)) {
						this.letztesUfdFBZDatum = datum;
					} else if (datenArt.equals(UmfeldDatenArt.rlf)) {
						this.letztesUfdRLFDatum = datum;
						if (datum.getWert().isOk()
								&& this.parameterSensor.isInitialisiert()
								&& this.parameterSensor.getWFDgrenzNassRLF()
										.isOk()
								&& datum.getWert().getWert() > this.parameterSensor
										.getWFDgrenzNassRLF().getWert()) {
							this.rlfUeberWfdgrenzNassFuerMS += datum.getT();
						} else {
							this.rlfUeberWfdgrenzNassFuerMS = 0;
						}
					} else if (datenArt.equals(UmfeldDatenArt.wfd)) {
						this.letztesUfdWFDDatum = datum;
					} else {
						erfolgreich = false;
					}

					if (erfolgreich) {
						this.aktuellerZeitstempel = umfeldDatum.getDataTime();
					}
				} else {
					Debug.getLogger().warning(this.getClass().getSimpleName()
							+ ", Datum nicht speicherbar:\n" + umfeldDatum); //$NON-NLS-1$
				}
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

		if (this.letztesUfdNIDatum != null) {
			aktuelleWerte.add(this.letztesUfdNIDatum
					.getVeraendertesOriginalDatum());
		}
		if (this.letztesUfdFBZDatum != null) {
			aktuelleWerte.add(this.letztesUfdFBZDatum
					.getVeraendertesOriginalDatum());
		}
		if (this.letztesUfdWFDDatum != null) {
			aktuelleWerte.add(this.letztesUfdWFDDatum
					.getVeraendertesOriginalDatum());
		}
		if (this.letztesUfdRLFDatum != null) {
			aktuelleWerte.add(this.letztesUfdRLFDatum
					.getVeraendertesOriginalDatum());
		}

		return aktuelleWerte.toArray(new ResultData[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<UmfeldDatenArt> getDatenArten() {
		return datenArten;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loescheAlleWerte() {
		this.letztesUfdNIDatum = null;
		this.letztesUfdFBZDatum = null;
		this.letztesUfdRLFDatum = null;
		this.letztesUfdWFDDatum = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean sindAlleWerteFuerIntervallDa() {
		return this.letztesUfdNIDatum != null
				&& this.letztesUfdFBZDatum != null
				&& this.letztesUfdRLFDatum != null
				&& this.letztesUfdWFDDatum != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isPufferLeer() {
		return this.letztesUfdNIDatum == null
				&& this.letztesUfdFBZDatum == null
				&& this.letztesUfdRLFDatum == null
				&& this.letztesUfdWFDDatum == null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected UmfeldDatenSensorDatum getDatumBereitsInPosition(
			ResultData umfeldDatum) {
		UmfeldDatenSensorDatum datumInPosition = null;

		UmfeldDatenArt datenArt = UmfeldDatenArt
				.getUmfeldDatenArtVon(umfeldDatum.getObject());
		if (datenArt != null) {
			if (datenArt.equals(UmfeldDatenArt.ni)) {
				datumInPosition = this.letztesUfdNIDatum;
			} else if (datenArt.equals(UmfeldDatenArt.fbz)) {
				datumInPosition = this.letztesUfdFBZDatum;
			} else if (datenArt.equals(UmfeldDatenArt.rlf)) {
				datumInPosition = this.letztesUfdRLFDatum;
			} else if (datenArt.equals(UmfeldDatenArt.wfd)) {
				datumInPosition = this.letztesUfdWFDDatum;
			}
		}

		return datumInPosition;
	}

	/**
	 * Die Regeln aus SE-02.00.00.00.00-AFo-3.4
	 */

	/**
	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (NI > 0.5) <b>und</b> (WFD == 0) <b>und</b> (RLF > WFDgrenzNassRLF für Zeitraum > WFDminNassRLF)
	 * <b>dann</b> (WFD=implausibel)</code>
	 * <br>. Die Ergebnisse werden zurück in die lokalen Variablen geschrieben
	 */
	private void regel1() {
		if (this.letztesUfdNIDatum != null
				&& this.letztesUfdWFDDatum != null
				&& this.letztesUfdRLFDatum != null
				&& this.letztesUfdNIDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
				&& this.letztesUfdWFDDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
				&& this.letztesUfdRLFDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN) {
			if (this.parameterSensor.isInitialisiert()
					&& this.parameterSensor.getWFDgrenzNassNI().isOk()
					&& this.parameterSensor.getWFDgrenzNassRLF().isOk()) {
				if (this.letztesUfdNIDatum.getWert().getWert() > this.parameterSensor
						.getWFDgrenzNassNI().getWert()
						&& this.letztesUfdWFDDatum.getWert().getWert() == 0
						&& this.rlfUeberWfdgrenzNassFuerMS > this.parameterSensor
								.getWFDminNassRLF()) {
					this.letztesUfdWFDDatum
							.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					this.letztesUfdWFDDatum.getWert().setFehlerhaftAn();
					Debug.getLogger()
							.fine("[WFD.R1]Daten geändert:\n" + this.letztesUfdWFDDatum.toString()); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (WFD > 0) <b>und</b> (FBZ == trocken) <b>dann</b> (WFD=implausibel, FBZ=implausibel)</code>
	 * <br>. Die Ergebnisse werden zurück in die lokalen Variablen geschrieben
	 */
	private void regel2() {
		if (this.letztesUfdWFDDatum != null
				&& this.letztesUfdFBZDatum != null
				&& this.letztesUfdWFDDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
				&& this.letztesUfdFBZDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN) {
			if (this.letztesUfdWFDDatum.getWert().getWert() > 0
					&& this.letztesUfdFBZDatum.getWert().getWert() == 0) {
				this.letztesUfdWFDDatum
						.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				this.letztesUfdWFDDatum.getWert().setFehlerhaftAn();
				this.letztesUfdFBZDatum
						.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				this.letztesUfdFBZDatum.getWert().setFehlerhaftAn();
				Debug.getLogger()
						.fine("[WFD.R2]Daten geändert:\n" + this.letztesUfdWFDDatum.toString() + //$NON-NLS-1$ 
								"\n" + this.letztesUfdFBZDatum.toString()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (WFD == 0) <b>und</b> (FBZ == nass) <b>dann</b> (WFD=implausibel, FBZ=implausibel)</code>
	 * <br>. Die Ergebnisse werden zurück in die lokalen Variablen geschrieben
	 */
	private void regel3() {
		if (this.letztesUfdWFDDatum != null
				&& this.letztesUfdFBZDatum != null
				&& this.letztesUfdWFDDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
				&& this.letztesUfdFBZDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN) {
			if (this.letztesUfdWFDDatum.getWert().getWert() == 0
					&& this.letztesUfdFBZDatum.getWert().getWert() > 0) {
				this.letztesUfdWFDDatum
						.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				this.letztesUfdWFDDatum.getWert().setFehlerhaftAn();
				this.letztesUfdFBZDatum
						.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
				this.letztesUfdFBZDatum.getWert().setFehlerhaftAn();
				Debug.getLogger()
						.fine("[WFD.R3]Daten geändert:\n" + this.letztesUfdWFDDatum.toString() + //$NON-NLS-1$ 
								"\n" + this.letztesUfdFBZDatum.toString()); //$NON-NLS-1$
			}
		}
	}

}
