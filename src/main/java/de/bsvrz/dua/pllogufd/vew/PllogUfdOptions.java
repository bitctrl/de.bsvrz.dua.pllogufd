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
 * BitCtrl Systems GmbH
 * Weißenfelser Straße 67
 * 04229 Leipzig
 * phone: +49 341 49067 0 
 * mail: <info@bitctrl.de>
 */

package de.bsvrz.dua.pllogufd.vew;

/**
 * Allgemeine Optionen der SWE, die per Startzeilen-Parameter übergeben werden.
 * 
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class PllogUfdOptions {

	/**
	 * bestimmt, ob für die Plausibilitätsprüfung von WFD in Bezug zum FBZ, der
	 * Grenzwert 0 (Standard in TLS2012) oder der im Parameter
	 * "atg.ufdmsParameterMeteorologischeKontrolle" festgelegte Grenzwert
	 * verwendet wird (Unterstützung der TLS2002 und nicht konformer Systeme).
	 */
	private boolean useWfdTrockenGrenzwert = false;
	private boolean fehlerhafteWertePublizieren = false;

	public void update(VerwaltungPlPruefungLogischUFD verwaltung) {

		String argument = verwaltung.getArgument("useWfdTrockenGrenzwert");
		if (argument != null) {
			useWfdTrockenGrenzwert = Boolean.valueOf(argument);
		}

		argument = verwaltung.getArgument("fehlerhafteWertePublizieren");
		if (argument != null) {
			fehlerhafteWertePublizieren  = Boolean.valueOf(argument);
		}

	}

	public boolean isUseWfdTrockenGrenzwert() {
		return useWfdTrockenGrenzwert;
	}

	public boolean isFehlerhafteWertePublizieren() {
		return fehlerhafteWertePublizieren;
	}
}