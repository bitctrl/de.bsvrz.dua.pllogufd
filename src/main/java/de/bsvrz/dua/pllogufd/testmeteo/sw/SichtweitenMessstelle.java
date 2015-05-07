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

package de.bsvrz.dua.pllogufd.testmeteo.sw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.DuplicateFormatFlagsException;
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
 * Analogon zur <code>SichtweitenTabelle</code> aus der Feinspezifikation mit
 * zugehörigen Funktionalitäten. In dieser Klasse wird je eine Messstelle mit
 * allen Sensoren, die für das Submodul "Sichtweiten" interessant sind
 * betrachtet. Die eigentliche Plausibilisierung wird innerhalb der Super-Klasse
 * <code>{@link AbstraktMeteoMessstelle}</code> über die Methode
 * <code>aktualisiereDaten(..)</code> durchgeführt.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public final class SichtweitenMessstelle extends AbstraktMeteoMessstelle {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Im Submodul Sichtweiten betrachtete Datenarten.
	 */
	private static Collection<UmfeldDatenArt> datenArten = new HashSet<UmfeldDatenArt>();
	static {
		SichtweitenMessstelle.datenArten.add(UmfeldDatenArt.sw);
		SichtweitenMessstelle.datenArten.add(UmfeldDatenArt.ns);
		SichtweitenMessstelle.datenArten.add(UmfeldDatenArt.rlf);
	}

	/**
	 * Zuordnung des Systemobjekts eines Umfelddatensensors zu einer Instanz
	 * dieser Klasse.
	 */
	private static Map<SystemObject, SichtweitenMessstelle> ufdsAufUfdMs = new HashMap<SystemObject, SichtweitenMessstelle>();

	/**
	 * letztes Scihtweite-Datum.
	 */
	private UmfeldDatenSensorDatum letztesUfdSWDatum = null;

	/**
	 * letztes Niederschlagsart-Datum.
	 */
	private UmfeldDatenSensorDatum letztesUfdNSDatum = null;

	/**
	 * letztes Datum der relativen Luftfeuchte.
	 */
	private UmfeldDatenSensorDatum letztesUfdRLFDatum = null;

	/**
	 * Parameter der Meteorologischen Kontrolle für den Sichtweiten-Sensor.
	 */
	private SichtweitenParameter parameterSensor = null;

	/**
	 * Standardkonstruktor.
	 *
	 * @param ufdmsObj
	 *            das Systemobjekt einer Umfelddaten-Messstelle
	 * @throws DUAInitialisierungsException
	 *             wenn die Umfelddaten-Messstelle nicht vollständig
	 *             initialisiert werden konnte (mit allen Sensoren usw.)
	 * @throws DuplicateFormatFlagsException
	 */
	private SichtweitenMessstelle(final SystemObject ufdmsObj)
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
						if (sensorMengeAnMessStelle.getElements().contains(
								betrachtetesObjekt)) {
							UmfeldDatenArt datenArt;
							try {
								datenArt = UmfeldDatenArt
										.getUmfeldDatenArtVon(betrachtetesObjekt);
							} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
								LOGGER.warning(	"Unbekannter Sensor (" + //$NON-NLS-1$
												betrachtetesObjekt
												+ ") an Messstelle " + ufdmsObj + ": " + e.getMessage()); //$NON-NLS-1$);
								continue;
							}
							
							if (datenArt == null) {
								throw new DUAInitialisierungsException(
										"Unbekannter Sensor (" + //$NON-NLS-1$
												betrachtetesObjekt
												+ ") an Messstelle " + ufdmsObj); //$NON-NLS-1$
							} else if (SichtweitenMessstelle.datenArten
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
		AbstraktMeteoMessstelle.setVerwaltungsModul(verwaltung);

		for (final SystemObject ufdmsObj : verwaltung.getVerbindung()
				.getDataModel()
				.getType("typ.umfeldDatenMessStelle").getElements()) { //$NON-NLS-1$
			if (ufdmsObj.isValid()) {
				final SichtweitenMessstelle messStelle = new SichtweitenMessstelle(
						ufdmsObj);
				if (messStelle.getSensoren().isEmpty()) {
					LOGGER.config(
							"Umfelddaten-Messstelle " + ufdmsObj + //$NON-NLS-1$
							" wird nicht betrachtet"); //$NON-NLS-1$
				} else {
					for (final SystemObject umfeldDatenSensor : messStelle
							.getSensoren()) {
						if (SichtweitenMessstelle.ufdsAufUfdMs
								.get(umfeldDatenSensor) != null) {
							throw new DUAInitialisierungsException(
									"Der Umfelddatensensor " + umfeldDatenSensor + //$NON-NLS-1$
									" ist gleichzeitig an mehr als einer Messstelle konfiguriert:\n" //$NON-NLS-1$
									+ SichtweitenMessstelle.ufdsAufUfdMs
									.get(umfeldDatenSensor)
									+ " und\n" + messStelle); //$NON-NLS-1$
						}
						SichtweitenMessstelle.ufdsAufUfdMs.put(
								umfeldDatenSensor, messStelle);
					}
					try {
						messStelle.initialisiereMessStelle();
					} catch (final NoSuchSensorException e) {
						LOGGER.config(
								"Umfelddaten-Messstelle " + ufdmsObj + //$NON-NLS-1$
								" wird nicht betrachtet"); //$NON-NLS-1$
						for (final SystemObject umfeldDatenSensor : messStelle
								.getSensoren()) {
							SichtweitenMessstelle.ufdsAufUfdMs
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
	public static SichtweitenMessstelle getMessStelleVonSensor(
			final SystemObject umfeldDatenSensorObj) {
		return SichtweitenMessstelle.ufdsAufUfdMs.get(umfeldDatenSensorObj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiereMessStelle()
			throws DUAInitialisierungsException, NoSuchSensorException {
		SystemObject parameterSensorObj = null;

		for (final SystemObject sensor : this.getSensoren()) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt
						.getUmfeldDatenArtVon(sensor);
			} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
				throw new NoSuchSensorException("An Messstelle " + this + //$NON-NLS-1$
						": " + e.getMessage());
			}
			if (datenArt.equals(UmfeldDatenArt.sw)) {
				parameterSensorObj = sensor;
				break;
			}
		}

		if (parameterSensorObj == null) {
			throw new NoSuchSensorException("An Messstelle " + this + //$NON-NLS-1$
					" konnte kein Sensor für Sichtweiten identifiziert werden"); //$NON-NLS-1$
		}

		try {
			this.parameterSensor = new SichtweitenParameter(
					AbstraktMeteoMessstelle.verwaltung, parameterSensorObj);
		} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
			throw new NoSuchSensorException("An Messstelle " + this + //$NON-NLS-1$
					" konnte kein Sensor für Sichtweiten identifiziert werden: " + e.getMessage()); //$NON-NLS-1$
		}
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
	protected boolean bringeDatumInPosition(final ResultData umfeldDatum) {
		boolean erfolgreich = false;

		if (umfeldDatum.getData() != null) {
			if (this.isDatumSpeicherbar(umfeldDatum)) {
				UmfeldDatenArt datenArt;
				try {
					datenArt = UmfeldDatenArt
							.getUmfeldDatenArtVon(umfeldDatum.getObject());
				} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
					LOGGER
					.error(
							this.getClass().getSimpleName()
							+ ", Datum nicht speicherbar:\n" + umfeldDatum + "(" + e.getMessage() + ")"); //$NON-NLS-1$
					return false;
				}

				if ((datenArt != null) && this.isDatumSpeicherbar(umfeldDatum)) {
					final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
							umfeldDatum);

					erfolgreich = true;
					if (datenArt.equals(UmfeldDatenArt.sw)) {
						this.letztesUfdSWDatum = datum;
					} else if (datenArt.equals(UmfeldDatenArt.ns)) {
						this.letztesUfdNSDatum = datum;
					} else if (datenArt.equals(UmfeldDatenArt.rlf)) {
						this.letztesUfdRLFDatum = datum;
					} else {
						erfolgreich = false;
					}

					if (erfolgreich) {
						this.aktuellerZeitstempel = umfeldDatum.getDataTime();
					}
				} else {
					LOGGER
					.warning(
							this.getClass().getSimpleName()
							+ ", Datum nicht speicherbar:\n" + umfeldDatum); //$NON-NLS-1$
				}
			} else {
				LOGGER.info(
						this.getClass().getSimpleName()
						+ ", Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
			}
		}

		return erfolgreich;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultData[] getAlleAktuellenWerte() {
		final List<ResultData> aktuelleWerte = new ArrayList<ResultData>();

		if (this.letztesUfdSWDatum != null) {
			aktuelleWerte.add(this.letztesUfdSWDatum
					.getVeraendertesOriginalDatum());
		}
		if (this.letztesUfdNSDatum != null) {
			aktuelleWerte.add(this.letztesUfdNSDatum
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
		return SichtweitenMessstelle.datenArten;
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
		return (this.letztesUfdSWDatum != null)
				&& (this.letztesUfdNSDatum != null)
				&& (this.letztesUfdRLFDatum != null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isPufferLeer() {
		return (this.letztesUfdSWDatum == null)
				&& (this.letztesUfdNSDatum == null)
				&& (this.letztesUfdRLFDatum == null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected UmfeldDatenSensorDatum getDatumBereitsInPosition(
			final ResultData umfeldDatum) {
		UmfeldDatenSensorDatum datumInPosition = null;

		UmfeldDatenArt datenArt;
		try {
			datenArt = UmfeldDatenArt
					.getUmfeldDatenArtVon(umfeldDatum.getObject());
		} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
			LOGGER
			.error(
					this.getClass().getSimpleName()
					+ ", Datum nicht speicherbar:\n" + umfeldDatum + "(" + e.getMessage() + ")"); //$NON-NLS-1$
			return null;
		}
		
		if (datenArt != null) {
			if (datenArt.equals(UmfeldDatenArt.sw)) {
				datumInPosition = this.letztesUfdSWDatum;
			} else if (datenArt.equals(UmfeldDatenArt.ns)) {
				datumInPosition = this.letztesUfdNSDatum;
			} else if (datenArt.equals(UmfeldDatenArt.rlf)) {
				datumInPosition = this.letztesUfdRLFDatum;
			}
		} else {
			LOGGER.info(
					this.getClass().getSimpleName()
					+ ", Unbekannte Datenart:\n" + umfeldDatum); //$NON-NLS-1$
		}

		return datumInPosition;
	}

	/**
	 * Die Regeln aus SE-02.00.00.00.00-AFo-3.4
	 */

	/**
	 * Folgende Regel wird abgearbeitet:<br>
	 * <code><b>Wenn</b> (SW &lt;= SWgrenz) <b>und</b> (NS == kein Niederschlag) <b>und</b> (RLF &lt; SWgrenzTrockenRLF)
	 * <b>dann</b> (SW=implausibel)</code> <br>
	 * . Die Ergebnisse werden zurück in die lokalen Variablen geschrieben
	 */
	private void regel1() {
		if ((this.letztesUfdSWDatum != null)
				&& (this.letztesUfdNSDatum != null)
				&& (this.letztesUfdRLFDatum != null)
				&& (this.letztesUfdSWDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)
						&& (this.letztesUfdNSDatum
								.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)
								&& (this.letztesUfdRLFDatum
										.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN)) {
			if (this.parameterSensor.isInitialisiert()
					&& this.parameterSensor.getSWgrenzSW().isOk()
					&& this.parameterSensor.getSWgrenzTrockenRLF().isOk()
					&& this.letztesUfdRLFDatum.getWert().isOk()
					&& this.letztesUfdSWDatum.getWert().isOk()) {
				if ((this.letztesUfdSWDatum.getWert().getWert() <= this.parameterSensor
						.getSWgrenzSW().getWert())
						&& (this.letztesUfdNSDatum.getWert().getWert() == 0)
						&& (this.letztesUfdRLFDatum.getWert().getWert() < this.parameterSensor
								.getSWgrenzTrockenRLF().getWert())) {
					this.letztesUfdSWDatum
					.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					this.letztesUfdSWDatum.getWert().setFehlerhaftAn();
					LOGGER
					.fine("[SW.R1]Daten geändert:\n" + this.letztesUfdSWDatum.toString()); //$NON-NLS-1$
				}
			}
		}
	}

}
