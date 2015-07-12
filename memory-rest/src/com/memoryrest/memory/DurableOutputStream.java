package com.memoryrest.memory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


final class DurableOutputStream  {

	final void sync(final TransactionGuide guide) throws IOException {
		final int thisWrite;
		guide._turn.start();
		try {
			thisWrite = writeObject(guide);
		} finally {
			guide._turn.end();
		}

		waitUntilSynced(thisWrite);
	}

	final int writeObject(final TransactionGuide guide) throws IOException {
		synchronized (_writeLock) {
			if (_closed)
				throw new IOException("already closed");
			try {
				guide.writeTo(_active);
			} catch (final IOException exception) {
				internalClose();
				throw exception;
			}
			_objectsWritten++;
			return _objectsWritten;
		}
	}

	final void waitUntilSynced(final int thisWrite) throws IOException {

		synchronized (_syncLock) {
			if (_objectsSynced < thisWrite) {
				int objectsWritten;
				synchronized (_writeLock) {
					if (_closed)
						throw new IOException("already closed");

					final ByteArrayOutputStream swap = _active;
					_active = _inactive;
					_inactive = swap;
					objectsWritten = _objectsWritten;
				}

				try {
					_inactive.writeTo(_fileOutputStream);
					_inactive.reset();
					_fileOutputStream.flush();
					_fileOutputStream.getFD().sync();

				} catch (final IOException exception) {
					internalClose();
					throw exception;
				}

				_objectsSynced = objectsWritten;
				_fileSyncCount++;
			}
		}
	}

	final void close() throws IOException {
		synchronized (_syncLock) {
			synchronized (_writeLock) {
				if (_closed)
					return;

				internalClose();
				_fileOutputStream.close();
			}
		}
	}

	final void internalClose() {
		synchronized (_writeLock) {
			_closed = true;
			_active = null;
			_inactive = null;
		}
	}

	{
		_active = new ByteArrayOutputStream();
		_inactive = new ByteArrayOutputStream();
		_writeLock = new Object();
		_syncLock = new Object();
		
	}
	
	DurableOutputStream(final File file) throws IOException {
		_file = file;
		_fileOutputStream = new FileOutputStream(file);
	}

	final Object _writeLock;
	final Object _syncLock;
	final File _file;
	final FileOutputStream _fileOutputStream;
	ByteArrayOutputStream _active;
	ByteArrayOutputStream _inactive;
	boolean _closed;
	int _objectsWritten;
	int _objectsSynced;
	int _fileSyncCount;

}