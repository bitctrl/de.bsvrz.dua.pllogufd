package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoMessstelle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoParameter.MeteoParameterType;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRuleCondition;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule7 extends MeteoRule {

	private static final MeteoRuleCondition CONDITION = new MeteoRuleCondition(
			"SW={0} m <= {1} m, NS=Kein Niederschlag, RLF={2} % rF < {3} % rF", new Object[] { UmfeldDatenArt.sw,
					MeteoParameterType.SW_GRENZ, UmfeldDatenArt.rlf, MeteoParameterType.RLF_GRENZ_TROCKEN });

	public MeteoRule7() {
		super(new UmfeldDatenArt[] { UmfeldDatenArt.sw, UmfeldDatenArt.ns, UmfeldDatenArt.rlf },
				new UmfeldDatenArt[] { UmfeldDatenArt.sw });
	}

	@Override
	public void checkRule(MeteoMessstelle messStelle, Set<MeteoRuleCondition> verletzteBedingungen,
			Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		if (messStelle.swKleinerGleichGrenze() && messStelle.keinNiederschlag() && messStelle.rlfKleinerTrocken()) {
			implausibleDatenArten.add(UmfeldDatenArt.sw);
			verletzteBedingungen.add(CONDITION);
			ids.add("[DUA-PP-MK07]");
		}
	}
}
