package conversion

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.Apply
import cats.data.ValidatedNel
import cats.data.Validated.{invalid, invalidNel, valid}

import scala.util.Try
import scala.util.matching.Regex

trait Decoder[A] {
  def decode(str: String): Decoded[A]
}

object Decoder {
  implicit class DecoderOps(val str: String) {
    def as[A : Decoder]: Decoded[A] =
      implicitly[Decoder[A]].decode(str)
  }
}

object CSV {

  val recordPattern: Regex =
    """\"(.*)\",(.*),(.*),(.*),(.*),(.*)""".r

  implicit val birthDayDecoder: Decoder[BirthDay] =
    DomainDecoders.defaultBirthDayDecoder("dd/MM/yyyy")

  implicit val debitDecoder: Decoder[Debit] =
    DomainDecoders.defaultDebitDecoder()

  implicit val debitRecordDecoder: Decoder[DebitRecord] =
    DomainDecoders.defaultDebitRecordDecoder(recordPattern)
}

object FLR {

  val recordPattern: Regex =
    """(.{16})(.{22})(.{9})(.{14})([0-9|\\ ]{13})(.{8})""".r

  implicit val debitDecoder: Decoder[Debit] =
    DomainDecoders.defaultDebitDecoder(100)

  implicit val birthDayDecoder: Decoder[BirthDay] =
    DomainDecoders.defaultBirthDayDecoder("yyyyMMdd")

  implicit val debitRecordDecoder: Decoder[DebitRecord] =
    DomainDecoders.defaultDebitRecordDecoder(recordPattern)
}

object DomainDecoders {

  val phonePattern: Regex = "(\\+[0-9][0-9])?([0-9]+)".r

  import Decoder._

  implicit val nameDecoder: Decoder[Name] = (str: String) =>
    (str.split(',').map(_.trim) match {
      case Array(last, first) if last.nonEmpty && first.nonEmpty => valid(Name(first, last))
      case _ => invalid(s"Invalid name: '$str'")
    }).toValidatedNel

  implicit val addressDecoder: Decoder[Address] = (str: String) => (
    if (str.trim.nonEmpty) valid(Address(str.trim))
    else invalid("Empty address line")
  ).toValidatedNel

  implicit val postalCodeDecoder: Decoder[PostalCode] = (str: String) => (
    if (str.trim.nonEmpty) valid(PostalCode(str.trim.toUpperCase))
    else invalid("Empty postal code")
  ).toValidatedNel

  implicit val phoneDecoder: Decoder[Phone] = (str: String) =>
    (str.trim.replaceAll("\\-|\\ ", "") match {
      case phonePattern(code, number) => valid(Phone(Option(code), number))
      case _ => invalid(s"Invalid phone number: '$str'")
    }).toValidatedNel

  def defaultDebitDecoder(base: Int = 1): Decoder[Debit] = (str: String) =>
    Try(valid(Debit(str.trim.toDouble / base)))
      .getOrElse(invalid(s"Invalid debit value: '$str'"))
      .toValidatedNel

  def defaultBirthDayDecoder(pattern: String): Decoder[BirthDay] = (str: String) =>
    Try(valid(BirthDay(LocalDate.parse(str.trim, DateTimeFormatter.ofPattern(pattern)))))
      .getOrElse(invalid(s"Invalid birthday: '$str'"))
      .toValidatedNel

  def defaultDebitRecordDecoder(recordPattern: Regex)(
    implicit
    CD: Decoder[Debit],
    BD: Decoder[BirthDay]
  ): Decoder[DebitRecord] = {
    case recordPattern(c1, c2, c3, c4, c5, c6) =>
      Apply.apply(using Apply[Decoded]).map6(
        c1.as[Name],
        c2.as[Address],
        c3.as[PostalCode],
        c4.as[Phone],
        c5.as[Debit],
        c6.as[BirthDay]
      ) { case (nm, ad, pc, ph, cr, bd) => DebitRecord(nm, ad, pc, ph, cr, bd) }
    case str =>
      invalidNel(s"Invalid line pattern `${recordPattern.regex}`: '$str'")
  }
}
