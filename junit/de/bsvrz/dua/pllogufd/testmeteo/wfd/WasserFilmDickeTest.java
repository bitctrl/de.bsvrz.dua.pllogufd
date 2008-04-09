/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
 * Copyright (C) 2007 BitCtrl Systems GmbH 
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

import junit.framework.Assert;

import org.junit.Test;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoErgebnis;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoKonst;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolleTest;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;

/**
 * Überprüfung des Submoduls WasserFilmDicke aus der Komponente Meteorologische
 * Kontrolle. Diese Überprüfung richtet sich nach den Vorgaben von
 * [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.27-28
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public class WasserFilmDickeTest extends MeteorologischeKontrolleTest {

	/**
	 * Standardkonstruktor.
	 * 
	 * @throws Exception
	 *             leitet die Ausnahmen weiter
	 */
	public WasserFilmDickeTest() throws Exception {
		super();

		for (SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			if (!this.wfdSensoren.contains(sensor)
					&& !this.niSensoren.contains(sensor)
					&& !this.rlfSensoren.contains(sensor)
					&& !this.fbzSensoren.contains(sensor)) {
				this.restSensoren.add(sensor);
			}
		}
	}

	/**
	 * Testet implizit die Methode <code>regel1</code> aus
	 * {@link WasserfilmDickeMessstelle}.
	 */
	@Test
	public final void testRegel1() {

		long rlfStart = MeteoKonst.WFD_GRENZ_NASS_RLF + 4;

		/**
		 * Erste Zeile aus Tabelle auf Seite 27
		 * 
		 * RLF = WFDgrenzNassRLF + 3, RLF > WFDgrenzNassRLF (für = 1T)
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(WFD)R1.1.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = WFDgrenzNassRLF + 2, RLF > WFDgrenzNassRLF (für = 2T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(WFD)R1.1.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = WFDgrenzNassRLF + 1, RLF > WFDgrenzNassRLF (für = 3T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(WFD)R1.1.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = WFDgrenzNassRLF, !(RLF > WFDgrenzNassRLF (1T))
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(WFD)R1.1.4\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * Lasse jetzt RLF wieder ansteigen bis für mehr als 3T gilt: RLF >
		 * WFDgrenzNassRLF
		 * 
		 * RLF = WFDgrenzNassRLF + 1, RLF > WFDgrenzNassRLF (für = 1T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(WFD)R1.1.5\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = WFDgrenzNassRLF + 2, RLF > WFDgrenzNassRLF (für = 2T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(WFD)R1.1.6\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = WFDgrenzNassRLF + 3, RLF > WFDgrenzNassRLF (für = 3T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(WFD)R1.1.7\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = WFDgrenzNassRLF + 4, RLF > WFDgrenzNassRLF (für = 4T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println("(WFD)R1.1.8\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = WFDgrenzNassRLF + 4, RLF > WFDgrenzNassRLF (für = 5T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println("(WFD)R1.1.9\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = WFDgrenzNassRLF + 5, RLF > WFDgrenzNassRLF (für = 6T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println("(WFD)R1.1.10\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = WFDgrenzNassRLF - 1
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.WFD_GRENZ_NASS_RLF - 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(WFD)R1.1.11\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * 2. Zeile aus Tabelle auf Seite 27-28
		 * 
		 * lasse jetzt die Relative Luftfeuchte wieder 4T über WFDgrenzNassRLF
		 * sein und ändera dann auf WFD > 0
		 */

		for (int i = 0; i < 3; i++) {
			zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
			DAVTest.warteBis(zeitStempel + 50);

			this.sendeDaten(wfdSensoren, 0, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T);
			this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1.0,
					zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
			this.sendeDaten(rlfSensoren, MeteoKonst.WFD_GRENZ_NASS_RLF + 1,
					zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
			this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T);
			this.sendeFehlerhaftDaten(restSensoren, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T);

			DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
			for (SystemObject wfdSensor : this.wfdSensoren) {
				MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
				MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
						- PlPruefungLogischUFDTest.STANDARD_T, false);
				System.out
						.println("(WFD)R1.2.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
				if (TEST_AN) {
					Assert.assertEquals(soll, ist);
				}
			}
		}

		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.WFD_GRENZ_NASS_RLF + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println("(WFD)R1.2.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * Ändere jetzt WFD auf WFD > 0: Implausibilisierung wird aufgehoben
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.WFD_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.WFD_GRENZ_NASS_RLF + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(fbzSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(WFD)R1.2.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

	}

	/**
	 * Testet implizit die Methode <code>regel3</code> aus
	 * {@link WasserfilmDickeMessstelle}.
	 */
	@Test
	public final void testRegel3() {

		/**
		 * 3. Zeile aus Tabelle auf Seite 27-28
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(fbzSensoren, 32, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(niSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(rlfSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println("(WFD)R3.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}
		for (SystemObject fbzSensor : this.fbzSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(fbzSensor);
			MeteoErgebnis soll = new MeteoErgebnis(fbzSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println("(WFD)R3.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * 4. Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(fbzSensoren, 32, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(niSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(rlfSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject nsSensor : this.nsSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(WFD)R3.4\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}
	}

	/**
	 * Testet implizit die Methode <code>regel2</code> aus
	 * {@link WasserfilmDickeMessstelle}.
	 */
	@Test
	public final void testRegel2() {

		/**
		 * 5. Zeile aus Tabelle auf Seite 28
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(fbzSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(niSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(rlfSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (SystemObject wfdSensor : this.wfdSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(wfdSensor);
			MeteoErgebnis soll = new MeteoErgebnis(wfdSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println("(WFD)R2.5\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}
		for (SystemObject fbzSensor : this.fbzSensoren) {
			MeteoErgebnis ist = this.ergebnisIst.get(fbzSensor);
			MeteoErgebnis soll = new MeteoErgebnis(fbzSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println("(WFD)R2.5\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}
	}
}
