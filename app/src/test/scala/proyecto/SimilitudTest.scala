package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import Similitud._

/**
 * Pruebas de contrato de la versión secuencial: una por función, con los
 * valores de los dos ejemplos del enunciado. No se modifican.
 *
 * La rúbrica pide además cinco casos propios por función, distintos de estos y
 * de los ejemplos del enunciado. Van al final de este archivo o en un archivo
 * nuevo de esta misma carpeta; las dos formas valen.
 */
@RunWith(classOf[JUnitRunner])
class SimilitudTest extends AnyFunSuite {

  val eps = 1e-9

  // Ejemplo 1 del enunciado, con las palabras ya normalizadas para que estas
  // pruebas no dependan unas de otras.
  val d0: Documento = ("d0", Vector("el", "gato", "duerme", "en", "el", "sofa"))
  val d1: Documento = ("d1", Vector("el", "gato", "duerme", "en", "la", "alfombra"))
  val d2: Documento = ("d2", Vector("la", "lluvia", "moja", "el", "parque"))
  val c1: Corpus = Vector(d0, d1, d2)

  // Ejemplo 2 del enunciado.
  val e0: Documento = ("e0", Vector("la", "funcion", "recursiva", "termina",
    "cuando", "llega", "al", "caso", "base"))
  val e1: Documento = ("e1", Vector("toda", "funcion", "recursiva", "termina",
    "cuando", "llega", "al", "caso", "base"))
  val e2: Documento = ("e2", Vector("el", "paralelismo", "divide", "el",
    "trabajo", "en", "tareas", "independientes"))
  val e3: Documento = ("e3", Vector("el", "paralelismo", "divide", "el",
    "trabajo", "en", "hilos", "independientes"))
  val c2: Corpus = Vector(e0, e1, e2, e3)

  test("normalizar: baja a minúsculas y parte en palabras") {
    assert(normalizar("El Gato Duerme") == Vector("el", "gato", "duerme"))
  }

  test("fragmentos: son las ventanas consecutivas, en orden") {
    val ts = Vector("a", "b", "c", "d")
    assert(fragmentos(ts, 2) ==
      Vector(Vector("a", "b"), Vector("b", "c"), Vector("c", "d")))
  }

  test("huella: el ejemplo 1 tiene 4, 4 y 3 fragmentos") {
    assert(huella(d0, 3).size == 4)
    assert(huella(d1, 3).size == 4)
    assert(huella(d2, 3).size == 3)
  }

  test("huellas: hay una huella por documento") {
    assert(huellas(c1, 3).length == 3)
  }

  test("jaccard: ejemplo 2, las dos parejas dan 0.75 y 0.5") {
    assert(math.abs(jaccard(huella(e0, 3), huella(e1, 3)) - 0.75) < eps)
    assert(math.abs(jaccard(huella(e2, 3), huella(e3, 3)) - 0.5) < eps)
  }

  test("parejas: hay n (n - 1) / 2") {
    assert(parejas(5).length == 10)
  }

  test("similitudes: el ejemplo 1 da 1/3, 0 y 0") {
    val hs = huellas(c1, 3)
    val ms = similitudes(hs, parejas(3)).map(_._3)
    assert(math.abs(ms(0) - 2.0 / 6.0) < eps && ms(1) == 0.0 && ms(2) == 0.0)
  }

  test("indiceInvertido: el fragmento inicial de d0 está en d0 y en d1") {
    val idx = indiceInvertido(huellas(c1, 3))
    assert(idx(Vector("el", "gato", "duerme")) == Set(0, 1))
  }

  test("parejasCandidatas: en el ejemplo 2 quedan dos de seis") {
    val idx = indiceInvertido(huellas(c2, 3))
    assert(parejasCandidatas(idx) == Vector((0, 1), (2, 3)))
  }

  test("ranking: ordena de mayor a menor y trunca a p") {
    val ms: Vector[Medicion] = Vector((0, 1, 0.4), (0, 2, 0.1), (1, 2, 0.9))
    assert(ranking(ms, 0.0, 10).map(_._3) == Vector(0.9, 0.4, 0.1))
    assert(ranking(ms, 0.0, 2).length == 2)
  }

  test("detectar: el ejemplo 2 reporta (0, 1) con 0.75 y (2, 3) con 0.5") {
    val r = detectarDirecto(c2, 3, 0.3, 5)
    assert(r.map(m => (m._1, m._2)) == Vector((0, 1), (2, 3)))
    assert(math.abs(r(0)._3 - 0.75) < eps && math.abs(r(1)._3 - 0.5) < eps)
  }

  test("detectar: las dos soluciones coinciden en los ejemplos") {
    assert(detectarDirecto(c1, 3, 0.2, 5) == detectarConIndice(c1, 3, 0.2, 5))
    assert(detectarDirecto(c2, 3, 0.3, 5) == detectarConIndice(c2, 3, 0.3, 5))
  }

