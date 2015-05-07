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

package de.bsvrz.dua.pllogufd.testaufab;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Test des Moduls Anstieg-Abfall-Kontrolle<br>
 * Der Test implementiert die Vorgaben aus dem Dokument
 * [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
@Ignore("Testdatenverteiler pr�fen")
public class AnstiegAbfallKontrolleTest implements ClientSenderInterface,
ClientReceiverInterface {

	/**
	 * Debug-Ausgaben?
	 */
	private static final boolean DEBUG = true;

	/**
	 * Markierung der einzelnen Messwerte analog Graphik 5-4
	 * [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25
	 */
	protected static enum MARKIERUNG {
		/**
		 * ok.
		 */
		ok,
		/**
		 * nicht_ermittelbar.
		 */
		nicht_ermittelbar,
		/**
		 * implausibel_o_fehlerfahft.
		 */
		implausibel_o_fehlerfahft
	}

	/**
	 * Messwerte mit der Vorg�nger-Nachfolger-Relation entsprechend der Graphik
	 * in [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25 (bei maximaler Differenz
	 * von 5)
	 */
	private static MessWert[] messWerte = null;

	/**
	 * die hier betrachteten Sensoren (z.B. nur Sensoren, die kontinuierliche
	 * Werten bereitstellen)
	 */
	private final Collection<SystemObject> untersuchteSensoren = new HashSet<>();

	/**
	 * Datenverteiler-Verbindung.
	 */
	private ClientDavInterface dav = null;

	/**
	 * letzter Ist-Ergebnis-Wert von einem Sensor<br>
	 * (<code>Implausibel</code> und <code>fehlerhaft</code> ==
	 * <code>true</code>).
	 */
	private final Map<SystemObject, Boolean> ergebnisIst = new HashMap<>();

	/**
	 * aktuelles Intervall f�r Testdaten.
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
		 * filtere FBZ heraus, da diese nicht sinnvoll �berpr�ft werden k�nnen
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht gepr�ft: " + e.getMessage());
				continue;
			}
			if (!datenArt.equals(UmfeldDatenArt.fbz)) {
				this.untersuchteSensoren.add(sensor);
			}
		}

		/**
		 * Setzte den maximalen Ausfall auf 1,0s Setzte die Parameter f�r die
		 * Differenzialkontrolle auf harmlose Werte
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor,
					1000);
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht gepr�ft: " + e.getMessage());
				continue;
			}
			
			if (!datenArt.equals(UmfeldDatenArt.fbz)) {
				PlPruefungLogischUFDTest.sender.setDiffPara(sensor, 5,
						Constants.MILLIS_PER_HOUR);
			}
		}

		/**
		 * maximal zul�ssige Differenz auf pauschal 5 setzen
		 */
		for (final SystemObject sensor : this.untersuchteSensoren) {
			PlPruefungLogischUFDTest.sender.setAnAbPara(sensor, 5);
		}

		/**
		 * Anmeldung auf alle Daten die aus der Applikation Pl-Pr�fung logisch
		 * UFD kommen
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht gepr�ft: " + e.getMessage());
				continue;
			}
			final DataDescription datenBeschreibung = new DataDescription(dav
					.getDataModel().getAttributeGroup(
							"atg.ufds" + datenArt.getName()), //$NON-NLS-1$
							dav.getDataModel().getAspect(
									"asp.plausibilit�tsPr�fungLogisch")); //$NON-NLS-1$
			dav.subscribeReceiver(this, sensor, datenBeschreibung,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
		}

		/**
		 * Warte eine Sekunde bis Datenanmeldungen durch sind
		 */
		try {
			Thread.sleep(1000L);
		} catch (final InterruptedException ex) {
			//
		}

		/**
		 * Produziere initialie Werte, die noch nicht getestet werden, um
		 * Seiteneffekte mit anderen Pl-Pr�fungen innerhalb dieser SWE zu
		 * vermeiden. Es wird hier je Sensor ein Wert mit dem Zeitstempel der
		 * n�chsten Sekunden und dem Intervall von 1s geschickt
		 */
		final GregorianCalendar kal = new GregorianCalendar();
		kal.setTimeInMillis(System.currentTimeMillis());
		kal.set(Calendar.MILLISECOND, 0);
		final long zeitStempel = kal.getTimeInMillis();
		aktuellesIntervall = zeitStempel
				+ (5 * PlPruefungLogischUFDTest.STANDARD_T);

		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor, -1);
		}

		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				DUAKonstanten.ZEIT_FORMAT_GENAU_STR);

		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			final ResultData resultat;
			try {
				resultat = TestUtensilien
						.getExterneErfassungDatum(sensor);
			} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht gepr�ft: " + e.getMessage());
				continue;
			}
			
			final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
					resultat);
			datum.setT(PlPruefungLogischUFDTest.STANDARD_T);
			datum.getWert().setFehlerhaftAn();

			final ResultData sendeDatum = new ResultData(datum
					.getOriginalDatum().getObject(), datum.getOriginalDatum()
					.getDataDescription(), zeitStempel, datum.getDatum());

			if (AnstiegAbfallKontrolleTest.DEBUG) {
				System.out.println(TestUtensilien.jzt() + " Sende initial: " + //$NON-NLS-1$
						dateFormat.format(new Date(sendeDatum.getDataTime()))
						+ ", " + //$NON-NLS-1$
								datum.getOriginalDatum().getObject());
			}

			PlPruefungLogischUFDTest.sender.sende(sendeDatum);
		}

		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor,
					1000);
		}
		DAVTest.warteBis(zeitStempel
				+ (2 * PlPruefungLogischUFDTest.STANDARD_T) + 50);

		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			final ResultData resultat;
			try {
				resultat = TestUtensilien
						.getExterneErfassungDatum(sensor);
			} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
				System.err.println("Wird nicht gepr�ft: " + e.getMessage());
				continue;
			}

			final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
					resultat);
			datum.setT(PlPruefungLogischUFDTest.STANDARD_T);
			datum.getWert().setFehlerhaftAn();

			final ResultData sendeDatum = new ResultData(datum
					.getOriginalDatum().getObject(), datum.getOriginalDatum()
					.getDataDescription(), zeitStempel
					+ PlPruefungLogischUFDTest.STANDARD_T, datum.getDatum());

			if (AnstiegAbfallKontrolleTest.DEBUG) {
				System.out.println(TestUtensilien.jzt() + " Sende initial: " + //$NON-NLS-1$
						dateFormat.format(new Date(sendeDatum.getDataTime()))
						+ ", " + //$NON-NLS-1$
								datum.getOriginalDatum().getObject());
			}

			PlPruefungLogischUFDTest.sender.sende(sendeDatum);
		}

		/**
		 * Initialisierung der Messwerte mit der Vorg�nger-Nachfolger-Relation
		 * entsprechend der Graphik 4-5 in [QS-02.04.00.00.00-PrSpez-2.0 (DUA)],
		 * S. 25 (bei maximaler Differenz von 5)
		 */
		AnstiegAbfallKontrolleTest.messWerte = new MessWert[] {
				new MessWert(5, MARKIERUNG.ok, null),
				new MessWert(5, MARKIERUNG.ok, null),
				new MessWert(11, MARKIERUNG.ok, true),
				new MessWert(9, MARKIERUNG.ok, null),
				new MessWert(9, MARKIERUNG.ok, null),
				new MessWert(8, MARKIERUNG.ok, null),
				new MessWert(4, MARKIERUNG.ok, null),
				new MessWert(10, MARKIERUNG.ok, true),
				new MessWert(1, MARKIERUNG.ok, true),
				new MessWert(2, MARKIERUNG.ok, null),
				new MessWert(3, MARKIERUNG.ok, null),
				new MessWert(3, MARKIERUNG.ok, null),
				new MessWert(9, MARKIERUNG.ok, true),
				new MessWert(9, MARKIERUNG.ok, null),
				new MessWert(6, MARKIERUNG.ok, null),
				new MessWert(6, MARKIERUNG.nicht_ermittelbar, false),
				new MessWert(15, MARKIERUNG.ok, false),
				new MessWert(15, MARKIERUNG.ok, null),
				new MessWert(12, MARKIERUNG.implausibel_o_fehlerfahft, false),
				new MessWert(4, MARKIERUNG.ok, false),
				new MessWert(4, MARKIERUNG.ok, null),
				new MessWert(4, MARKIERUNG.implausibel_o_fehlerfahft, false),
				new MessWert(12, MARKIERUNG.ok, false),
				new MessWert(12, MARKIERUNG.nicht_ermittelbar, false),
				new MessWert(8, MARKIERUNG.ok, null),
				new MessWert(8, MARKIERUNG.ok, null),
				new MessWert(8, MARKIERUNG.ok, null),
				new MessWert(8, MARKIERUNG.ok, null),
				new MessWert(4, MARKIERUNG.ok, null),
				new MessWert(4, MARKIERUNG.ok, null) };
	}

	/**
	 * F�hrt den Vergleich aller Ist-Werte mit allen Soll-Werten durch und zeigt
	 * die Ergebnisse an. Gleichzeitig werden die Ergebnisse �ber
	 * <code>JUnit</code> getestet<br>
	 * <br>
	 * Nach dem Test werden die Mengen der Soll- und Ist-Werte wieder gel�scht
	 *
	 * @param durchlauf
	 *            der Durchlauf
	 */
	private void ergebnisUeberpruefen(final int durchlauf) {
		if (!this.ergebnisIst.isEmpty()) {
			final Boolean erwartung = AnstiegAbfallKontrolleTest.messWerte[durchlauf]
					.wirdAlsFehlerhaftUndImplausibelErwartet();
			if (erwartung != null) {
				final boolean erwarteterStatusIstImplausibelUndFehlerHaft = erwartung;
				for (final SystemObject sensor : this.untersuchteSensoren) {
					System.out
					.println("Vergleiche (AAKONTR)[" + durchlauf + "] " + sensor.getPid() + ": Soll(" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							(erwarteterStatusIstImplausibelUndFehlerHaft ? "impl" : "ok") + //$NON-NLS-1$ //$NON-NLS-2$
							"), Ist(" //$NON-NLS-1$
							+ (this.ergebnisIst.get(sensor) ? "impl" : "ok") + ") --> " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							(erwarteterStatusIstImplausibelUndFehlerHaft == this.ergebnisIst
							.get(sensor) ? "Ok" : "!!!FEHLER!!!")); //$NON-NLS-1$ //$NON-NLS-2$
					Assert.assertEquals(
							"Objekt: " + sensor.toString(), //$NON-NLS-1$
							erwarteterStatusIstImplausibelUndFehlerHaft,
							this.ergebnisIst.get(sensor).booleanValue());
				}
			}
		}
		this.ergebnisIst.clear();
	}

	/**
	 * der eigentliche Test.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@Test
	public void testAnstiegAbfallKontrolle() throws Exception {

		DAVTest.warteBis(aktuellesIntervall);

		for (int durchlauf = 0; durchlauf < AnstiegAbfallKontrolleTest.messWerte.length; durchlauf++) {

			/**
			 * nach dem Anfang des n�chsten Intervalls geht es los
			 */
			DAVTest.warteBis(aktuellesIntervall
					+ PlPruefungLogischUFDTest.STANDARD_T + 50);

			final SimpleDateFormat dateFormat = new SimpleDateFormat(
					DUAKonstanten.ZEIT_FORMAT_GENAU_STR);

			/**
			 * Produziere Werte, die getestet werden und "unbesch�digt" durch
			 * die Diff-Pr�fung kommen
			 */
			for (final SystemObject sensor : this.untersuchteSensoren) {
				final ResultData resultat;
				try {
					resultat = TestUtensilien
							.getExterneErfassungDatum(sensor);
				} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
					System.err.println("Wird nicht gepr�ft: " + e.getMessage());
					continue;
				}

				final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
						resultat);
				datum.setT(PlPruefungLogischUFDTest.STANDARD_T);

				/**
				 * Setzte Pr�fwert
				 */
				datum.getWert().setWert(
						AnstiegAbfallKontrolleTest.messWerte[durchlauf]
								.getWert());
				if (AnstiegAbfallKontrolleTest.messWerte[durchlauf]
						.getMarkierung() == MARKIERUNG.nicht_ermittelbar) {
					datum.getWert().setNichtErmittelbarAn();
				} else if (AnstiegAbfallKontrolleTest.messWerte[durchlauf]
						.getMarkierung() == MARKIERUNG.implausibel_o_fehlerfahft) {
					if ((durchlauf % 2) == 0) {
						datum.getWert().setFehlerhaftAn();
					} else {
						datum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					}
				}

				final ResultData sendeDatum = new ResultData(datum
						.getOriginalDatum().getObject(), datum
						.getOriginalDatum().getDataDescription(),
						aktuellesIntervall, datum.getDatum());

				if (AnstiegAbfallKontrolleTest.DEBUG) {
					System.out
							.println(TestUtensilien.jzt()
									+ ", Sende[" + durchlauf + "]: " + //$NON-NLS-1$ //$NON-NLS-2$
									dateFormat.format(new Date(sendeDatum
											.getDataTime())) + ", " + //$NON-NLS-1$
									datum.getOriginalDatum().getObject()
									+ ", T: " + datum.getT()); //$NON-NLS-1$
				}

				PlPruefungLogischUFDTest.sender.sende(sendeDatum);
			}

			/**
			 * Warte bis zum n�chsten Intervall
			 */
			aktuellesIntervall += PlPruefungLogischUFDTest.STANDARD_T;
			DAVTest.warteBis(aktuellesIntervall
					+ ((PlPruefungLogischUFDTest.STANDARD_T / 20) * 18));

			/**
			 * �berpr�fe Ergebnisse
			 */
			this.ergebnisUeberpruefen(durchlauf);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dataRequest(final SystemObject object,
			final DataDescription dataDescription, final byte state) {
		//
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRequestSupported(final SystemObject object,
			final DataDescription dataDescription) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final ResultData[] resultate) {
		if (resultate != null) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat(
					DUAKonstanten.ZEIT_FORMAT_GENAU_STR);

			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					final UmfeldDatenSensorDatum ufdDatum = new UmfeldDatenSensorDatum(
							resultat);
					final boolean implausibelUndFehlerhaft = ufdDatum.getWert()
							.isFehlerhaft()
							&& (ufdDatum
									.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA);

					if (AnstiegAbfallKontrolleTest.DEBUG) {
						System.out.println(TestUtensilien.jzt()
								+ ", Empfange: " + //$NON-NLS-1$
								dateFormat.format(new Date(resultat
										.getDataTime()))
								+ ", " + //$NON-NLS-1$
								resultat.getObject()
								+ ", T: " + ufdDatum.getT() + ", impl: " + //$NON-NLS-1$ //$NON-NLS-2$ 
								(implausibelUndFehlerhaft ? "ja" : "nein")); //$NON-NLS-1$ //$NON-NLS-2$
					}

					if (this.untersuchteSensoren.contains(resultat.getObject())) {
						this.ergebnisIst.put(resultat.getObject(),
								implausibelUndFehlerhaft);
					}
				}
			}
		}
	}

	/**
	 * Ein Objekt dieser Klasse entspricht einem Punkt innerhalb der Graphik aus
	 * [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25
	 *
	 * @author Thierfelder
	 *
	 */
	protected class MessWert {

		/**
		 * Der Sensor-Wert.
		 */
		private long wert = -1;

		/**
		 * Markierung des Wertes (analog [QS-02.04.00.00.00-PrSpez-2.0 (DUA)],
		 * S. 25) (Eingangsrichtung)
		 */
		private MARKIERUNG markierung = null;

		/**
		 * gibt an, ob das Pl-Ergebnis der Pr�fung dieses Wertes als
		 * <code>implausibel</code> und <code>fehlerhaft</code>. gekennzeichnet
		 * erwartet wird <code>null</code> wird als don't care interpretiert
		 */
		private Boolean fehlerhaftUndImplausibel = null;

		/**
		 * Standardkontruktor.
		 *
		 * @param wert
		 *            der Sensor-Wert
		 * @param markierung
		 *            Markierung des Wertes (analog
		 *            [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25)
		 * @param fehlerhaftUndImplausibel
		 *            gibt an, ob das Pl-Ergebnis der Pr�fung dieses Wertes als
		 *            <code>implausibel</code> und <code>fehlerhaft</code>
		 *            gekennzeichnet erwartet wird<br>
		 *            <code>null</code> wird als don't care interpretiert
		 */
		public MessWert(final long wert, final MARKIERUNG markierung,
				final Boolean fehlerhaftUndImplausibel) {
			this.wert = wert;
			this.markierung = markierung;
			this.fehlerhaftUndImplausibel = fehlerhaftUndImplausibel;
		}

		/**
		 * Erfragt die Markierung des Wertes (analog
		 * [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25)
		 *
		 * @return markierung Markierung des Wertes (analog
		 *         [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25)
		 */
		public final MARKIERUNG getMarkierung() {
			return markierung;
		}

		/**
		 * Erfragt den Sensor-Wert.
		 *
		 * @return wert der Sensor-Wert
		 */
		public final long getWert() {
			return wert;
		}

		/**
		 * Erfragt, ob das Pl-Ergebnis der Pr�fung dieses Wertes als
		 * <code>implausibel</code> und <code>fehlerhaft</code> gekennzeichnet
		 * erwartet wird.
		 *
		 * @return fehlerhaftUndImplausibel ob das Pl-Ergebnis der Pr�fung
		 *         dieses Wertes als <code>implausibel</code> und
		 *         <code>fehlerhaft</code> gekennzeichnet erwartet wird<br>
		 *         <code>null</code> wird als don't care interpretiert
		 */
		public final Boolean wirdAlsFehlerhaftUndImplausibelErwartet() {
			return fehlerhaftUndImplausibel;
		}

	}
}
