package com.memoryrest.memory;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

final class Chunking  {

	static final void writeChunk(final java.io.OutputStream stream,
			final Chunk chunk) throws IOException {
		stream.write(Integer.toHexString(chunk._bytes.length).toUpperCase()
				.getBytes("US-ASCII"));
		stream.write(';');
		stream.write(Boolean.toString(chunk._withQuery).getBytes("US-ASCII"));
		stream.write(';');
		stream.write(Long.toString(chunk._systemVersion).getBytes("US-ASCII"));
		stream.write(new byte[] { '\r', '\n' });
		stream.write(chunk._bytes);
		stream.write(new byte[] { '\r', '\n' });
	}

	static final Chunk readChunk(final InputStream stream) throws IOException {

		final StringTokenizer tokenizer = new StringTokenizer(readLine(stream),
				";=\r\n");
		final int size = Integer.parseInt(tokenizer.nextToken(), 16);
		final byte[] bytes = new byte[size];
		int total = 0;
		while (total < size) {
			final int read = stream.read(bytes, total, size - total);
			if (read == -1) {
				throw new EOFException("Unexpected end of stream in chunk data");
			}
			total += read;
		}

		final int cr = stream.read();
		final int lf = stream.read();
		if (cr == -1 || cr == '\r' && lf == -1)
			throw new EOFException("Unexpected end of stream in chunk trailer");
		else if (cr != '\r' || lf != '\n')
			throw new IOException("Chunk trailer corrupted");

		final Chunk chunk = new Chunk(bytes);
		chunk._withQuery = Boolean.valueOf(tokenizer.nextToken());
		chunk._systemVersion = Long.valueOf(tokenizer.nextToken());
		return chunk;
	}

	static final String readLine(final InputStream stream) throws IOException {
		final ByteArrayOutputStream header = new ByteArrayOutputStream();
		while (true) {
			final int b = stream.read();
			if (b == -1)
				if (header.size() == 0)
					return null;
				else
					throw new EOFException(
							"Unexpected end of stream in chunk header");
			header.write(b);
			if (b == '\n')
				return header.toString("US-ASCII");
		}
	}


}