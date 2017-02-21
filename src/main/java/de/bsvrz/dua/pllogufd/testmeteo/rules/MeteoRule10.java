package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoWerte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule10 extends MeteoRule {

	public MeteoRule10() {
		super(new UmfeldDatenArt[]{UmfeldDatenArt.wfd, UmfeldDatenArt.fbz}, new UmfeldDatenArt[]{UmfeldDatenArt.wfd, UmfeldDatenArt.fbz});
	}

	@Override
	public void checkRule(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		UmfeldDatenSensorWert fbzWert = werte.getData(UmfeldDatenArt.fbz);
		UmfeldDatenSensorWert wfdWert = werte.getData(UmfeldDatenArt.wfd);

		if (isOk(wfdWert) && wfdWert.getWert() > 0 && isOk(fbzWert) && fbzWert.getWert() == 0) {
			implausibleDatenArten.add(UmfeldDatenArt.wfd);
			implausibleDatenArten.add(UmfeldDatenArt.fbz);
			verletzteBedingungen.add("WFD=" + formatWert(wfdWert) + " mm > 0,0 mm, " + "FBZ=Trocken");
			ids.add("[DUA-PP-MK10]");
		}
	}
}
