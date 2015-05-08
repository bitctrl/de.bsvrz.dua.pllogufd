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
import java.util.List;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoKonst;
import de.bsvrz.dua.pllogufd.typen.UfdsVergleichsOperator;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Basisklasse der Tests der SWE Pl-Prüfung logisch UFD.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class PlPruefungLogischUFDTest implements ClientSenderInterface {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Verbindungsdaten.
	 */
	public static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083", //$NON-NLS-1$
			"-benutzer=Tester", //$NON-NLS-1$
			"-authentifizierung=passwd", //$NON-NLS-1$
			"-debugLevelStdErrText=OFF", //$NON-NLS-1$
			"-debugLevelFileText=OFF" }; //$NON-NLS-1$

	// /**
	// * Verbindungsdaten.
	// */
	// public static final String[] CON_DATA = new String[] {
	// "-datenverteiler=localhost:8083", //$NON-NLS-1$
	// "-benutzer=Tester", //$NON-NLS-1$
	// "-authentifizierung=c:\\passwd", //$NON-NLS-1$
	// "-debugLevelStdErrText=INFO", //$NON-NLS-1$
	// "-debugLevelFileText=OFF" }; //$NON-NLS-1$
	/**
	 * Standardintervalllänge der Testdaten für die meisten Tests (2s).
	 */
	public static final long STANDARD_T = 2000L;

	/**
	 * Sender-Instanz.
	 */
	public static PlPruefungLogischUFDTest sender = null;

	/**
	 * alle betrachteten Umfelddatensensoren.
	 */
	public static SystemObject ni1 = null;
	public static SystemObject ni2 = null;
	public static SystemObject ni3 = null;

	public static SystemObject ns1 = null;
	public static SystemObject ns2 = null;
	public static SystemObject ns3 = null;

	public static SystemObject fbz1 = null;
	public static SystemObject fbz2 = null;
	public static SystemObject fbz3 = null;

	public static SystemObject wfd1 = null;
	public static SystemObject wfd2 = null;
	public static SystemObject wfd3 = null;

	public static SystemObject lt1 = null;
	public static SystemObject lt2 = null;
	public static SystemObject lt3 = null;

	public static SystemObject rlf1 = null;
	public static SystemObject rlf2 = null;
	public static SystemObject rlf3 = null;

	public static SystemObject sw1 = null;
	public static SystemObject sw2 = null;
	public static SystemObject sw3 = null;

	public static SystemObject hk1 = null;
	public static SystemObject hk2 = null;
	public static SystemObject hk3 = null;

	public static SystemObject fbt1 = null;
	public static SystemObject fbt2 = null;
	public static SystemObject fbt3 = null;

	public static SystemObject tt11 = null;
	public static SystemObject tt12 = null;
	public static SystemObject tt13 = null;

	public static SystemObject tt31 = null;
	public static SystemObject tt32 = null;
	public static SystemObject tt33 = null;

	public static SystemObject rs1 = null;
	public static SystemObject rs2 = null;
	public static SystemObject rs3 = null;

	public static SystemObject gt1 = null;
	public static SystemObject gt2 = null;
	public static SystemObject gt3 = null;

	public static SystemObject tpt1 = null;
	public static SystemObject tpt2 = null;
	public static SystemObject tpt3 = null;

	public static SystemObject wgs1 = null;
	public static SystemObject wgs2 = null;
	public static SystemObject wgs3 = null;

	public static SystemObject wgm1 = null;
	public static SystemObject wgm2 = null;
	public static SystemObject wgm3 = null;

	public static SystemObject wr1 = null;
	public static SystemObject wr2 = null;
	public static SystemObject wr3 = null;

	public static SystemObject uk1 = null;

	/**
	 * Menge aller im Test betrachteten Sensoren.
	 */
	public static final List<SystemObject> SENSOREN = new ArrayList<>();

	/**
	 * Datenverteiler-Verbindung.
	 */
	public static ClientDavInterface dav;

	/**
	 * Parameterdatenbeschreibung für die Ausfallüberwachung.
	 */
	public static DataDescription paraAusfallUeberwachung;

	/**
	 * Standardkonstruktor.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public PlPruefungLogischUFDTest() throws Exception {

		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				PlPruefungLogischUFDTest.LOGGER.warning(e.getMessage());
				continue;
			}

			final DataDescription datenBeschreibung = new DataDescription(
					PlPruefungLogischUFDTest.dav.getDataModel()
					.getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
					PlPruefungLogischUFDTest.dav.getDataModel()
							.getAspect("asp.externeErfassung")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.dav.subscribeSender(this, sensor,
					datenBeschreibung, SenderRole.source());
		}

		/**
		 * Anmelden zum Senden von Parameter für die Meteorologische Kontrolle
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				PlPruefungLogischUFDTest.LOGGER.warning(e.getMessage());
				continue;
			}

			if (datenArt.equals(UmfeldDatenArt.ns)
					|| datenArt.equals(UmfeldDatenArt.ni)
					|| datenArt.equals(UmfeldDatenArt.wfd)
					|| datenArt.equals(UmfeldDatenArt.sw)) {
				final DataDescription parameterBeschreibung = new DataDescription(
						PlPruefungLogischUFDTest.dav.getDataModel()
						.getAttributeGroup(
								"atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
										UmfeldDatenArt
										.getUmfeldDatenArtVon(
												sensor)
								.getName()),
								PlPruefungLogischUFDTest.dav.getDataModel().getAspect(
								DaVKonstanten.ASP_PARAMETER_VORGABE));
				PlPruefungLogischUFDTest.dav.subscribeSender(this, sensor,
						parameterBeschreibung, SenderRole.sender());
			}
		}

		/**
		 * Anmeldung auf alle Parameter für die Ausfallkontrolle
		 */
		PlPruefungLogischUFDTest.paraAusfallUeberwachung = new DataDescription(
				PlPruefungLogischUFDTest.dav.getDataModel()
						.getAttributeGroup("atg.ufdsAusfallÜberwachung"), //$NON-NLS-1$
						PlPruefungLogischUFDTest.dav.getDataModel()
						.getAspect(DaVKonstanten.ASP_PARAMETER_VORGABE));
		PlPruefungLogischUFDTest.dav.subscribeSender(this,
				PlPruefungLogischUFDTest.SENSOREN,
				PlPruefungLogischUFDTest.paraAusfallUeberwachung,
				SenderRole.sender());

		/**
		 * Anmeldung auf die Parameter der Differenzialkontrolle
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				PlPruefungLogischUFDTest.LOGGER.warning(e.getMessage());
				continue;
			}

			final DataDescription paraDifferenzialkontrolle = new DataDescription(
					PlPruefungLogischUFDTest.dav.getDataModel()
							.getAttributeGroup("atg.ufdsDifferenzialKontrolle" //$NON-NLS-1$
									+ datenArt.getName()),
					PlPruefungLogischUFDTest.dav.getDataModel()
							.getAspect(DaVKonstanten.ASP_PARAMETER_VORGABE));
			PlPruefungLogischUFDTest.dav.subscribeSender(this, sensor,
					paraDifferenzialkontrolle, SenderRole.sender());
		}

		/**
		 * Anmeldung auf alle Parameter der Anstieg-Abfall-Kontrolle
		 */
		for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
			UmfeldDatenArt datenArt;
			try {
				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				PlPruefungLogischUFDTest.LOGGER.warning(e.getMessage());
				continue;
			}

			final DataDescription paraAnstiegAbfallKontrolle = new DataDescription(
					PlPruefungLogischUFDTest.dav.getDataModel()
							.getAttributeGroup("atg.ufdsAnstiegAbstiegKontrolle" //$NON-NLS-1$
									+ datenArt.getName()),
					PlPruefungLogischUFDTest.dav.getDataModel()
							.getAspect(DaVKonstanten.ASP_PARAMETER_VORGABE));
			PlPruefungLogischUFDTest.dav.subscribeSender(this, sensor,
					paraAnstiegAbfallKontrolle, SenderRole.sender());
		}

		/**
		 * Warte bis Anmeldung sicher durch ist
		 */
		try {
			Thread.sleep(1000L);
		} catch (final InterruptedException ex) {
			//
		}
	}

	/**
	 * Initialisiert alle Umfelddatensensoren als statische Objekte.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public static final void initialisiere() throws Exception {
		if (PlPruefungLogischUFDTest.dav == null) {
			PlPruefungLogischUFDTest.dav = DAVTest
					.getDav(PlPruefungLogischUFDTest.CON_DATA);
			DUAUtensilien.setAlleParameter(PlPruefungLogischUFDTest.dav);
			UmfeldDatenArt.initialisiere(PlPruefungLogischUFDTest.dav);

			// SENSOREN.add(ni1 =
			// DAV.getDataModel().getObject("AAA.pllogufd.NI.1")); //$NON-NLS-1$
			// SENSOREN.add(fbz1 =
			// DAV.getDataModel().getObject("AAA.pllogufd.FBZ.1"));
			// //$NON-NLS-1$
			// SENSOREN.add(ns1 =
			// DAV.getDataModel().getObject("AAA.pllogufd.NS.1")); //$NON-NLS-1$
			// SENSOREN.add(wfd1 =
			// DAV.getDataModel().getObject("AAA.pllogufd.WFD.1"));
			// //$NON-NLS-1$
			// SENSOREN.add(lt1 =
			// DAV.getDataModel().getObject("AAA.pllogufd.LT.1")); //$NON-NLS-1$
			// SENSOREN.add(rlf1 =
			// DAV.getDataModel().getObject("AAA.pllogufd.RLF.1"));
			// //$NON-NLS-1$
			// SENSOREN.add(sw1 =
			// DAV.getDataModel().getObject("AAA.pllogufd.SW.1")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.ni1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.NI.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.ni2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.NI.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.ni3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.NI.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.fbz1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.FBZ.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.fbz2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.FBZ.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.fbz3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.FBZ.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.ns1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.NS.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.ns2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.NS.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.ns3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.NS.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wfd1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WFD.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wfd2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WFD.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wfd3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WFD.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.lt1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.LT.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.lt2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.LT.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.lt3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.LT.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.rlf1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.RLF.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.rlf2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.RLF.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.rlf3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.RLF.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.sw1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.SW.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.sw2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.SW.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.sw3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.SW.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.hk1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.HK.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.hk2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.HK.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.hk3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.HK.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.fbt1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.FBT.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.fbt2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.FBT.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.fbt3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.FBT.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.tt11 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.TT1.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.tt12 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.TT1.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.tt13 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.TT1.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.tt31 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.TT3.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.tt32 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.TT3.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.tt33 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.TT3.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.rs1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.RS.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.rs2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.RS.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.rs3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.RS.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.gt1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.GT.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.gt2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.GT.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.gt3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.GT.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.tpt1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.TPT.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.tpt2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.TPT.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.tpt3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.TPT.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wgs1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WGS.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wgs2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WGS.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wgs3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WGS.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wgm1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WGM.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wgm2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WGM.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wgm3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WGM.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wr1 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WR.1")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wr2 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WR.2")); //$NON-NLS-1$
			PlPruefungLogischUFDTest.SENSOREN
			.add(PlPruefungLogischUFDTest.wr3 = PlPruefungLogischUFDTest.dav
			.getDataModel().getObject("AAA.pllogufd.WR.3")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.SENSOREN
					.add(PlPruefungLogischUFDTest.uk1 = PlPruefungLogischUFDTest.dav
							.getDataModel().getObject("BBB.pllogufd.US.1")); //$NON-NLS-1$

			PlPruefungLogischUFDTest.sender = new PlPruefungLogischUFDTest();
		}
	}

	/**
	 * Versendet ein Umfelddatum als Quelle.
	 *
	 * @param resultat
	 *            ein Umfelddatum
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public final void sende(final ResultData resultat) throws Exception {
		PlPruefungLogischUFDTest.dav.sendData(resultat);
	}

	/**
	 * Setzt alle (Standard-)Parameter der Meteorologischen Kontrolle an bzw.
	 * aus.
	 *
	 * @param an
	 *            Standardparameter anschalten?
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public void setMeteoKontrolle(final boolean an)
			throws UmfeldDatenSensorUnbekannteDatenartException {
		final Aspect vorgabeAspekt = PlPruefungLogischUFDTest.dav.getDataModel()
				.getAspect(DaVKonstanten.ASP_PARAMETER_VORGABE);

		if (an) {
			/**
			 * Standardparameter senden
			 */
			for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
				UmfeldDatenArt datenArt;
				try {
					datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
				} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
					System.err.println("Wird nicht geprüft: " + e.getMessage());
					continue;
				}

				if (datenArt.equals(UmfeldDatenArt.ns)) {
					final AttributeGroup atg = PlPruefungLogischUFDTest.dav
							.getDataModel().getAttributeGroup(
									"atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
											UmfeldDatenArt.getUmfeldDatenArtVon(
													sensor).getName());
					final Data parameterDatum = PlPruefungLogischUFDTest.dav
							.createData(atg);

					parameterDatum.getUnscaledValue("NSGrenzLT") //$NON-NLS-1$
							.set(MeteoKonst.NS_GRENZ_LT);
					parameterDatum.getUnscaledValue("NSGrenzTrockenRLF") //$NON-NLS-1$
							.set(MeteoKonst.NI_GRENZ_TROCKEN_RLF);
					parameterDatum.getScaledValue("NSminNI") //$NON-NLS-1$
							.set(MeteoKonst.NS_MIN_NI);
					parameterDatum.getUnscaledValue("NSGrenzRLF") //$NON-NLS-1$
							.set(MeteoKonst.NS_GRENZ_RLF);
					final ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt),
							System.currentTimeMillis(), parameterDatum);
					try {
						PlPruefungLogischUFDTest.dav
						.sendData(parameterResultat);
					} catch (final Exception e) {
						e.printStackTrace();
						PlPruefungLogischUFDTest.LOGGER
								.error(Constants.EMPTY_STRING, e);
					}
				}

				if (datenArt.equals(UmfeldDatenArt.ni)) {
					final AttributeGroup atg = PlPruefungLogischUFDTest.dav
							.getDataModel().getAttributeGroup(
									"atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
											UmfeldDatenArt.getUmfeldDatenArtVon(
													sensor).getName());
					final Data parameterDatum = PlPruefungLogischUFDTest.dav
							.createData(atg);

					parameterDatum.getScaledValue("NIgrenzNassNI") //$NON-NLS-1$
							.set(MeteoKonst.NI_GRENZ_NASS_NI);
					parameterDatum.getUnscaledValue("NIgrenzNassRLF") //$NON-NLS-1$
							.set(MeteoKonst.NI_GRENZ_NASS_RLF);
					parameterDatum.getScaledValue("NIminNI") //$NON-NLS-1$
							.set(MeteoKonst.NI_MIN_NI);
					parameterDatum.getUnscaledValue("NIgrenzTrockenRLF") //$NON-NLS-1$
							.set(MeteoKonst.NI_GRENZ_TROCKEN_RLF);
					parameterDatum.getTimeValue("NIminTrockenRLF") //$NON-NLS-1$
							.setMillis(MeteoKonst.NI_MIN_TROCKEN_RLF);
					final ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt),
							System.currentTimeMillis(), parameterDatum);
					try {
						PlPruefungLogischUFDTest.dav
						.sendData(parameterResultat);
					} catch (final Exception e) {
						e.printStackTrace();
						PlPruefungLogischUFDTest.LOGGER
								.error(Constants.EMPTY_STRING, e);
					}
				}

				if (datenArt.equals(UmfeldDatenArt.wfd)) {
					final AttributeGroup atg = PlPruefungLogischUFDTest.dav
							.getDataModel().getAttributeGroup(
									"atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
											UmfeldDatenArt.getUmfeldDatenArtVon(
													sensor).getName());
					final Data parameterDatum = PlPruefungLogischUFDTest.dav
							.createData(atg);

					parameterDatum.getScaledValue("WFDgrenzNassNI") //$NON-NLS-1$
							.set(MeteoKonst.WFD_GRENZ_NASS_NI);
					parameterDatum.getUnscaledValue("WFDgrenzNassRLF") //$NON-NLS-1$
							.set(MeteoKonst.WFD_GRENZ_NASS_RLF);
					parameterDatum.getTimeValue("WDFminNassRLF") //$NON-NLS-1$
							.setMillis(MeteoKonst.WDF_MIN_NASS_RLF);
					final ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt),
							System.currentTimeMillis(), parameterDatum);
					try {
						PlPruefungLogischUFDTest.dav
						.sendData(parameterResultat);
					} catch (final Exception e) {
						e.printStackTrace();
						PlPruefungLogischUFDTest.LOGGER
								.error(Constants.EMPTY_STRING, e);
					}
				}

				if (datenArt.equals(UmfeldDatenArt.sw)) {
					final AttributeGroup atg = PlPruefungLogischUFDTest.dav
							.getDataModel().getAttributeGroup(
									"atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
											UmfeldDatenArt.getUmfeldDatenArtVon(
													sensor).getName());
					final Data parameterDatum = PlPruefungLogischUFDTest.dav
							.createData(atg);

					parameterDatum.getUnscaledValue("SWgrenzTrockenRLF") //$NON-NLS-1$
							.set(MeteoKonst.SW_GRENZ_TROCKEN_RLF);
					parameterDatum.getUnscaledValue("SWgrenzSW") //$NON-NLS-1$
							.set(MeteoKonst.SW_GRENZ_SW);

					final ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt),
							System.currentTimeMillis(), parameterDatum);
					try {
						PlPruefungLogischUFDTest.dav
						.sendData(parameterResultat);
					} catch (final Exception e) {
						e.printStackTrace();
						PlPruefungLogischUFDTest.LOGGER
								.error(Constants.EMPTY_STRING, e);
					}
				}
			}
		} else {
			/**
			 * Alle Vergleichswerte auf <code>fehlerhaft</code> setzen
			 */
			for (final SystemObject sensor : PlPruefungLogischUFDTest.SENSOREN) {
				UmfeldDatenArt datenArt;
				try {
					datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
				} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
					PlPruefungLogischUFDTest.LOGGER.warning(e.getMessage());
					continue;
				}

				if (datenArt.equals(UmfeldDatenArt.ns)) {
					final AttributeGroup atg = PlPruefungLogischUFDTest.dav
							.getDataModel().getAttributeGroup(
									"atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
											UmfeldDatenArt.getUmfeldDatenArtVon(
													sensor).getName());
					final Data parameterDatum = PlPruefungLogischUFDTest.dav
							.createData(atg);

					final UmfeldDatenSensorWert ltWert = new UmfeldDatenSensorWert(
							UmfeldDatenArt.lt);
					ltWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NSGrenzLT") //$NON-NLS-1$
							.set(ltWert.getWert());

					final UmfeldDatenSensorWert rlfWert = new UmfeldDatenSensorWert(
							UmfeldDatenArt.rlf);
					rlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NSGrenzTrockenRLF") //$NON-NLS-1$
							.set(rlfWert.getWert());

					final UmfeldDatenSensorWert niWert = new UmfeldDatenSensorWert(
							UmfeldDatenArt.ni);
					niWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NSminNI") //$NON-NLS-1$
							.set(niWert.getWert());

					final UmfeldDatenSensorWert nsRlfWert = new UmfeldDatenSensorWert(
							UmfeldDatenArt.rlf);
					nsRlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NSGrenzRLF") //$NON-NLS-1$
							.set(nsRlfWert.getWert());
					final ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt),
							System.currentTimeMillis(), parameterDatum);
					try {
						PlPruefungLogischUFDTest.dav
						.sendData(parameterResultat);
					} catch (final Exception e) {
						e.printStackTrace();
						PlPruefungLogischUFDTest.LOGGER
								.error(Constants.EMPTY_STRING, e);
					}
				}

				if (datenArt.equals(UmfeldDatenArt.ni)) {
					final AttributeGroup atg = PlPruefungLogischUFDTest.dav
							.getDataModel().getAttributeGroup(
									"atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
											UmfeldDatenArt.getUmfeldDatenArtVon(
													sensor).getName());
					final Data parameterDatum = PlPruefungLogischUFDTest.dav
							.createData(atg);

					UmfeldDatenSensorWert niWert = new UmfeldDatenSensorWert(
							UmfeldDatenArt.ni);
					niWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NIgrenzNassNI") //$NON-NLS-1$
							.set(niWert.getWert());

					UmfeldDatenSensorWert rlfWert = new UmfeldDatenSensorWert(
							UmfeldDatenArt.rlf);
					rlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NIgrenzNassRLF") //$NON-NLS-1$
							.set(rlfWert.getWert());

					niWert = new UmfeldDatenSensorWert(UmfeldDatenArt.ni);
					niWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NIminNI") //$NON-NLS-1$
							.set(niWert.getWert());

					rlfWert = new UmfeldDatenSensorWert(UmfeldDatenArt.rlf);
					rlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NIgrenzTrockenRLF") //$NON-NLS-1$
							.set(rlfWert.getWert());

					parameterDatum.getTimeValue("NIminTrockenRLF") //$NON-NLS-1$
							.setMillis(PlPruefungLogischUFDTest.STANDARD_T * 2);
					final ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt),
							System.currentTimeMillis(), parameterDatum);
					try {
						PlPruefungLogischUFDTest.dav
						.sendData(parameterResultat);
					} catch (final Exception e) {
						e.printStackTrace();
						PlPruefungLogischUFDTest.LOGGER
								.error(Constants.EMPTY_STRING, e);
					}
				}

				if (datenArt.equals(UmfeldDatenArt.wfd)) {
					final AttributeGroup atg = PlPruefungLogischUFDTest.dav
							.getDataModel().getAttributeGroup(
									"atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
											UmfeldDatenArt.getUmfeldDatenArtVon(
													sensor).getName());
					final Data parameterDatum = PlPruefungLogischUFDTest.dav
							.createData(atg);

					final UmfeldDatenSensorWert niWert = new UmfeldDatenSensorWert(
							UmfeldDatenArt.ni);
					niWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("WFDgrenzNassNI") //$NON-NLS-1$
							.set(niWert.getWert());

					final UmfeldDatenSensorWert rlfWert = new UmfeldDatenSensorWert(
							UmfeldDatenArt.rlf);
					rlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("WFDgrenzNassRLF") //$NON-NLS-1$
							.set(rlfWert.getWert());

					parameterDatum.getTimeValue("WDFminNassRLF") //$NON-NLS-1$
							.setMillis(PlPruefungLogischUFDTest.STANDARD_T * 2);
					final ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt),
							System.currentTimeMillis(), parameterDatum);
					try {
						PlPruefungLogischUFDTest.dav
						.sendData(parameterResultat);
					} catch (final Exception e) {
						e.printStackTrace();
						PlPruefungLogischUFDTest.LOGGER
								.error(Constants.EMPTY_STRING, e);
					}
				}

				if (datenArt.equals(UmfeldDatenArt.sw)) {
					final AttributeGroup atg = PlPruefungLogischUFDTest.dav
							.getDataModel().getAttributeGroup(
									"atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
											UmfeldDatenArt.getUmfeldDatenArtVon(
													sensor).getName());
					final Data parameterDatum = PlPruefungLogischUFDTest.dav
							.createData(atg);

					final UmfeldDatenSensorWert rlfWert = new UmfeldDatenSensorWert(
							UmfeldDatenArt.rlf);
					rlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("SWgrenzTrockenRLF") //$NON-NLS-1$
							.set(rlfWert.getWert());

					final UmfeldDatenSensorWert swWert = new UmfeldDatenSensorWert(
							UmfeldDatenArt.rlf);
					swWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("SWgrenzSW") //$NON-NLS-1$
							.set(swWert.getWert());

					final ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt),
							System.currentTimeMillis(), parameterDatum);
					try {
						PlPruefungLogischUFDTest.dav
						.sendData(parameterResultat);
					} catch (final Exception e) {
						e.printStackTrace();
						PlPruefungLogischUFDTest.LOGGER
								.error(Constants.EMPTY_STRING, e);
					}
				}
			}
		}
	}

	/**
	 * Setzt den maximalen Zeitverzug eines Umfelddatensensors.
	 *
	 * @param obj
	 *            Umfelddatensensor
	 * @param verzugInMillis
	 *            maximalen Zeitverzug in ms
	 */
	public final void setMaxAusfallFuerSensor(final SystemObject obj,
			final long verzugInMillis) {
		final Data parameterData = PlPruefungLogischUFDTest.dav
				.createData(PlPruefungLogischUFDTest.dav.getDataModel()
						.getAttributeGroup("atg.ufdsAusfallÜberwachung")); //$NON-NLS-1$
		parameterData.getTimeValue("maxZeitVerzug").setMillis(verzugInMillis); //$NON-NLS-1$
		final ResultData parameter = new ResultData(obj,
				PlPruefungLogischUFDTest.paraAusfallUeberwachung,
				System.currentTimeMillis(), parameterData);

		try {
			PlPruefungLogischUFDTest.dav.sendData(parameter);
		} catch (final DataNotSubscribedException e) {
			e.printStackTrace();
			PlPruefungLogischUFDTest.LOGGER.error(Constants.EMPTY_STRING, e);
		} catch (final SendSubscriptionNotConfirmed e) {
			e.printStackTrace();
			PlPruefungLogischUFDTest.LOGGER.error(Constants.EMPTY_STRING, e);
		}
	}

	/**
	 * Setzt die Parameter eines Umfelddatensensors für de
	 * Differenzialkontrolle.
	 *
	 * @param sensor
	 *            der Sensor
	 * @param wert
	 *            der Vergleichswert, demgegenüber der Sensorwert kleiner sein
	 *            muss, damit die Differenzialkontrolle durchgeführt werden kann
	 * @param zeit
	 *            die Zeit, die ein Wert maximal gleich bleiben darf
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public final void setDiffPara(final SystemObject sensor, final int wert,
			final long zeit)
					throws UmfeldDatenSensorUnbekannteDatenartException {
		final UmfeldDatenArt datenArt = UmfeldDatenArt
				.getUmfeldDatenArtVon(sensor);
		final Data datum = PlPruefungLogischUFDTest.dav.createData(
				PlPruefungLogischUFDTest.dav.getDataModel().getAttributeGroup(
						"atg.ufdsDifferenzialKontrolle" + datenArt.getName())); //$NON-NLS-1$

		datum.getUnscaledValue("Operator") //$NON-NLS-1$
				.set(UfdsVergleichsOperator.KLEINER.getCode());
		datum.getUnscaledValue(datenArt.getAbkuerzung() + "Grenz").set(wert); //$NON-NLS-1$
		datum.getTimeValue(datenArt.getAbkuerzung() + "maxZeit") //$NON-NLS-1$
				.setMillis(zeit);

		final DataDescription paraDifferenzialkontrolle = new DataDescription(
				PlPruefungLogischUFDTest.dav.getDataModel()
						.getAttributeGroup("atg.ufdsDifferenzialKontrolle" //$NON-NLS-1$
								+ datenArt.getName()),
				PlPruefungLogischUFDTest.dav.getDataModel()
						.getAspect(DaVKonstanten.ASP_PARAMETER_VORGABE));
		final ResultData parameterSatz = new ResultData(sensor,
				paraDifferenzialkontrolle, System.currentTimeMillis(), datum);
		try {
			PlPruefungLogischUFDTest.dav.sendData(parameterSatz);
		} catch (final DataNotSubscribedException e) {
			e.printStackTrace();
			PlPruefungLogischUFDTest.LOGGER.error(Constants.EMPTY_STRING, e);
		} catch (final SendSubscriptionNotConfirmed e) {
			e.printStackTrace();
			PlPruefungLogischUFDTest.LOGGER.error(Constants.EMPTY_STRING, e);
		}
	}

	/**
	 * Setzt die Parameter eines Umfelddatensensors für die
	 * Anstieg-Abfall-Kontrolle.
	 *
	 * @param sensor
	 *            der Sensor
	 * @param maxDiff
	 *            die maximale Differenz zwischen zwei Werten
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public final void setAnAbPara(final SystemObject sensor, final long maxDiff)
			throws UmfeldDatenSensorUnbekannteDatenartException {
		final UmfeldDatenArt datenArt = UmfeldDatenArt
				.getUmfeldDatenArtVon(sensor);

		final Data datum = PlPruefungLogischUFDTest.dav
				.createData(PlPruefungLogischUFDTest.dav.getDataModel()
						.getAttributeGroup("atg.ufdsAnstiegAbstiegKontrolle" //$NON-NLS-1$
								+ datenArt.getName()));

		datum.getUnscaledValue(datenArt.getAbkuerzung() + "maxDiff") //$NON-NLS-1$
				.set(maxDiff);

		final DataDescription paraAnstiegAbfallKontrolle = new DataDescription(
				PlPruefungLogischUFDTest.dav.getDataModel()
						.getAttributeGroup("atg.ufdsAnstiegAbstiegKontrolle" //$NON-NLS-1$
								+ datenArt.getName()),
				PlPruefungLogischUFDTest.dav.getDataModel()
						.getAspect(DaVKonstanten.ASP_PARAMETER_VORGABE));
		final ResultData parameterSatz = new ResultData(sensor,
				paraAnstiegAbfallKontrolle, System.currentTimeMillis(), datum);
		try {
			PlPruefungLogischUFDTest.dav.sendData(parameterSatz);
		} catch (final DataNotSubscribedException e) {
			e.printStackTrace();
			PlPruefungLogischUFDTest.LOGGER.error(Constants.EMPTY_STRING, e);
		} catch (final SendSubscriptionNotConfirmed e) {
			e.printStackTrace();
			PlPruefungLogischUFDTest.LOGGER.error(Constants.EMPTY_STRING, e);
		}
	}

	@Override
	public void dataRequest(final SystemObject object,
			final DataDescription dataDescription, final byte state) {
		// mache nichts
	}

	@Override
	public boolean isRequestSupported(final SystemObject object,
			final DataDescription dataDescription) {
		return false;
	}

}
