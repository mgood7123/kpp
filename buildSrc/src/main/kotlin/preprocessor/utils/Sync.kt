package preprocessor.utils

import preprocessor.base.globalVariables
import preprocessor.core.macroList
import preprocessor.core.process
import java.io.File
import preprocessor.utils.core.*
import java.nio.file.Files

/**
 *
 *//*
if A/FILE exists
    if B/FILE does not exist
        delete A/FILE
            if A/FILE.PRO exists
                delete A/FILE.PRO
    else
        if A/FILE contains DATA
            KEEP A/FILE
            if A/FILE.PRO exists
                if A/FILE contains DATA
                    KEEP A/FILE.PRO
        else delete A/FILE
            if A/FILE.PRO exists
                delete A/FILE.PRO
else if B/FILE exists
    if B/FILE contains DATA
        copy B/FILE to A/FILE
 */
class Sync {

    private val ignore: ArrayList<String> = arrayListOf(
        // ignore png files
        "png",
        // ignore proguard files
        "pro",
        // ignore gradle files
        "gradle",
        // ignore module files
        "iml",
        // ignore git files
        "gitignore"
    )

    /**
     * syncs directory **B** (**src**) with directory **A** (**dir**)
     *
     * this needs to be called BEFORE [syncA] in order to sync correctly
     * @see syncA
     * @sample findSourceFiles
     */
    private fun syncB(dir: File, src: File, extension: String? = null) {
        dir.listFiles().forEach {
            val a = it
            val b = File(
                // Line is longer than allowed by code style (> 120 columns)
                globalVariables.rootDirectory.toString() + '/' + basename(globalVariables.kppDir) +
                        a.toString().removePrefix(globalVariables.rootDirectory!!.path)
            )
            println("A :     $a")
            println("B : $b")
            if (a.exists()) {
                if (a.isDirectory) {
                    // ignore build dir in A
                    // if build dir exists in B, delete it
                    val blocked = a.toString() == globalVariables.projectDirectory?.path + "/build"
                    if (!blocked) {
                        println("entering $a")
                        syncB(a, src, extension)
                        println("leaving $a")
                    } else println("A is blocked")
                    if (b.exists()) {
                        if (blocked) {
                            println("deleting B")
                            deleteRecursive(b)
                        } else if (empty(b)) {
                            println("deleting B")
                            delete(b)
                        }
                    }
                    val aPreProcessed = File(a.path + globalVariables.preprocessedExtension + "." + a.extension)
                    val bPreProcessed = File(b.path + globalVariables.preprocessedExtension + "." + b.extension)
                    if (aPreProcessed.exists()) {
                        println("deleting preprocessing file associated with A")
                        delete(aPreProcessed)
                    }
                    if (bPreProcessed.exists()) {
                        println("deleting preprocessing file associated with B")
                        delete(bPreProcessed)
                    }
                } else if (a.isFile) {
                    if (a.path.endsWith(globalVariables.preprocessedExtension + "." + a.extension)) {
                        println("A is preprocessor file")
                        println("deleting A")
                        delete(a)
                    } else if (!ignore.contains(a.extension)) {
                        // if extension is null, test every file
                        // ignore these extensions
                        if (it.extension == extension || extension == null) {
                            if (!b.exists()) {
                                if (testFile(a!!)) {
                                    println("copying A to B")
                                    if (!cp(a.path, b.path, true))
                                        abort("failed to copy $a to $b")
                                } else {
                                    println("B does not exist however A does not contain DATA")
                                    println("B cannot be deleted as it does not exist")
                                    println("A will not be copied to B")
                                }
                            } else {
                                if (!testFile(b)) {
                                    println("B exists however does not contains DATA")
                                    println("deleting B")
                                    delete(b)
                                } else println("B contains DATA")
                            }
                        }
                    } else {
                        println("ignoring extension: ${a.extension}")
                        if (b.exists()) {
                            println("A is ignoring but B exists, deleting B")
                            delete(b)
                        }
                        val aPreProcessed = File(a.path + globalVariables.preprocessedExtension + "." + a.extension)
                        val bPreProcessed = File(b.path + globalVariables.preprocessedExtension + "." + b.extension)
                        if (aPreProcessed.exists()) {
                            println("deleting preprocessing file associated with A")
                            delete(aPreProcessed)
                        }
                        if (bPreProcessed.exists()) {
                            println("deleting preprocessing file associated with B")
                            delete(bPreProcessed)
                        }
                    }
                }
            } else {
                println("A does not exist")
            }
        }
    }

