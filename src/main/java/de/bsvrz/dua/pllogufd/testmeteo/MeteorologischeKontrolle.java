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

package de.bsvrz.dua.pllogufd.testmeteo;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.pllogufd.vew.VerwaltungPlPruefungLogischUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Diese Klasse hat die Aufgabe vergleichbare oder meteorologisch sich
 * beeinflussende Messgrößen zueinander in Beziehung zu setzen, wenn diese in
 * den vorangegangenen Einzelprüfungen nicht als Implausibel gekennzeichnet
 * wurden. Wird ein Messwert über die Meteorologische Kontrolle als nicht
 * plausibel erkannt, so wird der entsprechende Wert auf Fehlerhaft und
 * Implausibel gesetzt.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class MeteorologischeKontrolle extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Alle betrachteten Messstellen zu den einzelnen Plausibilisierungs-Instanzen
	 */
	private Map<DUAUmfeldDatenMessStelle, MeteoMessstelle> _meteoMessstellen = new HashMap<>();


	/**
	 * Verwaltungsmodul
	 */
	private VerwaltungPlPruefungLogischUFD _verwaltung;

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
		_verwaltung = (VerwaltungPlPruefungLogischUFD) dieVerwaltung;
		for(DUAUmfeldDatenMessStelle messStelle : _verwaltung.getMessstellen()) {
			_meteoMessstellen.put(messStelle, new MeteoMessstelle(dieVerwaltung.getVerbindung(), messStelle, _verwaltung.getPllogUfdOptions()));
		}
	}

	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {
		final List<ResultData> weiterzuleitendeResultate = new ArrayList<>();

		for(ResultData resultData : resultate) {
			DUAUmfeldDatenSensor sensor = _verwaltung.getSensor(resultData.getObject());
			DUAUmfeldDatenMessStelle messstelle = _verwaltung.getMessstelle(sensor);
			if(messstelle == null){
				weiterzuleitendeResultate.add(resultData);
			}
			else {
				MeteoMessstelle meteoMessstelle = _meteoMessstellen.get(messstelle);
				if(meteoMessstelle == null) throw new AssertionError();
				weiterzuleitendeResultate.addAll(meteoMessstelle.updateData(resultData));
			}
		}
		
		if ((knoten != null) && !weiterzuleitendeResultate.isEmpty()) {
			knoten.aktualisiereDaten(weiterzuleitendeResultate
					                         .toArray(new ResultData[0]));
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
