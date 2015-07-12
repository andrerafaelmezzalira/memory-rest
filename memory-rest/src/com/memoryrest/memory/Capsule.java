package com.memoryrest.memory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

abstract class Capsule<C extends Serializable>  {

	final C deserialize() {
		try {
			return JavaSerializer.readObject(new java.io.ByteArrayInputStream(
					_serialized));
		} catch (final Exception exception) {
			throw new Error("Unable to deserialize transaction", exception);
		}
	}

	final void executeOn(
			final StringBuilder prevalentSystem) throws Exception {
		synchronized (prevalentSystem) {
			justExecute(deserialize(), prevalentSystem);
		}
	}

	final Chunk toChunk() {
		final Chunk chunk = new Chunk(_serialized);
		chunk._withQuery = this instanceof TransactionWithQueryCapsule;
		return chunk;
	}

	static final Capsule<? extends Serializable> fromChunk(final Chunk chunk) {
		return chunk._withQuery ? new TransactionWithQueryCapsule(
				chunk._bytes) : new TransactionCapsule(chunk._bytes);
	}

	Capsule(final Serializable transaction) {
		try {
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			JavaSerializer.writeObject(bytes, transaction);
			_serialized = bytes.toByteArray();
		} catch (final Exception exception) {
			throw new Error("Unable to serialize transaction", exception);
		}
	}

	abstract void justExecute(
			final C transaction,
			final StringBuilder prevalentSystem) throws Exception;

	abstract Capsule<C> cleanCopy();

	Capsule(final byte[] serialized) {
		_serialized = serialized;
	}

	final byte[] _serialized;
	
}