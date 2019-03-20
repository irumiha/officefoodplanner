package org.codecannery.lunchplanner.infrastructure.repository.postgres

import cats.implicits._
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.param.Param

object FragmentsExtra {
  def array[A: Param](fs: List[A]): Fragment =
    fr0"ARRAY[" ++ fs.map(e => fr0"$e").intercalate(fr",") ++ fr0"]"

  def unnestArray[A: Param](fs: List[A]): Fragment =
    fr0"unnest(" ++ array(fs) ++ fr0")"

  def unnestAsKey[A: Param](key: String, fs: List[A]): Fragment =
    unnestArray(fs) ++ fr" as" ++ Fragment.const(key)

  def valuesUnnest[A: Param](fs: List[A]): Fragment =
    fr0"VALUES (" ++ unnestArray(fs) ++ fr0")"

  def valuesUnnest[A1: Param, A2: Param](fs1: List[A1], fs2: List[A2]): Fragment =
    fr0"VALUES (" ++
      unnestArray(fs1) ++ fr0"," ++
      unnestArray(fs2) ++ fr0")"

  def valuesUnnest[A1: Param, A2: Param, A3: Param](
      fs1: List[A1],
      fs2: List[A2],
      fs3: List[A3]): Fragment =
    fr0"VALUES (" ++
      unnestArray(fs1) ++ fr0"," ++
      unnestArray(fs2) ++ fr0"," ++
      unnestArray(fs3) ++ fr0")"

  def valuesUnnest[A1: Param, A2: Param, A3: Param, A4: Param](
      fs1: List[A1],
      fs2: List[A2],
      fs3: List[A3],
      fs4: List[A4]): Fragment =
    fr0"VALUES (" ++
      unnestArray(fs1) ++ fr0"," ++
      unnestArray(fs2) ++ fr0"," ++
      unnestArray(fs3) ++ fr0"," ++
      unnestArray(fs4) ++ fr0")"

  def fromUnnest[A: Param](fs: List[A], key: String): Fragment =
    fr"FROM (select" ++ unnestAsKey(key, fs) ++ fr0")"

  def fromUnnest[A1: Param, A2: Param](
      fs1: List[A1],
      key1: String,
      fs2: List[A2],
      key2: String): Fragment =
    fr"FROM (select" ++
      unnestAsKey(key1, fs1) ++ fr0"," ++
      unnestAsKey(key2, fs2) ++ fr0")"

  def fromUnnest[A1: Param, A2: Param, A3: Param](
      fs1: List[A1],
      key1: String,
      fs2: List[A2],
      key2: String,
      fs3: List[A3],
      key3: String): Fragment =
    fr"FROM (select" ++
      unnestAsKey(key1, fs1) ++ fr0"," ++
      unnestAsKey(key2, fs2) ++ fr0"," ++
      unnestAsKey(key3, fs3) ++ fr0")"

  def fromUnnest[A1: Param, A2: Param, A3: Param, A4: Param](
      fs1: List[A1],
      key1: String,
      fs2: List[A2],
      key2: String,
      fs3: List[A3],
      key3: String,
      fs4: List[A4],
      key4: String): Fragment =
    fr"FROM (select" ++
      unnestAsKey(key1, fs1) ++ fr0"," ++
      unnestAsKey(key2, fs2) ++ fr0"," ++
      unnestAsKey(key3, fs3) ++ fr0"," ++
      unnestAsKey(key4, fs4) ++ fr0")"

}
