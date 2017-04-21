package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoMessstelle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoParameter.MeteoParameterType;
import de.bsvrz.dua.pllogufd.vew.PllogUfdOptions;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRuleCondition;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule3 extends MeteoRule {

	private static final MeteoRuleCondition CONDITION = new MeteoRuleCondition(
			"NS=Kein Niederschlag, NI={0} mm/h > {1} mm/h, RLF={2} % rF < {3} % rF", new Object[] { UmfeldDatenArt.ni,
					MeteoParameterType.NI_GRENZ_NS, UmfeldDatenArt.rlf, MeteoParameterType.RLF_GRENZ_TROCKEN });

	public MeteoRule3() {
		super(3, new UmfeldDatenArt[] { UmfeldDatenArt.ns, UmfeldDatenArt.ni, UmfeldDatenArt.rlf },
				new UmfeldDatenArt[] { UmfeldDatenArt.ni });
	}

	@Override
	public void checkRule(MeteoMessstelle messStelle, Set<MeteoRuleCondition> verletzteBedingungen,
			Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids, PllogUfdOptions options) {

		if (messStelle.keinNiederschlag() && messStelle.niGroesserGrenzNs() && messStelle.rlfKleinerTrocken()) {
			implausibleDatenArten.add(UmfeldDatenArt.ni);
			verletzteBedingungen.add(CONDITION);
			ids.add("[DUA-PP-MK03]");
		}
	}
}
