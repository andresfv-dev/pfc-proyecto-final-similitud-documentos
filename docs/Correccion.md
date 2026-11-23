# Informe de corrección

Para cada función se escribe primero su especificación, es decir, la función
matemática $f$ que debe calcular. Después se argumenta que el programa $P_f$
devuelve $f(a)$ para toda entrada $a$, y al final se listan los casos de
prueba que lo comprueban.

Notación:

- Un vector se escribe $\langle x_0, x_1, \ldots, x_{n-1} \rangle$ y su
  longitud $|v|$.
- $\langle\rangle$ es el vector vacío, $x +: v$ agrega $x$ al frente y
  $v :+ x$ agrega $x$ al final.
- Si $ts = \langle w_0, \ldots, w_{t-1} \rangle$, el fragmento que empieza en
  la posición $i$ es $F_i = \langle w_i, \ldots, w_{i+k-1} \rangle$.

---

## 1. `normalizar`

```scala
def normalizar(texto: String): Vector[String] = {
  val minuscula  = texto.toLowerCase
  val conEspacio = minuscula.map(char => if (char.isLetterOrDigit) char else ' ')
  val divide     = conEspacio.split(' ')
  val filtra     = divide.filter(letras => letras.nonEmpty)
  filtra.toVector
}
```

### Especificación

Sea $s = \text{minúsculas}(\text{texto})$. Una **palabra** de $s$ es una
secuencia máxima de caracteres consecutivos que son letras o dígitos (no se
puede alargar ni a la izquierda ni a la derecha sin incluir un separador).
Entonces

```math
f(\text{texto}) = \langle p_1, p_2, \ldots, p_m \rangle
```

donde $p_1, \ldots, p_m$ son las palabras de $s$ en el orden en que aparecen.

### Argumento de corrección

`normalizar` no es recursiva: es una composición de cinco pasos. Se argumenta
qué cumple el resultado de cada paso.

**Paso 1.** `minuscula` $= s$, por definición de `toLowerCase`.

**Paso 2.** Sea $\varphi(c) = c$ si $c$ es letra o dígito, y $\varphi(c) = $
`' '` si no. `map` cumple, por inducción sobre la longitud de la cadena:

```math
\text{map}_\varphi(\text{""}) = \text{""} \qquad
\text{map}_\varphi(c \cdot r) = \varphi(c) \cdot \text{map}_\varphi(r)
```

Entonces `conEspacio` $= \varphi(c_0)\varphi(c_1)\cdots\varphi(c_{n-1})$: tiene
la misma longitud que $s$, deja en su lugar cada letra o dígito y cambia por
espacio cada uno de los demás caracteres. Como las letras y dígitos no se
mueven y todo lo demás quedó como espacio, las palabras de `conEspacio` son
exactamente las palabras $p_1, \ldots, p_m$ de $s$, en el mismo orden, y entre
dos palabras seguidas hay **al menos un** espacio.

**Paso 3.** `split(' ')` parte la cadena en los trozos que quedan entre
espacios consecutivos (y antes del primero y después del último), en orden, y
quita las cadenas vacías del final. Cada trozo es:

- una palabra $p_j$ completa, porque un trozo no contiene espacios y va de un
  separador al siguiente, o
- la cadena vacía `""`, cuando hay dos espacios seguidos o un espacio al
  comienzo.

Por lo tanto `divide` es la secuencia $p_1, \ldots, p_m$ con cadenas vacías
intercaladas, y en el mismo orden.

**Paso 4.** `filter` cumple, por inducción sobre la longitud del arreglo:

```math
\text{filter}_q(\langle\rangle) = \langle\rangle \qquad
\text{filter}_q(x +: v) =
\begin{cases}
x +: \text{filter}_q(v) & \text{si } q(x) \\
\text{filter}_q(v) & \text{si no}
\end{cases}
```

Con $q(x) = x.\text{nonEmpty}$, el filtro quita exactamente las cadenas vacías
y conserva el orden de las demás. Entonces
`filtra` $= \langle p_1, \ldots, p_m \rangle$.

**Paso 5.** `toVector` copia los elementos en el mismo orden.

Concluimos que

