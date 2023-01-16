package org.wuxianggujun;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.wuxianggujun.util.JDBCUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author WuXiangGuJun
 * @create 2023-01-11 19:40
 **/
public class ReadMain {
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        File txtFile = new File("D:\\社工库资料\\裤子\\q绑\\6.9更新总库.txt");
        long startTime = System.currentTimeMillis();
        int totalNumber = getFileLineNumber(txtFile);

        //线程数
        int threadNum = 8;
        int threadCount = totalNumber / threadNum + 1;

        System.out.println("threadNum = " + threadNum);

        ExecutorService service = Executors.newFixedThreadPool(threadNum);
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);

        for (int i = 0; i < threadNum; i++) {
            int startIndex = i * threadCount + 1;
            int endIndex = (i + 1) * threadCount;
            //Math.min 将两个形参中最小的返回，这样子就是获取totalNumber而不会导致读出多余的数据。
            service.execute(new ReadFileRunnable(txtFile, startIndex, Math.min(endIndex, totalNumber), countDownLatch));
        }
        countDownLatch.await();
        service.shutdown();
        System.out.println("System.currentTimeMillis()-startTime = " + (System.currentTimeMillis() - startTime));

    }

    /**
     * 大文件读取
     *
     * @param file
     */

    public static void readFileLine(File file, int startLine, int endLine, boolean flag, CallBack callBack) {
        System.out.println("flag = " + flag);
        int lineNumber = 0;
        if (startLine < 1) startLine = 0;
        String result = null;
        try (LineIterator lineIterator = FileUtils.lineIterator(file, StandardCharsets.UTF_8.name())) {
            while (lineIterator.hasNext() && flag) {
                lineNumber++;
                if (lineNumber < startLine && startLine < endLine) continue;
                if (endLine > 0 && (lineNumber - startLine) >= endLine) break;
                if (lineNumber > endLine) break;
                result = lineIterator.next();
                if (result.trim().length() > 0 && lineNumber > startLine && lineNumber < endLine && flag) {
                    callBack.onReceive(lineNumber, result);
                }
                //防止读取的行号超过文每个线程要读取的大小
                if (lineNumber >= endLine) break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("final lineNumber = " + lineNumber);
    }

    public static int getFileLineNumber(File file) {
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            reader.skip(Long.MAX_VALUE);
            return reader.getLineNumber() + 1;//实际上读取换行符数量，所以需要+1
        } catch (IOException e) {
            return -1;
        }
    }

    public interface CallBack {
        void onReceive(int lineNumber, String lineContent);
    }


    static class ReadFileRunnable implements Runnable {
        CountDownLatch latch;

        private volatile boolean flag = true;

        //起始位置
        int startIndex = 0;
        //读取的总行数
        int size = 0;
        File file;

        Connection connection = null;
        ResultSet resultSet = null;
        String sql = "INSERT INTO SGKTest.sgk_table (phone, qn) VALUES(?,?);";

        PreparedStatement statement = null;

        public ReadFileRunnable(File file, int startIndex, int size, CountDownLatch latch) {
            this.file = file;
            this.startIndex = startIndex;
            this.size = size;
            this.latch = latch;
            try {
                connection = JDBCUtils.getConnection();
                statement = connection.prepareStatement(sql);
                statement.setFetchSize(Integer.MIN_VALUE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());

            readFileLine(file, startIndex, size, flag, (lineNumber, line) -> {

                int count = atomicInteger.incrementAndGet();
                try {
                    String qq = line.substring(0, line.indexOf('-'));
                    String phone = line.substring(line.lastIndexOf('-') + 1);
                    statement.setString(1, phone);
                    statement.setString(2, qq);

                    //添加到批处理
                    statement.addBatch();
                    //每插入100000条插一次
                    if (lineNumber % 100000 == 0) {
                        System.out.println("count = " + count);
                        System.out.println("Flag = " + flag + " ThreadName = " + Thread.currentThread().getName() + " lineNumber = " + lineNumber + " line = " + line);
                        statement.executeBatch();
                        statement.clearBatch();
                        // flag = false;
                    } else if (lineNumber == size) {
                        System.out.println("count = " + count);
                        statement.executeBatch();
                        statement.clearBatch();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            JDBCUtils.close(statement, connection);
            latch.countDown();
        }
    }
}
