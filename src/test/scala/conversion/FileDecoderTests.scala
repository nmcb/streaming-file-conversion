package conversion

import cats.data.Validated.{Invalid, Valid}
import org.scalatest.flatspec.*
import org.scalatest.matchers.should.*

class FileDecoderTests extends AnyFlatSpec with Matchers:

  import Decoder.*
  import DomainDecoders.given

  "Name" should "construct from valid values" in:
    "Last, First".as[Name] should be (Valid(Name("First", "Last")))
    "  Last  ,  First  ".as[Name] should be (Valid(Name("First", "Last")))


  "Name" should "not construct from invalid values" in:
    "".as[Name]   should be (Invalid(NEL.of("Invalid name: ''")))
    "X".as[Name]  should be (Invalid(NEL.of("Invalid name: 'X'")))
    ",".as[Name]  should be (Invalid(NEL.of("Invalid name: ','")))
    "X,".as[Name] should be (Invalid(NEL.of("Invalid name: 'X,'")))
    ",Y".as[Name] should be (Invalid(NEL.of("Invalid name: ',Y'")))

  "AddressLine" should "construct from valid values" in:
    "Street House #1".as[Address]     should be (Valid(Address("Street House #1")))
    "  Street House #1  ".as[Address] should be (Valid(Address("Street House #1")))

  "AddressLine" should "not construct from invalid values" in:
    "\n\t\r ".as[Address] should be (Invalid(NEL.of("Empty address line")))

  "PostalCode" should "construct from valid values" in:
    "1234 AB".as[PostalCode]     should be (Valid(PostalCode("1234 AB")))
    "  1234 AB  ".as[PostalCode] should be (Valid(PostalCode("1234 AB")))
    "1234ab".as[PostalCode]      should be (Valid(PostalCode("1234AB")))
    "1234 ab".as[PostalCode]     should be (Valid(PostalCode("1234 AB")))
    "123 456".as[PostalCode]     should be (Valid(PostalCode("123 456")))
    "  123 456  ".as[PostalCode] should be (Valid(PostalCode("123 456")))

  "PostalCode" should "not construct from invalid values" in:
    "  ".as[PostalCode] should be (Invalid(NEL.of("Empty postal code")))

  "Phone" should "construct from valid values" in:
    "+44123456789".as[Phone]     should be (Valid(Phone(Some("+44"), "123456789")))
    "  +44123456789  ".as[Phone] should be (Valid(Phone(Some("+44"), "123456789")))
    "+44 123456789".as[Phone]    should be (Valid(Phone(Some("+44"), "123456789")))
    "01023456789".as[Phone]      should be (Valid(Phone(None, "01023456789")))
    "010-23456789".as[Phone]     should be (Valid(Phone(None, "01023456789")))
    "010-234 567 89".as[Phone]   should be (Valid(Phone(None, "01023456789")))
    "06-23456789".as[Phone]      should be (Valid(Phone(None, "0623456789")))
    " + 4 4 1 2 3 4 5 6 7 8 9 ".as[Phone] should be (Valid(Phone(Some("+44"), "123456789")))

  "Phone" should "not construct from invalid values" in:
    "".as[Phone]            should be (Invalid(NEL.of("Invalid phone number: ''")))
    " ".as[Phone]           should be (Invalid(NEL.of("Invalid phone number: ' '")))
    "#44 4567890".as[Phone] should be (Invalid(NEL.of("Invalid phone number: '#44 4567890'")))

