package controllers

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}

import jp.t2v.lab.play2.auth.AuthenticationElement
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._

import models.Favorite
import services.{FavoriteService, UserService}

@Singleton
class FavoriteController @Inject()(val favoriteService: FavoriteService,
    val userService: UserService,
    components: ControllerComponents)
    extends AbstractController(components)
        with I18nSupport
        with AuthConfigSupport
        with AuthenticationElement {

  def favorite(microPostId: Long): Action[AnyContent] = StackAction { implicit request =>
    val currentUser = loggedIn
    val now = ZonedDateTime.now()
    val favorite = Favorite(None, currentUser.id.get, microPostId, now, now)
    favoriteService
        .create(favorite)
        .map { _ =>
          Redirect(routes.FavoritesController.index())
        }
        .recover {
          case e: Exception =>
            Logger.error("occurred error", e)
            Redirect(routes.HomeController.index())
                .flashing("failure" -> Messages("InternalError"))
        }
        .getOrElse(InternalServerError(Messages("InternalError")))
  }

  def unFavorite(microPostId: Long): Action[AnyContent] = StackAction { implicit request =>
    val currentUser = loggedIn
    favoriteService
        .deleteBy(currentUser.id.get, microPostId)
        .map { _ =>
          Redirect(routes.FavoritesController.index())
        }
        .recover {
          case e: Exception =>
            Logger.error("occurred error", e)
            Redirect(routes.HomeController.index())
                .flashing("failure" -> Messages("InternalError"))
        }
        .getOrElse(InternalServerError(Messages("InternalError")))
  }

}
