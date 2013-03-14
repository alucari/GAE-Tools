package com.silverwzw.gae.tools.pad_emulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
	public Object get(String itemName) {
		String key;
		if (cache == null) {
			try {
				cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			} catch (CacheException e) {
				cache = null;
				return null;
			}
		}
		key = itemName + '-' + pid;
		if (cache.containsKey(key)) {
			return cache.get(key);
		} else {
			return null;
		}	
	}
	public boolean set(String itemName, Object value) {
		if (cache == null) {
			try {
				cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			} catch (CacheException e) {
				cache = null;
				return false;
			}
		}
		String key = itemName + '-' + pid;
		cache.put(key, value);
		return true;
	}
	public boolean is(String itemName) {
		Boolean b = (Boolean)get(itemName);
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
		set("blockLevelUp", bool);
	}
	public void setDungeonString(String dungeonStr) {
		set("LastDungeonRecieved", dungeonStr);
	}
	public String getDungeonString() {
		return (String)get("LastDungeonRecieved");
	}
	public boolean isLookingForCertainEgg() {
		return is("isLookingForCertainEgg");
	}
	public void setLookingForCertainEgg(boolean bool) {
		set("isLookingForCertainEgg", bool);
	}
	@SuppressWarnings("unchecked")
	public void addWantedEgges(String ... eggIDs) {
		ArrayList<String> cacheCopy;
		cacheCopy = (ArrayList<String>)get("wantedEggs");
		if (cacheCopy == null) {
			cacheCopy = new ArrayList<String>();
		}
		for (String egg : eggIDs) {
			cacheCopy.add(egg);
		}
		set("wantedEggs", (Object)cacheCopy);
	}
	public void cleanWantedEggs() {
		set("wantedEggs",(Object)new ArrayList<String>());
		setLookingForCertainEgg(false);
	}
	@SuppressWarnings("unchecked")
	public Collection<String> WantedEggs() {
		ArrayList<String> cacheCopy;
		cacheCopy = (ArrayList<String>)get("wantedEggs");
		if (cacheCopy != null) {
			return cacheCopy;
		} else {
			return new ArrayList<String>();
		}
	}
	public void acquireSaveLock() {
		set("notLocked", false);
	}
	public boolean isLocked() {
		return !is("notLocked");
	}
	public void releaseSaveLock() {
		set("notLocked", true);
	}
}
