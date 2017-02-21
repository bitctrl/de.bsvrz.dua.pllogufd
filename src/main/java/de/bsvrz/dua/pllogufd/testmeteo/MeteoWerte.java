package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
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
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.funclib.operatingMessage.OperatingMessage;

public class MeteoWerte {

	/**
	 * Logger
	 */
	private static final Debug debug = Debug.getLogger();

	/**
	 * Template für Betriebsmeldungstext
	 */
	private final MessageTemplate MESSAGE_TEMPLATE = new MessageTemplate(MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN, MessageTemplate.set("attr", ", ", "Messwert ", "Messwerte "),
			MessageTemplate.fixed(" bei meteorologischer Kontrolle an Messstelle "), MessageTemplate.object(),
			MessageTemplate.fixed(" auf fehlerhaft gesetzt, da "), MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". "), MessageTemplate.ids());
	
	private long dataTime;
	private Map<SystemObject, UmfeldDatenArt> sensoren = new LinkedHashMap<>();
	private final Map<UmfeldDatenArt, ResultData> sensorWerte = new LinkedHashMap<>();
	private final Set<UmfeldDatenArt> publiziert = new LinkedHashSet<>();
	private List<MeteoRule> rules = new ArrayList<>();
	private List<MeteoRule> checkedRules = new ArrayList<>();

	private UmfeldDatenSensorWert niGrenzNs;
	private UmfeldDatenSensorWert niGrenzWfd;
	private UmfeldDatenSensorWert wfdGrenzTrocken;
	private UmfeldDatenSensorWert ltGrenzRegen;
	private UmfeldDatenSensorWert ltGrenzSchnee;
	private UmfeldDatenSensorWert rlfGrenzTrocken;
	private UmfeldDatenSensorWert rlfGrenzNass;
	private UmfeldDatenSensorWert swGrenz;

	private DUAUmfeldDatenMessStelle messStelle;

	private boolean sendMessage = true;

	public MeteoWerte(DUAUmfeldDatenMessStelle messStelle) {
		this.messStelle = messStelle;
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
		if (rule.isValidFor(sensoren.values())) {
			rules.add(rule);
		}
	}

	public UmfeldDatenSensorWert getData(UmfeldDatenArt datenArt) {
		ResultData resultData = sensorWerte.get(datenArt);
		if ((resultData == null) || !resultData.hasData()) {
			return null;
		}

		UmfeldDatenSensorDatum wert = new UmfeldDatenSensorDatum(resultData);
		return wert.getWert();
	}

	public Collection<ResultData> setData(SystemObject sensorObject, long timeStamp, ResultData data) {

		Collection<ResultData> resultList = new ArrayList<>();

		if( data == null) {
			return resultList;
		}
		
		if (dataTime != 0) {
			if( timeStamp < dataTime) {
				return resultList;
			}
			if (timeStamp > dataTime) {
				checkRules(resultList, true);
				reset();
			} 
		}

		dataTime = timeStamp;

		UmfeldDatenArt umfeldDatenArt = sensoren.get(sensorObject);
		if (data != null) {
			sensorWerte.put(umfeldDatenArt, data);
		}

		checkRules(resultList, false);

		if( data != null) {
			if( !publiziert.contains(umfeldDatenArt)) {
				boolean publication = true;
				for( MeteoRule rule : rules) {
					if( rule.getResultTypes().contains(umfeldDatenArt)) {
						publication = false;
						break;
					}
				}
				if( publication) {
					publiziert.add(umfeldDatenArt);
					resultList.add(data);
				}
			}
		}
		
		return resultList;
	}

	private void checkRules(Collection<ResultData> resultList, boolean force) {

		Set<String> verletzteBedingungen = new LinkedHashSet<>();
		Set<UmfeldDatenArt> implausibleDatenArten = new LinkedHashSet<>();
		Set<UmfeldDatenArt> plausibleDatenArten = new LinkedHashSet<>();
		Set<String> ids = new LinkedHashSet<>();

		for (MeteoRule rule : rules) {
			if (checkedRules.contains(rule)) {
				continue;
			}

			if (!force) {
				if (!rule.isEvaluableFor(this)) {
					continue;
				}

				Set<MeteoRule> connectedRules = new LinkedHashSet<>(rules);
				connectedRules.removeAll(checkedRules);
				connectedRules.remove(rule);
				boolean checkable = true;
				for (MeteoRule connectedRule : connectedRules) {
					if (!connectedRule.isEvaluableFor(this)) {
						checkable = false;
						break;
					}
				}

				if (!checkable) {
					continue;
				}
			}
			plausibleDatenArten.addAll(rule.pruefe(this, verletzteBedingungen, implausibleDatenArten, ids));
			checkedRules.add(rule);
		}

		plausibleDatenArten.removeAll(implausibleDatenArten);

		for (Entry<UmfeldDatenArt, ResultData> entry : sensorWerte.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			if (!publiziert.contains(entry.getKey())) {
				if (plausibleDatenArten.contains(entry.getKey())) {
					resultList.add(entry.getValue());
				}
				if (implausibleDatenArten.contains(entry.getKey())) {
					UmfeldDatenSensorDatum umfeldDatenSensorDatum = new UmfeldDatenSensorDatum(entry.getValue());
					umfeldDatenSensorDatum.getWert().setFehlerhaftAn();
					umfeldDatenSensorDatum.getDatum(); // Workaround fehlende
														// Aktualisierung
					umfeldDatenSensorDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					resultList.add(umfeldDatenSensorDatum.getVeraendertesOriginalDatum());
				}
			}
		}
		publiziert.addAll(plausibleDatenArten);
		publiziert.addAll(implausibleDatenArten);
		
		if (!ids.isEmpty()) {
			OperatingMessage message = MESSAGE_TEMPLATE.newMessage(messStelle.getObjekt());
			for (String id : ids) {
				message.addId(id);
			}
			for (UmfeldDatenArt art : implausibleDatenArten) {
				message.add("attr", art.getName() + " " + art.getAbkuerzung());
			}
			for (String s : verletzteBedingungen) {
				message.add("values", s);
			}
			if (sendMessage) {
				message.send();
			} else {
				debug.info(message.toString());
			}
		}
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
		sendMessage  = data.getTextValue("erzeugeBetriebsmeldungMeteorologischeKontrolle").getValueText()
				.equals("Ja");

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

	public long getDataTime() {
		return dataTime;
	}

	public void reset() {
		sensorWerte.clear();
		checkedRules.clear();
		publiziert.clear();
	}

	public boolean hasData(UmfeldDatenArt art) {
		return sensorWerte.containsKey(art);
	}

	public boolean containsSensor(SystemObject object) {
		return sensoren.containsKey(object);
	}
}