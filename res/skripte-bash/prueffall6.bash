#!/bin/bash
source ../../../skripte-bash/einstellungen.sh

echo =================================================
echo =
echo =       Pruefungen SE4 - DUA, SWE 4.3 
echo =
echo =================================================
echo 

index=0
declare -a tests
declare -a testTexts

#########################
# Name der Applikation #
#########################
appname=pllogufd

########################
#     Testroutinen     #
########################
testTexts[$index]="Test des Moduls Anstieg-Abfall-Kontrolle. Der Test implementiert die Vorgaben aus dem Dokument [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25"
tests[$index]="testaufab.AnstiegAbfallKontrolleTest"
index=$(($index+1))

testTexts[$index]="Test des Moduls Ausfallüberwachung"
tests[$index]="testausfall.UFDAusfallUeberwachungTest"
index=$(($index+1))

testTexts[$index]="Test des Moduls Differenzialkontrolle. Der Test implementiert die Vorgaben aus dem Dokument [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 24"
tests[$index]="testdiff.UFDDifferenzialKontrolleTest"
index=$(($index+1))

testTexts[$index]="Überprüfung des Submoduls NiederschlagsArt aus der Komponente. Meteorologische Kontrolle. Diese Überprüfung richtet sich nach den Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.26"
tests[$index]="testmeteo.na.NiederschlagsArtTest"
index=$(($index+1))

testTexts[$index]="Überprüfung des Submoduls NiederschlagsIntensität aus der Komponente Meteorologische Kontrolle. Diese Überprüfung richtet sich nach den Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.27"
tests[$index]="testmeteo.ni.NiederschlagsIntensitaetTest"
index=$(($index+1))

testTexts[$index]="Überprüfung des Submoduls Sichtweite aus der Komponente Meteorologische Kontrolle. Diese Überprüfung richtet sich nach den Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.28"
tests[$index]="testmeteo.sw.SichtweiteTest"
index=$(($index+1))

testTexts[$index]="Überprüfung des Submoduls WasserFilmDicke aus der Komponente Meteorologische Kontrolle. Diese Überprüfung richtet sich nach den Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.27-28"
tests[$index]="testmeteo.wfd.WasserFilmDickeTest"
index=$(($index+1))

########################
#      ClassPath       #
########################
cp="../../de.bsvrz.sys.funclib.bitctrl/de.bsvrz.sys.funclib.bitctrl-runtime.jar"
cp=$cp":../de.bsvrz.dua."$appname"-runtime.jar"
cp=$cp":../de.bsvrz.dua."$appname"-test.jar"
cp=$cp":../../junit-4.1.jar"

########################
#     Ausfuehrung      #
########################

for ((i=0; i < ${#tests[@]}; i++));
do
	echo "================================================="
	echo "="
	echo "= Test Nr. "$(($i+1))":"
	echo "="
	echo "= "${testTexts[$i]}
	echo "="
	echo "================================================="
	echo 
	java -cp $cp $jvmArgs org.junit.runner.JUnitCore "de.bsvrz.dua."$appname"."${tests[$i]}
	sleep 5
done

exit
