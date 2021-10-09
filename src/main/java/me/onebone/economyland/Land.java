package me.onebone.economyland;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector2;

public class Land{
	private int id;
	private Vector2 start, end;
	private Level level;
	private String levelName;
	private double price;
	private String owner;
	
	private Map<String, Boolean> options;
	private List<String> invitee;
	
	public Land(int id, Vector2 start, Vector2 end, Level level, String levelName, double price, String owner, 
			Map<String, Object> options, List<String> invitee){
		this.id = id;
		
		this.price = price;
		this.owner = owner;
		
		this.invitee = new ArrayList<String>(invitee);
		
		start = start.floor();
		end = end.floor();
		
		double startX = start.x, endX = end.x;
		double startZ = start.y, endZ = end.y;
		
		if(start.x > end.x){
			startX = end.x;
			endX = start.x;
		}
		
		if(start.y > end.y){
			startZ = end.y;
			endZ = start.y;
		}
		
		this.start = new Vector2(startX, startZ);
		this.end = new Vector2(endX, endZ);
		this.level = level;
		this.levelName = levelName;
		
		this.options = new HashMap<String, Boolean>(){{
			put("pvp", (boolean)options.getOrDefault("pvp", false));
			put("pickup", (boolean)options.getOrDefault("pickup", true));
			put("access", (boolean)options.getOrDefault("access", true));
			put("hide", (boolean)options.getOrDefault("hide", false));
		}};
	}
	
	public boolean check(Position pos){
		return pos.level == this.level
				&& (this.start.x <= pos.getFloorX() && pos.getFloorX() <= this.end.x)
				&& (this.start.y <= pos.getFloorZ() && pos.getFloorZ() <= this.end.y);
	}
	
	public boolean hasPermission(Player player){
		return this.hasPermission(player.getName());
	}
	
	public boolean hasPermission(String player){
		player = player.toLowerCase();
		
		return player.equals(this.owner.toLowerCase()) || this.invitee.contains(player);
	}
	
	public void setOwner(String player){
		this.owner = player;
	}
	
	public Vector2 getStart(){
		return new Vector2(start.x, start.y);
	}
	
	public Vector2 getEnd(){
		return new Vector2(end.x, end.y);
	}
	
	public int getWidth(){
		return (int) ((Math.abs(Math.floor(end.x) - Math.floor(start.x)) + 1) * (Math.abs(Math.floor(end.y) - Math.floor(start.y)) + 1));
	}
	
	public Level getLevel(){
		return this.level;
	}
	
	public String getLevelName(){
		return this.levelName;
	}
	
	public double getPrice(){
		return this.price;
	}
	
	public String getOwner(){
		return this.owner;
	}
	
	public List<String> getInvitee(){
		return this.invitee;
	}
	
	public void addInvitee(String player){
		player = player.toLowerCase();
		
		this.invitee.add(player);
	}
	
	public void setOption(String option, Boolean value){
		this.options.put(option, value);
	}
	
	public void removeInvitee(String player){
		player = player.toLowerCase();
		
		this.invitee.remove(player);
	}
	
	public Map<String, Boolean> getOptions() {
		return this.options;
	}
	
	public int getId(){
		return this.id;
	}
	
	//@SuppressWarnings("unchecked")
	public boolean getOption(String key, Boolean defaultValue) {
		return this.options.getOrDefault(key, defaultValue);
	}
}
