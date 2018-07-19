package conversion

trait Html[A] {
  def toHtml(a: A): String
}

object Html {
  implicit class HtmlOps[A : Html](val a: A) {
    def render: String =
      implicitly[Html[A]].toHtml(a)
  }
}

object DebitLineHtml {

  import Html._

  val header: String =
    s"""<html>
       |  <head><meta charset="UTF-8"></head>
       |<body>
       |<table>
       |<tr>
       |  <th>Name</th>
       |  <th>Address</th>
       |  <th>Postal Code</th>
       |  <th>Phone</th>
       |  <th>Debit</th>
       |  <th>Birthday</th>
       |</tr>
     """.stripMargin.trim + '\n'

  val footer: String =
    s"</table></body></html>" + '\n'

  implicit val nameHtml: Html[Name] = (name: Name) =>
    s"<td><b>${name.last} (${name.first})</b></td>"

  implicit val addressHtml: Html[Address] = (address: Address) =>
    s"<td>${address.value}</td>"

  implicit val postalCodeHtml: Html[PostalCode] = (postalCode: PostalCode) =>
    s"<td>${postalCode.value}</td>"

  implicit val phoneHtml: Html[Phone] = (phone: Phone) =>
    s"<td>${phone.toPhoneString}</td>"

  implicit val debitHtml: Html[Debit] = (debit: Debit) =>
    s"<td><b>${debit.show}</b></td>"

  implicit val birthDay: Html[BirthDay] = (birthDay: BirthDay) =>
    s"<td><i>${birthDay.date}</i></td>"

  implicit val debitRecordHtml: Html[DebitRecord] = (record: DebitRecord) =>
    s"""<tr>
       |  ${record.name.render}
       |  ${record.address.render}
       |  ${record.postalCode.render}
       |  ${record.phone.render}
       |  ${record.debit.render}
       |  ${record.birthDay.render}
       |</tr>
     """.stripMargin.trim + '\n'

}