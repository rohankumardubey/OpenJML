package org.jmlspecs.openjml.jmldoc;

import java.io.PrintWriter;
import java.util.LinkedList;

import org.jmlspecs.annotations.NonNull;
import org.jmlspecs.openjml.JmlOptionName;
import org.jmlspecs.openjml.JmlSpecs;
import org.jmlspecs.openjml.Utils;
import org.jmlspecs.openjml.Main.IProgressReporter;
import org.jmlspecs.openjml.Main.PrintProgressReporter;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JavacMessages;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javadoc.Start;

/** This is the class containing the entry points for the jmldoc tool. 
 * See the material in the <A HREF="package-summary.html">package description</A> for use and design discussion. */
public class Main extends org.jmlspecs.openjml.Main {
    
    // THESE ARE NOT (YET?) USED  // FIXME
    static public String JmlTableHeadingColor = "#FFCCCC";
    static public String JmlTableRowColor = "#FFFFCC";

    /** The entry point for command-line execution of the tool.  The conventional
     * array of arguments contains options and their arguments, files to be 
     * documented and any annotation processing classes.
     * 
     * @param args the command-line arguments
     */
    //@ requires \nonnullelements(args);
    public static void main(@NonNull String[] args) {
        System.exit(execute(args));
    }

    /** The compilation context used for JML parsing (which is different than
     * the context used for javadoc parsing - see the
     * <A HREF="package-summary.html">package documentation</A>.
     */
    public static Context jmlContext = new Context(); // FIXME - not thread safe

    /**
     * The programmatic interface, used to execute the tool on a set of options
     * and files.  The return value is the value used as the exit code on the
     * command-line.
     * @param args   The command line parameters.
     * @return The return code.
     */
    //@ requires \nonnullelements(args);
    public static int execute(@NonNull String[] args) {
        new org.jmlspecs.openjml.jmldoc.ConfigurationJml();  // stores an instance in a static location
        int errorcode = 1; //com.sun.tools.javac.main.Main.EXIT_ERROR;
        try {
            if (args == null) {
                Context context = new Context(); // This is a temporary context just for this error message.
                                    // It is not the one used for the options and compilation
                Log log = Log.instance(context);
                JavacMessages.instance(context).add(Utils.messagesJML);
                log.error("jmldoc.main.null.args","org.jmlspecs.openjml.jmldoc.Main");
                errorcode = 2; // com.sun.tools.javac.main.Main.EXIT_CMDERR;
            } else {
                // We have to interpret the -useJavaCompiler option before we start
                // the compiler (which does the normal option processing).
                // Since this is rare, we'll require that it be the first
                // option.
                boolean useJavaCompiler = args.length > 0 &&
                args[0].equals(JmlOptionName.USEJAVACOMPILER.optionName());
                if (useJavaCompiler) {
                    errorcode = com.sun.tools.javac.Main.compile(args);
                } else {
                    jmlContext = new Context();
                    jmlContext.put(IProgressReporter.class,new PrintProgressReporter(jmlContext,System.out));
                    Start jdoc = new JmlStart("jmldoc");//,"org.jmlspecs.openjml.jmldoc.StandardJml");
                    registerTools(jmlContext,new PrintWriter(System.out,true),null);
                    JavacFileManager.preRegister(jmlContext); // can't create it until Log has been set up - is it? FIXME
                    args = processArgs(args,jmlContext);
                    errorcode = jdoc.begin(args);
                    if (Utils.jmldebug || errorcode != 0) System.out.println("ENDING with exit code " + errorcode);
                }
            }
        } catch (Exception e) {
            // Most exceptions are caught prior to this, so this will happen only for the
            // most catastrophic kinds of failure such as failures to initialize
            // properly.  (You can test this by programmatically throwing an exception in the try
            // block above.)
            Context context = new Context(); // This is a temporary context just for this error message.
            // It is not the one used for the options and compilation
            Log log = Log.instance(context);
            JavacMessages.instance(context).add(Utils.messagesJML);
            log.error("jmldoc.toplevel.exception",e);
            //e.printStackTrace(System.err); // FIXME - enable in verbose mode?
            errorcode = 3; //com.sun.tools.javac.main.Main.EXIT_SYSERR;
        }
        return errorcode;
    }
    
