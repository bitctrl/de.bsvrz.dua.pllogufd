/**
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.x
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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import sys.funclib.InvalidArgumentException;

/**
 * Instanzen dieser Klasse rufen zu bestimmten Zeitpunkten all ihre
 * Beobachter auf. Der Zeitpunkt kann dabei w�hrend der Laufzeit ver�ndert werden
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class KontrollProzess<T> {
	
	/**
	 * Es d�rfen nur Aufrufzeitpunkte jenseits dieses Zeitfensters in der
	 * Zukunft eingeplant werden
	 */
	private static final long SICHERHEITS_ZEITFENSTER = 50;

	/**
	 * der Timer, der den Prozess steuert
	 */
	private Timer timer = null;
	
	/**
	 * aktueller Prozess
	 */
	private Prozess prozess = null;
	
	/**
	 * n�chster Zeitpunkt, zu dem dieser Prozess seine Beobachter informiert
	 */
	private long naechsterAufrufZeitpunkt = -1;
	
	/**
	 * ein Objekt mit einer bestimmten Information, das beim n�chsten Aufrufzeitpunkt
	 * an alle Beobachterobjekte weitergeleitet wird
	 */
	protected T aktuelleInformation = null;
	
	/**
	 * Menge von Beobachtern, die auf diesen Prozess h�ren
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
	 * Setzt den n�chsten Zeitpunkt, zu dem dieser Prozess seine Beobachter
	 * informiert
	 * 
	 * @param zeitpunktInMillis n�chster Zeitpunkt, zu dem dieser Prozess
	 * seine Beobachter informiert
	 * @throws InvalidArgumentException wenn der �bergebene Zeitpunkt weniger 
	 * als <code>SICHERHEITS_ZEITFENSTER</code> ms in der Zukunft liegt
	 */
	public final void setNaechstenAufrufZeitpunkt(final long zeitpunktInMillis)
	throws InvalidArgumentException{
		if(zeitpunktInMillis < System.currentTimeMillis() + SICHERHEITS_ZEITFENSTER){
			throw new InvalidArgumentException("�bergebener Zeitpunkt ist nicht einplanbar, da" + //$NON-NLS-1$
					" er weniger als " + SICHERHEITS_ZEITFENSTER + "ms in der Zukunft liegt"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		synchronized (this) {
			if(this.naechsterAufrufZeitpunkt != zeitpunktInMillis){
				this.naechsterAufrufZeitpunkt = zeitpunktInMillis;
				this.prozess.cancel();
				this.prozess = new Prozess();
				timer.schedule(this.prozess, new Date(this.naechsterAufrufZeitpunkt));		
			}			
		}
	}

	
	/**
	 * Setzt den n�chsten Zeitpunkt, zu dem dieser Prozess seine Beobachter
	 * informiert und �bergibt eine Information, die zu diesem Zeitpunkt 
	 * an alle Beobachter weitergereicht werden soll. Sollte dieser Zeitpunkt identisch
	 * mit dem bsilang eingeplanten Zeitpunkt sein, so werden nur die Informationen
	 * angepasst 
	 * 
	 * @param zeitpunktInMillis n�chster Zeitpunkt, zu dem dieser Prozess
	 * seine Beobachter informiert
	 * @param information ein Objekt mit einer bestimmten Information, das beim n�chsten
	 * Aufrufzeitpunkt an alle Beobachterobjekte weitergeleitet wird
	 * @throws InvalidArgumentException wenn der �bergebene Zeitpunkt weniger 
	 * als <code>SICHERHEITS_ZEITFENSTER</code> ms in der Zukunft liegt
	 */
	public final void setNaechstenAufrufZeitpunkt(final long zeitpunktInMillis, 
												  final T information)
	throws InvalidArgumentException{
		synchronized (this) {
			this.aktuelleInformation = information;	
		}		
		this.setNaechstenAufrufZeitpunkt(zeitpunktInMillis);
	}
	
	
	/**
	 * Erfragt den n�chsten Zeitpunkt, zu dem dieser Prozess seine Beobachter informiert
	 * 
	 * @return n�chster Zeitpunkt, zu dem dieser Prozess seine Beobachter informiert
	 */
	public final synchronized long getNaechstenAufrufZeitpunkt(){
		return this.naechsterAufrufZeitpunkt;
	}	
	
	
	/**
	 * Setzt ein Objekt mit einer bestimmten Information, das beim n�chsten
	 * Aufrufzeitpunkt an alle Beobachterobjekte weitergeleitet wird 
	 * 
	 * @param information ein Objekt mit einer bestimmten Information, das beim n�chsten
	 * Aufrufzeitpunkt an alle Beobachterobjekte weitergeleitet wird
	 */
	public final synchronized void setInformation(final T information){
		this.aktuelleInformation = information;
	}

	
	/**
	 * Erfragt das Objekt mit einer bestimmten Information, das beim n�chsten
	 * Aufrufzeitpunkt an alle Beobachterobjekte weitergeleitet wird 
	 * 
	 * @return das Objekt mit einer bestimmten Information, das beim n�chsten
	 * Aufrufzeitpunkt an alle Beobachterobjekte weitergeleitet wird
	 */
	public final synchronized T getInformation(){
		return this.aktuelleInformation;
	}


	/**
	 * F�gt diesem Element einen neuen Beobachter hinzu.
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
	 * L�scht ein Beobachterobjekt.
	 *
	 * @param listener
	 *            das zu l�schende Beobachterobjekt
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
			synchronized (KontrollProzess.this) {
				for(IKontrollProzessListener<T> listener:KontrollProzess.this.listenerMenge){
					listener.trigger(KontrollProzess.this.aktuelleInformation);
				}
			}			
		}
		
	}
	
}
