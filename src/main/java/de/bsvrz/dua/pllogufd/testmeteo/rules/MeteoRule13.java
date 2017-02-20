package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoWerte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule13 extends MeteoRule {

	public MeteoRule13() {
		super(new UmfeldDatenArt[]{UmfeldDatenArt.ns, UmfeldDatenArt.ni, UmfeldDatenArt.rlf}, new UmfeldDatenArt[]{UmfeldDatenArt.ns, UmfeldDatenArt.ni});
	}

	@Override
	public void pruefe(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		UmfeldDatenSensorWert niWert = werte.getData(UmfeldDatenArt.ni);
		UmfeldDatenSensorWert rlfWert = werte.getData(UmfeldDatenArt.rlf);

		boolean rlfUndef = !isOk(rlfWert) || (isOk(werte.getRlfGrenzNass()) && isOk(werte.getRlfGrenzTrocken())
				&& rlfWert.getWert() >= werte.getRlfGrenzTrocken().getWert() && rlfWert.getWert() <= werte.getRlfGrenzNass().getWert());

		// Prüfung 12
		// Prüfung 13
		if (werte.niederschlag() && isOk(niWert) && niWert.getWert() == 0 && rlfUndef) {
			implausibleDatenArten.add(UmfeldDatenArt.ns);
			implausibleDatenArten.add(UmfeldDatenArt.ni);
			verletzteBedingungen.add("NS=Niederschlag, " + "NI=" + formatWert(niWert) + " mm/h");
			ids.add("[DUA-PP-MK13]");
		}
	
	}
}
