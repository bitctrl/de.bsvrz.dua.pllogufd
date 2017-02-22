package de.bsvrz.dua.pllogufd.testmeteo.rules;

import java.util.Set;

import de.bsvrz.dua.pllogufd.testmeteo.MeteoMessstelle;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoParameter.MeteoParameterType;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRule;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoRuleCondition;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoRule12 extends MeteoRule {

	private static final MeteoRuleCondition CONDITION = new MeteoRuleCondition(
			"NS=Kein Niederschlag, " + "NI={0} mm/h > {1} mm/h",
			new Object[] { UmfeldDatenArt.ni, MeteoParameterType.NI_GRENZ_NS });

	public MeteoRule12() {
		super(new UmfeldDatenArt[] { UmfeldDatenArt.ns, UmfeldDatenArt.ni, UmfeldDatenArt.rlf },
				new UmfeldDatenArt[] { UmfeldDatenArt.ns, UmfeldDatenArt.ni });
	}

	@Override
	public void checkRule(MeteoMessstelle messStelle, Set<MeteoRuleCondition> verletzteBedingungen,
			Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids) {

		if (messStelle.keinNiederschlag() && messStelle.niGroesserGrenzNs()
				&& (messStelle.rlfUndef() || messStelle.rlfZwischenTrockenUndNass())) {
			implausibleDatenArten.add(UmfeldDatenArt.ns);
			implausibleDatenArten.add(UmfeldDatenArt.ni);
			verletzteBedingungen.add(CONDITION);
			ids.add("[DUA-PP-MK12]");
		}
	}
}
