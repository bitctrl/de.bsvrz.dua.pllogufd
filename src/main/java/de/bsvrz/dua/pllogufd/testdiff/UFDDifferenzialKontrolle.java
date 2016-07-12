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

package de.bsvrz.dua.pllogufd.testdiff;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Das Modul Differenzialkontrolle meldet sich auf alle Parameter an und führt
 * mit allen über die Methode <code>aktualisiereDaten(..)</code> übergebenen
 * Daten eine Prüfung durch. Diese kontrolliert, ob bestimmte Werte innerhalb
 * eines bestimmten Intervalls konstant geblieben sind. Ist dies der Fall, so
 * werden diese als Implausibel und Fehlerhaft gesetzt. Nach der Prüfung werden
 * die Daten an den nächsten Bearbeitungsknoten weitergereicht
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id: UFDDifferenzialKontrolle.java 53825 2015-03-18 09:36:42Z peuker
 *          $
 */
public class UFDDifferenzialKontrolle extends AbstraktBearbeitungsKnotenAdapter {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * Mapt alle Systemobjekte aller erfassten Umfelddatensensoren auf
	 * assoziierte Objekte mit allen für die Differentialkontrolle benötigten
	 * Informationen.
	 */
	private final Map<SystemObject, DiffUmfeldDatenSensor> sensoren = new HashMap<SystemObject, DiffUmfeldDatenSensor>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		for (final SystemObject obj : dieVerwaltung.getSystemObjekte()) {
			DiffUmfeldDatenSensor sensor;
			try {
				sensor = new DiffUmfeldDatenSensor(dieVerwaltung, obj);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException ex) {
				LOGGER.warning(
						"UmfeldDatenSensor '" + obj
								+ "': wird nicht verarbeitet: "
								+ ex.getMessage());
				continue;
			}
			this.sensoren.put(obj, sensor);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {
		if (resultate != null) {
			final Collection<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();

			for (final ResultData resultat : resultate) {
				if(resultat != null){
					for(DiffUmfeldDatenSensor diffUmfeldDatenSensor : sensoren.values()) {
						DiffUmfeldDatenSensor.SensorNA sensorNA = diffUmfeldDatenSensor.getSensorNA();
						if(sensorNA != null && sensorNA.getSystemObject().equals(resultat.getObject())){
							sensorNA.update(resultat);
						}
					}
				}
			}
			for (final ResultData resultat : resultate) {
				if (resultat != null) {
					if (resultat.getData() != null) {
						ResultData resultatNeu = resultat;

						final DiffUmfeldDatenSensor sensor = this.sensoren
								.get(resultat.getObject());

						Data data = null;
						if (sensor != null) {
							data = sensor.plausibilisiere(resultat);
						}

						if (data != null) {
							resultatNeu = new ResultData(resultat.getObject(),
									resultat.getDataDescription(),
									resultat.getDataTime(), data);
						}

						weiterzuleitendeResultate.add(resultatNeu);
					} else {
						weiterzuleitendeResultate.add(resultat);
					}
				}
			}

			if ((this.knoten != null) && !weiterzuleitendeResultate.isEmpty()) {
				this.knoten.aktualisiereDaten(weiterzuleitendeResultate
						.toArray(new ResultData[0]));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModulTyp getModulTyp() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung dfs) {
		// hier wird nicht publiziert
	}

}
