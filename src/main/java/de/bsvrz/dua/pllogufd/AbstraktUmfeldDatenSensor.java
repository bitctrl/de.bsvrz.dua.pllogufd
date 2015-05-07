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
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;

/**
 * Abstrakter Umfelddatensensor, der sich auf die Parameter für seine Pl-Prüfung
 * logisch UFD anmeldet.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id: AbstraktUmfeldDatenSensor.java 53825 2015-03-18 09:36:42Z
 *          peuker $
 */
public abstract class AbstraktUmfeldDatenSensor
		implements ClientReceiverInterface {

	/**
	 * <code>asp.parameterSoll</code>.
	 */
	protected static Aspect aspParameterSoll = null;

	/**
	 * statische Verbindung zum Verwaltungsmodul.
	 */
	protected static IVerwaltung verwaltungsModul = null;

	/**
	 * Systemobjekt.
	 */
	protected final SystemObject objekt;

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
	protected AbstraktUmfeldDatenSensor(final IVerwaltung verwaltung,
			final SystemObject obj) throws DUAInitialisierungsException {
		this.objekt = obj;

		if (AbstraktUmfeldDatenSensor.verwaltungsModul == null) {
			AbstraktUmfeldDatenSensor.verwaltungsModul = verwaltung;
			AbstraktUmfeldDatenSensor.aspParameterSoll = verwaltung
					.getVerbindung().getDataModel()
					.getAspect(DaVKonstanten.ASP_PARAMETER_SOLL);
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
			throws DUAInitialisierungsException,
			UmfeldDatenSensorUnbekannteDatenartException;

	/**
	 * Fuehrt die Empfangsanmeldung durch.
	 *
	 * @throws DUAInitialisierungsException
	 *             wird weitergereicht
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 *             die Datenart des übergebenen Sensors wird nicht unterstützt
	 */
	public void init() throws DUAInitialisierungsException,
			UmfeldDatenSensorUnbekannteDatenartException {
		final Collection<DataDescription> parameterBeschreibungen = new ArrayList<DataDescription>();
		for (final AttributeGroup atg : this.getParameterAtgs()) {
			parameterBeschreibungen.add(new DataDescription(atg,
					AbstraktUmfeldDatenSensor.aspParameterSoll));
		}

		for (final DataDescription parameterBeschreibung : parameterBeschreibungen) {
			AbstraktUmfeldDatenSensor.verwaltungsModul.getVerbindung()
			.subscribeReceiver(this, this.objekt, parameterBeschreibung,
							ReceiveOptions.normal(), ReceiverRole.receiver());
		}
	}

}
