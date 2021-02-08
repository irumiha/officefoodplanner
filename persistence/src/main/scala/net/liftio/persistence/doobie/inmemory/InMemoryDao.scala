package net.liftio.persistence.doobie.inmemory

import cats._
import cats.implicits._
import com.officefoodplanner.infrastructure.repository.Dao
import shapeless.ops.hlist.ToList
import shapeless.ops.record.Keys
import shapeless.{HList, LabelledGeneric}

import scala.collection.concurrent.TrieMap

object InMemoryDao {
  type Aux[F[_], A0, B0] = Dao[F, A0] { type Key = B0 }

  def make[F[_], E](implicit instance: Dao[F, E]): Aux[F, E, instance.Key] = instance

  object derive {
    def apply[F[_]: Applicative, E, K] = new Partial[F, E, K]

    class Partial[F[_]: Applicative, E, K] {
      def apply[R <: HList, S <: HList](getId: E => K)(implicit
        ev: LabelledGeneric.Aux[E, R],
        ks: Keys.Aux[R, S],
        tl: ToList[S, Symbol],
      ): Aux[F, E, K] =
        new Dao[F, E] {
          override type Key = K
          override val idColumn: String      = ""
          override val columns: List[String] = List()

          private val cache = new TrieMap[Key, E]

          override def get(entityId: Key): F[Option[E]] =
            cache.get(entityId).pure[F]

          override def get(entityIds: List[Key]): F[List[E]] =
            entityIds
              .map(cache.get)
              .collect {
                case Some(e) => e
              }
              .pure[F]

          override def listAll: F[List[E]] =
            cache.values.toList.pure[F]

          override def create(entity: E): F[E] = {
            cache += (getId(entity) -> entity)
            entity.pure[F]
          }

          override def create(entities: List[E]): F[List[E]] =
            entities
              .map { e =>
                cache += (getId(e) -> e)
                e
              }
              .pure[F]

          override def update(entity: E): F[Int] = {
            val key = getId(entity)

            if (cache.contains(key)) {
              cache.update(key, entity)
              1.pure[F]
            } else {
              0.pure[F]
            }
          }

          override def update(entities: List[E]): F[Int] = {
            entities.map(update).sequence.map(_.sum)
          }

          override def deleteById(entityId: Key): F[Int] =
            cache.remove(entityId).toList.length.pure[F]

          override def deleteByIds(entityIds: List[Key]): F[Int] =
            entityIds.map(deleteById).sequence.map(_.sum)

          override def deleteEntity(entity: E): F[Int] = {
            val key = getId(entity)
            deleteById(key)
          }

          override def deleteEntities(entities: List[E]): F[Int] =
            entities.map(deleteEntity).sequence.map(_.sum)
        }
    }

  }

}
