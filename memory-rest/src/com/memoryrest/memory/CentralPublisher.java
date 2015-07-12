package com.memoryrest.memory;

import java.io.IOException;
import java.io.Serializable;


final class CentralPublisher  {

	final void publish(final Capsule<? extends Serializable> capsule) throws Exception {
		synchronized (_pendingPublicationsMonitor) {
			_pendingPublications++;
		}

		try {
			publishWithoutWorryingAboutNewSubscriptions(capsule);
		} finally {
			synchronized (_pendingPublicationsMonitor) {
				_pendingPublications--;
				if (_pendingPublications == 0)
					_pendingPublicationsMonitor.notifyAll();
			}
		}
	}

	final synchronized void notifySubscribers(
			final TransactionTimestamp transactionTimestamp) throws Exception {
		for (final PrevalentSystemGuard prevalentSystemGuard : _subscribers)
			prevalentSystemGuard.receive(transactionTimestamp);
	}

	final void publishWithoutWorryingAboutNewSubscriptions(
			final Capsule<? extends Serializable> capsule) throws Exception {
		final TransactionGuide guide = approve(capsule);
		_journal.append(guide);
		notifySubscribers(guide);
	}

	final TransactionGuide approve(final Capsule<? extends Serializable> capsule) throws RuntimeException, Error, Exception {
		synchronized (_nextTurnMonitor) {
			final TransactionTimestamp timestamp = new TransactionTimestamp(
					capsule, _nextTransaction);
			_censor.approve(timestamp);

			final Turn turn = _nextTurn;
			_nextTurn = _nextTurn.next();
			_nextTransaction++;

			return new TransactionGuide(timestamp, turn);
		}
	}

	final void notifySubscribers(final TransactionGuide guide) throws Exception {
		guide._turn.start();
		try {
			notifySubscribers(guide._transactionTimestamp);
		} finally {
			guide._turn.end();
		}
	}

	final void subscribe(final PrevalentSystemGuard subscriber,
			final long initialTransaction) throws Exception {
		synchronized (_pendingPublicationsMonitor) {
			while (_pendingPublications != 0)
				try {
					_pendingPublicationsMonitor.wait();
				} catch (final InterruptedException e) {
					throw new RuntimeException(
							"Unexpected Exception was thrown.", e);
				}

			_journal.update(subscriber, initialTransaction);

			synchronized (_nextTurnMonitor) {
				_nextTransaction = _journal.nextTransaction();
			}

			_subscribers.add(subscriber);
		}
	}

	final void close() throws IOException {
		_journal.close();
	}

	{
		_nextTurnMonitor = new Object();
		_pendingPublicationsMonitor = new Object();
		_subscribers = new java.util.LinkedList<PrevalentSystemGuard>();
		_nextTurn = Turn.first();
		_journal = new PersistentJournal();
	}
	
	CentralPublisher(final PrevalentSystemGuard guard) throws IOException {
		_censor = new StrictTransactionCensor(guard);
	}

	
	final java.util.List<PrevalentSystemGuard> _subscribers;
	final StrictTransactionCensor _censor;
	final PersistentJournal _journal;
	final Object _pendingPublicationsMonitor;
	final Object _nextTurnMonitor;
	volatile int _pendingPublications;
	long _nextTransaction;
	Turn _nextTurn;
}