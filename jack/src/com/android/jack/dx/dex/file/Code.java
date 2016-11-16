package com.android.jack.dx.dex.file;

import java.io.PrintWriter;

/**
 * Interface representing code.
 */
public interface Code {

  /** file alignment of this class, in bytes */
  public static final int ALIGNMENT = 4;

  /** write size of the header of this class, in bytes */
  public static final int HEADER_SIZE = 16;

  /**
   * Does a human-friendly dump of this instance.
   *
   * @param out {@code non-null;} where to dump
   * @param prefix {@code non-null;} per-line prefix to use
   * @param verbose whether to be verbose with the output
   */
  public void debugPrint(PrintWriter out, String prefix, boolean verbose);
}
