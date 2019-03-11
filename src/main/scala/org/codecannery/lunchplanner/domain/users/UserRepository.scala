package org.codecannery.lunchplanner.domain.users
import org.codecannery.lunchplanner.domain.Repository
import org.codecannery.lunchplanner.domain.users.model.User

trait UserRepository[F[_], K] extends Repository[F[_], User, K] {

  def findByUserName(userName: String): F[Option[User]]

  def deleteByUserName(userName: String): F[Option[User]]

}
