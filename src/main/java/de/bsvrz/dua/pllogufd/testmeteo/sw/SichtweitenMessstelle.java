/*
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.3 Pl-Pr�fung logisch UFD
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
 * Wei�enfelser Stra�e 67<br>
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
 * Analogon zur <code>SichtweitenTabelle</code> aus der Feinspezifikation mit
 * zugeh�rigen Funktionalit�ten. In dieser Klasse wird je eine Messstelle mit
 * allen Sensoren, die f�r das Submodul "Sichtweiten" interessant sind
 * betrachtet. Die eigentliche Plausibilisierung wird innerhalb der Super-Klasse
 * <code>{@link AbstraktMeteoMessstelle}</code> �ber die Methode
 * <code>aktualisiereDaten(..)</code> durchgef�hrt.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public final class SichtweitenMessstelle extends AbstraktMeteoMessstelle {

	/**
	 * Im Submodul Sichtweiten betrachtete Datenarten.
	 */
	private static Collection<UmfeldDatenArt> datenArten = new HashSet<UmfeldDatenArt>();
	static {
		datenArten.add(UmfeldDatenArt.sw);
		datenArten.add(UmfeldDatenArt.ns);
		datenArten.add(UmfeldDatenArt.rlf);
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
	 * Parameter der Meteorologischen Kontrolle f�r den Sichtweiten-Sensor.
	 */
	private SichtweitenParameter parameterSensor = null;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param ufdmsObj
	 *            das Systemobjekt einer Umfelddaten-Messstelle
	 * @throws DUAInitialisierungsException
	 *             wenn die Umfelddaten-Messstelle nicht vollst�ndig
	 *             initialisiert werden konnte (mit allen Sensoren usw.)
	 */
	private SichtweitenMessstelle(final SystemObject ufdmsObj)
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
				SichtweitenMessstelle messStelle = new SichtweitenMessstelle(
						ufdmsObj);
				if (messStelle.getSensoren().isEmpty()) {
					Debug.getLogger().config(
							"Umfelddaten-Messstelle " + ufdmsObj + //$NON-NLS-1$ 
									" wird nicht betrachtet"); //$NON-NLS-1$
				} else {
					for (SystemObject umfeldDatenSensor : messStelle
							.getSensoren()) {
						if (ufdsAufUfdMs.get(umfeldDatenSensor) != null) {
							throw new DUAInitialisierungsException(
									"Der Umfelddatensensor " + umfeldDatenSensor + //$NON-NLS-1$
											" ist gleichzeitig an mehr als einer Messstelle konfiguriert:\n" //$NON-NLS-1$
											+ ufdsAufUfdMs
													.get(umfeldDatenSensor)
											+ " und\n" + messStelle); //$NON-NLS-1$
						}
						ufdsAufUfdMs.put(umfeldDatenSensor, messStelle);
					}
					try {
						messStelle.initialisiereMessStelle();
					} catch (NoSuchSensorException e) {
						Debug.getLogger().config(
								"Umfelddaten-Messstelle " + ufdmsObj + //$NON-NLS-1$ 
										" wird nicht betrachtet"); //$NON-NLS-1$
						for (SystemObject umfeldDatenSensor : messStelle
								.getSensoren()) {
							ufdsAufUfdMs.remove(umfeldDatenSensor);
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
		return ufdsAufUfdMs.get(umfeldDatenSensorObj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiereMessStelle()
			throws DUAInitialisierungsException, NoSuchSensorException {
		SystemObject parameterSensorObj = null;

		for (SystemObject sensor : this.getSensoren()) {
			UmfeldDatenArt datenArt = UmfeldDatenArt
					.getUmfeldDatenArtVon(sensor);
			if (datenArt.equals(UmfeldDatenArt.sw)) {
				parameterSensorObj = sensor;
				break;
			}
		}

		if (parameterSensorObj == null) {
			throw new NoSuchSensorException("An Messstelle " + this + //$NON-NLS-1$
					" konnte kein Sensor f�r Sichtweiten identifiziert werden"); //$NON-NLS-1$
		}

		this.parameterSensor = new SichtweitenParameter(verwaltung,
				parameterSensorObj);
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

		if (umfeldDatum.getData() != null) {
			if (this.isDatumSpeicherbar(umfeldDatum)) {
				UmfeldDatenArt datenArt = UmfeldDatenArt
						.getUmfeldDatenArtVon(umfeldDatum.getObject());

				if (datenArt != null && this.isDatumSpeicherbar(umfeldDatum)) {
					UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
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
					Debug
							.getLogger()
							.warning(
									this.getClass().getSimpleName()
											+ ", Datum nicht speicherbar:\n" + umfeldDatum); //$NON-NLS-1$
				}
			} else {
				Debug.getLogger().info(
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
		List<ResultData> aktuelleWerte = new ArrayList<ResultData>();

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
		return datenArten;
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
		return this.letztesUfdSWDatum != null && this.letztesUfdNSDatum != null
				&& this.letztesUfdRLFDatum != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isPufferLeer() {
		return this.letztesUfdSWDatum == null && this.letztesUfdNSDatum == null
				&& this.letztesUfdRLFDatum == null;
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
			if (datenArt.equals(UmfeldDatenArt.sw)) {
				datumInPosition = this.letztesUfdSWDatum;
			} else if (datenArt.equals(UmfeldDatenArt.ns)) {
				datumInPosition = this.letztesUfdNSDatum;
			} else if (datenArt.equals(UmfeldDatenArt.rlf)) {
				datumInPosition = this.letztesUfdRLFDatum;
			}
		} else {
			Debug.getLogger().info(
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
	 * <code><b>Wenn</b> (SW <= SWgrenz) <b>und</b> (NS == kein Niederschlag) <b>und</b> (RLF < SWgrenzTrockenRLF)
	 * <b>dann</b> (SW=implausibel)</code>
	 * <br>. Die Ergebnisse werden zur�ck in die lokalen Variablen geschrieben
	 */
	private void regel1() {
		if (this.letztesUfdSWDatum != null
				&& this.letztesUfdNSDatum != null
				&& this.letztesUfdRLFDatum != null
				&& this.letztesUfdSWDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
				&& this.letztesUfdNSDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
				&& this.letztesUfdRLFDatum
						.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN) {
			if (this.parameterSensor.isInitialisiert()
					&& this.parameterSensor.getSWgrenzSW().isOk()
					&& this.parameterSensor.getSWgrenzTrockenRLF().isOk()
					&& this.letztesUfdRLFDatum.getWert().isOk()
					&& this.letztesUfdSWDatum.getWert().isOk()) {
				if (this.letztesUfdSWDatum.getWert().getWert() <= this.parameterSensor
						.getSWgrenzSW().getWert()
						&& this.letztesUfdNSDatum.getWert().getWert() == 0
						&& this.letztesUfdRLFDatum.getWert().getWert() < this.parameterSensor
								.getSWgrenzTrockenRLF().getWert()) {
					this.letztesUfdSWDatum
							.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					this.letztesUfdSWDatum.getWert().setFehlerhaftAn();
					Debug
							.getLogger()
							.fine(
									"[SW.R1]Daten ge�ndert:\n" + this.letztesUfdSWDatum.toString()); //$NON-NLS-1$
				}
			}
		}
	}

}