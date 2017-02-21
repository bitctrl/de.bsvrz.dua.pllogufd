package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoWerte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule6 extends MeteoRule {

	public MeteoRule6() {
		super(new UmfeldDatenArt[]{UmfeldDatenArt.ni, UmfeldDatenArt.wfd, UmfeldDatenArt.rlf}, new UmfeldDatenArt[]{UmfeldDatenArt.wfd});
	}

	@Override
	public void checkRule(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		UmfeldDatenSensorWert niWert = werte.getData(UmfeldDatenArt.ni);
		UmfeldDatenSensorWert rlfWert = werte.getData(UmfeldDatenArt.rlf);
		UmfeldDatenSensorWert wfdWert = werte.getData(UmfeldDatenArt.wfd);

		if (isOk(niWert) && isOk(werte.getNiGrenzWfd()) && niWert.getWert() > werte.getNiGrenzWfd().getWert() && isOk(wfdWert)
				&& isOk(werte.getWfdGrenzTrocken()) && wfdWert.getWert() <= werte.getWfdGrenzTrocken().getWert() && isOk(rlfWert)
				&& isOk(werte.getRlfGrenzNass()) && rlfWert.getWert() > werte.getRlfGrenzNass().getWert()) {
			implausibleDatenArten.add(UmfeldDatenArt.wfd);
			verletzteBedingungen.add("NI=" + formatWert(niWert) + " mm/h, " + "WFD=" + formatWert(wfdWert)
					+ " mm <= " + formatWert(werte.getWfdGrenzTrocken()) + " mm, " + "RLF=" + formatWert(rlfWert) + " % rF > "
					+ formatWert(werte.getRlfGrenzNass()) + " % rF");
			ids.add("[DUA-PP-MK06]");
		}
	}
}
