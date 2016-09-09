package mc.yqt.fastplace;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.World;

import mc.yqt.fastplace.threads.ChunkModificationThread;
import mc.yqt.fastplace.threads.ChunkPushSynchronizationThread;

/**
 * Class that manages the threads and maintains chunk order.
 * 
 */
public class AsyncThreadManager {

	private World world;
	private WrappedChunkSection sections;
	
	private ArrayList<ChunkModificationThread> threads = new ArrayList<>();
	private ChunkPushSynchronizationThread chunkSavingThread;
	
	private AtomicInteger xChunk = new AtomicInteger(0);
	private AtomicInteger yChunk = new AtomicInteger(0);
	private AtomicInteger totalChunkUpdates = new AtomicInteger(0);
	
	public AsyncThreadManager(World world, WrappedChunkSection sections) {
		this.world = world;
		this.sections = sections;
	}
	
	/**
	 * Starts the threads for chunk modification.
	 */
	public void startThreads() {
		// start ChunkPushSunchronizationThread
		chunkSavingThread = new ChunkPushSynchronizationThread(this, world);
		chunkSavingThread.start();
		
		// start ChunkModificationThread threads
		int numThreads = Runtime.getRuntime().availableProcessors() - 1;
		if(numThreads < 1)
			numThreads = 1;
		
		for(int i = 0; i < numThreads; i++) {
			ChunkModificationThread thread = new ChunkModificationThread(this, sections, world, i + 1);
			threads.add(thread);
			thread.start();
		}
		
		Bukkit.broadcastMessage("num of threads: " + numThreads);
		
	}
	
	/**
	 * Stops the threads.
	 * @return The amount of chunks and blocks modified as a broadcastable string.
	 */
	public String stop() {
		// stop threads
		threads.forEach(t -> t.end());
		chunkSavingThread.end();
		
		return "total chunk updates: " + totalChunkUpdates.get();
	}
	
	/**
	 * Updates and pulls the synchronized chunk locations for the new chunk to be located.
	 * @return An int array with two elements. First element is x and second is z positions.
	 */
	public int[] updateAndPullnewChunkLocations() {
		int x, z;
		synchronized(this) {
			x = xChunk.getAndIncrement();
			z = yChunk.get();
			if(z > 1000)
				z = 0;
			else
				z++;
			yChunk.set(z);
		}
		
		return new int[] {x,z};
	}
	
	/**
	 * Adds the number of new chunks to the total chunk count.
	 * @param newOnes
	 */
	public void updateTotalModifiedChunks(int newOnes) {
		totalChunkUpdates.addAndGet(newOnes);
	}
	
	public ChunkPushSynchronizationThread getSavingThread() {
		return chunkSavingThread;
	}
	
	public int getTotalChunkUpdates() {
		return totalChunkUpdates.get();
	}
	
}
