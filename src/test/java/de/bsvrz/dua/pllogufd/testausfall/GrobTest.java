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

package de.bsvrz.dua.pllogufd.testausfall;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
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
 * Nicht vorgeschriebener Test. Provoziert das Senden von Daten durch die
 * Ausfallkontrolle
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
@Ignore("Testdatenverteiler pr�fen")
public class GrobTest
implements ClientSenderInterface, ClientReceiverInterface {

	/**
	 * Die Zeit (in ms) die die erwartete Eintreffzeit eines Datums von der
	 * tats�chlichen Eintreffzeit differieren darf.
	 */
	protected static final long ERGEBNIS_TOLERANZ = 2000;

	/**
	 * Parameter <code>maxZeitVerzug</code> f�r Sensoren xxx1.
	 */
	private static final long MAX_VERZUG_1 = 500L;

	/**
	 * Parameter <code>maxZeitVerzug</code> f�r Sensoren xxx2.
	 */
	private static final long MAX_VERZUG_2 = 501L;

	/**
	 * Parameter <code>maxZeitVerzug</code> f�r Sensoren xxx3.
	 */
	private static final long MAX_VERZUG_3 = 1500L;

	/**
	 * Datenverteiler-Verbindung.
	 */
	private ClientDavInterface dav = null;

	/**
	 * erg1.
	 */
	private Boolean erg1 = null;

	/**
	 * erg2.
	 */
	private Boolean erg2 = null;

	/**
	 * gesendet1.
	 */
	boolean gesendet1 = false;

	/**
	 * gesendet2.
	 */
	boolean gesendet2 = false;

	/**
	 * ist1.
	 */
	boolean ist1 = false;

	/**
	 * ist2.
	 */
	boolean ist2 = false;

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

		/**
		 * Warte bis Anmeldung sicher durch ist
		 */
		try {
			Thread.sleep(1000L);
		} catch (final InterruptedException ex) {
			//
		}

		/**
		 * Parameter setzen auf 10s (f�r Sensoren xxx1), 15s (f�r Sensoren xxx2)
		 * und 20s (f�r Sensoren xxx3)
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			if (sensor.getPid().endsWith("1")) { //$NON-NLS-1$
				PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor,
						GrobTest.MAX_VERZUG_1);
			} else if (sensor.getPid().endsWith("2")) { //$NON-NLS-1$
				PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor,
						GrobTest.MAX_VERZUG_2);
			} else if (sensor.getPid().endsWith("3")) { //$NON-NLS-1$
				PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor,
						GrobTest.MAX_VERZUG_3);
			}
		}

		/**
		 * Warte eine Sekunde bis die Parameter sicher da sind
		 */

		/**
		 * Anmeldung auf alle Daten die aus der Applikation Pl-Pr�fung logisch
		 * UFD kommen
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			final UmfeldDatenArt datenArt = UmfeldDatenArt
					.getUmfeldDatenArtVon(sensor);
			final DataDescription datenBeschreibung = new DataDescription(
					dav.getDataModel()
					.getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel()
					.getAspect("asp.plausibilit�tsPr�fungLogisch")); //$NON-NLS-1$
			dav.subscribeReceiver(this, sensor, datenBeschreibung,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
		}

		/**
		 * Warte eine Sekunde bis Datenanmeldung durch ist
		 */
		try {
			Thread.sleep(1000L);
		} catch (final InterruptedException ex) {
			//
		}
	}

	/**
	 * Erzeugt einen Messwert mit der Datenbeschreibung
	 * <code>asp.externeErfassung</code>.
	 *
	 * @param sensor
	 *            ein Umfelddatensensor, f�r den ein Messwert erzeugt werden
	 *            soll
	 * @return ein (ausgef�llter) Umfelddaten-Messwert der zum �bergebenen
	 *         Systemobjekt passt. Alle Pl-Pr�fungs-Flags sind auf
	 *         <code>NEIN</code> gesetzt. Der Daten-Intervall betr�gt 1 min.
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public static final ResultData getExterneErfassungDatum(
			final SystemObject sensor)
					throws UmfeldDatenSensorUnbekannteDatenartException {
		final UmfeldDatenArt datenArt = UmfeldDatenArt
				.getUmfeldDatenArtVon(sensor);
		final DataDescription datenBeschreibung = new DataDescription(
				PlPruefungLogischUFDTest.dav.getDataModel()
				.getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
				PlPruefungLogischUFDTest.dav.getDataModel()
				.getAspect("asp.externeErfassung")); //$NON-NLS-1$
		final Data datum = PlPruefungLogischUFDTest.dav
				.createData(PlPruefungLogischUFDTest.dav.getDataModel()
						.getAttributeGroup("atg.ufds" + datenArt.getName())); //$NON-NLS-1$
		datum.getTimeValue("T").setMillis(1L * 1000L); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getUnscaledValue("Wert").set(0); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("Erfassung") //$NON-NLS-1$ //$NON-NLS-2$
		.
		getUnscaledValue("NichtErfasst").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("PlFormal").//$NON-NLS-1$ //$NON-NLS-2$
		getUnscaledValue("WertMax").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("PlFormal").//$NON-NLS-1$ //$NON-NLS-2$
		getUnscaledValue("WertMin").set(DUAKonstanten.NEIN); //$NON-NLS-1$

		datum.getItem(datenArt.getName()).getItem("Status") //$NON-NLS-1$
		.getItem("MessWertErsetzung").//$NON-NLS-1$
		getUnscaledValue("Implausibel").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status") //$NON-NLS-1$
		.getItem("MessWertErsetzung").//$NON-NLS-1$
		getUnscaledValue("Interpoliert").set(DUAKonstanten.NEIN); //$NON-NLS-1$

		datum.getItem(datenArt.getName()).getItem("G�te") //$NON-NLS-1$
		.getUnscaledValue("Index").set(10000); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("G�te") //$NON-NLS-1$
		.getUnscaledValue("Verfahren").set(0); //$NON-NLS-1$

		return new ResultData(sensor, datenBeschreibung,
				System.currentTimeMillis(), datum);
	}

	/**
	 * Anzahl der Intervalle, die der Test der Ausfall�berwachung laufen soll.
	 */
	private static final long TEST_AUSFALL_UEBERWACHUNG_LAEUFE = 30;

	/**
	 * der eigentliche Test.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@Test
	public void test() throws Exception {

		GregorianCalendar kal = new GregorianCalendar();
		kal.setTimeInMillis(System.currentTimeMillis());
		kal.set(Calendar.MILLISECOND, 0);
		int fSekunden = kal.get(Calendar.SECOND) / 1;
		kal.set(Calendar.SECOND, (fSekunden * 1) + 1);
		long startAlles = kal.getTimeInMillis();

		/**
		 * Sende initiale Daten
		 */
		long ersteDatenZeit = startAlles - 1000;

		ResultData resultat1 = TestUtensilien
				.getExterneErfassungDatum(PlPruefungLogischUFDTest.gt1);
		ResultData resultat2 = TestUtensilien
				.getExterneErfassungDatum(PlPruefungLogischUFDTest.gt2);
		// resultat1.setDataTime(ersteDatenZeit);
		// resultat2.setDataTime(ersteDatenZeit);
		// PlPruefungLogischUFDTest.SENDER.sende(resultat1);
		// PlPruefungLogischUFDTest.SENDER.sende(resultat2);
		// System.out.println(ct() + ", Initiales Datum: " +
		// DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(ersteDatenZeit)));
		// //$NON-NLS-1$

		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				DUAKonstanten.NUR_ZEIT_FORMAT_GENAU_STR);

		/**
		 * Test-Schleife
		 */
		for (int testZaehler = 0; testZaehler < GrobTest.TEST_AUSFALL_UEBERWACHUNG_LAEUFE; testZaehler++) {

			kal = new GregorianCalendar();
			kal.setTimeInMillis(System.currentTimeMillis());
			kal.set(Calendar.MILLISECOND, 0);
			fSekunden = kal.get(Calendar.SECOND) / 1;
			kal.set(Calendar.SECOND, (fSekunden * 1) + 1);
			startAlles = kal.getTimeInMillis();

			ersteDatenZeit = startAlles - 1000;
			/**
			 * Warte bis zum Anfang des n�chsten Intervalls
			 */
			while (startAlles > System.currentTimeMillis()) {
				try {
					Thread.sleep(10L);
				} catch (final InterruptedException ex) {
					//
				}
			}

			erg1 = null;
			erg2 = null;
			gesendet1 = false;
			gesendet2 = false;

			System.out.println("\n-------\n" + //$NON-NLS-1$
					ct() + ", " + (testZaehler + 1) + ". Datum: " //$NON-NLS-1$ //$NON-NLS-2$
					+ dateFormat.format(new Date(startAlles)));

			if ((testZaehler % 2) == 0) {
				if ((testZaehler % 10) == 0) {
					continue;
				}
				try {
					Thread.sleep(DAVTest.RANDOM.nextInt(700));
				} catch (final InterruptedException ex) {
					//
				}

				resultat1 = GrobTest
						.getExterneErfassungDatum(PlPruefungLogischUFDTest.gt1);
				resultat1.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet1 = true;
					PlPruefungLogischUFDTest.sender.sende(resultat1);
					System.out.println(ct() + ", Sende: " //$NON-NLS-1$
							+ resultat1.getObject().getPid() + ", " + dateFormat //$NON-NLS-1$
							.format(new Date(resultat1.getDataTime())));
				}

				if ((testZaehler % 7) == 0) {
					continue;
				}
				try {
					Thread.sleep(DAVTest.RANDOM.nextInt(10));
				} catch (final InterruptedException ex) {
					//
				}
				resultat2 = GrobTest
						.getExterneErfassungDatum(PlPruefungLogischUFDTest.gt2);
				resultat2.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet2 = true;
					PlPruefungLogischUFDTest.sender.sende(resultat2);
					System.out.println(ct() + ", Sende: " //$NON-NLS-1$
							+ resultat2.getObject().getPid() + ", " + dateFormat //$NON-NLS-1$
							.format(new Date(resultat2.getDataTime())));
				}
			} else {
				if ((testZaehler % 13) == 0) {
					continue;
				}
				try {
					Thread.sleep(DAVTest.RANDOM.nextInt(700));
				} catch (final InterruptedException ex) {
					//
				}
				resultat2 = GrobTest
						.getExterneErfassungDatum(PlPruefungLogischUFDTest.gt2);
				resultat2.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet2 = true;
					PlPruefungLogischUFDTest.sender.sende(resultat2);
					System.out.println(ct() + ", Sende: " //$NON-NLS-1$
							+ resultat2.getObject().getPid() + ", " + dateFormat //$NON-NLS-1$
							.format(new Date(resultat2.getDataTime())));
				}

				try {
					Thread.sleep(DAVTest.RANDOM.nextInt(5));
				} catch (final InterruptedException ex) {
					//
				}

				if ((testZaehler % 9) == 0) {
					continue;
				}
				resultat1 = GrobTest
						.getExterneErfassungDatum(PlPruefungLogischUFDTest.gt1);
				resultat1.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet1 = true;
					PlPruefungLogischUFDTest.sender.sende(resultat1);
					System.out.println(ct() + ", Sende: " //$NON-NLS-1$
							+ resultat1.getObject().getPid() + ", " + dateFormat //$NON-NLS-1$
							.format(new Date(resultat1.getDataTime())));
				}
			}

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
			final SimpleDateFormat dateFormat = new SimpleDateFormat(
					DUAKonstanten.ZEIT_FORMAT_GENAU_STR);

			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					synchronized (this) {
						final UmfeldDatenSensorDatum ufdDatum = new UmfeldDatenSensorDatum(
								resultat);
						if (resultat.getObject()
								.equals(PlPruefungLogischUFDTest.gt1)) {
							final String implausibel = (ufdDatum
									.getStatusErfassungNichtErfasst() == DUAKonstanten.JA
									? "nicht erfasst" : "erfasst"); //$NON-NLS-1$ //$NON-NLS-2$
							if (erg1 == null) {
								erg1 = ufdDatum
										.getStatusErfassungNichtErfasst() == DUAKonstanten.JA;

								this.ist1 = gesendet1 == (ufdDatum
										.getStatusErfassungNichtErfasst() != DUAKonstanten.JA);
							}
							System.out.println(ct() + ", Empfange: " //$NON-NLS-1$
									+ resultat.getObject() + ", " + //$NON-NLS-1$
									dateFormat.format(
											new Date(resultat.getDataTime()))
									+ " --> " + implausibel); //$NON-NLS-1$

						}
						if (resultat.getObject()
								.equals(PlPruefungLogischUFDTest.gt2)) {
							final String implausibel = (ufdDatum
									.getStatusErfassungNichtErfasst() == DUAKonstanten.JA
									? "nicht erfasst" : "erfasst"); //$NON-NLS-1$ //$NON-NLS-2$
							if (erg2 == null) {
								erg2 = ufdDatum
										.getStatusErfassungNichtErfasst() == DUAKonstanten.JA;

								this.ist2 = gesendet2 == (ufdDatum
										.getStatusErfassungNichtErfasst() != DUAKonstanten.JA);
							}
							System.out.println(ct() + ", Empfange: " //$NON-NLS-1$
									+ resultat.getObject() + ", " + //$NON-NLS-1$
									dateFormat.format(
											new Date(resultat.getDataTime()))
									+ " --> " + implausibel); //$NON-NLS-1$
						}
					}
				}
			}
		}
	}

	/**
	 * Ausgabe der Aktuellen Zeit.
	 *
	 * @return der Aktuellen Zeit
	 */
	private String ct() {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				DUAKonstanten.ZEIT_FORMAT_GENAU_STR);
		return "(NOW:" + dateFormat.format(new Date(System.currentTimeMillis())) //$NON-NLS-1$
		+ ")"; //$NON-NLS-1$
	}
}
