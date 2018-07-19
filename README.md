# Streaming File Conversion

Converts two different file formats, i.e. `.csv` or `.flr`, containing debit limits into an `.html` representation.

## Contextual Rationale

1. Thy shall check thy inputs for malformities.
2. Parsable input should be normalized as much as possible.
3. We allow for partial failure, i.e. we convert each line that could be parsed to html.
4. We focus on easy extensibility, i.e. support for new file types.

### Assumptions

- Name will be parsed comma separated first- and last-name string values, both mandatory and trimmed for `.csv` and `.flr`.

- Address will be parsed as the trimmed (non-empty) string value, both for `.csv` and `.flr`.

- Postal code will be parsed as the trimmed upper cased value, both for `.csv` and for `.flr`.

- Phone numbers will be parsed as trimmed sequences of numbers, `-` and ` ` characters removed both for `.csv` and `.flr`.
  Numbers starting with a `+` followed by two digits will be recognized as international numbers.  The rationale here
  is to make automated calling, i.e. a computer doing the call, possible.

- Debit value currencies will be parsed as Scala doubles, `.csv` values are interpreted floating point, `.flr` values in
  cents, i.e. will be divided by a 100 after conversion.  Note, we do not allow floating points in the `.flr` debit
  values.  See https://www.scala-lang.org/files/archive/spec/2.12/01-lexical-syntax.html#floating-point-literals

- Birthday values will be parsed according to format `dd/MM/yyyy` (`.csv`) and `yyyyMMdd` (`.flr`)

- `.csv` debit lines match the regular expression `\"(.*)\",(.*),(.*),(.*),(.*),(.*)`, i.e. note that the first cell is
  delimited by double quotes.  `.flr` lines are delimited on column length, respectively 16, 22, 9, 13, 14 and 8 character
  wide for the name, address, postal code, phone number, debit and birthday values.  For the moment we assume Euros
  and display in the Dutch locale.

- Errors are additive, i.e. if a line matches the pattern for a given file type than all errors that occur in that line
  are reported.  We don't "drop" lines from the files, even if we know they can't be parsed like the column headers and
  trailing new-line, we just ignore them, though, of course, we do write occurrences of these to standard out.

### Implementation Notes

- File decoding and html rendering using type classes.
- Error reporting during decoding using cats a Validated[NonEmptyList[Error],DebitRecord].
- Error aggregation using cats higher-kinded apply, logging to console.
- Program purely functional using fs2 stream composition with cats effects.
- Main takes a commandline argument as filename, defaults to `data.csv`.
- Html output is written to file with `<filename>.<ext>.html` extension.
- New file-type support would require:
- - Adding an object similar to the ``.flr`` and ``.csv`` ones in the `decoders.scala` file.
- - Updating the `DebitRecorderDecoder` trait in the `main.scala` file.

### Building / Testing / Running

Just look at the console trail below, it's pretty easy to follow ... Enjoy!

```
streaming-file-conversion ∴ sbt
[info] Loading project definition from /Users/marco/code/streaming-file-conversion/project
[info] Loading settings from build.sbt ...
[info] Set current project to streaming-file-conversion (in build file:/Users/marco/code/streaming-file-conversion/)
[info] sbt server started at local:///Users/marco/.sbt/1.0/server/a7546f9c1d984755fc65/sock
sbt:streaming-file-conversion> test
[info] Compiling 3 Scala sources to /Users/marco/code/streaming-file-conversion/target/scala-2.12/test-classes ...
[info] Done compiling.
[info] FileDecoderTests:
[info] Name
[info] - should construct from valid values
[info] Name
[info] - should not construct from invalid values
[info] AddressLine
[info] - should construct from valid values
[info] AddressLine
[info] - should not construct from invalid values
[info] PostalCode
[info] - should construct from valid values
[info] PostalCode
[info] - should not construct from invalid values
[info] Phone
[info] - should construct from valid values
[info] Phone
[info] - should not construct from invalid values
[info] FLRDecoderTests:
[info] BirthDay
[info] - should construct from valid values
[info] BirthDay
[info] - should construct not from valid values
[info] Debit
[info] - should construct from valid values
[info] Debit
[info] - should not construct from invalid values
[info] DebitRecord
[info] - should construct from valid values
[info] DebitRecord
[info] - should not construct from invalid values
[info] CSVDecoderTests:
[info] BirthDay
[info] - should construct from valid values
[info] BirthDay
[info] - should construct not from valid values
[info] Debit
[info] - should construct from valid values
[info] Debit
[info] - should not construct from invalid values
[info] DebitRecord
[info] - should construct from valid values
[info] DebitRecord
[info] - should not construct from invalid values
[info] Run completed in 1 second, 33 milliseconds.
[info] Total number of tests run: 20
[info] Suites: completed 3, aborted 0
[info] Tests: succeeded 20, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 9 s, completed Jul 19, 2018 10:42:39 AM
sbt:streaming-file-conversion> run data.csv
[info] Running conversion.Main
Invalid line pattern `\"(.*)\",(.*),(.*),(.*),(.*),(.*)`: 'Name,Address,Postcode,Phone,Debit Limit,Birthday'
Invalid line pattern `\"(.*)\",(.*),(.*),(.*),(.*),(.*)`: ''
[success] Total time: 1 s, completed Jul 19, 2018 10:42:44 AM
sbt:streaming-file-conversion>
```