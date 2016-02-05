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

package de.bsvrz.dua.pllogufd.testmeteo.wfd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.testmeteo.AbstraktMeteoMessstelle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolle;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
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
 * @author BitCtrl Systems GmbH, Thierfelder, A. Uhlmann
 * @version $Id$
 */
public final class WasserfilmDickeMessstelle extends AbstraktMeteoMessstelle {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Im Submodul WasserfilmDicke betrachtete Datenarten.
	 */
	private static Collection<UmfeldDatenArt> datenArten = new HashSet<>();

	static {
		WasserfilmDickeMessstelle.datenArten.add(UmfeldDatenArt.fbz);
		WasserfilmDickeMessstelle.datenArten.add(UmfeldDatenArt.ni);
		WasserfilmDickeMessstelle.datenArten.add(UmfeldDatenArt.wfd);
		WasserfilmDickeMessstelle.datenArten.add(UmfeldDatenArt.rlf);
	}

	/**
	 * Zuordnung des Systemobjekts eines Umfelddatensensors zu einer Instanz
	 * dieser Klasse.
	 */
	private static Map<SystemObject, WasserfilmDickeMessstelle> ufdsAufUfdMs = new HashMap<>();

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
		final DUAUmfeldDatenMessStelle duaufdms = DUAUmfeldDatenMessStelle.getInstanz(ufdmsObj);
		final Collection<SystemObject> betrachteteObjekte = Arrays.asList(AbstraktMeteoMessstelle.verwaltung
						.getSystemObjekte());
		for (final DUAUmfeldDatenSensor sensor : duaufdms.getSensoren()) {
			// M.E. kann das weg, da bei der Initialisierung der DUAUmfeldDatenMessStelle
			// nur die zu betrachtenden Messstellen geladen werden!
			if (!betrachteteObjekte.contains(sensor.getObjekt())) {
				WasserfilmDickeMessstelle.LOGGER.config("Sensor '" + sensor
						+ "' an Messstelle '" + duaufdms.getObjekt() + "' wird nicht betrachtet");
				continue;
			}
			try {
				final UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor.getObjekt());
				if (WasserfilmDickeMessstelle.datenArten.contains(datenArt)) {
					if (sensor.isHauptSensor() || !MeteorologischeKontrolle.getNurHauptsensoren()) {
						WasserfilmDickeMessstelle.LOGGER.config("Sensor '" + sensor
								+ "' an Messstelle '" + duaufdms.getObjekt() + "' wird benutzt!");
						sensorenAnMessStelle.add(sensor.getObjekt());
					} else {
						WasserfilmDickeMessstelle.LOGGER.warning("Sensor '" + sensor
								+ "' an Messstelle '" + duaufdms.getObjekt() + "' wird nicht benutzt, da er der Nebensensor ist");
					}
				} else {
					WasserfilmDickeMessstelle.LOGGER.fine("Sensor '" + sensor
							+ "' an Messstelle '" + duaufdms.getObjekt() + "' wird nicht benötigt");
				}
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				WasserfilmDickeMessstelle.LOGGER.warning(e.getMessage());
				continue;
			}
		}
		LOGGER.config("Konstruiert: " + this);
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

		for (final DUAUmfeldDatenMessStelle duaufdms : DUAUmfeldDatenMessStelle.getInstanzen()) {
			final WasserfilmDickeMessstelle messStelle = new WasserfilmDickeMessstelle(
					duaufdms.getObjekt());
			if (messStelle.getSensoren().isEmpty()) {
				WasserfilmDickeMessstelle.LOGGER
						.config("Umfelddaten-Messstelle " + duaufdms + //$NON-NLS-1$
								" wird nicht betrachtet"); //$NON-NLS-1$
			} else {
				for (final DUAUmfeldDatenSensor duaSensor : duaufdms.getSensoren()) {
					final SystemObject umfeldDatenSensor = duaSensor.getObjekt();
					if (!duaSensor.isHauptSensor()) {
						LOGGER.config("Der Sensor '" + umfeldDatenSensor.getPid() + "' an Messstelle '" + duaufdms.getObjekt().getPid()
								+ "' wird an der korrespondierenden WFD-Messstelle nicht benutzt, da er nicht der Hauptsensor ist.");
						continue;
					}
					if (WasserfilmDickeMessstelle.ufdsAufUfdMs
							.get(umfeldDatenSensor) != null) {
						throw new DUAInitialisierungsException(
								"Der Umfelddatensensor " + umfeldDatenSensor //$NON-NLS-1$
										+
										" ist gleichzeitig an mehr als einer Messstelle konfiguriert:\n"
										+ WasserfilmDickeMessstelle.ufdsAufUfdMs
												.get(umfeldDatenSensor)
										+ " und\n" + messStelle); //$NON-NLS-1$
					}
					WasserfilmDickeMessstelle.ufdsAufUfdMs
							.put(umfeldDatenSensor, messStelle);
				}
				try {
					messStelle.initialisiereMessStelle();
				} catch (final NoSuchSensorException e) {
					WasserfilmDickeMessstelle.LOGGER
							.config("Umfelddaten-Messstelle " + duaufdms + //$NON-NLS-1$
									" wird nicht betrachtet"); //$NON-NLS-1$
					for (final SystemObject umfeldDatenSensor : messStelle
							.getSensoren()) {
						WasserfilmDickeMessstelle.ufdsAufUfdMs
						.remove(umfeldDatenSensor);
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
		return WasserfilmDickeMessstelle.ufdsAufUfdMs.get(umfeldDatenSensorObj);
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
				WasserfilmDickeMessstelle.LOGGER.warning(e.getMessage());
				continue;
			}
			if (datenArt.equals(UmfeldDatenArt.wfd)) {
				parameterSensorObj = sensor;
				break;
			}
		}

		if (parameterSensorObj == null) {
			throw new NoSuchSensorException("An Messstelle " + this + //$NON-NLS-1$
					" konnte kein Sensor für Wasserfilmdicke identifiziert werden"); //$NON-NLS-1$
		}

		try {
			this.parameterSensor = new WasserFilmDickeParameter(
					AbstraktMeteoMessstelle.verwaltung, parameterSensorObj);
		} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
			throw new NoSuchSensorException("An Messstelle " + this + //$NON-NLS-1$
					": " + e.getMessage());
		}
	}

	@Override
	protected ResultData[] berechneAlleRegeln() {
		regel1();
		regel2();
		regel3();
		return this.getAlleAktuellenWerte();
	}

	@Override
	protected boolean bringeDatumInPosition(final ResultData umfeldDatum) {
		boolean erfolgreich = false;

		if (umfeldDatum.getData() != null) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt
						.getUmfeldDatenArtVon(umfeldDatum.getObject());
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				WasserfilmDickeMessstelle.LOGGER.warning(e.getMessage());
				return false;
			}

			if (datenArt != null) {
				if (this.isDatumSpeicherbar(umfeldDatum)) {
					final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
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
								&& (datum.getWert()
										.getWert() > this.parameterSensor
												.getWFDgrenzNassRLF()
												.getWert())) {
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
					WasserfilmDickeMessstelle.LOGGER
							.warning(this.getClass().getSimpleName()
									+ ", Datum nicht speicherbar:\n" //$NON-NLS-1$
									+ umfeldDatum);
				}
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
		if (this.letztesUfdFBZDatum != null) {
			aktuelleWerte.add(
					this.letztesUfdFBZDatum.getVeraendertesOriginalDatum());
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
		return WasserfilmDickeMessstelle.datenArten;
	}

	@Override
	protected void loescheAlleWerte() {
		this.letztesUfdNIDatum = null;
		this.letztesUfdFBZDatum = null;
		this.letztesUfdRLFDatum = null;
		this.letztesUfdWFDDatum = null;
	}

	@Override
	protected boolean sindAlleWerteFuerIntervallDa() {
		return (this.letztesUfdNIDatum != null)
				&& (this.letztesUfdFBZDatum != null)
				&& (this.letztesUfdRLFDatum != null)
				&& (this.letztesUfdWFDDatum != null);
	}

	@Override
	protected boolean isPufferLeer() {
		return (this.letztesUfdNIDatum == null)
				&& (this.letztesUfdFBZDatum == null)
				&& (this.letztesUfdRLFDatum == null)
				&& (this.letztesUfdWFDDatum == null);
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
			WasserfilmDickeMessstelle.LOGGER.warning(e.getMessage());
			return null;
		}

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
	 * <code><b>Wenn</b> (NI &gt; 0.5) <b>und</b> (WFD == 0) <b>und</b> (RLF &gt; WFDgrenzNassRLF für Zeitraum &gt; WFDminNassRLF)
	 * <b>dann</b> (WFD=implausibel)</code> <br>
	 * . Die Ergebnisse werden zurück in die lokalen Variablen geschrieben
	 */
	private void regel1() {
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
					&& this.parameterSensor.getWFDgrenzNassNI().isOk()
					&& this.parameterSensor.getWFDgrenzNassRLF().isOk()) {
				if ((this.letztesUfdNIDatum.getWert()
						.getWert() > this.parameterSensor.getWFDgrenzNassNI()
								.getWert())
						&& (this.letztesUfdWFDDatum.getWert().getWert() == 0)
						&& (this.rlfUeberWfdgrenzNassFuerMS > this.parameterSensor
								.getWFDminNassRLF())) {
					this.letztesUfdWFDDatum
							.setStatusMessWertErsetzungImplausibel(
									DUAKonstanten.JA);
					this.letztesUfdWFDDatum.getWert().setFehlerhaftAn();
					WasserfilmDickeMessstelle.LOGGER
							.fine("[WFD.R1]Daten geändert:\n" //$NON-NLS-1$
									+ this.letztesUfdWFDDatum.toString());
				}
			}
		}
	}

	/**
	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (WFD &gt; 0) <b>und</b> (FBZ == trocken) <b>dann</b> (WFD=implausibel, FBZ=implausibel)</code>
	 * <br>
	 * . Die Ergebnisse werden zurück in die lokalen Variablen geschrieben
	 */
	private void regel2() {
		if ((this.letztesUfdWFDDatum != null)
				&& (this.letztesUfdFBZDatum != null)
				&& (this.letztesUfdWFDDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)
				&& (this.letztesUfdFBZDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)) {
			if ((this.letztesUfdWFDDatum.getWert().getWert() > 0)
					&& (this.letztesUfdFBZDatum.getWert().getWert() == 0)) {
				this.letztesUfdWFDDatum.setStatusMessWertErsetzungImplausibel(
						DUAKonstanten.JA);
				this.letztesUfdWFDDatum.getWert().setFehlerhaftAn();
				this.letztesUfdFBZDatum.setStatusMessWertErsetzungImplausibel(
						DUAKonstanten.JA);
				this.letztesUfdFBZDatum.getWert().setFehlerhaftAn();
				WasserfilmDickeMessstelle.LOGGER
						.fine("[WFD.R2]Daten geändert:\n" //$NON-NLS-1$
								+ this.letztesUfdWFDDatum.toString() +
								"\n" + this.letztesUfdFBZDatum.toString());
			}
		}
	}

	/**
	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (WFD == 0) <b>und</b> (FBZ == nass) <b>dann</b> (WFD=implausibel, FBZ=implausibel)</code>
	 * <br>
	 * . Die Ergebnisse werden zurück in die lokalen Variablen geschrieben
	 */
	private void regel3() {
		if ((this.letztesUfdWFDDatum != null)
				&& (this.letztesUfdFBZDatum != null)
				&& (this.letztesUfdWFDDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)
				&& (this.letztesUfdFBZDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)) {
			if ((this.letztesUfdWFDDatum.getWert().getWert() == 0)
					&& (this.letztesUfdFBZDatum.getWert().getWert() > 0)) {
				this.letztesUfdWFDDatum.setStatusMessWertErsetzungImplausibel(
						DUAKonstanten.JA);
				this.letztesUfdWFDDatum.getWert().setFehlerhaftAn();
				this.letztesUfdFBZDatum.setStatusMessWertErsetzungImplausibel(
						DUAKonstanten.JA);
				this.letztesUfdFBZDatum.getWert().setFehlerhaftAn();
				WasserfilmDickeMessstelle.LOGGER
						.fine("[WFD.R3]Daten geändert:\n" //$NON-NLS-1$
								+ this.letztesUfdWFDDatum.toString() +
								"\n" + this.letztesUfdFBZDatum.toString());
			}
		}
	}

}
