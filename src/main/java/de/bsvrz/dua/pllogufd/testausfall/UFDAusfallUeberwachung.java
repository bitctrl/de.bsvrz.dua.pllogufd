/*
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.3 Pl-Pr�fung logisch UFD
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
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.pllogufd.testausfall;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.testausfall.AbstraktAusfallUeberwachung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Das Modul Ausfall�berwachung meldet sich auf alle Parameter an und f�hrt mit
 * allen �ber die Methode aktualisiereDaten(ResultData[] arg0) �bergebenen Daten
 * eine Pr�fung durch. Die Pr�fung �berwacht, ob ein Messwert nach Ablauf des
 * daf�r vorgesehenen Intervalls �bertragen wurde. Der erwartete
 * Meldungszeitpunkt f�r einen zyklisch gelieferten Messwert ergibt sich aus dem
 * Intervallbeginn zuz�glich der Erfassungsintervalldauer. Ein nicht
 * �bertragener Messwert wird intern als Datensatz mit dem erwarteten
 * Intervallbeginn angelegt, wobei die Messwerte jeweils auf den Status Nicht
 * erfasst gesetzt werden. Nach der Pr�fung werden die Daten dann an den
 * n�chsten Bearbeitungsknoten weitergereicht.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class UFDAusfallUeberwachung extends AbstraktAusfallUeberwachung
implements ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		final DataDescription parameterBeschreibung = new DataDescription(
				dieVerwaltung.getVerbindung().getDataModel()
				.getAttributeGroup("atg.ufdsAusfall�berwachung"), //$NON-NLS-1$
				dieVerwaltung.getVerbindung().getDataModel()
				.getAspect(DaVKonstanten.ASP_PARAMETER_SOLL));
		dieVerwaltung.getVerbindung().subscribeReceiver(this,
				dieVerwaltung.getSystemObjekte(), parameterBeschreibung,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	@Override
	protected ResultData getAusfallDatumVon(final ResultData originalResultat) {
		final UmfeldDatenSensorDatum wert = new UmfeldDatenSensorDatum(
				originalResultat);
		wert.setStatusErfassungNichtErfasst(DUAKonstanten.JA);
		wert.getWert().setNichtErmittelbarAn();

		final long zeitStempel = wert.getDatenZeit() + wert.getT();

		final ResultData resultat = new ResultData(originalResultat.getObject(),
				originalResultat.getDataDescription(), zeitStempel,
				wert.getDatum());

		return resultat;
	}

	@Override
	protected long getTVon(final ResultData resultat) {
		final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(
				resultat);
		return datum.getT();
	}

	@Override
	public void update(final ResultData[] resultate) {
		if (resultate != null) {
			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					setObjectWertErfassungVerzug(resultat.getObject(),
							new Long(resultat.getData()
									.getTimeValue("maxZeitVerzug")
									.getMillis()));
					UFDAusfallUeberwachung.LOGGER
					.info("Neue Parameter: maxZeitVerzug("
							+ resultat.getObject() + ") = "
							+ resultat.getData()
							.getTimeValue("maxZeitVerzug")
							.getMillis()
							+ "ms");
				}
			}
		}
	}

}
