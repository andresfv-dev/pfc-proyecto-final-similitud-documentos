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
| propia | `""` | `Vector()` | texto vacío |
| propia | `"¡¿... , ; !?"` | `Vector()` | texto sin letras ni dígitos: `divide` solo tiene cadenas vacías y el filtro las quita todas |
| propia | `"¡Expecto Patronum!"` | `Vector("expecto", "patronum")` | el separador del comienzo deja una cadena vacía que el filtro quita |
| propia | `"uno,,dos--tres"` | `Vector("uno", "dos", "tres")` | varios separadores seguidos no dejan palabras vacías |
| propia | `"Harry tiene 11 AÑOS"` | `Vector("harry", "tiene", "11", "años")` | los dígitos y las letras con tilde se conservan |

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

---

## 6. `jaccard`

```scala
def jaccard(h1: Huella, h2: Huella): Double = {
  val comunes = h1.intersect(h2)
  val unidos = h1.union(h2)
  if (unidos.isEmpty) 0.0
  else comunes.size.toDouble / unidos.size
}
```

### Especificación

```math
J(h_1,h_2) = \begin{cases}
0 & \text{si } h_1 \cup h_2 = \emptyset \\
\dfrac{|h_1 \cap h_2|}{|h_1 \cup h_2|} & \text{si no}
\end{cases}
```

### Argumento de corrección

`intersect` deja los fragmentos que están en las dos huellas y `union`
reúne los que aparecen en al menos una, sin repetirlos. Si la unión queda
vacía, las dos huellas eran vacías y se devuelve `0.0` sin dividir.
En el otro caso se dividen los tamaños. El `toDouble` evita la división
entera: por ejemplo, un fragmento común de cuatro da `0.25`, no `0`.

### Casos de prueba

| Prueba | Entrada | Resultado esperado | Qué comprueba |
|---|---|---|---|
| contrato | ejemplo 2, con $k = 3$ | `0.75` y `0.5` | las dos comparaciones del ejemplo |
| propia | dos huellas vacías | `0.0` | no divide por cero |
| propia | huella vacía y huella con `[sol]` | `0.0` | no hay fragmentos en común |
| propia | huella con `[sol]` y huella con `[luna]` | `0.0` | dos huellas no vacías pueden no coincidir |
| propia | dos huellas iguales y no vacías | `1.0` | todos los fragmentos coinciden |
| propia | `{[informe, final], [costos, estimados]}` y `{[informe, final], [ventas, mensuales], [saldo, pendiente]}` | `0.25` | hay un fragmento común y cuatro distintos en la unión |

---

## 7. `parejas`

```scala
def parejas(n: Int): Vector[Pareja] = {
  for {
    primera <- (0 until n).toVector
    segunda <- (primera + 1) until n
  } yield (primera, segunda)
}
```

### Especificación

Para $n$ documentos se devuelven las parejas $(i,j)$ que cumplen
$0 \le i < j < n$, ordenadas primero por $i$ y después por $j$.

### Argumento de corrección

El primer rango pasa por cada posición $i$ desde $0$ hasta $n-1$. Para
cada una, el segundo rango empieza en $i+1$ y termina antes de $n$. Así
no salen parejas de un documento consigo mismo ni parejas al revés.
Cada pareja válida aparece una vez porque sus dos posiciones se recorren
una sola vez en esos rangos. Como ambos rangos avanzan de menor a mayor,
las parejas salen en el orden pedido.

Si $n$ es $0$ o $1$ no se produce ninguna pareja. Para $n \ge 2$ se
producen $n-1$ con la primera posición $0$, luego $n-2$ con la primera
posición $1$, y así hasta tener $n(n-1)/2$ en total.

### Casos de prueba

| Prueba | Entrada | Resultado esperado | Qué comprueba |
|---|---|---|---|
| contrato | `n = 5` | 10 parejas | el total es $n(n-1)/2$ |
| propia | `n = 0` | `Vector()` | no hay documentos |
| propia | `n = 1` | `Vector()` | un documento no se compara consigo mismo |
| propia | `n = 2` | `Vector((0,1))` | sale una sola pareja |
| propia | `n = 4` | `Vector((0,1), (0,2), (0,3), (1,2), (1,3), (2,3))` | orden de las parejas |
| propia | `n = 6`, tomando las parejas con segunda posición `5` | `Vector((0,5), (1,5), (2,5), (3,5), (4,5))` | el último documento aparece con todos los anteriores |

