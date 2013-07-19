package com.silverwzw.gae.tools.pad_emulator;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jsr107cache.CacheException;

import org.apache.commons.codec.binary.Hex;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserServiceFactory;



final public class PadEmulatorSettings {
	static StorageLayer storage;
	private String pid;
	
	//inner exceptions
	@SuppressWarnings("serial")
	final public static class PlayerIdNotRecognizeException extends RuntimeException {
		PlayerIdNotRecognizeException() {};
		PlayerIdNotRecognizeException(String s) {super(s);};
		PlayerIdNotRecognizeException(Exception e) {super(e);};
	};
	@SuppressWarnings("serial")
	final public static class UserHashNotFoundException extends RuntimeException {
		UserHashNotFoundException() {};
		UserHashNotFoundException(String s) {super(s);};
		UserHashNotFoundException(Exception e) {super(e);};
	};
	
	public static Map<String,String> pid2name;
	public static Map<String,String> pid2uid;
	public static Map<String,String> pid2google;
	private static Map<String,Agent> pid2agent;
	public static Map<String,Boolean> pid2dev;
	public static Map<String,Boolean> pid2reg;
	public static Map<String,Boolean> pid2fullfunction;
	public static Set<String> adminGoogleSet;
	public static Map<String,Integer> pid2tzadj;
	
	static {
		
		pid2agent = new HashMap<String,Agent>();
		pid2agent.put("324151024", new Agent("31fed252-c432-4ba7-b544-7375e06b8e81", "324151024", false));
		pid2agent.put("324363124", new Agent("01d97c68-8c5e-44bc-86b1-c1faa033de89", "324363124", false));

		storage = StorageLayerFactory.googleCacheAndDatastore("PadSettings");
		
		loadMeta();
		
	}
	
	final public static synchronized void saveMeta() {
		String s = "";
		for (Entry<String,String> e : pid2name.entrySet()) {
			s += e.getKey() + '`' + e.getValue() + ';';
		}
		setGeneral("pid2name",s);
		s = "";
		for (Entry<String,String> e : pid2google.entrySet()) {
			s += e.getKey() + '`' + e.getValue() + ';';
		}
		setGeneral("pid2google",s);
		s = "";
		for (Entry<String,String> e : pid2uid.entrySet()) {
			s += e.getKey() + '`' + e.getValue() + ';';
		}
		setGeneral("pid2uid",s);
		s = "";
		for (Entry<String,Boolean> e : pid2dev.entrySet()) {
			s += e.getKey() + '`' + (e.getValue()?'t':'f') + ';';
		}
		setGeneral("pid2dev",s);
		s = "";
		for (Entry<String,Boolean> e : pid2reg.entrySet()) {
			s += e.getKey() + '`' + (e.getValue()?'t':'f') + ';';
		}
		setGeneral("pid2reg",s);
		s = "";
		for (Entry<String,Boolean> e : pid2fullfunction.entrySet()) {
			s += e.getKey() + '`' + (e.getValue()?'t':'f') + ';';
		}
		setGeneral("pid2fullfunction",s);
		s = "";
		for (String hash : adminGoogleSet) {
			s += hash + ";";
		}
		setGeneral("adminGoogleSet",s);
		s = "";
		for (Entry<String,Integer> e : pid2tzadj.entrySet()) {
			s += e.getKey() + '`' + e.getValue() + ';';
		}
		setGeneral("pid2tzadj",s);
		
	}
	final public static synchronized void loadMeta() {
		String s;
		s = (String) getGeneral("pid2name");
		pid2name = new HashMap<String,String>();
		for (String pn : s.split(";")) {
			if (pn.equals("")) {
				continue;
			}
			pid2name.put(pn.split("`")[0], pn.split("`")[1]);
		}
		s = (String) getGeneral("pid2uid");
		pid2uid = new HashMap<String,String>();
		for (String pn : s.split(";")) {
			if (pn.equals("")) {
				continue;
			}
			pid2uid.put(pn.split("`")[0], pn.split("`")[1]);
		}
		s = (String) getGeneral("pid2google");
		pid2google = new HashMap<String,String>();
		for (String pn : s.split(";")) {
			if (pn.equals("")) {
				continue;
			}
			pid2google.put(pn.split("`")[0], pn.split("`")[1]);
		}
		s = (String) getGeneral("pid2dev");
		pid2dev = new HashMap<String,Boolean>();
		for (String pn : s.split(";")) {
			if (pn.equals("")) {
				continue;
			}
			pid2dev.put(pn.split("`")[0], (Boolean)(pn.split("`")[1].equals("t") ? true : false));
		}
		s = (String) getGeneral("pid2reg");
		pid2reg = new HashMap<String,Boolean>();
		for (String pn : s.split(";")) {
			if (pn.equals("")) {
				continue;
			}
			pid2reg.put(pn.split("`")[0], (Boolean)(pn.split("`")[1].equals("t") ? true : false));
		}
		s = (String) getGeneral("pid2fullfunction");
		pid2fullfunction = new HashMap<String,Boolean>();
		for (String pn : s.split(";")) {
			if (pn.equals("")) {
				continue;
			}
			pid2fullfunction.put(pn.split("`")[0], (Boolean)(pn.split("`")[1].equals("t") ? true : false));
		}
		s = (String) getGeneral("adminGoogleSet");
		adminGoogleSet = new HashSet<String>();
		for (String pn : s.split(";")) {
			if (pn.equals("")) {
				continue;
			}
			adminGoogleSet.add(pn);
		}
		s = (String) getGeneral("pid2tzadj");
		pid2tzadj = new HashMap<String,Integer>();
		if (s != null) {
			for (String pn : s.split(";")) {
				if (pn.equals("")) {
					continue;
				}
				pid2tzadj.put(pn.split("`")[0], (Integer)Integer.parseInt(pn.split("`")[1]));
			}
		}
	}
	@SuppressWarnings("serial")
	final public static class Agent implements java.io.Serializable {
		private String agentUid;
		private boolean isApple;
		private String principalId;
		Agent(String agentUid,String principalId, boolean isApple) {
			this.isApple = isApple;
			this.principalId = principalId;
			this.agentUid = agentUid;
		}
		public boolean isApple() {
			return isApple;
		}
		public String agentString() {
			return PadEmulatorSettings.instance(principalId).loginString.get();
		}
		public String agentUid(){
			return agentUid;
		}
	};
	
