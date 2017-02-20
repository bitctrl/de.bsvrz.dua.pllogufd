package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule1;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule10;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule11;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule12;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule13;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule2;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule3;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule4;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule5;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule6;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule7;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule8;
import de.bsvrz.dua.pllogufd.testmeteo.rules.MeteoRule9;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

public class MeteoWerte {
	
	private long dataTime;
	private Map<SystemObject, UmfeldDatenArt> sensoren = new LinkedHashMap<>();
	private final Map<UmfeldDatenArt, UmfeldDatenSensorWert> sensorWerte = new LinkedHashMap<>();
	private List<MeteoRule> rules = new ArrayList<>();
	
	private UmfeldDatenSensorWert niGrenzNs;
	private UmfeldDatenSensorWert niGrenzWfd;
	private UmfeldDatenSensorWert wfdGrenzTrocken;
	private UmfeldDatenSensorWert ltGrenzRegen;
	private UmfeldDatenSensorWert ltGrenzSchnee;
	private UmfeldDatenSensorWert rlfGrenzTrocken;
	private UmfeldDatenSensorWert rlfGrenzNass;
	private UmfeldDatenSensorWert swGrenz;
	
	public MeteoWerte(DUAUmfeldDatenMessStelle messStelle) {
		initSensor(messStelle, UmfeldDatenArt.ni);
		initSensor(messStelle, UmfeldDatenArt.ns);
		initSensor(messStelle, UmfeldDatenArt.wfd);
		initSensor(messStelle, UmfeldDatenArt.fbz);
		initSensor(messStelle, UmfeldDatenArt.lt);
		initSensor(messStelle, UmfeldDatenArt.rlf);
		initSensor(messStelle, UmfeldDatenArt.sw);
		
		initRules();
	}

	private void initSensor(final DUAUmfeldDatenMessStelle messStelle, final UmfeldDatenArt datenArt) {
		DUAUmfeldDatenSensor hauptSensor = messStelle.getHauptSensor(datenArt);
		if (hauptSensor == null) {
			return;
		}

		SystemObject sensor = hauptSensor.getObjekt();
		if (sensor != null) {
			sensoren.put(sensor, datenArt);
		}
	}

	private void initRules() {
		initRule(new MeteoRule1());
		initRule(new MeteoRule2());
		initRule(new MeteoRule3());
		initRule(new MeteoRule4());
		initRule(new MeteoRule5());
		initRule(new MeteoRule6());
		initRule(new MeteoRule7());
		initRule(new MeteoRule8());
		initRule(new MeteoRule9());
		initRule(new MeteoRule10());
		initRule(new MeteoRule11());
		initRule(new MeteoRule12());
		initRule(new MeteoRule13());
	}

	private void initRule(MeteoRule rule) {
		if( rule.isValidFor(sensoren.values())) {
			rules.add(rule);
		}
	}

	public UmfeldDatenSensorWert getData(UmfeldDatenArt datenArt) {
		return sensorWerte.get(datenArt);
	}

	public void setData(SystemObject sensorObject, UmfeldDatenSensorWert data) {
		UmfeldDatenArt umfeldDatenArt = sensoren.get(sensorObject);
		sensorWerte.put(umfeldDatenArt, data);
	}

	public void pruefe(Set<String> verletzteBedingungen, Set<UmfeldDatenArt> implausibleDatenArten,
			Set<String> ids) {
		
		for( MeteoRule rule : rules) {
			rule.pruefe(this, verletzteBedingungen, implausibleDatenArten,
					ids);
		}
		// TODO Auto-generated method stub
		
	}
	
	public boolean keinNiederschlag() {
		UmfeldDatenSensorWert data = getData(UmfeldDatenArt.ns);
		return (data != null) && data.isOk() && data.getWert() == 0;
	}

	public boolean niederschlag() {
		UmfeldDatenSensorWert data = getData(UmfeldDatenArt.ns);
		return (data != null) && data.isOk() && data.getWert() >= 50 && data.getWert() <= 69;
	}

	public boolean regen() {
		UmfeldDatenSensorWert data = getData(UmfeldDatenArt.ns);
		return (data != null) && data.isOk()
				&& ((data.getWert() >= 40 && data.getWert() <= 69) || (data.getWert() >= 80 && data.getWert() <= 84));
	}

	public boolean schnee() {
		UmfeldDatenSensorWert data = getData(UmfeldDatenArt.ns);
		return (data != null) && data.isOk()
				&& ((data.getWert() >= 70 && data.getWert() <= 78) || (data.getWert() >= 85 && data.getWert() <= 87));
	}

	public void updateGrenzwerte(Data data) {
		niGrenzNs = get(data, "NIgrenzNS", UmfeldDatenArt.ni);
		niGrenzWfd = get(data, "NIgrenzWFD", UmfeldDatenArt.ni);
		wfdGrenzTrocken = get(data, "WFDgrenzTrocken", UmfeldDatenArt.wfd);
		ltGrenzRegen = get(data, "LTgrenzRegen", UmfeldDatenArt.lt);
		ltGrenzSchnee = get(data, "LTgrenzSchnee", UmfeldDatenArt.lt);
		rlfGrenzTrocken = get(data, "RLFgrenzTrocken", UmfeldDatenArt.rlf);
		rlfGrenzNass = get(data, "RLFgrenzNass", UmfeldDatenArt.rlf);
		swGrenz = get(data, "SWgrenz", UmfeldDatenArt.sw);
	}
	
	private static UmfeldDatenSensorWert get(final Data data, final String name, final UmfeldDatenArt art) {
		UmfeldDatenSensorWert wert = new UmfeldDatenSensorWert(art);
		wert.setWert(data.getUnscaledValue(name).longValue());
		return wert;
	}

	public UmfeldDatenSensorWert getNiGrenzNs() {
		return niGrenzNs;
	}

	public UmfeldDatenSensorWert getNiGrenzWfd() {
		return niGrenzWfd;
	}

	public UmfeldDatenSensorWert getWfdGrenzTrocken() {
		return wfdGrenzTrocken;
	}

	public UmfeldDatenSensorWert getLtGrenzRegen() {
		return ltGrenzRegen;
	}

	public UmfeldDatenSensorWert getLtGrenzSchnee() {
		return ltGrenzSchnee;
	}

	public UmfeldDatenSensorWert getRlfGrenzTrocken() {
		return rlfGrenzTrocken;
	}

	public UmfeldDatenSensorWert getRlfGrenzNass() {
		return rlfGrenzNass;
	}

	public UmfeldDatenSensorWert getSwGrenz() {
		return swGrenz;
	}

}