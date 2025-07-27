package conversion

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.Apply
import cats.data.Validated.*

import scala.util.Try
import scala.util.matching.Regex

trait Decoder[A]:
  def decode(str: String): Decoded[A]


object Decoder:
  extension (str: String)
    def as[A : Decoder]: Decoded[A] =
      summon[Decoder[A]].decode(str)


object CSV:

  val recordPattern: Regex =
    """\"(.*)\",(.*),(.*),(.*),(.*),(.*)""".r

  given birthDayDecoder: Decoder[BirthDay] =
    DomainDecoders.defaultBirthDayDecoder("dd/MM/yyyy")

  given debitDecoder: Decoder[Debit] =
    DomainDecoders.defaultDebitDecoder()

  given debitRecordDecoder: Decoder[DebitRecord] =
    DomainDecoders.defaultDebitRecordDecoder(recordPattern)


object FLR:

  val recordPattern: Regex =
    """(.{16})(.{22})(.{9})(.{14})([0-9|\\ ]{13})(.{8})""".r

  given debitDecoder: Decoder[Debit] =
    DomainDecoders.defaultDebitDecoder(100)

  given birthDayDecoder: Decoder[BirthDay] =
    DomainDecoders.defaultBirthDayDecoder("yyyyMMdd")

  given debitRecordDecoder: Decoder[DebitRecord] =
    DomainDecoders.defaultDebitRecordDecoder(recordPattern)


object DomainDecoders:

  val phonePattern: Regex = "(\\+[0-9][0-9])?([0-9]+)".r

  import Decoder.*

  given nameDecoder: Decoder[Name] =
    (str: String) =>
      (str.split(',').map(_.trim) match
        case Array(last, first) if last.nonEmpty && first.nonEmpty => valid(Name(first, last))
        case _                                                     => invalid(s"Invalid name: '$str'")
      ).toValidatedNel

  given addressDecoder: Decoder[Address] =
    (str: String) => (
      if str.trim.nonEmpty then
        valid(Address(str.trim))
      else
        invalid("Empty address line")
    ).toValidatedNel

  given postalCodeDecoder: Decoder[PostalCode] =
    (str: String) => (
      if str.trim.nonEmpty then
        valid(PostalCode(str.trim.toUpperCase))
      else
        invalid("Empty postal code")
    ).toValidatedNel

  given phoneDecoder: Decoder[Phone] =
    (str: String) =>
      (str.trim.replaceAll("[- ]", "") match
        case phonePattern(code, number) => valid(Phone(Option(code), number))
        case _                          => invalid(s"Invalid phone number: '$str'")
      ).toValidatedNel

  def defaultDebitDecoder(base: Int = 1): Decoder[Debit] =
    (str: String) =>
      Try(valid(Debit(str.trim.toDouble / base)))
        .getOrElse(invalid(s"Invalid debit value: '$str'"))
        .toValidatedNel

  def defaultBirthDayDecoder(pattern: String): Decoder[BirthDay] =
    (str: String) =>
      Try(valid(BirthDay(LocalDate.parse(str.trim, DateTimeFormatter.ofPattern(pattern)))))
        .getOrElse(invalid(s"Invalid birthday: '$str'"))
        .toValidatedNel

  def defaultDebitRecordDecoder(recordPattern: Regex)(using Decoder[Debit], Decoder[BirthDay]): Decoder[DebitRecord] =
    case recordPattern(c1, c2, c3, c4, c5, c6) =>
      Apply(using Apply[Decoded]).map6(
        c1.as[Name],
        c2.as[Address],
        c3.as[PostalCode],
        c4.as[Phone],
        c5.as[Debit],
        c6.as[BirthDay]
      )((nm, ad, pc, ph, cr, bd) => DebitRecord(nm, ad, pc, ph, cr, bd))
    case str =>
      invalidNel(s"Invalid line pattern `${recordPattern.regex}`: '$str'")
