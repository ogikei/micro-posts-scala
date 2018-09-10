package controllers

import javax.inject.{Inject, Singleton}
import jp.t2v.lab.play2.auth.AuthenticationElement
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.{FavoriteService, MicroPostService, UserFollowService, UserService}
import skinny.Pagination

@Singleton
class FavoritesController @Inject()(val userService: UserService,
    val microPostService: MicroPostService,
    val userFollowService: UserFollowService,
    val favoriteService: FavoriteService,
    components: ControllerComponents)
    extends AbstractController(components)
        with I18nSupport
        with AuthConfigSupport
        with AuthenticationElement {

  def index(page: Int): Action[AnyContent] = StackAction { implicit request =>
    val favoriteIds = if (loggedIn.id.isDefined) {
      favoriteService.findByUserId(loggedIn.id.get).get.map(_.microPostId)
    } else Nil
    microPostService.findAllByFavoriteWithLimitOffset(
      Pagination(pageSize = 10, pageNo = page), loggedIn.id.get)
        .map { microPosts =>
          Ok(views.html.favorite.index(loggedIn, microPosts, favoriteIds))
        }
        .recover {
          case e: Exception =>
            Logger.error(s"occurred error", e)
            Redirect(routes.UsersController.index())
                .flashing("failure" -> Messages("InternaError"))
        }
        .getOrElse(InternalServerError(Messages("InternaError")))
  }

}
