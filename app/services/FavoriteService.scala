package services

import scala.util.Try
import scalikejdbc.{AutoSession, DBSession}
import skinny.Pagination

import models._

trait FavoriteService {

  def create(favorite: Favorite)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def findById(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[Favorite]]

  def findByMicroPostId(microPostId: Long)(implicit dbSession: DBSession = AutoSession
  ): Try[Option[Favorite]]

  def findFavoritingByUserId(pagination: Pagination, userId: Long)(
      implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[MicroPost]]

  def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  //  def findFavoritedByUserId(pagination: Pagination, userId: Long)(
  //      implicit dbSession: DBSession = AutoSession
  //  ): Try[PagedItems[MicroPost]]

  //  def countByMicroPostId(microPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def deleteBy(userId: Long, microPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Int]

}
