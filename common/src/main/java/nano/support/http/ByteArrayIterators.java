package nano.support.http;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public abstract class ByteArrayIterators {

    public static @NotNull Iterator<byte @NotNull []> singleton(byte[] bytes) {
        return new Iterator<>() {
            private boolean drained = false;

            @Override
            public boolean hasNext() {
                return !this.drained;
            }

            @Override
            public byte[] next() {
                this.drained = true;
                return bytes;
            }
        };
    }

    public static <T> @NotNull Iterator<byte @NotNull []> map(Iterator<T> iterator, Function<T, Iterator<byte @NotNull []>> mapper) {
        return new Iterator<>() {

            Iterator<byte[]> delegate = Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                if (this.delegate.hasNext()) {
                    return true;
                }
                if (!iterator.hasNext()) {
                    return false;
                }
                this.delegate = mapper.apply(iterator.next());
                return this.hasNext();
            }

            @Override
            public byte[] next() {
                return this.delegate.next();
            }
        };
    }

    @SafeVarargs
    public static @NotNull Iterator<byte @NotNull []> compose(Iterator<byte[]> @NotNull ... iterator) {
        if (iterator.length == 0) {
            return Collections.emptyIterator();
        }
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                if (this.index == iterator.length) {
                    return false;
                }
                if (iterator[this.index].hasNext()) {
                    return true;
                }
                this.index++;
                return this.hasNext();
            }

            @Override
            public byte[] next() {
                return iterator[this.index].next();
            }
        };
    }

    public static @NotNull Iterator<byte @NotNull []> from(@NotNull InputStream inputStream) {

        return new Iterator<>() {

            private final byte[] buffer = new byte[8192];
            private boolean drained = false;

            @Override
            public boolean hasNext() {
                return !this.drained;
            }

            @Override
            public byte[] next() {
                this.ensureNotDrained();
                int read = this.readBytes();
                if (read > 0) {
                    var actualBytes = new byte[read];
                    System.arraycopy(this.buffer, 0, actualBytes, 0, read);
                    return actualBytes;
                } else {
                    this.drained = true;
                    this.closeStream();
                    return new byte[0];
                }
            }

            private int readBytes() {
                try {
                    return inputStream.read(this.buffer);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }

            private void closeStream() {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }

            private void ensureNotDrained() {
                if (this.drained) {
                    throw new NoSuchElementException("Stream is drained");
                }
            }
        };
    }
}
