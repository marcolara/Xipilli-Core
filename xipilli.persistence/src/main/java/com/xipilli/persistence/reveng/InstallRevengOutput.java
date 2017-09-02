package com.xipilli.persistence.reveng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Install reveng output from the src/util source folder into the src source
 * folder. Handles package name refactoring for pojo class files as well since
 * they come from dao and end up in model.
 */
public class InstallRevengOutput {
	private DAOMigrator daoMigrator = new DAOMigrator();

	// settings
	private boolean doMoveInsteadOfCopy = false;
	private String projectParentRelativeDir;
	private String projectName;
	private String projectPath;

	private String mainJavaPath;
	private String sublassTemplateFileLocation;
	private String installDaoDir;
	private String installDaoPackage;
	private String installDaoBaseDir;
	private String installDaoBasePackage;
	private String installPojoDir;
	private String installPojoPackage;

	private String revengOutputDir = mainJavaPath + "/" + projectPath + "/reveng/tmp";
	private String revengOutputPackage = projectName + ".reveng.tmp";

	private String mainProjectPath;

	// abstract helper patterns
	private static final String LOG_DOT = "log.";
	private static final String SUPPRESS_WARNINGS_UNUSED = "@SuppressWarnings(\"unused\")";

	// demarcations in the reveng output files to help migration and conditional
	// formatting
	public static final String REVENG_IMPORT_START_COMMENT = "//<revengImports>";
	public static final String REVENG_IMPORT_END_COMMENT = "//</revengImports>";
	public static final String REVENG_CUSTOM_WORK_START_LINE = "    /****** Custom accessor methods (or any other custom code) goes below this line: **************/";
	public static final String REVENG_LOG_SUPPRESS = "//<revengLogSuppress/>";

	public InstallRevengOutput(Boolean shouldMove, String parentPath, String projectName, String basePackageName, String projectPath) {
		this.doMoveInsteadOfCopy = shouldMove;
		this.projectParentRelativeDir = parentPath + "/";
		this.projectName = projectName;
		this.projectPath = projectPath;

		this.mainJavaPath = projectName + "/src/main/java";
		this.sublassTemplateFileLocation = projectName + "/reveng/myeclipse_templates_8.5/dao/SubclassTemplate.txt";
		this.mainProjectPath = mainJavaPath + "/" + projectPath;
		this.installDaoDir = mainProjectPath + "/dao";
		this.installDaoPackage = basePackageName + ".dao";
		this.installDaoBaseDir = installDaoDir + "/base";
		this.installDaoBasePackage = installDaoPackage + ".base";
		this.installPojoDir = mainProjectPath + "/model";
		this.installPojoPackage = basePackageName + ".model";
		this.revengOutputDir = mainProjectPath + "/reveng/tmp";
		this.revengOutputPackage = basePackageName + ".reveng.tmp";
	}

