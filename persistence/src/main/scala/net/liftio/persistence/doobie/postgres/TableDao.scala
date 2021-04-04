package net.liftio.persistence.doobie.postgres

import com.officefoodplanner.infrastructure.repository.{Dao, Table}
import cats.implicits._
import doobie.util.fragments.{in, whereAndOpt}
import doobie._
import doobie.implicits._
import shapeless._
import shapeless.ops.record._
import shapeless.ops.hlist._

object TableDao {
  // https://stackoverflow.com/questions/43900674/understanding-the-aux-pattern-in-scala-type-system
  type Aux[A0, B0] = Dao[ConnectionIO, A0] { type Key = B0 }

  def make[E](instance: Dao[ConnectionIO, E]): Aux[E, instance.Key] = instance

  object derive {
    def apply[E, K] = new Partial[E, K]

    class Partial[E, K] {
      def apply[R <: HList, S <: HList](
        idName: String,
        table: Table,
      )(implicit
        ev: LabelledGeneric.Aux[E, R],
        ra: Read[E],
        wa: Write[E],
        ks: Keys.Aux[R, S],
        tl: ToList[S, Symbol],
        mk: Meta[K],
        wlk: Write[List[K]],
      ): Aux[E, K] =
        new Dao[ConnectionIO, E] {

          override type Key = K
          override val idColumn: String = idName
          val columns: List[String]     = ks.apply.toList.map(s => s.name)

          private val columnsSelect: Fragment = Fragment.const(columnsQuoted)

          private lazy val idColumnQuoted = s""""$idColumn""""
          private val tableNameFragment   = Fragment.const(table.toString)

          override def get(entityId: Key): ConnectionIO[Option[E]] = {
            Query[Key, E](s"""SELECT $columnsQuoted
                FROM $table
                WHERE $idName = ?
              """).option(entityId)
          }

          override def get(entityIds: List[Key]): ConnectionIO[List[E]] = {
            val q =
              fr"SELECT " ++ columnsSelect ++
                fr"  FROM " ++ tableNameFragment ++
                whereAndOpt(entityIds.toNel.map(r => in(Fragment.const(idColumnQuoted), r)))

            q.query[E].stream.compile.toList
          }

          override def listAll: ConnectionIO[List[E]] =
            Query0[E](s"""SELECT ${columns.mkString(", ")}
                FROM $table
              """).stream.compile.toList

          override def create(entity: E): ConnectionIO[E] =
            Query[E, E](s"""INSERT INTO $table ($columnsQuoted)
                VALUES (${columns.as("?").mkString(", ")})
                RETURNING $columnsQuoted
            """).unique(entity)

          override def create(entities: List[E]): ConnectionIO[List[E]] =
            Update[E](s"""INSERT INTO $table ($columnsQuoted)
                VALUES ${columns.as("?").mkString(", ")}
            """)
              .updateManyWithGeneratedKeys[E](columns: _*)(entities)
              .compile
              .toList

          override def update(entity: E): ConnectionIO[Int] = update(List(entity))

          override def update(entities: List[E]): ConnectionIO[Int] = {
            Update[E](s"""UPDATE $table
                SET ${columnsWithoutId.map { c => "\"" + c + "\"" + " = __tmp_update_data." + "\"" + c + "\"" }.mkString(", ")}
                FROM (
                  SELECT (___inner::$table).* from (select ${columns.as("?").mkString(",")}) as ___inner
                ) as __tmp_update_data
                WHERE $table.$idColumnQuoted = __tmp_update_data.$idColumnQuoted
              """)
              .updateMany(entities)
          }

          override def deleteById(entityId: Key): ConnectionIO[Int] = deleteByIds(List(entityId))

          override def deleteByIds(entityIds: List[Key]): ConnectionIO[Int] = {
            if (entityIds.isEmpty) {
              0.pure[ConnectionIO]
            } else {
              Update[List[Key]](
                s"""DELETE FROM $table where id=ANY(?)""",
              ).run(entityIds)
            }
          }
        }
    }

  }
}
