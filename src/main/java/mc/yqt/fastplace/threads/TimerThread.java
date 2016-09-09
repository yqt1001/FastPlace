package mc.yqt.fastplace.threads;

import org.bukkit.Bukkit;

import mc.yqt.fastplace.FastPlace;

/**
 * Quite literally the simplest thread there is.
 * I don't trust bukkit enough to count 1 second for me.
 * 
 */
public class TimerThread extends Thread {

	private FastPlace plugin;
	
	public TimerThread(FastPlace plugin) {
		super("FastPlace Timer");
		this.setPriority(Thread.MAX_PRIORITY);
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		try {
			sleep(1000);
			plugin.stop();
		} catch (InterruptedException e) {
			Bukkit.broadcastMessage("interrupted?");
			plugin.stop();
		}
	}
}