package com.aide.ui.re.utils;

import android.os.Build;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文本文件读写工具类
 */
public class FileUtils {

    private static final ExecutorService IO_EXECUTOR = Executors.newCachedThreadPool();

    /**
     * 读取文本文件（默认UTF-8编码）
     */
    public static String read(File file) {
        return read(file, StandardCharsets.UTF_8);
    }

    /**
     * 读取文本文件（指定编码）
     */
    public static String read(File file, Charset charset) {
        if (file == null || !file.exists() || !file.isFile()) {
            return "";
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(new FileInputStream(file), charset))) {
            char[] buffer = new char[8192];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                content.append(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return content.toString();
    }

    /**
     * 异步读取文本文件
     */
    public static CompletableFuture<String> readAsync(File file) {
        return readAsync(file, StandardCharsets.UTF_8);
    }

    /**
     * 异步读取文本文件（指定编码）
     */
    public static CompletableFuture<String> readAsync(File file, Charset charset) {
        return CompletableFuture.supplyAsync(() -> read(file, charset), IO_EXECUTOR);
    }

    /**
     * 写入文本文件（默认UTF-8编码）
     */
    public static boolean write(File file, String content) {
        return write(file, content, StandardCharsets.UTF_8);
    }

    /**
     * 写入文本文件（指定编码）
     */
    public static boolean write(File file, String content, Charset charset) {
        if (file == null) {
            return false;
        }

        // 确保父目录存在
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                return false;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(
			new OutputStreamWriter(new FileOutputStream(file), charset))) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 异步写入文本文件
     */
    public static CompletableFuture<Boolean> writeAsync(File file, String content) {
        return writeAsync(file, content, StandardCharsets.UTF_8);
    }

    /**
     * 异步写入文本文件（指定编码）
     */
    public static CompletableFuture<Boolean> writeAsync(File file, String content, Charset charset) {
        return CompletableFuture.supplyAsync(() -> write(file, content, charset), IO_EXECUTOR);
    }

    /**
     * 追加写入文本文件（默认UTF-8编码）
     */
    public static boolean append(File file, String content) {
        return append(file, content, StandardCharsets.UTF_8);
    }

    /**
     * 追加写入文本文件（指定编码）
     */
    public static boolean append(File file, String content, Charset charset) {
        if (file == null) {
            return false;
        }

        // 确保父目录存在
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                return false;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(
			new OutputStreamWriter(new FileOutputStream(file, true), charset))) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 异步追加写入文本文件
     */
    public static CompletableFuture<Boolean> appendAsync(File file, String content) {
        return appendAsync(file, content, StandardCharsets.UTF_8);
    }

    /**
     * 异步追加写入文本文件（指定编码）
     */
    public static CompletableFuture<Boolean> appendAsync(File file, String content, Charset charset) {
        return CompletableFuture.supplyAsync(() -> append(file, content, charset), IO_EXECUTOR);
    }

    /**
     * 删除文件
     */
    public static boolean delete(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        return file.delete();
    }

    /**
     * 检查文件是否存在
     */
    public static boolean exists(File file) {
        return file != null && file.exists();
    }

    /**
     * 确保父目录存在
     */
    public static boolean ensureParentDir(File file) {
        if (file == null) {
            return false;
        }
        File parent = file.getParentFile();
        return parent == null || parent.exists() || parent.mkdirs();
    }

    /**
     * 获取文件大小（字节）
     */
    public static long getSize(File file) {
        return file == null ? 0 : file.length();
    }

    /**
     * 判断是否为目录
     */
    public static boolean isDirectory(File file) {
        return file != null && file.isDirectory();
    }

    /**
     * 判断是否为文件
     */
    public static boolean isFile(File file) {
        return file != null && file.isFile();
    }

    /**
     * 格式化文件大小
     */
    public static String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
        }
    }
	
	
		/**
		 * 判断文件是否为文本文件
		 * 原理：读取文件前 4KB 字节，如果未发现 null 字节 (0x00) 且不可打印控制字符比例较低，则认为是文本文件。
		 *
		 * @param file 待判断的文件
		 * @return true 是文本文件, false 是二进制文件或发生错误
		 */
		public static boolean isTextFile(File file) {
			if (!file.exists() || !file.isFile()) {
				return false;
			}

			// 定义检查的字节数，不需要读取整个大文件，通常前 4096 字节足够判断
			byte[] buffer = new byte[4096];

			try (FileInputStream fis = new FileInputStream(file)) {
				int read = fis.read(buffer);
				if (read == -1) {
					// 空文件视为文本文件
					return true;
				}

				int suspiciousCount = 0; // 可疑（不可打印）字符计数

				for (int i = 0; i < read; i++) {
					int b = buffer[i] & 0xFF; // 转换为无符号 int (0-255)

					// 1. 检查是否存在 NULL 字节 (0x00)，这是二进制文件的最强特征
					if (b == 0) {
						return false;
					}

					// 2. 统计不可打印的控制字符
					// 标准 ASCII 文本中，小于 32 (Space) 的通常是控制字符
					// 我们允许 Tab (9), 换行 (10), 回车 (13)
					if (b < 32 && b != 9 && b != 10 && b != 13) {
						suspiciousCount++;
					}
				}

				// 如果不可打印字符超过总读取数的 5% (经验阈值)，则认为是二进制文件
				// 例如：一张图片或 exe 文件的开头可能全是乱码控制符
				return (suspiciousCount / (double) read) < 0.05;

			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

	
}

