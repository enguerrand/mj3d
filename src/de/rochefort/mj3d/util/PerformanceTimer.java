package de.rochefort.mj3d.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceTimer {
	private static Map<String, Long> times = new HashMap<String, Long>();
	private static Long startTime;
	private static Long lastStoppedTime;
	private static final boolean ACTIVE = true;
	private static final boolean INTERIM_TIMES_ACTIVE = false;
	public PerformanceTimer() {
	}

	public static void start(){
		if(!ACTIVE) return;
		startTime = System.currentTimeMillis();
		lastStoppedTime = System.currentTimeMillis();
		times.clear();
	}
	
	public static void stopInterimTime(String refName){
		if(!INTERIM_TIMES_ACTIVE) return;
		long now = System.currentTimeMillis();
		long delta = now - lastStoppedTime;
		if(times.containsKey(refName)){
			delta += times.get(refName);
		}
		times.put(refName, delta);
		lastStoppedTime = now;
	}
	
	public static void stopAndPrintReport(){
		if(!ACTIVE) return;
		long now = System.currentTimeMillis();
		long delta = now - startTime;
		System.out.println("== Total Time used: "+delta+" ms ==");
		List<String> keyset = new ArrayList<String>(times.keySet());
		Collections.sort(keyset, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if(times.get(o1) > times.get(o2))
					return -1;
				if(times.get(o1) < times.get(o2))
					return 1;
				return 0;
			}
		});
		if(!INTERIM_TIMES_ACTIVE) return;
		for(String ref : keyset){
			long timeUsed = times.get(ref);
			System.out.println("  -> "+ref+": "+timeUsed+" ms");
			delta -=timeUsed;
		}
		System.out.println("  -> Time not recorded: "+delta+" ms");
		System.out.println("");
	}
}
