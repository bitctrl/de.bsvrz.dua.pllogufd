package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoWerte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule2 extends MeteoRule {

	public MeteoRule2() {
		super(new UmfeldDatenArt[]{UmfeldDatenArt.ns, UmfeldDatenArt.ni, UmfeldDatenArt.rlf}, new UmfeldDatenArt[]{UmfeldDatenArt.ns});	}

	@Override
	public void pruefe(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		UmfeldDatenSensorWert niWert = werte.getData(UmfeldDatenArt.ni);
		UmfeldDatenSensorWert rlfWert = werte.getData(UmfeldDatenArt.rlf);

		if (werte.niederschlag() && isOk(niWert) && niWert.getWert() == 0 && isOk(rlfWert) && isOk(werte.getRlfGrenzTrocken())
				&& rlfWert.getWert() < werte.getRlfGrenzTrocken().getWert()) {
			implausibleDatenArten.add(UmfeldDatenArt.ns);
			verletzteBedingungen.add("NS=Niederschlag, " + "NI=" + formatWert(niWert) + " mm/h, " + "RLF="
					+ formatWert(rlfWert) + " % rF < " + formatWert(werte.getRlfGrenzTrocken()) + " % rF");
			ids.add("[DUA-PP-MK02]");
		}
	}
}
