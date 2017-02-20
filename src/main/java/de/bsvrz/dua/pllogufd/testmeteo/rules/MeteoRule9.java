package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoWerte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule9 extends MeteoRule {

	public MeteoRule9() {
		super(new UmfeldDatenArt[]{UmfeldDatenArt.ns, UmfeldDatenArt.lt}, new UmfeldDatenArt[]{UmfeldDatenArt.ns});
	}

	@Override
	public void pruefe(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		UmfeldDatenSensorWert ltWert = werte.getData(UmfeldDatenArt.lt);

		if (werte.schnee() && isOk(ltWert) && isOk(werte.getLtGrenzSchnee())
				&& ltWert.getWert() > werte.getLtGrenzSchnee().getWert()) {
			implausibleDatenArten.add(UmfeldDatenArt.ns);
			verletzteBedingungen
					.add("NS=Schnee, " + "LT=" + formatWert(ltWert) + " °C > " + formatWert(werte.getLtGrenzSchnee()) + " °C");
			ids.add("[DUA-PP-MK09]");
		}
		
	}
}
