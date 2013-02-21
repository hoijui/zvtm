package fr.inria.zvtm.scratch;

import java.nio.FloatBuffer;


/**
 * A cache of data chunks.
 * @author gpothier
 */
public interface IChunkCache {
	/**
	 * Returns the {@link ChunkData} object for the specified chunk
	 */
	public ChunkData getChunkData(int seriesId, int scale, int offset);

	/**
	 * Represents the data for a given chunk.
	 *  
	 * @author gpothier
	 */
	public static class ChunkData {
		public final int offset;
		public final FloatBuffer buffer;
		
		public ChunkData(int offset, FloatBuffer buffer) {
			this.offset = offset;
			this.buffer = buffer;
		}
	}
}
