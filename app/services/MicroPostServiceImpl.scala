package services

import javax.inject.Singleton
import models.{Favorite, MicroPost, PagedItems, UserFollow}
import scalikejdbc._
import skinny.Pagination

import scala.util.Try

@Singleton
class MicroPostServiceImpl extends MicroPostService {

  override def create(microPost: MicroPost)(implicit dbSession: DBSession): Try[Long] = Try {
    MicroPost.create(microPost)
  }

  override def deleteById(microPostId: Long)(implicit dbSession: DBSession): Try[Int] = Try {
    MicroPost.deleteById(microPostId)
  }

  override def findByUserId(pagination: Pagination, userId: Long)(
      implicit dbSession: DBSession
  ): Try[PagedItems[MicroPost]] =
    countBy(userId).map { size =>
      PagedItems(pagination, size, findAllByFollowWithLimitOffset(Seq(userId))(pagination))
    }

  override def countBy(userId: Long)(implicit dbSession: DBSession): Try[Long] = Try {
    MicroPost.countBy(sqls.eq(MicroPost.defaultAlias.userId, userId))
  }

  override def findAllByFollowWithLimitOffset(pagination: Pagination, userId: Long)(
      implicit dbSession: DBSession
  ): Try[PagedItems[MicroPost]] = Try {
    val followingIds: Seq[Long] =
      UserFollow.findAllBy(sqls.eq(UserFollow.defaultAlias.userId, userId)).map(_.followId)
    val size = MicroPost.countBy(sqls.in(MicroPost.defaultAlias.userId, userId +: followingIds))
    PagedItems(pagination, size, findAllByFollowWithLimitOffset(userId +: followingIds)(pagination))
  }

  private def findAllByFollowWithLimitOffset(userIds: Seq[Long])(pagination: Pagination)(
      implicit dbSession: DBSession
  ): Seq[MicroPost] = MicroPost.findAllByWithLimitOffset(
    sqls.in(MicroPost.defaultAlias.userId, userIds),
    pagination.limit,
    pagination.offset,
    Seq(MicroPost.defaultAlias.id.desc)
  )

  override def findAllByFavoriteWithLimitOffset(pagination: Pagination, userId: Long)(
      implicit dbSession: DBSession
  ): Try[PagedItems[MicroPost]] = Try {
    val favoriteMicroPostIds: Seq[Long] =
      Favorite.findAllBy(sqls.eq(Favorite.defaultAlias.userId, userId)).map(_.microPostId)
    val size = MicroPost.countBy(
      sqls.in(MicroPost.defaultAlias.id, favoriteMicroPostIds))
    PagedItems(
      pagination, size, findAllByFavoriteWithLimitOffset(favoriteMicroPostIds)(pagination))
  }

  private def findAllByFavoriteWithLimitOffset(
      favoriteMicroPostId: Seq[Long])(pagination: Pagination)(
      implicit dbSession: DBSession
  ): Seq[MicroPost] = MicroPost.findAllByWithLimitOffset(
    sqls.in(MicroPost.defaultAlias.id, favoriteMicroPostId),
    pagination.limit,
    pagination.offset,
    Seq(MicroPost.defaultAlias.id.desc)
  )

}