    /**
     * syncs directory **A** (**dir**) with directory **B** (**src**)
     *
     * this needs to be called AFTER [syncB] in order to sync correctly
     * @see syncB
     * @sample findSourceFiles
     */
    private fun syncA(dir: File, src: File, extension: String? = null) {
        dir.listFiles().forEach {
            val a = it
            val b = File(
                globalVariables.rootDirectory.toString() + '/' + a.toString().removePrefix(globalVariables.kppDir!!)
            )
            println("A : $a")
            println("B :     $b")
            run returnPoint@{
                if (a.exists()) {
                    if (a.isDirectory) {
                        // ignore build dir in A
                        // if build dir exists in B, delete it
                        val blocked = a.toString() == globalVariables.projectDirectory?.path + "/build"
                        if (!blocked) {
                            if (b.path == globalVariables.kppDir) {
                                println("error: B is kpp dir")
                                println("kpp should not contain its own directory")
                                println("deleting A")
                                deleteRecursive(a)
                                println("returning to @returnPoint")
                                return@returnPoint
                            } else {
                                if (b.exists()) {
                                    println("entering $a")
                                    syncA(a, src, extension)
                                    println("leaving $a")
                                    if (empty(a)) {
                                        println("deleting A")
                                        delete(a)
                                        println("returning to @returnPoint")
                                        return@returnPoint
                                    }
                                } else {
                                    println("B does not exist")
                                    println("deleting A")
                                    deleteRecursive(a)
                                    println("returning to @returnPoint")
                                    return@returnPoint
                                }
                            }
                        } else println("A is blocked")
                        if (b.exists()) {
                            if (blocked) {
                                println("deleting A")
                                deleteRecursive(a)
                                println("returning to @returnPoint")
                                return@returnPoint
                            } else if (empty(a)) {
                                println("deleting A")
                                delete(a)
                                println("returning to @returnPoint")
                                return@returnPoint
                            }
                        }
                    } else if (a.isFile) {
                        // if B does not exist, delete A
                        if (!b.exists()) {
                            println("B does not exist")
                            println("deleting A")
                            delete(a)
                            println("returning to @returnPoint")
                            return@returnPoint
                        }
                        // if extension is null, test every file
                        // ignore these extensions
                        if (!ignore.contains(a.extension)) {
                            if (it.extension == extension || extension == null) {
                                if (!testFile(a!!)) {
                                    println("A exists however does not contains DATA")
                                    if (!a.path.endsWith(globalVariables.preprocessedExtension + "." + extension)) {
                                        println("moving A to B")
                                        if (!mv(a.path, b.path, verbose = true, overwrite = true))
                                            abort()
                                    } else {
                                        println("A is preprocessor file")
                                        println("moving A to B (renamed)")
                                        if (!mv(
                                                a.path,
                                                // Line is longer than allowed by code style (> 120 columns)
                                                b.path.removeSuffix(
                                                    "." +
                                                            globalVariables.preprocessedExtension +
                                                            "." +
                                                            extension
                                                ),
                                                verbose = true,
                                                overwrite = true
                                            )
                                        )
                                            abort()
                                    }
                                    println("returning to @returnPoint")
                                    return@returnPoint
                                } else {
                                    println("A contains DATA")
                                    println("processing A")
                                    if (a.extension == "") {
                                        println("error: cannot process a file with no extension")
                                        return@returnPoint
                                    }
                                    process(a.path, a.extension, globalVariables.kppMacroList)
                                    println("moving resulting preprocessing file A to B (renamed)")
                                    if (!mv(
                                            a.path + globalVariables.preprocessedExtension + "." + a.extension,
                                            b.path,
                                            verbose = true,
                                            overwrite = true
                                        )
                                    )
                                        println("returning to @returnPoint")
                                    return@returnPoint
                                }
                            }
                        } else {
                            println("ignoring extension: ${a.extension}")
                            println("returning to @returnPoint")
                            return@returnPoint
                        }
                    }
                } else {
                    println("A does not exist")
                }
            }
        }
    }

    /**
     * self explanatory
     *
     * this function finds and processes all source files in the directory **dir** with the extension **extension**
     * @param dir the directory to search in
     * @param extension the extension that each file must end
     * @param extension if **null** then any file is accepted
     * @sample findSourceFilesSample
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun findSourceFiles(dir: File, extension: String? = null) {
        // sync dir with kppDir
        syncB(dir, globalVariables.kppDirAsFile as File, extension)
        // sync kppDir with dir, calling process on a valid processing file
        syncA(globalVariables.kppDirAsFile as File, dir, extension)
    }


    /**
     * self explanatory
     *
     * this function finds and processes all source files in the directory **dir** with the extension **extension**
     * @param dir the directory to search in
     * @param extension the extension that each file must end
     * @param extension if **null** then any file is accepted
     * @sample findSourceFilesSample
     */

    @Suppress("MemberVisibilityCanBePrivate")
    fun findSourceFilesOrNull(dir: File?, extension: String? = null) {
        if (dir == null) abort("dir cannot be null")
        findSourceFiles(dir, extension)
    }

    /**
     * self explanatory
     *
     * this function finds and processes all source files in the directory **dir** with the extension **extension**
     * @param dir the directory to search in
     * @param extension the extension that each file must end
     * @param extension if **null** then any file is accepted
     * @sample findSourceFilesSample
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun findSourceFiles(dir: String, extension: String? = null) {
        findSourceFiles(File(dir), extension)
    }

    private fun findSourceFilesSample() {
        val path =
            globalVariables.projectDirectory.toString()
        // find all source files with kotlin extension
        findSourceFiles(path, "kt")
        // find all source files, regardless of its extension
        findSourceFiles(path)
    }

    /**
     * test if file **src** contains any preprocessor directives
     */
    private fun testFile(src: File): Boolean {
        globalVariables.currentFileContainsPreprocessor = false
        println("testing file: ${src.path}")
        val lines: List<String> = Files.readAllLines(src.toPath())
        lines.forEach { line ->
            checkIfPreprocessorIsNeeded(line)
        }
        return globalVariables.currentFileContainsPreprocessor
    }

    private fun checkIfPreprocessorIsNeeded(line: String) {
        if (line.trimStart().startsWith('#')) globalVariables.currentFileContainsPreprocessor = true
    }
}