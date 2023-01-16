package org.wuxianggujun;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author WuXiangGuJun
 * @create 2023-01-11 9:39
 **/
public class StatisticalLineNumber {
    public static void main(String[] args) {
        long s = System.currentTimeMillis();
        File txtFile = new File("D:\\社工库资料\\裤子\\q绑\\6.9更新总库.txt");
        System.out.println("readLastLineV2(txtFile) = " + readLastLineV2(txtFile));
        //getFileLineNumber(txtFile);
//        System.out.println("getFileLineNumber(txtFile) = " + getFileLineNumber(txtFile));
        System.out.println(System.currentTimeMillis() - s);
    }

    public static int getFileLineNumber(File file) {
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            reader.skip(Long.MAX_VALUE);
            return reader.getLineNumber() + 1;//实际上读取换行符数量，所以需要+1
        } catch (IOException e) {
            return -1;
        }
    }

    public static int readLastLineV2(File file) {
        int i = 0;
        String result = null;
        try (ReversedLinesFileReader reversedLinesReader = new ReversedLinesFileReader(file, Charset.defaultCharset())) {
            while (reversedLinesReader.readLine() != null) {
                i++;

            }
            return i;
        } catch (Exception e) {
            System.out.println("e = " + e);
        }
        return i;
    }
}
