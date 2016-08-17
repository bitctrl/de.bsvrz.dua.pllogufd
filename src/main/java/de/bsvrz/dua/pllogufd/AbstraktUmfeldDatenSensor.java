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

package de.bsvrz.dua.pllogufd;

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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstrakter Umfelddatensensor, der sich auf die Parameter für seine Pl-Prüfung
 * logisch UFD anmeldet.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public abstract class AbstraktUmfeldDatenSensor implements ClientReceiverInterface {

	/**
	 * <code>asp.parameterSoll</code>.
	 */
	protected Aspect aspParameterSoll = null;

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	protected IVerwaltung verwaltungsModul = null;

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
	protected AbstraktUmfeldDatenSensor(final IVerwaltung verwaltung, final SystemObject obj)
			throws DUAInitialisierungsException {
		this.objekt = obj;
		verwaltungsModul = verwaltung;
		if (verwaltungsModul != null) {
			aspParameterSoll = verwaltungsModul.getVerbindung().getDataModel()
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
			throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException;

	/**
	 * Fuehrt die Empfangsanmeldung durch.
	 *
	 * @throws DUAInitialisierungsException
	 *             wird weitergereicht
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public void init() throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		final Collection<DataDescription> parameterBeschreibungen = new ArrayList<DataDescription>();
		for (final AttributeGroup atg : this.getParameterAtgs()) {
			parameterBeschreibungen.add(new DataDescription(atg, aspParameterSoll));
		}

		for (final DataDescription parameterBeschreibung : parameterBeschreibungen) {
			verwaltungsModul.getVerbindung().subscribeReceiver(this, this.objekt, parameterBeschreibung,
					ReceiveOptions.normal(), ReceiverRole.receiver());
		}
	}

}
