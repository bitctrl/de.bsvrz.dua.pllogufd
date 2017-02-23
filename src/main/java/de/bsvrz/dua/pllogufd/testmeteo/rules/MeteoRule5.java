package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoMessstelle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoParameter.MeteoParameterType;
import de.bsvrz.dua.pllogufd.vew.PllogUfdOptions;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRuleCondition;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule5 extends MeteoRule {

	private static final MeteoRuleCondition CONDITION = new MeteoRuleCondition(
			"NI={0} mm/h, WFD={1} mm <= {2} mm, RLF={3} % rF < {4} % rF",
			new Object[] { UmfeldDatenArt.ni, UmfeldDatenArt.wfd, MeteoParameterType.WFD_GRENZ_TROCKEN,
					UmfeldDatenArt.rlf, MeteoParameterType.RLF_GRENZ_TROCKEN });

	public MeteoRule5() {
		super(new UmfeldDatenArt[] { UmfeldDatenArt.ni, UmfeldDatenArt.wfd, UmfeldDatenArt.rlf },
				new UmfeldDatenArt[] { UmfeldDatenArt.ni });
	}

	@Override
	public void checkRule(MeteoMessstelle messStelle, Set<MeteoRuleCondition> verletzteBedingungen,
			Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids, PllogUfdOptions options) {

		if (messStelle.niGroesserGrenzWfd() && messStelle.wfdKleinerGleichTrocken() && messStelle.rlfKleinerTrocken()) {
			implausibleDatenArten.add(UmfeldDatenArt.ni);
			verletzteBedingungen.add(CONDITION);
			ids.add("[DUA-PP-MK05]");
		}
	}
}
