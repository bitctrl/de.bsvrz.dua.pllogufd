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

package de.bsvrz.dua.pllogufd.testdiff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

/**
 * Das Modul Differenzialkontrolle meldet sich auf alle Parameter an und führt
 * mit allen über die Methode <code>aktualisiereDaten(..)</code> übergebenen
 * Daten eine Prüfung durch. Diese kontrolliert, ob bestimmte Werte innerhalb
 * eines bestimmten Intervalls konstant geblieben sind. Ist dies der Fall, so
 * werden diese als Implausibel und Fehlerhaft gesetzt. Nach der Prüfung werden
 * die Daten an den nächsten Bearbeitungsknoten weitergereicht
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class UFDDifferenzialKontrolle
extends AbstraktBearbeitungsKnotenAdapter {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * Mapt alle Systemobjekte aller erfassten Umfelddatensensoren auf
	 * assoziierte Objekte mit allen für die Differentialkontrolle benötigten
	 * Informationen.
	 */
	private final Map<SystemObject, DiffUmfeldDatenSensor> sensoren = new HashMap<>();

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		for (final SystemObject obj : dieVerwaltung.getSystemObjekte()) {
			DiffUmfeldDatenSensor sensor;
			try {
				sensor = new DiffUmfeldDatenSensor(dieVerwaltung, obj);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException ex) {
				UFDDifferenzialKontrolle.LOGGER.warning("UmfeldDatenSensor '"
						+ obj + "': wird nicht verarbeitet: "
						+ ex.getMessage());
				continue;
			}
			this.sensoren.put(obj, sensor);
		}
	}

	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {
		if (resultate != null) {
			final Collection<ResultData> weiterzuleitendeResultate = new ArrayList<>();

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

			if ((getKnoten() != null) && !weiterzuleitendeResultate.isEmpty()) {
				getKnoten().aktualisiereDaten(
						weiterzuleitendeResultate.toArray(new ResultData[0]));
			}
		}
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
