import cats.data.*

package object conversion:
  type Error      = String
  type Decoded[A] = ValidatedNel[Error,A]
  val  NEL        = NonEmptyList
