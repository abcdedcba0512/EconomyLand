package me.onebone.economyland;

import cn.nukkit.scheduler.Task;

public class AutoSaveTask extends Task {

	private EconomyLand plugin;

	public AutoSaveTask(EconomyLand plugin){
		this.plugin = plugin;
	}

	@Override
	public void onRun(int currentTick){
		this.plugin.provider.save();
	}
}
