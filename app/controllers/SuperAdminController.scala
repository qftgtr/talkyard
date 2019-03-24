/**
 * Copyright (c) 2016 Kaj Magnus Lindberg
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package controllers

import com.debiki.core._
import debiki.SiteTpi
import debiki.EdHttp._
import ed.server.{EdContext, EdController}
import javax.inject.Inject
import play.{api => p}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.ControllerComponents
import talkyard.server.JsX._


class SuperAdminController @Inject()(cc: ControllerComponents, edContext: EdContext)
  extends EdController(cc, edContext) {

  import context.globals

  def redirect = GetAction { apiReq =>
    Redirect(routes.SuperAdminController.superAdminApp("").url)
  }


  def superAdminApp(clientRoute: String) = SuperAdminGetAction { apiReq =>
    _root_.controllers.dieIfAssetsMissingIfDevTest()
    val siteTpi = SiteTpi(apiReq)
    val pageBody = views.html.adminPage(siteTpi, appId = "theSuperAdminApp").body
    Ok(pageBody) as HTML
  }


  // (Rename to getDashboardData() ?)
  def listSites() = SuperAdminGetAction { request =>
    listSitesImpl()
  }


  def updateSites() = SuperAdminPostJsonAction(maxBytes = 10*1000) { request =>
    val jsObjs = request.body.as[Seq[JsObject]]
    val siteData = jsObjs.map(jsObj => {
      val siteId = (jsObj \ "id").as[SiteId]
      val newStatusInt = (jsObj \ "status").as[Int]
      val newStatus = SiteStatus.fromInt(newStatusInt) getOrElse {
        throwBadRequest("EsE402KU2", s"Bad status: $newStatusInt")
      }
      (siteId, newStatus)
    })
    globals.systemDao.updateSites(siteData)
    listSitesImpl()
  }


  private def listSitesImpl(): p.mvc.Result = {
    // The most recent first.
    val sites: Seq[Site] = globals.systemDao.loadSites().sortBy(-_.createdAt.toUnixMillis)
    OkSafeJson(Json.obj(
      "appVersion" -> globals.applicationVersion,
      "superadmin" -> Json.obj(
        "firstSiteHostname" -> JsStringOrNull(globals.defaultSiteHostname),
        "baseDomain" -> globals.baseDomainWithPort,
        "sites" -> sites.map(siteToJson))))
  }


  private def siteToJson(site: Site) = {
    Json.obj(
      "id" -> site.id,
      "pubId" -> site.pubId,
      "status" -> site.status.toInt,
      "canonicalHostname" -> JsStringOrNull(site.canonicalHostname.map(_.hostname)),
      "name" -> site.name,
      "createdAtMs" -> site.createdAt.toUnixMillis)
  }

}

