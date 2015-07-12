package com.memoryrest.memory;

import java.io.Serializable;


final class TransactionTimestamp  {

	final TransactionTimestamp cleanCopy() {
		return new TransactionTimestamp(_capsule.cleanCopy(), _systemVersion);
	}

	final Chunk toChunk() {
		final Chunk chunk = _capsule.toChunk();
		chunk._systemVersion = _systemVersion;
		return chunk;
	}

	final static TransactionTimestamp fromChunk(final Chunk chunk) {
		return new TransactionTimestamp(Capsule.fromChunk(chunk),
				chunk._systemVersion);
	}

	TransactionTimestamp(final Capsule<? extends Serializable> capsule,
			final long systemVersion) {
		_capsule = capsule;
		_systemVersion = systemVersion;
	}

	final Capsule<? extends Serializable> _capsule;
	final long _systemVersion;

}