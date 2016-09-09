package mc.yqt.fastplace;

import mc.yqt.fastplace.util.Reflect;
import net.minecraft.server.v1_8_R3.ChunkSection;

/**
 * Represents wrapped chunk sections for one chunk full of stone blocks.
 * Exactly one is created per run of fast place and duplicated a lot.
 * 
 */
public class WrappedChunkSection {

	private ChunkSection sections[] = new ChunkSection[16];
	
	public WrappedChunkSection() {
		for(int i = 0; i < 16; i++) {
			// create chunk section
			ChunkSection section = new ChunkSection(i, true);
			sections[i] = section;
			
			try {
				// get char array
				char blocks[] = (char[]) Reflect.getPrivateField("blockIds", section.getClass(), section);
				
				// fill in chunk section
				for(int x = 0; x < 16; x++)
					for(int y = 0; y < 16; y++)
						for(int z = 0; z < 16; z++)
							blocks[z << 8 | y << 4 | x] = (char) 1;
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public ChunkSection[] getWrapped() {
		return sections;
	}
}
