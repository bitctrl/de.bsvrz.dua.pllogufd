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

package de.bsvrz.dua.pllogufd.vew;

import java.util.Collection;

import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.AttributeGroup;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.application.StandardApplicationRunner;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.plformal.plformal.PlPruefungFormal;
import de.bsvrz.dua.pllogufd.plformal.PlFormUFDStandardAspekteVersorger;
import de.bsvrz.dua.pllogufd.testaufab.AnstiegAbfallKontrolle;
import de.bsvrz.dua.pllogufd.testausfall.UFDAusfallUeberwachung;
import de.bsvrz.dua.pllogufd.testdiff.UFDDifferenzialKontrolle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolle;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Dieses Modul Verwaltung ist die zentrale Steuereinheit der SWE PL-Prüfung
 * logisch UFD. Seine Aufgabe besteht in der Auswertung der Aufrufparameter,
 * der Anmeldung beim Datenverteiler und der entsprechenden Initialisierung
 * der Module. Weiter ist das Modul Verwaltung für die Anmeldung der zu prüfenden
 * Daten zuständig. Die Verwaltung gibt ein Objekt des Moduls Ausfallüberwachun
 * als Beobachterobjekt an, an das die zu überprüfenden Daten durch den
 * Aktualisierungsmechanismus weitergeleitet werden. Weiterhin stellt die Verwaltung
 * die Verkettung der Module Ausfallüberwachung, PL-Prüfung formal, Differenzialkontrolle,
 * Anstieg-Abfall-Kontrolle und der Komponente Meteorologische Kontrolle in
 * dieser Reihenfolge durch die Angabe eines Moduls als Beobachterobjekt des
 * jeweiligen Vorgängermoduls her.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class VerwaltungPlPruefungLogischUFD
extends AbstraktVerwaltungsAdapter {

	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Modul Pl-Prüfung formal 
	 */
	private PlPruefungFormal formal = null;
	
	/**
	 * Modul UFD-Ausfallüberwachung 
	 */
	private UFDAusfallUeberwachung ausfall = null;
	
	/**
	 * Modul UFD-Differenzialkontrolle 
	 */
	private UFDDifferenzialKontrolle diff = null;
	
	/**
	 * Modul Anstieg-Abfall-Kontrolle
	 */
	private AnstiegAbfallKontrolle aak = null;
	
	/**
	 * Modul Meteorologische Kontrolle 
	 */
	private MeteorologischeKontrolle mk  = null;
	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiere()
	throws DUAInitialisierungsException {
		
		UmfeldDatenArt.initialisiere(this.verbindung);
		
		String infoStr = Konstante.LEERSTRING;
		Collection<SystemObject> plLogLveObjekte = DUAUtensilien.getBasisInstanzen(
				this.verbindung.getDataModel().getType(DUAKonstanten.TYP_UFD_SENSOR),
				this.verbindung, this.getKonfigurationsBereiche());
		this.objekte = plLogLveObjekte.toArray(new SystemObject[0]);
		
		for(SystemObject obj:this.objekte){
			infoStr += obj + "\n"; //$NON-NLS-1$
		}
		LOGGER.config("---\nBetrachtete Objekte:\n" + infoStr + "---\n"); //$NON-NLS-1$ //$NON-NLS-2$

		IStandardAspekte standardAspekte = new PlLogUFDStandardAspekteVersorger(this).getStandardPubInfos();
		
		/**
		 * Instanziierung
		 */
		this.ausfall = new UFDAusfallUeberwachung();
		this.formal = new PlPruefungFormal(
				new PlFormUFDStandardAspekteVersorger(this).getStandardPubInfos());
		this.diff = new UFDDifferenzialKontrolle();
		this.aak = new AnstiegAbfallKontrolle();
		this.mk = new MeteorologischeKontrolle(standardAspekte);
		
		/**
		 * Initialisierung
		 */
		this.ausfall.setNaechstenBearbeitungsKnoten(this.formal);
		this.ausfall.initialisiere(this);
		
		this.formal.setNaechstenBearbeitungsKnoten(this.diff);
		this.formal.setPublikation(true);
		this.formal.initialisiere(this);
		
		this.diff.setNaechstenBearbeitungsKnoten(this.aak);
		this.diff.initialisiere(this);
		
		this.aak.setNaechstenBearbeitungsKnoten(this.mk);
		this.aak.initialisiere(this);
		
		this.mk.setNaechstenBearbeitungsKnoten(null);
		this.mk.setPublikation(true);
		this.mk.initialisiere(this);

		/**
		 * Datenanmeldung
		 */
		for(AttributeGroup atg:standardAspekte.getAlleAttributGruppen()){
			
			for(SystemObject obj:this.objekte){
				
				if(obj.getType().getAttributeGroups().contains(atg)){
					
					DataDescription datenBeschreibung = new DataDescription(atg,
							verbindung.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
							(short)0);
					
					this.verbindung.subscribeReceiver(this, obj,
							datenBeschreibung, ReceiveOptions.delayed(), ReceiverRole.receiver());
					
				}
				
			}
			
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public SWETyp getSWETyp() {
		return SWETyp.SWE_PL_PRUEFUNG_LOGISCH_UFD;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		this.ausfall.aktualisiereDaten(resultate);
	}

	
	/**
	 * Startet diese Applikation
	 * 
	 * @param args Argumente der Kommandozeile
	 */
	public static void main(String argumente[]){
        Thread.setDefaultUncaughtExceptionHandler(new Thread.
        				UncaughtExceptionHandler(){
            public void uncaughtException(@SuppressWarnings("unused")
			Thread t, Throwable e) {
            	e.printStackTrace();
                LOGGER.error("Applikation wird wegen" +  //$NON-NLS-1$
                		" unerwartetem Fehler beendet", e);  //$NON-NLS-1$
                Runtime.getRuntime().exit(0);
            }
        });
		StandardApplicationRunner.run(
					new VerwaltungPlPruefungLogischUFD(), argumente);
	}
	
}
