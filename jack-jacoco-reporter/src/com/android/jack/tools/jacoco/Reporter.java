/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.tools.jacoco;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A class responsible for generating a coverage report.
 */
public class Reporter {
  /**
   * A list of coverage description files produced by Jack.
   */
  @Nonnull
  private List<File> coverageDescriptionFiles = Collections.emptyList();

  /**
   * The coverage execution file produced during execution.
   */
  @Nonnull
  private List<File> coverageExecutionDataFiles = Collections.emptyList();

  /**
   * The directory where the report must be generated into.
   */
  @CheckForNull
  private File reportOutputDirectory;

  /**
   * A list of source files directories.
   */
  @Nonnull
  private List<File> sourceFilesDirectories = Collections.emptyList();

  /**
   * The name of the report.
   */
  @Nonnull
  private String reportName = Constants.DEFAULT_REPORT_NAME;

  /**
   * The type of the report.
   */
  @Nonnull
  private ReportType reportType = Constants.DEFAULT_REPORT_TYPE;

  /**
   * The output encoding of the report.
   */
  @Nonnull
  private String outputEncoding = Constants.DEFAULT_OUTPUT_ENCODING;

  /**
   * The input encoding of source files.
   */
  @Nonnull
  private String sourceFilesEncoding = Constants.DEFAULT_INPUT_ENCODING;

  /**
   * The tab width of the report.
   */
  @Nonnegative
  private int tabWidth = Constants.DEFAULT_TAB_WIDTH;

  /**
   * The mapping file for shrinking and obfuscation.
   */
  @CheckForNull
  private File mappingFile;

  /**
   * @return a list of coverage description files.
   */
  @Nonnull
  public List<File> getCoverageDescriptionFiles() {
    return coverageDescriptionFiles;
  }

  /**
   * @return a list of coverage execution data files.
   */
  @Nonnull
  public List<File> getCoverageExecutionDataFiles() {
    return coverageExecutionDataFiles;
  }

  /**
   * @return the report output directory.
   */
  @CheckForNull
  public File getReportOutputDirectory() {
    return reportOutputDirectory;
  }

  /**
   * @return a list of source files directories.
   */
  @Nonnull
  public List<File> getSourceFilesDirectories() {
    return sourceFilesDirectories;
  }

  /**
   * @return the report name.
   */
  @Nonnull
  public String getReportName() {
    return reportName;
  }

  /**
   * @return the report type.
   */
  @Nonnull
  public ReportType getReportType() {
    return reportType;
  }

  /**
   * @return the output encoding.
   */
  @Nonnull
  public String getOutputEncoding() {
    return outputEncoding;
  }

  /**
   * @return the source files encoding.
   */
  @Nonnull
  public String getSourceFilesEncoding() {
    return sourceFilesEncoding;
  }

  /**
   * @return the tab width.
   */
  @Nonnegative
  public int getTabWidth() {
    return tabWidth;
  }

  /**
   * @return the mapping file
   */
  @CheckForNull
  public File getMappingFile() {
    return mappingFile;
  }

  /**
   * Sets input coverage description files.
   *
   * @param coverageDescriptionFiles the coverageDescriptionFiles to set
   * @throws NullPointerException if one file is null in the list.
   * @throws ReporterException if a file does not exist or is not readable.
   */
  public void setCoverageDescriptionFiles(@Nonnull List<File> coverageDescriptionFiles)
      throws ReporterException {
    for (File coverageDescriptionFile : coverageDescriptionFiles) {
      if (coverageDescriptionFile == null) {
        throw new NullPointerException();
      }
      checkFileExists(coverageDescriptionFile);
      checkCanReadFromFile(coverageDescriptionFile);
    }
    this.coverageDescriptionFiles = coverageDescriptionFiles;
  }

  /**
   * Sets input coverage execution files.
   *
   * @param coverageExecutionDataFiles the coverageExecutionDataFiles to set
   * @throws NullPointerException if one file is null in the list.
   * @throws ReporterException if a file does not exist or is not readable.
   */
  public void setCoverageExecutionDataFiles(@Nonnull List<File> coverageExecutionDataFiles)
      throws ReporterException {
    for (File coverageExecutionDataFile : coverageExecutionDataFiles) {
      if (coverageExecutionDataFile == null) {
        throw new NullPointerException();
      }
      checkFileExists(coverageExecutionDataFile);
      checkCanReadFromFile(coverageExecutionDataFile);
    }
    this.coverageExecutionDataFiles = coverageExecutionDataFiles;
  }

