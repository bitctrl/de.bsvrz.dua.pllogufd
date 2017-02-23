package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoMessstelle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoParameter.MeteoParameterType;
import de.bsvrz.dua.pllogufd.vew.PllogUfdOptions;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRuleCondition;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule4 extends MeteoRule {

	private static final MeteoRuleCondition CONDITION = new MeteoRuleCondition(
			"NS=Niederschlag, NI={0} mm/h, " + "RLF={1} % rF > {2} % rF",
			new Object[] { UmfeldDatenArt.ni, UmfeldDatenArt.rlf, MeteoParameterType.RLF_GRENZ_NASS });

	public MeteoRule4() {
		super(new UmfeldDatenArt[] { UmfeldDatenArt.ns, UmfeldDatenArt.ni, UmfeldDatenArt.rlf },
				new UmfeldDatenArt[] { UmfeldDatenArt.ni });
	}

	@Override
	public void checkRule(MeteoMessstelle messStelle, Set<MeteoRuleCondition> verletzteBedingungen,
			Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids, PllogUfdOptions options) {

		if (options.isUseNiGrenzNS()) {
			if (messStelle.niederschlag() && messStelle.niKleinerGrenzNsOrGleichNull() && messStelle.rlfGroesserNass()) {
				implausibleDatenArten.add(UmfeldDatenArt.ni);
				verletzteBedingungen.add(CONDITION);
				ids.add("[DUA-PP-MK04]");
			}
		} else {
			if (messStelle.niederschlag() && messStelle.niIsNull() && messStelle.rlfGroesserNass()) {
				implausibleDatenArten.add(UmfeldDatenArt.ni);
				verletzteBedingungen.add(CONDITION);
				ids.add("[DUA-PP-MK04]");
			}
		}
	}
}