---

## 8. `similitudes`

```scala
def similitudes(hs: Vector[Huella], ps: Vector[Pareja]): Vector[Medicion] = {
  ps.map { case (primera, segunda) =>
    val similitud = jaccard(hs(primera), hs(segunda))
    (primera, segunda, similitud)
  }
}
```

### Especificación

Por cada pareja $(i,j)$ de `ps`, con índices válidos en `hs`, se devuelve
$(i,j,J(hs_i,hs_j))$. Las mediciones quedan en el mismo orden de `ps`.

### Argumento de corrección

El `map` recorre `ps` sin cambiar su orden. Con cada pareja, `hs(primera)`
y `hs(segunda)` buscan las dos huellas y `jaccard` calcula el valor que
va en la tercera posición. Como el `map` produce un resultado por cada
entrada, también quedan las mediciones que dan `0.0`. Si `ps` está vacío,
no se llama a `jaccard` y se devuelve un vector vacío.

### Casos de prueba

| Prueba | Entrada | Resultado esperado | Qué comprueba |
|---|---|---|---|
| contrato | huellas y parejas del ejemplo 1, con $k = 3$ | valores `1/3`, `0.0` y `0.0` | las tres mediciones del ejemplo |
| propia | huellas del informe, sin parejas pedidas | `Vector()` | no compara si no se pide ninguna pareja |
| propia | huellas de las recetas, pareja `(0,2)` | `Vector((0,2,0.5))` | solo compara los documentos indicados |
| propia | huellas de los avisos, parejas `(1,2)` y `(0,1)` | `Vector((1,2,1.0), (0,1,0.5))` | respeta el orden de entrada |
| propia | huellas de los menús, parejas `(0,1)`, `(0,2)` y `(2,3)` | `Vector((0,1,1.0), (0,2,0.0), (2,3,0.5))` | incluye la medición de cero |
| propia | dos títulos que dejan huellas vacías, pareja `(0,1)` | `Vector((0,1,0.0))` | medir huellas vacías da cero, no un vector vacío |

---

## 9. `indiceInvertido`

```scala
def indiceInvertido(hs: Vector[Huella]): Indice = {
  val conPosicion = hs.zipWithIndex
  val entradas    = conPosicion.flatMap(par => par._1.toVector.map(fragmento => (fragmento, par._2)))
  val agrupado    = entradas.groupBy(entrada => entrada._1)
  agrupado.map(grupo => (grupo._1, grupo._2.map(entrada => entrada._2).toSet))
}
```

### Especificación

Sea $hs = \langle H_0, \ldots, H_{n-1} \rangle$ el vector de huellas. El índice
es el mapa que a cada fragmento $F$ que aparece en alguna huella le asocia el
conjunto de las posiciones donde aparece:

```math
f(hs)(F) = \{\, i \mid 0 \le i < n,\ F \in H_i \,\} \qquad
\text{para todo } F \in H_0 \cup \cdots \cup H_{n-1}
```

Un fragmento que no está en ninguna huella no es clave del mapa.

### Argumento de corrección

`indiceInvertido` no es recursiva: es una composición de cuatro pasos.

**Paso 1.** `conPosicion` $= \langle (H_0, 0), \ldots, (H_{n-1}, n-1) \rangle$, por
definición de `zipWithIndex`.

**Paso 2.** Para cada $(H_i, i)$, `H_i.toVector.map(F => (F, i))` produce la
pareja $(F, i)$ por cada $F \in H_i$, una sola vez, porque $H_i$ es un conjunto.
`flatMap` concatena esos vectores, así que

```math
\text{entradas} = \{\, (F, i) \mid F \in H_i \,\}
```

como colección, sin repetidos. Cada entrada depende solo de $F$ y de $i$, no del
orden en que se recorra.

