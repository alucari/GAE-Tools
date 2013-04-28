package com.silverwzw.gae.tools.pad_emulator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;

import com.google.appengine.api.users.UserServiceFactory;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

final public class PadEmulatorSettings {
	static Cache cache = null;
	private String pid;
	
	//inner exceptions
	@SuppressWarnings("serial")
	final public static class PlayerIdNotRecognizeException extends RuntimeException {};
	@SuppressWarnings("serial")
	final public static class UserHashNotFoundException extends RuntimeException {};
	
	private static HashMap<String,String> pid2name;
	private static HashMap<String,String> pid2uid;
	private static HashMap<String,String> pid2google;
	private static HashMap<String,Agent> pid2agent;
	private static HashMap<String,Boolean> pid2dev;
	
	static {
		
		pid2name = new HashMap<String,String>();
		pid2name.put("324151024", "tea");
		pid2name.put("324363124", "silverwzw");
		pid2name.put("324224887", "x");
		pid2name.put("324427796", "Tester");
		

		pid2google = new HashMap<String,String>();
		pid2google.put("324151024", "36795a4756f4b90fac03d4dd82b28db4");//tea
		pid2google.put("324363124", "cbf9d8da00cdc95dcd017fe07028029f");//silverwzw
		pid2google.put("324427796", "cbf9d8da00cdc95dcd017fe07028029f");//silverwzw
		pid2google.put("324224887", "361d39b1af4fa514bd48e43ad0bdcf0d"); //x
		

		pid2uid = new HashMap<String,String>();
		pid2uid.put("324151024", "B33ECFC8-F74D-4A88-A5D5-81183DAFC850");
		pid2uid.put("324363124", "0a78f1a0-f5a0-49ef-950e-e6205f5e9389");
		pid2uid.put("324224887", "27C8DDB8-D23C-4345-94B6-805A5DD36A1F");
		pid2uid.put("324427796", "6bcd1fb7-656b-419d-b67f-ba80ec08a6ca");
		

		pid2agent = new HashMap<String,Agent>();
		pid2agent.put("324151024", new Agent("31fed252-c432-4ba7-b544-7375e06b8e81","action=login&t=0&v=5.00&u=B33ECFC8-F74D-4A88-A5D5-81183DAFC850&dev=iPad3,4&osv=6.0&key=DB3B1815",false));
		pid2agent.put("324427796", new Agent("0a78f1a0-f5a0-49ef-950e-e6205f5e9389","action=login&t=1&v=5.01&u=6bcd1fb7-656b-419d-b67f-ba80ec08a6ca&dev=occam&osv=4.2.2&key=3933658F",false));
		
		pid2dev = new HashMap<String,Boolean>();
		pid2dev.put("324151024", (Boolean)true);
		pid2dev.put("324363124", (Boolean)false);
		pid2dev.put("324224887", (Boolean)true);
		pid2dev.put("324427796", (Boolean)false);
		
	}
	
	@SuppressWarnings("serial")
	final public static class Agent implements java.io.Serializable {
		private String agentUid;
		private boolean isApple;
		private String agentString;
		Agent(String agentUid,String agentString, boolean isApple) {
			this.isApple = isApple;
			this.agentString = agentString;
			this.agentUid = agentUid;
		}
		public boolean isApple() {
			return isApple;
		}
		public String agentString() {
			return agentString;
		}
		public String agentUid(){
			return agentUid;
		}
	};

