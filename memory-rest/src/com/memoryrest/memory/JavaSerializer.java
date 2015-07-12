package com.memoryrest.memory;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

final class JavaSerializer {

	static final <T extends Serializable> void writeObject(
			final java.io.OutputStream stream, final T object)
			throws IOException {
		final ObjectOutputStream objects = new ObjectOutputStream(stream);
		try {
			objects.writeObject(object);
		} finally {
			objects.close();
		}
	}

	static final <T extends Serializable> T readObject(final InputStream stream)
			throws IOException, ClassNotFoundException {
		final ObjectInputStream objects = new ObjectInputStream(stream);
		try {
			return (T) objects.readObject();
		} finally {
			objects.close();
		}
	}

}