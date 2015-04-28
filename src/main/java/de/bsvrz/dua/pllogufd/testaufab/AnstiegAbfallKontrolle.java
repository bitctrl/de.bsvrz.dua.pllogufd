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

package de.bsvrz.dua.pllogufd.testaufab;

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
 * Implementierung des Moduls Anstieg-Abfall-Kontrolle. Dieses meldet sich auf
 * alle Parameter an und f�hrt mit allen �ber die Methode
 * <code>aktualisiereDaten(..)</code> �bergebenen Daten eine Pr�fung durch. Es
 * wird kontrolliert, ob die Differenz zweier zeitlich aufeinander folgender
 * Messwerte eine je Sensor parametrierbare maximale Messwertdifferenz (Betrag)
 * nicht �bersteigt. Die �berpr�fung wird aber nur vorgenommen, wenn eine Reihe
 * von Bedingungen erf�llt ist. Wird ein Messwert als �ber die
 * Anstieg-Abfall-Kontrolle als nicht plausibel erkannt, so wird der
 * entsprechende Wert auf Fehlerhaft und Implausibel zu setzen. Nach der Pr�fung
 * werden die Daten an den n�chsten Bearbeitungsknoten weitergereicht.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public class AnstiegAbfallKontrolle extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Mapt alle Systemobjekte aller erfassten Umfelddatensensoren auf
	 * assoziierte Objekte mit allen f�r die Anstieg-Abfall-Kontrolle ben�tigten
	 * Informationen.
	 */
	private final Map<SystemObject, AufAbUmfeldDatenSensor> sensoren = new HashMap<SystemObject, AufAbUmfeldDatenSensor>();

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		for (final SystemObject obj : dieVerwaltung.getSystemObjekte()) {
			try {
				this.sensoren.put(obj, new AufAbUmfeldDatenSensor(dieVerwaltung,
						obj));
			} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
				Debug.getLogger().warning(e.getMessage());
			}
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
				if (resultat != null) {
					if (resultat.getData() != null) {
						ResultData resultatNeu = resultat;

						final AufAbUmfeldDatenSensor sensor = this.sensoren
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
