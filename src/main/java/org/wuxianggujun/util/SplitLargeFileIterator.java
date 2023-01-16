package org.wuxianggujun.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author WuXiangGuJun
 * @create 2023-01-16 21:19
 **/
public class SplitLargeFileIterator implements Iterator<String>, Closeable {
    private final BufferedReader bufferedReader;

    private List<String> strings;

    private String cachedLine;

    private boolean finished;

    private int lineNumber = 0;

    public SplitLargeFileIterator(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("Reader must not be null");
        }
        if (reader instanceof BufferedReader bufferedReader) {
            this.bufferedReader = bufferedReader;
        } else {
            bufferedReader = new BufferedReader(reader);
        }
    }


    @Override
    public void close() throws IOException {
        finished = true;
        cachedLine = null;
        close(bufferedReader);
    }

    @Override
    public boolean hasNext() {
        if (cachedLine != null) {
            return true;
        }
        if (finished) {
            finished = false;
        }
        try {
            while (true) {
                final String line = bufferedReader.readLine();
                if (line == null) {
                    finished = true;
                    return false;
                }
                lineNumber++;
                cachedLine = line;
                return true;

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public int getLineNumber(){
        return lineNumber;
    }

    @Override
    public String next() {
        return nextLine();
    }


    public String nextLine() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more lines");
        }
        final String currentLine = cachedLine;
        cachedLine = null;
        return currentLine;
    }

    public void close(final Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

}