	/**
	 * @param args
	 */
	public void install() {
		try {

			List<File> migratedDaos = new ArrayList<File>(); // need these for
			// updating the
			// appContext
			List<File> migratedHbmXmls = new ArrayList<File>(); // need these
			// for updating
			// the
			// appContext

			// install reveng output
			File revengDir = new File(path(revengOutputDir));
			File[] revengFiles = revengDir.listFiles();
			// create the file using the template
			File tmpl = new File(projectParentRelativeDir + sublassTemplateFileLocation);

			for (File f : revengFiles) {
				if (f.isFile()) {
					String fileName = f.getName();
					if ("BaseHibernateDAO.java".equals(fileName) || "IBaseHibernateDAO.java".equals(fileName)) {
						continue;
					}
					if (fileName.endsWith("DAO.java")) {
						// check if we have a subclass
						String className = new String(fileName).replace("DAO.java", "DAO");
						String baseClassName = className.startsWith("Base") ? className : "Base" + className;

						/* DAO */
						File installFile = new File(path(installDaoBaseDir, baseClassName + ".java"));
						File activeRevengFile = f;
						boolean isMigration = false;

						// migrate dao imports and custom work, if exists
						if (installFile.exists()) {
							activeRevengFile = daoMigrator.createFileWithCodeMigratedFromExistingDao(installFile, f);
							migratedDaos.add(f);
							isMigration = true;
						}

						// update dao package name, model package names, and
						// deploy it to the dao dir
						replaceTextInCopiedFile(activeRevengFile, installFile,
								new String[][] {
										{ packageDeclaration(revengOutputPackage),
												packageDeclaration(installDaoBasePackage) },
										{ className, baseClassName } });

						// it's a temp file if was a migration, so delete it --
						// the orig reveng file remains as f
						if (isMigration) {
							activeRevengFile.delete();
						}

						File newSubclassFile = new File(
								projectParentRelativeDir + installDaoDir + "/" + className + ".java");
						if (!newSubclassFile.exists()) {
							copyFile(tmpl, newSubclassFile);
							System.out.println(String.format("Creating Subclass for %s...", fileName));
							replaceTextInFile(newSubclassFile, new String[][] { { "${className}", className },
									{ "${classNameExtends}", baseClassName } });
						}

					} else {

						/* POJO */

						// update pojo package name and deploy it to the model
						// dir
						replaceTextInCopiedFile(f, new File(path(installPojoDir, f.getName())), new String[][] {
								{ packageDeclaration(revengOutputPackage), packageDeclaration(installPojoPackage) } },
								1);
					}

					if (doMoveInsteadOfCopy) {
						f.delete();
					}

					System.out.println(String.format("File %s installed..", f.getName()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done.");
	}

	private String path(String path) {
		return new StringBuilder(projectParentRelativeDir).append(path).append("/").toString();
	}

	private String path(String path, String file) {
		return new StringBuilder(projectParentRelativeDir).append(path).append("/").append(file).toString();
	}

	private String packageDeclaration(String packageName) {
		return new StringBuilder("package ").append(packageName).append(";").toString();
	}

	public boolean copyFile(File source, File dest) throws IOException {
		boolean success = true;

		if (!dest.exists()) {
			dest.createNewFile();
		}

		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (Exception e) {
			success = false;
		} finally {
			in.close();
			out.close();
		}
		return success;
	}

	public void replaceTextInFile(File inFile, String[][] textReplacements) throws Exception {
		File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
		replaceTextInCopiedFile(inFile, tempFile, textReplacements);
		inFile.delete();
		copyFile(tempFile, inFile);
		tempFile.delete();
	}

	public void replaceTextInCopiedFile(File inFile, File outFile, String[][] textReplacements) {
		replaceTextInCopiedFile(inFile, outFile, textReplacements, 10000);
	}

	public void replaceTextInCopiedFile(File inFile, File outFile, String[][] textReplacements, int numReplacements) {
		try {
			if (outFile.exists()) {
				outFile.delete();
			}

			BufferedReader br = new BufferedReader(new FileReader(inFile));
			PrintWriter pw = new PrintWriter(new FileWriter(outFile));

			try {
				String line = null;
				int incrReplacements = 0;
				while ((line = br.readLine()) != null) {

					if (incrReplacements < numReplacements) {
						for (String[] textReplacement : textReplacements) {
							String replaceText = textReplacement[0];
							String newText = textReplacement[1];

							int replaceTextLength = replaceText.length();

							// find it
							int replaceTextIndexOf = line.indexOf(replaceText);

							if (replaceTextIndexOf != -1) {

								// replace it
								line = new StringBuilder(line.substring(0, replaceTextIndexOf)).append(newText)
										.append(line.substring(replaceTextIndexOf + replaceTextLength)).toString();
								incrReplacements++;
							}
						}
					}

					// write it
					pw.println(line);
					pw.flush();
				}
				if (inFile.exists()) {
					inFile.delete();
				}
			} finally {
				pw.close();
				br.close();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public class DAOMigrator {

		/**
		 * Update new file with import and custom code migrated from the
		 * existingFile.
		 *
		 * @return the temp file created for continued installation
		 */
		public File createFileWithCodeMigratedFromExistingDao(File existingFile, File newFile) throws Exception {
			File tempFile = new File(newFile.getAbsolutePath() + ".tmp");

			// consolidate imports from existing and new class files
			Set<String> importLineSet = new HashSet<String>();
			addImportsToSet(existingFile, importLineSet);
			addImportsToSet(newFile, importLineSet);
			List<String> sortedImportLineList = asSortedList(importLineSet);

			// get custom code from existing
			List<String> customWorkLines = getCustomLinesList(existingFile);

			// rebuild newFile with consolidated imports and to include custom
			// work
			BufferedReader br = new BufferedReader(new FileReader(newFile));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			try {
				// replace new imports with consolidated imports
				boolean printOriginalLine = true;
				boolean alreadyFoundImports = false;
				String nextLine = br.readLine();
				for (String line = nextLine, lastLine = null; (nextLine = br.readLine()) != null
						|| line != null; lastLine = line, line = nextLine) {
					// find imports block start
					if (!alreadyFoundImports && line.startsWith(REVENG_IMPORT_START_COMMENT)) {
						for (String importLine : sortedImportLineList) {
							pw.println(importLine);
							pw.flush();
						}
						printOriginalLine = false;
						alreadyFoundImports = true;
					}

					if (printOriginalLine == true) {
						// only copy file up to custom work block start
						if (line.startsWith(REVENG_CUSTOM_WORK_START_LINE)) {
							break;
						}

						// final filtering for original lines.
						boolean doPrint = true;

						// don't suppress unused warning on log if it is
						// actually used.
						if (line.endsWith(REVENG_LOG_SUPPRESS)) {
							doPrint = false;
						}
						if (line.trim().endsWith(SUPPRESS_WARNINGS_UNUSED) && lastLine.endsWith(REVENG_LOG_SUPPRESS)) {
							if (usesLogSomewhere(existingFile)) {
								doPrint = false;
							}
						}

						if (doPrint) {
							pw.println(line);
							pw.flush();
						}
					}

					if (alreadyFoundImports && !printOriginalLine && line.startsWith(REVENG_IMPORT_END_COMMENT)) {
						printOriginalLine = true;
					}
				}
				// finish off file with custom work from existing
				for (String line : customWorkLines) {
					pw.println(line);
					pw.flush();
				}
			} finally {
				pw.close();
				br.close();
			}

			return tempFile;
		}

		private void addImportsToSet(File f, Set<String> importLines) throws Exception {
			BufferedReader br = new BufferedReader(new FileReader(f));
			try {
				for (String line; (line = br.readLine()) != null;) {
					line = line.trim();
					if (line.startsWith("import ")) {
						importLines.add(line);
					}
				}
			} finally {
				br.close();
			}
		}

		private List<String> getCustomLinesList(File f) throws Exception {
			List<String> customWorkLines = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(f));
			try {
				boolean isCustomLine = false;
				for (String line; (line = br.readLine()) != null;) {
					if (!isCustomLine && line.startsWith(REVENG_CUSTOM_WORK_START_LINE)) {
						isCustomLine = true;
					}
					if (isCustomLine) {
						customWorkLines.add(line);
					}
				}
			} finally {
				br.close();
			}
			return customWorkLines;
		}

		private boolean usesLogSomewhere(File f) throws Exception {
			BufferedReader br = new BufferedReader(new FileReader(f));
			for (String line; (line = br.readLine()) != null;) {
				if (line.trim().startsWith(LOG_DOT)) {
					return true;
				}
			}
			return false;
		}

		public <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
			List<T> list = new ArrayList<T>(c);
			java.util.Collections.sort(list);
			return list;
		}
	}
}
