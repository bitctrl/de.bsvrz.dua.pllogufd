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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DavConnectionListener;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.clock.ClockScheduler;
import de.bsvrz.dua.pllogufd.vew.PllogUfdOptions;
import de.bsvrz.dua.pllogufd.vew.VerwaltungPlPruefungLogischUFD;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.funclib.operatingMessage.OperatingMessage;

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
	private PllogUfdOptions options;

	private static final MessageTemplate TEMPLATE = new MessageTemplate(MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN, MessageTemplate.fixed("Datensatz mit Zeitstempel "),
			MessageTemplate.variable("timestamp"),
			MessageTemplate.fixed(" Uhr durch PL-Ausfallüberwachung UFD angelegt bei Messstelle "),
			MessageTemplate.object(), MessageTemplate.fixed(". "), MessageTemplate.ids());

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung) throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		if (dieVerwaltung instanceof VerwaltungPlPruefungLogischUFD) {
			options = ((VerwaltungPlPruefungLogischUFD) dieVerwaltung).getPllogUfdOptions();
		}
		kontrollProzess = new ClockScheduler(VerwaltungPlPruefungLogischUFD.clock);

		dieVerwaltung.getVerbindung().addConnectionListener(new DavConnectionListener() {
			@Override
			public void connectionClosed(final ClientDavInterface connection) {
				kontrollProzess.terminate();
			}
		});

		final DataDescription parameterBeschreibung = new DataDescription(
				dieVerwaltung.getVerbindung().getDataModel().getAttributeGroup("atg.ufdsAusfallÜberwachung"),
				dieVerwaltung.getVerbindung().getDataModel().getAspect(DaVKonstanten.ASP_PARAMETER_SOLL));
		dieVerwaltung.getVerbindung().subscribeReceiver(this, dieVerwaltung.getSystemObjekte(), parameterBeschreibung,
				ReceiveOptions.normal(), ReceiverRole.receiver());

		for (SystemObject objekt : dieVerwaltung.getSystemObjekte()) {
			ResultData data = dieVerwaltung.getVerbindung().getData(objekt, parameterBeschreibung, 0);
			if (data != null) {
				update(new ResultData[] { data });
			}
		}

		for (final SystemObject objekt : verwaltung.getSystemObjekte()) {
			letzteEmpfangeneDatenZeitProObj.put(objekt, (long) -1);
			initInitalChecker(dieVerwaltung, objekt);
		}
	}

	private void initInitalChecker(IVerwaltung dieVerwaltung, SystemObject objekt) {

		if ((options == null) || !options.isInitialeAusfallKontrolle()) {
			return;
		}

		UmfeldDatenArt datenArt;
		try {
			datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(objekt);
		} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
			LOGGER.warning(e.getLocalizedMessage());
			return;
		}

		int periodenDauer = 1;

		Data configData = objekt.getConfigurationData(
				dieVerwaltung.getVerbindung().getDataModel().getAttributeGroup("atg.umfeldDatenSensor"));
		if (configData == null) {
			return;
		}

		SystemObject quelle = configData.getReferenceValue("UmfeldDatenSensorQuelle").getSystemObject();
		if ((quelle != null) && quelle.isOfType("typ.deUfd")) {
			ResultData parameter = dieVerwaltung.getVerbindung().getData(quelle,
					new DataDescription(
							dieVerwaltung.getVerbindung().getDataModel()
									.getAttributeGroup("atg.tlsUfdBetriebsParameter"),
							dieVerwaltung.getVerbindung().getDataModel().getAspect("asp.parameterSoll")),
					0);
			if ((parameter != null) && parameter.hasData()) {
				periodenDauer = parameter.getData().getUnscaledValue("Erfassungsperiodendauer").intValue();
			}
		}

		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime cal = ZonedDateTime.of(LocalDate.now(), LocalTime.of(0, 0), ZoneId.systemDefault());

		while (cal.isBefore(now)) {
			cal = cal.plus(Duration.ofMinutes(periodenDauer));
		}
		cal = cal.minus(Duration.ofMinutes(periodenDauer));

		AttributeGroup atg = dieVerwaltung.getVerbindung().getDataModel()
				.getAttributeGroup("atg.ufds" + datenArt.getName());
		Aspect asp = dieVerwaltung.getVerbindung().getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG);

		Data data = dieVerwaltung.getVerbindung().createData(atg);
		data.getTimeValue("T").setMillis(TimeUnit.SECONDS.toMillis(periodenDauer));

		Data item = data.getItem(datenArt.getName());
		item.getUnscaledValue("Wert").setText("nicht ermittelbar");

		item.getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").setText("Ja");
		item.getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").setText("Nein");
		item.getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").setText("Nein");
		item.getItem("Status").getItem("PlLogisch").getUnscaledValue("WertMaxLogisch").setText("Nein");
		item.getItem("Status").getItem("PlLogisch").getUnscaledValue("WertMinLogisch").setText("Nein");
		item.getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").setText("Nein");
		item.getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").setText("Nein");

		item.getItem("Güte").getUnscaledValue("Index").set(1);
		item.getItem("Güte").getUnscaledValue("Verfahren").setText("Standard");

		long datenZeitpunkt = cal.toInstant().toEpochMilli();
		ResultData resultData = new ResultData(objekt, new DataDescription(atg, asp), datenZeitpunkt, data);

		long kontrollZeitpunkt = getKontrollZeitpunktVon(resultData);
		if (kontrollZeitpunkt > 0) {
			scheduleKontrollTask(resultData, kontrollZeitpunkt);
		}
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
	public synchronized void aktualisiereDaten(final ResultData... resultate) {
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
						Long letzteZeit = letzteEmpfangeneDatenZeitProObj.get(resultat.getObject());
						if ((letzteZeit == null) || (letzteZeit < resultat.getDataTime())) {

							/**
							 * Zeitstempel ist echt neu!
							 */
							weiterzuleitendeResultate.add(resultat);

							letzteEmpfangeneDatenZeitProObj.put(resultat.getObject(), resultat.getDataTime());
						}
						if (resultat.getData() != null) {
							final long kontrollZeitpunkt = getKontrollZeitpunktVon(resultat);
							scheduleKontrollTask(resultat, kontrollZeitpunkt);
						}
					}
				}
			}

			if ((knoten != null) && !weiterzuleitendeResultate.isEmpty()) {
				knoten.aktualisiereDaten(weiterzuleitendeResultate.toArray(new ResultData[0]));
			}
		}
	}

	private void scheduleKontrollTask(final ResultData resultat, final long kontrollZeitpunkt) {

		if (!kontrollProzess.isTerminated()) {
			// Timer starten zur Ausfallüberwachung
			kontrollProzess.schedule(Instant.ofEpochMilli(kontrollZeitpunkt), new Runnable() {
				@Override
				public void run() {

					// Schon mal vorab einen leeren
					// Datensatz erstellen
					ResultData ausfallDatum = getAusfallDatumVon(resultat);

					Long letzteZeit = letzteEmpfangeneDatenZeitProObj.get(resultat.getObject());
					if ((letzteZeit == null) || (letzteZeit < ausfallDatum.getDataTime())) {
						// Datum nicht rechtzeitig
						// angekommen, da der leere
						// Datensatz hinter dem zuletzt
						// empfangenen Datensatz liegt

						aktualisiereDaten(ausfallDatum);

						// Betriebsmeldung erzeugen
						VerwaltungPlPruefungLogischUFD verwaltung = (VerwaltungPlPruefungLogischUFD) getVerwaltung();
						OperatingMessage message = TEMPLATE
								.newMessage(verwaltung.getBetriebsmeldungsObjekt(resultat.getObject()));
						LocalTime localTime = Instant.ofEpochMilli(ausfallDatum.getDataTime())
								.atZone(ZoneId.systemDefault()).toLocalTime();
						message.put("timestamp", TIME_FORMAT.format(localTime));
						message.addId("[DUA-PP-UA01]");
					}
				}
			});
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
		long maxZeitVerzug = options.getDefaultMaxZeitVerzug();

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
