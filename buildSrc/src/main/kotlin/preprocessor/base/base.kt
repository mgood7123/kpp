package preprocessor.base

import java.io.File
import preprocessor.extra.globalVariables
import preprocessor.extra.process
import preprocessor.utils.abort
import java.io.IOException
import java.nio.file.Files


/**
 * self exlanatory
 *
 * this function finds and processes all source files in the directory **Dir** with the extention **ext**
 * @param Dir the directory to search in
 * @param ext the extention that each file must end
 * @sample find_source_files_sample
 */
fun find_source_files(Dir : String, ext : String) {
    var srcDir = File(Dir)
    srcDir?.listFiles().forEach {
        if (it.isDirectory) find_source_files(it.toString(), ext)
        if (it.isFile) {
            if (it.extension.equals(ext)) {
                initiate(it, ext)
            }
        }
    }
}


fun find_source_files_sample() {
    // find all source files with kotlin extention
    find_source_files(globalVariables.INITPROJECTDIR.toString(), "kt")
}

/**
 * copy one file to another, optionally overwriting it
 * @return true if the operation suceeds, otherwise false
 */
fun cp(src : String, dest : String, verbose : Boolean = false, overwrite : Boolean = false) : Boolean {
    try {
        File(src).copyTo(File(dest), overwrite)
        if (verbose) println("$src -> $dest")
        return true
    } catch (e: IOException) {
        println("failed to copy file $src")
        return false
    }
    return false
}

/**
 * initializes the file **src** to be pre-processed
 */
fun initiate(src : File, ext : String) {
    globalVariables.initKppDir()
    val source = src.toString()
    val dest = globalVariables.kppDir + src.toString().removePrefix(globalVariables.INITPROJECTDIR.toString())
    globalVariables.currentFileContainsPreprocesser = false
    globalVariables.cachedFileContainsPreprocesser = false
    globalVariables.currentFileIsCashed = false
    globalVariables.firstLine = true

    if (File(dest).exists()) {
        test_cache_File(File(dest))
        if (!globalVariables.cachedFileContainsPreprocesser) {
            // if dest no longer contains preprocessing info, copy it back to src, overwriting the current src, and use src
            if (cp(dest, source, globalVariables.globalCpVerbose, true)) { // copy dest to source
                println("${dest.substringAfterLast('/')} (in kpp/src) moved back to original source")
                File(dest).delete()
                if (File(dest + ".preprocessed.kt").exists()) File(dest + ".preprocessed.kt").delete()
            }
            else abort("failed to move ${dest} (in kpp/src) to $source")
        }
        else {
            // if dest already exist use dest
            println("using ${dest.substringAfterLast('/')} in kpp/src")
            process(dest, ext, globalVariables.kppMacroList)
            if (cp(dest + ".preprocessed.kt", source, globalVariables.globalCpVerbose, true)) {
                println("${dest.substringAfterLast('/')}.preprocessed.kt (in kpp/src) copied back to original source")
            }
            else abort("failed to copy ${dest}.preprocessed.kt (in kpp/src) to $source")
        }
    }
    else {
        test_File(src)
        if (!globalVariables.currentFileContainsPreprocesser) {
            // if src does not contain preprocessing info, use src
        } else {
            // if src contains preprocessing info, copy it to dest and use dest
            if (cp(source, dest, globalVariables.globalCpVerbose, true)) { // copy if dest does not exist
                println("original ${dest.substringAfterLast('/')} added to kpp/src")
            }
            else abort("failed to copy $source to $dest")
            println("using ${dest.substringAfterLast('/')} in kpp/src")
            process(dest, ext, globalVariables.kppMacroList)
            if (cp(dest + ".preprocessed.kt", source, globalVariables.globalCpVerbose, true)) { // copy back to source
                println("${dest.substringAfterLast('/')}.preprocessed.kt (in kpp/src) copied back to original source")
            }
            else abort("failed to copy ${dest}.preprocessed.kt (in kpp/src) to $source")
        }
    }
}

/**
 * test if file **src** contains any preprocessor directives
 */
fun test_File(src : File) {
    val lines: List<String> = Files.readAllLines(src.toPath())
    lines.forEach {
            line ->
        check_if_preprocessor_is_needed(line)
    }
}

fun check_if_preprocessor_is_needed(line: String) {
    if (line.trimStart().startsWith('#')) globalVariables.currentFileContainsPreprocesser = true
}

/**
 * test if file **src** contains any preprocessor directives
 */
fun test_cache_File(src : File) {
    val lines: List<String> = Files.readAllLines(src.toPath())
    lines.forEach {
            line ->
        check_if_cachepreprocessor_is_needed(line)
    }
}

fun check_if_cachepreprocessor_is_needed(line: String) {
    if (line.trimStart().startsWith('#')) globalVariables.cachedFileContainsPreprocesser = true
}
