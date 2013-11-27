package com.silverwzw.gae.tools.pad_emulator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class AbstractDungeon {

	private static Pattern pitemv = Pattern.compile("\"pval\"\\s*?:\\s*?[1-9]+?");
	private static Pattern pitem = Pattern.compile("\"item\"\\s*?:\\s*?\"(\\d+?)\"");
	
	static Pattern p = Pattern.compile("\"hash\"\\s*:\\s*\"([0-9a-zA-Z]*)\"");

	@SuppressWarnings("serial")
	final static class HashNotFoundException extends RuntimeException {
		HashNotFoundException(){};
		HashNotFoundException(String s){super(s);};
		HashNotFoundException(Exception s){super(s);};
		HashNotFoundException(String s, Exception e){super(s,e);};
	};
	
	private String DungeonString;
	private Integer pval;
	private String hash;
	private Collection<Integer> eggs;
	
	AbstractDungeon() {
		DungeonString = null;
		pval = null;
		hash = null;
		eggs = null;
	}
	
	public abstract String getDungeonString();
	public abstract int getMode();
	public static String modDungeon(String dungeon, int mode, String hash) {
		String newDungeon;
		switch (mode) {
			case 1:
				newDungeon = "{\"res\":0,\"hash\":\"" + hash + "\",\"btype\":0,\"barg\":0,\"fp\":0,\"waves\":[{\"seq\":\"1\",\"monsters\":[{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":\"42\",\"inum\":1,\"pval\":0}]},{\"seq\":\"2\",\"monsters\":[{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":\"42\",\"inum\":1,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0}]},{\"seq\":\"3\",\"monsters\":[{\"type\":0,\"num\":\"58\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":1,\"num\":\"60\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"58\",\"lv\":1,\"item\":\"58\",\"inum\":1,\"pval\":0}]}]}";
				break;
			case 2:
				newDungeon = dungeon.replaceAll("(?<=\"num\"\\s?:\\s?\"\\d{3}\"\\s?,\\s?\"lv\"\\s?:\\s?)\"?\\d+\"?(?=,)", "1");
				break;
			case 3:
				newDungeon = "{\"res\":0,\"hash\":\"" + hash + "\",\"btype\":0,\"barg\":0,\"fp\":0,\"waves\":[{\"seq\":\"1\",\"monsters\":[{\"type\":0,\"num\":\"173\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":\"42\",\"inum\":1,\"pval\":0}]},{\"seq\":\"2\",\"monsters\":[{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":\"42\",\"inum\":1,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0}]},{\"seq\":\"3\",\"monsters\":[{\"type\":0,\"num\":\"58\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":1,\"num\":\"173\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"58\",\"lv\":1,\"item\":\"58\",\"inum\":1,\"pval\":0}]}]}";
				break;
			case 4:
				newDungeon = "{\"res\":0,\"hash\":\"" + hash + "\",\"btype\":0,\"barg\":0,\"fp\":0,\"waves\":[{\"seq\":\"1\",\"monsters\":[{\"type\":1,\"num\":\"" + PadEmulatorSettings.getShadowId() + "\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0}]}]}";
				break;
			default:
				newDungeon = dungeon;
		}
		return newDungeon;
	}
	public String moddedDungeon() {
		return modDungeon(getDungeonString(), getMode(), hash());
	}
	public synchronized String hash() {
		checkSync();
		if (hash == null) { 
			Matcher m = p.matcher(DungeonString);
			if (m.find()) {
				hash = m.group(1);
			} else {
				throw new HashNotFoundException();
			}
		}
		return hash;
	}
	public synchronized int pval() {
		checkSync();
		if (pval == null) {
			Matcher m;
			m = pitemv.matcher(DungeonString);
			pval = (Integer)0;
			while (m.find()) {
				pval++;
			}
		}
		return (int)pval;
	}
	public synchronized Collection<Integer> eggs() {
		checkSync();
		if (eggs == null) {
			LinkedList<Integer> ll;
			ll = new LinkedList<Integer>();
			Matcher m;
			m = pitem.matcher(DungeonString);
			while (m.find()) {
				if (!m.group(1).equals("9900")) {
					ll.add((Integer) Integer.parseInt(m.group(1)));
				}
			}
			eggs = ll;
		}
		return eggs;
	}
	private synchronized void checkSync() {
		if (DungeonString != null && DungeonString.equals(getDungeonString())) {
			return;
		}
		hash = null;
		eggs = null;
		pval = null;
		DungeonString = new String(getDungeonString());
	}
}