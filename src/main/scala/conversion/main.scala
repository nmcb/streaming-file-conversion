package conversion

import cats.effect.*
import fs2.{Pipe, Stream, io, text}

import java.nio.file.{Path, Paths}
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import fs2.io.file.Files

trait DebitRecordDecoder {

  implicit val debitRecordDecoder: Decoder[DebitRecord]

  def debitRecorderDecoder(path: Path): Decoder[DebitRecord] =
    path.toString match {
      case s if s.endsWith(".csv") => CSV.debitRecordDecoder
      case s if s.endsWith(".flr") => FLR.debitRecordDecoder
      case s => sys.error(s"Unsupported file extension: '${s.reverse.takeWhile(_ != '.').reverse}'")
    }
}

object Main extends App with DebitRecordDecoder {

  import cats.effect.unsafe.implicits.global

  import Decoder._
  import Html._
  import DebitLineHtml._

  val input: Path =
    if (args.isEmpty) Paths.get("data.csv")
    else Paths.get(args.head)

  val output: Path =
    input.resolveSibling(input.getFileName.toString + ".html")

  def streamErrorsToStdOut[F[_]: Async]: Pipe[F, Decoded[DebitRecord], DebitRecord] =
    stream => stream.flatMap {
      case Valid(record)   => Stream.emit(record)
      case Invalid(errors) => Stream.eval_(implicitly[Sync[F]].delay(errors.map(println)))
    }

  def emit[F[_]: Async](string: String): Stream[F, Byte] =
    Stream.emit(string).through(text.utf8.encode)

  implicit val debitRecordDecoder: Decoder[DebitRecord] =
    debitRecorderDecoder(input)

  def records[F[_]: Async]: Stream[F, Byte] = {
    io.file.readAll[F](input, 4096)
      .through(text.utf8.decode)
      .through(text.lines)
      .map(_.as[DebitRecord])
      .through(streamErrorsToStdOut)
      .map(_.render)
      .through(text.utf8.encode)
  }

  def html[F[_]: Async]: Stream[F, Byte] =
    emit(header) ++ records ++ emit(footer)

  html[IO]
    .through(io.file.writeAll(output))
    .compile
    .drain
    .unsafeRunSync()
}
