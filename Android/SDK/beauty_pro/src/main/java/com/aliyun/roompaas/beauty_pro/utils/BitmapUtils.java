package com.aliyun.roompaas.beauty_pro.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

import com.aliyun.roompaas.base.util.IOUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BitmapUtils {
    public static int HORIZON = 0;
    public static int VERTICAL = 1;
    public static final String IMAGE_FORMAT_JPG = ".jpg";
    public static final String IMAGE_FORMAT_PNG = ".png";

    public static Bitmap combineBitmap(Bitmap first, Bitmap second, int orientation) {
        Bitmap rs = null;

        int width;
        int height;

        if (orientation == HORIZON) {
            width = first.getWidth() + second.getWidth();
            height = first.getHeight() > second.getHeight() ? first.getHeight() : second.getHeight();
        } else {
            height = first.getHeight() + second.getHeight();
            width = first.getWidth() > second.getWidth() ? first.getWidth() : second.getWidth();
        }

        rs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(rs);

        if (orientation == HORIZON) {
            comboImage.drawBitmap(first, 0f, 0f, null);
            comboImage.drawBitmap(second, first.getWidth(), 0f, null);
        } else {
            comboImage.drawBitmap(first, 0f, 0f, null);
            comboImage.drawBitmap(second, 0, first.getHeight(), null);
        }
        return rs;
    }


    private static Map<String, Bitmap> sBmpCache = new HashMap();
    public static Bitmap decodeBitmapFromAssets(Context context, String fileName) {
        Bitmap bitmap = sBmpCache.get(fileName);

        if (null == bitmap) {
            InputStream is = null;

            try {
                is = FileUtils.parseISRemoteRes(fileName);
                bitmap = BitmapFactory.decodeStream(is);

                sBmpCache.put(fileName, bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtil.close(is);
            }
        }

        return bitmap;
    }

    public static Bitmap rotateBitmap(Bitmap originBmp, int degree) {
        if (originBmp == null)
            return originBmp;

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(originBmp, 0, 0, originBmp.getWidth(), originBmp.getHeight(), matrix, true);
    }

    public static Bitmap bitmapFlipY(Bitmap originBmp) {
        Matrix matrix = new Matrix();

        matrix.postScale(1, -1, originBmp.getWidth()/2, originBmp.getHeight()/2);

        return Bitmap.createBitmap(originBmp, 0, 0, originBmp.getWidth(), originBmp.getHeight(), matrix, true);
    }

    public static Bitmap bitmapFlipYAndRotate(Bitmap originBmp, int degree) {
        Matrix matrix = new Matrix();

        matrix.postScale(1, -1, originBmp.getWidth()/2, originBmp.getHeight()/2);
        matrix.postRotate(degree);

        return Bitmap.createBitmap(originBmp, 0, 0, originBmp.getWidth(), originBmp.getHeight(), matrix, true);
    }

    public static void saveGrayToFile(byte[] grayData, int width, int height, String filePath) {
        try {
            Bitmap bmp = gray2Bitmap(
                    grayData,
                    width,
                    height
            );
            FileOutputStream outputStream = new FileOutputStream(filePath);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void saveRgbToFile(byte[] rgbData, int width, int height, String filePath) {
        try {
            Bitmap bmp = rgb2Bitmap(
                    rgbData,
                    width,
                    height
            );
            FileOutputStream outputStream = new FileOutputStream(filePath);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void saveRgbaToFile(byte[] rgbaData, int width, int height, String filePath) {
        try {
            Bitmap bmp = rgba2Bitmap(
                    rgbaData,
                    width,
                    height
            );
            FileOutputStream outputStream = new FileOutputStream(filePath);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Bitmap gray2Bitmap(byte[] data, int width, int height) {
        int[] colors = convertGrayByteArrayToColor(data);
        if (colors == null) {
            return null;
        }

        Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height,
                Bitmap.Config.ARGB_8888);
        return bmp;
    }

    public static Bitmap rgb2Bitmap(byte[] data, int width, int height) {
        int[] colors = convertRgbByteArrayToColor(data);    //取RGB值转换为int数组
        if (colors == null) {
            return null;
        }

        Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height,
                Bitmap.Config.RGB_565);
        return bmp;
    }

    public static Bitmap rgba2Bitmap(byte[] data, int width, int height) {
        int[] colors = convertRgbaByteArrayToColor(data);    //取RGB值转换为int数组
        if (colors == null) {
            return null;
        }

        Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height,
                Bitmap.Config.ARGB_8888);
        return bmp;
    }

    public static int[] convertRgbaByteArrayToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        int arg = 0;
        if (size % 4 != 0) {
            arg = 1;
        }

        // 一般RGB字节数组的长度应该是3的倍数，
        // 不排除有特殊情况，多余的RGB数据用黑色0XFF000000填充
        int[] color = new int[size/4 + arg];
        int red, green, blue, alpha;
        int colorLen = color.length;
        if (arg == 0) {
            for (int i = 0; i < colorLen; ++i) {
                red = convertByteToInt(data[i * 4]);
                green = convertByteToInt(data[i * 4 + 1]);
                blue = convertByteToInt(data[i * 4 + 2]);
                alpha = convertByteToInt(data[i * 4 + 3]);

                // 获取RGB分量值通过按位或生成int的像素值
                color[i] = (alpha << 24) | (red << 16) | (green << 8) | blue | 0xFF000000;
            }
        } else {
            for (int i = 0; i < colorLen - 1; ++i) {
                red = convertByteToInt(data[i * 4]);
                green = convertByteToInt(data[i * 4 + 1]);
                blue = convertByteToInt(data[i * 4 + 2]);
                alpha = convertByteToInt(data[i * 4 + 3]);

                // 获取RGB分量值通过按位或生成int的像素值
                color[i] = (alpha << 24) | (red << 16) | (green << 8) | blue | 0xFF000000;
            }

            color[colorLen - 1] = 0xFF000000;
        }

        return color;
    }

    public static int[] convertRgbByteArrayToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }

        // 一般RGB字节数组的长度应该是3的倍数，
        // 不排除有特殊情况，多余的RGB数据用黑色0XFF000000填充
        int[] color = new int[size/3 + arg];
        int red, green, blue;
        int colorLen = color.length;
        if (arg == 0) {
            for (int i = 0; i < colorLen; ++i) {
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);

                // 获取RGB分量值通过按位或生成int的像素值
                color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
            }
        } else {
            for (int i = 0; i < colorLen - 1; ++i) {
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);
                color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
            }

            color[colorLen - 1] = 0xFF000000;
        }

        return color;
    }

    public static int[] convertGrayByteArrayToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        // 一般RGB字节数组的长度应该是3的倍数，
        // 不排除有特殊情况，多余的RGB数据用黑色0XFF000000填充
        int[] color = new int[size];// / 3 + arg];
        int red, green, blue;
        int colorLen = color.length;
        for (int i = 0; i < colorLen; ++i) {
            red = convertByteToInt(data[i]);
            green = convertByteToInt(data[i]);
            blue = convertByteToInt(data[i]);
            // 获取RGB分量值通过按位或生成int的像素值
            color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
        }

        return color;
    }


    // 将一个byte数转成int
    // 实现这个函数的目的是为了将byte数当成无符号的变量去转化成int
    public static int convertByteToInt(byte data) {

        int heightBit = (int) ((data >> 4) & 0x0F);
        int lowBit = (int) (0x0F & data);
        return heightBit * 16 + lowBit;
    }

    public static boolean saveToFile(Bitmap bitmap, String outPath, Bitmap.CompressFormat format, int quality) {
        if (bitmap == null) {
            return false;
        } else {
            try {
                File fileOut = new File(outPath);
                boolean ret;
                if (fileOut.exists()) {
                    ret = fileOut.delete();
                    if (!ret) {
                        Log.e("Sticker", "delete() FAIL:" + fileOut.getAbsolutePath());
                    }
                }

                if (!fileOut.getParentFile().exists()) {
                    fileOut.getParentFile().mkdirs();
                }

                ret = fileOut.createNewFile();
                if (!ret) {
                    Log.e("Sticker", "createNewFile() FAIL:" + fileOut.getAbsolutePath());
                }

                FileOutputStream fos = new FileOutputStream(fileOut);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bitmap.compress(format, quality, bos);
                bitmap.recycle();
                fos.flush();
                if (bos != null) {
                    bos.close();
                }

                return true;
            } catch (Exception var9) {
                var9.printStackTrace();
                return false;
            }
        }
    }
}
