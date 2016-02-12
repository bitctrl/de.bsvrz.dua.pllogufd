*************************************************************************************
*  Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD  *
*************************************************************************************

Version: ${version}

Übersicht
=========

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


Versionsgeschichte
==================

1.5.0
- Unterstützung für die Umfalddatenarten ZeitreserveGlätte (Vaisala) und Taustoffmenge je Quadratmeter (TLS2012)
- Neues Kommandozeilenargument -fehlerhafteWertePublizieren, mit dem das Defaultverhalten, implausible Werte
  durch 'fehlerhaft' zu ersetzen, überschrieben werden kann.
- Berechnung der Ausfallzeit UFD korrigiert (war zu groß)
- Neues Kommandozeilenargument -meteorologischeKontrolle=[default,hs,aus], um die meteorologische
  Kontrolle auf die Hauptsensoren zu beschränken oder gänzlich zu deaktivieren.

1.4.0
- Umstellung auf Maven-Build
- Behandlung nicht unterstützter Sensorarten über die 'UmfeldDatenSensorUnbekannteDatenartException'
- benötigt SWE_de.bsvrz.sys.funclib.bitctrl_FREI_V1.2.3.zip oder höher 


1.3.3

  - Bei unbekannten Umfelddatenarten wird die neue Exception 'UmfeldDatenSensorUnbekannteDatenartException' 
  	derart behandelt, dass eine Warnung ausgegeben und der betroffene Sensor ignoriert wird
  	
  - benötigt: de.bsvrz.sys.funclib.bitctrl_V20140612

1.3.2

  - Sämtliche Konstruktoren DataDescription(atg, asp, sim)
    ersetzt durch DataDescription(atg, asp)

1.3.1

  - Debug: Probleme beim Betrieb von Messstellen mit fehlenden Umfelddatensensoren
    beseitigt.
    
1.3.0

  - Debug: Probleme beim Auslesen der Parameter fuer die Differentialkontrolle
    beseitigt.

1.2.1

  - Bash-Startskript hinzu
  
1.2.0

  - Debug: Gueteinitialisierung war fehlerhaft wurde veraendert.
    Code wurde verschlankt.

1.1.0

  - Umpacketierung

1.0.0

  - Erste Auslieferung
  


Bemerkungen
===========

- Tests:

	Die automatischen Tests, die in Zusammenhang mit der Prüfspezifikation durchgeführt
	werden, sind analog der Package-Struktur der SWE selbst definiert. Die genaue
	Durchführung der Tests dieser SWE ist im Dokument PrDok_SWE4.3_LosC1C2_VRZ3.doc
	(innerhalb von ...extra-src.zip) beschrieben.
	

- Logging-Hierarchie (Wann wird welche Art von Logging-Meldung produziert?):

	ERROR:
	- DUAInitialisierungsException --> Beendigung der Applikation
	- Fehler beim An- oder Abmelden von Daten beim Datenverteiler
	- Interne unerwartete Fehler
	
	WARNING:
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
	
	INFO:
	- Wenn neue Parameter empfangen wurden
	
	CONFIG:
	- Allgemeine Ausgaben, welche die Konfiguration betreffen
	- Benutzte Konfigurationsbereiche der Applikation bzw.
	  einzelner Funktionen innerhalb der Applikation
	- Benutzte Objekte für Parametersteuerung von Applikationen
	  (z.B. die Instanz der Datenflusssteuerung, die verwendet wird)
	- An- und Abmeldungen von Daten beim Datenverteiler
	
	FINE:
	- Wenn Daten empfangen wurden, die nicht weiterverarbeitet 
	  (plausibilisiert) werden können (weil keine Parameter vorliegen)
	- Informationen, die nur zum Debugging interessant sind 


Disclaimer
==========

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


Kontakt
=======

BitCtrl Systems GmbH
Weißenfelser Straße 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
