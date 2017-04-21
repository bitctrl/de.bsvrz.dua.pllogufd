package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoMessstelle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRuleCondition;
import de.bsvrz.dua.pllogufd.vew.PllogUfdOptions;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule13 extends MeteoRule {

	private static final MeteoRuleCondition CONDITION = new MeteoRuleCondition("NS=Niederschlag, NI={0} mm/h",
			new Object[] { UmfeldDatenArt.ni });

	public MeteoRule13() {
		super(13, new UmfeldDatenArt[] { UmfeldDatenArt.ns, UmfeldDatenArt.ni, UmfeldDatenArt.rlf },
				new UmfeldDatenArt[] { UmfeldDatenArt.ns, UmfeldDatenArt.ni });
	}

	@Override
	public void checkRule(MeteoMessstelle messStelle, Set<MeteoRuleCondition> verletzteBedingungen,
			Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids, PllogUfdOptions options) {

		if (messStelle.niederschlag() && messStelle.niIsNull()
				&& (messStelle.rlfUndef() || messStelle.rlfZwischenTrockenUndNass())) {
			implausibleDatenArten.add(UmfeldDatenArt.ns);
			implausibleDatenArten.add(UmfeldDatenArt.ni);
			verletzteBedingungen.add(CONDITION);
			ids.add("[DUA-PP-MK13]");
		}
	}
}
