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

package de.bsvrz.dua.pllogufd.testmeteo.pub;

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

import java.util.ArrayList;
import java.util.Collection;

/**
 * In diesem Submodul findet lediglich die Publikation der Daten nach den
 * Vorgaben der Datenflusssteuerung für das Modul Pl-Prüfung logisch UFD statt.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id: Publikation.java 53837 2015-03-18 11:45:45Z peuker $
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
		this.standardAspekte = stdAspekte;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		if (this.publizieren) {
			this.publikationsAnmeldungen
			.modifiziereObjektAnmeldung(this.standardAspekte
					.getStandardAnmeldungen(this.verwaltung
							.getSystemObjekte()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {
		if (this.publizieren) {
			if (resultate != null) {
				for (final ResultData resultat : resultate) {
					if (resultat != null/* && resultat.getData() != null */) {
						final ResultData publikationsDatum = iDfsMod
								.getPublikationsDatum(resultat, resultat
										.getData(), standardAspekte
										.getStandardAspekt(resultat));
						if (publikationsDatum != null) {
							this.publikationsAnmeldungen
							.sende(publikationsDatum);
						}
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModulTyp getModulTyp() {
		return ModulTyp.PL_PRUEFUNG_LOGISCH_UFD;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung iDfs) {
		this.iDfsMod = iDfs.getDFSFuerModul(this.verwaltung.getSWETyp(),
				this.getModulTyp());

		if (this.publizieren) {
			Collection<DAVObjektAnmeldung> anmeldungenStd = new ArrayList<DAVObjektAnmeldung>();

			if (this.standardAspekte != null) {
				anmeldungenStd = this.standardAspekte
						.getStandardAnmeldungen(this.verwaltung
								.getSystemObjekte());
			}

			final Collection<DAVObjektAnmeldung> anmeldungen = this.iDfsMod
					.getDatenAnmeldungen(this.verwaltung.getSystemObjekte(),
							anmeldungenStd);

			synchronized (this) {
				this.publikationsAnmeldungen
				.modifiziereObjektAnmeldung(anmeldungen);
			}
		}
	}
}
