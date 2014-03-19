package com.android.jack.jayce;

import com.android.jack.Jack;
import com.android.jack.JackIOException;
import com.android.jack.ir.ast.JDefinedClassOrInterface;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Thrown when an I/O error occurred during loading of a class or interface.
 */
public class LoadIOException extends JackIOException {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final JDefinedClassOrInterface notLoaded;

  public LoadIOException(@Nonnull String message, @Nonnull JDefinedClassOrInterface notLoaded) {
    super(message);
    this.notLoaded = notLoaded;
  }

  public LoadIOException(@Nonnull String message, @Nonnull JDefinedClassOrInterface notLoaded,
      @Nonnull IOException cause) {
    super(message, cause);
    this.notLoaded = notLoaded;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return "Failed to load "
        + Jack.getUserFriendlyFormatter().getName(notLoaded);
  }

}
