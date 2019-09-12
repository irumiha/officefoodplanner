package com.officefoodplanner.domain

import cats.{Applicative, Functor}
import cats.data.EitherT


trait DomainError extends Exception with Product with Serializable

trait ErrorT[E <: DomainError] {

  implicit class feitherToEitherT[F[_], A](r: F[Either[E,A]]) {
    def wrapT: EitherT[F, E, A] = EitherT(r)
  }
  implicit class foptionToEitherT[F[_]: Functor, A](o: F[Option[A]]) {
    // Make a LeftF containing the error E if the option is empty
    def orError(e: E): EitherT[F, E, A] = EitherT.fromOptionF(o, e)

    // If the option is NOT empty make a LeftF with the contents of the option
    def liftError: EitherT[F, A, Unit] = EitherT(Functor[F].map(o)(_.fold[Either[A, Unit]](Right(()))(Left(_))))

    // Make a LeftF containing the error E if the option is FULL (inverse of orError)
    def errorIfFull(e: E): EitherT[F, E, Unit] = EitherT(
      Functor[F].map(o) {
        case None => Right(())
        case Some(_) => Left(e)
      }
    )
    def errorIfFound(e: E): EitherT[F, E, Unit] = errorIfFull(e)
  }

  implicit class fanyToRightT[F[_]: Applicative, A](a: F[A]) {
    def rightF: EitherT[F, E, A] = EitherT.right(a)
  }

}