**Paso 3.** `groupBy(_._1)` deja una clave por cada $F$ que aparece como primera
componente de alguna entrada, es decir, por cada $F \in \bigcup H_i$, y asocia a
$F$ todas las entradas $(F, i)$. No crea claves para fragmentos que no están.

**Paso 4.** Para cada grupo, `map(_._2)` deja las posiciones de sus entradas y
`toSet` las vuelve conjunto. Para la clave $F$ queda
$\{\, i \mid (F, i) \in \text{entradas} \,\} = \{\, i \mid F \in H_i \,\} = f(hs)(F)$.

Concluimos que $\forall hs : \text{indiceInvertido}(hs) == f(hs)$.

Como el resultado es una unión de entradas que cada una depende solo de
$(F, i)$, y la unión de conjuntos es asociativa y conmutativa, **no depende del
orden** en que se recorran las huellas. Es el argumento que después justifica
que una versión paralela dé lo mismo, siempre que conserve las posiciones.

### Casos de prueba

| Prueba | Entrada | Resultado esperado | Qué comprueba |
|---|---|---|---|
| contrato | huellas del ejemplo 1, $k = 3$ | `idx([el,gato,duerme]) == {0, 1}` | un fragmento compartido por dos documentos |
| propia | `Vector()` | `Map()` | caso base: sin huellas |
| propia | una huella `{[a,b], [b,c]}` | `[a,b] → {0}`, `[b,c] → {0}` | un solo documento |
| propia | tres huellas `{[x]}` | `[x] → {0,1,2}` | un fragmento en todos los documentos |
| propia | `Vector(Set(), Set([a]))` | `Map([a] → {1})` | una huella vacía no aporta entradas y las posiciones no se corren |
| propia | `{[a]}`, `{[b]}`, `{[a],[b]}` | `[a] → {0,2}`, `[b] → {1,2}`, 2 claves | cada fragmento queda con los documentos donde aparece |

---

## 10. `parejasCandidatas`

```scala
def parejasCandidatas(idx: Indice): Vector[Pareja] = {
  val conjuntos = idx.values.toVector.map(docs => docs.toVector.sorted)
  val todas     = conjuntos.flatMap(docs => docs.combinations(2).map(par => (par(0), par(1))).toVector)
  todas.distinct.sorted
}
```

### Especificación

```math
f(idx) = \{\, (i, j) \mid i < j,\ \exists F : i \in idx(F) \wedge j \in idx(F) \,\}
```

como vector sin repetidos y en orden ascendente (primero por $i$, luego por $j$).

### Argumento de corrección

**Paso 1.** Para cada $D = idx(F)$, `docs.toVector.sorted` es el vector de sus
elementos en orden ascendente, sin repetidos porque $D$ es un conjunto.

**Paso 2.** `combinations(2)` entrega cada subconjunto de dos elementos del
vector una sola vez y conserva el orden relativo, así que en cada
$\text{par} = \langle a, b \rangle$ se cumple $a < b$, y
`(par(0), par(1))` $= (a, b)$ con $a < b$. Por lo tanto `todas` contiene
exactamente las parejas $(i, j)$ con $i < j$ y $i, j \in idx(F)$, para algún $F$.
Si $|D| < 2$ no hay combinaciones y ese fragmento no aporta parejas.

**Paso 3.** Una pareja puede salir de varios fragmentos y aparece una vez por
cada uno. `distinct` deja una sola, sin cambiar el conjunto de parejas.

**Paso 4.** `sorted` ordena las parejas con el orden lexicográfico de
`(Int, Int)`.

Concluimos que $\forall idx : \text{parejasCandidatas}(idx) == f(idx)$. Que el
resultado no dependa del orden de las entradas del `Map` (que no está definido)
lo garantiza el `sorted` final.

### Casos de prueba

