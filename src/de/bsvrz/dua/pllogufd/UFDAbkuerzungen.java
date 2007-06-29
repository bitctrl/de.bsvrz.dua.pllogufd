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

import java.util.HashMap;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class UFDAbkuerzungen {
	
	/**
	 * 
	 */
	private static HashMap<String, String> UFD_NAME_AUF_ABK = null;
		
	/**
	 * 
	 */
	private static HashMap<String, String> ABK_AUF_UFD_NAME = null;
	
	
	/**
	 * 
	 *
	 */
	private static final void init(){
		if(UFD_NAME_AUF_ABK == null){
			UFD_NAME_AUF_ABK = new HashMap<String, String>();
			ABK_AUF_UFD_NAME = new HashMap<String, String>();

			UFD_NAME_AUF_ABK.put("FahrBahnFeuchte", "FBF");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("FahrBahnGlätte", "FBG");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("FahrBahnOberFlächenTemperatur", "FBT");  //$NON-NLS-1$//$NON-NLS-2$	
			UFD_NAME_AUF_ABK.put("GefrierTemperatur", "GT");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("Helligkeit", "HK");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("LuftTemperatur", "LT");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("NiederschlagsArt", "NS");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("NiederschlagsIntensität", "NI");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("NiederschlagsMenge", "NM");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("RelativeLuftFeuchte", "RLF");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("SchneeHöhe", "SH");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("SichtWeite", "SW");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("TaupunktTemperatur", "TPT");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("TemperaturInTiefe1", "TT1");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("TemperaturInTiefe2", "TT2");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("TemperaturInTiefe3", "TT3");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("WasserFilmDicke", "WFD");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("WindRichtung", "WR");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("FahrBahnOberFlächenZustand", "FBZ");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("LuftDruck", "LD");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("RestSalz", "RS");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("WindGeschwindigkeitMittelWert", "WGM");  //$NON-NLS-1$//$NON-NLS-2$
			UFD_NAME_AUF_ABK.put("WindGeschwindigkeitSpitzenWert", "WGS");  //$NON-NLS-1$//$NON-NLS-2$	
					
			for(String atgName:UFD_NAME_AUF_ABK.keySet()){
				String abkuerzung = UFD_NAME_AUF_ABK.get(atgName);
				ABK_AUF_UFD_NAME.put(abkuerzung, atgName);
			}
		}
	}
	
	
	/**
	 * 
	 * @param atgName
	 * @return
	 */
	public static final String getAbkFuerUfdName(final String atgName){
		init();
		return UFD_NAME_AUF_ABK.get(atgName);
	}
	
	
	/**
	 * 
	 * @param abkuerzung
	 * @return
	 */
	public static final String getUfdNameFuerAbk(final String abkuerzung){
		init();
		return ABK_AUF_UFD_NAME.get(abkuerzung);
	}

}
