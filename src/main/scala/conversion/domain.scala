package conversion

import java.time.LocalDate
import java.text.NumberFormat
import java.util.Locale

case class Name(first: String, last: String)

case class Address(value: String)

case class PostalCode(value: String)

case class Phone(country: Option[String], number: String) {
  def toPhoneString: String =
    country.getOrElse("") + number
  def isInternational: Boolean =
    country.nonEmpty
}

case class Debit(value: Double) {
  def show: String =
    NumberFormat.getCurrencyInstance(new Locale("nl", "NL")).format(value)
}

case class BirthDay(date: LocalDate)

case class DebitRecord(
  name: Name,
  address: Address,
  postalCode: PostalCode,
  phone: Phone,
  debit: Debit,
  birthDay: BirthDay
)
