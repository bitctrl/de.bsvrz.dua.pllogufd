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

package de.bsvrz.dua.pllogufd.testmeteo.ni;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.TestUtensilien;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoErgebnis;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoKonst;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolleTest;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;

/**
 * �berpr�fung des Submoduls NiederschlagsIntensit�t aus der Komponente
 * Meteorologische Kontrolle. Diese �berpr�fung richtet sich nach den Vorgaben
 * von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.27<br>
 * <b>Achtung:</b> Die ersten drei Zeilen der Tabelle werden hier nicht
 * ber�cksichtigt, da diese sich auf eine Regel beziehen, die nicht mehr zur
 * Implementierung vorgesehen ist (
 * <code><b>Wenn</b> (NS == Niederschlag) <b>und</b> (NI == 0) <b>und</b> (RLF &gt; NIgrenzNassRLF)
 * <b>dann</b> (NI=implausibel)</code>)
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
@Ignore("Testdatenverteiler pr�fen")
public class NiederschlagsIntensitaetTest extends MeteorologischeKontrolleTest {

	/**
	 * Standardkonstruktor.
	 *
	 * @throws Exception
	 *             leitet die Ausnahmen weiter
	 */
	public NiederschlagsIntensitaetTest() throws Exception {
		super();
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			if (!this.niSensoren.contains(sensor)
					&& !this.wfdSensoren.contains(sensor)
					&& !this.rlfSensoren.contains(sensor)
					&& !this.nsSensoren.contains(sensor)) {
				this.restSensoren.add(sensor);
			}
		}
	}

	// /**
	// * Testet implizit die Methode <code>regel1</code> aus {@link
	// NiederschlagsIntensitaetsMessstelle}
	// */
	// @Test
	// public final void testRegel1(){
	//
	// /**
	// * 4. Zeile aus Tabelle auf Seite 27
	// */
	// long zeitStempel = this.getTestBeginnIntervall();
	// DAVTest.warteBis(zeitStempel + 50);
	//
	// this.sendeDaten(niSensoren, 1, zeitStempel -
	// PlPruefungLogischUFDTest.STANDARD_T);
	// this.sendeDaten(nsSensoren, 0, zeitStempel -
	// PlPruefungLogischUFDTest.STANDARD_T);
	// DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T );
	// for(SystemObject nsSensor:this.nsSensoren){
	// MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
	// MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel -
	// PlPruefungLogischUFDTest.STANDARD_T, true);
	// System.out.println(TestUtensilien.jzt() + ", (NI)R1.4\nSoll: " + soll +
	// "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
	// if(TEST_AN)Assert.assertEquals(soll, ist);
	// }
	// for(SystemObject niSensor:this.niSensoren){
	// MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
	// MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel -
	// PlPruefungLogischUFDTest.STANDARD_T, true);
	// System.out.println(TestUtensilien.jzt() + ", (NI)R1.4\nSoll: " + soll +
	// "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
	// if(TEST_AN)Assert.assertEquals(soll, ist);
	// }
	//
	//
	// /**
	// * 5. Zeile aus Tabelle auf Seite 27
	// */
	// zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
	// DAVTest.warteBis(zeitStempel + 50);
	//
	// this.sendeDaten(niSensoren, 1, zeitStempel -
	// PlPruefungLogischUFDTest.STANDARD_T);
	// this.sendeDaten(nsSensoren, 1, zeitStempel -
	// PlPruefungLogischUFDTest.STANDARD_T);
	// DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T );
	// for(SystemObject nsSensor:this.nsSensoren){
	// MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
	// MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel -
	// PlPruefungLogischUFDTest.STANDARD_T, false);
	// System.out.println(TestUtensilien.jzt() + ", (NI)R1.5\nSoll: " + soll +
	// "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
	// if(TEST_AN)Assert.assertEquals(soll, ist);
	// }
	// for(SystemObject niSensor:this.niSensoren){
	// MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
	// MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel -
	// PlPruefungLogischUFDTest.STANDARD_T, false);
	// System.out.println(TestUtensilien.jzt() + ", (NI)R1.5\nSoll: " + soll +
	// "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
	// if(TEST_AN)Assert.assertEquals(soll, ist);
	// }
	// }

	/**
	 * Testet implizit die Methode <code>regel3</code> aus
	 * {@link NiederschlagsIntensitaetsMessstelle}.
	 * @throws UmfeldDatenSensorUnbekannteDatenartException 
	 */
	@Test
	public final void testRegel3() throws UmfeldDatenSensorUnbekannteDatenartException {

		long rlfStart = MeteoKonst.NI_GRENZ_TROCKEN_RLF - 4;

		/**
		 * 6. Zeile aus Tabelle auf Seite 27
		 *
		 * RLF = NIgrenzTrockenRLF - 3, RLF < NIgrenzTrockenRLF (f�r = 1T)
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 2, RLF < NIgrenzTrockenRLF (f�r = 2T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 1, RLF < NIgrenzTrockenRLF (f�r = 3T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = NIgrenzTrockenRLF, !(RLF < NIgrenzTrockenRLF (1T))
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.4\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * Lasse jetzt RLF wieder abfallen bis f�r mehr als 3T gilt: RLF <
		 * NIgrenzTrockenRLF
		 *
		 * RLF = NIgrenzTrockenRLF - 1, RLF < NIgrenzTrockenRLF (f�r = 1T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.5\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 2, RLF < NIgrenzTrockenRLF (f�r = 2T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.6\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 3, RLF < NIgrenzTrockenRLF (f�r = 3T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.7\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 4, RLF < NIgrenzTrockenRLF (f�r = 4T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.8\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 4, RLF < NIgrenzTrockenRLF (f�r = 5T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.9\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 5, RLF > NIgrenzTrockenRLF (f�r = 6T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.10\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 1
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NI_GRENZ_TROCKEN_RLF + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.1.11\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * lasse jetzt die Relative Luftfeuchte wieder 4T unter
		 * NIgrenzTrockenRLF sein und �nder dann auf WFD > 0
		 */

		for (int i = 0; i < 3; i++) {
			zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
			DAVTest.warteBis(zeitStempel + 50);

			this.sendeDaten(wfdSensoren, 0, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T);
			this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1.0,
					zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
			this.sendeDaten(rlfSensoren, MeteoKonst.NI_GRENZ_TROCKEN_RLF - 1,
					zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
			this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T);
			this.sendeFehlerhaftDaten(restSensoren, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T);

			DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
			for (final SystemObject niSensor : this.niSensoren) {
				final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
				final MeteoErgebnis soll = new MeteoErgebnis(niSensor,
						zeitStempel - PlPruefungLogischUFDTest.STANDARD_T,
						false);
				System.out.println(TestUtensilien.jzt()
						+ ", (NI)R1.2.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
				if (MeteorologischeKontrolleTest.TEST_AN) {
					Assert.assertEquals(soll, ist);
				}
			}
		}

		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NI_GRENZ_TROCKEN_RLF - 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.2.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * �ndere jetzt WFD auf WFD > 0: Implausibilisierung wird aufgehoben
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(wfdSensoren, 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NI_GRENZ_NASS_NI + 1.0,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NI_GRENZ_TROCKEN_RLF - 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(nsSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R1.2.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}
	}

	/**
	 * Testet implizit die Methode <code>regel2</code> aus
	 * {@link NiederschlagsIntensitaetsMessstelle}.
	 * @throws UmfeldDatenSensorUnbekannteDatenartException 
	 */
	@Test
	public final void testRegel2() throws UmfeldDatenSensorUnbekannteDatenartException {

		/**
		 * 7. Zeile aus Tabelle auf Seite 27
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(niSensoren, MeteoKonst.NI_MIN_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NI_GRENZ_TROCKEN_RLF - 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(wfdSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, true);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R2.7\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * 8. Zeile aus Tabelle auf Seite 27
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(niSensoren, MeteoKonst.NI_MIN_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 0, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NI_GRENZ_TROCKEN_RLF + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(wfdSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject nsSensor : this.nsSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R2.8\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * 9. Zeile aus Tabelle auf Seite 27
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(niSensoren, MeteoKonst.NI_MIN_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NI_GRENZ_TROCKEN_RLF - 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(wfdSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R2.9\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}

		/**
		 * 10. Zeile aus Tabelle auf Seite 27
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);

		this.sendeDaten(niSensoren, MeteoKonst.NI_MIN_NI + 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 1, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NI_GRENZ_TROCKEN_RLF + 1,
				zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(wfdSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeFehlerhaftDaten(restSensoren, zeitStempel
				- PlPruefungLogischUFDTest.STANDARD_T);

		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T);
		for (final SystemObject niSensor : this.niSensoren) {
			final MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			final MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel
					- PlPruefungLogischUFDTest.STANDARD_T, false);
			System.out.println(TestUtensilien.jzt()
					+ ", (NI)R2.10\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if (MeteorologischeKontrolleTest.TEST_AN) {
				Assert.assertEquals(soll, ist);
			}
		}
	}

	/**
	 * Schaltet die Ausfall�berwachung wieder aus und wartet f�nf Sekunden.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@After
	public void after() throws Exception {
		/**
		 * Ausfall�berwachung f�r alle Sensoren ausschalten
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor, -1);
		}

		try {
			Thread.sleep(5 * Constants.MILLIS_PER_SECOND);
		} catch (final InterruptedException ex) {
		}
	}

}
