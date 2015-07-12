package com.memoryrest.memory;

import java.io.File;
import java.io.IOException;

final class PrevaylerDirectory  {

	final void produceDirectory() throws IOException {
		_directory.mkdirs();
	}

	static final void checkValidSnapshotSuffix(final String suffix) {
		if (!suffix.matches(SNAPSHOT_SUFFIX_PATTERN))
			throw new IllegalArgumentException(
					"Snapshot filename suffix must match /"
							+ SNAPSHOT_SUFFIX_PATTERN + "/, but '" + suffix
							+ "' does not");

	}

	static final void checkValidJournalSuffix(final String suffix) {
		if (!suffix.matches(JOURNAL_SUFFIX_PATTERN))
			throw new IllegalArgumentException(
					"Journal filename suffix must match /"
							+ JOURNAL_SUFFIX_PATTERN + "/, but '" + suffix
							+ "' does not");

	}

	final File snapshotFile(final long version) {
		checkValidSnapshotSuffix("snapshot");
		return file(version, "snapshot");
	}

	final File journalFile(final long transaction) {
		checkValidJournalSuffix("journal");
		return file(transaction, "journal");
	}

	final File file(final long version, final String suffix) {
		final String fileName = "0000000000000000000" + version;
		return new File(_directory, fileName.substring(fileName.length() - 19)
				+ "." + suffix);
	}

	static final long snapshotVersion(File file) {
		return version(file, SNAPSHOT_FILENAME_PATTERN);
	}

	static final long journalVersion(final File file) {
		return version(file, JOURNAL_FILENAME_PATTERN);
	}

	static final long version(final File file, final String filenamePattern) {
		final String fileName = file.getName();
		if (!fileName.matches(filenamePattern))
			return -1;
		return Long.parseLong(fileName.substring(0, fileName.indexOf(".")));
	}

	final File latestSnapshot() throws IOException {
		final File[] files = _directory.listFiles();
		if (files == null)
			throw new IOException("Error reading file list from directory "
					+ _directory);

		File latestSnapshot = null;
		long latestVersion = 0;
		for (final File candidateSnapshot : files) {
			final long candidateVersion = snapshotVersion(candidateSnapshot);
			if (candidateVersion > latestVersion) {
				latestVersion = candidateVersion;
				latestSnapshot = candidateSnapshot;
			}
		}
		return latestSnapshot;
	}

	final File findInitialJournalFile(long initialTransactionWanted) {
		final File[] journals = _directory.listFiles(new java.io.FileFilter() {
			public final boolean accept(final File pathname) {
				return pathname.getName().matches(JOURNAL_FILENAME_PATTERN);
			}
		});

		java.util.Arrays.sort(journals, new java.util.Comparator<File>() {
			public final int compare(final File f1, final File f2) {
				return new Long(journalVersion(f1)).compareTo(new Long(
						journalVersion(f2)));
			}
		});

		for (int i = journals.length - 1; i >= 0; i--) {
			final File journal = journals[i];
			if (journalVersion(journal) <= initialTransactionWanted) {
				return journal;
			}
		}
		return null;
	}

	final File createTempFile(final String prefix, final String suffix)
			throws IOException {
		return File.createTempFile(prefix, suffix, _directory);
	}

	final static void renameUnusedFile(final File journalFile) {
		journalFile.renameTo(new File(journalFile.getAbsolutePath()
				+ ".unusedFile" + System.currentTimeMillis()));
	}

	{
		_directory = new File(System.getProperty("user.home") + "/prevalent");
	}

	final File _directory;
	static final String SNAPSHOT_SUFFIX_PATTERN = "[a-zA-Z0-9]*[Ss]napshot";
	static final String SNAPSHOT_FILENAME_PATTERN = "\\d{19}\\."
			+ SNAPSHOT_SUFFIX_PATTERN;
	static final String JOURNAL_SUFFIX_PATTERN = "[a-zA-Z0-9]*[Jj]ournal";
	static final String JOURNAL_FILENAME_PATTERN = "\\d{19}\\."
			+ JOURNAL_SUFFIX_PATTERN;

}