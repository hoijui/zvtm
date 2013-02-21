package fr.inria.zvtm.scratch;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import zz.utils.cache.MRUBuffer;


/**
 * A cache of arbitrary data. It maintains a certain amount of data
 * in memory, and offloads the least recently used items to disk.
 * The implementation of serialization and deserialization operations
 * is left to subclasses.
 * @author gpothier
 */
public abstract class DiskCache<K, V> extends MRUBuffer<K, V> {
	private final FileChannel itsChannel;
	private long itsCurrentOffset = 0;
	private TObjectLongHashMap<K> itsOffsetsMap = new TObjectLongHashMap<K>(
			Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR,
			-1);

	// For statistics
	private int itsDropCount;
	private int itsDumpCount;
	private int itsFetchCount;
	
	private int itsCurrentMemorySize = 0;
	private final int itsMaxMemorySize;
	
	private ByteBuffer itsBuffer;
	private int itsBufferSize;
	
	public DiskCache(File cachePath) {
		this(cachePath, 32*1024*1024);
	}
	
	/**
	 * @param memorySize The size of the in-memory cache in bytes.
	 */
	public DiskCache(File cachePath, int memorySize) {
		super(0); // We handle the size through shouldDrop
		try {
			RandomAccessFile file = new RandomAccessFile(cachePath, "rw");
			itsChannel = file.getChannel();
			itsMaxMemorySize = memorySize;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the size in bytes of the storage required for the value
	 * corresponding to the given key.
	 */
	protected abstract int getStorageSize(K aKey);
	
	/**
	 * Returns the size in bytes of the memory required for the value
	 * corresponding to the given key, once it has been loaded from disk.
	 * This might or might not be the same as the storage size.
	 * It might also be slightly approximated to account for pointers
	 * and object headers.
	 */
	protected abstract int getMemorySize(V aValue);
	
	/**
	 * Indicates whether the specified value is dirty, 
	 * ie. needs to be saved to disk when ejected from memory
	 */
	protected abstract boolean isDirty(V aValue);
	
	/**
	 * Loads the value corresponding to the specified key, using the
	 * bytes from the specified buffer.
	 */
	protected abstract V load(K aKey, ByteBuffer aBuffer);
	
	/**
	 * Saves the given value into the specified buffer.
	 */
	protected abstract void save(V aValue, ByteBuffer aBuffer);
	
	private ByteBuffer getBuffer(int size) {
		if (itsBuffer == null || itsBufferSize < size) {
			itsBufferSize = size*2;
			itsBuffer = ByteBuffer.allocate(itsBufferSize);
		}
		return itsBuffer;
	}
	
	protected synchronized V load(K aKey) {
		long offset = itsOffsetsMap.get(aKey);
		if (offset < 0) throw new RuntimeException("Key was never saved: "+aKey);
		
		int size = getStorageSize(aKey);
		ByteBuffer buffer = getBuffer(size);
		buffer.clear();
		buffer.limit(size);

		try {
			itsChannel.position(offset);
			while (size > 0) {
				int s = itsChannel.read(buffer);
				if (s == -1) throw new RuntimeException("Could not read all required bytes");
				size -= s;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		buffer.rewind();
		return load(aKey, buffer);
	}
	
	protected synchronized void save(V aValue) {
		K key = getKey(aValue);
		int size = getStorageSize(key);
		long offset = itsOffsetsMap.get(key);
		
		if (offset < 0) {
			offset = itsCurrentOffset;
			itsOffsetsMap.put(key, offset);
			itsCurrentOffset += size;
		}
		
		ByteBuffer buffer = getBuffer(size);
		buffer.clear();
		buffer.limit(size);
		
		save(aValue, buffer);
		
		buffer.rewind();
		try {
			itsChannel.position(offset);
			itsChannel.write(buffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected boolean shouldDrop(int aCachedItems) {
		return itsCurrentMemorySize >= itsMaxMemorySize;
	}
	
	@Override
	protected V fetch(K aKey) {
		itsFetchCount++;
		return load(aKey);
	}
	
	@Override
	protected void added(V aValue) {
		itsCurrentMemorySize += getMemorySize(aValue);
	}
	
	@Override
	protected void dropped(V aValue) {
		itsDropCount++;
		if (isDirty(aValue)) {
			save(aValue);
			itsDumpCount++;
		}
		itsCurrentMemorySize -= getMemorySize(aValue);
	}
	
	public int getDropCount() {
		return itsDropCount;
	}
	
	public int getDumpCount() {
		return itsDumpCount;
	}
	
	public int getFetchCount() {
		return itsFetchCount;
	}
	
	public void resetStats() {
		itsDropCount = 0;
		itsDumpCount = 0;
		itsFetchCount = 0;
	}
}