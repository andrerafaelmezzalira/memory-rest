package com.memoryrest.memory;


final class StrictTransactionCensor  {

	final void approve(final TransactionTimestamp transactionTimestamp)
			throws RuntimeException, Error, Exception {
		try {
			royalFoodTaster(transactionTimestamp._systemVersion - 1).receive(
					transactionTimestamp.cleanCopy());
		} catch (final RuntimeException rx) {
			_royalFoodTaster = null;
			throw rx;
		} catch (final Error error) {
			_royalFoodTaster = null;
			throw error;
		}
	}

	final PrevalentSystemGuard royalFoodTaster(final long systemVersion) {
		if (_royalFoodTaster == null)
			produceNewFoodTaster(systemVersion);
		return _royalFoodTaster;
	}

	final void produceNewFoodTaster(final long systemVersion) {
		try {
			_royalFoodTaster = _king.deepCopy(systemVersion);
		} catch (final Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(
					"Unable to produce a copy of the prevalent system for trying out transactions before applying them to the real system.");
		}
	}

	StrictTransactionCensor(PrevalentSystemGuard prevalentSystemGuard) {
		_king = prevalentSystemGuard;
	}

	final PrevalentSystemGuard _king;
	PrevalentSystemGuard _royalFoodTaster;

}