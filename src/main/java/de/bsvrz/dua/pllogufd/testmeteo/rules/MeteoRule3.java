package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoWerte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule3 extends MeteoRule {

	public MeteoRule3() {
		super(new UmfeldDatenArt[]{UmfeldDatenArt.ns, UmfeldDatenArt.ni, UmfeldDatenArt.rlf}, new UmfeldDatenArt[]{UmfeldDatenArt.ni});	}

	@Override
	public void checkRule(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		UmfeldDatenSensorWert niWert = werte.getData(UmfeldDatenArt.ni);
		UmfeldDatenSensorWert rlfWert = werte.getData(UmfeldDatenArt.rlf);

		if (werte.keinNiederschlag() && isOk(niWert) && isOk(werte.getNiGrenzNs())
				&& niWert.getWert() > werte.getNiGrenzNs().getWert() && isOk(rlfWert) && isOk(werte.getRlfGrenzTrocken())
				&& rlfWert.getWert() < werte.getRlfGrenzTrocken().getWert()) {
			implausibleDatenArten.add(UmfeldDatenArt.ni);
			verletzteBedingungen.add("NS=Kein Niederschlag, " + "NI=" + formatWert(niWert) + " mm/h > "
					+ formatWert(werte.getNiGrenzNs()) + " mm/h, " + "RLF=" + formatWert(rlfWert) + " % rF < "
					+ formatWert(werte.getRlfGrenzTrocken()) + " % rF");
			ids.add("[DUA-PP-MK03]");
		}
	}
}
