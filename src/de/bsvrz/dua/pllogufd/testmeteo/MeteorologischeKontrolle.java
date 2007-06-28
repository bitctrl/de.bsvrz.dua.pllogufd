package de.bsvrz.dua.pllogufd.testmeteo;

import stauma.dav.clientside.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;

public class MeteorologischeKontrolle extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Standardkonstruktor
	 * 
	 * @param stdAspekte Informationen zu den
	 * Standardpublikationsaspekten für diese
	 * Instanz des Moduls Pl-Prüfung formal
	 */
	public MeteorologischeKontrolle(final IStandardAspekte stdAspekte){
		if(stdAspekte != null){
			this.standardAspekte = stdAspekte;
		}
	}
	
	public void aktualisiereDaten(ResultData[] resultate) {
		// TODO Automatisch erstellter Methoden-Stub

	}

	public ModulTyp getModulTyp() {
		// TODO Automatisch erstellter Methoden-Stub
		return null;
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Automatisch erstellter Methoden-Stub

	}

}
