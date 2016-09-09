package mc.yqt.fastplace.threads;

import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import mc.yqt.fastplace.AsyncThreadManager;
import mc.yqt.fastplace.WrappedChunkSection;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.World;

/**
 * Duplicates of these threads are started in order to maximize the amount of chunks modified.
 * 
 */
public class ChunkModificationThread extends Thread {

	private AsyncThreadManager manager;
	private World world;
	private ChunkSection[] section;
	private volatile boolean stopped = false;
	
	public ChunkModificationThread(AsyncThreadManager manager, WrappedChunkSection section, org.bukkit.World world, int i) {
		super("FastPlace Chunk Modification #" + i);
		this.manager = manager;
		this.world = ((CraftWorld) world).getHandle();
		this.section = section.getWrapped();
	}
	
	@Override
	public void run() {
		// loop as much as possible
		while(true) {
			if(stopped)
				break;
			
			int chunkCoords[] = manager.updateAndPullnewChunkLocations();
			
			Chunk chunk = new Chunk(world, chunkCoords[0], chunkCoords[1]);
			
			// copy over section
			for(int i = 0; i < 16; i++)
				chunk.getSections()[i] = section[i];
			
			// save the chunk
			manager.getSavingThread().add(chunkCoords[0], chunkCoords[1], chunk);
		}
	}
	
	public void end() {
		synchronized(this) {
			stopped = true;
		}
	}
}
