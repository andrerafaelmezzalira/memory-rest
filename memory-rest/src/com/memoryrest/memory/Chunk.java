package com.memoryrest.memory;


final class Chunk  {

	Chunk(final byte[] bytes) {
		_bytes = bytes;
	}

	final byte[] _bytes;
	long _systemVersion;
	boolean _withQuery;

}