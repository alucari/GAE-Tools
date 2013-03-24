package com.silverwzw.gae.tools.pad_emulator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


final public class Dungeon {
	public String DungeonString;
	static Pattern p = Pattern.compile("\"hash\"\\s*:\"([0-9a-zA-Z]*)\"");
	public Dungeon(String dungeonString) {
		DungeonString = dungeonString;
	}
	public String modDungeon(int mode) {
		String newDungeon;
		if (mode == 1) {
			try {
				newDungeon = "{\"res\":0,\"hash\":\"" + hash() + "\",\"btype\":0,\"barg\":0,\"fp\":0,\"waves\":[{\"seq\":\"1\",\"monsters\":[{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":\"42\",\"inum\":1,\"pval\":0}]},{\"seq\":\"2\",\"monsters\":[{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":\"42\",\"inum\":1,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"42\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0}]},{\"seq\":\"3\",\"monsters\":[{\"type\":0,\"num\":\"58\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":1,\"num\":\"60\",\"lv\":1,\"item\":0,\"inum\":0,\"pval\":0},{\"type\":0,\"num\":\"58\",\"lv\":1,\"item\":\"58\",\"inum\":1,\"pval\":0}]}]}";
			} catch (HashNotFoundException e) {
				newDungeon = DungeonString;
				throw e;
			}
		} else if (mode == 2 ){
			newDungeon = DungeonString.replaceAll("(?<=\"num\"\\s?:\\s?\"\\d{3}\"\\s?,\\s?\"lv\"\\s?:\\s?)\"?\\d+\"?(?=,)", "0");
		} else {
			return DungeonString;
		}

		return newDungeon;
	}
	public String hash() throws HashNotFoundException {
		Matcher m = p.matcher(DungeonString);
		if (m.find()) {
			return m.group(1);
		} else {
			throw new HashNotFoundException();
		}
	}
}

@SuppressWarnings("serial")
final class HashNotFoundException extends RuntimeException {}