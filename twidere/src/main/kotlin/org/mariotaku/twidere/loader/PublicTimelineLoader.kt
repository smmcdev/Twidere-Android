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
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.InternalTwitterContentUtils

class PublicTimelineLoader(
        context: Context,
        accountKey: UserKey?,
        sinceId: String?,
        maxId: String?,
        adapterData: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        loadingMore: Boolean
) : RequestStatusesLoader(context, accountKey, sinceId, maxId, -1, adapterData, savedStatusesArgs,
        tabPosition, fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): List<ParcelableStatus> {
        when (account.type) {
            AccountType.MASTODON -> throw MicroBlogException("STUB")
            else -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                return microBlog.getPublicTimeline(paging).map {
                    it.toParcelable(account.key, account.type, profileImageSize = profileImageSize)
                }
            }
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        return InternalTwitterContentUtils.isFiltered(database, status, true)
    }
}
