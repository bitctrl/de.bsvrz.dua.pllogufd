package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoWerte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule7 extends MeteoRule {

	public MeteoRule7() {
		super(new UmfeldDatenArt[]{UmfeldDatenArt.sw, UmfeldDatenArt.ns, UmfeldDatenArt.rlf}, new UmfeldDatenArt[]{UmfeldDatenArt.sw});
	}

	@Override
	public void pruefe(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		UmfeldDatenSensorWert rlfWert = werte.getData(UmfeldDatenArt.rlf);
		UmfeldDatenSensorWert swWert = werte.getData(UmfeldDatenArt.sw);
		
		if (isOk(swWert) && isOk(werte.getSwGrenz()) && swWert.getWert() <= werte.getSwGrenz().getWert() && werte.keinNiederschlag() && isOk(rlfWert) && isOk(werte.getRlfGrenzTrocken())
				&& rlfWert.getWert() < werte.getRlfGrenzTrocken().getWert()) {
			implausibleDatenArten.add(UmfeldDatenArt.sw);
			verletzteBedingungen.add(
					"SW=" + formatWert(swWert) + " m <= " + formatWert(werte.getSwGrenz()) + " m, " + "NS=Kein Niederschlag, "
							+ "RLF=" + formatWert(rlfWert) + " % rF < " + formatWert(werte.getRlfGrenzTrocken()) + " % rF");
			ids.add("[DUA-PP-MK07]");
		}
		
	}
}
