package com.silverwzw.gae.tools.pad_emulator;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bonus {
	
	final public static String getCurrentDateString(){
		Calendar today = Calendar.getInstance();
		return format(today.get(Calendar.YEAR))+format(today.get(Calendar.MONTH)+1)+format(today.get(Calendar.DAY_OF_MONTH));
	}
	final private static String format(int i){
		i %= 100;
		return (i<10)?("0" + i):("" + i);
	}
	final private static String getEST(String PST) {
		int h;
		h = (Integer.parseInt(PST) + 2) % 24;
		return format(h);
	}
	final static void saveData(String pid, String bonus) {
		PadEmulatorSettings settings;
		String b;
		b = "[";
		settings = new PadEmulatorSettings(pid);
		Matcher m = Pattern.compile("\"edatepd\":\""+getCurrentDateString()+"(\\d{2})(\\d{2})\\d{2}\",\"dung\":12([2-6])").matcher(bonus);
		if (m.find()){
			while(true) {
				b += "{\"t\":\""+ getEST(m.group(1)) + ":" + m.group(2) + "\",\"d\":" + m.group(3) + "}";
				if (m.find()) {
					b += ",";
				} else {
					break;
				}
			}
		}
		b += "]";
		settings.setBonus(b);
	}
}
