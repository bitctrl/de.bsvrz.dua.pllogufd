/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.x
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

package de.bsvrz.dua.pllogufd;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

/**
 * Instanzen dieser Klasse rufen zu bestimmten Zeitpunkten all ihre
 * Beobachter auf und teilen diesen dann eine bestimmte Information
 * des generischen Typs <code>T</code> mit. Der Zeitpunkt sowie die
 * Information können dabei während der Laufzeit verändert werden
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class KontrollProzess<T> {
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * der Timer, der den Prozess steuert
	 */
	private Timer timer = null;
	
	/**
	 * aktueller Prozess
	 */
	private Prozess prozess = null;
	
	/**
	 * nächster Zeitpunkt, zu dem dieser Prozess seine Beobachter informiert
	 */
	private long naechsterAufrufZeitpunkt = -1;
	
	/**
	 * ein Objekt mit einer bestimmten Information, das beim nächsten Aufrufzeitpunkt
	 * an alle Beobachterobjekte weitergeleitet wird
	 */
	protected T aktuelleInformation = null;
	
	/**
	 * Menge von Beobachtern, die auf diesen Prozess hören
	 */
	protected Collection<IKontrollProzessListener<T>> listenerMenge = 
			Collections.synchronizedSet(new HashSet<IKontrollProzessListener<T>>());
	
	
	
	/**
	 * Standardkonstruktor
	 */
	public KontrollProzess() {
		this.timer = new Timer();
		this.prozess = new Prozess();
	}
	
	
	/**
	 * Setzt den nächsten Zeitpunkt, zu dem dieser Prozess seine Beobachter
	 * informiert<br>
	 * <b>Achtung:</b> Wenn der nächste Aufrufzeitpunkt in der Vergangenheit
	 * liegt, wird er sofort ausgeführt. 
	 * 
	 * @param zeitpunktInMillis nächster Zeitpunkt, zu dem dieser Prozess
	 * seine Beobachter informiert
	 */
	public final synchronized void setNaechstenAufrufZeitpunkt(final long zeitpunktInMillis){
		if(this.naechsterAufrufZeitpunkt != zeitpunktInMillis){
			LOGGER.info("Der eingeplante Kontrollzeitpunkt wird verändert" + //$NON-NLS-1$
						"\nAlt: " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(this.naechsterAufrufZeitpunkt)) + //$NON-NLS-1$
						"\nNeu: " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(zeitpunktInMillis))); //$NON-NLS-1$
			this.naechsterAufrufZeitpunkt = zeitpunktInMillis;
			this.prozess.cancel();
			this.timer.purge();
			this.prozess = new Prozess();
			timer.schedule(this.prozess, new Date(this.naechsterAufrufZeitpunkt));
		}
	}

	
	/**
	 * Setzt den nächsten Zeitpunkt, zu dem dieser Prozess seine Beobachter
	 * informiert und übergibt eine Information, die zu diesem Zeitpunkt 
	 * an alle Beobachter weitergereicht werden soll. Sollte dieser Zeitpunkt identisch
	 * mit dem bislang eingeplanten Zeitpunkt sein, so werden nur die Informationen
	 * angepasst 
	 * 
	 * @param zeitpunktInMillis nächster Zeitpunkt, zu dem dieser Prozess
	 * seine Beobachter informiert
	 * @param information ein Objekt mit einer bestimmten Information, das beim nächsten
	 * Aufrufzeitpunkt an alle Beobachterobjekte weitergeleitet wird
	 */
	public final synchronized void setNaechstenAufrufZeitpunkt(final long zeitpunktInMillis, 
												  final T information){
		this.aktuelleInformation = information;			
		this.setNaechstenAufrufZeitpunkt(zeitpunktInMillis);
	}
	
	
	/**
	 * Erfragt den nächsten Zeitpunkt, zu dem dieser Prozess seine Beobachter informiert
	 * 
	 * @return nächster Zeitpunkt, zu dem dieser Prozess seine Beobachter informiert
	 */
	public final synchronized long getNaechstenAufrufZeitpunkt(){
		return this.naechsterAufrufZeitpunkt;
	}	
	
	
	/**
	 * Setzt ein Objekt mit einer bestimmten Information, das beim nächsten
	 * Aufrufzeitpunkt an alle Beobachterobjekte weitergeleitet wird 
	 * 
	 * @param information ein Objekt mit einer bestimmten Information, das beim nächsten
	 * Aufrufzeitpunkt an alle Beobachterobjekte weitergeleitet wird
	 */
	public final synchronized void setInformation(final T information){
		this.aktuelleInformation = information;
	}

	
	/**
	 * Erfragt das Objekt mit einer bestimmten Information, das beim nächsten
	 * Aufrufzeitpunkt an alle Beobachterobjekte weitergeleitet wird 
	 * 
	 * @return das Objekt mit einer bestimmten Information, das beim nächsten
	 * Aufrufzeitpunkt an alle Beobachterobjekte weitergeleitet wird
	 */
	public final synchronized T getInformation(){
		return this.aktuelleInformation;
	}


	/**
	 * Fügt diesem Element einen neuen Beobachter hinzu.
	 *
	 * @param listener
	 *            der neue Beobachter
	 */
	public final void addListener(final IKontrollProzessListener<T> listener) {
		if (listener != null) {
			synchronized (this.listenerMenge) {
				this.listenerMenge.add(listener);
			}
		}
	}
	

	/**
	 * Löscht ein Beobachterobjekt.
	 *
	 * @param listener
	 *            das zu löschende Beobachterobjekt
	 */
	public final void removeListener(
			final IKontrollProzessListener<T> listener) {
		if (listener != null) {
			synchronized (this.listenerMenge) {
				this.listenerMenge.remove(listener);
			}
		}
	}
	
	
	/**
	 * Prozess, der zu einem bestimmten Zeitpunkt alle Beobachter informiert.
	 * 
	 * @author BitCtrl Systems GmbH, Thierfelder
	 *
	 */
	protected class Prozess
	extends TimerTask{

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			synchronized (KontrollProzess.this.listenerMenge) {
				for(IKontrollProzessListener<T> listener:KontrollProzess.this.listenerMenge){
					listener.trigger(KontrollProzess.this.aktuelleInformation);
				}
			}
		}
		
	}
	
}
