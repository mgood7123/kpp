//CPP
//# 1 "cpp test comparisions against kpp.c"
//# 1 "<built-in>"
//# 1 "<command-line>"
//# 31 "<command-line>"
//# 1 "/usr/include/stdc-predef.h" 1 3 4
//# 32 "<command-line>" 2
//# 1 "cpp test comparisions against kpp.c"
//# 24 "cpp test comparisions against kpp.c"
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
//cpp test comparisions against kpp.c:68: warning: "foob" redefined
//#define foob(X,                 y                 ,               Z             )              bar X y Z // FUNCTION
//
//cpp test comparisions against kpp.c:67: note: this is the location of the previous definition
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
#define foo() bar
foo()baz
foo().baz
#define x y
#define y hai xx foo bar j(k).String()
#define Z z
#define z x
foo()baz
/*
    → bar baz
not
    → barbaz
*/

foo().baz
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
# define a 1
a
//# define aa 2
//aa
# define aa 2
aa
//#  define aaa 3
//aaa
#  define aaa 3
aaa
//#                        define aaaa          4
//aaaa
#                        define aaaa          4
aaaa



#define fooa(X,                 y                 ,               Z             )              bar X y Z // FUNCTION
#define foob (X,                 y                 ,               Z             )              bar X y Z // OBJECT
#define foob(X,                 y                 ,               Z             )              bar X y Z // FUNCTION
fooa(1,2,3) // function
foob(1,2,3) // function
foob (1,2,3) // function
foob // object
fooa(1,2,3) // function
foob (1,2,3) // function
foob a(1,2,3) // object
foob (1,2,3) // function
foob(1, foob(a,b,c), 3) // function

// macro expansion loop
#define a(x) b() x
#define b(x) c() x
#define c(x) d a() x

//a(a())
//b(b())
//c(c())


#define a(x) x
#define z(e) e-1
#define z e
#define UIK UIK2
a(z(UIK))
a(z)

