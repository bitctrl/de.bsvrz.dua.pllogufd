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

package de.bsvrz.dua.pllogufd.testdiff;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Test des Moduls Differenzialkontrolle<br>
 * Der Test implementiert die Vorgaben aus dem Dokument
 * [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 24
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
@Ignore("Testdatenverteiler prüfen")
public class UFDDifferenzialKontrolleTest
		implements ClientSenderInterface, ClientReceiverInterface {

	/**
	 * standardmäßige maximal zulässige Ergebniskonstanz in Intervallen.
	 */
	private static final long STANDARD_MAX_INTERVALLE = 3;

	/**
	 * die hier betrachteten Sensoren.
	 */
	private final Collection<SystemObject> untersuchteSensoren = new HashSet<>();

	/**
	 * Datenverteiler-Verbindung.
	 */
	private ClientDavInterface dav = null;

	/**
	 * letzter Soll-Ergebnis-Wert von einem Sensor<br>
	 * (<code>Implausibel</code> und <code>fehlerhaft</code> ==
	 * <code>true</code>).
	 */
	private final Map<SystemObject, Boolean> ergebnisSoll = new HashMap<>();

	/**
	 * letzter Ist-Ergebnis-Wert von einem Sensor<br>
	 * (<code>Implausibel</code> und <code>fehlerhaft</code> ==
	 * <code>true</code>).
	 */
	private final Map<SystemObject, Boolean> ergebnisIst = new HashMap<>();

	/**
	 * letzter für einen Sensor eingetroffener Ergebnisdatensatz (für
	 * Debugging).
	 */
	private final Map<SystemObject, ResultData> ergebnisEingetroffen = new HashMap<>();

	/**
	 * aktuelles Intervall für Testdaten.
	 */
	private long aktuellesIntervall = -1;

	/**
	 * Vorbereitungen.
	 *
	 * @throws Exception
	 *             wird weitergeleitet
	 */
	@Before
	public void setUp() throws Exception {
		this.dav = DAVTest.getDav(PlPruefungLogischUFDTest.CON_DATA);

		DUAUtensilien.setAlleParameter(dav);

		PlPruefungLogischUFDTest.initialisiere();
		PlPruefungLogischUFDTest.sender.setMeteoKontrolle(false);

		/**
		 * filtere FBZ heraus
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht geprüft: " + e.getMessage());
				continue;
			}

			if (!datenArt.equals(UmfeldDatenArt.fbz)) {
				this.untersuchteSensoren.add(sensor);
			}
		}

		/**
		 * maximal zulässige Zeitdauer der Ergebniskonstanz auf
		 * <code>STANDARD_T * STANDARD_MAX_INTERVALLE</code> stellen Eine
		 * Überprüfung findet nur statt, wenn ein eingetroffener Wert "<" als
		 * der Grenzwert von 5 ist
		 */
		for (final SystemObject sensor : this.untersuchteSensoren) {
			PlPruefungLogischUFDTest.sender.setDiffPara(sensor, 5,
					PlPruefungLogischUFDTest.STANDARD_T
							* UFDDifferenzialKontrolleTest.STANDARD_MAX_INTERVALLE);
		}

		/**
		 * Anmeldung auf alle Daten die aus der Applikation Pl-Prüfung logisch
		 * UFD kommen
		 */
		for (final SystemObject sensor : this.untersuchteSensoren) {
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
		 * Stelle Ausfallüberwachung so ein, dass nach 0,5s nicht erfasste Werte
		 * produziert werden
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor,
					500L);
		}

		/**
		 * Produziere initialie Werte, die noch nicht getestet werden, um
		 * Seiteneffekte mit anderen Pl-Prüfungen innerhalb dieser SWE zu
		 * vermeiden. Es wird hier je Sensor ein Wert mit dem Zeitstempel dieser
		 * Sekunden und dem Intervall von 2s geschickt
		 */
		final GregorianCalendar kal = new GregorianCalendar();
		kal.setTimeInMillis(System.currentTimeMillis());
		kal.set(Calendar.MILLISECOND, 0);
		final long zeitStempel = kal.getTimeInMillis();
		aktuellesIntervall = zeitStempel
				+ (4 * PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(
				zeitStempel + PlPruefungLogischUFDTest.STANDARD_T + 10);

		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			final ResultData resultat;
			try {
				resultat = TestUtensilien.getExterneErfassungDatum(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht geprüft: " + e.getMessage());
				continue;
			}

			final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
					resultat);
			datum.setT(PlPruefungLogischUFDTest.STANDARD_T);
			datum.getWert().setFehlerhaftAn();

			final ResultData sendeDatum = new ResultData(
					datum.getOriginalDatum().getObject(),
					datum.getOriginalDatum().getDataDescription(), zeitStempel,
					datum.getDatum());

			PlPruefungLogischUFDTest.sender.sende(sendeDatum);
		}
	}

	/**
	 * Führt den Vergleich aller Ist-Werte mit allen Soll-Werten durch und zeigt
	 * die Ergebnisse an. Gleichzeitig werden die Ergebnisse über
	 * <code>JUnit</code> getestet<br>
	 * <br>
	 * . Nach dem Test werden die Mengen der Soll- und Ist-Werte wieder gelöscht
	 */
	private void ergebnisUeberpruefen() {
		if (!this.ergebnisIst.isEmpty() && !this.ergebnisSoll.isEmpty()) {
			for (final SystemObject sensor : this.untersuchteSensoren) {
				System.out.println(
						"Vergleiche (DIFF)" + sensor.getPid() + ": Soll(" //$NON-NLS-1$ //$NON-NLS-2$
								+ (this.ergebnisSoll
										.get(sensor) ? "impl" : "ok") //$NON-NLS-1$ //$NON-NLS-2$
								+
								"), Ist("
								+ (this.ergebnisIst.get(sensor) ? "impl" : "ok") //$NON-NLS-1$ //$NON-NLS-2$
								+ ") --> " + //$NON-NLS-1$
						(this.ergebnisSoll.get(
										sensor) == this.ergebnisIst.get(sensor)
												? "Ok" : "!!!FEHLER!!!")); //$NON-NLS-1$ //$NON-NLS-2$
				Assert.assertEquals(
						"fehlerhaftes Resultat: " //$NON-NLS-1$
								+ this.ergebnisEingetroffen.get(sensor),
						this.ergebnisSoll.get(sensor),
						this.ergebnisIst.get(sensor));
			}
		}
		this.ergebnisIst.clear();
		this.ergebnisSoll.clear();
	}

	/**
	 * Anzahl der Intervalle, die der Test der Differenzialkontrolle laufen
	 * soll.
	 */
	private static final int TEST_DIFF_KONTROLLE_LAEUFE = 10;

	/**
	 * der eigentliche Test.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@Test
	public void testUFDDifferenzialKontrolle() throws Exception {

		/**
		 * Konstanzzähler für Objekte, die als Implausibel zu markieren sind
		 */
		int konstanzZaehlerOK = 0;

		/**
		 * Konstanzzähler für Objekte, die als nicht als Implausbiel zu
		 * markieren sind
		 */
		int konstanzZaehlerImpl = 0;

		/**
		 * Zeile 1 in Tabelle auf Seite 24 (QS-02.04.00.00.00-PrSpez-2.0 [DUA])
		 * Objekt für Folge von Messwerten, die sich (gerade) so häufig ändert,
		 * dass die maximale Zeitdauer der Ergebniskonstanz nicht erreichen
		 */
		final SystemObject objMaxGleichUndKontrolle = PlPruefungLogischUFDTest.fbt1;

		/**
		 * Zeile 2 in Tabelle auf Seite 24 (QS-02.04.00.00.00-PrSpez-2.0 [DUA])
		 * Objekt für Folge von Messwerten, die sich innerhalb der maximalen
		 * Zeitdauer nicht ändert, dabei aber die Bedingung für die
		 * Differentialkontrolle nicht erfüllt
		 */
		final SystemObject objImmerGleichUndKeineKontrolle = PlPruefungLogischUFDTest.hk1;

		/**
		 * Zeile 3=5 in Tabelle auf Seite 24 (QS-02.04.00.00.00-PrSpez-2.0
		 * [DUA]) Objekt für Folge von Messwerten, die sich innerhalb der
		 * maximalen Zeitdauer nicht ändert und die eventuell vorhandene
		 * Bedingung für die Differentialkontrolle erfüllt
		 */
		final SystemObject objImmerGleichUndKontrolle = PlPruefungLogischUFDTest.rs1;

		/**
		 * Zeile 4 in Tabelle auf Seite 24 (QS-02.04.00.00.00-PrSpez-2.0 [DUA])
		 * Messwert, der sich vom Vorgängerwert unterscheidet<br>
		 * Dies sind alle anderen Sensoren
		 */

		DAVTest.warteBis(aktuellesIntervall);

		for (int durchlauf = 0; durchlauf < UFDDifferenzialKontrolleTest.TEST_DIFF_KONTROLLE_LAEUFE; durchlauf++) {

			/**
			 * Ergebnisse überprüfen, so schon welche eingetroffen sind
			 */
			this.ergebnisUeberpruefen();

			/**
			 * nach dem Anfang des nächsten Intervalls geht es los
			 */
			DAVTest.warteBis(aktuellesIntervall
					+ PlPruefungLogischUFDTest.STANDARD_T + 50);

			konstanzZaehlerImpl++;
			konstanzZaehlerOK++;

			/**
			 * Produziere Werte, die getestet werden und "unbeschädigt" durch
			 * die Diff-Prüfung kommen
			 */
			for (final SystemObject sensor : this.untersuchteSensoren) {
				final ResultData resultat = TestUtensilien
						.getExterneErfassungDatum(sensor);
				final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
						resultat);
				datum.setT(PlPruefungLogischUFDTest.STANDARD_T);
				/**
				 * Setzte Wert erst mal immer auf alternierend 1 und 2
				 */
				datum.getWert().setWert(1 + (durchlauf % 2));

				ResultData sendeDatum = null;

				/**
				 * Manipuliere die Testwerte
				 */
				if (sensor.equals(objImmerGleichUndKeineKontrolle)) {
					datum.getWert().setWert(5);
					this.ergebnisSoll.put(resultat.getObject(), false);
				} else if (sensor.equals(objImmerGleichUndKontrolle)) {
					datum.getWert().setWert(3);
					if (konstanzZaehlerImpl > UFDDifferenzialKontrolleTest.STANDARD_MAX_INTERVALLE) {
						this.ergebnisSoll.put(resultat.getObject(), true);
					} else {
						this.ergebnisSoll.put(resultat.getObject(), false);
					}
				} else if (sensor.equals(objMaxGleichUndKontrolle)) {
					if (konstanzZaehlerOK > UFDDifferenzialKontrolleTest.STANDARD_MAX_INTERVALLE) {
						datum.getWert().setWert(3);
						konstanzZaehlerOK = 0;
					} else {
						datum.getWert().setWert(4);
					}
					this.ergebnisSoll.put(resultat.getObject(), false);
				} else {
					/**
					 * Andere Werte einfach senden
					 */
					this.ergebnisSoll.put(resultat.getObject(), false);
				}
				sendeDatum = new ResultData(
						datum.getOriginalDatum().getObject(),
						datum.getOriginalDatum().getDataDescription(),
						aktuellesIntervall, datum.getDatum());

				PlPruefungLogischUFDTest.sender.sende(sendeDatum);
			}

			/**
			 * Warte bis zum nächsten Intervall
			 */
			aktuellesIntervall += PlPruefungLogischUFDTest.STANDARD_T;
			DAVTest.warteBis(aktuellesIntervall
					+ ((PlPruefungLogischUFDTest.STANDARD_T / 20) * 18));
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

	@Override
	public void update(final ResultData[] resultate) {
		if (resultate != null) {
			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					final UmfeldDatenSensorDatum ufdDatum = new UmfeldDatenSensorDatum(
							resultat);
					final boolean implausibelUndFehlerhaft = ufdDatum.getWert()
							.isFehlerhaft()
							&& (ufdDatum
									.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA);
					this.ergebnisIst.put(resultat.getObject(),
							implausibelUndFehlerhaft);
					this.ergebnisEingetroffen.put(resultat.getObject(),
							resultat);
				}
			}
		}
	}
}
