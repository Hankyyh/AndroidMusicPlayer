package com.wwj.sb.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Environment;

import com.wwj.sb.domain.Mp3Info;

/**
 * 2013/5/7
 * @author wwj
 *
 */
public class FileUtils {
	private String SDCardRoot;

	public FileUtils() {
		// ����ⲿ�洢�豸��Ŀ¼
		SDCardRoot = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator;
	}

	/**
	 * ��SD���ϴ����ļ�
	 * 
	 * @param filename
	 * @param dir
	 * @return
	 */
	public File createFileInSDCard(String filename, String dir)
			throws IOException {
		File file = new File(SDCardRoot + dir + File.separator + filename);
		file.createNewFile();
		return file;
	}

	/**
	 * ��SD���ϴ���Ŀ¼
	 * 
	 * @param dir
	 * @return
	 */
	public File createDirInSDCard(String dir) {
		File dirFile = new File(SDCardRoot + dir + File.separator);
		dirFile.mkdir();
		return dirFile;
	}

	/**
	 * �ж��ļ���SD�����Ƿ����
	 * 
	 * @param filename
	 * @param path
	 * @return
	 */
	public boolean isFileExist(String filename, String path) {
		File file = new File(SDCardRoot + path + File.separator + filename);
		return file.exists();
	}

	/**
	 * ��һ��InputStream���������д�뵽SD����
	 * @param path
	 * @param filename
	 * @param inStream
	 * @return
	 */
	public File writeToSDCardInput(String path, String filename,
			InputStream inStream) {
		File file = null;
		OutputStream outStrem = null;
		try {
			createDirInSDCard(path); // ����Ŀ¼
			file = createFileInSDCard(filename, path); // �����ļ�
			outStrem = new FileOutputStream(file); // �õ��ļ������
			byte[] buffer = new byte[4 * 1024]; // ����һ��������
			int len;
			while ((len = inStream.read(buffer)) != -1) {
				outStrem.write(buffer, 0, len);
			}
			outStrem.flush(); // ȷ������д�뵽���̵���
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outStrem.close(); // �ر������
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	
	
	public List<Mp3Info> getMp3Infos(String path) {
		List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
		File file = new File(SDCardRoot +  File.separator + path);
		File[] files = file.listFiles();
		FileUtils fileUtils = new FileUtils();
		for(int i = 0; i < files.length; i++) {
			if(files[i].getName().endsWith("mp3")) {
				Mp3Info mp3Info = new Mp3Info();
				mp3Info.setTitle(files[i].getName());
				mp3Info.setSize(files[i].length());
				String temp[] = mp3Info.getTitle().split("\\.");
				String lrcName = temp[0] + ".lrc";
				if(fileUtils.isFileExist(lrcName, "/mp3")){
					mp3Info.setLrcTitle(lrcName);
				}
				mp3Infos.add(mp3Info);
			}
		}
		return mp3Infos;
	}
	
}
