/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Pl-Prüfung logisch UFD
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.pllogufd.
 * 
 * de.bsvrz.dua.pllogufd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.pllogufd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.pllogufd.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;

/**
 * Eine konkrete Messstelle für die Meteorologische Kontrolle
 *
 * @author Kappich Systemberatung
 */
public class MeteoMessstelle implements ClientReceiverInterface {

	/**
	 * Messstellen-Objekt
	 */
	private final DUAUmfeldDatenMessStelle _messStelle;


	/**
	 * Aktuelle Datenarten, die implausible Daten liefern
	 */
	private final Set<UmfeldDatenArt> _implausibleDatenArten = new LinkedHashSet<>();
	/**
	 * Aktuelle verletze Bedingungen (für die Ausgabe in der Betriebsmeldung)
	 */
	private final Set<String> _verletzteBedingungen = new LinkedHashSet<>();
	/**
	 * Aktuell vorliegende Betriebsmeldungs-IDs
	 */
	private final Set<String> _ids = new LinkedHashSet<>();

	private final MeteoWerte meteoWerte;
	


	/**
	 * Logger
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Template für Betriebsmeldungstext
	 */
	private final MessageTemplate MESSAGE_TEMPLATE = new MessageTemplate(MessageGrade.ERROR,
			MessageType.APPLICATION_DOMAIN, MessageTemplate.set("attr", ", ", "Messwert ", "Messwerte "),
			MessageTemplate.fixed(" bei meteorologischer Kontrolle an Messstelle "), MessageTemplate.object(),
			MessageTemplate.fixed(" auf fehlerhaft gesetzt, da "), MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". "), MessageTemplate.ids());

	/**
	 * Erstellt eine neue MeteoMessstelle
	 * 
	 * @param connection
	 *            Verbindung
	 * @param messStelle
	 *            Messstellen-Objekt
	 */
	public MeteoMessstelle(final ClientDavInterface connection, final DUAUmfeldDatenMessStelle messStelle) {
		_messStelle = messStelle;
		meteoWerte = new MeteoWerte(messStelle);

		DataModel dataModel = connection.getDataModel();
		String atgPid = "atg.ufdmsParameterMeteorologischeKontrolle";
		AttributeGroup attributeGroup = dataModel.getAttributeGroup(atgPid);
		Aspect aspect = dataModel.getAspect("asp.parameterSoll");

		if (attributeGroup == null) {
			_debug.fine("Es konnte keine Parameter-Attributgruppe für die " + "Meteorologische Kontrolle des Objektes "
					+ messStelle + " bestimmt werden\n" + "Atg-Name: " + atgPid);
			return;
		}

		connection.subscribeReceiver(this, messStelle.getObjekt(), new DataDescription(attributeGroup, aspect),
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}


	public Collection<ResultData> updateData(final ResultData resultData) {

		Collection<ResultData> resultList = new ArrayList<>();
		
		if( !meteoWerte.containsSensor(resultData.getObject())) {
			resultList.add(resultData);
			return resultList;
		}
		
		ResultData data;
		if (resultData.hasData()) {
			data = resultData;
		} else {
			data = null;
		}

		long timeStamp = resultData.getDataTime();
//		long meteoTime = meteoWerte.getDataTime();
//		if(( meteoTime > 0) && ( meteoTime < timeStamp)) {
//			resultList.addAll(pruefe(false));
//			meteoWerte.reset();
//		}
		resultList.addAll(meteoWerte.setData(resultData.getObject(), timeStamp, data));
		return resultList;
	}

//	public ResultData plausibilisiere(final ResultData resultData) { 
//		if (!resultData.hasData()) {
//			return resultData;
//		} else {
//			UmfeldDatenArt datenArt;
//			try {
//				datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(resultData.getObject());
//			} catch (UmfeldDatenSensorUnbekannteDatenartException e) {
//				return resultData;
//			}
//
//			pruefe();
//
//			if (_implausibleDatenArten.contains(datenArt)) {
//				UmfeldDatenSensorDatum umfeldDatenSensorDatum = new UmfeldDatenSensorDatum(resultData);
//				umfeldDatenSensorDatum.getWert().setFehlerhaftAn();
//				umfeldDatenSensorDatum.getDatum(); // Workaround fehlende
//													// Aktualisierung
//				umfeldDatenSensorDatum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
//				return umfeldDatenSensorDatum.getVeraendertesOriginalDatum();
//			}
//			return resultData;
//		}
//	}

//	private Collection<ResultData> pruefe(boolean checkRules) {
//
//		_verletzteBedingungen.clear();
//		_implausibleDatenArten.clear();
//		_ids.clear();
//
//		Collection<ResultData> resultList = meteoWerte.pruefe(_verletzteBedingungen, _implausibleDatenArten, _ids);
//		
//		if (!_ids.isEmpty()) {
//			OperatingMessage message = MESSAGE_TEMPLATE.newMessage(_messStelle.getObjekt());
//			for (String id : _ids) {
//				message.addId(id);
//			}
//			for (UmfeldDatenArt art : _implausibleDatenArten) {
//				message.add("attr", art.getName() + " " + art.getAbkuerzung());
//			}
//			for (String s : _verletzteBedingungen) {
//				message.add("values", s);
//			}
//			if (_sendMessage) {
//				message.send();
//			} else {
//				_debug.info(message.toString());
//			}
//		}
//
//		return resultList;
//	}

	@Override
	public void update(final ResultData[] results) {
		for (ResultData result : results) {
			if (result.hasData()) {
				Data data = result.getData();
				meteoWerte.updateGrenzwerte(data);
			}
		}
	}

}
