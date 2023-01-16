package org.wuxianggujun;

import org.apache.commons.io.Charsets;
import org.wuxianggujun.util.SplitLargeFileIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author WuXiangGuJun
 * @create 2023-01-16 20:57
 **/
public class SplitLargeFilesMain {
    public static void main(String[] args) {
        File txtFile = new File("D:\\社工库资料\\裤子\\q绑\\6.9更新总库.txt");
        try (SplitLargeFileIterator lineIterator = new SplitLargeFileIterator(new InputStreamReader(new FileInputStream(txtFile), Charsets.toCharset(StandardCharsets.UTF_8)))) {
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                //System.out.println("line = " + line + " LineNumber" + lineIterator.getLineNumber());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
