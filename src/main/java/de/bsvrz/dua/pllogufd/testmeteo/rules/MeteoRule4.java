package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoWerte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule4 extends MeteoRule {

	public MeteoRule4() {
		super(new UmfeldDatenArt[]{UmfeldDatenArt.ns, UmfeldDatenArt.ni, UmfeldDatenArt.rlf}, new UmfeldDatenArt[]{UmfeldDatenArt.ni});
	}

	@Override
	public void pruefe(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		UmfeldDatenSensorWert niWert = werte.getData(UmfeldDatenArt.ni);
		UmfeldDatenSensorWert rlfWert = werte.getData(UmfeldDatenArt.rlf);

		if (werte.niederschlag() && isOk(niWert) && niWert.getWert() == 0 && isOk(rlfWert)
				&& isOk(werte.getRlfGrenzNass()) && rlfWert.getWert() > werte.getRlfGrenzNass().getWert()) {
			implausibleDatenArten.add(UmfeldDatenArt.ni);
			verletzteBedingungen.add("NS=Niederschlag, " + "NI=" + formatWert(niWert) + " mm/h, " + "RLF="
					+ formatWert(rlfWert) + " % rF > " + formatWert(werte.getRlfGrenzNass()) + " % rF");
			ids.add("[DUA-PP-MK04]");
		}
	
	}
}
