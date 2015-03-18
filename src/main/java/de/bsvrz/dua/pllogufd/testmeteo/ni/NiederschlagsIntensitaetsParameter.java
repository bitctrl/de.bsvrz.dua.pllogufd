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

package de.bsvrz.dua.pllogufd.testmeteo.ni;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.testmeteo.AbstraktMeteoUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Parameter für die meteorologische Kontrolle "Niederschlagsintensität".
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class NiederschlagsIntensitaetsParameter extends
		AbstraktMeteoUmfeldDatenSensor {

	/**
	 * Wenn NS= 'Niederschlag' und NI = 0 mm/h und RLF < NIgrenzNassRLF, dann NI
	 * implausibel.
	 */
	private UmfeldDatenSensorWert niGrenzNassRLF;

	/**
	 * Überhalb dieses Wertes wird angenommen, dass Niederschlag herrscht.
	 */
	private UmfeldDatenSensorWert niGrenzNassNI;

	/**
	 * Wenn NS = 'kein Niederschlag' und NI > NIminNI und RLF < NIgrenzTrockenRLF, dann NI implausibel.
	 */
	private UmfeldDatenSensorWert niMinNI;

	/**
	 * Wenn NI > 0,5 mm/h und WDF = 0 mm und RLF < NIgrenzTrockenRLF für
	 * Zeitraum > NIminTrockenRLF, dann NI implausibel.
	 */
	private UmfeldDatenSensorWert niGrenzTrockenRLF;

	/**
	 * Wenn NI > 0,5 mm/h und WDF = 0 mm und RLF < NIgrenzTrockenRLF für
	 * Zeitraum > NIminTrockenRLF, dann NI implausibel.
	 */
	private long niMinTrockenRLF = -1;

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
	public NiederschlagsIntensitaetsParameter(IVerwaltung verwaltung,
			SystemObject obj) throws DUAInitialisierungsException {
		super(verwaltung, obj);
		niGrenzNassRLF = new UmfeldDatenSensorWert(
				UmfeldDatenArt.rlf);
		niGrenzNassNI = new UmfeldDatenSensorWert(
				UmfeldDatenArt.ni);
		niMinNI = new UmfeldDatenSensorWert(
				UmfeldDatenArt.ni);
		niGrenzTrockenRLF = new UmfeldDatenSensorWert(
				UmfeldDatenArt.rlf);
		this.init();
	}

	/**
	 * Erfragt <code>NIGrenzNassRLF</code>.
	 * 
	 * @return NIGrenzNassRLF
	 */
	public final synchronized UmfeldDatenSensorWert getNIGrenzNassRLF() {
		return this.niGrenzNassRLF;
	}

	/**
	 * Erfragt <code>NIGrenzNassNI</code>.
	 * 
	 * @return NIGrenzNassNI
	 */
	public final synchronized UmfeldDatenSensorWert getNIGrenzNassNI() {
		return this.niGrenzNassNI;
	}

	/**
	 * Erfragt <code>NIminNI</code>.
	 * 
	 * @return NIminNI
	 */
	public final synchronized UmfeldDatenSensorWert getNIminNI() {
		return this.niMinNI;
	}

	/**
	 * Erfragt <code>NIGrenzTrockenRLF</code>.
	 * 
	 * @return NIGrenzTrockenRLF
	 */
	public final synchronized UmfeldDatenSensorWert getNIGrenzTrockenRLF() {
		return this.niGrenzTrockenRLF;
	}

	/**
	 * Erfragt <code>NIminTrockenRLF</code>.
	 * 
	 * @return NIminTrockenRLF
	 */
	public final long getNIminTrockenRLF() {
		return this.niMinTrockenRLF;
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if (resultate != null) {
			for (ResultData resultat : resultate) {
				if (resultat != null && resultat.getData() != null) {
					synchronized (this) {
						this.niGrenzNassRLF
								.setWert(resultat.getData().getUnscaledValue(
										"NIgrenzNassRLF").longValue()); //$NON-NLS-1$
						this.niGrenzNassNI.setWert(resultat.getData()
								.getUnscaledValue("NIgrenzNassNI").longValue()); //$NON-NLS-1$
						this.niMinNI.setWert(resultat.getData()
								.getUnscaledValue("NIminNI").longValue()); //$NON-NLS-1$
						this.niGrenzTrockenRLF
								.setWert(resultat.getData().getUnscaledValue(
										"NIgrenzTrockenRLF").longValue()); //$NON-NLS-1$
						this.niMinTrockenRLF = resultat.getData().getTimeValue(
								"NIminTrockenRLF").getMillis(); //$NON-NLS-1$
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
		return "niGrenzNassRLF = " + niGrenzNassRLF + //$NON-NLS-1$
				"\nniGrenzNassNI = " + niGrenzNassNI + //$NON-NLS-1$
				"\nniMinNI = " + niMinNI + //$NON-NLS-1$
				"\nniGrenzTrockenRLF = " + niGrenzTrockenRLF + //$NON-NLS-1$
				"\nniMinTrockenRLF = " + niMinTrockenRLF; //$NON-NLS-1$
	}

}
