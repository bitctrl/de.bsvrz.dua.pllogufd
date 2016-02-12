*************************************************************************************
*  Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.3 Pl-Pr�fung logisch UFD  *
*************************************************************************************

Version: ${version}

�bersicht
=========

Aufgabe der SWE PL-Pr�fung logisch UFD besteht in der Durchf�hrung der logischen
Plausibilit�tspr�fungen der von Umfelddatensensoren gelieferten Umfelddaten. Dazu
wird eine Reihe von einzelnen Pr�fungen durchgef�hrt. Dies sind
-	Ausfall�berwachung von Messwerten,
-	Wertebereichspr�fung,
-	Differentialkontrolle von Werten,
-	Anstieg-Abstieg-Kontrolle und
-	Meteorologische Kontrolle.
Die genauen Pr�falgorithmen sind in den Anwenderforderungen aufgef�hrt. Nach der
Pr�fung werden die Daten ggf. unter einem parametrierbaren Aspekt publiziert.


Versionsgeschichte
==================

1.5.0
- Unterst�tzung f�r die Umfalddatenarten ZeitreserveGl�tte (Vaisala) und Taustoffmenge je Quadratmeter (TLS2012)
- Neues Kommandozeilenargument -fehlerhafteWertePublizieren, mit dem das Defaultverhalten, implausible Werte
  durch 'fehlerhaft' zu ersetzen, �berschrieben werden kann.
- Berechnung der Ausfallzeit UFD korrigiert (war zu gro�)
- Neues Kommandozeilenargument -meteorologischeKontrolle=[default,hs,aus], um die meteorologische
  Kontrolle auf die Hauptsensoren zu beschr�nken oder g�nzlich zu deaktivieren.

1.4.0
- Umstellung auf Maven-Build
- Behandlung nicht unterst�tzter Sensorarten �ber die 'UmfeldDatenSensorUnbekannteDatenartException'
- ben�tigt SWE_de.bsvrz.sys.funclib.bitctrl_FREI_V1.2.3.zip oder h�her 


1.3.3

  - Bei unbekannten Umfelddatenarten wird die neue Exception 'UmfeldDatenSensorUnbekannteDatenartException' 
  	derart behandelt, dass eine Warnung ausgegeben und der betroffene Sensor ignoriert wird
  	
  - ben�tigt: de.bsvrz.sys.funclib.bitctrl_V20140612

1.3.2

  - S�mtliche Konstruktoren DataDescription(atg, asp, sim)
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

	Die automatischen Tests, die in Zusammenhang mit der Pr�fspezifikation durchgef�hrt
	werden, sind analog der Package-Struktur der SWE selbst definiert. Die genaue
	Durchf�hrung der Tests dieser SWE ist im Dokument PrDok_SWE4.3_LosC1C2_VRZ3.doc
	(innerhalb von ...extra-src.zip) beschrieben.
	

- Logging-Hierarchie (Wann wird welche Art von Logging-Meldung produziert?):

	ERROR:
	- DUAInitialisierungsException --> Beendigung der Applikation
	- Fehler beim An- oder Abmelden von Daten beim Datenverteiler
	- Interne unerwartete Fehler
	
	WARNING:
	- Fehler, die die Funktionalit�t grunds�tzlich nicht
	  beeintr�chtigen, aber zum Datenverlust f�hren k�nnen
	- Nicht identifizierbare Konfigurationsbereiche
	- Probleme beim Explorieren von Attributpfaden 
	  (von Plausibilisierungsbeschreibungen)
	- Wenn mehrere Objekte eines Typs vorliegen, von dem
	  nur eine Instanz erwartet wird
	- Wenn Parameter nicht korrekt ausgelesen werden konnten
	  bzw. nicht interpretierbar sind
	- Wenn inkompatible Parameter �bergeben wurden
	- Wenn Parameter unvollst�ndig sind
	- Wenn ein Wert bzw. Status nicht gesetzt werden konnte
	
	INFO:
	- Wenn neue Parameter empfangen wurden
	
	CONFIG:
	- Allgemeine Ausgaben, welche die Konfiguration betreffen
	- Benutzte Konfigurationsbereiche der Applikation bzw.
	  einzelner Funktionen innerhalb der Applikation
	- Benutzte Objekte f�r Parametersteuerung von Applikationen
	  (z.B. die Instanz der Datenflusssteuerung, die verwendet wird)
	- An- und Abmeldungen von Daten beim Datenverteiler
	
	FINE:
	- Wenn Daten empfangen wurden, die nicht weiterverarbeitet 
	  (plausibilisiert) werden k�nnen (weil keine Parameter vorliegen)
	- Informationen, die nur zum Debugging interessant sind 


Disclaimer
==========

Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.3 Pl-Pr�fung logisch UFD
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
Wei�enfelser Stra�e 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
