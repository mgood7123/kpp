# KPP
KPP is a kotlin pre-processor developed in Kotlin DSL (kotlin build script) for android studio and the kotlin language

it is currently in beta as it is not fully functional yet

# how do i use kpp?
in project root
```
git clone https://github.com/mgood7123/kpp
```

by defailt, kpp's install directory is hardcoded to ProjectRoot/kpp, eg `/home/superlock/AndroidStudioProjects/AudioEvolved/kpp` where `AudioEvolved` is the ProjectRoot

in module you wish kpp to operate on

for example, if i want KPP to process all files in `/home/superlock/AndroidStudioProjects/AudioEvolved/app`, i would append
```
apply from: '../kpp/build.gradle.kts'

preBuild.dependsOn KOTLIN_PRE_PROCESSOR
```

to `/home/superlock/AndroidStudioProjects/AudioEvolved/app/build.gradle`

# how it works
the internals of kpp are simple

first, it operates on the following rules:

```
in order to explain we need to assume something first

src = /home/superlock/AndroidStudioProjects/AudioEvolved/app/src/main/java/start_of_email/smallville7123/at/gmail/com/end_of_email/apps/audioevolved/UI.kt

dest = /home/superlock/AndroidStudioProjects/AudioEvolved/kpp/src/main/java/start_of_email/smallville7123/at/gmail/com/end_of_email/apps/audioevolved/UI.kt

when src is about to be scanned, it first checks for the existence of dest

if dest exist, kpp will process dest

if dest does not exist, it will scan src
    if src contains any pre-processing information such as #defines, kpp will move src to dest, overwriting it if it exists
    if src does not contain pre-processing info, kpp will leave src alone

if dest no longer contains pre-processing info, kpp will copy dest back to src, overwriting src
```

after it has found files with pre-processing information, those files are kept in kpp's internal folder and any changes to the src will no longer be effective, this is explained after

when kpp processes files in its internal folder, any changes made to any file are automatically applied to the original src of that file

for example:
```
if you have a

#define a b

in src, kpp will copy src to dest, process dest to dest.preprocessed.kt, then copy dest.preprocessed.kt to src, overwriting src

if you delete the contents of src, but not src itself, the contents of src is restored when kpp processes dest

if you modify dest, src will also be modified accordingly

if dest no longer contains

#define a b

dest will be moved to src, and its files removed from the kpp folder

if you delete the contents of dest, the src will also have its contents deleted, and dest will be deleted as it has no pre-processing information such as #define due to the fact dest is now empty
```

given this, the verbose log of KPP is maintained in the Build tabs console view, and depending on your build file, is the very first task executed

ones build might look like this

```
sync failed	19 s 923 ms
Run build	19 s 279 ms
    Load build	161 ms
        Run init scripts	145 ms
        Evaluate settings	16 ms
    Configure build	19 s 49 ms
        Load projects	14 ms
        Configure project :	711 ms
            Register task :init
            Register task :wrapper	1 ms
        Configure project :app	18 s 302 ms

> Configure project :app
starting KOTLIN_PRE_PROCESSOR
using preprocessing examples.kt in kpp/src
registered macro definition for preprocessing examples.kt at index 1
processing preprocessing examples.kt -> preprocessing examples.kt.preprocessed.kt
preprocessing examples.kt: preprocessor directive: define
preprocessing examples.kt: preprocessor line: #define foob(X,                 y                 ,               Z             )              bar X y Z
text : (X,                 y                 ,               Z             )
extracting arguments for X,                 y                 ,               Z
Arguments List = [X, y, Z]
expanding line '             bar X y Z'
tokens to ignore : [ , (, ), ., ,, -, >]
tokenization : [ ,  ,  ,  ,  ,  ,  ,  ,  ,  ,  ,  ,  , bar,  , X,  , y,  , Z]
expanded string :              bar X y Z
Type       = function
Token      = foob
Arguments  = [X, y, Z]
Value      =              bar X y Z
LISTING MACROS
[0].FullMacro  = #define foob(X,                 y                 ,               Z             )              bar X y Z
[0].Type       = function
[0].Token      = foob
[0].Arguments  = [X, y, Z]
[0].Value      =              bar X y Z
LISTED MACROS
expanding line 'foob a(1,2,3) // object'
tokens to ignore : [ , (, ), ., ,, -, >]
tokenization : [foob,  , a, (, 1, ,, 2, ,, 3, ),  , //,  , object]
foob of type matches foob
token list     = foob
determining if foob is a function or an object
foob is an object
foob of type function has value              bar X y Z
foob of type function does not match a
foob of type function does not match 1
foob of type function does not match 2
foob of type function does not match 3
foob of type function does not match //
foob of type function does not match object
expanded string :              bar X y Z a(1,2,3) // object
expanding line 'foob (1,2,3) // function'
tokens to ignore : [ , (, ), ., ,, -, >]
tokenization : [foob,  , (, 1, ,, 2, ,, 3, ),  , //,  , function]
foob of type matches foob
token list     = foob
determining if foob is a function or an object
foob is a function
token list [2] = (
foob of type function has value              bar X y Z
foob of type function does not match 1
foob of type function does not match 2
foob of type function does not match 3
foob of type function does not match //
foob of type function does not match function
expanded string : foob (1,2,3) // function
expanding line 'foob(1,2,3) // function'
tokens to ignore : [ , (, ), ., ,, -, >]
tokenization : [foob, (, 1, ,, 2, ,, 3, ),  , //,  , function]
foob of type matches foob
token list     = foob
determining if foob is a function or an object
foob is a function
token list [1] = (
foob of type function has value              bar X y Z
foob of type function does not match 1
foob of type function does not match 2
foob of type function does not match 3
foob of type function does not match //
foob of type function does not match function
expanded string : foob(1,2,3) // function
expanding line ''
tokens to ignore : [ , (, ), ., ,, -, >]
tokenization : []
expanded string :
preprocessing examples.kt.preprocessed.kt (in kpp/src) copied back to original source
KOTLIN_PRE_PROCESSOR finished

FAILURE: Build failed with an exception.

* Where:
Build file '/home/superlock/AndroidStudioProjects/AudioEvolved/app/build.gradle' line: 39

* What went wrong:
A problem occurred evaluating project ':app'.
> Could not create task ':app:KOTLIN_PRE_PROCESSOR'.
   > Aborted

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

CONFIGURE FAILED in 19s
Aborted
Open File
```

this is taken at the time of this documents writing, and at the current state of KPP

the Aborted here is intentional as i am only focussing on KPP, and need not actually build the project untill i actually get kpp to be stable, in which i will start implementing kotlin specific pre-processor directives

java is also going to be supported, but for now i am focusing on kotlin

# implemented
`#define`   ( define directive fully supports object like macros, and function like macros )

# yet to be implemented
`#undef`

stringilization :  `#define tostring(x) #x`

token concation :  `#define cat(a,b) a##b`

predefined macros : `__FILE__, __LINE__, and others`

`#ifdef`

`#endif`

`#if`

`#else`

`#fi`

any other directives i may have missed

# why not just use cpp

cpp is intended for `C`, `C#`, and `C++` and as such produces unwanted text in its output, aswell as not properly supporting the kotlin language (using cpp on anything other than the C language set is considered abuse of the preprocessor, and may result in unintended side effects
