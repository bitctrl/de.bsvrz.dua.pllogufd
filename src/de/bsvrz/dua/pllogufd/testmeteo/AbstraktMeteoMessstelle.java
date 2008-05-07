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

package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Abstrakte Klasse für Umfelddatenmessstellen, für die eine meteorologische
 * Kontrolle durchgeführt werden soll.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public abstract class AbstraktMeteoMessstelle extends AbstractSystemObjekt {

	/**
	 * Nur für Debugging-Zwecke.
	 */
	private static final boolean DEBUG = false;

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	protected static IVerwaltung verwaltung = null;

	/**
	 * die Systemobjekte aller Umfelddatensensoren, die an dieser Messstelle
	 * konfiguriert sind (und in diesem Submodul betrachtet werden).
	 */
	protected Collection<SystemObject> sensorenAnMessStelle = new HashSet<SystemObject>();

	/**
	 * der Zeitstempel aller im Moment gespeicherten Werte.
	 */
	protected long aktuellerZeitstempel = -1;

	/**
	 * Weist lediglich das Systemobjekt (UFD-Sensor) zu.
	 * 
	 * @param obj
	 *            Das zu kapselnde Systemobjekt
	 */
	protected AbstraktMeteoMessstelle(SystemObject obj) {
		super(obj);
	}

	/**
	 * Setzt die Verbindung zum Verwaltungsmodul.
	 * 
	 * @param verwaltung1
	 *            Verbindung zum Verwaltungsmodul
	 */
	protected static final void setVerwaltungsModul(final IVerwaltung 
			verwaltung1) {
		if (verwaltung == null) {
			verwaltung = verwaltung1;
		}
	}

	/**
	 * Erfragt die Menge der in dieser Umfelddatenmessstelle verarbeiteten
	 * Datenarten.
	 * 
	 * @return die Menge der in dieser Umfelddatenmessstelle verarbeiteten
	 *         Datenarten
	 */
	protected abstract Collection<UmfeldDatenArt> getDatenArten();

	/**
	 * Arbeitet <b>alle</b> Regeln der Reihe nach ab, so die Voraussetzungen
	 * zur Abarbeitung der jeweiligen Regel gegeben sind. Die Ergebnisse
	 * überschreiben die Variablen mit den originalen Werten (lokaler Puffer).
	 * 
	 * @return das Ergebnis des Aufrufs der Methode
	 *         <code>getAlleAktuellenWerte()</code>.
	 */
	protected abstract ResultData[] berechneAlleRegeln();

	/**
	 * Erfragt, ob alle Werte, die zur Abarbeitung <b>aller</b> Regeln dieses
	 * Submoduls notwendig sind vorliegen.
	 * 
	 * @return ob <b>alle</b> Werte für <b>ein</b> Intervall vorliegen
	 */
	protected abstract boolean sindAlleWerteFuerIntervallDa();

	/**
	 * Schreibt ein angekommenes Datum in die Member-Variable in die es gehört,
	 * so der aktuelle lokale Puffer entweder leer ist, oder der
	 * Datenzeitstempel des übergebenen Datums mit den Zeitstempeln der bereits
	 * gespeicherten Daten übereinstimmt.<br>
	 * <b>Die heißt insbesondere, dass mit dieser Methode nur Daten in das Modul
	 * gespeichert werden können, die den gleichen Zeitstempel haben</b>
	 * 
	 * @param umfeldDatum
	 *            ein Umfelddatum
	 * @return ob das Umfelddatum in seine Member-Variable gespeichert werden
	 *         konnte<br>
	 *         <b>Ein Datum kann nicht gespeichert werden, wenn sein Zeitstempel
	 *         vom Zeitstempel aller anderen gespeicherten Werte differiert.</b>
	 */
	protected abstract boolean bringeDatumInPosition(
			final ResultData umfeldDatum);

	/**
	 * Löscht alle Member-Variablen mit gespeicherten Umfelddaten im lokalen
	 * Puffer.
	 */
	protected abstract void loescheAlleWerte();

	/**
	 * Erfragt alle im Moment gespeicherten Member-Variablen als
	 * <code>ResultData</code>-Objekte.
	 * 
	 * @return alle im Moment gespeicherten Member-Variablen als
	 *         <code>ResultData</code>-Objekte
	 */
	protected abstract ResultData[] getAlleAktuellenWerte();

	/**
	 * Initialisiert eine Messstelle diesen Typs (Parameteranmeldungen usw.).
	 * 
	 * @throws DUAInitialisierungsException
	 *             wenn die Initialisierung fehlgeschlagen ist
	 */
	protected abstract void initialisiereMessStelle()
			throws DUAInitialisierungsException;

	/**
	 * Erfragt, ob für einen bestimmten Umfelddatensensor bereits ein Datum im
	 * lokalen Puffer steht und gibt dieses zurück. Dabei wird nicht überprüft,
	 * ob das eingetroffene Datum überhaupt Daten enthält.
	 * 
	 * @param umfeldDatum
	 *            ein Datum eines bestimmten Umfelddatensensors
	 * @return das für einen bestimmten Umfelddatensensor bereits im lokalen
	 *         Puffer stehende Datum oder <code>null</code> wenn noch keins im
	 *         Puffer steht
	 */
	protected abstract UmfeldDatenSensorDatum getDatumBereitsInPosition(
			final ResultData umfeldDatum);

	/**
	 * Erfragt, ob der lokale Puffer dieses Moduls leer ist.
	 * 
	 * @return ob der lokale Puffer dieses Moduls leer ist
	 */
	protected abstract boolean isPufferLeer();

	/**
	 * Aktualisiert diese Messstelle der meteorologischen Kontrolle mit einem
	 * neuen Umfelddatum.
	 * 
	 * @param umfeldDatum
	 *            ein aktuelles Umfelddatum
	 * @return die Ergebnisse der Überprüfung bzw. <code>null</code>, wenn
	 *         das übergebene Umfelddatum nicht zur Berechnung von Werten
	 *         geführt hat
	 */
	public final ResultData[] aktualisiereDaten(final ResultData umfeldDatum) {
		ResultData[] ergebnisse = null;

		if (umfeldDatum != null) {
			synchronized (this) {

				/** Debug * */
				if (DEBUG) {
					String zusatzInfo = this.getClass().getSimpleName()
							+ ", Zur Zeit gespeichert: "; //$NON-NLS-1$
					for (ResultData resu : this.getAlleAktuellenWerte()) {
						zusatzInfo += "\n" + resu; //$NON-NLS-1$
					}
					Debug.getLogger().info(zusatzInfo);

					Debug.getLogger().info(this.getClass().getSimpleName()
							+ " IN: " + umfeldDatum.getObject() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
							DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(
									umfeldDatum.getDataTime()))
							+ "\n" + umfeldDatum); //$NON-NLS-1$
				}
				/** Debug * */

				if (this.isDatenArtRelevantFuerSubModul(umfeldDatum)) {
					if (umfeldDatum.getData() == null) {

						/**
						 * Keine Daten oder keine Quelle heißt hier: Mache FLUSH
						 * und leitet den übergebenen Datensatz sofort weiter
						 */
						Collection<ResultData> ergebnisListe = new ArrayList<ResultData>();
						for (ResultData berechnungsErgebnis : this
								.berechneAlleRegeln()) {
							ergebnisListe.add(berechnungsErgebnis);
						}
						ergebnisListe.add(umfeldDatum);
						ergebnisse = ergebnisListe.toArray(new ResultData[0]);
						this.loescheAlleWerte();

					} else {
						if (this.isNeuesIntervall(umfeldDatum)) {

							ergebnisse = this.berechneAlleRegeln();
							this.loescheAlleWerte();
							if (!this.bringeDatumInPosition(umfeldDatum)) {
								Debug.getLogger()
										.warning("Datum konnte nicht gespeichert werden:\n" + umfeldDatum); //$NON-NLS-1$
								ArrayList<ResultData> ergebnisseDummy = new ArrayList<ResultData>();
								for (ResultData ergebnis : ergebnisse) {
									ergebnisseDummy.add(ergebnis);
								}
								ergebnisseDummy.add(umfeldDatum);
								ergebnisse = ergebnisseDummy
										.toArray(new ResultData[0]);
							}

						} else {
							/**
							 * Es kann hier davon ausgegangen werden, dass noch
							 * nicht alle Daten für das aktuelle Intevall da
							 * sind. Es fehlt mindestens noch das gerade
							 * angekommene Datum.
							 */
							if (this.bringeDatumInPosition(umfeldDatum)) {
								if (this.sindAlleWerteFuerIntervallDa()) {
									ergebnisse = this.berechneAlleRegeln();
									this.loescheAlleWerte();
								}
							} else {
								/**
								 * Datum konnte nicht in Position gebracht
								 * werden
								 */
								ergebnisse = new ResultData[] { umfeldDatum };
								Debug.getLogger()
										.warning("Datum konnte nicht in Position gebracht werden:\n" + //$NON-NLS-1$
												umfeldDatum);
							}
						}
					}
				} else {
					/**
					 * Das Datum interessiert hier nicht und wird direkt
					 * zurückgegeben
					 */
					ergebnisse = new ResultData[] { umfeldDatum };
				}

				/** Debug * */
				if (DEBUG) {
					String zusatzInfo = this.getClass().getSimpleName()
							+ ", Jetzt gespeichert: "; //$NON-NLS-1$
					for (ResultData resu : this.getAlleAktuellenWerte()) {
						zusatzInfo += "\n" + resu; //$NON-NLS-1$
					}
					Debug.getLogger().info(zusatzInfo);

					String log = this.getClass().getSimpleName() + " OUT: "; //$NON-NLS-1$
					if (ergebnisse != null && ergebnisse.length != 0) {
						for (ResultData ergebnis : ergebnisse) {
							log += "\n  " + ergebnis.getObject() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
									DUAKonstanten.ZEIT_FORMAT_GENAU
											.format(new Date(ergebnis
													.getDataTime()));
						}
						log += "\n"; //$NON-NLS-1$
						for (ResultData ergebnis : ergebnisse) {
							log += "\n  " + ergebnis; //$NON-NLS-1$
						}
					} else {
						log += "nichts"; //$NON-NLS-1$
					}
					Debug.getLogger().info(log);
				}
				/** Debug * */

			}
		}

		return ergebnisse;
	}

	/**
	 * Erfragt, ob das empfangene Umfelddatum zu einem neuen Intervall gehört.<br>
	 * Dies ist der Fall, wenn der Zeitstempel des gerade empfangenen
	 * Umfelddatums echt größer als <code>aktuellerZeitstempel</code> ist und
	 * in der für das Datum vorgesehenen Member-Variable bereits ein Datum steht
	 * 
	 * @param umfeldDatum
	 *            ein Umfelddatum (muss <code>!= null</code> sein)
	 * @return ob das empfangene Umfelddatum zu einem neuen Intervall gehört
	 */
	private boolean isNeuesIntervall(ResultData umfeldDatum) {
		return this.getDatumBereitsInPosition(umfeldDatum) != null
				&& this.aktuellerZeitstempel < umfeldDatum.getDataTime();
	}

	/**
	 * Erfragt, ob ein übergebenes Umfelddatum in diesem Modul speicherbar ist.<br>
	 * Ein Datum ist dann speicherbar, wenn
	 * <code>aktuellerZeitstempel == -1</code> oder
	 * <code>aktuellerZeitstempel == umfeldDatum.getDataTime()</code> oder der
	 * lokale Speicher an sich leer ist.<br>
	 * Ein Datum wird also nur als im Modul speicherbar erachtet (und dann
	 * gespeichert), wenn noch keine Daten im Modul gespeichert sind, oder die
	 * Daten im Modul zeitlich zum übergebenen Datum passen.
	 * 
	 * @param umfeldDatum
	 *            ein Umfelddatum (muss <code>!= null</code> sein)
	 * @return ob ein übergebenes Umfelddatum in diesem Modul speicherbar ist
	 */
	protected final boolean isDatumSpeicherbar(ResultData umfeldDatum) {
		return this.aktuellerZeitstempel == -1
				|| this.aktuellerZeitstempel == umfeldDatum.getDataTime()
				|| this.isPufferLeer();
	}

	/**
	 * Erfragt, ob ein Umfelddatum in diesem Submodul innerhalb der
	 * Meteorologischen Kontrolle verarbeitet wird (also insbesondere, ob es
	 * hier zwischengespeichert werden muss).
	 * 
	 * @param umfeldDatum
	 *            ein Umfelddatum
	 * @return ob das Umfelddatum in diesem Submodul verarbeitet wird
	 */
	private boolean isDatenArtRelevantFuerSubModul(
			final ResultData umfeldDatum) {
		boolean relevant = false;

		UmfeldDatenArt datenArt = UmfeldDatenArt
				.getUmfeldDatenArtVon(umfeldDatum.getObject());
		if (datenArt != null) {
			relevant = this.getDatenArten().contains(datenArt);
		}

		return relevant;
	}

	/**
	 * Erfragt die Systemobjekte aller Umfelddatensensoren, die an dieser
	 * Messstelle konfiguriert sind (und in diesem Submodul betrachtet werden).
	 * 
	 * @return eine Menge von Umfelddatensensoren (Systemobjekte)
	 */
	public Collection<SystemObject> getSensoren() {
		return this.sensorenAnMessStelle;
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemObjektTyp getTyp() {
		return new SystemObjektTyp() {

			public Class<? extends SystemObjekt> getKlasse() {
				return AbstraktMeteoMessstelle.class;
			}

			public String getPid() {
				return getSystemObject().getType().getPid();
			}

		};
	}

}