	public static interface StorageLayer {
		boolean put(String key,Object obj);
		Object get(String key);
	}
	
	@SuppressWarnings("serial")
	final public static class LogList implements java.io.Serializable {
		final static public class Log implements java.io.Serializable {
			public String request;
			public String response;
			Log(String req,String resp) {
				request = req;
				response = resp;
			}
		}
		private Log[] ll;
		private int index;
		private int length;
		LogList() {
			int i;
			length = 16;
			ll = new Log[length];
			index = length - 1;
			for (i = 0; i < length; i++) {
				ll[i] = null;
			}
		}
		public void add(String req, String resp) {
			ll[index] = new Log(req,resp);
			index = (index == 0) ? (length - 1) : (index - 1);
		}
		public Log get(int i) {
			return ll[(index + 1 + i)%length];
		}
		public int capacity() {
			return length;
		}
	}

	PadEmulatorSettings(String playerId) {
		if (pid2uid.containsKey(playerId)) {
			pid = playerId;
			return;
		}
		for (Entry<String, Agent> s2agent : pid2agent.entrySet()) {
			if (playerId.equals(s2agent.getValue().agentUid()) && PadEmulatorSettings.instance(s2agent.getKey()).agentOn.does()) {
				pid = s2agent.getKey();
				return;
			}
		}
		if (pid2uid.containsValue(playerId)){
			for (Entry<String,String> e : pid2uid.entrySet()) {
				if (e.getValue().equals(playerId)) {
					pid = e.getKey();
					if (PadEmulatorSettings.instance(pid).agentOn.does()) {
						continue;
					} else {
						return;
					}
				}
			}
		}
		throw new PlayerIdNotRecognizeException();
	}
	
	public static PadEmulatorSettings instance(String id) {
		return new PadEmulatorSettings(id);
	}
	
