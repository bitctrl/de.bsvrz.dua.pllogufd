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

package de.bsvrz.dua.pllogufd.typen;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.configuration.interfaces.DataModel;
import stauma.dav.configuration.interfaces.SystemObject;
import stauma.dav.configuration.interfaces.SystemObjectType;
import sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;

/**
 * Umfelddatenart. Verbindet alle notwendigen Informationen (Name, Abkürzung) eines
 * Umfelddaten-Systemobjekttyps mit dem Systemobjekttyp selbst
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public class UmfeldDatenArt{
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Mapt den Systemobjekttyp eines Umfelddatensensors auf die
	 * Informationen zu seinem Namen und seiner Abkürzung
	 */
	private static Map<SystemObjectType, UmfeldDatenArt> TYP_AUF_ART = 
		Collections.synchronizedMap(new TreeMap<SystemObjectType, UmfeldDatenArt>());
	
	/**
	 * <code>typ.ufdsFahrBahnFeuchte</code>
	 */
	public static UmfeldDatenArt FBF;
	
	/**
	 * <code>typ.ufdsFahrBahnGlätte</code>
	 */
	public static UmfeldDatenArt FBG;
	
	/**
	 * <code>typ.ufdsFahrBahnOberFlächenTemperatur</code>
	 */
	public static UmfeldDatenArt FBT;

	/**
	 * <code>typ.ufdsGefrierTemperatur</code>
	 */
	public static UmfeldDatenArt GT;
	
	/**
	 * <code>typ.ufdsHelligkeit</code>
	 */
	public static UmfeldDatenArt HK;
	
	/**
	 * <code>typ.ufdsLuftTemperatur</code>
	 */
	public static UmfeldDatenArt LT;
	
	/**
	 * <code>typ.ufdsNiederschlagsArt</code>
	 */
	public static UmfeldDatenArt NS;
	
	/**
	 * <code>typ.ufdsNiederschlagsIntensität</code>
	 */
	public static UmfeldDatenArt NI;
	
	/**
	 * <code>typ.ufdsNiederschlagsMenge</code>
	 */
	public static UmfeldDatenArt NM;
	
	/**
	 * <code>typ.ufdsRelativeLuftFeuchte</code>
	 */
	public static UmfeldDatenArt RLF;	
	
	/**
	 * <code>typ.ufdsSchneeHöhe</code>
	 */
	public static UmfeldDatenArt SH;
	
	/**
	 * <code>typ.ufdsSichtWeite</code>
	 */
	public static UmfeldDatenArt SW;
	
	/**
	 * <code>typ.ufdsTaupunktTemperatur</code>
	 */
	public static UmfeldDatenArt TPT;	
	
	/**
	 * <code>typ.ufdsTemperaturInTiefe1</code>
	 */
	public static UmfeldDatenArt TT1;
	
	/**
	 * <code>typ.ufdsTemperaturInTiefe2</code>
	 */
	public static UmfeldDatenArt TT2;	
	
	/**
	 * <code>typ.ufdsTemperaturInTiefe3</code>
	 */
	public static UmfeldDatenArt TT3;
	
	/**
	 * <code>typ.ufdsWasserFilmDicke</code>
	 */
	public static UmfeldDatenArt WDF;	
	
	/**
	 * <code>typ.ufdsWindRichtung</code>
	 */
	public static UmfeldDatenArt WR;
	
	/**
	 * <code>typ.ufdsFahrBahnOberFlächenZustand</code>
	 */
	public static UmfeldDatenArt FBZ;	
	
	/**
	 * <code>typ.ufdsLuftDruck</code>
	 */
	public static UmfeldDatenArt LD;	
	
	/**
	 * <code>typ.ufdsRestSalz</code>
	 */
	public static UmfeldDatenArt RS;	
	
	/**
	 * <code>typ.ufdsWindGeschwindigkeitMittelWert</code>
	 */
	public static UmfeldDatenArt WGM;	
	
	/**
	 * <code>typ.ufdsWindGeschwindigkeitSpitzenWert</code>
	 */
	public static UmfeldDatenArt WGS;	

	/**
	 * der Systemobjekt-Typ des Umfelddatensensors
	 */
	private SystemObjectType typ = null;
	
	/**
	 * der Name der Umfelddatenart
	 */
	private String name = null;
	
	/**
	 * die Abkürzung der Umfelddatenart
	 */
	private String abkuerzung = null;
	
	
	/**
	 * Erfragt die Umfelddatenart eines Systemobjekts
	 * 
	 * @param objekt die Umfelddatenart eines Systemobjekts oder
	 * <code>null</code>, wenn es sich nicht um das Systemobjekt
	 * eines Umfelddatensensors handelt
	 */
	public static final UmfeldDatenArt getUmfeldDatenArtVon(final SystemObject objekt){
		UmfeldDatenArt umfeldDatenArt = null;
		
		if(objekt != null){
			synchronized (TYP_AUF_ART) {
				umfeldDatenArt = TYP_AUF_ART.get(objekt);	
			}			
		}else{
			LOGGER.error("Übergebenes Systemobjekt ist <<null>>"); //$NON-NLS-1$
		}
		
		return umfeldDatenArt;
	}
	
	
	/**
	 * Initialisierung
	 * 
	 * @param dav eine Datenverteiler-Verbindung
	 * @throws DUAInitialisierungsException wenn nicht alle Objekte initialisiert werden konnten
	 */
	public static final void initialisiere(final ClientDavInterface dav)
	throws DUAInitialisierungsException{
		DataModel datenModell = dav.getDataModel();
		
		FBF = new UmfeldDatenArt(datenModell.getType("typ.ufdsFahrBahnFeuchte"), "FBF");  //$NON-NLS-1$//$NON-NLS-2$
		FBG = new UmfeldDatenArt(datenModell.getType("typ.ufdsFahrBahnGlätte"), "FBG");  //$NON-NLS-1$//$NON-NLS-2$	
		FBT = new UmfeldDatenArt(datenModell.getType("typ.ufdsFahrBahnOberFlächenTemperatur"), "FBT");  //$NON-NLS-1$//$NON-NLS-2$	
		HK = new UmfeldDatenArt(datenModell.getType("typ.ufdsHelligkeit"), "HK");  //$NON-NLS-1$//$NON-NLS-2$
		GT = new UmfeldDatenArt(datenModell.getType("typ.ufdsGefrierTemperatur"), "GT");  //$NON-NLS-1$//$NON-NLS-2$
		LT = new UmfeldDatenArt(datenModell.getType("typ.ufdsLuftTemperatur"), "LT");  //$NON-NLS-1$//$NON-NLS-2$	
		NS = new UmfeldDatenArt(datenModell.getType("typ.ufdsNiederschlagsArt"), "NS");  //$NON-NLS-1$//$NON-NLS-2$	
		NI = new UmfeldDatenArt(datenModell.getType("typ.ufdsNiederschlagsIntensität"), "NI");  //$NON-NLS-1$//$NON-NLS-2$	
		NM = new UmfeldDatenArt(datenModell.getType("typ.ufdsNiederschlagsMenge"), "NM");  //$NON-NLS-1$//$NON-NLS-2$	
		RLF = new UmfeldDatenArt(datenModell.getType("typ.ufdsRelativeLuftFeuchte"), "RLF");  //$NON-NLS-1$//$NON-NLS-2$	
		SH = new UmfeldDatenArt(datenModell.getType("typ.ufdsSchneeHöhe"), "SH");  //$NON-NLS-1$//$NON-NLS-2$	
		SW = new UmfeldDatenArt(datenModell.getType("typ.ufdsSichtWeite"), "SW");  //$NON-NLS-1$//$NON-NLS-2$	
		TPT = new UmfeldDatenArt(datenModell.getType("typ.ufdsTaupunktTemperatur"), "TPT");  //$NON-NLS-1$//$NON-NLS-2$	
		TT1 = new UmfeldDatenArt(datenModell.getType("typ.ufdsTemperaturInTiefe1"), "TT1");  //$NON-NLS-1$//$NON-NLS-2$	
		TT2 = new UmfeldDatenArt(datenModell.getType("typ.ufdsTemperaturInTiefe2"), "TT2");  //$NON-NLS-1$//$NON-NLS-2$	
		TT3 = new UmfeldDatenArt(datenModell.getType("typ.ufdsTemperaturInTiefe3"), "TT3");  //$NON-NLS-1$//$NON-NLS-2$	
		WDF = new UmfeldDatenArt(datenModell.getType("typ.ufdsWasserFilmDicke"), "WDF");  //$NON-NLS-1$//$NON-NLS-2$	
		WR = new UmfeldDatenArt(datenModell.getType("typ.ufdsWindRichtung"), "WR");  //$NON-NLS-1$//$NON-NLS-2$	
		FBZ = new UmfeldDatenArt(datenModell.getType("typ.ufdsFahrBahnOberFlächenZustand"), "FBZ");  //$NON-NLS-1$//$NON-NLS-2$	
		LD = new UmfeldDatenArt(datenModell.getType("typ.ufdsLuftDruck"), "LD");  //$NON-NLS-1$//$NON-NLS-2$	
		RS = new UmfeldDatenArt(datenModell.getType("typ.ufdsRestSalz"), "RS");  //$NON-NLS-1$//$NON-NLS-2$	
		WGM = new UmfeldDatenArt(datenModell.getType("typ.ufdsWindGeschwindigkeitMittelWert"), "WGM");  //$NON-NLS-1$//$NON-NLS-2$	
		WGS = new UmfeldDatenArt(datenModell.getType("typ.ufdsWindGeschwindigkeitSpitzenWert"),  "WGS");  //$NON-NLS-1$//$NON-NLS-2$	

		TYP_AUF_ART.put(FBF.getTyp(), FBF);
		TYP_AUF_ART.put(FBG.getTyp(), FBG);
		TYP_AUF_ART.put(FBT.getTyp(), FBT);
		TYP_AUF_ART.put(HK.getTyp(), HK);
		TYP_AUF_ART.put(GT.getTyp(), GT);
		TYP_AUF_ART.put(LT.getTyp(), LT);
		TYP_AUF_ART.put(NS.getTyp(), NS);
		TYP_AUF_ART.put(NI.getTyp(), NI);
		TYP_AUF_ART.put(NM.getTyp(), NM);
		TYP_AUF_ART.put(RLF.getTyp(), RLF);
		TYP_AUF_ART.put(SH.getTyp(), SH);
		TYP_AUF_ART.put(SW.getTyp(), SW);
		TYP_AUF_ART.put(TPT.getTyp(), TPT);
		TYP_AUF_ART.put(TT1.getTyp(), TT1);
		TYP_AUF_ART.put(TT2.getTyp(), TT2);
		TYP_AUF_ART.put(TT3.getTyp(), TT3);
		TYP_AUF_ART.put(WDF.getTyp(), WDF);
		TYP_AUF_ART.put(WR.getTyp(), WR);
		TYP_AUF_ART.put(FBZ.getTyp(), FBZ);
		TYP_AUF_ART.put(LD.getTyp(), LD);
		TYP_AUF_ART.put(RS.getTyp(), RS);
		TYP_AUF_ART.put(WGM.getTyp(), WGM);
		TYP_AUF_ART.put(WGS.getTyp(), WGS);
	}
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param typ der Systemobjekttyp der Umfelddatenart
	 * @param abkuerzung die Abkürzung der Umfelddatenart
	 * @throws DUAInitialisierungsException wenn eines der übergebenen Objekte
	 * <code>null</code> ist oder der Name des Umfelddatensensor-Typs nicht ermittelt
	 * werden konnte
	 */
	private UmfeldDatenArt(final SystemObjectType typ,
						   final String abkuerzung)
	throws DUAInitialisierungsException{
		if(typ == null){
			throw new DUAInitialisierungsException("Umfelddatensensor-Typ ist <<null>>"); //$NON-NLS-1$
		}
		if(abkuerzung == null){
			throw new DUAInitialisierungsException("Abkürzung ist <<null>> für " + typ); //$NON-NLS-1$
		}
		this.typ = typ;
		this.abkuerzung = abkuerzung;
		this.name = typ.getPid().substring("typ.ufds".length()); //$NON-NLS-1$
	}
	
	
	/**
	 * Erfragt den Name der Umfelddatenart
	 * 
	 * @return der Name der Umfelddatenart
	 */
	public final String getName(){
		return this.name;
	}
	
	
	/**
	 * Erfragt die Abkürzung der Umfelddatenart
	 * 
	 * @return die Abkürzung der Umfelddatenart
	 */
	public final String getAbkuerzung(){
		return this.abkuerzung;
	}
	
	
	/**
	 * Erfragt den Systemobjekt-Typ des Umfelddatensensors
	 * 
	 * @return den Systemobjekt-Typ des Umfelddatensensors
	 */
	public final SystemObjectType getTyp(){
		return this.typ;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean gleich = false;
		
		if(obj != null && obj instanceof UmfeldDatenArt){
			UmfeldDatenArt that = (UmfeldDatenArt)obj;
			gleich = this.getTyp().getId() == that.getTyp().getId();
		}
		
		return gleich;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.name + " (" + this.abkuerzung + //$NON-NLS-1$ 
					")\n" + this.typ; //$NON-NLS-1$
	}	
	
}
