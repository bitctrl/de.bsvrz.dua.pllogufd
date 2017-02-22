package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.LinkedHashMap;
import java.util.Map;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorWert;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

public class MeteoParameter implements ClientReceiverInterface {

	public enum MeteoParameterType {
		NI_GRENZ_NS,
		NI_GRENZ_WFD,
		WFD_GRENZ_TROCKEN,
		LT_GRENZ_REGEN,
		LT_GRENZ_SCHNEE,
		RLF_GRENZ_TROCKEN,
		RLF_GRENZ_NASS,
		SW_GRENZ;
	}
	
	private static final Debug _debug = Debug.getLogger();

	Map<MeteoParameterType, UmfeldDatenSensorWert> grenzWerte = new LinkedHashMap<>();
	private boolean sendMessage = true;

	MeteoParameter(ClientDavInterface connection, DUAUmfeldDatenMessStelle messStelle) {
		DataModel dataModel = connection.getDataModel();
		String atgPid = "atg.ufdmsParameterMeteorologischeKontrolle";
		AttributeGroup attributeGroup = dataModel.getAttributeGroup(atgPid);
		Aspect aspect = dataModel.getAspect("asp.parameterSoll");

		if (attributeGroup == null) {
			_debug.warning("Es konnte keine Parameter-Attributgruppe für die " + "Meteorologische Kontrolle des Objektes "
					+ messStelle + " bestimmt werden\n" + "Atg-Name: " + atgPid);
		} else {
			connection.subscribeReceiver(this, messStelle.getObjekt(), new DataDescription(attributeGroup, aspect),
					ReceiveOptions.normal(), ReceiverRole.receiver());
		}
	}
	
	@Override
	public void update(final ResultData[] results) {
		for (ResultData result : results) {
			if (result.hasData()) {
				Data data = result.getData();
				sendMessage = data.getTextValue("erzeugeBetriebsmeldungMeteorologischeKontrolle").getValueText()
						.equals("Ja");

				grenzWerte.clear();
				
				grenzWerte.put(MeteoParameterType.NI_GRENZ_NS, initParameter(data, "NIgrenzNS", UmfeldDatenArt.ni));
				grenzWerte.put(MeteoParameterType.NI_GRENZ_WFD, initParameter(data, "NIgrenzWFD", UmfeldDatenArt.ni));
				grenzWerte.put(MeteoParameterType.WFD_GRENZ_TROCKEN, initParameter(data, "WFDgrenzTrocken", UmfeldDatenArt.wfd));
				grenzWerte.put(MeteoParameterType.LT_GRENZ_REGEN, initParameter(data, "LTgrenzRegen", UmfeldDatenArt.lt));
				grenzWerte.put(MeteoParameterType.LT_GRENZ_SCHNEE, initParameter(data, "LTgrenzSchnee", UmfeldDatenArt.lt));
				grenzWerte.put(MeteoParameterType.RLF_GRENZ_TROCKEN, initParameter(data, "RLFgrenzTrocken", UmfeldDatenArt.rlf));
				grenzWerte.put(MeteoParameterType.RLF_GRENZ_NASS, initParameter(data, "RLFgrenzNass", UmfeldDatenArt.rlf));
				grenzWerte.put(MeteoParameterType.SW_GRENZ, initParameter(data, "SWgrenz", UmfeldDatenArt.sw));
			}
		}
	}
	
	private UmfeldDatenSensorWert initParameter(final Data data, final String name, final UmfeldDatenArt art) {
		UmfeldDatenSensorWert wert = new UmfeldDatenSensorWert(art);
		wert.setWert(data.getUnscaledValue(name).longValue());
		return wert;
	}
	
	public UmfeldDatenSensorWert getParameter(MeteoParameterType type) {
		return grenzWerte.get(type);
	}

	protected boolean isSendMessage() {
		return sendMessage;
	}

}