```math
\forall\, \text{texto} : \text{normalizar}(\text{texto}) == f(\text{texto})
```

Un caso particular: si el texto no tiene ninguna letra ni dígito,
`conEspacio` es solo espacios, `divide` solo tiene cadenas vacías y el
resultado es $\langle\rangle$.

### Casos de prueba

| Prueba | Entrada | Resultado esperado | Qué comprueba |
|---|---|---|---|
| contrato | `"El Gato Duerme"` | `Vector("el", "gato", "duerme")` | minúsculas y separación por espacios |

---

## 2. `huella`

```scala
def huella(d: Documento, k: Int): Huella = {
  val palabras = palabrasDoc(d)
  val frags    = fragmentos(palabras, k)
  frags.toSet
}
```

### Especificación

Sea $d = (\text{id}, ts)$ con $ts = \langle w_0, \ldots, w_{t-1} \rangle$.

```math
f(d, k) = \{\, F_i \mid 0 \le i \le t - k \,\}
```

Es decir, el **conjunto** de los fragmentos de $k$ palabras consecutivas; si
$t < k$, el conjunto vacío.

### Argumento de corrección

`huella` no es recursiva: llama a `palabrasDoc`, a `fragmentos` y a `toSet`.
Se usa como hipótesis la especificación de `fragmentos`, cuya corrección se
argumenta por aparte:

```math
\text{fragmentos}(ts, k) =
\begin{cases}
\langle F_0, F_1, \ldots, F_{t-k} \rangle & \text{si } t \ge k \\
\langle\rangle & \text{si } t < k
\end{cases}
```

**Lema (`toSet`).** Para todo vector $v$, $\text{toSet}(v)$ es el conjunto de
los elementos de $v$, y $|\text{toSet}(v)| \le |v|$.

Por inducción sobre $|v|$, usando que $\text{toSet}(v :+ x) = \text{toSet}(v) \cup \{x\}$:

- **Caso base:** $v = \langle\rangle$. $\text{toSet}(\langle\rangle) = \emptyset$,
  que es el conjunto de elementos de $v$, y $|\emptyset| = 0 \le 0$.
- **Caso de inducción:** supongamos que se cumple para $v$ (HI) y veamos
  $v :+ x$.

```math
\text{toSet}(v :+ x) = \text{toSet}(v) \cup \{x\}
\overset{HI}{=} \{\text{elementos de } v\} \cup \{x\}
= \{\text{elementos de } v :+ x\}
```

```math
|\text{toSet}(v) \cup \{x\}| \le |\text{toSet}(v)| + 1 \overset{HI}{\le} |v| + 1 = |v :+ x|
```

La desigualdad es estricta justo cuando $x$ ya estaba en $v$: un fragmento
repetido no aumenta el tamaño del conjunto.

**Corrección.** Por el modelo de sustitución:

```math
\text{huella}(d, k) \rightarrow \text{fragmentos}(ts, k).\text{toSet}
```

- Si $t \ge k$: $\rightarrow \text{toSet}(\langle F_0, \ldots, F_{t-k} \rangle) = \{F_0, \ldots, F_{t-k}\} = f(d, k)$.
- Si $t < k$: $\rightarrow \text{toSet}(\langle\rangle) = \emptyset = f(d, k)$.

Concluimos que $\forall d, k : \text{huella}(d, k) == f(d, k)$.

### Propiedad: la huella no supera los $t - k + 1$ fragmentos

```math
|\text{huella}(d, k)| \le \max(0,\ t - k + 1)
```

Si $t \ge k$, por el lema,
$|\text{huella}(d, k)| = |\text{toSet}(\text{fragmentos}(ts, k))| \le |\text{fragmentos}(ts, k)| = t - k + 1$.
Si $t < k$, la huella es vacía y tiene tamaño $0$.

La igualdad se da cuando no hay fragmentos repetidos. En el ejemplo 1 del
enunciado, $d_0$ tiene $t = 6$ palabras y $k = 3$, así que tiene
$6 - 3 + 1 = 4$ fragmentos, todos distintos, y la huella tiene tamaño 4.

### Casos de prueba

