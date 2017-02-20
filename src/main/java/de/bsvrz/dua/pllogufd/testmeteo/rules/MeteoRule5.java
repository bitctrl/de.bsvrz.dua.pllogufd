package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoWerte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule5 extends MeteoRule {

	public MeteoRule5() {
		super(new UmfeldDatenArt[]{UmfeldDatenArt.ni, UmfeldDatenArt.wfd, UmfeldDatenArt.rlf}, new UmfeldDatenArt[]{UmfeldDatenArt.ni});
	}

	@Override
	public void pruefe(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		UmfeldDatenSensorWert niWert = werte.getData(UmfeldDatenArt.ni);
		UmfeldDatenSensorWert rlfWert = werte.getData(UmfeldDatenArt.rlf);
		UmfeldDatenSensorWert wfdWert = werte.getData(UmfeldDatenArt.wfd);

		if (isOk(niWert) && isOk(werte.getNiGrenzWfd()) && niWert.getWert() > werte.getNiGrenzWfd().getWert() && isOk(wfdWert)
				&& isOk(werte.getWfdGrenzTrocken()) && wfdWert.getWert() <= werte.getWfdGrenzTrocken().getWert() && isOk(rlfWert)
				&& isOk(werte.getRlfGrenzTrocken()) && rlfWert.getWert() < werte.getRlfGrenzTrocken().getWert()) {
			implausibleDatenArten.add(UmfeldDatenArt.ni);
			verletzteBedingungen.add("NI=" + formatWert(niWert) + " mm/h, " + "WFD=" + formatWert(wfdWert)
					+ " mm <= " + formatWert(werte.getWfdGrenzTrocken()) + " mm, " + "RLF=" + formatWert(rlfWert) + " % rF < "
					+ formatWert(werte.getRlfGrenzTrocken()) + " % rF");
			ids.add("[DUA-PP-MK05]");
		}
		
	}
}
