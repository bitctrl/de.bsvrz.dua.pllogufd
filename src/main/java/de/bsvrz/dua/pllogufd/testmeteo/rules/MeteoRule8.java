package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoWerte;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule8 extends MeteoRule {

	public MeteoRule8() {
		super(new UmfeldDatenArt[]{UmfeldDatenArt.ns, UmfeldDatenArt.lt}, new UmfeldDatenArt[]{UmfeldDatenArt.ns});
	}

	@Override
	public void pruefe(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		UmfeldDatenSensorWert ltWert = werte.getData(UmfeldDatenArt.lt);

		if (werte.regen() && isOk(ltWert) && isOk(werte.getLtGrenzRegen())
				&& ltWert.getWert() < werte.getLtGrenzRegen().getWert()) {
			implausibleDatenArten.add(UmfeldDatenArt.ns);
			verletzteBedingungen
					.add("NS=Regen, " + "LT=" + formatWert(ltWert) + " °C < " + formatWert(werte.getLtGrenzRegen()) + " °C");
			ids.add("[DUA-PP-MK08]");
		}
		
	}
}
