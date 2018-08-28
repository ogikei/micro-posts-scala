package models

import java.time.ZonedDateTime

import scalikejdbc._, jsr310._
import skinny.orm._
import skinny.orm.feature._

case class Favorite(id: Option[Long],
    userId: Long,
    microPostId: Long,
    createAt: ZonedDateTime = ZonedDateTime.now(),
    updateAt: ZonedDateTime = ZonedDateTime.now(),
    user: Option[MicroPost] = None,
    microPost: Option[MicroPost] = None)

object Favorite extends SkinnyCRUDMapper[Favorite] {

  lazy val m1 = MicroPost.createAlias("m1")

  lazy val userRef = belongsToWithAliasAndFkAndJoinCondition[MicroPost](
    right = MicroPost -> m1,
    fk = "userId",
    on = sqls.eq(defaultAlias.userId, m1.userId),
    merge = (fa, f) => fa.copy(user = f)
  )

  lazy val m2 = MicroPost.createAlias("m2")

  lazy val microPostRef = belongsToWithAliasAndFkAndJoinCondition[MicroPost](
    right = MicroPost -> m2,
    fk = "micro_post_id",
    on = sqls.eq(defaultAlias.microPostId, m2.id),
    merge = (fa, f) => fa.copy(microPost = f)
  )

  lazy val allAssociations: CRUDFeatureWithId[Long, Favorite] = joins(userRef, microPostRef)

  override def tableName = "favorites"

  override def defaultAlias: Alias[Favorite] = createAlias("fa")

  override def extract(rs: WrappedResultSet, n: ResultName[Favorite]): Favorite =
    autoConstruct(rs, n, "user", "microPost")

  def create(favorite: Favorite)(implicit session: DBSession): Long =
    createWithAttributes(toNamedValues(favorite): _*)

  private def toNamedValues(record: Favorite): Seq[(Symbol, Any)] = Seq(
    'userId -> record.userId,
    'microPostId -> record.microPostId,
    'createAt -> record.createAt,
    'updateAt -> record.updateAt
  )

  def update(favorite: Favorite)(implicit session: DBSession): Int =
    updateById(favorite.id.get).withAttributes(toNamedValues(favorite): _*)

}
