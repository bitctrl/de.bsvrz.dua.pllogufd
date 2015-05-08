/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.testmeteo.AbstraktMeteoMessstelle;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Analogon zur <code>NiederschlagsIntensitätsTabelle</code> aus der
 * Feinspezifikation mit zugehörigen Funktionalitäten. In dieser Klasse wird je
 * eine Messstelle mit allen Sensoren, die für das Submodul
 * "Niederschlagsintensität" interessant sind betrachtet. Die eigentliche
 * Plausibilisierung wird innerhalb der Super-Klasse
 * <code>{@link AbstraktMeteoMessstelle}</code> über die Methode
 * <code>aktualisiereDaten(..)</code> durchgeführt.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public final class NiederschlagsIntensitaetsMessstelle
extends AbstraktMeteoMessstelle {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Im Submodul Niederschlagsintensität betrachtete Datenarten.
	 */
	private static Collection<UmfeldDatenArt> datenArten = new HashSet<>();

	static {
		NiederschlagsIntensitaetsMessstelle.datenArten.add(UmfeldDatenArt.ns);
		NiederschlagsIntensitaetsMessstelle.datenArten.add(UmfeldDatenArt.ni);
		NiederschlagsIntensitaetsMessstelle.datenArten.add(UmfeldDatenArt.wfd);
		NiederschlagsIntensitaetsMessstelle.datenArten.add(UmfeldDatenArt.rlf);
	}

	/**
	 * Zuordnung des Systemobjekts eines Umfelddatensensors zu einer Instanz
	 * dieser Klasse.
	 */
	private static Map<SystemObject, NiederschlagsIntensitaetsMessstelle> ufdsAufUfdMs = new HashMap<>();

	/**
	 * letztes Niederschlagsintensitäts-Datum.
	 */
	private UmfeldDatenSensorDatum letztesUfdNIDatum = null;

	/**
	 * letztes Niederschlagsart-Datum.
	 */
	private UmfeldDatenSensorDatum letztesUfdNSDatum = null;

	/**
	 * letztes Datum der relativen Luftfeuchte.
	 */
	private UmfeldDatenSensorDatum letztesUfdRLFDatum = null;

	/**
	 * zählt die Millisekunden, die sich die relative Luftfeuchte schon
	 * unterhalb von <code>NIgrenzTrockenRLF</code> befindet.
	 */
	private long rlfUnterNIgrenzTrockenFuerMS = 0;

	/**
	 * letztes Datum der Wasserfilmdicke.
	 */
	private UmfeldDatenSensorDatum letztesUfdWFDDatum = null;

	/**
	 * Parameter der Meteorologischen Kontrolle für den
	 * Niederschlagsintensitäts-Sensor.
	 */
	private NiederschlagsIntensitaetsParameter parameterSensor = null;

	/**
	 * Standardkonstruktor.
	 *
	 * @param ufdmsObj
	 *            das Systemobjekt einer Umfelddaten-Messstelle
	 * @throws DUAInitialisierungsException
	 *             wenn die Umfelddaten-Messstelle nicht vollständig
	 *             initialisiert werden konnte (mit allen Sensoren usw.)
	 */
	private NiederschlagsIntensitaetsMessstelle(final SystemObject ufdmsObj)
			throws DUAInitialisierungsException {
		super(ufdmsObj);
		if (ufdmsObj instanceof ConfigurationObject) {
			final ConfigurationObject ufdmsConObj = (ConfigurationObject) ufdmsObj;
			final ObjectSet sensorMengeAnMessStelle = ufdmsConObj
					.getObjectSet("UmfeldDatenSensoren"); //$NON-NLS-1$

			if (sensorMengeAnMessStelle != null) {
				for (final SystemObject betrachtetesObjekt : AbstraktMeteoMessstelle.verwaltung
						.getSystemObjekte()) {
					if (betrachtetesObjekt.isValid()) {
						if (sensorMengeAnMessStelle.getElements()
								.contains(betrachtetesObjekt)) {
							UmfeldDatenArt datenArt;
							try {
								datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(
										betrachtetesObjekt);
							} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
								NiederschlagsIntensitaetsMessstelle.LOGGER
								.warning(e.getMessage());
								continue;
							}

							if (datenArt == null) {
								throw new DUAInitialisierungsException(
										"Unbekannter Sensor (" + //$NON-NLS-1$
												betrachtetesObjekt
												+ ") an Messstelle " //$NON-NLS-1$
												+ ufdmsObj);
							} else
								if (NiederschlagsIntensitaetsMessstelle.datenArten
										.contains(datenArt)) {
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
			throw new DUAInitialisierungsException(
					ufdmsObj + " ist kein Konfigurationsobjekt"); //$NON-NLS-1$
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
		AbstraktMeteoMessstelle.setVerwaltungsModul(verwaltung);

		for (final SystemObject ufdmsObj : verwaltung.getVerbindung()
				.getDataModel().getType("typ.umfeldDatenMessStelle") //$NON-NLS-1$
				.getElements()) {
			if (ufdmsObj.isValid()) {
				final NiederschlagsIntensitaetsMessstelle messStelle = new NiederschlagsIntensitaetsMessstelle(
						ufdmsObj);
				if (messStelle.getSensoren().isEmpty()) {
					NiederschlagsIntensitaetsMessstelle.LOGGER
					.config("Umfelddaten-Messstelle " + ufdmsObj + //$NON-NLS-1$
							" wird nicht betrachtet"); //$NON-NLS-1$
				} else {
					for (final SystemObject umfeldDatenSensor : messStelle
							.getSensoren()) {
						if (NiederschlagsIntensitaetsMessstelle.ufdsAufUfdMs
								.get(umfeldDatenSensor) != null) {
							throw new DUAInitialisierungsException(
									"Der Umfelddatensensor " + umfeldDatenSensor //$NON-NLS-1$
									+ " ist gleichzeitig an mehr als einer Messstelle konfiguriert:\n"
									+ NiederschlagsIntensitaetsMessstelle.ufdsAufUfdMs
									.get(umfeldDatenSensor)
									+ " und\n" + messStelle); //$NON-NLS-1$
						}
						NiederschlagsIntensitaetsMessstelle.ufdsAufUfdMs
						.put(umfeldDatenSensor, messStelle);
					}
					try {
						messStelle.initialisiereMessStelle();
					} catch (final NoSuchSensorException e) {
						NiederschlagsIntensitaetsMessstelle.LOGGER
						.config("Umfelddaten-Messstelle " + ufdmsObj + //$NON-NLS-1$
								" wird nicht betrachtet"); //$NON-NLS-1$
						for (final SystemObject umfeldDatenSensor : messStelle
								.getSensoren()) {
							NiederschlagsIntensitaetsMessstelle.ufdsAufUfdMs
									.remove(umfeldDatenSensor);
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
	public static NiederschlagsIntensitaetsMessstelle getMessStelleVonSensor(
			final SystemObject umfeldDatenSensorObj) {
		return NiederschlagsIntensitaetsMessstelle.ufdsAufUfdMs
				.get(umfeldDatenSensorObj);
	}

	@Override
	protected void initialisiereMessStelle()
			throws DUAInitialisierungsException, NoSuchSensorException {
		SystemObject parameterSensorObj = null;

		for (final SystemObject sensor : this.getSensoren()) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				NiederschlagsIntensitaetsMessstelle.LOGGER
				.warning(e.getMessage());
				continue;
			}
			if (datenArt.equals(UmfeldDatenArt.ni)) {
				parameterSensorObj = sensor;
				break;
			}
		}

		if (parameterSensorObj == null) {
			throw new NoSuchSensorException("An Messstelle " + this + //$NON-NLS-1$
					" konnte kein Sensor für Niederschlagsintensität identifiziert werden"); //$NON-NLS-1$
		}

		try {
			this.parameterSensor = new NiederschlagsIntensitaetsParameter(
					AbstraktMeteoMessstelle.verwaltung, parameterSensorObj);
		} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
			throw new NoSuchSensorException("An Messstelle " + this + //$NON-NLS-1$
					" konnte kein Sensor für Niederschlagsintensität identifiziert werden: " //$NON-NLS-1$
					+ e.getMessage());
		}
	}

	@Override
	protected ResultData[] berechneAlleRegeln() {
		/**
		 * Gerhard Kappich (19.07.2007) Bei der Prüfung ist mir aufgefallen,
		 * dass die 2. Regel zur Niederschlagsart der 1. Regel zur
		 * Niederschlagsintensität entspricht und somit die Regel zur
		 * Niederschlagsintensität wegfallen kann
		 */
		// regel1();
		regel2();
		regel3();
		return this.getAlleAktuellenWerte();
	}

	@Override
	protected boolean bringeDatumInPosition(final ResultData umfeldDatum) {
		boolean erfolgreich = false;

		if (umfeldDatum.getData() != null) {
			if (this.isDatumSpeicherbar(umfeldDatum)) {
				UmfeldDatenArt datenArt;
				try {
					datenArt = UmfeldDatenArt
							.getUmfeldDatenArtVon(umfeldDatum.getObject());
				} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
					NiederschlagsIntensitaetsMessstelle.LOGGER
					.warning(e.getMessage());
					return false;
				}

				if ((datenArt != null)
						&& this.isDatumSpeicherbar(umfeldDatum)) {
					final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
							umfeldDatum);

					erfolgreich = true;
					if (datenArt.equals(UmfeldDatenArt.ni)) {
						this.letztesUfdNIDatum = datum;
					} else if (datenArt.equals(UmfeldDatenArt.ns)) {
						this.letztesUfdNSDatum = datum;
					} else if (datenArt.equals(UmfeldDatenArt.rlf)) {
						this.letztesUfdRLFDatum = datum;
						if (datum.getWert().isOk()
								&& this.parameterSensor.isInitialisiert()
								&& this.parameterSensor.getNIGrenzTrockenRLF()
								.isOk()
								&& (datum.getWert()
										.getWert() < this.parameterSensor
										.getNIGrenzTrockenRLF()
										.getWert())) {
							this.rlfUnterNIgrenzTrockenFuerMS += datum.getT();
						} else {
							this.rlfUnterNIgrenzTrockenFuerMS = 0;
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
					NiederschlagsIntensitaetsMessstelle.LOGGER
					.warning(this.getClass().getSimpleName()
							+ ", Datum nicht speicherbar:\n" //$NON-NLS-1$
							+ umfeldDatum);
				}
			} else {
				NiederschlagsIntensitaetsMessstelle.LOGGER
				.info(this.getClass().getSimpleName()
						+ ", Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
			}
		}

		return erfolgreich;
	}

	@Override
	protected ResultData[] getAlleAktuellenWerte() {
		final List<ResultData> aktuelleWerte = new ArrayList<>();

		if (this.letztesUfdNIDatum != null) {
			aktuelleWerte
			.add(this.letztesUfdNIDatum.getVeraendertesOriginalDatum());
		}
		if (this.letztesUfdNSDatum != null) {
			aktuelleWerte
			.add(this.letztesUfdNSDatum.getVeraendertesOriginalDatum());
		}
		if (this.letztesUfdWFDDatum != null) {
			aktuelleWerte.add(
					this.letztesUfdWFDDatum.getVeraendertesOriginalDatum());
		}
		if (this.letztesUfdRLFDatum != null) {
			aktuelleWerte.add(
					this.letztesUfdRLFDatum.getVeraendertesOriginalDatum());
		}

		return aktuelleWerte.toArray(new ResultData[0]);
	}

	@Override
	protected Collection<UmfeldDatenArt> getDatenArten() {
		return NiederschlagsIntensitaetsMessstelle.datenArten;
	}

	@Override
	protected void loescheAlleWerte() {
		this.letztesUfdNIDatum = null;
		this.letztesUfdNSDatum = null;
		this.letztesUfdRLFDatum = null;
		this.letztesUfdWFDDatum = null;
	}

	@Override
	protected UmfeldDatenSensorDatum getDatumBereitsInPosition(
			final ResultData umfeldDatum) {
		UmfeldDatenSensorDatum datumInPosition = null;

		UmfeldDatenArt datenArt;
		try {
			datenArt = UmfeldDatenArt
					.getUmfeldDatenArtVon(umfeldDatum.getObject());
		} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
			NiederschlagsIntensitaetsMessstelle.LOGGER.warning(e.getMessage());
			return null;
		}

		if (datenArt != null) {
			if (datenArt.equals(UmfeldDatenArt.ni)) {
				datumInPosition = this.letztesUfdNIDatum;
			} else if (datenArt.equals(UmfeldDatenArt.ns)) {
				datumInPosition = this.letztesUfdNSDatum;
			} else if (datenArt.equals(UmfeldDatenArt.rlf)) {
				datumInPosition = this.letztesUfdRLFDatum;
			} else if (datenArt.equals(UmfeldDatenArt.wfd)) {
				datumInPosition = this.letztesUfdWFDDatum;
			}
		} else {
			NiederschlagsIntensitaetsMessstelle.LOGGER
			.info(this.getClass().getSimpleName()
					+ ", Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
		}

		return datumInPosition;
	}

	@Override
	protected boolean sindAlleWerteFuerIntervallDa() {
		return (this.letztesUfdNIDatum != null)
				&& (this.letztesUfdNSDatum != null)
				&& (this.letztesUfdRLFDatum != null)
				&& (this.letztesUfdWFDDatum != null);
	}

	@Override
	protected boolean isPufferLeer() {
		return (this.letztesUfdNIDatum == null)
				&& (this.letztesUfdNSDatum == null)
				&& (this.letztesUfdRLFDatum == null)
				&& (this.letztesUfdWFDDatum == null);
	}

	/**
	 * Die Regeln aus SE-02.00.00.00.00-AFo-3.4
	 */

	// /**
	// * Folgende Regel wird abgearbeitet:<br>
	// * <code><b>Wenn</b> (NI > 0) <b>und</b> (NS == kein Niederschlag)
	// <b>dann</b> (NS=implausibel, NI=implausibel)</code>
	// * <br>Die Ergebnisse werden zurück in die lokalen Variablen geschrieben
	// */
	// private final void regel1(){
	// if(this.letztesUfdNIDatum != null &&
	// this.letztesUfdNSDatum != null &&
	// this.letztesUfdNIDatum.getDatenZeit() ==
	// this.letztesUfdNSDatum.getDatenZeit() &&
	// this.letztesUfdNIDatum.getStatusMessWertErsetzungImplausibel() ==
	// DUAKonstanten.NEIN &&
	// this.letztesUfdNSDatum.getStatusMessWertErsetzungImplausibel() ==
	// DUAKonstanten.NEIN){
	// if(this.letztesUfdNIDatum.getWert().getWert() > 0 &&
	// this.letztesUfdNSDatum.getWert().getWert() == 0){
	// this.letztesUfdNIDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
	// this.letztesUfdNIDatum.getWert().setFehlerhaftAn();
	// this.letztesUfdNSDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
	// this.letztesUfdNSDatum.getWert().setFehlerhaftAn();
	// LOGGER.fine("[NI.R1]Daten geändert:\n" +
	// this.letztesUfdNIDatum.toString() + //$NON-NLS-1$
	// "\n" + this.letztesUfdNSDatum.toString()); //$NON-NLS-1$
	// }
	// }
	// }
	/**
	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (NS == Niederschlag) <b>und</b> (NI &gt; NIminNI) <b>und</b> (RLF &lt; NIgrenzTrockenRLF)
	 * <b>dann</b> (NI=implausibel)</code> <br>
	 * . Die Ergebnisse werden zurück in die lokalen Variablen geschrieben
	 */
	private void regel2() {
		if ((this.letztesUfdNIDatum != null) && (this.letztesUfdNSDatum != null)
				&& (this.letztesUfdRLFDatum != null)
				&& (this.letztesUfdNIDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)
						&& (this.letztesUfdNSDatum
								.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)
								&& (this.letztesUfdRLFDatum
										.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)) {
			if (this.parameterSensor.isInitialisiert()
					&& this.parameterSensor.getNIminNI().isOk()
					&& this.parameterSensor.getNIGrenzTrockenRLF().isOk()
					&& this.letztesUfdRLFDatum.getWert().isOk()) {
				if ((this.letztesUfdNSDatum.getWert().getWert() == 0)
						&& (this.letztesUfdNIDatum.getWert()
								.getWert() > this.parameterSensor.getNIminNI()
								.getWert())
								&& (this.letztesUfdRLFDatum.getWert()
										.getWert() < this.parameterSensor
										.getNIGrenzTrockenRLF().getWert())) {
					this.letztesUfdNIDatum
					.setStatusMessWertErsetzungImplausibel(
							DUAKonstanten.JA);
					this.letztesUfdNIDatum.getWert().setFehlerhaftAn();
					NiederschlagsIntensitaetsMessstelle.LOGGER
					.fine("[NI.R2]Daten geändert:\n" //$NON-NLS-1$
							+ this.letztesUfdNIDatum.toString());
				}
			}
		}
	}

	/**
	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (NI &gt; 0.5) <b>und</b> (WFD == 0) <b>und</b> (RLF &gt; WFDgrenzNassPLF für Zeitraum &gt; WFDminNassRLF)
	 * <b>dann</b> (NI=implausibel)</code> <br>
	 * . Die Ergebnisse werden zurück in die lokalen Variablen geschrieben
	 */
	private void regel3() {
		if ((this.letztesUfdNIDatum != null)
				&& (this.letztesUfdWFDDatum != null)
				&& (this.letztesUfdRLFDatum != null)
				&& (this.letztesUfdNIDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)
						&& (this.letztesUfdWFDDatum
								.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)
								&& (this.letztesUfdRLFDatum
										.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)) {
			if (this.parameterSensor.isInitialisiert()
					&& this.parameterSensor.getNIGrenzNassNI().isOk()
					&& this.parameterSensor.getNIGrenzTrockenRLF().isOk()
					&& this.letztesUfdRLFDatum.getWert().isOk()) {
				if ((this.letztesUfdNIDatum.getWert()
						.getWert() > this.parameterSensor.getNIGrenzNassNI()
						.getWert())
						&& (this.letztesUfdWFDDatum.getWert().getWert() == 0)
						&& (this.rlfUnterNIgrenzTrockenFuerMS > this.parameterSensor
								.getNIminTrockenRLF())) {
					this.letztesUfdNIDatum
					.setStatusMessWertErsetzungImplausibel(
							DUAKonstanten.JA);
					this.letztesUfdNIDatum.getWert().setFehlerhaftAn();
					NiederschlagsIntensitaetsMessstelle.LOGGER
					.fine("[NI.R3]Daten geändert:\n" //$NON-NLS-1$
							+ this.letztesUfdNIDatum.toString());
				}
			}
		}
	}

}
