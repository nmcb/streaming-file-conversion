import cats.Apply
import cats.data.{NonEmptyList, ValidatedNel}

package object conversion {
  type Error = String
  type Decoded[A] = ValidatedNel[Error, A]
  val  NEL   = NonEmptyList
}
