/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Pl-Prüfung logisch UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.pllogufd.
 * 
 * de.bsvrz.dua.pllogufd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.pllogufd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.pllogufd.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.pllogufd.testausfall;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.clock.ClockScheduler;
import de.bsvrz.dua.pllogufd.vew.VerwaltungPlPruefungLogischUFD;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.funclib.operatingMessage.OperatingMessage;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * Das Modul Ausfallüberwachung meldet sich auf alle Parameter an und führt mit
 * allen über die Methode aktualisiereDaten(ResultData[] arg0) übergebenen Daten
 * eine Prüfung durch. Die Prüfung überwacht, ob ein Messwert nach Ablauf des
 * dafür vorgesehenen Intervalls übertragen wurde. Der erwartete
 * Meldungszeitpunkt für einen zyklisch gelieferten Messwert ergibt sich aus dem
 * Intervallbeginn zuzüglich der Erfassungsintervalldauer. Ein nicht
 * übertragener Messwert wird intern als Datensatz mit dem erwarteten
 * Intervallbeginn angelegt, wobei die Messwerte jeweils auf den Status Nicht
 * erfasst gesetzt werden. Nach der Prüfung werden die Daten dann an den
 * nächsten Bearbeitungsknoten weitergereicht.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class UFDAusfallUeberwachung extends AbstraktBearbeitungsKnotenAdapter implements ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * speichert pro Systemobjekt die letzte empfangene Datenzeit.
	 */
	private final Map<SystemObject, Long> letzteEmpfangeneDatenZeitProObj = new HashMap<SystemObject, Long>();
	/**
	 * Mapt alle betrachteten Systemobjekte auf den aktuell für sie erlaubten
	 * maximalen Zeitverzug.
	 */
	protected Map<SystemObject, Long> objektWertErfassungVerzug = Collections
			.synchronizedMap(new TreeMap<SystemObject, Long>());
	/**
	 * interner Kontrollprozess.
	 */
	private ClockScheduler kontrollProzess = null;

	private static final MessageTemplate TEMPLATE = new MessageTemplate(MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN, MessageTemplate.fixed("Datensatz mit Zeitstempel "),
			MessageTemplate.variable("timestamp"),
			MessageTemplate.fixed(" Uhr durch PL-Ausfallüberwachung UFD angelegt bei Messstelle "),
			MessageTemplate.object(), MessageTemplate.fixed(". "), MessageTemplate.ids());

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung) throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
		kontrollProzess = new ClockScheduler(VerwaltungPlPruefungLogischUFD.clock);

		dieVerwaltung.getVerbindung().addConnectionListener(new DavConnectionListener() {
			@Override
			public void connectionClosed(final ClientDavInterface connection) {
				kontrollProzess.terminate();
			}
		});

		for (final SystemObject objekt : verwaltung.getSystemObjekte()) {
			letzteEmpfangeneDatenZeitProObj.put(objekt, (long) -1);
		}

		final DataDescription parameterBeschreibung = new DataDescription(
				dieVerwaltung.getVerbindung().getDataModel().getAttributeGroup("atg.ufdsAusfallÜberwachung"),
				dieVerwaltung.getVerbindung().getDataModel().getAspect(DaVKonstanten.ASP_PARAMETER_SOLL));
		dieVerwaltung.getVerbindung().subscribeReceiver(this, dieVerwaltung.getSystemObjekte(), parameterBeschreibung,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	/**
	 * Erfragt das ausgefallene Datum, dass sich aus dem übergebenen Datum
	 * ergibt.
	 *
	 * @param originalResultat
	 *            ein Datum
	 * @return das ausgefallene Datum, dass sich aus dem übergebenen Datum
	 *         ergibt
	 */
	protected ResultData getAusfallDatumVon(final ResultData originalResultat) {
		final UmfeldDatenSensorDatum wert = new UmfeldDatenSensorDatum(originalResultat);
		wert.setStatusErfassungNichtErfasst(DUAKonstanten.JA);
		wert.getWert().setNichtErmittelbarAn();

		final long zeitStempel = wert.getDatenZeit() + wert.getT();

		final ResultData resultat = new ResultData(originalResultat.getObject(), originalResultat.getDataDescription(),
				zeitStempel, wert.getDatum());

		return resultat;
	}

	/**
	 * Erfragt die Intervalllänge T eines Datums.
	 *
	 * @param resultat
	 *            ein Datum
	 * @return die im übergebenen Datum enthaltene Intervalllänge T
	 */
	protected long getTVon(final ResultData resultat) {
		final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(resultat);
		return datum.getT();
	}

	@Override
	public void update(final ResultData[] resultate) {
		if (resultate != null) {
			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					synchronized (this.objektWertErfassungVerzug) {
						this.objektWertErfassungVerzug.put(resultat.getObject(),
								new Long(resultat.getData().getTimeValue("maxZeitVerzug").getMillis()));
						LOGGER.info("Neue Parameter: maxZeitVerzug(" + resultat.getObject() + ") = "
								+ resultat.getData().getTimeValue("maxZeitVerzug").getMillis() + "ms");
					}
				}
			}
		}
	}

	@Override
	public synchronized void aktualisiereDaten(final ResultData[] resultate) {
		if (resultate != null) {
			final List<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();

			for (final ResultData resultat : resultate) {
				if (resultat != null) {

					if (getMaxZeitVerzug(resultat.getObject()) <= 0) {
						/**
						 * Datum wird nicht ueberwacht
						 */
						weiterzuleitendeResultate.add(resultat);
					} else {
						/**
						 * Hier werden die Daten herausgefiltert, die von der
						 * Ausfallkontrolle quasi zu unrecht generiert wurden,
						 * da das Datum nur minimal zu spät kam.
						 */
						if (letzteEmpfangeneDatenZeitProObj.get(resultat.getObject()) < resultat.getDataTime()) {

							/**
							 * Zeitstempel ist echt neu!
							 */
							weiterzuleitendeResultate.add(resultat);

							letzteEmpfangeneDatenZeitProObj.put(resultat.getObject(), resultat.getDataTime());
						}
						if (resultat.getData() != null) {
							final long kontrollZeitpunkt = getKontrollZeitpunktVon(resultat);
							if (!kontrollProzess.isTerminated()) {
								// Timer starten zur Ausfallüberwachung
								kontrollProzess.schedule(Instant.ofEpochMilli(kontrollZeitpunkt), new Runnable() {
									@Override
									public void run() {
										// Schon mal vorab einen leeren
										// Datensatz erstellen
										ResultData ausfallDatum = getAusfallDatumVon(resultat);

										if (letzteEmpfangeneDatenZeitProObj.get(resultat.getObject()) < ausfallDatum
												.getDataTime()) {
											// Datum nicht rechtzeitig
											// angekommen, da der leere
											// Datensatz hinter dem zuletzt
											// empfangenen Datensatz liegt

											aktualisiereDaten(new ResultData[] { ausfallDatum });

											// Betriebsmeldung erzeugen
											VerwaltungPlPruefungLogischUFD verwaltung = (VerwaltungPlPruefungLogischUFD) getVerwaltung();
											OperatingMessage message = TEMPLATE.newMessage(
													verwaltung.getBetriebsmeldungsObjekt(resultat.getObject()));
											LocalTime localTime = Instant.ofEpochMilli(ausfallDatum.getDataTime())
													.atZone(ZoneId.systemDefault()).toLocalTime();
											message.put("timestamp", TIME_FORMAT.format(localTime));
											message.addId("[DUA-PP-UA01]");
										}
									}
								});
							}
						}
					}
				}
			}

			if ((knoten != null) && !weiterzuleitendeResultate.isEmpty()) {
				knoten.aktualisiereDaten(weiterzuleitendeResultate.toArray(new ResultData[0]));
			}
		}
	}

	/**
	 * Erfragt den maximalen Zeitverzug für ein Systemobjekt.
	 *
	 * @param obj
	 *            ein Systemobjekt
	 * @return der maximale Zeitverzug für das Systemobjekt oder -1, wenn dieser
	 *         nicht ermittelt werden konnte
	 */
	protected long getMaxZeitVerzug(final SystemObject obj) {
		long maxZeitVerzug = -1;

		if (obj != null) {
			synchronized (objektWertErfassungVerzug) {
				final Long dummy = objektWertErfassungVerzug.get(obj);
				if ((dummy != null) && (dummy > 0)) {
					maxZeitVerzug = dummy;
				}
			}
		}

		return maxZeitVerzug;
	}

	/**
	 * Erfragt den Zeitpunkt, zu dem von dem Objekt, das mit diesem Datensatz
	 * assoziiert ist, ein neuer Datensatz (spätestens) erwartet wird.
	 *
	 * @param empfangenesResultat
	 *            ein empfangener Datensatz
	 * @return der späteste Zeitpunkt des nächsten Datensatzes oder -1, wenn
	 *         dieser nicht sinnvoll bestimmt werden konnte (wenn z.B. keine
	 *         Parameter vorliegen)
	 */
	private long getKontrollZeitpunktVon(final ResultData empfangenesResultat) {
		long kontrollZeitpunkt = -1;

		final long maxZeitVerzug = getMaxZeitVerzug(empfangenesResultat.getObject());

		if (maxZeitVerzug >= 0) {
			// Zeitpunkt bis Eintreffen: Zeitstempel des letzen Datensatzen +
			// Intervallänge + MaxZeitverzug
			// Denn der Datenzeitstempel markiert das Intervallende.
			kontrollZeitpunkt = empfangenesResultat.getDataTime() + getTVon(empfangenesResultat) + maxZeitVerzug;
		} else {
			Debug.getLogger()
					.fine("Es wurden noch keine (sinnvollen) Parameter empfangen: " + empfangenesResultat.getObject());
		}

		return kontrollZeitpunkt;
	}

	@Override
	public ModulTyp getModulTyp() {
		return null;
	}

	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung dfs) {
		// hier wird nicht publiziert
	}
}
