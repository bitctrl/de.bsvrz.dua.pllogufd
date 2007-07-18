/**
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.3 Pl-Pr�fung logisch UFD
 * Copyright (C) 2007 BitCtrl Systems GmbH 
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

package de.bsvrz.dua.pllogufd;

import java.util.Random;

import de.bsvrz.sys.funclib.bitctrl.app.Pause;

import stauma.dav.clientside.ClientDavInterface;
import sys.funclib.ArgumentList;
import sys.funclib.application.StandardApplication;
import sys.funclib.application.StandardApplicationRunner;

/**
 * Stellt eine Datenverteiler-Verbindung
 * zur Verf�gung.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public class DAVTest {

	/**
	 * Verbindungsdaten
	 */
	private static final String[] CON_DATA = new String[] {
			"-datenverteiler=192.168.1.191:8083", //$NON-NLS-1$ 
			"-benutzer=Tester", //$NON-NLS-1$
			"-authentifizierung=c:\\passwd1", //$NON-NLS-1$
			"-debugLevelStdErrText=INFO", //$NON-NLS-1$
			"-debugLevelFileText=INFO" }; //$NON-NLS-1$

	/**
	 * Verbindung zum Datenverteiler
	 */
	protected static ClientDavInterface VERBINDUNG = null;

	/**
	 * Randomizer
	 */
	public static Random R = new Random(System.currentTimeMillis());

	
	/**
	 * Erfragt bzw. initialisiert eine
	 * Datenverteiler-Verbindung
	 * 
	 * @return die Datenverteiler-Verbindung
	 * @throws Exception falls die Verbindung nicht
	 * hergestellt werden konnte
	 */
	public static final ClientDavInterface getDav()
	throws Exception {
		
		if(VERBINDUNG == null) {
			StandardApplicationRunner.run(new StandardApplication() {
	
				public void initialize(ClientDavInterface connection)
						throws Exception {
					DAVTest.VERBINDUNG = connection;
				}
	
				public void parseArguments(ArgumentList argumentList)
						throws Exception {
					//
				}
	
			}, CON_DATA);
		}
		
		return VERBINDUNG;
	}
	
	
	/**
	 * Wartet bis zu dem �bergebenen Zeitpunkt
	 * 
	 * @param zeitStempel ein Zeitstempel in ms
	 */
	public static final void warteBis(final long zeitStempel){
		while(System.currentTimeMillis() <= zeitStempel){
			Pause.warte(5L);
		}
	}


	/**
	 * Erfragt einen Array mit zuf�lligen Zahlen von
	 * 0 bis <code>anzahl</code>. Jede Zahl darf nur 
	 * einmal im Array vorkommen.
	 * 
	 * @param anzahl die Obergrenze
	 * @return Array mit zuf�lligen Zahlen von
	 * 0 bis <code>anzahl</code>
	 */
	public static final int[] getZufaelligeZahlen(int anzahl){
		int belegt = 0;
		int[] zahlen = new int[anzahl];
		for(int i = 0; i<anzahl; i++)zahlen[i] = -1;
		
		while(belegt < anzahl){
			int index = R.nextInt(anzahl);
			if(zahlen[index] == -1)zahlen[index] = belegt++;
		}
		
		return zahlen;
	}

}
