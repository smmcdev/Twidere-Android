/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.loader

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.support.annotation.WorkerThread
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.InternalTwitterContentUtils

class UserFavoritesLoader(
        context: Context,
        accountKey: UserKey?,
        private val userKey: UserKey?,
        private val screenName: String?,
        sinceId: String?,
        maxId: String?,
        page: Int,
        data: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        loadingMore: Boolean
) : RequestStatusesLoader(context, accountKey, sinceId, maxId, page, data, savedStatusesArgs,
        tabPosition, fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): List<ParcelableStatus> {
        return getMicroBlogStatuses(account, paging).map {
            it.toParcelable(account.key, account.type, profileImageSize)
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        return InternalTwitterContentUtils.isFiltered(database, status, false)
    }

    override fun processPaging(details: AccountDetails, loadItemLimit: Int, paging: Paging) {
        when (details.type) {
            AccountType.FANFOU -> {
                paging.setCount(loadItemLimit)
                if (page > 0) {
                    paging.setPage(page)
                }
            }
            else -> {
                super.processPaging(details, loadItemLimit, paging)
            }
        }
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): ResponseList<Status> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        if (userKey != null) {
            return microBlog.getFavorites(userKey.id, paging)
        } else if (screenName != null) {
            return microBlog.getFavoritesByScreenName(screenName, paging)
        }
        throw MicroBlogException("Null user")
    }
}
