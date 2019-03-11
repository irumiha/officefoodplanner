package org.codecannery.lunchplanner.infrastructure.repository.doobie

import cats._
import cats.data.OptionT
import cats.implicits._
import doobie._
import doobie.implicits._
import io.bfil.automapper._
import org.codecannery.lunchplanner.domain.users.UserRepository
import org.codecannery.lunchplanner.domain.users.command.{CreateUser, UpdateUser}
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.infrastructure.repository.doobie.SQLPagination._

private object UserSQL {
  def insert(user: User): Update0 = sql"""
    INSERT INTO USERS (USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, HASH, PHONE)
    VALUES (${user.userName}, ${user.firstName}, ${user.lastName}, ${user.email}, ${user.hash}, ${user.phone})
  """.update

  def update(user: User, id: Long): Update0 = sql"""
    UPDATE USERS
    SET FIRST_NAME = ${user.firstName}, LAST_NAME = ${user.lastName}, EMAIL = ${user.email}, HASH = ${user.hash}, PHONE = ${user.phone}
    WHERE ID = $id
  """.update

  def select(userId: Long): Query0[User] = sql"""
    SELECT USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, HASH, PHONE, ID
    FROM USERS
    WHERE ID = $userId
  """.query

  def byUserName(userName: String): Query0[User] = sql"""
    SELECT USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, HASH, PHONE, ID
    FROM USERS
    WHERE USER_NAME = $userName
  """.query[User]

  def delete(userId: Long): Update0 = sql"""
    DELETE FROM USERS WHERE ID = $userId
  """.update

  val selectAll: Query0[User] = sql"""
    SELECT USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, HASH, PHONE, ID
    FROM USERS
  """.query
}

class DoobieUserRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F]) extends UserRepository[F] {

  import UserSQL._

  def create(user: CreateUser): F[User] = {
    val userToStore = automap(user).to[User]
    insert(userToStore).withUniqueGeneratedKeys[Long]("ID").map(id => userToStore.copy(id = id.some)).transact(xa)
  }

  def update(user: UpdateUser): F[Option[User]] = OptionT.fromOption[F](user.id).semiflatMap { id =>
    val userToStore = automap(user).to[User]
    UserSQL.update(userToStore, id).run.transact(xa).as(userToStore)
  }.value

  def get(userId: Long): F[Option[User]] = select(userId).option.transact(xa)

  def findByUserName(userName: String): F[Option[User]] = byUserName(userName).option.transact(xa)

  def delete(userId: Long): F[Option[User]] = OptionT(get(userId)).semiflatMap(user =>
    UserSQL.delete(userId).run.transact(xa).as(user)
  ).value

  def deleteByUserName(userName: String): F[Option[User]] =
    OptionT(findByUserName(userName)).mapFilter(_.id).flatMapF(delete).value
}

object DoobieUserRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieUserRepositoryInterpreter[F] =
    new DoobieUserRepositoryInterpreter(xa)
}

