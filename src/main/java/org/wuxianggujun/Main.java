package org.wuxianggujun;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author WuXiangGuJun
 * @create ${YEAR}-${MONTH}-${DAY} ${TIME}
 **/
public class Main {
    public static void main(String[] args) {
        long s = System.currentTimeMillis();
        File txtFile = new File("D:\\社工库资料\\裤子\\q绑\\6.9更新总库.txt");
        int i = 0;
        try (LineIterator fileIterator = FileUtils.lineIterator(txtFile, StandardCharsets.UTF_8.name())) {
            while (fileIterator.hasNext()) {
                String str = fileIterator.next();
                i++;
                //lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("System.currentTimeMillis()-s = " + (System.currentTimeMillis() - s));
    }

    private static void largeFileIO(File path) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw")) {
            String str = null;
            while ((str = randomAccessFile.readLine()) != null) {
                String result = new String(str.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                System.out.println("result = " + result);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static String getString(final String filePath, final int startIndex, final int endIndex) {
        final int size = endIndex - startIndex;
        MappedByteBuffer inputBuffer = null;
        try {
            inputBuffer = new RandomAccessFile(filePath, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, startIndex, size);
            byte[] bs = new byte[inputBuffer.capacity()];
            for (int offset = 0; offset < inputBuffer.capacity(); offset++) {
                bs[offset] = inputBuffer.get(offset);
            }
            return new String(bs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}