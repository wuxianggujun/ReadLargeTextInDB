package org.wuxianggujun;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author WuXiangGuJun
 * @create 2023-01-10 22:00
 **/
public class ReadFileMain {
    public static void main(String[] args) {
        int totals = 100000000;
        int segment = 20;

        ExecutorService service = Executors.newFixedThreadPool(segment);
        AtomicInteger atomicInteger = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(segment);
        long s = System.currentTimeMillis();
        for (int j = 0; j < segment; j++) {
            service.execute(() -> {
                RandomAccessFile randomAccessFile;
                FileChannel fileChannel = null;
                try {
                    String fName = "D:\\tmp_" + atomicInteger.getAndIncrement() + ".txt";
                    randomAccessFile = new RandomAccessFile(fName, "rw");
                    fileChannel = randomAccessFile.getChannel();
                    int offset = 0;
                    for (int i = 0; i < totals / segment / 10000; i++) {
                        //每次写1w个数字
                        StringBuilder sb = new StringBuilder();
                        for (int k = 0; k < 10000; k++) {
                            sb.append(new Random().nextInt(10000000) + "\n");
                        }
                        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
                        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offset, bytes.length);
                        mappedByteBuffer.put(bytes);
                        offset += bytes.length;

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("await 唤醒， 小文件写入完毕! 耗時：" + (System.currentTimeMillis() - s));
        List<File> files = new ArrayList<>();
        for (int i = 0; i < segment; i++) {
            files.add(new File("D:\\tmp_" + i + ".txt"));
        }
        s = System.currentTimeMillis();
        //合併文件
        merge(files, "D:\\last.txt");
        System.out.println("合併文件完毕! 耗時：" + (System.currentTimeMillis() - s));
        service.shutdown();

    }


    public static void merge(List<File> files, String to) {
        File t = new File(to);
        FileInputStream in = null;
        FileChannel inChannel = null;

        FileOutputStream out = null;
        FileChannel outChannel = null;
        try {
            out = new FileOutputStream(t, true);
            outChannel = out.getChannel();
            // 记录新文件最后一个数据的位置
            long start = 0;
            for (File file : files) {
                in = new FileInputStream(file);
                inChannel = in.getChannel();
                // 从inChannel中读取file.length()长度的数据，写入outChannel的start处
                outChannel.transferFrom(inChannel, start, file.length());
                start += file.length();
                in.close();
                inChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                outChannel.close();
            } catch (Exception e2) {
            }
        }

    }
}
