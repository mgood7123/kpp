//CPP
//# 1 "preprocessing.c"
//# 1 "<built-in>"
//# 1 "<command-line>"
//# 31 "<command-line>"
//# 1 "/usr/include/stdc-predef.h" 1 3 4
//# 32 "<command-line>" 2
//# 1 "preprocessing.c"
//# 24 "preprocessing.c"
//bar baz
//bar.baz
//
//
//
//
//bar baz
//
//
//
//
//
//
//bar.baz
//
//
//
//
//
//a
//aa
//aaa
//aaaa
//
//
//
//1
//
//
//
//2
//
//
//
//3
//
//
//
//preprocessing.c:68: warning: "foob" redefined
//#define foob(X,                 y                 ,               Z             )              bar X y Z // FUNCTION
//
//preprocessing.c:67: note: this is the location of the previous definition
//#define foob (X,                 y                 ,               Z             )              bar X y Z // OBJECT
//
//4
//
//
//
//
//
//
//bar 1 2 3
//bar 1 2 3
//bar 1 2 3
//foob
//bar 1 2 3
//bar 1 2 3
//foob 1(1,2,3)
//bar 1 2 3
//bar 1 bar 1 b c 3

//#define foo() bar
//foo()baz
//foo().baz
//#define x y
//#define y hai x foo bar j(k).String()
//#define Z z
//#define z x
//foo()baz
//foo().baz
//#define fooa(X,                 y                 ,               Z             )              bar X y Z // FUNCTION
//#define foob (X,                 y                 ,               Z             )              bar X y Z // OBJECT
//#define foob(X,                 y                 ,               Z             )              bar X y Z // FUNCTION
//fooa(1,2,3) // function
//foob(1,2,3) // function
//foob (1,2,3) // function
//foob // object
//fooa(1,2,3) // function
//foob (1,2,3) // function
//foob a(1,2,3) // object
//foob (1,2,3) // function
//foob(1, foob(a,b,c), 3) // function
// #define fooa(X,                 y                 ,               Z             )              bar X y Z // FUNCTION
barbaz
bar.baz
barbaz
/*
    → bar baz
not
    → barbaz
*/

bar.baz
/*
    → bar.baz
not
    → bar .baz
*/
a
aa
aaa
aaaa
//# define a 1
//a
1
//# define aa 2
//aa
2
//#  define aaa 3
//aaa
3
//#                        define aaaa          4
//aaaa
4



bar 1 2 3 // FUNCTION // function
bar 1 2 3 // FUNCTION // function
bar 1 2 3 // FUNCTION // function
(X,                 hai xx foo bar j(k).String()                 ,               hai xx foo bar j(k).String()             )              bar X hai xx foo bar j(k).String() hai xx foo bar j(k).String() // OBJECT // object
bar 1 2 3 // FUNCTION // function
bar 1 2 3 // FUNCTION // function
(X,                 hai xx foo bar j(k).String()                 ,               hai xx foo bar j(k).String()             )              bar X hai xx foo bar j(k).String() hai xx foo bar j(k).String() // OBJECT a // object
bar 1 2 3 // FUNCTION // function
bar 1 bar 1 b c // FUNCTION 3 // FUNCTION // function

// macro expansion loop

//a(a())
//b(b())
//c(c())


UIK2-1
e

