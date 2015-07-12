package com.memoryrest.memory;

final class TransactionWithQueryCapsule extends Capsule<TransactionWithQuery> {

	final void justExecute(final TransactionWithQuery transaction,
			final StringBuilder prevalentSystem) {
		try {
			_queryResult = transaction.executeAndQuery(prevalentSystem);
		} catch (final RuntimeException rx) {
			_queryException = rx;
			throw rx;
		} catch (final Exception ex) {
			_queryException = ex;
		}
	}

	final StringBuilder result() throws Exception {
		if (_queryException != null)
			throw _queryException;
		return _queryResult;
	}

	final Capsule<TransactionWithQuery> cleanCopy() {
		return new TransactionWithQueryCapsule(_serialized);
	}

	TransactionWithQueryCapsule(final TransactionWithQuery transactionWithQuery) {
		super(transactionWithQuery);
	}

	TransactionWithQueryCapsule(final byte[] serialized) {
		super(serialized);
	}

	transient StringBuilder _queryResult;
	transient Exception _queryException;

}