package com.memoryrest.memory;

final class TransactionGuide  {

	final void checkSystemVersion(final long expectedSystemVersion) {
		if (_transactionTimestamp._systemVersion != expectedSystemVersion)
			throw new IllegalStateException("Attempted to process "
					+ _transactionTimestamp._systemVersion + " when ready for "
					+ expectedSystemVersion);
	}

	final void writeTo(final java.io.OutputStream stream)
			throws java.io.IOException {
		Chunking.writeChunk(stream, _transactionTimestamp.toChunk());
	}

	TransactionGuide(final TransactionTimestamp transactionTimestamp,
			final Turn pipelineTurn) {
		_turn = pipelineTurn;
		_transactionTimestamp = transactionTimestamp;
	}

	final Turn _turn;
	final TransactionTimestamp _transactionTimestamp;

}
