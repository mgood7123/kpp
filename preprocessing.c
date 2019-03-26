foo()baz
foo().baz
#define foo() bar
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
# define a 1
a
 # define aa 2
aa
  #  define aaa 3
aaa
                 #                        define aaaa          4
aaaa

                     #                    define                 foob(X,                 y                 ,               Z             )              bar X y Z
foob(1,2,3)
