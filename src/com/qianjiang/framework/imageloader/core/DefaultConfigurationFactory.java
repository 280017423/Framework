package com.qianjiang.framework.imageloader.core;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;

import com.qianjiang.framework.imageloader.cache.disc.DiscCacheAware;
import com.qianjiang.framework.imageloader.cache.disc.impl.FileCountLimitedDiscCache;
import com.qianjiang.framework.imageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.qianjiang.framework.imageloader.cache.disc.impl.UnlimitedDiscCache;
import com.qianjiang.framework.imageloader.cache.disc.naming.FileNameGenerator;
import com.qianjiang.framework.imageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.qianjiang.framework.imageloader.cache.memory.MemoryCacheAware;
import com.qianjiang.framework.imageloader.cache.memory.impl.FuzzyKeyMemoryCache;
import com.qianjiang.framework.imageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.qianjiang.framework.imageloader.core.assist.MemoryCacheKeyUtil;
import com.qianjiang.framework.imageloader.core.display.BitmapDisplayer;
import com.qianjiang.framework.imageloader.core.display.SimpleBitmapDisplayer;
import com.qianjiang.framework.imageloader.core.download.ImageDownloader;
import com.qianjiang.framework.imageloader.core.download.URLConnectionImageDownloader;
import com.qianjiang.framework.imageloader.utils.StorageUtils;

/**
 * Factory for providing of default options for
 * {@linkplain ImageLoaderConfiguration configuration}
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class DefaultConfigurationFactory {

	/**
	 * Create {@linkplain HashCodeFileNameGenerator default implementation} of
	 * FileNameGenerator
	 */
	public static FileNameGenerator createFileNameGenerator() {
		return new HashCodeFileNameGenerator();
	}

	/**
	 * Create default implementation of {@link DisckCacheAware} depends on
	 * incoming parameters
	 */
	public static DiscCacheAware createDiscCache(Context context,
			FileNameGenerator discCacheFileNameGenerator, int discCacheSize,
			int discCacheFileCount) {
		if (discCacheSize > 0) {
			File individualCacheDir = StorageUtils
					.getIndividualCacheDirectory(context);
			return new TotalSizeLimitedDiscCache(individualCacheDir,
					discCacheFileNameGenerator, discCacheSize);
		} else if (discCacheFileCount > 0) {
			File individualCacheDir = StorageUtils
					.getIndividualCacheDirectory(context);
			return new FileCountLimitedDiscCache(individualCacheDir,
					discCacheFileNameGenerator, discCacheFileCount);
		} else {
			// 如果不做限制条件，那么我们就用manifest文件的配置的目录作为缓存目录
			File cacheDir = StorageUtils.getOwnCacheDirectory(context);
			return new UnlimitedDiscCache(cacheDir, discCacheFileNameGenerator);
		}
	}

	/**
	 * Create default implementation of {@link MemoryCacheAware} depends on
	 * incoming parameters
	 */
	public static MemoryCacheAware<String, Bitmap> createMemoryCache(
			int memoryCacheSize, boolean denyCacheImageMultipleSizesInMemory) {
		MemoryCacheAware<String, Bitmap> memoryCache = new UsingFreqLimitedMemoryCache(
				memoryCacheSize);
		if (denyCacheImageMultipleSizesInMemory) {
			memoryCache = new FuzzyKeyMemoryCache<String, Bitmap>(memoryCache,
					MemoryCacheKeyUtil.createFuzzyKeyComparator());
		}
		return memoryCache;
	}

	/** Create default implementation of {@link ImageDownloader} */
	public static ImageDownloader createImageDownloader() {
		return new URLConnectionImageDownloader();
	}

	/** Create default implementation of {@link BitmapDisplayer} */
	public static BitmapDisplayer createBitmapDisplayer() {
		return new SimpleBitmapDisplayer();
	}
}
