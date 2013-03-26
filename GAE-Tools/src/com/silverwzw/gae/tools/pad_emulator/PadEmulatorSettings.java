package com.silverwzw.gae.tools.pad_emulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

final public class PadEmulatorSettings {
	static Cache cache = null;
	private String pid;
	@SuppressWarnings("unused")
	private PadEmulatorSettings(){;}
	PadEmulatorSettings(String playerId) {
		pid = playerId;
	}
	private static Object get(String itemName, String settingPid) {
		String key;
		if (cache == null) {
			try {
				cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			} catch (CacheException e) {
				cache = null;
				return null;
			}
		}
		key = itemName;
		if (settingPid != null) {
			key += '-' + settingPid;
		}
		if (cache.containsKey(key)) {
			return cache.get(key);
		} else {
			return null;
		}	
	}
	public Object getSpec(String itemName) {
		return get(itemName, pid);
	}
	public static Object getGeneral(String itemName) {
		return get(itemName, null);
	}
	private static boolean set(String itemName, Object value, String settingPid) {
		if (cache == null) {
			try {
				cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			} catch (CacheException e) {
				cache = null;
				return false;
			}
		}
		String key = itemName;
		if (settingPid != null) {
			key += '-' + settingPid;
		}
		cache.put(key, value);
		return true;
	}
	public boolean setSpec(String itemName, Object value){
		return set(itemName,value,pid);
	}
	public static boolean setGeneral(String itemName, Object value){
		return set(itemName,value,null);
	}
	public boolean is(String itemName) {
		Boolean b = (Boolean)getSpec(itemName);
		if (b == null) {
			return false;
		} else {
			return (boolean)b;
		}
	}
	public boolean isBlockLevelUp() {
		return is("blockLevelUp");
	}
	public void setBlockLevelUp(boolean bool) {
		setSpec("blockLevelUp", bool);
	}
	public void setDungeonString(String dungeonStr) {
		setSpec("LastDungeonRecieved", dungeonStr);
	}
	public String getDungeonString() {
		return (String)getSpec("LastDungeonRecieved");
	}
	public boolean isLookingForCertainEgg() {
		return is("isLookingForCertainEgg");
	}
	public void setLookingForCertainEgg(boolean bool) {
		setSpec("isLookingForCertainEgg", bool);
	}
	@SuppressWarnings("unchecked")
	public void addWantedEgges(String ... eggIDs) {
		ArrayList<String> cacheCopy;
		cacheCopy = (ArrayList<String>)getSpec("wantedEggs");
		if (cacheCopy == null) {
			cacheCopy = new ArrayList<String>();
		}
		for (String egg : eggIDs) {
			if (egg != null) {
				cacheCopy.add(egg);
				setFreqEgg(egg);
			}
		}
		setSpec("wantedEggs", (Object)cacheCopy);
	}
	public void cleanWantedEggs() {
		setSpec("wantedEggs",(Object)new ArrayList<String>());
		setLookingForCertainEgg(false);
	}
	@SuppressWarnings("unchecked")
	public Collection<String> WantedEggs() {
		ArrayList<String> cacheCopy;
		cacheCopy = (ArrayList<String>)getSpec("wantedEggs");
		if (cacheCopy != null) {
			return cacheCopy;
		} else {
			return new ArrayList<String>();
		}
	}
	public void acquireSaveLock() {
		setSpec("notLocked", false);
	}
	public boolean isLocked() {
		return !is("notLocked");
	}
	public void releaseSaveLock() {
		setSpec("notLocked", true);
	}
	public void setDisableDungeonMod(boolean bool) {
		setSpec("disableDungeonMod", bool);
	}
	public boolean isDungeonModDisabled() {
		return is("disableDungeonMod");
	}
	public static Iterable<String> getFreqEggs() {
		if (getGeneral("freqEggs") == null) {
			setGeneral("freqEggs", new freqAccessEggs(16));
		}
		return ((freqAccessEggs)getGeneral("freqEggs")).collection();
	}
	public static boolean setFreqEgg(String egg) {
		freqAccessEggs fae;
		boolean ret;
		fae = (freqAccessEggs)getGeneral("freqEggs");
		if (fae == null) {
			fae = new freqAccessEggs(16);
		}
		ret = fae.mtf(egg);
		setGeneral("freqEggs",fae);
		return ret;
	}
	public static void resetFreqEgg() {
		freqAccessEggs fae;
		fae = new freqAccessEggs(16);
		fae.mtf("399");
		fae.mtf("234");
		fae.mtf("321");
		fae.mtf("251");
		fae.mtf("153");
		fae.mtf("227");
		fae.mtf("254");
		fae.mtf("178");
		fae.mtf("257");
		fae.mtf("260");
		fae.mtf("181");
		fae.mtf("303");
		fae.mtf("305");
		fae.mtf("307");
		setGeneral("freqEggs",fae);
	}
}

@SuppressWarnings("serial")
final class freqAccessEggs implements java.io.Serializable {
	private int capacity;
	private LinkedList<String> list;
	freqAccessEggs(int c) {
		if (c < 1) {
			c = 1;
		} else if (c > 64) {
			c = 64;
		}
		capacity = c;
		list = new LinkedList<String>();
	}
	boolean mtf(String eggnumber) {
		boolean ret;
		ret = list.remove(eggnumber);
		list.addFirst(eggnumber);
		if (list.size() > capacity) {
			list.removeLast();
		}
		return ret;
	}
	Iterable<String> collection() {
		return (Iterable<String>)list;
	}
}
