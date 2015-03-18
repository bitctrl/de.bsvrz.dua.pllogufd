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
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Nicht vorgeschriebener Test. Provoziert das Senden von Daten durch die
 * Ausfallkontrolle
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
@Ignore ("Testdatenverteiler prüfen")
public class GrobTest implements ClientSenderInterface, ClientReceiverInterface {
	
	/**
	 * Die Zeit (in ms) die die erwartete Eintreffzeit eines Datums von der
	 * tatsächlichen Eintreffzeit differieren darf.
	 */
	protected static final long ERGEBNIS_TOLERANZ = 2000;

	/**
	 * Parameter <code>maxZeitVerzug</code> für Sensoren xxx1.
	 */
	private static final long MAX_VERZUG_1 = 500L;

	/**
	 * Parameter <code>maxZeitVerzug</code> für Sensoren xxx2.
	 */
	private static final long MAX_VERZUG_2 = 501L;

	/**
	 * Parameter <code>maxZeitVerzug</code> für Sensoren xxx3.
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
		} catch (InterruptedException ex) {
			//
		}

		/**
		 * Parameter setzen auf 10s (für Sensoren xxx1), 15s (für Sensoren xxx2)
		 * und 20s (für Sensoren xxx3)
		 */
		for (SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			if (sensor.getPid().endsWith("1")) { //$NON-NLS-1$
				PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor,
						MAX_VERZUG_1);
			} else if (sensor.getPid().endsWith("2")) { //$NON-NLS-1$
				PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor,
						MAX_VERZUG_2);
			} else if (sensor.getPid().endsWith("3")) { //$NON-NLS-1$
				PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor,
						MAX_VERZUG_3);
			}
		}

		/**
		 * Warte eine Sekunde bis die Parameter sicher da sind
		 */

		/**
		 * Anmeldung auf alle Daten die aus der Applikation Pl-Prüfung logisch
		 * UFD kommen
		 */
		for (SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt = UmfeldDatenArt
					.getUmfeldDatenArtVon(sensor);
			DataDescription datenBeschreibung = new DataDescription(dav
					.getDataModel().getAttributeGroup(
							"atg.ufds" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel().getAspect(
							"asp.plausibilitätsPrüfungLogisch")); //$NON-NLS-1$
			dav.subscribeReceiver(this, sensor, datenBeschreibung,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
		}

		/**
		 * Warte eine Sekunde bis Datenanmeldung durch ist
		 */
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException ex) {
			//
		}
	}

	/**
	 * Erzeugt einen Messwert mit der Datenbeschreibung
	 * <code>asp.externeErfassung</code>.
	 * 
	 * @param sensor
	 *            ein Umfelddatensensor, für den ein Messwert erzeugt werden
	 *            soll
	 * @return ein (ausgefüllter) Umfelddaten-Messwert der zum übergebenen
	 *         Systemobjekt passt. Alle Pl-Prüfungs-Flags sind auf
	 *         <code>NEIN</code> gesetzt. Der Daten-Intervall beträgt 1 min.
	 */
	public static final ResultData getExterneErfassungDatum(SystemObject sensor) {
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
		DataDescription datenBeschreibung = new DataDescription(
				PlPruefungLogischUFDTest.DAV.getDataModel().getAttributeGroup(
						"atg.ufds" + datenArt.getName()), //$NON-NLS-1$
				PlPruefungLogischUFDTest.DAV.getDataModel().getAspect(
						"asp.externeErfassung")); //$NON-NLS-1$
		Data datum = PlPruefungLogischUFDTest.DAV
				.createData(PlPruefungLogischUFDTest.DAV.getDataModel()
						.getAttributeGroup("atg.ufds" + datenArt.getName())); //$NON-NLS-1$
		datum.getTimeValue("T").setMillis(1L * 1000L); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getUnscaledValue("Wert").set(0); //$NON-NLS-1$
		datum.getItem(datenArt.getName())
				.getItem("Status").getItem("Erfassung").//$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("NichtErfasst").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName())
				.getItem("Status").getItem("PlFormal").//$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("WertMax").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName())
				.getItem("Status").getItem("PlFormal").//$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("WertMin").set(DUAKonstanten.NEIN); //$NON-NLS-1$

		datum.getItem(datenArt.getName())
				.getItem("Status").getItem("MessWertErsetzung").//$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("Implausibel").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName())
				.getItem("Status").getItem("MessWertErsetzung").//$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("Interpoliert").set(DUAKonstanten.NEIN); //$NON-NLS-1$

		datum.getItem(datenArt.getName())
				.getItem("Güte").getUnscaledValue("Index").set(10000); //$NON-NLS-1$ //$NON-NLS-2$
		datum.getItem(datenArt.getName())
				.getItem("Güte").getUnscaledValue("Verfahren").set(0); //$NON-NLS-1$ //$NON-NLS-2$

		return new ResultData(sensor, datenBeschreibung, System
				.currentTimeMillis(), datum);
	}

	/**
	 * Anzahl der Intervalle, die der Test der Ausfallüberwachung laufen soll.
	 */
	private static final long TEST_AUSFALL_UEBERWACHUNG_LAEUFE = 30;

	/**
	 * der eigentliche Test.
	 * 
	 * @throws Exception wird weitergereicht
	 */
	@Test
	public void test() throws Exception {

		GregorianCalendar kal = new GregorianCalendar();
		kal.setTimeInMillis(System.currentTimeMillis());
		kal.set(Calendar.MILLISECOND, 0);
		int fSekunden = kal.get(Calendar.SECOND) / 1;
		kal.set(Calendar.SECOND, fSekunden * 1 + 1);
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

		final SimpleDateFormat dateFormat = new SimpleDateFormat(DUAKonstanten.NUR_ZEIT_FORMAT_GENAU_STR);
		
		/**
		 * Test-Schleife
		 */
		for (int testZaehler = 0; testZaehler < TEST_AUSFALL_UEBERWACHUNG_LAEUFE; testZaehler++) {

			kal = new GregorianCalendar();
			kal.setTimeInMillis(System.currentTimeMillis());
			kal.set(Calendar.MILLISECOND, 0);
			fSekunden = kal.get(Calendar.SECOND) / 1;
			kal.set(Calendar.SECOND, fSekunden * 1 + 1);
			startAlles = kal.getTimeInMillis();

			ersteDatenZeit = startAlles - 1000;
			/**
			 * Warte bis zum Anfang des nächsten Intervalls
			 */
			while (startAlles > System.currentTimeMillis()) {
				try {
					Thread.sleep(10L);
				} catch (InterruptedException ex) {
					//
				}
			}

			erg1 = null;
			erg2 = null;
			gesendet1 = false;
			gesendet2 = false;

			System.out
					.println("\n-------\n" + //$NON-NLS-1$
							ct()
							+ ", " + (testZaehler + 1) + ". Datum: " + dateFormat.format(new Date(startAlles))); //$NON-NLS-1$ //$NON-NLS-2$

			if (testZaehler % 2 == 0) {
				if (testZaehler % 10 == 0) {
					continue;
				}
				try {
					Thread.sleep(DAVTest.r.nextInt(700));
				} catch (InterruptedException ex) {
					//
				}

				resultat1 = getExterneErfassungDatum(PlPruefungLogischUFDTest.gt1);
				resultat1.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet1 = true;
					PlPruefungLogischUFDTest.sender.sende(resultat1);
					System.out
							.println(ct()
									+ ", Sende: " + resultat1.getObject().getPid() + ", " + dateFormat.format(new Date(resultat1.getDataTime()))); //$NON-NLS-1$//$NON-NLS-2$
				}

				if (testZaehler % 7 == 0) {
					continue;
				}
				try {
					Thread.sleep(DAVTest.r.nextInt(10));
				} catch (InterruptedException ex) {
					//
				}
				resultat2 = getExterneErfassungDatum(PlPruefungLogischUFDTest.gt2);
				resultat2.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet2 = true;
					PlPruefungLogischUFDTest.sender.sende(resultat2);
					System.out
							.println(ct()
									+ ", Sende: " + resultat2.getObject().getPid() + ", " + dateFormat.format(new Date(resultat2.getDataTime()))); //$NON-NLS-1$//$NON-NLS-2$
				}
			} else {
				if (testZaehler % 13 == 0) {
					continue;
				}
				try {
					Thread.sleep(DAVTest.r.nextInt(700));
				} catch (InterruptedException ex) {
					//
				}
				resultat2 = getExterneErfassungDatum(PlPruefungLogischUFDTest.gt2);
				resultat2.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet2 = true;
					PlPruefungLogischUFDTest.sender.sende(resultat2);
					System.out
							.println(ct()
									+ ", Sende: " + resultat2.getObject().getPid() + ", " + dateFormat.format(new Date(resultat2.getDataTime()))); //$NON-NLS-1$//$NON-NLS-2$
				}

				try {
					Thread.sleep(DAVTest.r.nextInt(5));
				} catch (InterruptedException ex) {
					//
				}

				if (testZaehler % 9 == 0) {
					continue;
				}
				resultat1 = getExterneErfassungDatum(PlPruefungLogischUFDTest.gt1);
				resultat1.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet1 = true;
					PlPruefungLogischUFDTest.sender.sende(resultat1);
					System.out
							.println(ct()
									+ ", Sende: " + resultat1.getObject().getPid() + ", " + dateFormat.format(new Date(resultat1.getDataTime()))); //$NON-NLS-1$//$NON-NLS-2$
				}
			}

		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		// 		
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if (resultate != null) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat(DUAKonstanten.ZEIT_FORMAT_GENAU_STR);

			for (ResultData resultat : resultate) {
				if (resultat != null && resultat.getData() != null) {
					synchronized (this) {
						UmfeldDatenSensorDatum ufdDatum = new UmfeldDatenSensorDatum(
								resultat);
						if (resultat.getObject().equals(
								PlPruefungLogischUFDTest.gt1)) {
							String implausibel = (ufdDatum
									.getStatusErfassungNichtErfasst() == DUAKonstanten.JA ? "nicht erfasst" : "erfasst"); //$NON-NLS-1$ //$NON-NLS-2$
							if (erg1 == null) {
								erg1 = ufdDatum
										.getStatusErfassungNichtErfasst() == DUAKonstanten.JA;

								this.ist1 = gesendet1 == (ufdDatum
										.getStatusErfassungNichtErfasst() != DUAKonstanten.JA);
							}
							System.out
									.println(ct()
											+ ", Empfange: " + resultat.getObject() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
											dateFormat
													.format(new Date(resultat
															.getDataTime()))
											+ " --> " + implausibel); //$NON-NLS-1$						 

						}
						if (resultat.getObject().equals(
								PlPruefungLogischUFDTest.gt2)) {
							String implausibel = (ufdDatum
									.getStatusErfassungNichtErfasst() == DUAKonstanten.JA ? "nicht erfasst" : "erfasst"); //$NON-NLS-1$ //$NON-NLS-2$
							if (erg2 == null) {
								erg2 = ufdDatum
										.getStatusErfassungNichtErfasst() == DUAKonstanten.JA;

								this.ist2 = gesendet2 == (ufdDatum
										.getStatusErfassungNichtErfasst() != DUAKonstanten.JA);
							}
							System.out
									.println(ct()
											+ ", Empfange: " + resultat.getObject() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
											dateFormat
													.format(new Date(resultat
															.getDataTime()))
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
		final SimpleDateFormat dateFormat = new SimpleDateFormat(DUAKonstanten.ZEIT_FORMAT_GENAU_STR);
		return "(NOW:" + dateFormat.format(new Date(System.currentTimeMillis())) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
