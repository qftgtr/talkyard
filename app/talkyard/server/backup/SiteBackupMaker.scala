/**
 * Copyright (c) 2015-2019 Kaj Magnus Lindberg
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

package talkyard.server.backup

import com.debiki.core._
import com.debiki.core.Prelude._
import ed.server._
import play.api.libs.json.{JsObject, JsValue}
import scala.collection.mutable
import talkyard.server.JsX._



/** Creates json and .tar individual site backup files.
  *
  * Search for [readlater] for stuff ignored right now.
  */
case class SiteBackupMaker(context: EdContext) {

  import context.globals

  def createPostgresqlJsonBackup(siteId: SiteId): JsValue = {
    val fields = mutable.HashMap.empty[String, JsValue]
    globals.siteDao(siteId).readOnlyTransaction { tx =>
      val site: Site = tx.loadSite().getOrDie("TyE2RKKP85")
      //fields("site") = JsSiteInclDetails(site)
    }
    JsObject(fields.toSeq)
  }

}

