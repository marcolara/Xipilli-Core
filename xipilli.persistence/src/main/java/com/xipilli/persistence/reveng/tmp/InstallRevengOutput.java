package com.xipilli.persistence.reveng.tmp;

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

	// settings
	public static final boolean	DO_MOVE_INSTEAD_OF_COPY				= false;

	// paths
	public static final String	PROJECT_PARENT_RELATIVE_DIR			= "/Users/marcolara/Dropbox/Documents/Workspaces/MyEclipse4S/";
	public static final String	PROJECT_NAME						= "tattoosoft.persistence";
	public static final String	SUBCLASS_TEMPLATE_FILE_LOCATION		= "tattoosoft.persistence/reveng/SubclassTemplate.txt";

	public static final String	REVENG_OUTPUT_DIR					= PROJECT_NAME + "/src/main/java/tattoosoft/persistence/dao/base";
	public static final String	REVENG_OUTPUT_PACKAGE				= "tattoosoft.persistence.dao.base";
	public static final String	REVENG_OUTPUT_MAPPING_RESOURCE_URI	= "tattoosoft/persistence/dao/base/";
	public static final String	INSTALL_DAO_DIR						= PROJECT_NAME + "/src/main/java/tattoosoft/persistence/dao";
	public static final String	INSTALL_DAO_PACKAGE					= "tattoosoft.persistence.dao";
	public static final String	INSTALL_DAO_BASE_DIR				= INSTALL_DAO_DIR + "/base";
	public static final String	INSTALL_DAO_BASE_PACKAGE			= INSTALL_DAO_PACKAGE + ".base";
	public static final String	INSTALL_DAO_MAPPING_RESOURCE_URI	= "tattoosoft/persistence/dao/";
	public static final String	INSTALL_POJO_DIR					= PROJECT_NAME + "/src/main/java/tattoosoft/persistence/model";
	public static final String	INSTALL_POJO_PACKAGE				= "tattoosoft.persistence.model";

	public static final String	PERSISTENCE_APPLICATION_CONTEXT		= "tattoosoft.persistence/src/main/resources/applicationContext-persistence.xml";

	// abstract helper patterns
	public static final String	LOG_DOT								= "log.";
	public static final String	SUPPRESS_WARNINGS_UNUSED			= "@SuppressWarnings(\"unused\")";

	// demarcations in the reveng output files to help migration and conditional
	// formatting
	public static final String	REVENG_IMPORT_START_COMMENT			= "//<revengImports>";
	public static final String	REVENG_IMPORT_END_COMMENT			= "//</revengImports>";
	public static final String	REVENG_CUSTOM_WORK_START_LINE		= "    /****** Custom accessor methods (or any other custom code) goes below this line: **************/";
	public static final String	REVENG_LOG_SUPPRESS					= "//<revengLogSuppress/>";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InstallRevengOutput output = new InstallRevengOutput();
		output.install();
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
			File revengDir = new File(path(REVENG_OUTPUT_DIR));
			File[] revengFiles = revengDir.listFiles();
			// create the file using the template
			File tmpl = new File(PROJECT_PARENT_RELATIVE_DIR + SUBCLASS_TEMPLATE_FILE_LOCATION);

			for (File f : revengFiles) {
				if (f.isFile()) {
					String fileName = f.getName();
					if ("BaseHibernateDAO.java".equals(fileName) || "IBaseHibernateDAO.java".equals(fileName)){
						continue;
					}
					if (fileName.endsWith("DAO.java")) {
						// check if we have a subclass
						String className = new String(fileName).replace("DAO.java","DAO");
						String baseClassName = className.startsWith("Base") ? className : "Base" + className;
						
						/* DAO */

						File installFile = new File(path(INSTALL_DAO_BASE_DIR, baseClassName + ".java"));
						File activeRevengFile = f;
						boolean isMigration = false;

						// migrate dao imports and custom work, if exists
						if (installFile.exists()) {
							activeRevengFile = DAOMigrator.createFileWithCodeMigratedFromExistingDao(installFile, f);
							migratedDaos.add(f);
							isMigration = true;
						}

						// update dao package name, model package names, and
						// deploy it to the dao dir
						replaceTextInCopiedFile(activeRevengFile, installFile, new String[][]{
								{packageDeclaration(REVENG_OUTPUT_PACKAGE), packageDeclaration(INSTALL_DAO_BASE_PACKAGE)},{className,baseClassName}});

						// it's a temp file if was a migration, so delete it --
						// the orig reveng file remains as f
						if (isMigration) {
							activeRevengFile.delete();
						}


						File newSubclassFile = new File(PROJECT_PARENT_RELATIVE_DIR + INSTALL_DAO_DIR + "/" + className + ".java");
						boolean exists = newSubclassFile.exists();
						if (!exists) {
							copyFile(tmpl, newSubclassFile);
							System.out.println(String.format("Creating Subclass for %s...", fileName));
							replaceTextInFile(newSubclassFile, new String[][]{{"${className}", className},{"${classNameExtends}", baseClassName}});
						}

					} else {

						/* POJO */

						// update pojo package name and deploy it to the model
						// dir
						replaceTextInCopiedFile(f, new File(path(INSTALL_POJO_DIR, f.getName())), new String[][]{{packageDeclaration(REVENG_OUTPUT_PACKAGE),
								packageDeclaration(INSTALL_POJO_PACKAGE)}}, 1);
					}

					if (DO_MOVE_INSTEAD_OF_COPY) {
						f.delete();
					}

					System.out.println(String.format("File %s installed..", f.getName()));
				}
			}

			// update application context
			File appContextFile = new File(PROJECT_PARENT_RELATIVE_DIR + PERSISTENCE_APPLICATION_CONTEXT);
			System.out.println(String.format("Migrating %s...", appContextFile.getName()));
			File newAppContextFile = AppContextMigrator.createMigration(appContextFile, migratedDaos);
			appContextFile.delete();
			replaceTextInCopiedFile(newAppContextFile, appContextFile, new String[][]{{REVENG_OUTPUT_MAPPING_RESOURCE_URI, INSTALL_DAO_MAPPING_RESOURCE_URI},
					{REVENG_OUTPUT_PACKAGE, INSTALL_DAO_BASE_PACKAGE}});
			newAppContextFile.delete();

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("done.");
	}

	private static String path(String path) {
		return new StringBuilder(PROJECT_PARENT_RELATIVE_DIR).append(path).append("/").toString();
	}

	private static String path(String path, String file) {
		return new StringBuilder(PROJECT_PARENT_RELATIVE_DIR).append(path).append("/").append(file).toString();
	}

	private static String packageDeclaration(String packageName) {
		return new StringBuilder("package ").append(packageName).append(";").toString();
	}

	private static String mapping(String uri, String entityName) {
		return new StringBuilder(uri).append(entityName).append(".hbm.xml").toString();
	}

	public static boolean copyFile(File source, File dest) throws IOException {
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

	public static void replaceTextInFile(File inFile, String[][] textReplacements) throws Exception {
		File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
		replaceTextInCopiedFile(inFile, tempFile, textReplacements);
		inFile.delete();
		copyFile(tempFile, inFile);
		tempFile.delete();
	}

	public static void replaceTextInCopiedFile(File inFile, File outFile, String[][] textReplacements) {
		replaceTextInCopiedFile(inFile, outFile, textReplacements, 10000);
	}

	public static void replaceTextInCopiedFile(File inFile, File outFile, String[][] textReplacements, int numReplacements) {
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

	public static class DAOMigrator {

		/**
		 * Update new file with import and custom code migrated from the
		 * existingFile.
		 *
		 * @return the temp file created for continued installation
		 */
		public static File createFileWithCodeMigratedFromExistingDao(File existingFile, File newFile) throws Exception {
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
				for (String line = nextLine, lastLine = null; (nextLine = br.readLine()) != null || line != null; lastLine = line, line = nextLine) {
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

		private static void addImportsToSet(File f, Set<String> importLines) throws Exception {
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

		private static List<String> getCustomLinesList(File f) throws Exception {
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

		private static boolean usesLogSomewhere(File f) throws Exception {
			BufferedReader br = new BufferedReader(new FileReader(f));
			for (String line; (line = br.readLine()) != null;) {
				if (line.trim().startsWith(LOG_DOT)) {
					return true;
				}
			}
			return false;
		}

		public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
			List<T> list = new ArrayList<T>(c);
			java.util.Collections.sort(list);
			return list;
		}
	}

	public static class AppContextMigrator {

		public static File createMigration(File inFile, List<File> migratedDaos) throws Exception {

			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(inFile));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			try {

				/**
				 * All reveng'd entities have a new url mapping appended to a
				 * list in the context, so filter those out for entities that
				 * were merely migrated. Then, replace all remaining erroneous
				 * reveng uris with the install uri to handle new entities.
				 */

				Set<String> migratedMappingUriValueElementSet = toMappingUriValueElementSet(migratedDaos);

				for (String line; (line = br.readLine()) != null;) {
					boolean doPrint = true;

					String comparisonString = line.trim();

					// sometimes the line includes the final </list> element
					int closeListElIndex = comparisonString.indexOf("</list>");
					if (closeListElIndex != -1) {
						comparisonString = comparisonString.substring(0, closeListElIndex).trim();
					}

					// use the comparison string as the key for identification
					if (migratedMappingUriValueElementSet.contains(comparisonString)) {
						if (closeListElIndex == -1) {
							doPrint = false;
						} else {
							line = "      </list>";
						}
						System.out.println(String.format("+ Install entry already exists, so filtered out %s.", comparisonString));
					}

					if (doPrint) {
						pw.println(line);
						pw.flush();
					}
				}

			} finally {
				pw.close();
				br.close();
			}

			return tempFile;
		}

		/**
		 * Get the mapping uris into a set so we can just check the hash key for
		 * matches
		 */
		private static Set<String> toMappingUriValueElementSet(List<File> daoFiles) {
			Set<String> set = new HashSet<String>();
			for (File daoFile : daoFiles) {
				String fileName = daoFile.getName();
				String entityName = fileName.substring(0, fileName.indexOf("DAO."));
				String revengMappingUri = mapping(REVENG_OUTPUT_MAPPING_RESOURCE_URI, entityName);
				set.add(valueElement(revengMappingUri));
			}
			return set;
		}

		private static String valueElement(String uri) {
			return new StringBuffer("<value>").append(uri).append("</value>").toString();
		}
	}

}
