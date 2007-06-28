/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
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
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.pllogufd.testausfall;

import java.util.SortedSet;
import java.util.TreeSet;


/**
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AusfallKontrollZeitpunkt<E extends Comparable<? extends E>> 
implements Comparable<AusfallKontrollZeitpunkt<?>>{

	/**
	 * Zeitpunkt der Kontrolle
	 */
	private long kontrollZeitStempel = -1;
	
	/**
	 * Menge der zu kontrollierenden Objekte
	 */
	private SortedSet<E> objektMenge = new TreeSet<E>(); 

	
	
	public final synchronized SortedSet<E> getObjektMenge(){
		return objektMenge;
	}
	
	
	public final synchronized boolean add(final E kontrollObjekt){
		return this.objektMenge.add(kontrollObjekt);
	}
	
	
	public final synchronized boolean remove(final E kontrollObjekt){
		return this.objektMenge.remove(kontrollObjekt);
	}
	
	
	/**
	 * Erfragt den Kontrollzeitstempel
	 * 
	 * @return den Kontrollzeitstempel
	 */
	public final long getKontrollZeitStempel(){
		return this.kontrollZeitStempel;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ergebnis = false;
		
		if(obj instanceof AusfallKontrollZeitpunkt){
			AusfallKontrollZeitpunkt<?> that = (AusfallKontrollZeitpunkt<?>)obj;
			ergebnis = this.getKontrollZeitStempel() == that.getKontrollZeitStempel();
		}
		
		return ergebnis;
	}


	/**
	 * {@inheritDoc}
	 */
	public int compareTo(AusfallKontrollZeitpunkt<?> that) {
		return new Long(this.getKontrollZeitStempel()).compareTo(that.getKontrollZeitStempel());
	}
	
}
