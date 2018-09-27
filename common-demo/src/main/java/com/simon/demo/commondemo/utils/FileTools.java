package com.simon.demo.commondemo.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

public class FileTools {
	
	/**
	 * convert base64 string to file
	 * @param base64
	 * @param destPath
	 * @param fileName
	 */
	public static void base64ToFile(String base64, String destPath, String fileName) {
        File file = null;
        String filePath = destPath;
        File  dir = new File(filePath);
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdirs();
        }
        BufferedOutputStream bos = null;
        java.io.FileOutputStream fos = null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            file=new File(filePath + "/" + fileName);
            fos = new java.io.FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
	/**
	 * convert file to base64
	 * @param file
	 * @return
	 */
	public static String fileToBase64(File file) {
        FileInputStream fin =null;
        try {
            fin = new FileInputStream(file);
            return fileToBase64(fin);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally{
            try {
                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
	
	/**
	 * convert input stream to base64 string
	 * @param inputStream
	 * @return
	 */
	public static String fileToBase64(InputStream inputStream) {
        BufferedInputStream bin =null;
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bout =null;
        try {
            bin = new BufferedInputStream(inputStream);
            baos = new ByteArrayOutputStream();
            bout = new BufferedOutputStream(baos);
            byte[] buffer = new byte[1024];
            int len = bin.read(buffer);
            while(len != -1){
                bout.write(buffer, 0, len);
                len = bin.read(buffer);
            }
            bout.flush();
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                bin.close();
                bout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
	
	public static ByteArrayInputStream byteArrayOutputStreamToByteArrayInputStream(ByteArrayOutputStream out) throws Exception {
        ByteArrayInputStream swapStream = new ByteArrayInputStream(out.toByteArray());
        return swapStream;
    }

}
