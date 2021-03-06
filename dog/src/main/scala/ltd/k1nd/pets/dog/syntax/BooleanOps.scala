package ltd.k1nd.pets.dog.syntax

import cats.syntax.either._
import cats.{Applicative, ApplicativeError}
import ltd.k1nd.pets.dog.syntax.BooleanOps.BooleanSyntax

trait BooleanOps {
  implicit def createBooleanSyntax(b: Boolean): BooleanSyntax =
    new BooleanSyntax(b)
}

object BooleanOps {
  implicit final class BooleanSyntax(val b: Boolean) extends AnyVal {
    def toApplicativeError[F[_]]: PartiallyAppliedErrCtx[F] =
      new PartiallyAppliedErrCtx[F](b)
    def ifElseThen[T](ifFalse: => T, ifTrue: => T): T =
      if (b) ifTrue else ifFalse
    def whenA[F[_]: Applicative, A](f: => F[A]): F[Unit] =
      Applicative[F].whenA(b)(f)
    def toEither[L, R](l: => L, r: => R): Either[L, R] =
      ifElseThen(l.asLeft, r.asRight)
  }

  protected class PartiallyAppliedErrCtx[F[_]](val b: Boolean) extends AnyVal {
    def apply[L, R](l: => L, r: => R = ())(
        implicit appErr: ApplicativeError[F, L]): F[R] =
      new BooleanSyntax(b).ifElseThen(appErr.raiseError[R](l), appErr.pure(r))
  }
}
