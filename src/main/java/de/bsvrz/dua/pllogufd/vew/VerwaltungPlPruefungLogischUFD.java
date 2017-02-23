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

package de.bsvrz.dua.pllogufd.vew;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.clock.WaitableClock;
import de.bsvrz.dua.pllogufd.grenz.UFDGrenzwertpruefung;
import de.bsvrz.dua.pllogufd.testaufab.AnstiegAbfallKontrolle;
import de.bsvrz.dua.pllogufd.testausfall.UFDAusfallUeberwachung;
import de.bsvrz.dua.pllogufd.testdiff.UFDDifferenzialKontrolle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolle;
import de.bsvrz.dua.pllogufd.testmeteo.pub.Publikation;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapterMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.funclib.operatingMessage.OperatingMessage;

/**
 * Dieses Modul Verwaltung ist die zentrale Steuereinheit der SWE PL-Prüfung
 * logisch UFD. Seine Aufgabe besteht in der Auswertung der Aufrufparameter, der
 * Anmeldung beim Datenverteiler und der entsprechenden Initialisierung der
 * Module. Weiter ist das Modul Verwaltung für die Anmeldung der zu prüfenden
 * Daten zuständig. Die Verwaltung gibt ein Objekt des Moduls Ausfallüberwachun
 * als Beobachterobjekt an, an das die zu überprüfenden Daten durch den
 * Aktualisierungsmechanismus weitergeleitet werden. Weiterhin stellt die
 * Verwaltung die Verkettung der Module Ausfallüberwachung, PL-Prüfung formal,
 * Differenzialkontrolle, Anstieg-Abfall-Kontrolle und der Komponente
 * Meteorologische Kontrolle in dieser Reihenfolge durch die Angabe eines Moduls
 * als Beobachterobjekt des jeweiligen Vorgängermoduls her.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class VerwaltungPlPruefungLogischUFD extends AbstraktVerwaltungsAdapterMitGuete {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Betriebsmeldungs-Template für unbekannte Sensoren mit Messstelle
	 */
	public static final MessageTemplate TEMPLATE_WITH_MS = new MessageTemplate(MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN,

			MessageTemplate.fixed("Unbekannter Umfelddatensensor "), MessageTemplate.object(),
			MessageTemplate.fixed(" bei Messstelle "), MessageTemplate.variable("messstelle"),
			MessageTemplate.fixed(" entdeckt. Sensor wird ignoriert. "), MessageTemplate.ids());

	/**
	 * Betriebsmeldungs-Template für unbekannte Sensoren ohne Messstelle
	 */
	public static final MessageTemplate TEMPLATE_NO_MS = new MessageTemplate(MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN,

			MessageTemplate.fixed("Unbekannter Umfelddatensensor "), MessageTemplate.object(),
			MessageTemplate.fixed(" entdeckt. Sensor wird ignoriert. "), MessageTemplate.ids());

	private PllogUfdOptions options = new PllogUfdOptions();
	
	/**
	 * Verwendete Uhr, verwendet normalerweise die Systemzeit, kann aber von
	 * Testfällen anders gesetzt werden.
	 */
	public static WaitableClock clock = WaitableClock.systemClock();

	/**
	 * Modul Grenzwertprüfung.
	 */
	private UFDGrenzwertpruefung grenz = null;

	/**
	 * Modul UFD-Ausfallüberwachung.
	 */
	private UFDAusfallUeberwachung ausfall = null;

	/**
	 * Modul UFD-Differenzialkontrolle.
	 */
	private UFDDifferenzialKontrolle diff = null;

	/**
	 * Modul Anstieg-Abfall-Kontrolle.
	 */
	private AnstiegAbfallKontrolle aak = null;

	/**
	 * Modul Meteorologische Kontrolle.
	 */
	private MeteorologischeKontrolle mk = null;

	/**
	 * Liste mit allen relevanten Messstellen
	 */
	private final List<DUAUmfeldDatenMessStelle> _messtellen = new ArrayList<DUAUmfeldDatenMessStelle>();

	/**
	 * Alle relevanten Sensoren
	 */
	private final Set<DUAUmfeldDatenSensor> _sensoren = new HashSet<>();

	/**
	 * Map Systemobjekt -&gt; Sensor
	 */
	private final Map<SystemObject, DUAUmfeldDatenSensor> _objectZuSensor = new IdentityHashMap<>();

	/**
	 * Map Sensor -&gt; Messstelle
	 */
	private final Map<DUAUmfeldDatenSensor, DUAUmfeldDatenMessStelle> _sensorZuMessstelle = new IdentityHashMap<>();

	/**
	 * Standard-Aspekte
	 */
	private IStandardAspekte _standardAspekte;

	/**
	 * Modul Publikation
	 */
	private IBearbeitungsKnoten pub;

	@Override
    public void initialize(ClientDavInterface dieVerbindung) throws Exception {
        MessageSender.getInstance().setApplicationLabel("PLPruefung logisch UFD");
        super.initialize(dieVerbindung);
    }

	@Override
	protected void initialisiere() throws DUAInitialisierungsException {

		super.initialisiere();
		options.update(this);

		UmfeldDatenArt.initialisiere(this.verbindung);

		String infoStr = "";
		final Collection<SystemObject> plSensoren = DUAUtensilien.getBasisInstanzen(
				this.verbindung.getDataModel().getType(DUAKonstanten.TYP_UFD_SENSOR), this.verbindung,
				this.getKonfigurationsBereiche());
		final Collection<SystemObject> plMessstellen = DUAUtensilien.getBasisInstanzen(
				this.verbindung.getDataModel().getType(DUAKonstanten.TYP_UFD_MESSSTELLE), this.verbindung,
				this.getKonfigurationsBereiche());
		SystemObject[] messstellen = plMessstellen.toArray(new SystemObject[0]);

		DUAUmfeldDatenMessStelle.initialisiere(this.verbindung, messstellen);

		for (final SystemObject obj : messstellen) {
			infoStr += obj + "\n";
		}
		LOGGER.config("---\nBetrachtete Objekte:\n" + infoStr + "---\n");

		_standardAspekte = new PlLogUFDStandardAspekteVersorger(this).getStandardPubInfos();

		final Set<SystemObject> sensoren = new HashSet<>();
		for (SystemObject object : messstellen) {
			DUAUmfeldDatenMessStelle messStelle = DUAUmfeldDatenMessStelle.getInstanz(object);
			_messtellen.add(messStelle);
			// "Gültige" Sensoren ermitteln
			for (DUAUmfeldDatenSensor sensor : messStelle.getSensoren()) {
				_sensoren.add(sensor);
				_sensorZuMessstelle.put(sensor, messStelle);
				sensoren.add(sensor.getObjekt());
				_objectZuSensor.put(sensor.getObjekt(), sensor);
			}
			// Alle konfigurierten Sensoren ermitteln, auch die unbekannten
			// (kann die oben verwendete Bitctrl-Klasse nicht)
			// Für Betriebsmeldung
			for (SystemObject sensor : ((ConfigurationObject) object).getNonMutableSet("UmfeldDatenSensoren")
					.getElements()) {
				sensoren.add(sensor);
				if (!isSupportedSensor(sensor)) {
					OperatingMessage operatingMessage = TEMPLATE_WITH_MS.newMessage(sensor);
					operatingMessage.addId("[DUA-PP-UU01]");
					operatingMessage.put("messstelle", object);
					operatingMessage.send();
				}
			}
		}
		objekte = sensoren.toArray(new SystemObject[sensoren.size()]);

		for (SystemObject object : plSensoren) {
			if (!sensoren.contains(object)) {
				LOGGER.warning("Sensor ohne Messstelle", object);
				if (!isSupportedSensor(object)) {
					OperatingMessage operatingMessage = TEMPLATE_NO_MS.newMessage(object);
					operatingMessage.addId("[DUA-PP-UU02]");
					operatingMessage.send();
				}
			}
		}

		/**
		 * Instanziierung
		 */
		this.ausfall = new UFDAusfallUeberwachung();
		this.grenz = new UFDGrenzwertpruefung();
		this.diff = new UFDDifferenzialKontrolle();
		this.aak = new AnstiegAbfallKontrolle();
		this.mk = new MeteorologischeKontrolle();

		this.pub = new Publikation(_standardAspekte);

		/**
		 * Initialisierung
		 */
		this.ausfall.setNaechstenBearbeitungsKnoten(this.grenz);
		this.ausfall.initialisiere(this);

		this.grenz.setNaechstenBearbeitungsKnoten(this.diff);
		this.grenz.setPublikation(true);
		this.grenz.initialisiere(this);

		this.diff.setNaechstenBearbeitungsKnoten(this.aak);
		this.diff.initialisiere(this);

		this.aak.setNaechstenBearbeitungsKnoten(this.mk);
		this.aak.initialisiere(this);

		this.mk.setNaechstenBearbeitungsKnoten(pub);
		this.mk.setPublikation(true);
		this.mk.initialisiere(this);

		this.pub.setNaechstenBearbeitungsKnoten(null);
		this.pub.setPublikation(true);
		this.pub.initialisiere(this);

		/**
		 * Datenanmeldung
		 */
		for (final AttributeGroup atg : _standardAspekte.getAlleAttributGruppen()) {

			for (final SystemObject obj : this.objekte) {

				if (obj.getType().getAttributeGroups().contains(atg)) {

					final DataDescription datenBeschreibung = new DataDescription(atg,
							verbindung.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG));

					this.verbindung.subscribeReceiver(this, obj, datenBeschreibung, ReceiveOptions.delayed(),
							ReceiverRole.receiver());

				}

			}

		}
	}

	/**
	 * Gibt <tt>true</tt> zurück, wenn es sich um einen untersützen Sensortyp
	 * handelt
	 * 
	 * @param object
	 *            Sensor
	 * @return <tt>true</tt>, wenn es sich um einen untersützen Sensortyp
	 *         handelt, sonst <tt>false</tt>
	 */
	private boolean isSupportedSensor(final SystemObject object) {
		for (AttributeGroup atg : _standardAspekte.getAlleAttributGruppen()) {
			if (object.getType().getAttributeGroups().contains(atg)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public SWETyp getSWETyp() {
		return SWETyp.SWE_PL_PRUEFUNG_LOGISCH_UFD;
	}

	@Override
	public void update(final ResultData[] resultate) {
		this.ausfall.aktualisiereDaten(resultate);
	}

	/**
	 * Startet diese Applikation.
	 *
	 * @param argumente
	 *            Argumente der Kommandozeile
	 */
	public static void main(final String[] argumente) {
		StandardApplicationRunner.run(new VerwaltungPlPruefungLogischUFD(), argumente);
	}

	@Override
	public double getStandardGueteFaktor() {
		return 0.9;
	}

	/**
	 * Gibt alle Messstellen zurück
	 * 
	 * @return alle Messstellen
	 */
	public Collection<DUAUmfeldDatenMessStelle> getMesstellen() {
		return Collections.unmodifiableList(_messtellen);
	}

	/**
	 * Gibt alle Sensoren zurück
	 * 
	 * @return alle Sensoren
	 */
	public Collection<DUAUmfeldDatenSensor> getSensoren() {
		return Collections.unmodifiableSet(_sensoren);
	}

	/**
	 * Gibt alle Sensor-Objekte zurück
	 * 
	 * @return alle Sensor-Objekte
	 */
	public Collection<SystemObject> getSensorObjekte() {
		ArrayList<SystemObject> list = new ArrayList<>();
		for (DUAUmfeldDatenSensor umfeldDatenSensor : _sensoren) {
			list.add(umfeldDatenSensor.getObjekt());
		}
		return list;
	}

	/**
	 * Gibt zu einem Sensor-Objekt das Objekt zurück, dass in der
	 * Betriebsmeldung referenziert werden soll. Ist eine Messtelle vorhanden,
	 * wird diese zurückgegeben, sonst das ursprüngliche Objekt.
	 * 
	 * @param objekt
	 *            Systemobjekt eines Sensors
	 * @return zu einem Sensor-Objekt das Objekt, dass in der Betriebsmeldung
	 *         referenziert werden soll.
	 */
	public SystemObject getBetriebsmeldungsObjekt(final SystemObject objekt) {
		DUAUmfeldDatenMessStelle messstelle = getMessstelle(objekt);
		if (messstelle != null)
			return messstelle.getObjekt();
		return objekt;
	}

	/**
	 * Gibt zu einem Systemobjekt eines Sensors die Messstelle zurück
	 * 
	 * @param objekt
	 *            Systemobjekt eines Sensors
	 * @return zu einem Systemobjekt eines Sensors die Messstelle
	 */
	public DUAUmfeldDatenMessStelle getMessstelle(final SystemObject objekt) {
		return getMessstelle(getSensor(objekt));
	}

	/**
	 * Gibt zu einem Sensor die Messstelle zurück
	 * 
	 * @param objekt
	 *            Sensor
	 * @return zu einem Sensor die Messstelle
	 */
	public DUAUmfeldDatenMessStelle getMessstelle(final DUAUmfeldDatenSensor objekt) {
		return _sensorZuMessstelle.get(objekt);
	}

	/**
	 * Gibt zu einem Systemobjekt den Sensor zurück
	 * 
	 * @param objekt
	 *            Systemobjekt
	 * @return zu einem Systemobjekt den Sensor
	 */
	public DUAUmfeldDatenSensor getSensor(final SystemObject objekt) {
		return _objectZuSensor.get(objekt);
	}

	/**
	 * Gibt alle Messstellen zurück
	 * 
	 * @return alle Messstellen
	 */
	public Collection<DUAUmfeldDatenMessStelle> getMessstellen() {
		return _sensorZuMessstelle.values();
	}

	public PllogUfdOptions getPllogUfdOptions() {
		return options;
	}
}
