package mc.yqt.fastplace;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mc.yqt.fastplace.threads.TimerThread;
import net.md_5.bungee.api.ChatColor;

public class FastPlace extends JavaPlugin {
	
	private volatile AsyncThreadManager threadManager;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// only one command, start the placing
		if(cmd.getName().equalsIgnoreCase("place") && sender instanceof Player) {
			if(threadManager != null) {
				sender.sendMessage(ChatColor.RED + "Currently a fast place in progress!");
				return true;
			}
			
			start(((Player) sender).getWorld());
			return true;
		}
		
		return false;
	}
	
	/**
	 * Starts the placing.
	 * @param world to place the blocks in.
	 */
	public void start(World world) {
		// start timing
		new TimerThread(this).start();
		
		// load threads
		threadManager = new AsyncThreadManager(world, new WrappedChunkSection());
		threadManager.startThreads();
	}
	
	/**
	 * Stops the placing.
	 * Called from the asynchronous timer thread.
	 */
	public void stop() {
		Bukkit.broadcastMessage("timer stopped");
		Bukkit.broadcastMessage(threadManager.stop());
		threadManager = null;
	}
	
}
