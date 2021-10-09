package me.onebone.economyland.provider;

/*
 * EconomyLand: A plugin which allows your server to manage lands
 * Copyright (C) 2016  onebone <jyc00410@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector2;
import cn.nukkit.utils.Utils;
import me.onebone.economyland.EconomyLand;
import me.onebone.economyland.Land;


import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;


import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class YamlProvider implements Provider{
	private int landId = 0;
	
	private File file;
	public Map<Integer, Land> lands;
	private EconomyLand plugin;
	
	@SuppressWarnings("unchecked")
	public YamlProvider(EconomyLand plugin, File file){
		lands = new HashMap<>();
		
		this.plugin = plugin;
		this.file = file;
		
		try{
			File dataFile = new File(plugin.getDataFolder(), "LandData.json");
			if(dataFile.exists()){
				Map<String, Object> data = new GsonBuilder().create().fromJson(Utils.readFile(dataFile), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
				this.landId = (int) Double.parseDouble(data.getOrDefault("landId", 0).toString());
			}
		}catch(JsonSyntaxException | IOException e){
			plugin.getLogger().critical(e.getMessage());
		}
		
		Yaml yaml = new Yaml();
		
		HashMap<String, Level> tmp = new HashMap<String, Level>();
		try {
			Map<Integer, LinkedHashMap<String, Object>> load = ((Map<Integer, LinkedHashMap<String, Object>>)yaml.load(Utils.readFile(file)));

			load.forEach((k, v) -> {
				if(this.landId < k){
					this.landId = k + 1;
				}
				
				if(!tmp.containsKey((String) v.get("level"))){
					Level level = plugin.getServer().getLevelByName((String) v.get("level"));
					tmp.put((String) v.get("level"), level);
				}
				
				List<String> invitee = new ArrayList<String>();
				
				Object obj = v.getOrDefault("invitee", new ArrayList<String>());
				if(obj instanceof Map){
					Map<Object, Boolean> temp = (HashMap<Object, Boolean>) obj;
					temp.keySet().forEach((name) -> invitee.add(name.toString()));
				}else{
					((ArrayList<String>)obj).forEach((val) -> invitee.add(val));
				}
				lands.put(k, new Land(k, new Vector2((int) v.get("startX"), (int) v.get("startZ")), new Vector2((int) v.get("endX"), (int) v.get("endZ")), tmp.get((String) v.get("level")), (String) v.get("level"), Double.parseDouble(v.get("price").toString()), (String) v.get("owner"), (Map<String, Object>)v.getOrDefault("options", new HashMap<String, Object>()),
						invitee));
			});
		}catch(FileNotFoundException e){
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public int addLand(Vector2 start, Vector2 end, Level level, double price, String owner){
		lands.put(landId, new Land(landId, start, end, level, level.getFolderName(), price, owner, new HashMap<String, Object>(){{
			put("pvp", false);
			put("pickup", true);
			put("access", true);
			put("hide", false);
		}}, new ArrayList<String>()));
		return landId++;
	}

	@Override
	public boolean removeLand(int id){
		if(lands.containsKey(id)){
			lands.remove(id);
			return true;
		}
		return false;
	}

	@Override
	public Land getLand(int id){
		if(lands.containsKey(id)){
			return lands.get(id);
		}
		return null;
	}

	@Override
	public Land findLand(Position pos){
		for(Land land : lands.values()){
			if(land.check(pos)) return land;
		}
		return null;
	}

	@Override
	public boolean canUpdate(Position pos){
		String owner = "";

		int i = 0;
		Land[] landList = new Land[4];
		for(Land land : lands.values()){
			Vector2 start = land.getStart();
			Vector2 end = land.getEnd();
			if(pos.level == land.getLevel()
				&& (start.x - 1 <= pos.x && pos.x <= end.x + 1)
				&& (start.y - 1 <= pos.z && pos.z <= end.y + 1)){
				if(owner.equals("")){
					owner = land.getOwner();

					landList[i++] = land;
					continue;
				}else if(!owner.equals(land.getOwner())){
					return false;
				}
			}
		}
	
		if(i == 0) return true;
		else if(i == 1){
			Land land = landList[0];

			Vector2 start = land.getStart();
			Vector2 end = land.getEnd();

			return !(((pos.x - 1 <= start.x && start.x <= pos.x + 1) 
				|| (pos.x - 1 <= end.x && end.x <= pos.x + 1))
				|| ((pos.z - 1 <= start.y && start.y <= pos.z + 1)
				|| (pos.z - 1 <= end.y && end.y <= pos.z + 1)));				
		}else{
			for(i = 0; i < landList.length; i++){
				Land land = landList[i];
				if(land == null) return false;

				if(land.check(pos)) return true;
			}
			return false;
		}
	}
	
	@Override
	public Land checkOverlap(Position start, Position end){
		for(Land land : lands.values()){
			if(land.check(start) || land.check(end)) return land;
		}
		return null;
	}
	
	@Override
	public Map<Integer, Land> getAll(){
		return new HashMap<Integer, Land>(lands);
	}
	
	@Override
	public boolean addInvitee(int id, String player){
		if(this.lands.containsKey(id)){
			this.lands.get(id).addInvitee(player);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeInvitee(int id, String player){
		if(this.lands.containsKey(id)){
			this.lands.get(id).removeInvitee(player);
			return true;
		}
		return false;
	}
	
	@Override
	public List<String> getInvitee(int id){
		if(this.lands.containsKey(id)){
			return this.lands.get(id).getInvitee();
		}
		return null;
	}
	
	@Override
	public boolean setOwner(int id, String player){
		if(this.lands.containsKey(id)){
			this.lands.get(id).setOwner(player);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean setOption(int id, String option, Object value){
		if(this.lands.containsKey(id)){
			this.lands.get(id).setOption(option, (boolean)value);
			return true;
		}
		return false;
	}

	@SuppressWarnings("serial")
	@Override
	public void save(){
		HashMap<Integer, LinkedHashMap<String, Object>> saves = new LinkedHashMap<Integer, LinkedHashMap<String, Object>>();
		lands.values().forEach((land) -> {
			saves.put(land.getId(), new LinkedHashMap<String, Object>(){
				{
					Vector2 start = land.getStart();
					put("startX", (int) start.x);
					put("startZ", (int) start.y);
					
					Vector2 end = land.getEnd();
					put("endX", (int) end.x);
					put("endZ", (int) end.y);
					
					put("level", land.getLevelName());
					
					put("price", land.getPrice());
					put("owner", land.getOwner());
					
					put("invitee", land.getInvitee());
					
					/*Map<String, Boolean> options = land.getOptions();
					
					Map<String, Object> defaults = new HashMap<String, Object>(){
						{
							put("pvp", false);
							put("pickup", true);
							put("access", true);
							put("hide", false);
						}
					};
					for(String option : new String[]{"pvp", "pickup", "access", "hide"}){
						Object val = options.get(option);
						
						if(val == null || val.equals(defaults.get(option))){
							options.remove(option);
						}
					}*/
					put("options", land.getOptions());
				}
			});
		});
		
		try{
			DumperOptions option = new DumperOptions();
			option.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			
			Yaml yaml = new Yaml(option);
			Utils.writeFile(file, yaml.dump(saves));
			
			Map<String, Object> map = new HashMap<>();
			map.put("landId", landId);
			
			String content = new GsonBuilder().create().toJson(map);

			Utils.writeFile(new File(plugin.getDataFolder(), "LandData.json"), content);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public void close(){
		this.save();
	}
}
