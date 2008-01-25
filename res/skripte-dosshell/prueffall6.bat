@echo off

call ..\..\..\skripte-dosshell\einstellungen.bat

set cp=..\..\de.bsvrz.sys.funclib.bitctrl\de.bsvrz.sys.funclib.bitctrl-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.plformal-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.pllogufd-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.pllogufd-test.jar
set cp=%cp%;..\..\junit-4.1.jar

title Pruefungen SE4 - DUA, SWE 4.3

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.3
echo #
echo #  Test des Moduls Anstieg-Abfall-Kontrolle
echo #  Der Test implementiert die Vorgaben aus dem
echo  # Dokument [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.pllogufd.testaufab.AnstiegAbfallKontrolleTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.3
echo #
echo # Nicht vorgeschriebener Test. Provoziert das Senden von Daten
echo # durch die Ausfallkontrolle
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.pllogufd.testausfall.GrobTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.3
echo #
echo # Test des Moduls Ausfallüberwachung
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.pllogufd.testausfall.UFDAusfallUeberwachungTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.3
echo #
echo #  Test des Moduls Differenzialkontrolle
echo #  Der Test implementiert die Vorgaben aus dem Dokument
echo # [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 24
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.pllogufd.testdiff.UFDDifferenzialKontrolleTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.3
echo #
echo #  Überprüfung des Submoduls NiederschlagsArt aus der Komponente
echo #  Meteorologische Kontrolle. Diese Überprüfung richtet sich nach den
echo #  Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.26
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.pllogufd.testmeteo.na.NiederschlagsArtTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.3
echo #
echo #  Überprüfung des Submoduls NiederschlagsIntensität aus der Komponente
echo #  Meteorologische Kontrolle. Diese Überprüfung richtet sich nach den 
echo #  Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.27
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.pllogufd.testmeteo.ni.NiederschlagsIntensitaetTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.3
echo #
echo #  Überprüfung des Submoduls Sichtweite aus der Komponente 
echo #  Meteorologische Kontrolle. Diese Überprüfung richtet sich nach den
echo #  Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.28
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.pllogufd.testmeteo.sw.SichtweiteTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.3
echo #
echo #  Überprüfung des Submoduls WasserFilmDicke aus der Komponente
echo #  Meteorologische Kontrolle. Diese Überprüfung richtet sich nach den 
echo #  Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.27-28
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.pllogufd.testmeteo.wfd.WasserFilmDickeTest
pause