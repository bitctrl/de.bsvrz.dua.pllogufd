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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

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
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Test des Moduls Ausfall�berwachung.<br>
 * Voraussetzungen (Testbedingungen):<br>
 * 1.) Alle Sensoren im Konfigurationsbereich <code>kb.duaTestObjekteUFD</code>
 * werden �berwacht<br>
 * 2.) Daten werden im Minutenintervall zur vollen Minute gesendet
 * (Datenzeitstempel)<br>
 * 3.) Datenverzug wird auf 10s (f�r Sensoren xxx1), 15s (f�r Sensoren xxx2) und
 * 20s (f�r Sensoren xxx3) gesetzt<br>
 * 4.) jedes zehnte Datum f�llt komplett aus<br>
 * (Die Punkte 2.-4. werden durch den Test selbst realisiert)<br>
 * <br>
 * In diesem Test wird f�r alle Sensoren zun�chst ein Datum mit dem
 * Datenzeitstempel der bereits vergangenen Minute gesendet (TS = Zeitpunkt
 * Teststart, 0=Zeitstempel der bereits vergangenen Minute) um die Applikation
 * zu initialisieren. Dann (nach Zeitpunkt 2) werden f�r alle Sensoren im
 * Sekundenintervall Daten gesendet, die <b>nicht</b> als
 * <code>nicht erfasst</code> markiert sind. Die Ausfall-Informationen zu den
 * Datens�tzen werden beim Versand berechnet und gespeichert (jeweils Sensor und
 * erwarteter Zustand).<br>
 * <br>
 * 
 * 0(erstes Dat.) 1 TS 2 3<br>
 * |---------------------|---------------------|--------------------|<br>
 * <br>
 * 
 * Sollten die empfangenen Daten von den je Sensor berechneten Informationen
 * abweichen, gilt der Test als nicht bestanden. (Der Test l�uft
 * <code>TEST_AUSFALL_UEBERWACHUNG_LAEUFE</code> mal) <br>
 * <br>
 * Alle Ergebnisse des Tests werden in die Konsole ausgegeben
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
@Ignore ("Testdatenverteiler pr�fen")
public class UFDAusfallUeberwachungTest implements ClientSenderInterface,
		ClientReceiverInterface {

	/**
	 * Debugging Ausgaben anzeigen?
	 */
	private static final boolean DEBUG = false;

	/**
	 * Die Daten werden im Abstand von
	 * <code>ABSTAND + Random.nextInt(ABSTAND)</code> versendet.
	 */
	protected static final int ABSTAND = 500;

	/**
	 * Die Zeit (in ms) die die erwartete Eintreffzeit eines Datums von der
	 * tats�chlichen Eintreffzeit differieren darf.
	 */
	protected static final long ERGEBNIS_TOLERANZ = 500;

	/**
	 * jeder sovielte Sensor-Wert wird nicht versandt.
	 */
	protected static final int AUSFALL = 5;

	/**
	 * Parameter <code>maxZeitVerzug</code> f�r Sensoren xxx1.
	 */
	private static final long MAX_VERZUG_1 = 5000L;

	/**
	 * Parameter <code>maxZeitVerzug</code> f�r Sensoren xxx2.
	 */
	private static final long MAX_VERZUG_2 = 10000L;

	/**
	 * Parameter <code>maxZeitVerzug</code> f�r Sensoren xxx3.
	 */
	private static final long MAX_VERZUG_3 = 12000L;

	/**
	 * Datenverteiler-Verbindung.
	 */
	private ClientDavInterface dav = null;

	/**
	 * letzter Soll-Ergebnis-Wert von einem Sensor.
	 */
	private Map<SystemObject, Ergebnis> ergebnisSoll = new HashMap<SystemObject, Ergebnis>();

	/**
	 * letzte Ist-Ergebnis-Werte von einem Sensor. Dies sind hier mehrere, da
	 * ggf. auch mehrere Datens�tze pro Intervall empfangen werden k�nnen, wenn
	 * z.B. die Ausfallkontrolle <b>und</b> die Testapplikation einen Wert
	 * senden.
	 */
	private Map<SystemObject, Collection<Ergebnis>> ergebnisIst = new HashMap<SystemObject, Collection<Ergebnis>>();

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
		 * Anmeldung auf alle Daten die aus der Applikation Pl-Pr�fung logisch
		 * UFD kommen
		 */
		for (SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt = UmfeldDatenArt
					.getUmfeldDatenArtVon(sensor);
			DataDescription datenBeschreibung = new DataDescription(dav
					.getDataModel().getAttributeGroup(
							"atg.ufds" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel().getAspect(
							"asp.plausibilit�tsPr�fungLogisch")); //$NON-NLS-1$
			dav.subscribeReceiver(this, sensor, datenBeschreibung,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
		}

		/**
		 * Warte eine Sekunde bis Datenanmeldung durch ist und Parameter da sind
		 */

		/**
		 * Ausfall�berwachung f�r alle Sensoren ausschalten
		 */
		for (SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			PlPruefungLogischUFDTest.sender.setMaxAusfallFuerSensor(sensor, -1);
		}

		/**
		 * Sende jetzt drei Datens�tze, die nicht �berpr�ft werden
		 */
		long ersteDatenZeit = TestUtensilien.getBeginAktuellerSekunde() + 3
				* Constants.MILLIS_PER_SECOND;
		try {
			Thread.sleep(5 * Constants.MILLIS_PER_SECOND);
		} catch (InterruptedException ex) {
			//
		}
		for (int i = 0; i < 3; i++) {
			for (SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
				ResultData resultat = TestUtensilien
						.getExterneErfassungDatum(sensor);
				UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
						resultat);
				datum.setT(Constants.MILLIS_PER_HOUR);
				resultat.setDataTime(ersteDatenZeit
						+ (i * Constants.MILLIS_PER_SECOND));
				PlPruefungLogischUFDTest.sender.sende(resultat);
			}

		}

		/**
		 * Ausfall�berwachung f�r alle Sensoren ausschalten Parameter setzen auf
		 * 5s (f�r Sensoren xxx1), 10s (f�r Sensoren xxx2) und 12s (f�r Sensoren
		 * xxx3)
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
		 * Warte jetzt wenigstens bis zum Beginn der �bern�chsten Minute
		 */
		GregorianCalendar kal = new GregorianCalendar();
		kal.setTimeInMillis(System.currentTimeMillis());
		kal.add(Calendar.MINUTE, 2);
		kal.set(Calendar.SECOND, 2);
		DAVTest.warteBis(kal.getTimeInMillis());

		/**
		 * Sende initiale Daten f�r alle Sensoren mit dem Datenzeitstempel der
		 * vergangenen Minute
		 */
		ersteDatenZeit = TestUtensilien.getBeginNaechsterMinute()
				- Constants.MILLIS_PER_MINUTE * 2;
		for (SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			ResultData resultat = TestUtensilien
					.getExterneErfassungDatum(sensor);
			resultat.setDataTime(ersteDatenZeit);
			PlPruefungLogischUFDTest.sender.sende(resultat);
		}
	}

	/**
	 * F�hrt den Vergleich aller Ist-Werte mit allen Soll-Werten durch und zeigt
	 * die Ergebnisse an. Gleichzeitig werden die Ergebnisse �ber
	 * <code>JUnit</code> getestet<br>
	 * <br>
	 * Nach dem Test werden die Mengen der Soll- und Ist-Werte wieder gel�scht
	 */
	private void ergebnisUeberpruefen() {
		if (!this.ergebnisIst.isEmpty() && !this.ergebnisSoll.isEmpty()) {
			for (SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {

				Collection<Ergebnis> istErgebnisse = this.ergebnisIst
						.get(sensor);
				Ergebnis erfolgsErgebnis = null;
				if (istErgebnisse == null) {
					if (DEBUG) {
						System.out.println("NULL: " + sensor); //$NON-NLS-1$
					}
				} else {
					for (Ergebnis istErgebnis : istErgebnisse) {
						if (istErgebnis.equals(this.ergebnisSoll.get(sensor))) {
							erfolgsErgebnis = istErgebnis;
							break;
						}
					}

					if (erfolgsErgebnis != null) {
						System.out
								.println("Vergleiche (AUSFALL)" + sensor.getPid() + ": Soll(" + this.ergebnisSoll.get(sensor) + //$NON-NLS-1$ //$NON-NLS-2$
										"), Ist(" + erfolgsErgebnis //$NON-NLS-1$
										+ ") --> Ok"); //$NON-NLS-1$
					} else {
						System.out
								.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!Fehler!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //$NON-NLS-1$
						System.out
								.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!Fehler!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //$NON-NLS-1$
						System.out
								.println("  Soll: " + this.ergebnisSoll.get(sensor)); //$NON-NLS-1$
						System.out.println("  Ist-Werte: "); //$NON-NLS-1$
						for (Ergebnis istErgebnis : istErgebnisse) {
							System.out.println("    " + istErgebnis); //$NON-NLS-1$
						}
						System.out
								.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!Fehler!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //$NON-NLS-1$
						System.out
								.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!Fehler!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //$NON-NLS-1$
					}
				}

				/**
				 * JUnit-Test
				 */
				Assert
						.assertTrue(
								"Felher an Sensor: " + sensor.getPid(), erfolgsErgebnis != null); //$NON-NLS-1$
			}
		}

		/**
		 * L�sche Soll- und Ist-Ergebnissmenge vor n�chstem Durchlauf
		 */
		this.ergebnisIst.clear();
		this.ergebnisSoll.clear();
	}

	/**
	 * Anzahl der Intervalle, die der Test der Ausfall�berwachung laufen soll.
	 */
	private static final int TEST_AUSFALL_UEBERWACHUNG_LAEUFE = 10;

	/**
	 * der eigentliche Test.
	 * 
	 * @throws Exception wird weitergereciht
	 * 
	 */
	@Test
	public void testUFDAusfallUeberwachung() throws Exception {

		try {
			Thread.sleep(Constants.MILLIS_PER_MINUTE * 2);
		} catch (InterruptedException ex) {
			//
		}

		/**
		 * Test-Schleife
		 */
		for (int testZaehler = 0; testZaehler < TEST_AUSFALL_UEBERWACHUNG_LAEUFE; testZaehler++) {

			/**
			 * Warte bis zum Anfang der n�chsten Minute
			 */
			final long start = TestUtensilien.getBeginNaechsterMinute();
			DAVTest.warteBis(start + 20);

			this.ergebnisUeberpruefen();

			System.out
					.println("---\nTestlauf Nr." + (testZaehler + 1) + "\n---"); //$NON-NLS-1$ //$NON-NLS-2$

			/**
			 * In dieser Schleife wird f�r jeden Sensor im stochastischen Takt
			 * jeweils ein Datum gesendet. Die Reihenfolge der Sensoren wird
			 * dabei vor jedem Durchlauf neu "ausgew�rfelt". Jeder
			 * <code>AUSFALL</code>-te Sensor wird ignoriert
			 */
			int[] indexFeld = DAVTest
					.getZufaelligeZahlen(PlPruefungLogischUFDTest.SENSOREN
							.size());
			for (int i = 0; i < indexFeld.length; i++) {
				SystemObject sensor = PlPruefungLogischUFDTest.SENSOREN
						.get(indexFeld[i]);

				/**
				 * Dieser Wert f�llt komplett aus
				 */
				if (DAVTest.r.nextInt(AUSFALL) == 0) {
					Ergebnis erwartetesErgebnis = new Ergebnis(sensor, start
							- Constants.MILLIS_PER_MINUTE, true);
					this.ergebnisSoll.put(sensor, erwartetesErgebnis);
					System.out.println("Sende nicht: " + erwartetesErgebnis); //$NON-NLS-1$
					continue;
				}

				ResultData resultat = TestUtensilien
						.getExterneErfassungDatum(sensor);
				resultat.setDataTime(start - Constants.MILLIS_PER_MINUTE);
				PlPruefungLogischUFDTest.sender.sende(resultat);

				/**
				 * Berechne den Status (ob <code>nicht erfasst</code>) des
				 * gerade versendeten Datums
				 */
				Boolean nichtErfasst = null; // == egal

				if (sensor.getPid().endsWith("1")) { //$NON-NLS-1$
					if (Math.abs(System.currentTimeMillis() - start
							- MAX_VERZUG_1) > ERGEBNIS_TOLERANZ) {
						nichtErfasst = System.currentTimeMillis() - start > MAX_VERZUG_1;
					}
				}
				if (sensor.getPid().endsWith("2")) { //$NON-NLS-1$
					if (Math.abs(System.currentTimeMillis() - start
							- MAX_VERZUG_2) > ERGEBNIS_TOLERANZ) {
						nichtErfasst = System.currentTimeMillis() - start > MAX_VERZUG_2;
					}
				}
				if (sensor.getPid().endsWith("3")) { //$NON-NLS-1$
					if (Math.abs(System.currentTimeMillis() - start
							- MAX_VERZUG_3) > ERGEBNIS_TOLERANZ) {
						nichtErfasst = System.currentTimeMillis() - start > MAX_VERZUG_3;
					}
				}

				Ergebnis erwartetesErgebnis = new Ergebnis(sensor, start
						- Constants.MILLIS_PER_MINUTE, nichtErfasst);

				this.ergebnisSoll.put(sensor, erwartetesErgebnis);
				System.out.println(TestUtensilien.jzt()
						+ ", Sende: " + erwartetesErgebnis); //$NON-NLS-1$

				try {
					Thread.sleep(ABSTAND + DAVTest.r.nextInt(ABSTAND));
				} catch (InterruptedException ex) {
					//
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
			for (ResultData resultat : resultate) {
				if (resultat != null && resultat.getData() != null) {
					UmfeldDatenSensorDatum ufdDatum = new UmfeldDatenSensorDatum(
							resultat);

					Ergebnis ergebnisIstFuerSensor = new Ergebnis(
							resultat.getObject(),
							resultat.getDataTime(),
							ufdDatum.getStatusErfassungNichtErfasst() == DUAKonstanten.JA);

					Collection<Ergebnis> ergebnisseBisJetzt = this.ergebnisIst
							.get(resultat.getObject());
					if (ergebnisseBisJetzt == null) {
						ergebnisseBisJetzt = new ArrayList<Ergebnis>();
						ergebnisseBisJetzt.add(ergebnisIstFuerSensor);
						this.ergebnisIst.put(resultat.getObject(),
								ergebnisseBisJetzt);
					} else {
						ergebnisseBisJetzt.add(ergebnisIstFuerSensor);
					}

					if (DEBUG) {
						System.out.println(TestUtensilien.jzt()
								+ ", Empfange: " + ergebnisIstFuerSensor); //$NON-NLS-1$
					}
				}
			}
		}
	}

	/**
	 * F�r den Test der Ausfallkontrolle relevanter Teil der Informationen eines
	 * Sensorwertes. �ber Objekte dieser Klasse wird der Soll-Ist-Vergleich
	 * vorgenommen.
	 * 
	 * @author BitCtrl Systems GmbH, Thierfelder
	 * 
	 */
	private class Ergebnis {

		/**
		 * Datenzeit.
		 */
		protected long datenZeit = 1;

		/**
		 * Systemobjekt eines Umfelddatensensors.
		 */
		protected SystemObject sensor = null;

		/**
		 * ob das Datum als <code>nicht erfasst</code> gekennzeichnet erwartet
		 * wird. <code>null</code> gildt als "egal".
		 */
		protected Boolean nichtErfasst = false;

		/**
		 * Standardkontruktor.
		 * 
		 * @param sensor
		 *            Systemobjekt eines Umfelddatensensors
		 * @param datenZeit
		 *            Datenzeit
		 * @param nichtErfasst
		 *            ob das Datum als <code>nicht erfasst</code>
		 *            gekennzeichnet erwartet wird. <code>null</code> gilt als
		 *            "egal"<br>
		 *            <b>Achtung:</b> Die Markierung "egal" wird verwendet,
		 *            wenn die Zeit, zu der das Datum verschickt wurde und die
		 *            Zeit, zu der es die Ausfallkontrolle h�tte ebenfalls
		 *            verschicken sollen, innerhalb des Toleranzbereichs <code>ERGEBNIS_TOLERANZ</code>
		 *            liegen
		 */
		public Ergebnis(SystemObject sensor, long datenZeit,
				Boolean nichtErfasst) {
			this.sensor = sensor;
			this.datenZeit = datenZeit;
			this.nichtErfasst = nichtErfasst;
		}

		/**
		 * Zwei Ergebnisdatens�tze gelten als gleich, wenn:<br> - die Sensoren
		 * identisch sind,<br> - die erwarteten Datenzeiten identisch sind und<br> -
		 * die Markierungen als <code>nicht erfasst</code> identisch sind (so
		 * diese nicht als "egal" markiert sind).
		 * 
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			boolean ergebnis = false;

			if (obj instanceof Ergebnis) {
				Ergebnis that = (Ergebnis) obj;
				if (this.sensor.equals(that.sensor)
						&& this.datenZeit == that.datenZeit) {
					ergebnis = true;
					if (this.nichtErfasst != null && that.nichtErfasst != null) {
						ergebnis &= this.nichtErfasst == that.nichtErfasst;
					}
				}
			}

			return ergebnis;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			String nichtErfasstStr = (this.nichtErfasst == null ? "egal" : (this.nichtErfasst ? "ja" : "nein")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

			return "Sensor: " + this.sensor.getPid() + ", Daten: " + //$NON-NLS-1$//$NON-NLS-2$
					DUAKonstanten.NUR_ZEIT_FORMAT_GENAU.format(new Date(
							this.datenZeit))
					+ ", nicht Erfasst: " + nichtErfasstStr; //$NON-NLS-1$
		}
	}
}