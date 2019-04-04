#define a(x) x
#define z(e) e-1
#define z e
#define UIK UIK2
a(z(UIK))
/* preprocessor output
UIK2-1
*/
a(z)
/* preprocessor output
z
*/

// x(o) should not expand when expanded via global MACRO , expansion arguments are unable to cause expansion loops
#define x(h) h k x(o)
x(k)
/* preprocessor output
k k x(o)
*/

// self referencing macro
#define d l d k
d
/* preprocessor output
l d k
*/
// macro expansion loop
#define a(x) b("1" x)
#define b(x) c("2" x)
#define c(x) a("3" x)

a("x")
b("y")
c("z")
/* preprocessor out
a("3" "2" "1" "x")
b("1" "3" "2" "y")
c("2" "1" "3" "z")
*/

// another macro expansion loop
#define a(x) b() x
#define b(x) c() x
#define c(x) d a() x

a(a(A))
b(b(B))
c(c(C))
/* preprocessor output
l d k a()   l d k a()   A
l d k b()   l d k b()   B
l d k c()   l d k c()   C
*/
