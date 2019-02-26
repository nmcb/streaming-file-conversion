package conversion

import java.time.LocalDate

import cats.data.Validated.{Invalid, Valid}
import org.scalatest.{FlatSpec, Matchers}

class FLRDecoderTests extends FlatSpec with Matchers {

  import Decoder._
  import FLR._
  import FLRFixture._


  "BirthDay" should "construct from valid values" in {
    "19870101".as[BirthDay] should be
      (Valid(BirthDay(LocalDate.of(1987, 1, 1))))
  }
  "BirthDay" should "construct not from valid values" in {
    "01-01-1987".as[BirthDay] should be
      (Invalid(NEL.of("Invalid birthday: '01-01-1987'")))
  }

  "Debit" should "construct from valid values" in {
    "10000".as[Debit] should be
      (Valid(Debit(100.00d)))
    " 12345 ".as[Debit] should be
      (Valid(Debit(123.45d)))
    s"${Double.MaxValue}".as[Debit] should be
      (Valid(Debit(Double.MaxValue / 100)))
  }
  "Debit" should "not construct from invalid values" in {
    "".as[Debit] should be
      (Invalid(NEL.of("Invalid debit value: ''")))
    "1,00".as[Debit] should be
      (Invalid(NEL.of("Invalid debit value: '1,00'")))
  }

  "DebitRecord" should "construct from valid values" in {
    testLine.as[DebitRecord] should be (
      Valid(
        DebitRecord(
          Name("John", "Johnson"),
          Address("Voorstraat 32"),
          PostalCode("3122GG"),
          Phone(None, "0203849381"),
          Debit(10000d),
          BirthDay(LocalDate.of(1987, 1, 1))
        )
      )
    )
  }
  "DebitRecord" should "not construct from invalid values" in {
    faultyLine.as[DebitRecord] should be (
      Invalid(NEL.of(s"Invalid line pattern `${FLR.recordPattern.regex}`: '$faultyLine'"))
    )
    emptyLine.as[DebitRecord] should be (
      Invalid(
        NEL.of(
          "Invalid name: '                '",
          "Empty address line",
          "Empty postal code",
          "Invalid phone number: '              '",
          "Invalid debit value: '             '",
          "Invalid birthday: '        '"
        )
      )
    )
  }
}

object FLRFixture {
  val emptyLine: String =
    """                                                                                  """
  val testLine: String =
    """Johnson, John   Voorstraat 32         3122gg   020 3849381        1000000 19870101"""
  val faultyLine: String =
    """Johnson, John   Voorstraat 32         3122gg   020 3849381        1000000 19870101""".drop(1)
}
