package controllers

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}

import jp.t2v.lab.play2.auth.AuthenticationElement
import models.MicroPost
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services._
import skinny.Pagination

@Singleton
class PostController @Inject()(val userService: UserService,
    val microPostService: MicroPostService,
    val favoriteService: FavoriteService,
    components: ControllerComponents)
    extends AbstractController(components)
        with I18nSupport
        with AuthConfigSupport
        with AuthenticationElement {

  private val postForm = Form {
    "content" -> nonEmptyText
  }

  def post(page: Int): Action[AnyContent] = StackAction { implicit request =>
    val user = loggedIn
    postForm
        .bindFromRequest()
        .fold(
          { formWithErrors =>
            handleError(page, user, formWithErrors)
          }, { content =>
            createMicroPost(page, user, content)
          }
        )
  }

  private def createMicroPost(page: Int,
      user: User,
      content: String
  )(implicit request: RequestHeader) = {
    val now = ZonedDateTime.now
    val microPost = MicroPost(None, user.id.get, content, now, now)
    microPostService
        .create(microPost)
        .map { _ =>
          Redirect(routes.HomeController.index(page))
        }
        .recover {
          case e: Exception =>
            Logger.error("occurred error", e)
            Redirect(routes.HomeController.index())
                .flashing("failure" -> Messages("InternalError"))
        }
        .getOrElse(InternalServerError(Messages("InternalError")))
  }

  private def handleError(page: Int,
      user: User,
      formWithErrors: Form[String]
  )(implicit request: RequestHeader) = {
    val favoriteIds: List[Long] =
      if (user.id.isDefined) {
        favoriteService.findByUserId(user.id.get).get.map(_.microPostId)
      } else Nil
    microPostService
        .findAllByWithLimitOffset(Pagination(10, page), user.id.get)
        .map { pagedItems =>
          BadRequest(views.html.index(Some(user), formWithErrors, pagedItems, favoriteIds))
        }
        .recover {
          case e: Exception =>
            Logger.error("occurred error", e)
            Redirect(routes.HomeController.index())
                .flashing("failure" -> Messages("InternalError"))
        }
        .getOrElse(InternalServerError(Messages("InternalError")))
  }

  def delete(microPostId: Long, page: Int): Action[AnyContent] = StackAction { implicit request =>
    microPostService
        .deleteById(microPostId)
        .map { _ =>
          Redirect(routes.HomeController.index(page))
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