	private static Object get(String itemName, String settingPid) {
		String key;
		key = itemName;
		if (settingPid != null) {
			key += '-' + settingPid;
		}
		return storage.get(key);
	}
	public Object getSpec(String itemName) {
		return get(itemName, pid);
	}
	public static Object getGeneral(String itemName) {
		return get(itemName, null);
	}
	private static boolean set(String itemName, Object value, String settingPid) {
		String key = itemName;
		if (settingPid != null) {
			key += '-' + settingPid;
		}
		return storage.put(key, value);
	}
	public boolean setSpec(String itemName, Object value){
		return set(itemName,value,pid);
	}
	public static boolean setGeneral(String itemName, Object value){
		return set(itemName,value,null);
	}
	public boolean is(String itemName,boolean defaultValue) {
		Boolean b = (Boolean)getSpec(itemName);
		if (b == null) {
			return defaultValue;
		} else {
			return (boolean)b;
		}
	}
	final protected class LastFailedTimeStamp { 
		final public void set(String ts) {
			setSpec("fts",ts);
		}
		final public String get() {
			String ts;
			ts = (String)getSpec("fts");
			if (ts == null) {
				return "";
			}
			return ts;
		}
	};
	public LastFailedTimeStamp lastFailedTS = new LastFailedTimeStamp();
	final protected class AgentOn {
		final public boolean does() {
			return is("agentOn",false);
		}
		final public void set(Boolean b) {
			setSpec("agentOn",b);
			Channel.broadcast(Channel.refreshjson(pid));
		}
	};
	public AgentOn agentOn = new AgentOn();
	final protected class LoginString {
		final public void set(String s) {
			setSpec("loginString", s);
		}
		final public String get() {
			return (String)getSpec("loginString");
		}
	};
	public LoginString loginString = new LoginString();
	final protected class BlockLvlUp { 
		final public boolean does() {
			return is("blockLevelUp", true);
		}
		final public void set(boolean bool) {
			setSpec("blockLevelUp", bool);
			Channel.broadcast(Channel.refreshjson(pid));
		}
	};
	BlockLvlUp blockLvlUp = new BlockLvlUp();
	final protected class EggHunting {
		final public void setConditionNumber(int i) {
			setSpec("ConditionNumber", (Long)(long)i);
			lastFailedTS.set("0");
			Channel.broadcast(Channel.refreshjson(pid));
		}
		final public int getConditionNumber() {
			Long i;
			i = (Long) getSpec("ConditionNumber");
			if (i == null) {
				return 0;
			}
			return (int)(long)i;
		}
		final public int getMode() {
			Long i;
			if (getConditionNumber() == 0) {
				return 0;
			}
			i = (Long) getSpec("eggHuntingMode");
			if (i == null) {
				return 2;
			}
			return (int)(long)i;
		}
		final public void setMode(int mode) {
			setSpec("eggHuntingMode", (Long)(long)mode);
			lastFailedTS.set("0");
			Channel.broadcast(Channel.refreshjson(pid));
		}
		final public void huntEgg(String eggID, int value) {
			Map<String, Integer> evmap;
			evmap = new HashMap<String, Integer>();
			evmap.put(eggID, value);
			huntEgg(evmap);
		}
		@SuppressWarnings("unchecked")
		final public void huntEgg(Map<String,Integer> m) {
			Map<String, Integer> evmap;
			evmap = (Map<String, Integer>) getSpec("huntEgg");
			if (evmap == null) {
				evmap = new HashMap<String,Integer>();
			}
			for (Entry<String, Integer> e : m.entrySet()) {
				int v;
				v = e.getValue();
				if (v == 0 && evmap.containsKey(e.getKey())) {
					evmap.remove(e.getKey());
				}
				if (v > 0) {
					evmap.put(e.getKey(), v);
				}
			}
			setSpec("huntEgg", evmap);
			lastFailedTS.set("0");
			Channel.broadcast(Channel.refreshjson(pid));
		}
		@SuppressWarnings("unchecked")
		final public Map<String,Integer> huntEggMap() {
			Map<String,Integer> evmap;
			evmap = (Map<String,Integer>) getSpec("huntEgg");
			if (evmap == null) {
				return new HashMap<String,Integer>();
			} else {
				return evmap;
			}
		}
		final public void cleanHuntEggMap() {
			setSpec("huntEgg",new HashMap<String,Integer>());
			lastFailedTS.set("0");
			Channel.broadcast(Channel.refreshjson(pid));		
		}
	}
	public EggHunting eggHunting = new EggHunting();
	final protected class Dungeon extends AbstractDungeon {
		final public void setDungeon(String dungeonStr) {
			setSpec("LastDungeonRecieved", dungeonStr);
		}
		final public String getDungeonString() {
			return (String)getSpec("LastDungeonRecieved");
		}
		final public void setMode(int mode) {
			setSpec("DungeonMode", (Long)(long)mode);
			Channel.broadcast(Channel.refreshjson(pid));
		}
		final public int getMode() {
			if (getSpec("DungeonMode") == null || !pid2fullfunction.get(pid)) {
				return 0;
			}
			return (int)(long)(Long)getSpec("DungeonMode");
		}
	};
	public Dungeon dungeon = new Dungeon();
	final protected class Resolve {
		final public void set(boolean b) {
			setSpec("infStone",b);
			Channel.broadcast(Channel.refreshjson(pid));
		}
		final public boolean isActive() {
			if (!pid2fullfunction.get(pid)) {
				return false;
			}
			return is("infStone", true);
		}
	};
	public Resolve resolve = new Resolve();
	public static class StatisticFunction {
		final static public void log(int dung, int floor, Collection<Integer> eggs, int plus) {
			Map<Integer,Integer> egg2occurance;
			Map<Integer,Key> egg2key;
			Map<Key,Entity> key2entity;
			DatastoreService ds;
			
			ds = com.google.appengine.api.datastore.DatastoreServiceFactory.getDatastoreService();
			egg2occurance = new HashMap<Integer, Integer>();
			egg2key = new HashMap<Integer, Key>();
	
			egg2occurance.put(0, 1);
			if (plus > 0) {
				egg2occurance.put(-1, plus);
			}
			
			for (int egg : eggs) {
				if (egg2occurance.containsKey(egg)) {
					egg2occurance.put(egg, egg2occurance.get(egg) + 1);
				} else {
					egg2occurance.put(egg, 1);
				}
			}
			for (Entry<Integer, Integer> e: egg2occurance.entrySet()) {
				egg2key.put(e.getKey(), KeyFactory.createKey("PadStatistics", Integer.toString(dung) + '-' + Integer.toString(floor) + ':' + Integer.toString((int)e.getKey())));
			}
			key2entity = ds.get(egg2key.values());
			for (Entry<Integer, Key> e : egg2key.entrySet()) {
				Entity en;
				long tmp;
				if (!key2entity.containsKey(e.getValue())) {
					en = new Entity(e.getValue());
					en.setProperty("dung", dung);
					en.setProperty("floor", floor);
					en.setProperty("egg", e.getKey());
					en.setProperty("occurance", (Long)0L);
				} else {
					en = key2entity.get(e.getValue());
				}
				tmp = (long)(Long)en.getProperty("occurance");
				en.setProperty("occurance", tmp + (long)(int)egg2occurance.get(e.getKey()));
				key2entity.put(e.getValue(), en);
			}
			ds.put(key2entity.values());
		}
		final static public Collection<Integer> allLoggedDungeons() {
			Query q;
			Filter f;
			DatastoreService ds;
			SortedSet<Integer> ll;
			Iterable<Entity> result;
			
			ds = DatastoreServiceFactory.getDatastoreService();
			q = new Query("PadStatistics");
			f = new Query.FilterPredicate("egg", FilterOperator.EQUAL, 0);
			q.setFilter(f).addSort("dung",SortDirection.ASCENDING);
			result = ds.prepare(q).asIterable();
			
			ll = new TreeSet<Integer>();
			for (Entity entity : result) {
				ll.add((int)(long)(Long)entity.getProperty("dung"));
			}
			
			return ll;
		}
		final static public Collection<Statistics.Floor> queryDungeon(int dungeon) {
			HashMap<Integer, Statistics.Floor> floors;
			Query q;
			Filter f;
			DatastoreService ds;
			Iterable<Entity> result;
			
			floors = new HashMap<Integer, Statistics.Floor>();
			ds = DatastoreServiceFactory.getDatastoreService();
			q = new Query("PadStatistics");
			f = new Query.FilterPredicate("dung", FilterOperator.EQUAL, (long) dungeon);
			
			q.setFilter(f);
			result = ds.prepare(q).asIterable();
			for (Entity entity : result) {
				Integer i,egg;
				i = (Integer)(int)(long)(Long)entity.getProperty("floor");
				if (!floors.containsKey(i)) {
					floors.put(i, new Statistics.Floor(dungeon, (int)i));
				}
				egg = (Integer)(int)(long)(Long)entity.getProperty("egg");
				switch((int) egg) {
					case 0:
						floors.get(i).occurance = (Integer)(int)(long)(Long)entity.getProperty("occurance");
						break;
					case -1:
						floors.get(i).plus = (Integer)(int)(long)(Long)entity.getProperty("occurance");
						break;
					default:
						floors.get(i).egg.put(egg, (Integer)(int)(long)(Long)entity.getProperty("occurance"));
				}
			}
			return floors.values();
		}
		final static public Collection<Statistics.Floor> queryEgg(int eggID) {
			HashMap<Integer, Statistics.Floor> floors;
			Query q,qn;
			Filter f,fn;
			DatastoreService ds;
			Iterable<Entity> result;
			
			floors = new HashMap<Integer, Statistics.Floor>();
			ds = DatastoreServiceFactory.getDatastoreService();
			q = new Query("PadStatistics");
			f = new Query.FilterPredicate("egg", FilterOperator.EQUAL, (long) eggID);
			
			q.setFilter(f);
			result = ds.prepare(q).asIterable();
			for (Entity entity : result) {
				int index,i,j,egg;
				j = (int)(long)(Long)entity.getProperty("floor");
				i = (int)(long)(Long)entity.getProperty("dung");
				index = j + 100 * i;
				if (!floors.containsKey((Integer) index)) {
					floors.put((Integer) index, new Statistics.Floor((Integer)i, (Integer)j));
					qn = new Query("PadStatistics");
					fn = Query.CompositeFilterOperator.and(new Query.FilterPredicate("dung", FilterOperator.EQUAL, (long) i), new Query.FilterPredicate("floor", FilterOperator.EQUAL, (long) j), new Query.FilterPredicate("egg", FilterOperator.EQUAL, (long) 0));
					qn.setFilter(fn);
					floors.get((Integer) index).occurance = (int)(long)(Long)ds.prepare(qn).asSingleEntity().getProperty("occurance");
				}
				egg = (int)(long)(Long)entity.getProperty("egg");
				if (egg != -1) {
					floors.get((Integer) index).egg.put(egg, (Integer)(int)(long)(Long)entity.getProperty("occurance"));
				} else {
					floors.get((Integer) index).plus = (int)(long)(Long)entity.getProperty("occurance");
				}
			}
			return floors.values();
		}
		final static public void dungeonName(int dungeon, String name) {
			setGeneral("dungName-" + dungeon, name);
		}
		final static public String dungeonName(int dungeon) {
			String name;
			name = (String) getGeneral("dungName-" + dungeon);
			if (name == null) {
				return "";
			}
			return name;
		}
	};
	final protected class Stamina {
		final public String time2full() {
			String t;
			t = (String) getSpec("fullStaminaTime");
			if (t == null) {
				return "";
			} else {
				return t;
			} 
		}
		final public void time2full(String t) {
			setSpec("fullStaminaTime",t);
			Channel.broadcast(Channel.refreshjson(pid));
		}
		final public void maxValue(String max_sta) {
			setSpec("maxStamina",(Long)(long)Integer.parseInt(max_sta));
			Channel.broadcast(Channel.refreshjson(pid));
		}
		final public int maxValue() {
			Long i;
			i = (Long)getSpec("maxStamina");
			if (i == null) {
				return 0;
			} else {
				return (int)(long)i;
			}
		}
	};
	public Stamina stamina = new Stamina();
	final protected class SuperFriend {
		final public void set(String cid) {
			setSpec("superFriend",cid);
			Channel.broadcast(Channel.refreshjson(pid));
		}
		final public String get() {
			String s;
			s = (String)getSpec("superFriend");
			if (s == null) {
				s = "";
			}
			return s;
		}
	};
	public SuperFriend superFriend = new SuperFriend();
	final protected class PlayerData {
		final public String get() {
			String s;
			s = (String)getSpec("playerData");
			if (s == null) {
				return "";
			}
			return s;
		}
		final public void set(String s) {
			setSpec("playerData",s);
		}
	};
	public PlayerData playerData = new PlayerData(); 
	public static void setShadowId(String id) {
		setGeneral("shadowId",id);
	}
	public static String getShadowId() {
		return (String)getGeneral("shadowId");
	}
	final public static class Log {
		public static void log(String req,String resp) {
			LogList logList;
			logList = (LogList) getGeneral("log");
			if (logList == null) {
				logList = new LogList();
			}
			logList.add(req, resp);
			setGeneral("log",logList);
		}
		public static LogList log() {
			return (LogList) getGeneral("log");
		}
	}
	final protected static class ChannelToken {
		public static Channel.ChannelToken get(String hash) {
			return (Channel.ChannelToken) getGeneral("channel-token-" + hash);
		}
		public static void set(String hash, Channel.ChannelToken token) {
			setGeneral("channel-token-" + hash, token);
		}
	};
	final public static String currentUserHash() {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			assert false : "NoSuchAlgorithmException";
			return null;
		}
		if (UserServiceFactory.getUserService().getCurrentUser() == null) {
			return "";
		}
		digest.update((UserServiceFactory.getUserService().getCurrentUser().getUserId() + "silverwzw-Anti-Rainbow-Table-Salt").getBytes());
		return new String(Hex.encodeHex(digest.digest()));
	}
	private static Set<String> _cachedGoogleSet;
	final public static synchronized  Set<String> googleSet() {
		if (_cachedGoogleSet == null) {
			Set<String> tmp; //be careful to multi-thread sync problem, first create object then assign.
			tmp = new HashSet<String>();
			tmp.addAll(pid2google.values());
			_cachedGoogleSet = tmp;
		}
		return _cachedGoogleSet;
	}
	final public static Set<String> pidSet() {
		return pid2name.keySet();
	}
	final public static Collection<String> nameCollection() {
		return pid2name.values();
	}
	final public static Set<Entry<String,String>> pid2nameEntrySet() {
		return pid2name.entrySet();
	}
	final protected class UserInfo {
		final public Agent getAgent() {
			return pid2agent.get(pid);
		}
		final public boolean devIsApple() {
			if (agentOn.does() && getAgent()!=null) {
				return getAgent().isApple();
			} else {
				return pid2dev.get(pid);
			}
		}
		final public boolean regionIsUS() {
			return pid2reg.get(pid);
		}
		final public String getPid() {
			return pid;
		}
		final public String getHash() {
			return pid2google.get(pid);
		}
		final public String getName() {
			return pid2name.get(pid);
		}
		final public String getUid() {
			return pid2uid.get(pid);
		}
		final public boolean isFullFuntion() {
			return pid2fullfunction.get(pid);
		}
	};
	public UserInfo userInfo = new UserInfo();
	final static public Agent detectActiveAgentByQueryString(String qs) {
		if (!qs.contains("action=login")) { //only work with login action
			return null;
		}
		for (Entry<String,Agent> e : pid2agent.entrySet()) {
			boolean agentOn;
			agentOn = PadEmulatorSettings.instance(e.getKey()).agentOn.does();
			if (agentOn && qs.contains(e.getValue().agentUid())) {
				return e.getValue();
			}
		}
		return null;
	}
	final protected class DailyBonus {
		final public void set(String s) {
			setSpec("DailyBonus", s);
			Channel.broadcast(Channel.refreshbonus(pid));
		}
		final public String get() {
			String s = (String) getSpec("DailyBonus");
			if (s == null) {
				return "null";
			} else {
				return s;
			}
		}
	};
	public DailyBonus dailyBonus = new DailyBonus();
	final class DownloadData {
		final public void saveEnemySkillData(String s) {
			setSpec("EnemySkillData", s);
		}
		final public void saveSkillData(String s) {
			setSpec("SkillData", s);
		}
		final public void saveDungeonData(String s) {
			setSpec("DungeonData", s);
		}
		final public void saveCardData(String s) {
			setSpec("CardData", s);
		}
		final public String getSavedDataJSON() {
			String json = "", tmp;
			tmp = (String) getSpec("EnemySkillData");
			json += "enemySkillData:" + (tmp == null ? "null" : tmp) + ',';
			tmp = (String) getSpec("SkillData");
			json += "skillData:" + (tmp == null ? "null" : tmp) + ',';
			tmp = (String) getSpec("DungeonData");
			json += "dungeonData:" + (tmp == null ? "null" : tmp) + ',';
			tmp = (String) getSpec("CardData");
			json += "cardData:" + (tmp == null ? "null" : tmp);
			return "{" + json + "}";
		}
	};
	DownloadData downloadData = new DownloadData();
	final public static String dungeonGroup(int dungeon) throws UnsupportedEncodingException {
		//10-56 Normal
		if (dungeon >= 10 && dungeon <= 56) {
			return utf82iso8859_1("0-普通地城");
		}
		
		//102-105, 121 Weekly
		if ((dungeon >= 102 && dungeon <= 105) || dungeon == 121){
			return utf82iso8859_1("1-每日地城");
		}
		
		//201-221 Technical
		if (dungeon >= 201 && dungeon <= 216){
			return utf82iso8859_1("2-技术地城");
		}
		
		//217, 221-223 conditional
		if (dungeon == 217 || (dungeon >= 221 && dungeon <= 223)){
			return utf82iso8859_1("3-条件地城");
		}
		
		//122-126,326-327 Emergency
		if ((dungeon >= 122 && dungeon <= 127) || (dungeon >= 326 && dungeon <= 327) || dungeon == 134){
			return utf82iso8859_1("4-紧急地城");
		}
		
		//146-152,311-315,301-305 double weekly
		if ((dungeon >= 146 && dungeon <= 152) || (dungeon <= 315 && dungeon >= 311) || (dungeon <= 305 && dungeon >= 301)){
			return utf82iso8859_1("5-双周地城");
		}
		
		//176,158,163-164,169,307,332,334
		if (dungeon == 158 || dungeon == 176 || (dungeon >= 163 && dungeon <= 164) || dungeon == 169 || dungeon == 307 || dungeon == 318 || dungeon == 332 || dungeon == 334){
			return utf82iso8859_1("6-降临地城");
		}
		
		//187-188,308,310
		if ((dungeon >= 187 && dungeon <=188) || dungeon == 198 || dungeon == 341 || dungeon == 317 || dungeon == 308 || dungeon == 310){
			return utf82iso8859_1("7-合作地城");
		}
		
		//133, 135, 330, 190
		if (dungeon == 133 || dungeon == 135 || dungeon == 329 || dungeon == 330 || dungeon == 190) {
			return utf82iso8859_1("8-限定地城");
		}
		
		//306,162,165-168,170,130
		if (dungeon == 306 || dungeon == 162 || (dungeon <= 168 && dungeon >= 165) || dungeon == 170 || dungeon == 130) {
			return utf82iso8859_1("9-活动地城");
		}
		
		return utf82iso8859_1("未分类");
	}
	final public static Set<String> hash2pidSet(String hash) {
		Set<String> pids;
		pids = new HashSet<String>();
		for (Entry<String,String> e : pid2google.entrySet()) {
			if (e.getValue().equals(hash)) {
				pids.add(e.getKey());
			}
		}
		return pids;
	}
	final public static Set<String> hash2pidSet() {
		return hash2pidSet(PadEmulatorSettings.currentUserHash());
	}
	final public static boolean isAdmin() {
		return isAdmin(PadEmulatorSettings.currentUserHash());
	}
	final public static boolean isAdmin(String hash) {
		return adminGoogleSet.contains(hash);
	}
	final public static String utf82iso8859_1(String utf8) throws UnsupportedEncodingException {
		return new String(utf8.getBytes("UTF-8"),"ISO-8859-1");
	}
}

