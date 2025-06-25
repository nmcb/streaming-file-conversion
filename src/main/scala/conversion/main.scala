package conversion

import cats.effect.*
import fs2.*

import cats.data.Validated.*
import fs2.io.file.*

trait DebitRecordDecoder:

  def debitRecorderDecoder(path: Path): Decoder[DebitRecord] =
    path.toString match
      case s if s.endsWith(".csv") => CSV.debitRecordDecoder
      case s if s.endsWith(".flr") => FLR.debitRecordDecoder
      case s => sys.error(s"Unsupported file extension: '${s.reverse.takeWhile(_ != '.').reverse}'")


object Main extends App with DebitRecordDecoder:

  import cats.effect.unsafe.implicits.global

  import Decoder.*
  import Html.*
  import DebitLineHtml.*
  import DebitLineHtml.given

  val input: Path =
    if (args.isEmpty) Path("data.csv")
    else Path(args.head)

  val output: Path =
    input.resolveSibling(input.toString + ".html")

  def streamErrorsToStdOut[F[_]: Async]: Pipe[F, Decoded[DebitRecord], DebitRecord] =
    stream => stream.flatMap:
      case Valid(record)   => Stream.emit(record)
      case Invalid(errors) => Stream.exec(implicitly[Sync[F]].delay(errors.map(println)))

  def emit[F[_]: Async](string: String): Stream[F, Byte] =
    Stream.emit(string).through(text.utf8.encode)

  implicit val debitRecordDecoder: Decoder[DebitRecord] =
    debitRecorderDecoder(input)

  def records[F[_]: Async]: Stream[F, Byte] =
    Files(using Files.forAsync).readAll(input, 4096, Flags.Read)
      .through(text.utf8.decode)
      .through(text.lines)
      .map(_.as[DebitRecord])
      .through(streamErrorsToStdOut)
      .map(_.render)
      .through(text.utf8.encode)

  def html[F[_]: Async]: Stream[F, Byte] =
    emit(header) ++ records ++ emit(footer)

  html[IO]
    .through(Files[IO].writeAll(output))
    .compile
    .drain
    .unsafeRunSync()
