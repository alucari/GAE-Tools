package com.silverwzw.gae.tools.pad_emulator;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.servlet.ActionHandler;
import com.google.appengine.api.datastore.DatastoreService;


final public class Statistics implements ActionHandler {
	final static DatastoreService ds;
	private boolean readOnly;
	static {
		ds = com.google.appengine.api.datastore.DatastoreServiceFactory.getDatastoreService();
	}
	final static private class EggEntryComparator implements Comparator<Entry<Integer, Integer>>{
		public int compare(Entry<Integer, Integer> lhs, Entry<Integer, Integer> rhs) {
			long r;
			r = lhs.getValue() - rhs.getValue();
			if (r == 0) {
				assert lhs.getKey() != rhs.getKey() : "Two Egg Entry has same key and value";
				return (lhs.getKey() > rhs.getKey()) ? 1:-1;
			}
			return r>0?1:-1;
		}
	}
	final public static class Floor {
		public int occurance;
		public HashMap<Integer, Integer> egg;
		public int dung;
		public int floor;
		public int plus;
		public Floor(int d,int f) {
			dung = d;
			floor = f;
			plus = 0;
			egg = new HashMap<Integer,Integer>();
		}
		public String toString() {
			String res;
			Iterator<Entry<Integer,Integer>> iter;
			res = "{\"dung\":" + dung + ",\"name\":\"" + PadEmulatorSettings.StatisticFunction.dungeonName(dung) + "\",\"floor\":" + floor + ",\"occurance\":" + occurance + ",\"egg\":[";
			
			iter = egg.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Integer,Integer> e;
				e = iter.next();
				res += e.getKey() + ":" + e.getValue();
				if (iter.hasNext()) {
					res += ','; 
				}
			}
			return res + "]}";
		}
	}
	public Statistics(boolean readOnly) {
		this.readOnly = readOnly;
	}
	final public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PrintWriter o = resp.getWriter();
		int currentQueryingDungeon = -1;
		String currentQueryingEgg = "";
		String queryResultString = "";
		Iterable<Floor> queryResult = null;
		
		
		if (req.getParameter("do") != null) { 
			if (req.getParameter("do").equals("setname")) {
				PadEmulatorSettings.StatisticFunction.dungeonName(Integer.parseInt(req.getParameter("dung")), PadEmulatorSettings.utf82iso8859_1(req.getParameter("name")));
			} else if (req.getParameter("do").equals("dung")) {
				currentQueryingDungeon = Integer.parseInt(req.getParameter("dung"));
				queryResult = PadEmulatorSettings.StatisticFunction.queryDungeon(currentQueryingDungeon);
			} else if (req.getParameter("do").equals("egg")) {
				currentQueryingEgg = req.getParameter("egg");
				int eggID;
				if (currentQueryingEgg.equals("+")) {
					eggID = -1;
				} else {
					eggID = Integer.parseInt(currentQueryingEgg);
				}
				queryResult = PadEmulatorSettings.StatisticFunction.queryEgg(eggID);
			} else if (req.getParameter("do").equals("append")) {
				int dung, floor, plus;
				Collection<Integer> eggs;
				String[] eggArr;
				
				dung = Integer.parseInt(req.getParameter("dung"));
				floor = Integer.parseInt(req.getParameter("floor"));
				plus = Integer.parseInt(req.getParameter("plus"));
				
				eggArr = req.getParameter("egg").split(",");
				
				eggs = new LinkedList<Integer>();
				
				for (int i = 0; i < eggArr.length; i++) {
					eggs.add(Integer.parseInt(eggArr[i]));
				}
				
				PadEmulatorSettings.StatisticFunction.log(dung, floor, eggs, plus);
				return;
			}
			if (req.getParameter("ajax") != null) {
				resp.setHeader("Content-Type","application/json; charset=utf-8");
				if (queryResult == null) {
					o.print("true");
				} else {
					Iterator<Floor> iter;
					String s;
					Floor f;
					
					iter = queryResult.iterator();
					s = "";
					
					while (iter.hasNext()) {
						f = iter.next();
						s += f.toString();
						if (iter.hasNext()) {
							s += ',';
						}
					}
					o.print("[" + s + "]");
				}
				return;
			}
		}
		if (queryResult != null) {
			boolean showPlusValue;
			
			showPlusValue = (!req.getParameter("do").equals("egg")) || req.getParameter("egg").equals("+");
			queryResultString = "<h3>Query Result:</h3><table border='1'><tbody><tr class='heading'><th>Dungeon</th><th>Sample Size</th><th>Egg</th><th>Expected</th></tr>";
			
			for (Floor floor : queryResult) {
				SortedSet<Entry<Integer, Integer>> eggSorted;
				eggSorted = new TreeSet<Entry<Integer, Integer>>(new EggEntryComparator()); 
				eggSorted.addAll(floor.egg.entrySet());
				
				queryResultString += "<tr><td>" + dungeonID(floor.dung) + ' ' + PadEmulatorSettings.StatisticFunction.dungeonName(floor.dung) + " - "+ floorString(floor.floor) + "</td>";
				queryResultString += "<td align='middle'>" + (floor.occurance < 100 ? "<font color=red>" + floor.occurance + "</font>": Integer.toString(floor.occurance)) + "</td>";
				queryResultString += "<td align='middle'>Plus</td><td>" + (showPlusValue?expectedString(floor.plus, floor.occurance):"---")+ "</td></tr>";
				
				for (Entry<Integer,Integer> e : eggSorted) {
					queryResultString += "<tr>" +	
					"<td>&nbsp;</td><td>&nbsp;</td><td class='eggid2img'>" + e.getKey() + "</td><td>" + expectedString(e.getValue(), floor.occurance)+ "</td>" +
					"</tr>";
				}
			}
			queryResultString += "</tbody></table>";
		}

		resp.setHeader("Content-Type","text/html; charset=utf-8");
		if (req.getParameter("partial") == null) {
	
			o.println("<html><head>" +
					"<script src='//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'></script>" +
					"<script src='/pad/monster.js'></script>" +
					"<script src='/pad/monsterDB.js'></script>" +
					"<script src='/pad/statistics.js'></script>" +
					"<title>Silverwzw's Puzzle & Dungeon " + (readOnly?"Tool":"Craker") + " - Statistics</title></head><body>");
			
			o.println("<h1>Query:</h1>");
			o.println("<h3>Egg Statistics:</h3>");
			o.println("Egg: <input type='text' name='egg' id='egg' size=5 value='" + currentQueryingEgg + "'/><input type='submit' value='Submit' onclick='query_egg();'/>");
			o.println("<h3>Dungeon Statistics</h3>");
			
			
			o.print("Dungeon: ");
			o.println();
			o.print(getDungeonList(currentQueryingDungeon, "<select id='dung' name='dung' id='dungeon' onchange='query_dungeon();'>"));
			if (!readOnly) {
				o.println("<h4>Set a name for Dungeon</h4>" +
						"<form action='/pad' method='GET'>" +
						"<input type='hidden' name='action' value='stat' />" +
						"<input type='hidden' name='do' value='setname' />" +
						"Dungeon ID:<input type='text' name='dung' />" +
						"Dungeon Name:<input type='text' name='name' />" +
						"<input type='submit' value='Submit' />" +
						"</form>");
			} else {
				o.print("<br/><br />");
			}
			o.println(mlookup());
			o.println("<div id='result'>" + queryResultString + "</div>");
			o.println("</body></html>");
		} else {
			o.print(queryResultString);
		}
		
	}
	final static String dungeonID(int i) {
		if (i < 100) {
			return i>=10 ? ("0" + i) : "00" + i;
		} else {
			return Integer.toString(i);
		}
	}
	final private static String expectedString(Integer i, int j) {
		long k = i * 10000;
		k = k / (int) j;
		return (j < 100 ? "~" : "") + Float.toString(k/(float) 10000);
	}
	final private String floorString(int i) throws UnsupportedEncodingException {
		return PadEmulatorSettings.utf82iso8859_1("µÚ") + i + PadEmulatorSettings.utf82iso8859_1("²ã");
	}
	final static String getDungeonList(int currentQueryingDungeon, String selectLabel) throws UnsupportedEncodingException {
		String r, script;
		Map<String, SortedSet<Integer>> dungeonByGroup;
		dungeonByGroup = new HashMap<String, SortedSet<Integer>>();
		
		r = "";
		script ="<script>window['dungeon2name']={};";
		
		for (int dungeon : PadEmulatorSettings.StatisticFunction.allLoggedDungeons()) {
			String group;
			group = PadEmulatorSettings.dungeonGroup(dungeon);
			if (!dungeonByGroup.containsKey(group)) {
				dungeonByGroup.put(group, new TreeSet<Integer>());
			}
			dungeonByGroup.get(group).add(dungeon);
		}
		
		for (String group : new TreeSet<String>(dungeonByGroup.keySet())){
			r += "<optgroup label='" + group + "'>";
			for (Integer dungeon : dungeonByGroup.get(group)) {
				String selected, name;
				selected = (currentQueryingDungeon == dungeon)?" selected":"";
				name = PadEmulatorSettings.StatisticFunction.dungeonName(dungeon);
				r += "<option value=" + dungeon + selected +">" + dungeonID(dungeon) + ' ' + name + "</option>";
				script += "window['dungeon2name'][" + dungeon + "] = '" + name + "';";
			}
			r += "</optgroup>";
		}
		script += "</script>";
		r = (selectLabel != null) ? (selectLabel + r + "</select>") : "";
		return  r + script;
	}
	final static public String mlookup() {
		return "<script src='/pad/mlookup.js'></script>" +
			"<table><tbody><tr><td vlign='middle'><h3>Monster Lookup:</h3></td><td>&emsp;</td>" +
			"<td vlign='middle'><input type='text' id='mlookup' onchange='MlookupObj.exec();'/></td><td>&emsp;</td>" +
			"<td id='mlookup_img' vlign='middle'></td><td>&emsp;</td>" +
			"<td id='mlookup_detail' vlign='middle'></td>" +
			"</tr></tbody></table>";
		
	}
}