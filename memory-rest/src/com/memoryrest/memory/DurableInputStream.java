package com.memoryrest.memory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

final class DurableInputStream  {

	final Chunk readChunk() throws IOException {

		if (_EOF)
			throw new EOFException();

		try {
			final Chunk chunk = Chunking.readChunk(_fileStream);
			if (chunk != null)
				return chunk;
		} catch (final EOFException eofx) {
		} catch (final java.io.ObjectStreamException scx) {
		} catch (final java.io.UTFDataFormatException utfx) {
		} catch (final RuntimeException rx) {
		}

		_fileStream.close();
		_EOF = true;
		throw new EOFException();
	}

	DurableInputStream(final java.io.File file) throws IOException {
		_fileStream = new java.io.BufferedInputStream(
				new java.io.FileInputStream(file));
	}

	boolean _EOF;
	final InputStream _fileStream;
	
}