final class StorageLayerFactory {
	@SuppressWarnings("serial")
	final private static class NotSerializableException extends Exception {};
	final static class Dummy implements PadEmulatorSettings.StorageLayer{
		final public boolean put(String key, Object obj) {
			return false;
		}
		final public Object get(String key) {
			return null;
		}
	};
	final static class GoogleDatastore implements PadEmulatorSettings.StorageLayer {
		public com.google.appengine.api.datastore.DatastoreService datastore;
		String id;
		GoogleDatastore(String id) {
			datastore = com.google.appengine.api.datastore.DatastoreServiceFactory.getDatastoreService();
			this.id = id;
		}
		final public boolean put(String key, Object obj) {
			try {
				com.google.appengine.api.datastore.Entity en;
			
				en = new com.google.appengine.api.datastore.Entity(id,key);
				try {
					en.setProperty("value", obj);
				} catch (java.lang.IllegalArgumentException e) { // if the object is not basic type, try serialize it
					Class<?>[] interfaces;
					int i;
					boolean isSerializable;
					
					//test if the object is Serializable
					interfaces = obj.getClass().getInterfaces();
					isSerializable = false;
					for (i = 0; i < interfaces.length; i++) {
						if (java.io.Serializable.class.equals(interfaces[i])) {
							isSerializable = true;
							break;
						}
					}
					if (!isSerializable) {
						throw new NotSerializableException();
					}
					
					//save the object
					com.google.appengine.api.datastore.Blob blob;
					java.io.ObjectOutputStream oos;
					java.io.ByteArrayOutputStream bos;
					bos = new java.io.ByteArrayOutputStream();
					oos = new java.io.ObjectOutputStream(bos);
					oos.writeObject(obj);
					oos.close();
					blob = new com.google.appengine.api.datastore.Blob(bos.toByteArray());
					en.setProperty("value", blob);
				}
				datastore.put(en);
				return true;
			} catch (Exception e) {
				return false;	
			}
		}

