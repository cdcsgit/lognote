package com.blogspot.kotlinstudy.lognote

class BookmarkEvent(change:Int) {
    val mBookmarkChange = change
    companion object {
        const val ADDED = 0
        const val REMOVED = 1
    }
}

interface BookmarkEventListener {
    fun bookmarkChanged(event:BookmarkEvent?)
}

class BookmarkManager private constructor(){
    companion object {
        private val mInstance: BookmarkManager = BookmarkManager()

        fun getInstance(): BookmarkManager {
            return mInstance
        }
    }

    var mBookmarks = mutableListOf<Int>()
    private val mEventListeners = ArrayList<BookmarkEventListener>()

    fun addBookmarkEventListener(listener:BookmarkEventListener) {
        mEventListeners.add(listener)
    }

    fun isBookmark(bookmark:Int): Boolean {
        return mBookmarks.contains(bookmark)
    }

    fun updateBookmark(bookmark:Int) {
        if (mBookmarks.contains(bookmark)) {
            removeBookmark(bookmark)
        } else {
            addBookmark(bookmark)
        }
    }

    fun addBookmark(bookmark:Int) {
        if (mBookmarks.contains(bookmark)) {
            println("addBookmark : already added - $bookmark")
            return
        }
        mBookmarks.add(bookmark)
        mBookmarks.sort()

        for (listener in mEventListeners) {
            listener.bookmarkChanged(BookmarkEvent(BookmarkEvent.ADDED))
        }
    }

    fun removeBookmark(bookmark:Int) {
        if (!mBookmarks.contains(bookmark)) {
            println("addBookmark : already removed - $bookmark")
            return
        }
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