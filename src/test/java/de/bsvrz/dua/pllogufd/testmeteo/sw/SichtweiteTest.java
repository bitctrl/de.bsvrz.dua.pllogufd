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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoErgebnis;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoKonst;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolleTest;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;

/**
 * Überprüfung des Submoduls Sichtweite aus der Komponente Meteorologische
 * Kontrolle. Diese Überprüfung richtet sich nach den Vorgaben von
 * [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.28<br>
 * <b>Achtung:</b> Bei den Test-Vorgaben werden die Zeilen innerhalb der Tabelle
 * auf S. 28 ignoriert, in denen NI nicht auf "don't care" steht, da diese die
 * SW-Regel Nr.2 überprüfen sollten. Diese Regel ist hier allerdings nicht
 * implementiert.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
@Ignore("Testdatenverteiler prüfen")
public class SichtweiteTest extends MeteorologischeKontrolleTest {

	/**
	 * Standardkonstruktor.
	 *
	 * @throws Exception
	 *             leitet die Ausnahmen weiter
	 */
	public SichtweiteTest() throws Exception {
		super();

		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			if (!this.swSensoren.contains(sensor)
					&& !this.nsSensoren.contains(sensor)
					&& !this.rlfSensoren.contains(sensor)) {
				this.restSensoren.add(sensor);
			}
		}
	}

	/**
	 * Testet implizit die Methode <code>regel1</code> aus
	 * {@link SichtweitenMessstelle}.
	 */
	@Test
	public final void testRegel1() {

		/**
		 * Erste Zeile aus Tabelle auf Seite 28
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(swSensoren,
				MeteoKonst.SW_GRENZ_SW - DAVTest.r.nextInt(2), zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.SW_GRENZ_TROCKEN_RLF - 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject swSensor : this.swSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(swSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(swSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println("(SW)R1.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * Zweite Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(swSensoren,
				MeteoKonst.SW_GRENZ_SW - DAVTest.r.nextInt(2), zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.SW_GRENZ_TROCKEN_RLF + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel
				+ ((PlPruefungLogischUFDTest.STANDARD_T / 20) * 18));
		for (final SystemObject swSensor : this.swSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(swSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(swSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(SW)R1.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * Dritte Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(swSensoren,
				MeteoKonst.SW_GRENZ_SW - DAVTest.r.nextInt(2), zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 40, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.SW_GRENZ_TROCKEN_RLF - 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel
				+ ((PlPruefungLogischUFDTest.STANDARD_T / 20) * 18));
		for (final SystemObject swSensor : this.swSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(swSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(swSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(SW)R1.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * 7. Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(swSensoren, MeteoKonst.SW_GRENZ_SW + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.SW_GRENZ_TROCKEN_RLF - 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel
				+ ((PlPruefungLogischUFDTest.STANDARD_T / 20) * 18));
		for (final SystemObject swSensor : this.swSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(swSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(swSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println("(SW)R1.4\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}
	}
}
