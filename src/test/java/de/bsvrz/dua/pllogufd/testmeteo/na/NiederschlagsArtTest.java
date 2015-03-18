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

package de.bsvrz.dua.pllogufd.testmeteo.na;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.TestUtensilien;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoErgebnis;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoKonst;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolleTest;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Überprüfung des Submoduls NiederschlagsArt aus der Komponente Meteorologische
 * Kontrolle. Diese Überprüfung richtet sich nach den Vorgaben von
 * [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.26
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
@Ignore ("Testdatenverteiler prüfen")
public class NiederschlagsArtTest extends MeteorologischeKontrolleTest {

	/**
	 * Standardkonstruktor.
	 * 
	 * @throws Exception
	 *             leitet die Ausnahmen weiter
	 */
	public NiederschlagsArtTest() throws Exception {
		super();		
		for (SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			if (!this.nsSensoren.contains(sensor)
					&& !this.ltSensoren.contains(sensor)
					&& !this.rlfSensoren.contains(sensor)
					&& !this.niSensoren.contains(sensor)) {
				this.restSensoren.add(sensor);
			}
		}
	}

	/**
	 * Testet implizit die Methode <code>regel1</code> aus
	 * {@link NiederschlagsArtMessstelle}.
	 */
	@Test
	public final void testRegel1() {

		/**
		 * Erste Zeile aus Tabelle auf Seite 26
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(nsSensoren, 40, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(ltSensoren, MeteoKonst.NS_GRENZ_LT - 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.rlfSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.niSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject nsSensor : this.nsSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println(TestUtensilien.jzt()
					+ ", (NS)R1.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * Zweite Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(nsSensoren, 41, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(ltSensoren, MeteoKonst.NS_GRENZ_LT, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.rlfSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.niSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject nsSensor : this.nsSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NS)R1.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * Dritte Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(nsSensoren, 42, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(ltSensoren, MeteoKonst.NS_GRENZ_LT + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.rlfSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.niSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject nsSensor : this.nsSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NS)R1.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}
	}

	/**
	 * Testet implizit die Methode <code>regel3</code> aus
	 * {@link NiederschlagsArtMessstelle}.
	 */
	@Test
	public final void testRegel3() {

		/**
		 * 4. Zeile aus Tabelle auf Seite 26
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(nsSensoren, 40, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NS_GRENZ_TROCKEN_RLF - 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.ltSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject nsSensor : this.nsSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println(TestUtensilien.jzt()
					+ ", (NS)R3.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * 5. Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(nsSensoren, 40, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NS_GRENZ_TROCKEN_RLF - 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.ltSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject nsSensor : this.nsSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NS)R3.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * 6. Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(nsSensoren, 40, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.ltSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NS_GRENZ_TROCKEN_RLF + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject nsSensor : this.nsSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NS)R3.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}
	}

	/**
	 * Testet implizit die Methode <code>regel2</code> aus
	 * {@link NiederschlagsArtMessstelle}<br>
	 * <br>
	 * <b>Achtung:</b> Der Überprüfung der letzten drei Tabellenspalten
	 * ([QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.26) fällt weg, da diese die
	 * Funktionalität der NS-Regel Nr.4 aus den AFo testen (S. 104). Diese Regel
	 * wurde jedoch nicht implementiert.
	 */
	@Test
	public final void testRegel2() {

		/**
		 * 7. Zeile aus Tabelle auf Seite 26
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(nsSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.ltSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		for (SystemObject sensor : rlfSensoren) {
			UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
					TestUtensilien.getExterneErfassungDatum(sensor));
			datum.setT(PlPruefungLogischUFDTest.STANDARD_T);
			datum.getWert().setFehlerhaftAn();
			datum.setStatusErfassungNichtErfasst(DUAKonstanten.JA);
			ResultData resultat = new ResultData(sensor, datum
					.getOriginalDatum().getDataDescription(), zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, datum.getDatum());
			try {
				PlPruefungLogischUFDTest.sender.sende(resultat);
			} catch (Exception e) {
				e.printStackTrace();
				Debug.getLogger().error(Constants.EMPTY_STRING, e);
			}
		}

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject nsSensor : this.nsSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println(TestUtensilien.jzt()
					+ ", (NS)R2.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * 8. Zeile aus Tabelle auf Seite 26
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(nsSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.ltSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(this.restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		for (SystemObject sensor : rlfSensoren) {
			UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
					TestUtensilien.getExterneErfassungDatum(sensor));
			datum.setT(PlPruefungLogischUFDTest.STANDARD_T);
			datum.getWert().setFehlerhaftAn();
			datum.setStatusErfassungNichtErfasst(DUAKonstanten.JA);
			ResultData resultat = new ResultData(sensor, datum
					.getOriginalDatum().getDataDescription(), zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, datum.getDatum());
			try {
				PlPruefungLogischUFDTest.sender.sende(resultat);
			} catch (Exception e) {
				e.printStackTrace();
				Debug.getLogger().error(Constants.EMPTY_STRING, e);
			}
		}

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject nsSensor : this.nsSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NS)R2.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

	}

	/**
	 * Schaltet die Ausfallüberwachung wieder aus und wartet fünf Sekunden.
	 * 
	 * @throws Exception wird weitergereicht
	 */
	@After
	public void after() throws Exception {
		/**
		 * Ausfallüberwachung für alle Sensoren ausschalten
		 */
		for (SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor, -1);
		}

		try {
			Thread.sleep(5 * Constants.MILLIS_PER_SECOND);
		} catch (InterruptedException ex) {
			//
		}
	}

}
