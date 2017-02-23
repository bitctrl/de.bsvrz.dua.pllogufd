package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoMessstelle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoParameter.MeteoParameterType;
import de.bsvrz.dua.pllogufd.vew.PllogUfdOptions;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRuleCondition;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule8 extends MeteoRule {

	private static final MeteoRuleCondition CONDITION = new MeteoRuleCondition("NS=Regen, LT={0} °C < {1} °C",
			new Object[] { UmfeldDatenArt.lt, MeteoParameterType.LT_GRENZ_REGEN });

	public MeteoRule8() {
		super(new UmfeldDatenArt[] { UmfeldDatenArt.ns, UmfeldDatenArt.lt },
				new UmfeldDatenArt[] { UmfeldDatenArt.ns });
	}

	@Override
	public void checkRule(MeteoMessstelle messStelle, Set<MeteoRuleCondition> verletzteBedingungen,
			Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids, PllogUfdOptions options) {

		if (messStelle.regen() && messStelle.ltKleinerGrenzRegen()) {
			implausibleDatenArten.add(UmfeldDatenArt.ns);
			verletzteBedingungen.add(CONDITION);
			ids.add("[DUA-PP-MK08]");
		}
	}
}
