package com.memoryrest.memory;

import java.io.File;
import java.io.IOException;

final class GenericSnapshotManager  {

	final void writeSnapshot(
			final StringBuilder prevalentSystem,
			final long version) throws IOException {
		final File tempFile = _directory.createTempFile("snapshot" + version
				+ "temp", "generatingSnapshot");

		writeSnapshot(prevalentSystem, tempFile);

		final File permanent = _directory.snapshotFile(version);
		permanent.delete();
		if (!tempFile.renameTo(permanent))
			throw new IOException("Temporary snapshot file generated: "
					+ tempFile + "\nUnable to rename it permanently to: "
					+ permanent);
	}

	final void writeSnapshot(
			final StringBuilder prevalentSystem,
			final File snapshotFile) throws IOException {
		final java.io.OutputStream out = new java.io.FileOutputStream(
				snapshotFile);
		try {
			JavaSerializer
					.writeObject(
							out,
							(StringBuilder) prevalentSystem);
		} finally {
			out.close();
		}
	}

	final StringBuilder readSnapshot(
			final File snapshotFile) throws ClassNotFoundException, IOException {

		final java.io.InputStream in = new java.io.FileInputStream(snapshotFile);
		try {
			return JavaSerializer.readObject(in);
		} finally {
			in.close();
		}
	}

	{
		_directory = new PrevaylerDirectory();
	}
	
	GenericSnapshotManager() throws IOException, ClassNotFoundException {

		_directory.produceDirectory();

		final File latestSnapshot = _directory.latestSnapshot();

		long recoveredVersion = 0;
		final StringBuilder recoveredPrevalentSystem;

		if (latestSnapshot != null) {
			recoveredVersion = PrevaylerDirectory
					.snapshotVersion(latestSnapshot);
			recoveredPrevalentSystem = readSnapshot(latestSnapshot);
		} else
			recoveredPrevalentSystem = new StringBuilder();

		_recoveredPrevalentSystem = new PrevalentSystemGuard(
				recoveredPrevalentSystem, recoveredVersion);
	}

	final PrevaylerDirectory _directory;
	final PrevalentSystemGuard _recoveredPrevalentSystem;

}