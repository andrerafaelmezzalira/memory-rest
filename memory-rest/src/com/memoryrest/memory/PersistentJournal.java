package com.memoryrest.memory;

import java.io.File;
import java.io.IOException;


final class PersistentJournal  {

	final void append(final TransactionGuide guide) {
		if (!_nextTransactionInitialized)
			throw new IllegalStateException(
					"Journal.update() has to be called at least once before Journal.append().");

		final DurableOutputStream myOutputJournal;
		DurableOutputStream outputJournalToClose = null;

		guide._turn.start();
		try {
			guide.checkSystemVersion(_nextTransaction);

			if (!isOutputJournalStillValid()) {
				outputJournalToClose = _outputJournal;
				_outputJournal = createOutputJournal(_nextTransaction);
				t0 = System.currentTimeMillis();
			}

			_nextTransaction++;

			myOutputJournal = _outputJournal;
		} finally {
			guide._turn.end();
		}

		try {
			myOutputJournal.sync(guide);
		} catch (final IOException iox) {
			hang();
		}

		guide._turn.start();
		try {
			try {
				if (outputJournalToClose != null)
					outputJournalToClose.close();
			} catch (final IOException iox) {
				hang();
			}
		} finally {
			guide._turn.end();
		}
	}

	final boolean isOutputJournalStillValid() {
		return _outputJournal != null && !isOutputJournalTooBig()
				&& !isOutputJournalTooOld();
	}

	final boolean isOutputJournalTooOld() {
		return _journalAgeThresholdInMillis != 0
				&& (System.currentTimeMillis() - t0) >= _journalAgeThresholdInMillis;
	}

	final boolean isOutputJournalTooBig() {
		return _journalSizeThresholdInBytes != 0
				&& _outputJournal._file.length() >= _journalSizeThresholdInBytes;
	}

	final DurableOutputStream createOutputJournal(final long transactionNumber) {
		try {
			return new DurableOutputStream(_directory
					.journalFile(transactionNumber));
		} catch (final IOException iox) {
			hang();
			return null;
		}
	}

	final void update(final PrevalentSystemGuard subscriber,
			final long initialTransactionWanted) throws Exception {
		final File initialJournal = _directory
				.findInitialJournalFile(initialTransactionWanted);

		if (initialJournal == null) {
			initializeNextTransaction(initialTransactionWanted, 1);
			return;
		}

		initializeNextTransaction(initialTransactionWanted,
				recoverPendingTransactions(subscriber,
						initialTransactionWanted, initialJournal));
	}

	final void initializeNextTransaction(final long initialTransactionWanted,
			final long nextTransaction) throws IOException {
		if (_nextTransactionInitialized) {
			if (_nextTransaction < initialTransactionWanted)
				throw new IOException(
						"The transaction log has not yet reached transaction "
								+ initialTransactionWanted
								+ ". The last logged transaction was "
								+ (_nextTransaction - 1) + ".");
			if (nextTransaction < _nextTransaction)
				throw new IOException(
						"Unable to find journal file containing transaction "
								+ nextTransaction
								+ ". Might have been manually deleted.");
			if (nextTransaction > _nextTransaction)
				throw new IllegalStateException();
			return;
		}
		_nextTransactionInitialized = true;
		_nextTransaction = initialTransactionWanted > nextTransaction ? initialTransactionWanted
				: nextTransaction;
	}

	final long recoverPendingTransactions(
			final PrevalentSystemGuard subscriber,
			final long initialTransaction, final File initialJournal)
			throws Exception {
		long recoveringTransaction = PrevaylerDirectory
				.journalVersion(initialJournal);
		File journal = initialJournal;
		DurableInputStream input = new DurableInputStream(journal);

		while (true) {
			try {
				final Chunk chunk = input.readChunk();
				if (recoveringTransaction >= initialTransaction) {
					final TransactionTimestamp entry = TransactionTimestamp
							.fromChunk(chunk);
					if (entry._systemVersion != recoveringTransaction)
						throw new IOException("Expected "
								+ recoveringTransaction + " but was "
								+ entry._systemVersion);
					subscriber.receive(entry);
				}
				recoveringTransaction++;
			} catch (final java.io.EOFException eof) {
				final File nextFile = _directory
						.journalFile(recoveringTransaction);
				if (journal.equals(nextFile))
					PrevaylerDirectory.renameUnusedFile(journal);
				journal = nextFile;
				if (!journal.exists())
					break;
				input = new DurableInputStream(journal);
			}
		}
		return recoveringTransaction;
	}

	static final void hang() {
		while (true)
			try {
				Thread.sleep(5000);
			} catch (final InterruptedException ignored) {
			}

	}

	final void close() throws IOException {
		if (_outputJournal != null)
			_outputJournal.close();
	}

	final long nextTransaction() {
		if (!_nextTransactionInitialized)
			throw new IllegalStateException(
					"update() must be called at least once");
		return _nextTransaction;
	}

	{
		
		_directory = new PrevaylerDirectory();
		_journalSizeThresholdInBytes = 0;
		_journalAgeThresholdInMillis = 0;
		
	}
	
	PersistentJournal() throws IOException {
		_directory.produceDirectory();
	}

	final long _journalSizeThresholdInBytes;
	final long _journalAgeThresholdInMillis;
	final PrevaylerDirectory _directory;
	DurableOutputStream _outputJournal;
	long t0;
	long _nextTransaction;
	boolean _nextTransactionInitialized;
	
}