  //Pruebas fragmentos
  test("fragmentos: con menos de k palabras da vacío") {
    assert(fragmentos(Vector("hola"), 2) == Vector())
  }

  test("fragmentos: el documento vacío da vacío") {
    assert(fragmentos(Vector(), 2) == Vector())
  }

  test("fragmentos: con exactamente k palabras da un solo fragmento") {
    assert(fragmentos(Vector("hola", "mundo"), 2) == Vector(Vector("hola", "mundo")))
  }

  test("fragmentos: devuelve t - k + 1 fragmentos") {
    assert(fragmentos(Vector("yo", "como", "arroz", "con", "huevo"), 3).length == 3)
  }

  test("fragmentos: con k = 1 cada palabra es un fragmento") {
    assert(fragmentos(Vector("x", "y", "z"), 1) ==
      Vector(Vector("x"), Vector("y"), Vector("z")))
  }

  //Pruebas huella
  test("huella: un fragmento repetido cuenta una sola vez") {
    val d: Documento = ("r", Vector("a", "b", "a", "b"))
    assert(huella(d, 2) == Set(Vector("a", "b"), Vector("b", "a")))
  }

  test("huella: con menos de k palabras da el conjunto vacío") {
    assert(huella(("corto", Vector("hola")), 3) == Set())
  }

  test("huella: el documento vacío da el conjunto vacío") {
    assert(huella(("vacio", Vector()), 2) == Set())
  }

  test("huella: todas las palabras iguales dan un solo fragmento") {
    assert(huella(("igual", Vector("x", "x", "x", "x")), 2) == Set(Vector("x", "x")))
  }

  test("huella: sobre un texto normalizado") {
    val d: Documento = ("t", normalizar("Hola, MUNDO; hola!"))
    assert(huella(d, 2) == Set(Vector("hola", "mundo"), Vector("mundo", "hola")))
  }

  //Pruebas huellas
  test("huellas: el corpus vacío da el vector vacío") {
    assert(huellas(Vector(), 2) == Vector())
  }

  test("huellas: conserva el orden del corpus") {
    val c: Corpus = Vector(("a", Vector("x", "y")), ("b", Vector("y", "z")))
    assert(huellas(c, 2) == Vector(Set(Vector("x", "y")), Set(Vector("y", "z"))))
  }

  test("huellas: cada posición coincide con huella del documento") {
    val c: Corpus = Vector(("a", Vector("uno", "dos", "tres")), ("b", Vector("cuatro")))
    val hs = huellas(c, 2)
    assert(hs(0) == huella(c(0), 2) && hs(1) == huella(c(1), 2))
  }

  test("huellas: un documento corto deja su huella vacía") {
    val c: Corpus = Vector(("a", Vector("p", "q", "r")), ("b", Vector("p")))
    assert(huellas(c, 2)(1).isEmpty)
  }

  test("huellas: documentos iguales tienen la misma huella") {
    val c: Corpus = Vector(("a", normalizar("El sol sale")), ("b", normalizar("el SOL sale")))
    val hs = huellas(c, 2)
    assert(hs(0) == hs(1))
  }

  //Pruebas jaccard
  test("jaccard: dos huellas vacías dan cero") {
    assert(jaccard(Set.empty, Set.empty) == 0.0)
  }

  test("jaccard: una huella vacía y otra no vacía dan cero") {
    assert(jaccard(Set.empty, Set(Vector("sol"))) == 0.0)
  }

  test("jaccard: huellas sin fragmentos en común dan cero") {
    assert(jaccard(Set(Vector("sol")), Set(Vector("luna"))) == 0.0)
  }

  test("jaccard: dos huellas iguales no vacías dan uno") {
    val conjunto = Set(Vector("sol"), Vector("luna"))
    assert(jaccard(conjunto, conjunto) == 1.0)
  }

  test("jaccard: informe final se cuenta una sola vez en la unión") {
    val primera = Set(Vector("informe", "final"), Vector("costos", "estimados"))
    val segunda = Set(Vector("informe", "final"), Vector("ventas", "mensuales"),
      Vector("saldo", "pendiente"))

    assert(jaccard(primera, segunda) == 0.25)
  }

  //Pruebas parejas
  test("parejas: un corpus vacío no tiene parejas") {
    assert(parejas(0).isEmpty)
  }

  test("parejas: un documento no se compara consigo mismo") {
    assert(parejas(1).isEmpty)
  }

  test("parejas: dos documentos se comparan una sola vez") {
    assert(parejas(2) == Vector((0, 1)))
  }

  test("parejas: cuatro documentos dan las seis parejas en orden") {
    assert(parejas(4) == Vector((0, 1), (0, 2), (0, 3), (1, 2), (1, 3), (2, 3)))
  }

  test("parejas: el sexto documento se compara con todos los anteriores") {
    assert(parejas(6).filter(_._2 == 5) == Vector((0, 5), (1, 5), (2, 5), (3, 5), (4, 5)))
  }
}
