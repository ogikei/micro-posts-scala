package services

import javax.inject.Singleton
import scala.util.Try

import scalikejdbc._
import skinny.Pagination

import models.{Favorite, MicroPost, PagedItems}

@Singleton
class FavoriteServiceImpl extends FavoriteService {

  override def create(favorite: Favorite)(implicit dbSession: DBSession)
  : Try[Long] = Try {
    Favorite.create(favorite)
  }

  override def findById(userId: Long)(implicit dbSession: DBSession)
  : Try[List[Favorite]] = Try {
    Favorite.where('userId -> userId).apply()
  }

  override def findByMicroPostId(microPostId: Long)(implicit dbSession: DBSession)
  : Try[Option[Favorite]] = Try {
    Favorite.where('microPostId -> microPostId).apply().headOption
  }

  override def findFavoritingByUserId(pagination: Pagination, userId: Long)(
      implicit dbSession: DBSession)
  : Try[PagedItems[MicroPost]] = {
    countByUserId(userId).map { size =>
      PagedItems(pagination, size,
        Favorite.allAssociations
            .findAllByWithLimitOffset(
              sqls.eq(Favorite.defaultAlias.userId, userId),
              pagination.limit,
              pagination.offset,
              Seq(Favorite.defaultAlias.id.desc)
            )
            .map(_.user.get)
      )
    }
  }

  override def countByUserId(userId: Long)(implicit dbSession: DBSession)
  : Try[Long] = Try {
    Favorite.allAssociations.countBy(sqls.eq(Favorite.defaultAlias.userId, userId))
  }

  //  override def findFavoritedByUserId(pagination: Pagination, userId: Long)(
  //      implicit dbSession: DBSession)
  //  : Try[PagedItems[MicroPost]] = {
  //  }

  //  override def countByMicroPostId(userId: Long)(implicit dbSession: DBSession)
  //  : Try[Long] = Try {
  //    Favorite.allAssociations.countBy(sqls.eq(Favorite.defaultAlias.microPostId, microPostId))
  //  }

  override def deleteBy(userId: Long, microPostId: Long)(implicit dbSession: DBSession)
  : Try[Int] = Try {
    val c = Favorite.column
    val count = Favorite.countBy(sqls.eq(c.userId, userId).and.eq(c.microPostId, microPostId))
    if (count == 1) {
      Favorite.deleteBy(
        sqls
            .eq(Favorite.column.userId, userId)
            .and(sqls.eq(Favorite.column.microPostId, microPostId))
      )
    } else 0
  }

}
