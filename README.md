[![Build Status](https://travis-ci.org/bitctrl/de.bsvrz.dua.pllogufd.svg?branch=master)](https://travis-ci.org/bitctrl/de.bsvrz.dua.pllogufd)
[![Build Status](https://api.bintray.com/packages/bitctrl/maven/de.bsvrz.dua.pllogufd/images/download.svg)](https://bintray.com/bitctrl/maven/de.bsvrz.dua.pllogufd)

# Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD

Version: ${version}

 **Alle Änderungen wurden in die offizielle SWE des NERZ übernommen ->**_https://gitlab.nerz-ev.de/ERZ/SWE_de.bsvrz.dua.pllogufd_**. Dieses Repo wird archiviert!**

## Übersicht

Aufgabe der SWE PL-Prüfung logisch UFD besteht in der Durchführung der logischen
Plausibilitätsprüfungen der von Umfelddatensensoren gelieferten Umfelddaten. Dazu
wird eine Reihe von einzelnen Prüfungen durchgeführt. Dies sind
-	Ausfallüberwachung von Messwerten,
-	Wertebereichsprüfung,
-	Differentialkontrolle von Werten,
-	Anstieg-Abstieg-Kontrolle und
-	Meteorologische Kontrolle.
Die genauen Prüfalgorithmen sind in den Anwenderforderungen aufgeführt. Nach der
Prüfung werden die Daten ggf. unter einem parametrierbaren Aspekt publiziert.


## Versionsgeschichte

### Version 2.0.3

- Applikationsname für MessageSender entsprechend NERZ-Vorgabe gesetzt
- Für die Datenarten Niederschlagsintensität, Niederschlagsart, Fahrbahnzustand, 
  Sichtweite, Restsalzgehalt, Gefriertemperatur und Windrichtung wird gemäß 
  Anforderungen keine Anstiegs-Abstiegs-Plausibilisierung mehr durchgeführt
- Sourcecode-Bereinigung

- NERZ-Fehler 226: Sonderbehandlung der Restsalz-Wertes 255 (physikalisch nicht messbar)
  bei der Anstiegs-Abstiegs-Kontrolle
- NERZ-Fehler 231,232: Für die Auswertung der Regeln der meteorologischen Kontrolle werden nur Werte
  mit identischem Zeitstempel verwendet (Testfall ergänzt)
- NERZ-Fehler 235: Über den Kommandozeilenparameter -ignoriereRegeln=<n>,<n>,... können die Regeln der
  Meteorologischen Kontrolle global ausgeschlossen werden
- NERZ-ÄA 130: Über den Kommandozeilenparameter -initialeAusfallKontrolle=true/false erfolgt die Ausfallüberwachung 
  auf Wunsch auch, wenn initial keine Daten von einem Sensor geliefert werden

### 2.0.2

Release-Datum: 28.07.2016

de.bsvrz.dua.pllogufd.grenz.UniversalAtgUfdsGrenzwerte.java
de.bsvrz.dua.pllogufd.testaufab.UniversalAtgUfdsAnstiegAbstiegKontrolle
de.bsvrz.dua.pllogufd.testdiff.UniversalAtgUfdsDifferenzialKontrolle
- die Klassen erweitert nicht mehr de.bsvrz.sys.funclib.bitctrl.dua.AllgemeinerDatenContainer
- equals und hashCode werden nicht implementiert

de.bsvrz.dua.pllogufd.tests.DuAPlLogUfdTestBase
- der Member "_pruefungLogischUFD" sollte nicht statisch sein, der er bei jedem Test neu initialisiert wird

- Obsolete SVN-Tags aus Kommentaren entfernt
- Obsolete inheritDoc-Kommentare entfernt

### 2.0.1

Release-Datum: 22.07.2016

  - Umpacketierung gemäß NERZ-Konvention
  
### 2.0.0

Release-Datum: 31.05.2016

#### Neue Abhängigkeiten

Die SWE benötigt nun das Distributionspaket de.bsvrz.sys.funclib.bitctrl.dua
in Mindestversion 1.5.0 und de.bsvrz.sys.funclib.bitctrl in Mindestversion 1.4.0,
sowie die Kernsoftware in Mindestversion 3.8.0.

#### Datenmodelländerungen

Folgende Änderungen an Konfigurationsbereichen wurden durchgeführt:

##### kb.tmUmfeldDatenGlobal Version 25

– Neue Parameterattributgruppen für die Grenzwertprüfung UFD
  atg.ufdsGrenzwerte<Umfelddatenart>.
– Erweiterung der bestehenden Parameterattributgruppen für die Klassifizierung
  der Niederschlagsart, Niederschlagsintensität und Wasserfilmdicke um
  Attributlisten, um die Parameter abhängig von der NI-WFD-Klasse festlegen
  zu können.
– Ergänzung eines neuen Parameters für die Meteorologische Kontrolle an der
  Umfelddatenmesstelle: atg.ufdmsParameterMeteorologischeKontrolle.
– Die Online-Datensätze der Sensordaten wurden um Attribute für die logische
  Grenzwertprüfung ergänzt. (Achtung: Hierdurch kann es beispielsweise beim
  Zugriff auf Archivdaten zu Problemen kommen!)

#### Änderungen

Folgende Änderungen gegenüber vorhergehenden Versionen wurden durchgeführt:

- Überarbeitung der erzeugten Betriebsmeldungen gemäß neuen Anwenderforderungen.
- Eine Grenzwertprüfung wurde ergänzt, mit denen die Attribute logisch auf Maximalwerte
  (und zusätzlich Minimalwerte für Temperaturen) geprüft werden können.
- Die meteorologische Kontrolle wurde komplett ersetzt durch ein neues Modul mit
  neuen Prüfungen.
- Die Differenzialkontrolle der Fahrbahnoberflächentemperatur wird nicht mehr
  durchgeführt, falls Niederschlagsart=Schnee.
- Die SWE ermittelt jetzt die Messstelle die zu einem Sensor gehört, und erzeugt
  bei einem Sensor ohne zugehörige Messstelle eine Warnung.
- Die SWE erzeugt bei unbekannten Sensortypen jetzt eine Betriebsmeldung.
- Folgende Sensorarten werden nicht mehr verarbeitet: Luftdruck, Fahrbahnglätte,
  Fahrbahnfeuchte, Niederschlagsmenge, Schneehöhe, Temperatur in Tiefe 2.

#### Fehlerkorrekturen

Folgende Fehler gegenüber vorhergehenden Versionen wurden korrigiert:

- Verschiedene Betriebsmeldungen und Prüfungen verwenden jetzt korrekt skalierte
  Werte, bisher wurden teilweise falsche, unskalierte Werte in Meldungen ausgegeben,
  wodurch beispielsweise Temperaturabweichungen mit Faktor 10 berechnet
  und in Betriebsmeldungen ausgegeben wurden.


### 1.7.0
- Umstellung auf Java 8 und UTF-8

### 1.6.1
- Kompatibilität zu DuA-2.0 hergestellt

### 1.6.0
- Umstellung auf Funclib-Bitctrl-Dua

### 1.5.0
- Unterstützung für die Umfelddatenarten ZeitreserveGlätte (Vaisala) und Taustoffmenge je Quadratmeter (TLS2012)
- Neues Kommandozeilenargument -fehlerhafteWertePublizieren, mit dem das Defaultverhalten, implausible Werte
  durch 'fehlerhaft' zu ersetzen, überschrieben werden kann.
- Berechnung der Ausfallzeit UFD korrigiert (war zu groß)
- Neues Kommandozeilenargument -meteorologischeKontrolle=[default,hs,aus], um die meteorologische
  Kontrolle auf die Hauptsensoren zu beschränken oder gänzlich zu deaktivieren.

### 1.4.0
- Umstellung auf Maven-Build
- Behandlung nicht unterstützter Sensorarten über die 'UmfeldDatenSensorUnbekannteDatenartException'
- benötigt SWE_de.bsvrz.sys.funclib.bitctrl_FREI_V1.2.3.zip oder höher 


### 1.3.3

  - Bei unbekannten Umfelddatenarten wird die neue Exception 'UmfeldDatenSensorUnbekannteDatenartException' 
  	derart behandelt, dass eine Warnung ausgegeben und der betroffene Sensor ignoriert wird
  	
  - benötigt: de.bsvrz.sys.funclib.bitctrl_V20140612

### 1.3.2

  - Sämtliche Konstruktoren DataDescription(atg, asp, sim)
    ersetzt durch DataDescription(atg, asp)

### 1.3.1

  - Debug: Probleme beim Betrieb von Messstellen mit fehlenden Umfelddatensensoren
    beseitigt.
    
### 1.3.0

  - Debug: Probleme beim Auslesen der Parameter fuer die Differentialkontrolle
    beseitigt.

### 1.2.1

  - Bash-Startskript hinzu
  
### 1.2.0

  - Debug: Gueteinitialisierung war fehlerhaft wurde veraendert.
    Code wurde verschlankt.

### 1.1.0

  - Umpacketierung

### 1.0.0

  - Erste Auslieferung
  


## Bemerkungen

### Tests:

Die automatischen Tests, die in Zusammenhang mit der Prüfspezifikation durchgeführt
werden, sind analog der Package-Struktur der SWE selbst definiert. Die genaue
Durchführung der Tests dieser SWE ist im Dokument PrDok_SWE4.3_LosC1C2_VRZ3.doc
(innerhalb von ...extra-src.zip) beschrieben.
	

### Logging-Hierarchie (Wann wird welche Art von Logging-Meldung produziert?):

####	ERROR:
- DUAInitialisierungsException --> Beendigung der Applikation
- Fehler beim An- oder Abmelden von Daten beim Datenverteiler
- Interne unerwartete Fehler
	
####	WARNING:
- Fehler, die die Funktionalität grundsätzlich nicht
	  beeinträchtigen, aber zum Datenverlust führen können
- Nicht identifizierbare Konfigurationsbereiche
- Probleme beim Explorieren von Attributpfaden 
	  (von Plausibilisierungsbeschreibungen)
- Wenn mehrere Objekte eines Typs vorliegen, von dem
	  nur eine Instanz erwartet wird
- Wenn Parameter nicht korrekt ausgelesen werden konnten
	  bzw. nicht interpretierbar sind
- Wenn inkompatible Parameter übergeben wurden
- Wenn Parameter unvollständig sind
- Wenn ein Wert bzw. Status nicht gesetzt werden konnte
	
####	INFO:
- Wenn neue Parameter empfangen wurden
	
####	CONFIG:
- Allgemeine Ausgaben, welche die Konfiguration betreffen
- Benutzte Konfigurationsbereiche der Applikation bzw.
	  einzelner Funktionen innerhalb der Applikation
- Benutzte Objekte für Parametersteuerung von Applikationen
	  (z.B. die Instanz der Datenflusssteuerung, die verwendet wird)
- An- und Abmeldungen von Daten beim Datenverteiler
	
####	FINE:
- Wenn Daten empfangen wurden, die nicht weiterverarbeitet 
	  (plausibilisiert) werden können (weil keine Parameter vorliegen)
- Informationen, die nur zum Debugging interessant sind 


## Disclaimer

Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
Copyright (C) 2007 BitCtrl Systems GmbH 

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51
Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.


## Kontakt

BitCtrl Systems GmbH
Weißenfelser Straße 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