    /** Internal method used to process and remove from the argument list any
     * jmldoc specific arguments.  This is called prior to the arguments being
     * processed by the Javadoc tool itself.
     * @param args the command-line arguments
     * @param context the compiler context in which the arguments apply
     * @return a reduced list of command-line arguments
     */
    //@ requires \nonnullelements(args);
    //@ ensures \nonnullelements(\result);
    static public @NonNull String[] processArgs(@NonNull String[] args, @NonNull Context context) {
        Options options = Options.instance(context);
        JmlOptionName n=null;
        LinkedList<String> newargs = new LinkedList<String>();
        int cpindex = -1;
        int i = 0;
        while (i < args.length) {
            String a = args[i];
            i++;
            n = JmlOptionName.find(a);
            if (n != null) {
                if (n.hasArg()) {
                    options.put(n.optionName(),args[i++]);
                } else {
                    options.put(n.optionName(),"");
                }
            } else if (a.equals("-classpath")) {
                newargs.add(a);
                options.put(a,args[i]);
                cpindex = newargs.size();
                newargs.add(args[i++]);
            } else if (a.equals("-sourcepath")) {
                newargs.add(a);
                options.put(a,args[i]);
                newargs.add(args[i++]);
            } else {
                newargs.add(a);
            }
        }
        // FIXME - some cleanup needed here
        //args = processJmlArgs(args,Options.instance(context));
        //List<File> files = super.processArgs(args);
        Utils.jmldebug = Options.instance(context).get(JmlOptionName.JMLDEBUG.optionName()) != null; 
        JmlSpecs.instance(context).initializeSpecsPath();
        if (!JmlOptionName.isOption(context,JmlOptionName.NOINTERNALRUNTIME)) {
            JmlSpecs.instance(context).appendRuntime();
        }
        String[] result = newargs.toArray(new String[newargs.size()]);
        // append annotation definitions onto result[cpindex] - what if there is none - need to append it to classpath definition
        return result;
    }


}

/* TODO
 * 
 GENERAL:
 *** Clean up all files
 * Should we use the .xml to produce the JML
 * 
 LAYOUT:
 * Extra space around PRE
 * Font for annotations
 * Font and color for specs
 * Indentation of JML specs for methods depends on presence of javadocs (and tags?)
 * Extra horizontal rule after the JML class specifications; spacing in general
 * Rationalize newlines in annotations

 ENVIRONMENT
 *** Auto inclusion of annotations on the class path for the javadoc part
 
 INCOMPLETE
 *** Verify all pretty printing works
 * Pretty printing (as in the source) and wrapping of long expressions

 DOCUMENTING MODEL CLASSES:
 * Toplevel Model classes 
 * Generate pages and links for model classes, interfaces - link them into package listings and next/previous links
 * model classes: inherited specs
 * Javadoc comments for model classes and interfaces

 EMPTY MEMBER LIST PROBLEM:
 ** CEmpty: Need jml field summary when there are no java fields
 ** CEmpty: need jml nested class summary when there are no java nested classes
 ** JML inherited fields/methods are not shown if there are no Java inherited fields/methods

 OTHER MISSING DOC
 * Method: arguments and results do not have JML comments
 * Fields: JML comments do not turn into annotations
 * Classes, Nested classes: check that JML comments are listed as annotations
 ** Show specification sequence; show location of specs in spec sequence?
 * JML elements are not in index and in tree and in use and in deprecated

 OTHER
 * Read BML
 * Create jmldoc from binary + spec files
 * Test auxiliary jml files
 * Test redundant; test heavyweight clauses

 JAVADOC FLAGS
 * Test use of -noqualifier, -use, -overview, -sourcepath, -classpath, -specs
 * Test use of -nodeprecated, -nodeprecatedlist, -notree, -noindex, -sourcefile (links in refines?)
 * Test use of -nocomment on JML documentation
 
 BUGS 
 ** Enum bug - documents, but reports errors
 *** standalone bug - what is this?
 ** classpath bug (testSourcePath2?)
 ** class A should list methods inherited from BB - problem when using -protected
 *** Nested model class containing a method declaration with no body - whether or not is model
 ** Crashes that occurred when Comparable.jml was bad
 * Test: visibility with hiding of inherited members
 *** Poor recovery from parsing errors
 *** Does not support anonymous classes within JML annotations
 ** quantifiers and lbl expressions without parentheses
 *** White space in .. ranges
 *
 ** retest/report bug in DocEnv with unnamed packages
 
 * 
 * Note: problem with javadocs applying to both Jml and Java elements
 * Note: including annotations in signatures of summaries, even though javadoc does not
 * Note: synonyms are mapped to a canonical name (e.g. pre -> requires)
 * 
 * */
