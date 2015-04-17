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

package de.bsvrz.dua.pllogufd.testaufab;

import java.util.Collection;
import java.util.HashSet;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.AbstraktUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Assoziiert einen Umfelddatensensor mit dessen Parametern und Werten bzgl. der
 * Anstieg-Abfall-Kontrolle
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public class AufAbUmfeldDatenSensor extends AbstraktUmfeldDatenSensor {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * aktuelle Parameter für die Anstieg-Abfall-Kontrolle dieses
	 * Umfelddatensensors.
	 */
	private UniversalAtgUfdsAnstiegAbstiegKontrolle parameter = null;

	/**
	 * letztes für diesen Umfelddatensesor empfangenes Datum.
	 */
	private UmfeldDatenSensorDatum letzterWert = null;

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 *            das Sensor-Objekt
	 * @throws DUAInitialisierungsException
	 *             wenn die Instaziierung fehlschlägt
	 * @throws UmfeldDatenSensorUnbekannteDatenartException 
	 */
	protected AufAbUmfeldDatenSensor(final IVerwaltung verwaltung,
			final SystemObject obj) throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, obj);
		super.init();
	}

	/**
	 * {@inheritDoc}
	 * @throws UmfeldDatenSensorUnbekannteDatenartException 
	 */
	@Override
	protected Collection<AttributeGroup> getParameterAtgs()
			throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		if (this.objekt == null) {
			throw new NullPointerException(
					"Parameter können nicht bestimmt werden," + //$NON-NLS-1$
					" da noch kein Objekt festgelegt ist"); //$NON-NLS-1$
		}

		final Collection<AttributeGroup> parameterAtgs = new HashSet<AttributeGroup>();
		
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(this.objekt);
		if (datenArt == null) {
			throw new UmfeldDatenSensorUnbekannteDatenartException(
					"Datenart von Umfelddatensensor " + this.objekt + //$NON-NLS-1$ 
							" (" + objekt.getType()
							+ ") konnte nicht identifiziert werden"); //$NON-NLS-1$
		}

		final String atgPid = "atg.ufdsAnstiegAbstiegKontrolle" + datenArt.getName();

		final AttributeGroup atg = AbstraktUmfeldDatenSensor.verwaltungsModul
				.getVerbindung().getDataModel().getAttributeGroup(atgPid);

		if (atg != null) {
			parameterAtgs.add(atg);
		} else {
			throw new DUAInitialisierungsException(
					"Es konnte keine Parameter-Attributgruppe für die " + //$NON-NLS-1$
							"Anstieg-Abfall-Kontrolle des Objektes "//$NON-NLS-1$
							+ this.objekt + " bestimmt werden\n" + //$NON-NLS-1$
							"Atg-Name: " + atgPid); //$NON-NLS-1$
		}

		return parameterAtgs;
	}

	/**
	 * Hier findet die Prüfung eines Datums statt. Diese findet nur für den Fall
	 * statt, dass das empfangene Datum weder als Implausibel, Fehlerhaft noch
	 * Nicht ermittelbar gekennzeichnet ist. Das empfangene Datum wird
	 * gespeichert
	 *
	 * @param resultat
	 *            ein Roh-Datum eines Umfelddatensensors
	 * @return das gekennzeichnete Datum oder <code>null</code> wenn das Datum
	 *         plausibel ist
	 */
	public final Data plausibilisiere(final ResultData resultat) {
		Data copy = null;

		if ((resultat != null) && (resultat.getData() != null)) {
			final UmfeldDatenSensorDatum wert = new UmfeldDatenSensorDatum(
					resultat);

			if ((this.letzterWert != null)
					&& !this.letzterWert.getWert().isFehlerhaft()
					&& !this.letzterWert.getWert()
					.isFehlerhaftBzwNichtErmittelbar()
					&& !this.letzterWert.getWert().isNichtErmittelbar()
					&& (this.letzterWert
							.getStatusMessWertErsetzungImplausibel() != DUAKonstanten.JA)) {

				if (!wert.getWert().isFehlerhaft()
						&& !wert.getWert().isFehlerhaftBzwNichtErmittelbar()
						&& !wert.getWert().isNichtErmittelbar()
						&& (wert.getStatusMessWertErsetzungImplausibel() != DUAKonstanten.JA)) {

					if (this.parameter != null) {
						synchronized (this.parameter) {
							if (this.parameter.isSinnvoll()) {
								final boolean fehler = Math.abs(wert.getWert()
										.getWert()
										- this.letzterWert.getWert().getWert()) > this.parameter
										.getMaxDiff();
										if (fehler) {
											final UmfeldDatenSensorDatum neuerWert = new UmfeldDatenSensorDatum(
													resultat);
											neuerWert
											.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
											neuerWert.getWert().setFehlerhaftAn();
											copy = neuerWert.getDatum();
										}
							}
						}
					} else {
						LOGGER
						.fine("Fuer Umfelddatensensor " + this + //$NON-NLS-1$
								" wurden noch keine Parameter für die Anstieg-Abfall-Kontrolle empfangen"); //$NON-NLS-1$
					}
				}
			}

			this.letzterWert = wert;
		}

		return copy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final ResultData[] resultate) {
		if (resultate != null) {
			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					synchronized (this) {
						try {
							this.parameter = new UniversalAtgUfdsAnstiegAbstiegKontrolle(
									resultat);
						} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
							LOGGER.warning(e.getMessage());
							continue;
						}
						LOGGER
						.info("Neue Parameter für (" + resultat.getObject() + "):\n" //$NON-NLS-1$ //$NON-NLS-2$
								+ this.parameter);
					}
				}
			}
		}
	}

}