  /**
   * Sets output report directory.
   *
   * @param reportOutputDirectory the reportOutputDirectory to set
   * @throws ReporterException if the file does not exist or is not writable.
   */
  public void setReportOutputDirectory(@Nonnull File reportOutputDirectory)
      throws ReporterException {
    checkDirectoryExists(reportOutputDirectory);
    checkCanWriteToFile(reportOutputDirectory);
    this.reportOutputDirectory = reportOutputDirectory;
  }

  /**
   * Sets input source file directories.
   *
   * @param sourceFilesDirectories the sourceFilesDirectories to set
   * @throws NullPointerException if one file is null in the list.
   * @throws ReporterException if a file does not exist or is not readable.
   */
  public void setSourceFilesDirectories(@Nonnull List<File> sourceFilesDirectories)
      throws ReporterException {
    for (File sourceFilesDirectory : sourceFilesDirectories) {
      if (sourceFilesDirectory == null) {
        throw new NullPointerException();
      }
      checkDirectoryExists(sourceFilesDirectory);
      checkCanReadFromFile(sourceFilesDirectory);
    }
    this.sourceFilesDirectories = sourceFilesDirectories;
  }

  /**
   * Sets report name.
   *
   * @param reportName the reportName to set
   */
  public void setReportName(@Nonnull String reportName) {
    this.reportName = reportName;
  }

  /**
   * Sets report type.
   *
   * @param reportType the reportType to set
   */
  public void setReportType(@Nonnull ReportType reportType) {
    this.reportType = reportType;
  }

  /**
   * Sets output encoding of the report.
   *
   * @param outputEncoding the outputEncoding to set
   */
  public void setOutputEncoding(@Nonnull String outputEncoding) {
    this.outputEncoding = outputEncoding;
  }

  /**
   * Sets input encoding of the source files.
   *
   * @param sourceFilesEncoding the sourceFilesEncoding to set
   */
  public void setSourceFilesEncoding(@Nonnull String sourceFilesEncoding) {
    this.sourceFilesEncoding = sourceFilesEncoding;
  }

  /**
   * Sets tab width for the source files in the report.
   *
   * @param tabWidth the tabWidth to set
   */
  public void setTabWidth(@Nonnegative int tabWidth) {
    this.tabWidth = tabWidth;
  }

  /**
   * Set the mapping file for the report.
   *
   * @param mappingFile a mapping file
   * @throws ReporterException if the file does not exist or is not readable.
   */
  public void setMappingFile(@Nonnull File mappingFile) throws ReporterException {
    checkFileExists(mappingFile);
    checkCanReadFromFile(mappingFile);
    this.mappingFile = mappingFile;
  }

  public void createReport() throws IOException, ReporterException {
    checkFiles();

    // Load and analyze coverage execution file.
    ExecFileLoader loader = loadCoverageExecutionFile();
    IBundleCoverage bundleCoverage = createBundleCoverage(loader);

    // Create report.
    switch (reportType) {
      case HTML:
        createHtmlReport(loader, bundleCoverage);
        break;

      case XML:
        createXmlReport(loader, bundleCoverage);
        break;

      case CSV:
        createCsvReport(loader, bundleCoverage);
        break;

      default:
        throw new IllegalArgumentException("Unknown report type");
    }
  }

  private void checkFiles() throws ReporterException {
    if (coverageDescriptionFiles.isEmpty()) {
      throw new ReporterException("Missing coverage description file (at least one is required)");
    }
    if (coverageExecutionDataFiles.isEmpty()) {
      throw new ReporterException("Missing coverage execution file");
    }
    if (reportOutputDirectory == null) {
      throw new ReporterException("Missing report output directory");
    }
  }

  @Nonnull
  private ExecFileLoader loadCoverageExecutionFile() throws IOException {
    ExecFileLoader loader = new ExecFileLoader();
    for (File coverageExecutionDataFile : coverageExecutionDataFiles) {
      loader.load(coverageExecutionDataFile);
    }
    return loader;
  }

  @Nonnull
  private IBundleCoverage createBundleCoverage(@Nonnull ExecFileLoader loader) throws IOException {
    CoverageBuilder coverageBuilder = new CoverageBuilder();
    MappingFileLoader mappingFileLoader = null;
    if (mappingFile != null) {
      mappingFileLoader = new MappingFileLoader();
      InputStream in = new FileInputStream(mappingFile);
      try {
        mappingFileLoader.load(in);
      } finally {
        in.close();
      }
    }
    JackCoverageAnalyzer analyzer = new JackCoverageAnalyzer(loader.getExecutionDataStore(),
        coverageBuilder, mappingFileLoader);
    // Analyze each coverage description file to fill the coverage builder.
    for (File coverageDescriptionFile : coverageDescriptionFiles) {
      analyzer.analyze(coverageDescriptionFile);
    }
    return coverageBuilder.getBundle(reportName);
  }

