package me.nereo.multi_image_selector.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    public static int computeInitialSampleSize(BitmapFactory.Options options,
                                               int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = BitmapUtil.computeInitialSampleSize(options,
                minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    static int computeSampleSize(BitmapFactory.Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if (candidate == 0)
            return 1;
        if (candidate > 1) {
            if ((w > target) && (w / candidate) < target)
                candidate -= 1;
        }
        if (candidate > 1) {
            if ((h > target) && (h / candidate) < target)
                candidate -= 1;
        }
        return candidate;
    }

    public static Drawable makeSuitableDrawable(Context context, int ResId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), ResId, opts);

        // opts.inSampleSize = BitmapUtil.computeSampleSize(opts, -1, 128 *
        // 128)-2;
        try {
            final int IMAGE_MAX_SIZE = 500000; // 200k
            int scale = 1;
            while ((opts.outWidth * opts.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }
            Bitmap temp = null;
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                opts = new BitmapFactory.Options();
                opts.inSampleSize = scale;
                temp = BitmapFactory.decodeResource(context.getResources(),
                        ResId, opts);

                // resize to desired dimensions
                int height = temp.getHeight();
                int width = temp.getWidth();

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(temp, (int) x,
                        (int) y, true);
                temp.recycle();
                temp = scaledBitmap;
            } else {
                temp = BitmapFactory.decodeResource(context.getResources(),
                        ResId);
            }
            return new BitmapDrawable(temp);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
        return null;
    }

    public static Bitmap makeSuitableDrawable(Context context, String pathName) {
        if (pathName == null) {
            throw new RuntimeException("路径为空");
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        // BitmapFactory.decodeResource(context.getResources(), ResId, opts);
        BitmapFactory.decodeFile(pathName, opts);
        // opts.inSampleSize = BitmapUtil.computeSampleSize(opts, -1, 128 *
        // 128)-2;
        try {
            final int IMAGE_MAX_SIZE = 500000; // 200k
            int scale = 1;
            while ((opts.outWidth * opts.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }
            Bitmap temp = null;
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                opts = new BitmapFactory.Options();
                opts.inSampleSize = scale;
                temp = BitmapFactory.decodeFile(pathName, opts);

                // resize to desired dimensions
                int height = temp.getHeight();
                int width = temp.getWidth();

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(temp, (int) x,
                        (int) y, true);
                temp.recycle();
                temp = scaledBitmap;
            } else {
                temp = BitmapFactory.decodeFile(pathName);
            }
            return temp;
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
        return null;
    }

    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        if (bitmap == null) {
            return null;
        }
        // 这里经常内存溢出
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();// 内存溢出啊亲
        bitmap = null;
        return output;
    }

    /**
     * decode a bitmap from filepath,use 120px as default require size,decodes
     * image and scales it to reduce memory consumption
     *
     * @param filePath
     * @return
     */
    public static Bitmap decodeFile(String filePath) {
        File f = new File(filePath);
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 120;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE
                    && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    /**
     * decode a bitmap from filepath,with require size,decodes image and scales
     * it to reduce memory consumption
     *
     * @param filePath
     * @param REQUIRED_SIZE
     * @return
     */
    public static Bitmap decodeFile(String filePath, final int REQUIRED_SIZE) {
        File f = new File(filePath);
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            // final int REQUIRED_SIZE = 120;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE
                    && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPreferredConfig = Bitmap.Config.ARGB_4444;
            o2.inInputShareable = true;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    // public static Bitmap decodeFile(String filePath, ImageView img) {
    // BitmapFactory.Options options = new BitmapFactory.Options();
    // options.inSampleSize = 1;
    // Bitmap bm = BitmapFactory.decodeFile(filePath, options);
    // int height = img.getHeight();
    // int width = img.getWidth();
    // return ThumbnailUtils.extractThumbnail(bm, width, height);
    // // return bm;
    // }

    /**
     * @param inputStream
     * @return
     */
    public static int getSampleSize(InputStream inputStream, int requiredSize) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, o);
        // Find the correct scale value. It should be the power of
        // 2.
        int scale = 1;
        while (o.outWidth / scale / 2 >= requiredSize
                && o.outHeight / scale / 2 >= requiredSize)
            scale *= 2;
        return scale;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * @param bitmap 通过拍照获取的bitmap
     * @param ratate 旋转度数
     * @return 旋转后的bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int ratate) {
        final Matrix matrix = new Matrix();
        matrix.setRotate(ratate);
        try {
            Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (bitmap != bitmap2) {
                bitmap.recycle();
                bitmap = bitmap2;
            }
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
            System.gc();
        }
        return bitmap;
    }

    /**
     * @param imageUri 从本地获取图片的Uri
     * @return 旋转后的bitmap
     */
    public static Bitmap rotateBitmap2(Context context, Uri imageUri) {
        Bitmap bitmap = null;
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(imageUri, null, null, null, null);// 根据Uri从数据库中找
        if (cursor != null) {
            cursor.moveToFirst();// 把游标移动到首位，因为这里的Uri是包含ID的所以是唯一的不需要循环找指向第一个就是了
            String filePath = cursor.getString(cursor.getColumnIndex("_data"));// 获取图片路
            String orientation = cursor.getString(cursor
                    .getColumnIndex("orientation"));// 获取旋转的角度
            cursor.close();
            if (filePath != null) {
                bitmap = BitmapFactory.decodeFile(filePath);//根据Path读取资源图片
                int angle = 0;
                if (orientation != null && !"".equals(orientation)) {
                    angle = Integer.parseInt(orientation);
                }
                if (angle != 0) {
                    // 下面的方法主要作用是把图片转一个角度，也可以放大缩小等
                    Matrix m = new Matrix();
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    m.setRotate(angle); // 旋转angle度
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                            m, true);// 从新生成图片
                }
            }
        }
        return bitmap;
    }

    public static int getSampleSize(String fileAbsolutePath, int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        FileInputStream fIn = new FileInputStream(fileAbsolutePath);
        Bitmap bitmap = BitmapFactory.decodeStream(fIn, null, o);
        // Find the correct scale value. It should be the power of
        // 2.
        int scale = 1;
        while (o.outWidth / scale / 2 >= requiredSize
                && o.outHeight / scale / 2 >= requiredSize)
            scale *= 2;
        return scale;
    }

    public static Bitmap setBitmapSize(Bitmap bitmap, int height) {
        float size = (float) height / (float) bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(size, size); // 长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    public static Bitmap getImageFromNet(String url) {
        HttpURLConnection conn = null;
        try {
            URL mURL = new URL(url);
            conn = (HttpURLConnection) mURL.openConnection();
            conn.setRequestMethod("GET"); //设置请求方法
            conn.setConnectTimeout(10000); //设置连接服务器超时时间
            conn.setReadTimeout(5000);  //设置读取数据超时时间

            conn.connect(); //开始连接

            int responseCode = conn.getResponseCode(); //得到服务器的响应码
            if (responseCode == 200) {
                //访问成功
                InputStream is = conn.getInputStream(); //获得服务器返回的流数据
                Bitmap bitmap = BitmapFactory.decodeStream(is); //根据流数据 创建一个bitmap对象
                return bitmap;

            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect(); //断开连接
            }
        }
        return null;
    }

    /**
     * 不加载图片获取宽度和高度
     *
     * @param path
     * @return
     */
    public static PercentNum getImageWidthHeightRztio(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        Log.e("OPTIONS", String.format("原始图片比例(%1$dx%2$d)", options.outHeight, options.outWidth));
        return new PercentNum(options.outHeight, options.outWidth);
    }

    /**
     * 获取图片的纵横比模式
     *
     * @param path
     * @return 0:不裁切,1:minRztio,2:maxRztio
     */
    public static int getRztio(String path, String minRztio, String maxRztio) {
        if (minRztio.indexOf("/") < 0 || maxRztio.indexOf("/") < 0) {
            //最小和最大不合法时,默认返回不裁剪
            return 0;
        }
        PercentNum imageRztio = getImageWidthHeightRztio(path);
        String[] min = minRztio.split("/");
        String[] max = maxRztio.split("/");
        PercentNum minRztioPercent = new PercentNum(Integer.valueOf(min[0]), Integer.valueOf(min[1]));
        PercentNum maxRztioPercent = new PercentNum(Integer.valueOf(max[0]), Integer.valueOf(max[1]));
        int x1 = compareRztio(imageRztio, minRztioPercent);
        int x2 = compareRztio(imageRztio, maxRztioPercent);
        if (x1 < 0) {
            return 1;
        } else if (x2 > 0) {
            return 2;
        }
        //不裁切
        return 0;
    }

    public static int compareRztio(PercentNum x, PercentNum y) {
        /*两个分数进行通分*/
        int lcm = LCM(x.getDenominator(), y.getDenominator());
        int t1 = lcm / x.getDenominator();//把两个数的分子乘上通分倍数
        int a1 = x.getNumerator() * t1;
        int t2 = lcm / y.getDenominator();
        int a2 = y.getNumerator() * t2;
        if (a1 < a2) {
            return -1;
        } else if (a1 == a2) {
            return 0;
        } else {
            return 1;
        }
    }

    static class PercentNum {
        private int numerator;//分子
        private int denominator;//分母

        public PercentNum(int numerator, int denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        public int getNumerator() {
            return numerator;
        }

        public void setNumerator(int numerator) {
            this.numerator = numerator;
        }

        public int getDenominator() {
            return denominator;
        }

        public void setDenominator(int denominator) {
            this.denominator = denominator;
        }
    }

    /**
     * @param a 参数a
     * @param b 参数b
     * @return int 返回两个参数的最小公倍数
     * @throws
     * @explain LCM方法: 求出参数a和参数b的最小公倍数
     * @author 叶清逸
     * @date 2018年7月28日 下午11:11:36
     */
    public static int LCM(int a, int b) {
        /*排序保证a始终小于b*/
        if (a > b) {
            int t = a;
            a = b;
            b = t;
        }
        /*先求出最大公约数*/
        int c = a;
        int d = b;
        int gcd = 0;
        while (c % d != 0) {
            //k保存余数
            int k = c % d;
            //除数变为c
            c = d;
            //被除数变为余数
            d = k;
        }
        /*辗转相除结束后的c即为所求的最大公约数*/
        gcd = d;

        /*使用公式算出最小公倍数*/
        int lcm = a * b / gcd;

        return lcm;
    }

    /*
     * Java文件操作 获取文件扩展名
     * */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * 根据URI获取图片文件的宽高
     *
     * @param uri
     * @return
     */
    public static BitmapFactory.Options getImageOption(Uri uri) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
        return options;
    }

    /**
     * 根据路径获取图片文件的宽高
     *
     * @param absolutePath
     * @return
     */
    public static BitmapFactory.Options getImageOption(String absolutePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absolutePath, options);
        return options;
    }

    /**
     * 从相册获取的图片，根据Uri获取File
     *
     * @param uri
     * @return
     */
    public static String uri2File(Activity mContext, Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = mContext.managedQuery(uri, proj, null, null, null);
        if (actualimagecursor == null) {
            return null;
        }
        int actual_image_column_index = actualimagecursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        actualimagecursor.moveToFirst();

        String img_path = actualimagecursor
                .getString(actual_image_column_index);

        return img_path;

    }

    /**
     * 判断图片是否超出最大宽高
     *
     * @param imgPath
     * @param maxSide
     * @return
     */
    public static boolean isMoreThanSide(String imgPath, int maxSide) {
        BitmapFactory.Options imgOption = getImageOption(imgPath);
        return imgOption.outWidth >= maxSide || imgOption.outHeight >= maxSide;
    }

}
