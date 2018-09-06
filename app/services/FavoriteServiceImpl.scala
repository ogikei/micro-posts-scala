package services

import scala.util.Try
import scalikejdbc._
import models.{Favorite, PagedItems}
import skinny.Pagination

class FavoriteServiceImpl extends FavoriteService {

  override def create(favorite: Favorite)(implicit dbSession: DBSession): Try[Long] = Try {
    Favorite.create(favorite)
  }

  override def findByUserId(userId: Long)(implicit dbSession: DBSession)
  : Try[List[Favorite]] = Try {
    Favorite.where('userId -> userId).apply()
  }

  override def findByMicroPostId(microPostId: Long)(implicit dbSession: DBSession)
  : Try[List[Favorite]] = Try {
    Favorite.where('microPostId -> microPostId).apply()
  }

  override def countByUserId(userId: Long)(implicit dbSession: DBSession): Try[Long] = Try {
    Favorite.allAssociations.countBy(sqls.eq(Favorite.defaultAlias.userId, userId))
  }

  override def countByMicroPostId(microPostId: Long)(implicit dbSession: DBSession)
  : Try[Long] = Try {
    Favorite.allAssociations.countBy(sqls.eq(Favorite.defaultAlias.microPostId, microPostId))
  }

  override def findAll(pagination: Pagination)(implicit dbSession: DBSession = AutoSession)
  : Try[PagedItems[Favorite]] = Try {
    PagedItems[Favorite](
      pagination,
      Favorite.countAllModels(),
      Favorite.findAllWithPagination(pagination, Seq(Favorite.defaultAlias.id.asc))
    )
  }

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
