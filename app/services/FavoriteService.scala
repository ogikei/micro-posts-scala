package services

import scala.util.Try
import scalikejdbc.{AutoSession, DBSession}
import models.{Favorite, PagedItems}
import skinny.Pagination

trait FavoriteService {

  def create(favorite: Favorite)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def findByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[Favorite]]

  def findByMicroPostId(microPostId: Long)(implicit dbSession: DBSession = AutoSession)
  : Try[List[Favorite]]

  def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def countByMicroPostId(microPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def findAll(pagination: Pagination)(implicit dbSession: DBSession = AutoSession)
  : Try[PagedItems[Favorite]]

  def deleteBy(userId: Long, microPostId: Long)(implicit dbSession: DBSession = AutoSession)
  : Try[Int]

}
