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
#define y hai x foo bar j(k).String()
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
