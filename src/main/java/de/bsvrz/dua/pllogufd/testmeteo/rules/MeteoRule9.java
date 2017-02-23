package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoMessstelle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoParameter.MeteoParameterType;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRuleCondition;
import de.bsvrz.dua.pllogufd.vew.PllogUfdOptions;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule9 extends MeteoRule {

	private static final MeteoRuleCondition CONDITION = new MeteoRuleCondition("NS=Schnee, LT={0} °C > {1} °C",
			new Object[] { UmfeldDatenArt.lt, MeteoParameterType.LT_GRENZ_SCHNEE });

	public MeteoRule9() {
		super(9, new UmfeldDatenArt[] { UmfeldDatenArt.ns, UmfeldDatenArt.lt },
				new UmfeldDatenArt[] { UmfeldDatenArt.ns });
	}

	@Override
	public void checkRule(MeteoMessstelle messStelle, Set<MeteoRuleCondition> verletzteBedingungen,
			Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids, PllogUfdOptions options) {

		if (messStelle.schnee() && messStelle.ltGroesserGrenzSchnee()) {
			implausibleDatenArten.add(UmfeldDatenArt.ns);
			verletzteBedingungen.add(CONDITION);
			ids.add("[DUA-PP-MK09]");
		}
	}
}
