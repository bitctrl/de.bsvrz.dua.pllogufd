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

package de.bsvrz.dua.pllogufd.clock;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Diese Klasse verwendet eine {@link WaitableClock} und erlaubt es, beliebige {@link Runnable}s zu beliebigen Zeitpunkten zu planen, sodass diese zu
 * den angegebenen Terminen ausgeführt werden. Diese Klasse ist daher ähnlich zu einem {@link java.util.concurrent.ScheduledExecutorService},
 * unterstützt aber (derzeit) nur Runnables und und verwendet eine WaitableClock als Zeitquelle. 
 *
 * @author Kappich Systemberatung
 */
public class ClockScheduler {

	/**
	 * Interner Timer
	 */
	private final ClockTimer _clockTimer;

	/**
	 * Map mit Zeitpunkt -> Zu diesem Zeitpunkt geplante Aufgaben
	 */
	private final NavigableMap<Instant, List<Runnable>> _scheduledTasks = new TreeMap<>();

	/**
	 * Wurde die Klasse terminiert?
	 */
	private boolean _terminated = false;

	/** 
	 * Erstellt einen neuen ClockScheduler
	 * @param clock Uhrzeitgeber
	 */
	public ClockScheduler(WaitableClock clock) {
		_clockTimer = new ClockTimer(clock) {
			@Override
			public void run(final Instant targetTime) {
				List<Runnable> list;
				synchronized(_scheduledTasks) {
					list = _scheduledTasks.remove(targetTime);
					if(list == null) return;
					if(!_scheduledTasks.isEmpty()) {
						_clockTimer.setTargetTime(_scheduledTasks.firstKey());
					}
				}
				for(Runnable runnable : list) {
					runnable.run();
				}
			}
		};
	}

	/**
	 * Plant eine Ausführung
	 * @param time Zeitpunkt
	 * @param task Aufgabe
	 */
	public void schedule(Instant time, Runnable task) {
		synchronized(_scheduledTasks) {
			if(_terminated){
				throw new IllegalStateException("ClockScheduler wurde terminiert");
			}
			List<Runnable> list = _scheduledTasks.get(time);
			if(list == null){
				list = new ArrayList<>();
				_scheduledTasks.put(time, list);
			}
			list.add(task);
			_clockTimer.setTargetTime(_scheduledTasks.firstKey());
		}
	}

	/**
	 * Halt den Thread an und löscht alle geplanten Aufträge
	 */
	public void terminate() {
		synchronized(_scheduledTasks) {
			_clockTimer.terminate();
			_terminated = true;
			_scheduledTasks.clear();
		}
	}

	/** 
	 * Gibt <tt>true</tt> zurück, wenn der Scheduler terminiert wurde
	 * @return <tt>true</tt>, wenn der Scheduler terminiert wurde, sonst <tt>false</tt>
	 */
	public boolean isTerminated() {
		synchronized(_scheduledTasks) {
			return _terminated;
		}
	}
}
