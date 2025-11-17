package h4

// Similar to e33, implement a simple JSON serializer using type-classes
// Make sure the statements in the main can be executed. The sample output is given in comments. You can do the indentation
// as you like.

class PhoneNo(val prefix: Int, val number: Int)
class Person(val firstName: String, val lastName: String, val phone: PhoneNo)
class Address(val person: Person, val street: String, val city: String)

// ... add the necessary classes
def wrapObj(str: String): String =
  "{ " + str + " }"

extension (base: String)
  def addToStr(key: String, value: String): String =
    val pair = "\"" + key + "\": " + value
    if base.isEmpty then pair
    else base + ", " + pair

trait JsonSerializer[T]:
  def serialize(obj: T): String

  extension (x: T)
    def toJson: String = serialize(x)

object JsonSerializer:
  given JsonSerializer[String] with
    def serialize(obj: String) = "\"" + obj + "\""

  given JsonSerializer[Int] with
    def serialize(obj: Int) = obj.toString

  given listSerializer[T](using js: JsonSerializer[T]):
  JsonSerializer[List[T]] with
    def serialize(xs: List[T]) =
      xs.map(_.toJson).mkString("[ ", ", ", " ]")

  given mapSerializer[T](using js: JsonSerializer[T]):
  JsonSerializer[Map[String, T]] with
    def serialize(m: Map[String, T]) =
      m.map((k, v) => "\"" + k + "\": " + v.toJson).mkString("{ ", ", ", " }")

//object PhoneNo:
  given JsonSerializer[PhoneNo] with
    def serialize(obj: PhoneNo) =
      var temp = "".addToStr("prefix", obj.prefix.toJson)
      temp = temp.addToStr("number", obj.number.toJson)
      wrapObj(temp)

//  object Person:
  given JsonSerializer[Person] with
    def serialize(obj: Person) =
      var temp = "".addToStr("firstName", obj.firstName.toJson)
      temp = temp.addToStr("lastName", obj.lastName.toJson)
      temp = temp.addToStr("phone", obj.phone.toJson)
      wrapObj(temp)

//  object Address:
  given JsonSerializer[Address] with
    def serialize(obj: Address) =
      var temp = "".addToStr("person", obj.person.toJson)
      temp = temp.addToStr("street", obj.street.toJson)
      temp = temp.addToStr("city", obj.city.toJson)
      wrapObj(temp)

object JsonSerializerTest:
  def main(args: Array[String]): Unit =
    import JsonSerializer.given
    val a1 = "Hello"
//    println(a1.toJson) // "Hello"
    if !(a1.toJson.equals("\"Hello\"")) then println("Error")

    val a2 = 12
//    println(a2.toJson) // 12
    if !(a2.toJson.equals("12")) then println("Error")

    val b1 = List("ab", "cd")
    val b2 = List("ef", "gh")
//    println(b1.toJson) // [ "ab", "cd" ]
    if !(b1.toJson.equals("[ \"ab\", \"cd\" ]")) then println("Error")

    val c1 = List(b1, b2)
//    println(c1.toJson) // [ [ "ab", "cd" ], [ "ef", "gh" ] ]
    if !(c1.toJson.equals("[ [ \"ab\", \"cd\" ], [ \"ef\", \"gh\" ] ]")) then println("Error")

    val c2 = Map("b1" -> b1, "b2" -> b2)
//    println(c2.toJson) // { "b1": [ "ab", "cd" ], "b2": [ "ef", "gh" ] }
    if !(c2.toJson.equals("{ \"b1\": [ \"ab\", \"cd\" ], \"b2\": [ \"ef\", \"gh\" ] }")) then println("Error")

    val d1 = Person("John", "Doe", PhoneNo(1, 123456))
    val d2 = Person("Jane", "X", PhoneNo(420, 345678))
//    println(d1.toJson) // { "firstName": "John", "lastName": "Doe", "phone": { "prefix": 1, "number": 123456 } }
    if !(d1.toJson.equals("{ \"firstName\": \"John\", \"lastName\": \"Doe\", \"phone\": { \"prefix\": 1, \"number\": 123456 } }")) then println("Error")

    val e1 = Address(d1, "Bugmore Lane 3", "Lerfourche")
    val e2 = Address(d2, "West End Woods 1", "Holmefefer")

    val f = List(e1, e2)
//    println(f.toJson) // [ { "person": { "firstName": "John", "lastName": "Doe", "phone": { "prefix": 1, "number": 123456 } }, "street": "Bugmore Lane 3", "city": "Lerfourche" }, { "person": { "firstName": "Jane", "lastName": "X", "phone": { "prefix": 420, "number": 345678 } }, "street": "West End Woods 1", "city": "Holmefefer" } ]
    if !(f.toJson.equals("[ { \"person\": { \"firstName\": \"John\", \"lastName\": \"Doe\", \"phone\": { \"prefix\": 1, \"number\": 123456 } }, \"street\": \"Bugmore Lane 3\", \"city\": \"Lerfourche\" }, { \"person\": { \"firstName\": \"Jane\", \"lastName\": \"X\", \"phone\": { \"prefix\": 420, \"number\": 345678 } }, \"street\": \"West End Woods 1\", \"city\": \"Holmefefer\" } ]"))
      then println("Error")