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
//#define d l d k
//d
/* preprocessor output (self reference protection on)
l d k
*/
/* preprocessor output (self reference protection off)
... expansion loop
*/
// macro expansion loop
#define a(x) b("1" x)
#define b(x) c("2" x)
#define c(x) a("3" x)

a("x")
b("y")
c("z")
/* preprocessor out (self reference protection off)
a("3" "2" "1" "x")
b("1" "3" "2" "y")
c("2" "1" "3" "z")
*/
/* preprocessor out (self reference protection on)
a("3" x)
b("1" x)
c("2" x)
*/


// another macro expansion loop
#define a(x) b() x
#define b(x) c() x
#define c(x) d a() x

a(a(A))
b(b(B))
c(c(C))
/* preprocessor out (self reference protection off, 'd' undefined)
d a()   a(A)
d b()   b(B)
d c()   c(C)
*/
/* preprocessor out (self reference protection on)
l d k a() x x a(A)
l d k b() x x b(B)
l d k c() x x c(C)
*/
