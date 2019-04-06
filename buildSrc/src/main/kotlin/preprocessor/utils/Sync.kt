package preprocessor.utils

import preprocessor.base.globalVariables
import preprocessor.core.process
import java.io.File
import preprocessor.utils.core.*
import java.nio.file.Files

/*
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

    val ignore = arrayListOf<String>(
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
            val A = it
            val B = File(
                globalVariables.rootDirectory.toString() + '/' + basename(globalVariables.kppDir) + A.toString().removePrefix(
                    globalVariables.rootDirectory!!.path
                )
            )
            println("A :     " + A)
            println("B : " + B)
            if (A.exists()) {
                if (A.isDirectory) {
                    // ignore build dir in A
                    // if build dir exists in B, delete it
                    val blocked = A.toString().equals(globalVariables.projectDirectory?.path + "/build")
                    if (!blocked) {
                        println("entering $A")
                        syncB(A, src, extension)
                        println("leaving $A")
                    } else println("A is blocked")
                    if (B.exists()) {
                        if (blocked) {
                            println("deleting B")
                            deleteRecursive(B)
                        } else if (empty(B)) {
                            println("deleting B")
                            delete(B)
                        }
                    }
                    val AP = File(A.path + globalVariables.preprocessedExtension + "." + A.extension)
                    val BP = File(B.path + globalVariables.preprocessedExtension + "." + B.extension)
                    if (AP.exists()) {
                        println("deleting preprocessing file associated with A")
                        delete(AP)
                    }
                    if (BP.exists()) {
                        println("deleting preprocessing file associated with B")
                        delete(BP)
                    }
                } else if (A.isFile) {
                    if (A.path.endsWith(globalVariables.preprocessedExtension + "." + A.extension)) {
                        println("A is preprocessor file")
                        println("deleting A")
                        delete(A)
                    } else if (!ignore.contains(A.extension)) {
                        // if extension is null, test every file
                        // ignore these extensions
                        if (it.extension.equals(extension) || extension == null) {
                            if (!B.exists()) {
                                if (test_File(A!!)) {
                                    println("copying A to B")
                                    if (!cp(A.path, B.path, true))
                                        abort("failed to copy $A to $B")
                                } else {
                                    println("B does not exist however A does not contain DATA")
                                    println("B cannot be deleted as it does not exist")
                                    println("A will not be copied to B")
                                }
                            } else {
                                if (!test_File(B!!)) {
                                    println("B exists however does not contains DATA")
                                    println("deleting B")
                                    delete(B)
                                } else println("B contains DATA")
                            }
                        }
                    } else {
                        println("ignoring extension: ${A.extension}")
                        if (B.exists()) {
                            println("A is ignoring but B exists, deleting B")
                            delete(B)
                        }
                        val AP = File(A.path + globalVariables.preprocessedExtension + "." + A.extension)
                        val BP = File(B.path + globalVariables.preprocessedExtension + "." + B.extension)
                        if (AP.exists()) {
                            println("deleting preprocessing file associated with A")
                            delete(AP)
                        }
                        if (BP.exists()) {
                            println("deleting preprocessing file associated with B")
                            delete(BP)
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
            val A = it
            val B = File(
                globalVariables.rootDirectory.toString() + '/' + A.toString().removePrefix(globalVariables.kppDir!!)
            )
            println("A : " + A)
            println("B :     " + B)
            run returnpoint@{
                if (A.exists()) {
                    if (A.isDirectory) {
                        // ignore build dir in A
                        // if build dir exists in B, delete it
                        val blocked = A.toString().equals(globalVariables.projectDirectory?.path + "/build")
                        if (!blocked) {
                            if (B.path.equals(globalVariables.kppDir)) {
                                println("error: B is kpp dir")
                                println("kpp should not contain its own directory")
                                println("deleting A")
                                deleteRecursive(A)
                                println("returning to @returnpoint")
                                return@returnpoint
                            } else {
                                if (B.exists()) {
                                    println("entering $A")
                                    syncA(A, src, extension)
                                    println("leaving $A")
                                    if (empty(A)) {
                                        println("deleting A")
                                        delete(A)
                                        println("returning to @returnpoint")
                                        return@returnpoint
                                    }
                                } else {
                                    println("B does not exist")
                                    println("deleting A")
                                    deleteRecursive(A)
                                    println("returning to @returnpoint")
                                    return@returnpoint
                                }
                            }
                        } else println("A is blocked")
                        if (B.exists()) {
                            if (blocked) {
                                println("deleting A")
                                deleteRecursive(A)
                                println("returning to @returnpoint")
                                return@returnpoint
                            } else if (empty(A)) {
                                println("deleting A")
                                delete(A)
                                println("returning to @returnpoint")
                                return@returnpoint
                            }
                        }
                    } else if (A.isFile) {
                        // if extension is null, test every file
                        // ignore these extensions
                        if (!ignore.contains(A.extension)) {
                            if (it.extension.equals(extension) || extension == null) {
                                if (!test_File(A!!)) {
                                    println("A exists however does not contains DATA")
                                    if (!A.path.endsWith(globalVariables.preprocessedExtension + "." + extension)) {
                                        println("moving A to B")
                                        if (!mv(A.path, B.path, true, true))
                                            abort()
                                    } else {
                                        println("A is preprocessor file")
                                        println("moving A to B (renamed)")
                                        if (!mv(
                                                A.path,
                                                B.path.removeSuffix("." + globalVariables.preprocessedExtension + "." + extension),
                                                true,
                                                true
                                            )
                                        )
                                            abort()
                                    }
                                    println("returning to @returnpoint")
                                    return@returnpoint
                                } else {
                                    println("A contains DATA")
                                    println("processing A")
                                    if (A.extension.equals("")) {
                                        println("error: cannot process a file with no extension")
                                        return@returnpoint
                                    }
                                    process(A.path, A.extension, globalVariables.kppMacroList)
                                    println("moving resulting preprocessing file A to B (renamed)")
                                    if (!mv(
                                            A.path + globalVariables.preprocessedExtension + "." + A.extension,
                                            B.path,
                                            true,
                                            true
                                        )
                                    )
                                        println("returning to @returnpoint")
                                    return@returnpoint
                                }
                            }
                        } else {
                            println("ignoring extension: ${A.extension}")
                            println("returning to @returnpoint")
                            return@returnpoint
                        }
                    }
                } else {
                    println("A does not exist")
                }
            }
        }
    }

    /**
     * self exlanatory
     *
     * this function finds and processes all source files in the directory **dir** with the extension **extension**
     * @param dir the directory to search in
     * @param extension the extension that each file must end
     * @param extension if **null** then any file is accepted
     * @sample find_source_files_sample
     */
    fun findSourceFiles(dir: File, extension: String? = null) {
        // sync dir with kppDir
        syncB(dir, globalVariables.kppDirAsFile as File, extension)
        // sync kppDir with dir, calling process on a valid processing file
        syncA(globalVariables.kppDirAsFile as File, dir, extension)
    }


    /**
     * self exlanatory
     *
     * this function finds and processes all source files in the directory **dir** with the extension **extension**
     * @param dir the directory to search in
     * @param extension the extension that each file must end
     * @param extension if **null** then any file is accepted
     * @sample find_source_files_sample
     */

    fun findSourceFilesOrNull(dir: File?, extension: String? = null) {
        if (dir == null) abort("dir cannot be null")
        findSourceFiles(dir as File, extension)
    }

    /**
     * self exlanatory
     *
     * this function finds and processes all source files in the directory **dir** with the extension **extension**
     * @param dir the directory to search in
     * @param extension the extension that each file must end
     * @param extension if **null** then any file is accepted
     * @sample find_source_files_sample
     */
    fun findSourceFiles(dir: String, extension: String? = null) {
        findSourceFiles(File(dir), extension)
    }

    private fun find_source_files_sample() {
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
    fun test_File(src: File): Boolean {
        globalVariables.currentFileContainsPreprocesser = false
        println("testing file: ${src.path}")
        val lines: List<String> = Files.readAllLines(src.toPath())
        lines.forEach { line ->
            check_if_preprocessor_is_needed(line)
        }
        return globalVariables.currentFileContainsPreprocesser
    }

    private fun check_if_preprocessor_is_needed(line: String) {
        if (line.trimStart().startsWith('#')) globalVariables.currentFileContainsPreprocesser = true
    }
}