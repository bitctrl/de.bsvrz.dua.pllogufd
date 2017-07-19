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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Allgemeine Optionen der SWE, die per Startzeilen-Parameter übergeben werden.
 * 
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class PllogUfdOptions {

    private static final Debug LOGGER = Debug.getLogger();

	/**
	 * bestimmt, ob für die Plausibilitätsprüfung von WFD in Bezug zum FBZ, der
	 * Grenzwert 0 (Standard in TLS2012) oder der im Parameter
	 * "atg.ufdmsParameterMeteorologischeKontrolle" festgelegte Grenzwert
	 * verwendet wird (Unterstützung der TLS2002 und nicht konformer Systeme).
	 */
	private boolean useWfdTrockenGrenzwert = false;

	/**
	 * bestimmt, ob für die Plausibilitätsprüfung der Niederschlagsintensität, der
	 * Grenzwert 0 (Standard in TLS2012) oder der im Parameter
	 * "atg.ufdmsParameterMeteorologischeKontrolle" festgelegte Grenzwert
	 * verwendet wird (Unterstützung der TLS2002 und nicht konformer Systeme).
	 */
	private boolean useNiGrenzNS = false;
	
	private boolean fehlerhafteWertePublizieren = false;

	private final Set<Integer> ignoredMeteoRules = new LinkedHashSet<>();
	private boolean initialeAusfallKontrolle;
	private long defaultMaxZeitVerzug = -1;

	private boolean skipRules;
	
	public void update(VerwaltungPlPruefungLogischUFD verwaltung) {

		String argument = verwaltung.getArgument("useWfdTrockenGrenzwert");
		if (argument != null) {
			useWfdTrockenGrenzwert = Boolean.valueOf(argument);
		}

		argument = verwaltung.getArgument("useNiGrenzNS");
		if (argument != null) {
			useNiGrenzNS = Boolean.valueOf(argument);
		}
		
		argument = verwaltung.getArgument("fehlerhafteWertePublizieren");
		if (argument != null) {
			fehlerhafteWertePublizieren  = Boolean.valueOf(argument);
		}

		argument = verwaltung.getArgument("initialeAusfallKontrolle");
		if (argument != null) {
			initialeAusfallKontrolle = Boolean.valueOf(argument);
		}

		argument = verwaltung.getArgument("defaultMaxZeitVerzug");
		if (argument != null) {
			try {
				defaultMaxZeitVerzug = Long.parseLong(argument);
			} catch (NumberFormatException e) {
				LOGGER.warning("Fehler beim Einlesen des Parameters für den Standard-Zeitverzug", e);
			}
		}
		
		argument = verwaltung.getArgument("ignoriereRegeln");
		if (argument != null) {
			String[] items = argument.split(",");
			for( String item : items) {
				try {
					ignoredMeteoRules.add(Integer.parseInt(item));
				} catch (NumberFormatException e) {
					LOGGER.warning("Fehler beim Einlesen des Parameters für ignorierte Regeln", e);
				}
			}
		}
		
		argument = verwaltung.getArgument("skipRules");
		if (argument != null) {
			skipRules = Boolean.valueOf(argument);
		}
	}

	public boolean isUseNiGrenzNS() {
		return useNiGrenzNS;
	}
	public boolean isUseWfdTrockenGrenzwert() {
		return useWfdTrockenGrenzwert;
	}

	public boolean isFehlerhafteWertePublizieren() {
		return fehlerhafteWertePublizieren;
	}

	public Set<Integer> getIgnoredMeteoRules() {
		return Collections.unmodifiableSet(ignoredMeteoRules);
	}

	public boolean isInitialeAusfallKontrolle() {
		return initialeAusfallKontrolle;
	}

	public long getDefaultMaxZeitVerzug() {
		return defaultMaxZeitVerzug ;
	}

	public boolean isSkipRules() {
		return skipRules;
	}
}