| Prueba | Entrada | Resultado esperado | Qué comprueba |
|---|---|---|---|
| contrato | ejemplo 1, $k = 3$ | tamaños 4, 4 y 3 | la cota $t - k + 1$ con igualdad |
| propia | `("r", Vector(a, b, a, b))`, $k = 2$ | `Set([a,b], [b,a])` | un fragmento repetido cuenta una vez (3 fragmentos, huella de 2) |
| propia | `("corto", Vector(hola))`, $k = 3$ | `Set()` | caso $t < k$ |
| propia | `("vacio", Vector())`, $k = 2$ | `Set()` | documento vacío |
| propia | `("igual", Vector(x, x, x, x))`, $k = 2$ | `Set([x,x])` | todos los fragmentos iguales: 3 fragmentos, huella de 1 |
| propia | `normalizar("Hola, MUNDO; hola!")`, $k = 2$ | `Set([hola,mundo], [mundo,hola])` | funciona sobre un texto normalizado |

---

## 3. `huellas`

```scala
def huellas(c: Corpus, k: Int): Vector[Huella] =
  c.map(documento => huella(documento, k))
```

### Especificación

Para $c = \langle d_0, d_1, \ldots, d_{n-1} \rangle$:

```math
f(c, k) = \langle \text{huella}(d_0, k), \text{huella}(d_1, k), \ldots, \text{huella}(d_{n-1}, k) \rangle
```

Una huella por documento, en el mismo orden del corpus.

### Argumento de corrección

Sea $g(d) = \text{huella}(d, k)$. `map` cumple:

```math
\text{map}_g(\langle\rangle) = \langle\rangle \qquad
\text{map}_g(c :+ d) = \text{map}_g(c) :+ g(d)
```

Demostramos por inducción sobre $n = |c|$ que
$\text{huellas}(c, k) == f(c, k)$.

**Caso base:** $n = 0$.

```math
\text{huellas}(\langle\rangle, k) \rightarrow \text{map}_g(\langle\rangle) \rightarrow \langle\rangle = f(\langle\rangle, k)
```

**Caso de inducción:** $n = m + 1$, $m \ge 0$. Sea
$c = \langle d_0, \ldots, d_{m-1} \rangle$ y supongamos
$\text{huellas}(c, k) == f(c, k)$ (HI).

```math
\text{huellas}(c :+ d_m, k) \rightarrow \text{map}_g(c :+ d_m) \rightarrow \text{map}_g(c) :+ g(d_m)
```

```math
\overset{HI}{=} \langle \text{huella}(d_0, k), \ldots, \text{huella}(d_{m-1}, k) \rangle :+ \text{huella}(d_m, k) = f(c :+ d_m, k)
```

Concluimos por inducción que $\forall c, k : \text{huellas}(c, k) == f(c, k)$.

De aquí salen las dos propiedades que usan las demás funciones:

- $|\text{huellas}(c, k)| = |c|$.
- $\text{huellas}(c, k)(i) = \text{huella}(c(i), k)$ para todo $0 \le i < |c|$.
  La posición $i$ del vector de huellas identifica al documento $i$, que es lo
  que usan `indiceInvertido` y `similitudes`.

### Casos de prueba

| Prueba | Entrada | Resultado esperado | Qué comprueba |
|---|---|---|---|
| contrato | corpus del ejemplo 1, $k = 3$ | longitud 3 | una huella por documento |
| propia | `Vector()`, $k = 2$ | `Vector()` | caso base: corpus vacío |
| propia | `(a, [x,y])`, `(b, [y,z])`, $k = 2$ | `Vector({[x,y]}, {[y,z]})` | conserva el orden del corpus |
| propia | `(a, [uno,dos,tres])`, `(b, [cuatro])`, $k = 2$ | `hs(i) == huella(c(i), 2)` | la posición $i$ es la huella del documento $i$ |
| propia | `(a, [p,q,r])`, `(b, [p])`, $k = 2$ | `hs(1)` vacía | un documento corto deja su huella vacía sin afectar a los demás |
| propia | `normalizar("El sol sale")`, `normalizar("el SOL sale")`, $k = 2$ | `hs(0) == hs(1)` | documentos iguales después de normalizar tienen la misma huella |
