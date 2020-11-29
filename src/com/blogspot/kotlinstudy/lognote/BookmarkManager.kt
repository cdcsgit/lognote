package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import java.util.ArrayList
import javax.swing.JComponent

class BookmarkEvent(change:Int) {
    val mBookmarkChange = change
    companion object {
        val ADDED = 0
        val REMOVED = 1
    }
}

interface BookmarkEventListener {
    fun bookmarkChanged(event:BookmarkEvent?)
}

class BookmarkManager private constructor(){
    val mBackground = Color(0xD0, 0xD0, 0xFF)
    companion object {
        private val mInstance: BookmarkManager = BookmarkManager()

        fun getInstance(): BookmarkManager {
            return mInstance
        }
    }

    val mBookmarks = ArrayList<Int>()
    val mEventListeners = ArrayList<BookmarkEventListener>()

    fun addBookmarkEventListener(listener:BookmarkEventListener) {
        mEventListeners.add(listener)
    }

    fun updateBookmark(bookmark:Int) {
        if (mBookmarks.contains(bookmark)) {
            removeBookmark(bookmark)
        } else {
            addBookmark(bookmark)
        }
    }

    private fun addBookmark(bookmark:Int) {
        mBookmarks.add(bookmark)
        mBookmarks.sort()

        for (listener in mEventListeners) {
            listener.bookmarkChanged(BookmarkEvent(BookmarkEvent.ADDED))
        }
    }

    private fun removeBookmark(bookmark:Int) {
        mBookmarks.remove(bookmark)

        for (listener in mEventListeners) {
            listener.bookmarkChanged(BookmarkEvent(BookmarkEvent.REMOVED))
        }
    }

    fun clear() {
        mBookmarks.clear()

        for (listener in mEventListeners) {
            listener.bookmarkChanged(BookmarkEvent(BookmarkEvent.REMOVED))
        }
    }
}