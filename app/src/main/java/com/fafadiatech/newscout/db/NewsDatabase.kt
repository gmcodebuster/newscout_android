package com.fafadiatech.newscout.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fafadiatech.newscout.db.dailydigest.DDArticleMediaEntity
import com.fafadiatech.newscout.db.dailydigest.DDHashTagEntity
import com.fafadiatech.newscout.db.dailydigest.DailyDigestEntity
import com.fafadiatech.newscout.db.trending.TrendingData
import com.fafadiatech.newscout.db.trending.TrendingNewsEntity
import com.fafadiatech.newscout.typeconverter.ArticleListConverter

@TypeConverters(ArticleListConverter::class)
@Database(entities = arrayOf(NewsEntity::class, CategoryEntity::class, CategorySelectedEntity::class, LikeEntity::class, BookmarkEntity::class, SearchDataEntity::class, RecommendedDataEntity::class, HashTagEntity::class, ArticleMediaEntity::class, HeadingEntity::class, SubMenuEntity::class, SubMenuHashTagEntity::class, TrendingEntity::class, SearchSuggestionEntity::class, TrendingNewsEntity::class, TrendingData::class, DailyDigestEntity::class, DDArticleMediaEntity::class, DDHashTagEntity::class), version = 4)

abstract class NewsDatabase : RoomDatabase() {

    abstract fun newsDao(): NewsDao

    companion object {
        private var INSTANCE: NewsDatabase? = null

        fun getInstance(context: Context): NewsDatabase? {

            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            NewsDatabase::class.java, "newsdata.db").allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            return INSTANCE as NewsDatabase
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    override fun clearAllTables() {

    }
}