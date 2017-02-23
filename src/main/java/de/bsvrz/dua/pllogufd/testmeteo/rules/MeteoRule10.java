package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoMessstelle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRuleCondition;
import de.bsvrz.dua.pllogufd.vew.PllogUfdOptions;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule10 extends MeteoRule {

	private static final MeteoRuleCondition CONDITION = new MeteoRuleCondition("WFD={0} mm > 0,0 mm, FBZ=Trocken",
			new Object[] { UmfeldDatenArt.wfd });

	public MeteoRule10() {
		super(new UmfeldDatenArt[] { UmfeldDatenArt.wfd, UmfeldDatenArt.fbz },
				new UmfeldDatenArt[] { UmfeldDatenArt.wfd, UmfeldDatenArt.fbz });
	}

	@Override
	public void checkRule(MeteoMessstelle messStelle, Set<MeteoRuleCondition> verletzteBedingungen,
			Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids, PllogUfdOptions options) {

		if (options.isUseWfdTrockenGrenzwert()) {
			if (messStelle.wfdGroesserTrockenOrNull() && messStelle.fbzTrocken()) {
				implausibleDatenArten.add(UmfeldDatenArt.wfd);
				implausibleDatenArten.add(UmfeldDatenArt.fbz);
				verletzteBedingungen.add(CONDITION);
				ids.add("[DUA-PP-MK10]");
			}
		} else {
			if (messStelle.wfdGroesserNull() && messStelle.fbzTrocken()) {
				implausibleDatenArten.add(UmfeldDatenArt.wfd);
				implausibleDatenArten.add(UmfeldDatenArt.fbz);
				verletzteBedingungen.add(CONDITION);
				ids.add("[DUA-PP-MK10]");
			}
		}
	}
}
