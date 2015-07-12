package com.memoryrest.memory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;

final class PrevalentSystemGuard  {

	final void subscribeTo(final CentralPublisher publisher)
			throws IOException, ClassNotFoundException, Exception {
		final long initialTransaction;
		synchronized (this) {
			_ignoreRuntimeExceptions = true;
			initialTransaction = _systemVersion + 1;
		}

		publisher.subscribe(this, initialTransaction);

		synchronized (this) {
			_ignoreRuntimeExceptions = false;
		}
	}

	final void receive(final TransactionTimestamp transactionTimestamp) throws Exception {
		final long systemVersion = transactionTimestamp._systemVersion;

		synchronized (this) {
			if (_prevalentSystem == null)
				throw new Error(
						"Prevayler is no longer processing transactions due to an Error thrown from an earlier transaction.");

			if (systemVersion != _systemVersion + 1)
				throw new IllegalStateException(
						"Attempted to apply transaction " + systemVersion
								+ " when prevalent system was only at "
								+ _systemVersion);

			_systemVersion = systemVersion;

			try {
				transactionTimestamp._capsule.executeOn(_prevalentSystem);
			} catch (final RuntimeException rx) {
				if (!_ignoreRuntimeExceptions)
					throw rx;
			} catch (final Error error) {
				throw error;
			} finally {
				notifyAll();
			}
		}
	}

	final StringBuilder executeQuery(final Query sensitiveQuery) throws Exception {
		synchronized (this) {
			if (_prevalentSystem == null)
				throw new Error(
						"Prevayler is no longer processing queries due to an Error thrown from an earlier transaction.");

			synchronized (_prevalentSystem) {
				return sensitiveQuery.query(_prevalentSystem);
			}
		}
	}

	final void takeSnapshot(final GenericSnapshotManager snapshotManager)
			throws IOException {
		synchronized (this) {
			if (_prevalentSystem == null)
				throw new Error(
						"Prevayler is no longer allowing snapshots due to an Error thrown from an earlier transaction.");

			synchronized (_prevalentSystem) {
				snapshotManager.writeSnapshot(_prevalentSystem, _systemVersion);
			}
		}
	}

	final PrevalentSystemGuard deepCopy(final long systemVersion)
			throws IOException, ClassNotFoundException {
		synchronized (this) {
			while (_systemVersion < systemVersion && _prevalentSystem != null)
				try {
					this.wait();
				} catch (final InterruptedException e) {
					throw new RuntimeException(
							"Unexpected Exception was thrown.", e);
				}

			if (_prevalentSystem == null)
				throw new Error(
						"Prevayler is no longer accepting transactions due to an Error thrown from an earlier transaction.");

			if (_systemVersion > systemVersion)
				throw new IllegalStateException("Already at " + _systemVersion
						+ "; can't go back to " + systemVersion);

			synchronized (_prevalentSystem) {
				return new PrevalentSystemGuard(PrevalentSystemGuard
						.deepCopyParallel(_prevalentSystem), _systemVersion);
			}
		}
	}

	static final StringBuilder deepCopyParallel(
			final StringBuilder original)
			throws IOException, ClassNotFoundException {
		final PipedOutputStream outputStream = new PipedOutputStream();
		final Receiver receiver = new Receiver(new java.io.PipedInputStream(
				outputStream));

		try {
			JavaSerializer
					.writeObject(
							outputStream,
							(StringBuilder) original);
		} finally {
			outputStream.close();
		}

		return receiver.getResult();
	}

	final static class Receiver extends Thread {

		public final void run() {
			try {
				_result = JavaSerializer.readObject(_inputStream);
			} catch (final IOException e) {
				_ioException = e;
			} catch (final ClassNotFoundException e) {
				_classNotFoundException = e;
			} catch (final RuntimeException e) {
				_runtimeException = e;
			} catch (final Error e) {
				_error = e;
				throw e;
			}

			try {
				while (_inputStream.read() != -1) {
				}
			} catch (final IOException e) {
			}
		}

		final StringBuilder getResult()
				throws ClassNotFoundException, IOException {
			try {
				join();
			} catch (InterruptedException e) {
				throw new RuntimeException("Unexpected InterruptedException", e);
			}

			if (_error != null)
				throw new RuntimeException("Error during deserialization",
						_error);
			if (_runtimeException != null)
				throw _runtimeException;
			if (_classNotFoundException != null)
				throw _classNotFoundException;
			if (_ioException != null)
				throw _ioException;
			if (_result == null)
				throw new RuntimeException("Deep copy failed in an unknown way");

			return _result;
		}

		Receiver(final InputStream inputStream) {
			_inputStream = inputStream;
			start();
		}

		final InputStream _inputStream;
		StringBuilder _result;
		IOException _ioException;
		ClassNotFoundException _classNotFoundException;
		RuntimeException _runtimeException;
		Error _error;
	}

	PrevalentSystemGuard(
			final StringBuilder prevalentSystem,
			final long systemVersion) {
		_prevalentSystem = prevalentSystem;
		_systemVersion = systemVersion;
	}

	final StringBuilder _prevalentSystem;
	long _systemVersion;
	boolean _ignoreRuntimeExceptions;

}