| Prueba | Entrada | Resultado esperado | Qué comprueba |
|---|---|---|---|
| contrato | índice del ejemplo 2, $k = 3$ | `Vector((0,1), (2,3))` | dos de las seis parejas son candidatas |
| propia | `Map()` | `Vector()` | índice vacío |
| propia | `Map([a] → {0})` | `Vector()` | un fragmento en un solo documento no forma pareja |
| propia | `Map([x] → {2,0,1})` | `Vector((0,1), (0,2), (1,2))` | tres documentos dan tres parejas con $i < j$ y en orden |
| propia | `[a] → {0,1}`, `[b] → {0,1}` | `Vector((0,1))` | la pareja que sale de dos fragmentos cuenta una vez |
| propia | `[a] → {2,3}`, `[b] → {0,1}` | `Vector((0,1), (2,3))` | el resultado sale ordenado |

---

## 11. `ranking`

```scala
def ranking(ss: Vector[Medicion], umbral: Double, p: Int): Vector[Medicion] = {
  val sobreUmbral = ss.filter(medicion => medicion._3 >= umbral)
  val ordenadas   = sobreUmbral.sortBy(medicion => (-medicion._3, medicion._1, medicion._2))
  ordenadas.take(p)
}
```

### Especificación

Sea $\tau$ el umbral. Se toman las mediciones $(i, j, s)$ con $s \ge \tau$, se
ordenan por la clave $(-s, i, j)$ ascendente (mayor similitud primero y, a
igual similitud, por posición) y se devuelven las primeras $p$:

```math
f(ss, \tau, p) = \text{take}_p\bigl(\text{ord}\{\, (i, j, s) \in ss \mid s \ge \tau \,\}\bigr)
```

### Argumento de corrección

**Paso 1.** `filter` conserva exactamente las mediciones con $s \ge \tau$ y su
orden relativo (por inducción sobre $|ss|$, igual que el filtro de `normalizar`).

**Paso 2.** `sortBy` ordena por la clave $(-s, i, j)$. Como cada pareja se mide
una sola vez, dos mediciones distintas tienen distinta pareja $(i, j)$ y sus
claves son distintas y el orden es **total**: el resultado es único y no depende del orden
en que llegaron. Si $s_1 > s_2$ entonces $-s_1 < -s_2$, de modo que la mayor
similitud queda primero; si $s_1 = s_2$ decide $(i, j)$.

**Paso 3.** `take(p)` devuelve las primeras $\min(p, |\text{ordenadas}|)$
mediciones: si hay menos de $p$ devuelve todas, y con $p = 0$ el vector vacío.

Concluimos que $\forall ss, \tau, p : \text{ranking}(ss, \tau, p) == f(ss, \tau, p)$.
El umbral es inclusivo ($s \ge \tau$), como en el enunciado.

### Casos de prueba

| Prueba | Entrada | Resultado esperado | Qué comprueba |
|---|---|---|---|
| contrato | `(0,1,0.4)`, `(0,2,0.1)`, `(1,2,0.9)`, $\tau = 0$ | similitudes `0.9, 0.4, 0.1`; con $p = 2$, longitud 2 | orden descendente y truncado |
| propia | `Vector()` | `Vector()` | sin mediciones |
| propia | `(0,1,0.4)`, $\tau = 0.5$ | `Vector()` | un umbral mayor que todas las similitudes |
| propia | `(0,1,0.5)`, $\tau = 0.5$ | `Vector((0,1,0.5))` | el umbral es inclusivo |
| propia | `(0,1,0.9)`, $p = 0$ | `Vector()` | $p = 0$ |
| propia | `(1,2,0.5)`, `(0,2,0.5)`, `(0,1,0.5)` | `(0,1,0.5)`, `(0,2,0.5)`, `(1,2,0.5)` | los empates se rompen por posición, sin importar el orden de llegada |

---

## 12. `detectarDirecto` y `detectarConIndice`

```scala
def detectarDirecto(c: Corpus, k: Int, umbral: Double, p: Int): Vector[Medicion] = {
  val hs         = huellas(c, k)
  val todas      = parejas(c.length)
  val mediciones = similitudes(hs, todas)
  ranking(mediciones, umbral, p)
}

def detectarConIndice(c: Corpus, k: Int, umbral: Double, p: Int): Vector[Medicion] = {
  val hs         = huellas(c, k)
  val idx        = indiceInvertido(hs)
  val candidatas = parejasCandidatas(idx)
  val mediciones = similitudes(hs, candidatas)
  ranking(mediciones, umbral, p)
}
```

