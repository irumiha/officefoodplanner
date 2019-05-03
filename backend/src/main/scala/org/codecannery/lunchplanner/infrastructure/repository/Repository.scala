package org.codecannery.lunchplanner.infrastructure.repository

trait WriteRepository[F[_], K, E, P] {
  def create(entity: E)(implicit KE: KeyEntity[E, K]): F[E]

  def create(entities: List[E])(implicit KE: KeyEntity[E, K]): F[List[E]]

  def update(entity: E)(implicit KE: KeyEntity[E, K]): F[Int]

  def update(entities: List[E])(implicit KE: KeyEntity[E, K]): F[Int]

  def deleteById(entityId: K): F[Int]

  def deleteByIds(entityIds: List[K]): F[Int]

  def deleteEntity(entity: E)(implicit KE: KeyEntity[E, K]): F[Int]

  def deleteEntities(entities: List[E])(implicit KE: KeyEntity[E, K]): F[Int]

  def delete(specification: P)(implicit KE: KeyEntity[E, K]): F[List[E]]

}

trait ReadRepository[F[_], K, E] {
  def get(entityId: K): F[Option[E]]

  def get(entityIds: List[K]): F[List[E]]

  def listAll: F[List[E]]

}

trait FindRepository[F[_], K, E, P1] {
  def find(
    specification: P1,
    orderBy: Option[String],
    limit: Option[Int],
    offset: Option[Int]
  ): F[List[E]]
}

trait Repository[F[_], K, E, P] extends ReadRepository[F, K, E] with WriteRepository[F, K, E, P]
