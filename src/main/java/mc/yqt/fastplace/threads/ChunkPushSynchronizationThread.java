package mc.yqt.fastplace.threads;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;

import mc.yqt.fastplace.AsyncThreadManager;
import mc.yqt.fastplace.util.Reflect;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import net.minecraft.server.v1_8_R3.WorldServer;

public class ChunkPushSynchronizationThread extends Thread {

	private AsyncThreadManager manager;
	private ChunkProviderServer provider;
	private volatile boolean stopped = false;
	private AtomicInteger updatedNum = new AtomicInteger(0);
	private ConcurrentHashMap<Long, Chunk> toSave = new ConcurrentHashMap<>();
	
	public ChunkPushSynchronizationThread(AsyncThreadManager manager, org.bukkit.World world) {
		super("FastPlace Chunk Saving");
		this.manager = manager;
		
		try {
			// get chunk provider server
			WorldServer nmsWorld = ((CraftWorld) world).getHandle();
			this.provider = (ChunkProviderServer) Reflect.getPrivateField("chunkProviderServer", nmsWorld.getClass(), nmsWorld);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true) {
			// update number of changed chunks
			manager.updateTotalModifiedChunks(updatedNum.getAndSet(0));
			
			// iterate through hashmap and save all the entries
			Iterator<Entry<Long, Chunk>> it = toSave.entrySet().iterator();
			
			while(it.hasNext()) {
				// despite doing pretty much everything else thread safe
				// I decided to fuck around with this map without protection
				Entry<Long, Chunk> next = it.next();
				next.getValue().mustSave = true;
				provider.chunks.put(next.getKey(), next.getValue());
				it.remove();
			}
			
			Bukkit.broadcastMessage("updated chunks: " + manager.getTotalChunkUpdates());
			
			if(stopped)
				break;
		}
	}
	
	/**
	 * Adds the chunk to the saving queue.
	 * @param x
	 * @param z
	 * @param chunk
	 */
	public void add(int x, int z, Chunk chunk) {
		updatedNum.incrementAndGet();
		toSave.put(LongHash.toLong(x, z), chunk);
	}
	
	public void end() {
		synchronized(this) {
			stopped = true;
		}
	}
}
