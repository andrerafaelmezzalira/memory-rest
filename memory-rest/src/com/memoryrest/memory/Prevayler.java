package com.memoryrest.memory;

import java.io.IOException;

public final class Prevayler {

	public final StringBuilder prevalentSystem() {
		return _snapshotManager._recoveredPrevalentSystem._prevalentSystem;
	}

	public final void execute(final Transaction transaction) throws Exception {
		_publisher.publish(new TransactionCapsule(transaction));
	}

	public final StringBuilder execute(final Query sensitiveQuery)
			throws Exception {
		return _snapshotManager._recoveredPrevalentSystem
				.executeQuery(sensitiveQuery);
	}

	public final StringBuilder execute(
			final TransactionWithQuery transactionWithQuery) throws Exception {
		final TransactionWithQueryCapsule capsule = new TransactionWithQueryCapsule(
				transactionWithQuery);
		_publisher.publish(capsule);
		return capsule.result();
	}

	public final void close() throws IOException {
		_publisher.close();
	}

	{
		_snapshotManager = new GenericSnapshotManager();

	}

	public Prevayler() throws IOException, ClassNotFoundException, Exception {
		final PrevalentSystemGuard guard = _snapshotManager._recoveredPrevalentSystem;
		_publisher = new CentralPublisher(guard);
		guard.subscribeTo(_publisher);

		new Thread() {
			public final void run() {
				while (true)
					try {
//						'1000 * 60 * 60 * 1);
						Thread.sleep(1000 * 60);
						_snapshotManager._recoveredPrevalentSystem
								.takeSnapshot(_snapshotManager);
					} catch (final IOException e) {
						e.printStackTrace();
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
			}
		}.start();
	}

	final GenericSnapshotManager _snapshotManager;
	final CentralPublisher _publisher;

}