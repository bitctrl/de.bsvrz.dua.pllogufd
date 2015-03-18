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

package de.bsvrz.dua.pllogufd.testmeteo.sw;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.testmeteo.AbstraktMeteoUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Parameter für die meteorologische Kontrolle "Sichtweiten".
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class SichtweitenParameter extends AbstraktMeteoUmfeldDatenSensor {

	/**
	 * Wenn SW <= SWgrenzSW und NS = 'kein Niederschlag' und RLF <
	 * SWgrenzTrockenRLF, dann SW implausibel. Wenn SW <= SWgrenzSW und NS =
	 * Niederschlag' und NI > 0,5 mm/h, dann SW implausibel
	 */
	private UmfeldDatenSensorWert swGrenzTrockenRLF = null;

	/**
	 * Wenn SW <= SWgrenzSW und NS = 'kein Niederschlag' und RLF <
	 * SWgrenzTrockenRLF, dann SW implausibel. Wenn SW <= SWgrenzSW und NS =
	 * Niederschlag' und NI > 0,5 mm/h, dann SW implausibel.
	 */
	private UmfeldDatenSensorWert swGrenzSW = null;

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
	public SichtweitenParameter(IVerwaltung verwaltung, SystemObject obj)
			throws DUAInitialisierungsException {
		super(verwaltung, obj);
		swGrenzTrockenRLF = new UmfeldDatenSensorWert(
				UmfeldDatenArt.rlf);
		swGrenzSW = new UmfeldDatenSensorWert(
				UmfeldDatenArt.sw);
		this.init();
	}

	/**
	 * Erfragt <code>SWgrenzTrockenRLF</code>.
	 * 
	 * @return SWgrenzTrockenRLF
	 */
	public final synchronized UmfeldDatenSensorWert getSWgrenzTrockenRLF() {
		return this.swGrenzTrockenRLF;
	}

	/**
	 * Erfragt <code>SWgrenzSW</code>.
	 * 
	 * @return SWgrenzSW
	 */
	public final synchronized UmfeldDatenSensorWert getSWgrenzSW() {
		return this.swGrenzSW;
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if (resultate != null) {
			for (ResultData resultat : resultate) {
				if (resultat != null && resultat.getData() != null) {
					synchronized (this) {
						this.swGrenzTrockenRLF
								.setWert(resultat.getData().getUnscaledValue(
										"SWgrenzTrockenRLF").longValue()); //$NON-NLS-1$
						this.swGrenzSW.setWert(resultat.getData()
								.getUnscaledValue("SWgrenzSW").longValue()); //$NON-NLS-1$
						Debug.getLogger()
								.info("Neue Parameter für (" + resultat.getObject() + "):\n" //$NON-NLS-1$ //$NON-NLS-2$
										+ this);
					}
					this.parameterInitialisiert = true;
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "swGrenzTrockenRLF = " + swGrenzTrockenRLF + //$NON-NLS-1$
				"\nswGrenzSW = " + swGrenzSW; //$NON-NLS-1$
	}

}
