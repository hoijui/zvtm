package fr.inria.zvtm.timeseries.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

import zz.utils.Utils;

public class MappedDiskCache {
	public static final int BLOCK_SIZE = 1*1024*1024;
	
	private final FileChannel itsChannel;
	
	private List<Block> blocks = new ArrayList<Block>();
	
	public MappedDiskCache(File cachePath) {
		try {
			RandomAccessFile file = new RandomAccessFile(cachePath, "rw");
			itsChannel = file.getChannel();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the block that contains the location at the given offset
	 */
	public synchronized Block getBlock(long offset) {
		long lIndex = offset/BLOCK_SIZE;
		if (lIndex >= Integer.MAX_VALUE) throw new RuntimeException("Offset overflow: "+offset);
		int index = (int) lIndex;
		
		// Adjust offset to block boundary
		offset = 1L*index*BLOCK_SIZE;
		
		Block block = Utils.listGet(blocks, index);
		if (block == null) {
			try {
				block = new Block(offset, itsChannel.map(MapMode.READ_WRITE, offset, BLOCK_SIZE));
				Utils.listSet(blocks, index, block);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return block;
	}
	
	public class Block {
		private final long offset;
		private final MappedByteBuffer buffer;
		private final FloatBuffer floatView;
		
		public Block(long offset, MappedByteBuffer buffer) {
			this.offset = offset;
			this.buffer = buffer;
			this.buffer.order(ByteOrder.nativeOrder());
			this.floatView = buffer.asFloatBuffer();
		}

		public long getOffset() {
			return offset;
		}

		public MappedByteBuffer getBuffer() {
			return buffer;
		}
		
		public FloatBuffer getFloatView() {
			return floatView;
		}
	}
}
