package de.bsvrz.dua.pllogufd.testmeteo;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public abstract class MeteoRule {

	private Set<UmfeldDatenArt> inputTypes = new LinkedHashSet<>();
	private Set<UmfeldDatenArt> resultTypes = new LinkedHashSet<>();

	private Map<UmfeldDatenArt, UmfeldDatenSensorWert> inputData = new LinkedHashMap<>();
	
	protected MeteoRule(UmfeldDatenArt[] inputTypes, UmfeldDatenArt[] resultTypes) {
		for (UmfeldDatenArt inputType : inputTypes) {
			this.inputTypes.add(inputType);
		}
		for (UmfeldDatenArt resultType : resultTypes) {
			this.resultTypes.add(resultType);
		}
	}
	
	public void addData(UmfeldDatenArt datenArt, UmfeldDatenSensorWert data) {
		if( inputTypes.contains(datenArt)) {
			inputData.put(datenArt, data);
		}
	}

	public boolean isValidFor(Collection<UmfeldDatenArt> types) {

		if( !types.containsAll(inputTypes)) {
			return false;
		}

		if( !types.containsAll(resultTypes)) {
			return false;
		}

		return true;
	}

	public abstract void pruefe(MeteoWerte werte, Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten, Set<String> ids);

	protected boolean isOk(final UmfeldDatenSensorWert wert) {
		return wert != null && wert.isOk();
	}
	
	protected String formatWert(final UmfeldDatenSensorWert wert) {
		NumberFormat numberInstance = NumberFormat.getNumberInstance();
		numberInstance.setGroupingUsed(false);
		return numberInstance.format(wert.getSkaliertenWert());
	}
}