	@SuppressWarnings("serial")
	final private static class freqAccessEggs implements java.io.Serializable {
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
	};
	
	
	@SuppressWarnings("unused")
	private PadEmulatorSettings(){;}
	PadEmulatorSettings(String playerId) {
		
		if (pid2uid.containsKey(playerId)) {
			pid = playerId;
			return;
		} else if (pid2uid.containsValue(playerId)){
			for (Entry<String,String> e : pid2uid.entrySet()) {
				if (e.getValue().equals(playerId)) {
					pid = e.getKey();
					return;
				}
			}
		} else {
			throw new PlayerIdNotRecognizeException();
		}
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
	public void lastFailedTS(String ts) {
		setSpec("fts",ts);
	}
	public String lastFailedTS() {
		String ts;
		ts = (String)getSpec("fts");
		if (ts == null) {
			return "";
		}
		return ts;
	}
	public boolean agentOn() {
		Boolean b;
		b = (Boolean)getSpec("agentOn");
		if (b == null) {
			return false;
		}
		return b;
	}
	public void agentOn(Boolean b) {
		setSpec("agentOn",b);
		Channel.broadcast(Channel.refreshjson(pid));
	}
	public boolean isBlockLevelUp() {
		return is("blockLevelUp");
	}
	public void setBlockLevelUp(boolean bool) {
		setSpec("blockLevelUp", bool);
		Channel.broadcast(Channel.refreshjson(pid));
	}
	public void setDungeonString(String dungeonStr) {
		setSpec("LastDungeonRecieved", dungeonStr);
	}
	public String getDungeonString() {
		return (String)getSpec("LastDungeonRecieved");
	}
	public int isLookingForCertainEgg() {
		Integer i;
		i = (Integer) getSpec("isLookingForCertainEgg");
		if (i == null) {
			return 1;
		}
		return (int)i;
	}
	public void setLookingForCertainEgg(int mode) {
		setSpec("isLookingForCertainEgg", mode);
		Channel.broadcast(Channel.refreshjson(pid));
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
		Channel.broadcast(Channel.refreshjson(pid));
	}
	public void cleanWantedEggs() {
		setSpec("wantedEggs",(Object)new ArrayList<String>());
		setLookingForCertainEgg(0);
		//no need to broadcast channel here, since setLookingForCertainEgg method already does broadcast 
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
	public void setDungeonMode(int mode) {
		setSpec("DungeonMode", mode);
		Channel.broadcast(Channel.refreshjson(pid));
	}
	public int getDungeonMode() {
		if (getSpec("DungeonMode") == null) {
			return 1;
		}
		return (int)(Integer)getSpec("DungeonMode");
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
	public static void setShadowId(String id) {
		setGeneral("shadowId",id);
	}
	public static String getShadowId() {
		return (String)getGeneral("shadowId");
	}
	public void setInfStone(boolean b) {
		setSpec("infStone",b);
		Channel.broadcast(Channel.refreshjson(pid));
	}
	public boolean isInfStone() {
		return is("infStone");
	}
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
	public static Channel.ChannelToken getToken(String hash) {
		return (Channel.ChannelToken) getGeneral("channel-token-" + hash);
	}
	public static void setToken(String hash, Channel.ChannelToken token) {
		setGeneral("channel-token-" + hash, token);
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
	final public static String currentUserHash() {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			assert false : "NoSuchAlgorithmException";
			return null;
		}
		digest.update((UserServiceFactory.getUserService().getCurrentUser().getUserId() + "silverwzw-Anti-Rainbow-Table-Salt").getBytes());
		return new String(Hex.encodeHex(digest.digest()));
	}
	private static Set<String> _cachedGoogleSet;
	final public static Set<String> googleSet() {
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
	final public Agent getAgent() {
		return pid2agent.get(pid);
	}
	final public boolean devIsApple() {
		if (agentOn() && getAgent()!=null) {
			return getAgent().isApple();
		} else {
			return pid2dev.get(pid);
		}
	}
	final static Agent detectActiveAgentByQueryString(String qs) {
		if (!qs.contains("action=login")) { //only work with login action
			return null;
		}
		for (Entry<String,Agent> e : pid2agent.entrySet()) {
			boolean agentOn;
			agentOn = (new PadEmulatorSettings(e.getKey())).agentOn();
			if (agentOn && qs.contains(e.getValue().agentUid())) {
				return e.getValue();
			}
		}
		return null;
	}
	final public String getBonus() {
		String s = (String)getSpec("bonus");
		if (s == null) {
			return "";
		}
		return s;
	}
	final public void setBonus(String b) {
		setSpec("bonus", b);
	}
}

@SuppressWarnings("serial")
final class LogList implements java.io.Serializable{
	final static public class Log implements java.io.Serializable{
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