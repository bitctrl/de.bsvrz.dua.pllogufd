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

package de.bsvrz.dua.pllogufd.testmeteo;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.pllogufd.testmeteo.na.NiederschlagsArt;
import de.bsvrz.dua.pllogufd.testmeteo.ni.NiederschlagsIntensitaet;
import de.bsvrz.dua.pllogufd.testmeteo.pub.Publikation;
import de.bsvrz.dua.pllogufd.testmeteo.sw.Sichtweite;
import de.bsvrz.dua.pllogufd.testmeteo.wfd.WasserfilmDicke;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

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
public class MeteorologischeKontrolle
extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Submodul Niederschlagsart (NS).
	 */
	private final NiederschlagsArt ns = new NiederschlagsArt();

	/**
	 * Submodul Niederschlagsintensität (NI).
	 */
	private final NiederschlagsIntensitaet ni = new NiederschlagsIntensitaet();

	/**
	 * Submodul Wasserfilmdicke (WFD).
	 */
	private final WasserfilmDicke wfd = new WasserfilmDicke();

	/**
	 * Submodul Sichtweite (SW).
	 */
	private final Sichtweite sw = new Sichtweite();

	/**
	 * Submodul Publikation.
	 */
	private Publikation pub = null;

	/**
	 * Standardkonstruktor.
	 *
	 * @param stdAspekte
	 *            Informationen zu den Standardpublikationsaspekten für diese
	 *            Instanz des Moduls Pl-Prüfung formal
	 */
	public MeteorologischeKontrolle(final IStandardAspekte stdAspekte) {
		this.pub = new Publikation(stdAspekte);
	}

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		ni.setNaechstenBearbeitungsKnoten(ns);
		ni.initialisiere(dieVerwaltung);

		ns.setNaechstenBearbeitungsKnoten(wfd);
		ns.initialisiere(dieVerwaltung);

		wfd.setNaechstenBearbeitungsKnoten(sw);
		wfd.initialisiere(dieVerwaltung);

		sw.setNaechstenBearbeitungsKnoten(pub);
		sw.initialisiere(dieVerwaltung);

		pub.setNaechstenBearbeitungsKnoten(this.knoten);
		pub.setPublikation(true);
		pub.initialisiere(dieVerwaltung);
	}

	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {
		this.ni.aktualisiereDaten(resultate);
	}

	@Override
	public ModulTyp getModulTyp() {
		return null;
	}

	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung dfs) {
		// hier wird nicht publiziert (sondern im Submodul Publikation)
	}

}
