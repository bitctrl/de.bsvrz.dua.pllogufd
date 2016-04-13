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

package de.bsvrz.dua.pllogufd.testmeteo.pub;

import java.util.ArrayList;
import java.util.Collection;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.av.DAVObjektAnmeldung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.DFSKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerungFuerModul;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * In diesem Submodul findet lediglich die Publikation der Daten nach den
 * Vorgaben der Datenflusssteuerung für das Modul Pl-Prüfung logisch UFD statt.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class Publikation extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Parameter zur Datenflusssteuerung für diese SWE und dieses Modul.
	 */
	private IDatenFlussSteuerungFuerModul iDfsMod = DFSKonstanten.STANDARD;

	/**
	 * Standardkonstruktor.
	 *
	 * @param stdAspekte
	 *            Informationen zu den Standardpublikationsaspekten für diese
	 *            Instanz des Moduls Pl-Prüfung logisch UFD
	 */
	public Publikation(final IStandardAspekte stdAspekte) {
		setStandardAspekte(stdAspekte);
	}

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		if (isPublizieren()) {
			getPublikationsAnmeldungen().modifiziereObjektAnmeldung(
					getStandardAspekte().getStandardAnmeldungen(
							getVerwaltung().getSystemObjekte()));
		}
	}

	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {
		if (isPublizieren()) {
			if (resultate != null) {
				for (final ResultData resultat : resultate) {
					if (resultat != null/* && resultat.getData() != null */) {
						final ResultData publikationsDatum = iDfsMod
								.getPublikationsDatum(resultat,
										resultat.getData(), getStandardAspekte()
										.getStandardAspekt(resultat));
						if (publikationsDatum != null) {
							getPublikationsAnmeldungen()
							.sende(publikationsDatum);
						}
					}
				}
			}
		}
	}

	@Override
	public ModulTyp getModulTyp() {
		return ModulTyp.PL_PRUEFUNG_LOGISCH_UFD;
	}

	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung iDfs) {
		this.iDfsMod = iDfs.getDFSFuerModul(getVerwaltung().getSWETyp(),
				this.getModulTyp());

		if (isPublizieren()) {
			Collection<DAVObjektAnmeldung> anmeldungenStd = new ArrayList<>();

			if (getStandardAspekte() != null) {
				anmeldungenStd = getStandardAspekte().getStandardAnmeldungen(
						getVerwaltung().getSystemObjekte());
			}

			final Collection<DAVObjektAnmeldung> anmeldungen = this.iDfsMod
					.getDatenAnmeldungen(getVerwaltung().getSystemObjekte(),
							anmeldungenStd);

			synchronized (this) {
				getPublikationsAnmeldungen()
				.modifiziereObjektAnmeldung(anmeldungen);
			}
		}
	}
}