  private void createHtmlReport(@Nonnull ExecFileLoader loader,
      @Nonnull IBundleCoverage bundleCoverage) throws IOException {
    HTMLFormatter htmlFormatter = new HTMLFormatter();
    htmlFormatter.setOutputEncoding(outputEncoding);
    IReportVisitor visitor =
        htmlFormatter.createVisitor(new FileMultiReportOutput(reportOutputDirectory));
    applyVisitor(loader, bundleCoverage, visitor);
    File indexHtmlFile = new File(reportOutputDirectory, "index.html");
    System.out.println("Created HTML report at " + indexHtmlFile.getAbsolutePath());
  }

  private void createXmlReport(@Nonnull ExecFileLoader loader,
      @Nonnull IBundleCoverage bundleCoverage) throws IOException {
    XMLFormatter xmlFormatter = new XMLFormatter();
    xmlFormatter.setOutputEncoding(outputEncoding);
    File xmlReportFile = new File(reportOutputDirectory, "report.xml");
    OutputStream outputStream = new FileOutputStream(xmlReportFile);
    try {
      IReportVisitor visitor = xmlFormatter.createVisitor(outputStream);
      applyVisitor(loader, bundleCoverage, visitor);
      System.out.println("Created XML report at " + xmlReportFile.getAbsolutePath());
    } finally {
      outputStream.close();
    }
  }

  private void createCsvReport(@Nonnull ExecFileLoader loader,
      @Nonnull IBundleCoverage bundleCoverage) throws IOException {
    CSVFormatter csvFormatter = new CSVFormatter();
    csvFormatter.setOutputEncoding(outputEncoding);
    File csvReportFile = new File(reportOutputDirectory, "report.csv");
    OutputStream outputStream = new FileOutputStream(csvReportFile);
    try {
      IReportVisitor visitor = csvFormatter.createVisitor(outputStream);
      applyVisitor(loader, bundleCoverage, visitor);
      System.out.println("Created CSV report at " + csvReportFile.getAbsolutePath());
    } finally {
      outputStream.close();
    }
  }

  private void applyVisitor(@Nonnull ExecFileLoader loader, @Nonnull IBundleCoverage bundleCoverage,
      @Nonnull IReportVisitor visitor) throws IOException {
    // Let the visitor know about execution information. This must be done before visiting
    // the bundle.
    visitor.visitInfo(loader.getSessionInfoStore().getInfos(),
        loader.getExecutionDataStore().getContents());

    // Visit the bundle with source file information.
    MultiSourceFileLocator sourceFileLocator = new MultiSourceFileLocator(tabWidth);
    for (File sourceFilesDirectory : sourceFilesDirectories) {
      sourceFileLocator
          .add(new DirectorySourceFileLocator(sourceFilesDirectory, sourceFilesEncoding, tabWidth));
    }
    visitor.visitBundle(bundleCoverage, sourceFileLocator);

    // Let the visitor know we're done so the report gets generated.
    visitor.visitEnd();
  }

  private static void checkFileExists(@Nonnull File file) throws ReporterException {
    checkFileExistsImpl(file, false);
  }

  private static void checkCanReadFromFile(@Nonnull File file) throws ReporterException {
    if (!file.canRead()) {
      throw new ReporterException(MessageFormat.format("Cannot read from file {0}", file));
    }
  }

  private static void checkCanWriteToFile(@Nonnull File file) throws ReporterException {
    if (!file.canWrite()) {
      throw new ReporterException(MessageFormat.format("Cannot write to file {0}", file));
    }
  }

  private static void checkDirectoryExists(@Nonnull File file) throws ReporterException {
    checkFileExistsImpl(file, true);
    if (!file.isDirectory()) {
      throw new ReporterException(MessageFormat.format("File {0} is not a directory", file));
    }
  }

  private static void checkFileExistsImpl(@Nonnull File file, boolean expectDirectory)
      throws ReporterException {
    if (!file.exists()) {
      throw new ReporterException(MessageFormat.format("File {0} does not exist", file));
    } else if (expectDirectory != file.isDirectory()) {
      if (expectDirectory) {
        throw new ReporterException(MessageFormat.format("File {0} is not a directory", file));
      } else {
        throw new ReporterException(MessageFormat.format("File {0} is a directory", file));
      }
    }
  }
}
