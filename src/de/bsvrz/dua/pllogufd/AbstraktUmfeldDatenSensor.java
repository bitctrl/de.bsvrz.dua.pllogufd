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

package de.bsvrz.dua.pllogufd;

import java.util.ArrayList;
import java.util.Collection;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Abstrakter Umfelddatensensor, der sich auf die Parameter für seine Pl-Prüfung
 * logisch UFD anmeldet.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public abstract class AbstraktUmfeldDatenSensor extends AbstractSystemObjekt
		implements Comparable<AbstraktUmfeldDatenSensor>,
		ClientReceiverInterface {

	/**
	 * Debug-Logger.
	 */
	protected static final Debug LOGGER = Debug.getLogger();

	/**
	 * <code>asp.parameterSoll</code>.
	 */
	protected static Aspect aspParameterSoll = null;

	/**
	 * statische Verbindung zum Verwaltungsmodul.
	 */
	protected static IVerwaltung verwaltungsModul = null;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 *            das mit dieser Instanz zu assoziierende Systemobjekt (vom Typ
	 *            <code>typ.umfeldDatenSensor</code>)
	 * @throws DUAInitialisierungsException
	 *             wird weitergereicht
	 */
	protected AbstraktUmfeldDatenSensor(IVerwaltung verwaltung, SystemObject obj)
			throws DUAInitialisierungsException {
		super(obj);

		if (verwaltungsModul == null) {
			verwaltungsModul = verwaltung;
			aspParameterSoll = verwaltung.getVerbindung().getDataModel()
					.getAspect(DaVKonstanten.ASP_PARAMETER_SOLL);
		}

		Collection<DataDescription> parameterBeschreibungen = new ArrayList<DataDescription>();
		for (AttributeGroup atg : this.getParameterAtgs()) {
			parameterBeschreibungen.add(new DataDescription(atg,
					aspParameterSoll, (short) 0));
		}

		for (DataDescription parameterBeschreibung : parameterBeschreibungen) {
			verwaltungsModul.getVerbindung().subscribeReceiver(this, obj,
					parameterBeschreibung, ReceiveOptions.normal(),
					ReceiverRole.receiver());
		}
	}

	/**
	 * Erfragt die Parameter-Attributgruppen, auf die sich dieses Objekt
	 * anmelden soll.
	 * 
	 * @return eine ggf. leere Menge von Attributgruppen
	 * @throws DUAInitialisierungsException
	 *             wenn ein Fehler bei der Bestimmung der Attributgruppen
	 *             auftritt
	 */
	protected abstract Collection<AttributeGroup> getParameterAtgs()
			throws DUAInitialisierungsException;

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(AbstraktUmfeldDatenSensor that) {
		return new Long(this.getSystemObject().getId()).compareTo(that
				.getSystemObject().getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemObjektTyp getTyp() {
		return new SystemObjektTyp() {

			public Class<? extends SystemObjekt> getKlasse() {
				return AbstraktUmfeldDatenSensor.class;
			}

			public String getPid() {
				return getSystemObject().getType().getPid();
			}

		};
	}

}
