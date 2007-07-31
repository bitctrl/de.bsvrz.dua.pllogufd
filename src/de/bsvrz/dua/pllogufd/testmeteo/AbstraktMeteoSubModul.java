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

package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.ArrayList;
import java.util.List;

import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;

/**
 * Abstraktes Submodul der Komponente Meteorologischen Kontrolle 
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public abstract class AbstraktMeteoSubModul 
extends AbstraktBearbeitungsKnotenAdapter{
	
	
	/**
	 * Erfragt die Umfelddaten-Messstelle an der ein bestimmter Sensor konfiguriert ist
	 * 
	 * @param ufdsObjekt das Systemobjekt eines
	 * Umfelddatensensors
	 * @return die Umfelddaten-Messstelle oder <code>null</code>,
	 * wenn der Sensor nicht betrachtet wird
	 */
	public abstract AbstraktMeteoMessstelle getMessStelleVonSensor(final SystemObject ufdsObjekt);
	
	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {		
		if(resultate != null){
						
			List<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();
			for(ResultData resultat:resultate){
				if(resultat != null){

					AbstraktMeteoMessstelle messstelle = getMessStelleVonSensor(resultat.getObject());
					if(messstelle != null){
						ResultData[] ergebnisse = messstelle.aktualisiereDaten(resultat);
						if(ergebnisse != null && ergebnisse.length > 0){
							for(ResultData ergebnis:ergebnisse){
								weiterzuleitendeResultate.add(ergebnis);
							}
						}						
					}else{
						weiterzuleitendeResultate.add(resultat);
					}
				}				
			}
			
			/**
			 * Resultate weitergeben
			 */
			if(this.knoten != null && !weiterzuleitendeResultate.isEmpty()){
				this.knoten.aktualisiereDaten(weiterzuleitendeResultate.toArray(new ResultData[0]));
			}
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return null;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// hier wird nicht publiziert
	}

}