		final public Object get(String key) {
			try {
				com.google.appengine.api.datastore.Key k;
				com.google.appengine.api.datastore.Entity en;
				Object obj;
				
				k = com.google.appengine.api.datastore.KeyFactory.createKey(id,key);
				try {
					en = datastore.get(k);
				} catch (com.google.appengine.api.datastore.EntityNotFoundException e) {
					return null;
				}
				
				obj = en.getProperty("value");
				
				if (obj.getClass().equals(com.google.appengine.api.datastore.Blob.class)) {
					com.google.appengine.api.datastore.Blob blob;
					java.io.ObjectInputStream ois;
					java.io.ByteArrayInputStream bis;
					blob = (com.google.appengine.api.datastore.Blob) obj;
					bis = new java.io.ByteArrayInputStream(blob.getBytes());
					ois = new java.io.ObjectInputStream(bis);
					return ois.readObject();
				} else {
					return obj;
				}
			} catch (Exception e) {
				return null;
			}
		}
		
	};
	final static class GoogleCache implements PadEmulatorSettings.StorageLayer {
		private net.sf.jsr107cache.Cache cache;
		GoogleCache(String id) {
			cache = net.sf.jsr107cache.CacheManager.getInstance().getCache(id);
			if (cache == null) {
				try {
					cache = net.sf.jsr107cache.CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
				} catch (CacheException e) {
					e.printStackTrace();
				}
				net.sf.jsr107cache.CacheManager.getInstance().registerCache(id, cache);
				cache = net.sf.jsr107cache.CacheManager.getInstance().getCache(id);
			}
		}
		final public boolean put(String key, Object obj) {
			cache.put(key, obj);
			return true;
		}
		final public Object get(String key) {
			if (cache.containsKey(key)) {
				return cache.get(key);
			} else {
				return null;
			}
		}
	};
	static class GoogleCacheAndDatastore implements PadEmulatorSettings.StorageLayer{
		private PadEmulatorSettings.StorageLayer cache,datastore;
		GoogleCacheAndDatastore(String id) {
			cache = new GoogleCache(id);
			datastore = new GoogleDatastore(id);
		}
		final public boolean put(String key, Object obj) {
			boolean b1,b2;
			b1 = datastore.put(key, obj);
			b2 = cache.put(key, obj);
			return b1 && b2;
		}

		final public Object get(String key) {
			Object cobj;
			cobj = cache.get(key);
			if (cobj != null) {
				return cobj;
			} else {
				Object dobj;
				dobj = datastore.get(key);
				cache.put(key, dobj);
				return dobj;
			}
		}
	}
	final static PadEmulatorSettings.StorageLayer googleCacheAndDatastore(String identifier) {
		return new GoogleCacheAndDatastore(identifier);
	}
	final static PadEmulatorSettings.StorageLayer dummy() {
		return new Dummy();
	}
	final static PadEmulatorSettings.StorageLayer googleDatastore(String identifier) {
		return new GoogleDatastore(identifier);
	}
	final static PadEmulatorSettings.StorageLayer googleCache(String identifier) {
		return new GoogleCache(identifier);
	}
}