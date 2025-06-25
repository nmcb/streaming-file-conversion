package conversion

import java.time.LocalDate

import cats.data.Validated.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.should.*

class CSVDecoderTests extends AnyFlatSpec with Matchers {

  import Decoder._
  import CSV._

  "BirthDay" should "construct from valid values" in {
    "01/01/1987".as[BirthDay] should be (Valid(BirthDay(LocalDate.of(1987, 1, 1))))
  }
  "BirthDay" should "construct not from valid values" in {
    "01-01-1987".as[BirthDay] should be (Invalid(NEL.of("Invalid birthday: '01-01-1987'")))
  }

  "Debit" should "construct from valid values" in {
    "1234567890".as[Debit] should be (Valid(Debit(1234567890d)))
    " 1234567890 ".as[Debit] should be (Valid(Debit(1234567890d)))
    "12345678.90".as[Debit] should be (Valid(Debit(12345678.90d)))
    s"${Double.MaxValue}".as[Debit] should be (Valid(Debit(Double.MaxValue)))
  }
  "Debit" should "not construct from invalid values" in {
    "".as[Debit] should be (Invalid(NEL.of("Invalid debit value: ''")))
    "1,00".as[Debit] should be (Invalid(NEL.of("Invalid debit value: '1,00'")))
  }

  "DebitRecord" should "construct from valid values" in {
    CSVFixture.testLine.as[DebitRecord] should be (
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
    import CSVFixture._
    faultyLine.as[DebitRecord] should be (
      Invalid(NEL.of(s"Invalid line pattern `${CSV.recordPattern.regex}`: '$faultyLine'"))
    )
    emptyLine.as[DebitRecord] should be (
      Invalid(
        NEL.of(
          "Invalid name: ','",
          "Empty address line",
          "Empty postal code",
          "Invalid phone number: ''",
          "Invalid debit value: ''",
          "Invalid birthday: ''"
        )
      )
    )
  }
}

object CSVFixture {
  val testLine: String =
    """"Johnson, John",Voorstraat 32,3122gg,020 3849381,10000,01/01/1987"""
  val faultyLine: String =
    """ Last , First , Address , Code , 020 3849381 , 100 , 01/01/1987 """
  val emptyLine: String =
    """",",,,,,"""
}
