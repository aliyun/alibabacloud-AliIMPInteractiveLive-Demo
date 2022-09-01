package com.aliyun.roompaas.beauty_pro.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.beauty_pro.remote.ResDownloadDelegate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class FileUtils {

    public static final String DCIM_FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
    public static final String photoFilePath;
    public static final String exportVideoDir;

    static {
        if (Build.FINGERPRINT.contains("Flyme")
                || Pattern.compile("Flyme", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find()
                || Build.MANUFACTURER.contains("Meizu")
                || Build.MANUFACTURER.contains("MeiZu")) {
            photoFilePath = DCIM_FILE_PATH + File.separator + "Camera";
        } else if (Build.FINGERPRINT.contains("vivo")
                || Pattern.compile("vivo", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find()
                || Build.MANUFACTURER.contains("vivo")
                || Build.MANUFACTURER.contains("Vivo")) {
            photoFilePath = Environment.getExternalStoragePublicDirectory("") + File.separator + "相机";
        } else {
            photoFilePath = DCIM_FILE_PATH + File.separator + "Camera";
        }
        exportVideoDir = DCIM_FILE_PATH + File.separator + "Queen";
        createFileDir(photoFilePath);
        createFileDir(exportVideoDir);
    }

    private static void createFileDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static String getAlbumPath() {
        File fileDir = new File(exportVideoDir);
        return fileDir.exists() ? exportVideoDir : photoFilePath;
    }

    public static String getCurrentTimePhotoFileName(String suffix) {
        SimpleDateFormat timeSdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String fileName = "Queen_" + timeSdf.format(new Date()) + suffix;
        return fileName;
    }

    public static void remove(File file) {
        if (null != file && file.exists()) {
            if (file.isDirectory()) {
                File[] subFileList = file.listFiles();
                for (File subFile : subFileList) {
                    remove(subFile);
                }
            } else {
                file.delete();
            }
        }
    }

    public static String readAssetsFile(Context context, String fileName) {
        StringBuilder sb = new StringBuilder();
        InputStream inStream = null;
        BufferedReader bufReader = null;
        try {
            inStream = parseISRemoteRes(fileName);
            bufReader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
            String readLine;
            while ((readLine = bufReader.readLine()) != null) {
                sb.append(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) inStream.close();
                if (bufReader != null) bufReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static InputStream parseISRemoteRes(String fileName) throws FileNotFoundException {
        String path = ResDownloadDelegate.parseResourcePath();
        File e = new File(path + File.separator + fileName);
        return new FileInputStream(e);
    }

    public static String[] listRemoteRes(String fileName) throws FileNotFoundException {
        String path = ResDownloadDelegate.parseResourcePath();
        File e = new File(path + File.separator + fileName);
        String[] fs = e.list();
        if (fs == null || fs.length == 0) {
            return fs;
        }

        boolean nameHasPureInt = false;
        List<String> result = new ArrayList<>();
        for (String f : fs) {
            if (TextUtils.isEmpty(f) || f.startsWith(".")) {
                Logger.i("AAA", "list filtered: " + f); // OS file system file like .DS_STORE
                continue;
            }

            nameHasPureInt |= TextUtils.isDigitsOnly(f);
            result.add(f);
        }

        if (nameHasPureInt) {
            Collections.sort(result, (s1, s2) -> {
                int i1 = TextUtils.isDigitsOnly(s1) ? Integer.parseInt(s1) : Integer.MIN_VALUE;
                int i2 = TextUtils.isDigitsOnly(s1) ? Integer.parseInt(s2) : Integer.MIN_VALUE;
                if (i1 != Integer.MIN_VALUE && i2 != Integer.MIN_VALUE) {
                    return Integer.compare(i1, i2);
                }
                return s1 != null ? s1.compareTo(s2) : -1;
            });
        }

        return result.toArray(new String[0]);
    }

    public static String wrapRemoteRes(String name) {
        String path = ResDownloadDelegate.parseResourcePath();
        return path + File.separator + name;
    }
}
