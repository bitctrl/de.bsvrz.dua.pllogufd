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

package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.TestUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Super-Klasse für alle Tests der Meteorologischen Kontrolle. Sendet
 * Standardparameter und meldet sich als Empfänger auf alle Umfelddaten unter
 * dem Aspekt <code>asp.plausibilitätsPrüfungLogisch</code> an
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class MeteorologischeKontrolleTest
implements ClientSenderInterface, ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Debug-Ausgaben über Standardausgabe.
	 */
	protected static final boolean DEBUG = false;

	/**
	 * Sollen die <code>Assert</code>-Statements ausgeführt werden?
	 */
	protected static final boolean TEST_AN = true;

	/**
	 * Zum Ordnen der Systemobjekte nach ihrem Namen.
	 */
	private static final Comparator<SystemObject> C = new Comparator<SystemObject>() {
		@Override
		public int compare(final SystemObject so1, final SystemObject so2) {
			return so1.getName().compareTo(so2.getName());
		}
	};

	/**
	 * Datenverteiler-Verbindung.
	 */
	protected ClientDavInterface dav = null;

	/**
	 * alle NS-Sensoren.
	 */
	protected SortedSet<SystemObject> nsSensoren = new TreeSet<>(
			MeteorologischeKontrolleTest.C);

	/**
	 * alle NI-Sensoren.
	 */
	protected SortedSet<SystemObject> niSensoren = new TreeSet<>(
			MeteorologischeKontrolleTest.C);

	/**
	 * alle LT-Sensoren.
	 */
	protected SortedSet<SystemObject> ltSensoren = new TreeSet<>(
			MeteorologischeKontrolleTest.C);

	/**
	 * alle RLF-Sensoren.
	 */
	protected SortedSet<SystemObject> rlfSensoren = new TreeSet<>(
			MeteorologischeKontrolleTest.C);

	/**
	 * alle WFD-Sensoren.
	 */
	protected SortedSet<SystemObject> wfdSensoren = new TreeSet<>(
			MeteorologischeKontrolleTest.C);

	/**
	 * alle SW-Sensoren.
	 */
	protected SortedSet<SystemObject> swSensoren = new TreeSet<>(
			MeteorologischeKontrolleTest.C);

	/**
	 * alle FBZ-Sensoren.
	 */
	protected SortedSet<SystemObject> fbzSensoren = new TreeSet<>(
			MeteorologischeKontrolleTest.C);

	/**
	 * alle Sensoren, für die innerhalb dieses Tests nur Werte mit dem Status
	 * "fehlerhaft" gesendet werden müssen, um das Ergebnis nicht zu
	 * beeinflussen.
	 */
	protected HashSet<SystemObject> restSensoren = new HashSet<>();

	/**
	 * letzter Ist-Ergebnis-Wert von einem Sensor.
	 */
	protected Map<SystemObject, MeteoErgebnis> ergebnisIst = new HashMap<>();

	/**
	 * Standardkonstruktor.
	 *
	 * @throws Exception
	 *             leitet die Ausnahmen weiter
	 */
	public MeteorologischeKontrolleTest() throws Exception {
		this.dav = DAVTest.getDav(PlPruefungLogischUFDTest.CON_DATA);

		DUAUtensilien.setAlleParameter(dav);

		PlPruefungLogischUFDTest.initialisiere();
		PlPruefungLogischUFDTest.sender.setMeteoKontrolle(true);

		/**
		 * Setzte Ausfallüberwachung für alle Sensoren AUS Differentialkontrolle
		 * auf einen harmlosen Wert Anstieg-Abfall-Kontrolle aus
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor, -1);
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht geprüft: " + e.getMessage());
				continue;
			}
			if (!datenArt.equals(UmfeldDatenArt.fbz)) {
				PlPruefungLogischUFDTest.sender.setDiffPara(sensor, 5,
						Constants.MILLIS_PER_HOUR);
			}
			if (!datenArt.equals(UmfeldDatenArt.fbz)) {
				PlPruefungLogischUFDTest.sender.setAnAbPara(sensor, 5);
			}
		}

		/**
		 * Anmeldung auf alle Daten die aus der Applikation Pl-Prüfung logisch
		 * UFD kommen
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht geprüft: " + e.getMessage());
				continue;
			}
			final DataDescription datenBeschreibung = new DataDescription(
					dav.getDataModel()
					.getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel()
					.getAspect("asp.plausibilitätsPrüfungLogisch")); //$NON-NLS-1$
			dav.subscribeReceiver(this, sensor, datenBeschreibung,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
		}

		/**
		 * Initialisiere alle Objektmengen
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht geprüft: " + e.getMessage());
				continue;
			}
			if (datenArt.equals(UmfeldDatenArt.ni)) {
				this.niSensoren.add(sensor);
			}
			if (datenArt.equals(UmfeldDatenArt.ns)) {
				this.nsSensoren.add(sensor);
			}
			if (datenArt.equals(UmfeldDatenArt.wfd)) {
				this.wfdSensoren.add(sensor);
			}
			if (datenArt.equals(UmfeldDatenArt.sw)) {
				this.swSensoren.add(sensor);
			}
			if (datenArt.equals(UmfeldDatenArt.fbz)) {
				this.fbzSensoren.add(sensor);
			}
			if (datenArt.equals(UmfeldDatenArt.rlf)) {
				this.rlfSensoren.add(sensor);
			}
			if (datenArt.equals(UmfeldDatenArt.lt)) {
				this.ltSensoren.add(sensor);
			}
		}

		/**
		 * Warte eine Sekunde bis alle Anmeldungen durch sind
		 */
		try {
			Thread.sleep(1000L);
		} catch (final InterruptedException ex) {
			//
		}
	}

	@Override
	public void dataRequest(final SystemObject object,
			final DataDescription dataDescription, final byte state) {
		//
	}

	@Override
	public boolean isRequestSupported(final SystemObject object,
			final DataDescription dataDescription) {
		return false;
	}

	/**
	 * Sendet einen Sensorwert.
	 *
	 * @param sensor
	 *            der Umfelddatensensor
	 * @param wert
	 *            der zu sendende Wert
	 * @param datenZeitStempel
	 *            der Datenzeitstempel
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public final void sendeDatum(final SystemObject sensor, final double wert,
			final long datenZeitStempel)
					throws UmfeldDatenSensorUnbekannteDatenartException {
		final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
				TestUtensilien.getExterneErfassungDatum(sensor));
		datum.setT(PlPruefungLogischUFDTest.STANDARD_T);
		datum.getWert().setSkaliertenWert(wert);
		final ResultData resultat = new ResultData(sensor,
				datum.getOriginalDatum().getDataDescription(), datenZeitStempel,
				datum.getDatum());
		try {
			PlPruefungLogischUFDTest.sender.sende(resultat);
		} catch (final Exception e) {
			e.printStackTrace();
			MeteorologischeKontrolleTest.LOGGER.error(Constants.EMPTY_STRING,
					e);
		}
	}

	/**
	 * Sendet einen Sensorwert.
	 *
	 * @param sensoren
	 *            eine Menge von Umfelddatensensoren
	 * @param wert
	 *            der zu sendende Wert
	 * @param datenZeitStempel
	 *            der Datenzeitstempel
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public final void sendeDaten(final Collection<SystemObject> sensoren,
			final double wert, final long datenZeitStempel)
					throws UmfeldDatenSensorUnbekannteDatenartException {
		for (final SystemObject sensor : sensoren) {
			this.sendeDatum(sensor, wert, datenZeitStempel);
		}
	}

	/**
	 * Sendet einen Sensorwert mit der Kennzeichnung <code>fehlerhaft</code>.
	 *
	 * @param sensoren
	 *            eine Menge von Umfelddatensensoren
	 * @param datenZeitStempel
	 *            der Datenzeitstempel
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public final void sendeFehlerhaftDaten(
			final Collection<SystemObject> sensoren,
			final long datenZeitStempel)
					throws UmfeldDatenSensorUnbekannteDatenartException {
		for (final SystemObject sensor : sensoren) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht geprüft: " + e.getMessage());
				continue;
			}
			final UmfeldDatenSensorWert wert = new UmfeldDatenSensorWert(
					datenArt);
			wert.setFehlerhaftAn();
			this.sendeDatum(sensor, wert.getWert(), datenZeitStempel);
		}
	}

	/**
	 * Sendet einen Sensorwert.
	 *
	 * @param sensor
	 *            der Umfelddatensensor
	 * @param wert
	 *            der zu sendende Wert
	 * @param datenZeitStempel
	 *            der Datenzeitstempel
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public final void sendeDatum(final SystemObject sensor, final long wert,
			final long datenZeitStempel)
					throws UmfeldDatenSensorUnbekannteDatenartException {
		final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
				TestUtensilien.getExterneErfassungDatum(sensor));
		datum.setT(PlPruefungLogischUFDTest.STANDARD_T);
		datum.getWert().setWert(wert);
		final ResultData resultat = new ResultData(sensor,
				datum.getOriginalDatum().getDataDescription(), datenZeitStempel,
				datum.getDatum());
		if (MeteorologischeKontrolleTest.DEBUG) {
			System.out.println(TestUtensilien.jzt() + ", Sende: " + resultat); //$NON-NLS-1$
		}
		try {
			PlPruefungLogischUFDTest.sender.sende(resultat);
		} catch (final Exception e) {
			e.printStackTrace();
			MeteorologischeKontrolleTest.LOGGER.error(Constants.EMPTY_STRING,
					e);
		}
	}

	/**
	 * Sendet einen bzw. mehrere Sensorwert
	 *
	 * @param sensoren
	 *            eine Menge von Umfelddatensensoren
	 * @param wert
	 *            der zu sendende Wert
	 * @param datenZeitStempel
	 *            der Datenzeitstempel
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public final void sendeDaten(final Collection<SystemObject> sensoren,
			final long wert, final long datenZeitStempel)
					throws UmfeldDatenSensorUnbekannteDatenartException {
		for (final SystemObject sensor : sensoren) {
			this.sendeDatum(sensor, wert, datenZeitStempel);
		}
	}

	@Override
	public void update(final ResultData[] resultate) {
		if (resultate != null) {
			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					final UmfeldDatenSensorDatum ufdDatum = new UmfeldDatenSensorDatum(
							resultat);
					final boolean implausibel = ufdDatum
							.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA;
					final MeteoErgebnis ergebnis = new MeteoErgebnis(
							resultat.getObject(), resultat.getDataTime(),
							implausibel);
					this.ergebnisIst.put(resultat.getObject(), ergebnis);

					if (MeteorologischeKontrolleTest.DEBUG) {
						System.out.println(TestUtensilien.jzt() + ", Empfange(" //$NON-NLS-1$
								+ ergebnis + "): " + resultat); //$NON-NLS-1$
					}
				}
			}
		}
	}

	/**
	 * Senset ein fehlerhaftes Datum und wartet dann fünf Intervalle (Reset).
	 *
	 * @return ein Zeitstempel, an dem für <b>alle</b> Sensoren sicher schon
	 *         seit mehreren Intervallen keine Werte mehr vorliegen
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	protected final long getTestBeginnIntervall()
			throws UmfeldDatenSensorUnbekannteDatenartException {
		final long intervall = TestUtensilien.getBeginAktuellerSekunde();

		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht geprüft: " + e.getMessage());
				continue;
			}
			final UmfeldDatenSensorWert wert = new UmfeldDatenSensorWert(
					datenArt);
			wert.setFehlerhaftAn();
			this.sendeDatum(sensor, wert.getWert(), intervall);
		}

		return intervall + (PlPruefungLogischUFDTest.STANDARD_T * 5);
	}

}
