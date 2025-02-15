/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.fresco.samples.showcase.imageformat.color;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;
import com.facebook.common.internal.ByteStreams;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imageformat.ImageFormatCheckerUtils;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import com.facebook.imagepipeline.drawable.DrawableFactory;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.image.ImmutableQualityInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Example for a simple decoder that can decode color images that have the following format:
 *
 * <p><color>#FF5722</color>
 */
public class ColorImageExample {

  /** XML color tag that our colors must start with. */
  public static final String COLOR_TAG = "<color>";

  /** Custom {@link ImageFormat} for color images. */
  public static final ImageFormat IMAGE_FORMAT_COLOR =
      new ImageFormat("IMAGE_FORMAT_COLOR", "color");

  /**
   * Create a new image format checker for {@link #IMAGE_FORMAT_COLOR}.
   *
   * @return the image format checker
   */
  public static ImageFormat.FormatChecker createFormatChecker() {
    return new ColorFormatChecker();
  }

  /**
   * Create a new decoder that can decode {@link #IMAGE_FORMAT_COLOR} images.
   *
   * @return the decoder
   */
  public static ImageDecoder createDecoder() {
    return new ColorDecoder();
  }

  public static ColorDrawableFactory createDrawableFactory() {
    return new ColorDrawableFactory();
  }

  /**
   * Custom color format checker that verifies that the header of the file corresponds to our {@link
   * #COLOR_TAG}.
   */
  public static class ColorFormatChecker implements ImageFormat.FormatChecker {

    public static final byte[] HEADER = ImageFormatCheckerUtils.asciiBytes(COLOR_TAG);

    @Override
    public int getHeaderSize() {
      return HEADER.length;
    }

    @Nullable
    @Override
    public ImageFormat determineFormat(byte[] headerBytes, int headerSize) {
      if (headerSize < getHeaderSize()) {
        return null;
      }
      if (ImageFormatCheckerUtils.startsWithPattern(headerBytes, HEADER)) {
        return IMAGE_FORMAT_COLOR;
      }
      return null;
    }
  }

  /** Custom closeable color image that holds a single color int value. */
  public static class CloseableColorImage implements CloseableImage {

    @ColorInt private final int mColor;

    private boolean mClosed = false;

    public CloseableColorImage(int color) {
      mColor = color;
    }

    @ColorInt
    public int getColor() {
      return mColor;
    }

    @Override
    public int getSizeInBytes() {
      return 0;
    }

    @Override
    public void close() {
      mClosed = true;
    }

    @Override
    public boolean isClosed() {
      return mClosed;
    }

    @Override
    public void setImageExtras(@Nullable Map<String, Object> extras) {}

    @Override
    public void setImageExtra(String extra, Object value) {}

    @Override
    public boolean isStateful() {
      return false;
    }

    @Override
    public int getWidth() {
      return 0;
    }

    @Override
    public int getHeight() {
      return 0;
    }

    @Override
    public QualityInfo getQualityInfo() {
      return ImmutableQualityInfo.FULL_QUALITY;
    }

    @Override
    public Map<String, Object> getExtras() {
      return Collections.emptyMap();
    }
  }

  /** Decodes a color XML tag: <color>#rrggbb</color> */
  public static class ColorDecoder implements ImageDecoder {

    @Override
    public CloseableImage decode(
        EncodedImage encodedImage,
        int length,
        QualityInfo qualityInfo,
        ImageDecodeOptions options) {
      try {
        // Read the file as a string
        String text = new String(ByteStreams.toByteArray(encodedImage.getInputStream()));

        // Check if the string matches "<color>#"
        if (!text.startsWith(COLOR_TAG + "#")) {
          return null;
        }

        // Parse the int value between # and <
        int startIndex = COLOR_TAG.length() + 1;
        int endIndex = text.lastIndexOf('<');
        int color = Integer.parseInt(text.substring(startIndex, endIndex), 16);

        // Add the alpha component so that we actually see the color
        color = ColorUtils.setAlphaComponent(color, 255);

        // Return the CloseableImage
        return new CloseableColorImage(color);
      } catch (IOException e) {
        e.printStackTrace();
      }
      // Return nothing if an error occurred
      return null;
    }
  }

  /**
   * Color drawable factory that is able to render a {@link CloseableColorImage} by creating a new
   * {@link ColorDrawable} for the given color.
   */
  public static class ColorDrawableFactory implements DrawableFactory {

    @Override
    public boolean supportsImageType(CloseableImage image) {
      // We can only handle CloseableColorImages
      return image instanceof CloseableColorImage;
    }

    @Nullable
    @Override
    public Drawable createDrawable(CloseableImage image) {
      // Just return a simple ColorDrawable with the given color value
      return new ColorDrawable(((CloseableColorImage) image).getColor());
    }
  }
}
