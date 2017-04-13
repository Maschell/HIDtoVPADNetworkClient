package de.mas.HIDTest;

import java.io.File;
import java.io.IOException;
import java.security.CodeSource;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 * Created by Reddit user king_of_the_universe / StackOverflow user Dreamspace President / dreamspace-president.com
 * <p>
 * v[(2), 2015-11-13 13:00 UTC]
 * <p>
 * One static method call will start a new instance of *THIS* application in the console and will EXIT the current instance. SO FAR ONLY WORKS ON WINDOWS! Users
 * of other systems need to assist here. The methods are all in place.
 */
final public class AutoRunFromConsole {

    final private static String FAILMESSAGE_TITLE = "Please run in console.";
    final private static String FAILMESSAGE_BODY = "This application must be run in the console (or \"command box\").\n\nIn there, you have to type:\n\njava -jar nameofprogram.jar";

    static void showFailMessageAndExit() {

        JOptionPane.showMessageDialog(null, FAILMESSAGE_BODY, FAILMESSAGE_TITLE, JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private enum OSType {
        UNDETERMINED, WINDOWS, LINUX, MACOS
    }

    private static OSType getOsType() {

        // final String osName = System.getProperty("os.name");
        // final String osVersion = System.getProperty("os.version");
        // final String osArchitecture = System.getProperty("os.arch");
        // System.out.println("\n\nOSNAME: " + osName);
        // System.out.println("\n\nOSVERSION: " + osVersion);
        // System.out.println("\n\nOSARCHITECTURE: " + osArchitecture);

        final String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.startsWith("windows")) {
            return OSType.WINDOWS;
        } else if (osName.startsWith("linux")) {
            return OSType.LINUX;
        } else if (osName.startsWith("mac os") || osName.startsWith("macos") || osName.startsWith("darwin")) {
            return OSType.MACOS;
        }

        return OSType.UNDETERMINED;
    }

    /**
     * Checks if the program is currently running in console, and if not, starts the program from console and EXITS this instance of the program. Should be (one
     * of) the first calls in your program.
     * <p>
     * This is the less safe variant of the method: To check if you're currently in the IDE, it just tries to find the executable name and if it exists in the
     * current path. This should word perfectly at all times in IntelliJ - I don't know what values getExecutableName() returns inside Eclipse, but I suspect it
     * will work just as well.
     * <p>
     * It's also less safe because you can't give a fallback executable name, but I believe it should do the trick in all situations.
     * <p>
     * If this is used on a system other than Windows, a message box is shown telling the user to start the program from the console. BECAUSE I DON'T KNOW HOW
     * TO OPEN A CONSOLE ON OTHER SYSTEMS. SEE startExecutableInConsole();
     */
    public static void runYourselfInConsole(final boolean stayOpenAfterEnd) {

        runYourselfInConsole(false, stayOpenAfterEnd, null, null);
    }

    /**
     * Checks if the program is currently running in console, and if not, starts the program from console and EXITS this instance of the program. Should be (one
     * of) the first calls in your program.
     * <p>
     * This is the safer variant of the method: The first command line argument GIVEN BY THE IDE'S RUN CONFIGURATION should be "ide" (Case is ignored.), which
     * this method will use to determine if it's running from the IDE.
     * <p>
     * It is also safer because you can give a fallback executable name in case getExecutableName() could not determine it.
     * <p>
     * Ultimately, it is safer because if the executable could not be determined, it shows a message box telling the user to start the program from the console.
     * <p>
     * You will probably never make use of this variant. It's meant to be a solution if all else seems to fail (e.g. customer calls and you need a quick fix).
     * <p>
     * If this is used on a system other than Windows, a message box is shown telling the user to start the program from the console. BECAUSE I DON'T KNOW HOW
     * TO OPEN A CONSOLE ON OTHER SYSTEMS. SEE startExecutableInConsole();
     *
     * @param psvmArguments
     *            The arguments given to the main method.
     * @param fallbackExecutableName
     *            Can be null. In case getExecutableName() can't determine the proper name, the fallback is used.
     */
    public static void runYourselfInConsole(final String[] psvmArguments, final String fallbackExecutableName, final boolean stayOpenAfterEnd) {

        runYourselfInConsole(true, stayOpenAfterEnd, psvmArguments, fallbackExecutableName);
    }

    /**
     * see the other two methods
     */
    private static void runYourselfInConsole(final boolean useSaferApproach, final boolean stayOpenAfterEnd, final String[] psvmArguments,
            final String fallbackExecutableName) {

        String executableName = getExecutableName(fallbackExecutableName);

        if (useSaferApproach) {
            if (isRunFromIDE(psvmArguments)) {
                return;
            }
        } else {
            if (executableName == null) {
                // Running from IDE.
                return;
            }
        }

        if (isRunningInConsole()) {
            return;
        }

        if (executableName == null) {
            showFailMessageAndExit();
        }

        startExecutableInConsole(executableName, stayOpenAfterEnd);

        System.exit(0);
    }

    /**
     * Opens a console window and starts the Java executable there.
     * <p>
     * If this is used on a system other than Windows, a message box is shown telling the user to start the program from the console. BECAUSE I DON'T KNOW HOW
     * TO OPEN A CONSOLE ON OTHER SYSTEMS.
     *
     * @param executableName
     *            the full file name of the executable (without path)
     * @param stayOpenAfterEnd
     *            If true (and if someone can figure out the necessary parameters for other systems than Windows), the console will not close once the
     *            executable has terminated. This is useful e.g. if you want to give some kind of bye bye message because you actually assumed that people start
     *            the program from console manually.
     */
    private static void startExecutableInConsole(final String executableName, final boolean stayOpenAfterEnd) {

        String launchString = null;

        switch (getOsType()) {
        case UNDETERMINED:
            break;
        case WINDOWS:
            if (stayOpenAfterEnd) {
                launchString = "cmd /c start cmd /k java -jar \"" + executableName + "\""; // No, using /k directly here DOES NOT do the trick.
            } else {
                launchString = "cmd /c start java -jar \"" + executableName + "\"";
            }
            break;
        case LINUX:
            break;
        case MACOS:
            // launchString="/usr/bin/open -a Terminal /path/to/the/executable";
            break;
        }

        if (launchString == null) {
            showFailMessageAndExit();
        }

        try {
            Runtime.getRuntime().exec(launchString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     *            the args as given to PSVM
     * @return whether the first command line argument was "ide" (ignoring case). Don't forget to change your IDE's run configuration accordingly.
     */
    private static boolean isRunFromIDE(final String[] args) {

        return args != null && args.length > 0 && args[0].equalsIgnoreCase("ide");
    }

    /**
     * @return if System.console() is available. DOES NOT WORK properly from IDE, will return false then even though it should be true. Use isRunFromIDE or
     *         other means additionally.
     */
    private static boolean isRunningInConsole() {

        return System.console() != null;
    }

    /**
     * @param fallbackExecutableName
     *            Can be null. In the very unlikely case this method can't determine the executable, the fallback will also be checked. But if the fallback also
     *            doesn't exist AS A FILE in the CURRENT path, null will be returned regardless, even if you're sure that your fallback should be correct.
     * @return the name of the running jar file, OR NULL if it could not be determined (which should be a certainty while in IDE, hence can be abused for
     *         determining that).
     */
    public static String getExecutableName(final String fallbackExecutableName) {

        // APPROACH 1 - THE ONE EVERYBODY ON STACKOVERFLOW IS REPEATING
        String executableNameFromClass = null;
        final CodeSource codeSource = AutoRunFromConsole.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            System.err.println("UNEXPECTED: Main.class.getProtectionDomain().getCodeSource() returned null");
        } else {
            final String path = codeSource.getLocation().getPath();
            if (path == null || path.isEmpty()) {
                System.err.println("UNEXPECTED: codeSource.getLocation().getPath() returned null or empty");
            } else {

                executableNameFromClass = new File(path).getName();

            }
        }

        // APPROACH 2 - QUERY SYSTEM PROPERTIES
        final Properties properties = System.getProperties();
        final String executableNameFromJavaClassPathProperty = properties.getProperty("java.class.path");
        final String executableNameFromSunJavaCommandProperty = properties.getProperty("sun.java.command");

        // System.out.println("\n\nexecutableNameFromClass:\n" + executableNameFromClass);
        // System.out.println("\n\nexecutableNameFromJavaClassPathProperty:\n" + executableNameFromJavaClassPathProperty);
        // System.out.println("\n\nexecutableNameFromSunJavaCommandProperty:\n" + executableNameFromSunJavaCommandProperty);
        // System.out.println("\n\nfallbackExecutableName:\n" + fallbackExecutableName);

        if (isThisProbablyTheExecutable(executableNameFromClass)) {
            return executableNameFromClass;
        }

        if (isThisProbablyTheExecutable(executableNameFromJavaClassPathProperty)) {
            return executableNameFromJavaClassPathProperty;
        }

        if (isThisProbablyTheExecutable(executableNameFromSunJavaCommandProperty)) {
            return executableNameFromSunJavaCommandProperty;
        }

        if (isThisProbablyTheExecutable(fallbackExecutableName)) {
            return fallbackExecutableName;
        }

        return null;
    }

    /**
     * @param candidateName
     *            suspected name of the running java executable
     * @return if name is not null, ends with ".jar" (Case is ignored.), and points to a FILE existing in the CURRENT directory.
     */
    private static boolean isThisProbablyTheExecutable(final String candidateName) {

        if (candidateName == null || !candidateName.toLowerCase().endsWith(".jar")) {
            return false;
        }

        final File file = new File(candidateName);
        return file.exists() && file.isFile();
    }
}