### Especificación

Sea $J(i, j) = \text{jaccard}(H_i, H_j)$ con $H = \text{huellas}(c, k)$ y sea
$P$ un conjunto de parejas. Ambas funciones devuelven

```math
\text{ranking}(\{\, (i, j, J(i, j)) \mid (i, j) \in P \,\},\ \tau,\ p)
```

con $P = \{\, (i, j) \mid i < j \,\}$ en `detectarDirecto` y $P$ = las parejas
candidatas en `detectarConIndice`.

### Argumento de corrección

Ambas son composición de funciones ya argumentadas, así que la corrección sale
del modelo de sustitución: `huellas`, `parejas`, `similitudes`, `indiceInvertido`,
`parejasCandidatas` y `ranking` cumplen sus especificaciones, y cada
`detectar…` pasa el resultado de una a la siguiente. En `detectarDirecto`
$P$ es el resultado de `parejas(n)`; en `detectarConIndice`, el de
`parejasCandidatas(idx)`.

### Propiedad: una pareja que no es candidata tiene similitud cero

Si $(i, j)$ no es candidata, ningún fragmento $F$ cumple $i, j \in idx(F)$, es
decir, ningún $F$ está a la vez en $H_i$ y en $H_j$. Entonces
$H_i \cap H_j = \emptyset$ y

```math
J(i, j) = \frac{|H_i \cap H_j|}{|H_i \cup H_j|} = \frac{0}{|H_i \cup H_j|} = 0
```

(y si la unión es vacía, `jaccard` devuelve $0$ sin dividir).

### Propiedad: las dos soluciones dan el mismo ranking

Sea $\tau > 0$. Las mediciones que pasan el filtro de `ranking` son las de
similitud $\ge \tau > 0$.

- En `detectarDirecto` son las de las parejas $(i, j)$ con $J(i, j) \ge \tau$.
- Una pareja con $J(i, j) > 0$ es candidata (por la propiedad anterior), así que
  `detectarConIndice` también la mide, y con el mismo valor, porque usa las
  mismas huellas y el mismo `jaccard`. Las no candidatas valen $0 < \tau$ y la
  directa también las descarta.

Entonces el conjunto de mediciones que llega a la parte de ordenar y truncar es
el mismo. Como `ranking` tiene un orden total (ver 11), su resultado depende
solo de ese conjunto y no del orden en que llegó. Concluimos que

```math
\tau > 0 \implies \text{detectarDirecto}(c, k, \tau, p) == \text{detectarConIndice}(c, k, \tau, p)
```

Con $\tau = 0$ no es así: la directa incluye parejas de similitud $0$ que la del
índice nunca mide. Por eso las pruebas usan $\tau > 0$.

### Casos de prueba

Con `detectarDirecto` y con `detectarConIndice`, cada una con sus cinco:

| Prueba | Entrada | Resultado esperado | Qué comprueba |
|---|---|---|---|
| contrato | ejemplo 2, $k = 3$, $\tau = 0.3$, $p = 5$ | `(0,1)` con $0.75$ y `(2,3)` con $0.5$ | el resultado del enunciado |
| contrato | ejemplos 1 y 2 | directa $==$ con índice | las dos soluciones coinciden |
| propia | `Vector()` | `Vector()` | corpus vacío |
| propia | un solo documento | `Vector()` | no hay parejas |
| propia | `[x,y,z]`, `[x,y,w]`, `[p,q,r]`, $k = 2$, $\tau = 0.3$ | `Vector((0,1,1/3))` | solo la pareja con un fragmento en común (en la del índice, además, `==` a la directa) |
| propia | tres documentos `[x,y]`, $k = 2$, $\tau = 0.5$, $p = 2$ | `Vector((0,1,1.0), (0,2,1.0))` | trunca a $p$ y desempata por posición |
| propia | `[x,y]`, `[p,q]`, `[m,n]`, $k = 2$, $\tau = 0.1$ | `Vector()` | sin fragmentos en común no se reporta nada |
