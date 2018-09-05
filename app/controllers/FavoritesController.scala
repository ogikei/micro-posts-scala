package controllers

import javax.inject._

import jp.t2v.lab.play2.auth.AuthenticationElement
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import skinny.Pagination

import models.User
import services.{FavoriteService, MicroPostService, UserFollowService, UserService}

@Singleton
class FavoritesController @Inject()(val userService: UserService,
    val microPostService: MicroPostService,
    val favoriteService: FavoriteService,
    components: ControllerComponents)
    extends AbstractController(components)
        with I18nSupport
        with AuthConfigSupport
        with AuthenticationElement {

  def show(userId: Long, page: Int) = StackAction { implicit request =>
    val triedUserOpt = userService.findById(userId)
    val triedFavorites = favoriteService.findById(loggedIn.id.get)
    val pagination = Pagination(10, page)
    val triedMicroPosts = microPostService.findByUserId(pagination, userId)
    val triedFollowingsSize = favoriteService.countByUserId(userId)
    (for {
      userOpt <- triedUserOpt
      favorites <- triedFavorites
      microPosts <- triedMicroPosts
      followingsSize <- triedFollowingsSize
    } yield {
      userOpt.map { user =>
        Ok(views.html.favorite.show(loggedIn, user, favorites, microPosts, followingsSize))
      }.get
    }).recover {
      case e: Exception =>
        Logger.error(s"occurred error", e)
        Redirect(routes.UsersController.index())
            .flashing("failure" -> Messages("InternalError"))
    }
        .getOrElse(InternalServerError(Messages("InternalError")))
  }

}
