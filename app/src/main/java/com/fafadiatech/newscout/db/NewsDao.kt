package com.fafadiatech.newscout.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.fafadiatech.newscout.db.dailydigest.DDArticleMediaEntity
import com.fafadiatech.newscout.db.dailydigest.DDHashTagEntity
import com.fafadiatech.newscout.db.dailydigest.DailyDigestEntity
import com.fafadiatech.newscout.db.trending.TrendingData
import com.fafadiatech.newscout.db.trending.TrendingNewsEntity
import com.fafadiatech.newscout.model.*

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNews(news: ArrayList<NewsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewsEntity(news: NewsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearchNews(news: ArrayList<SearchDataEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecommendedNews(news: ArrayList<RecommendedDataEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategory(news: ArrayList<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHashTagList(list: ArrayList<HashTagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHashTagEntity(list: HashTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArticleMediaList(list: ArrayList<ArticleMediaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArticleMediaEntity(list: ArticleMediaEntity)

    @Query("SELECT * FROM ArticlesData")
    fun getNewsFromDb(): LiveData<List<NewsEntity>>

    @Query("SELECT category_name FROM CategoryData")
    fun getCategoryListFromDb(): LiveData<List<String>>

    @Query("SELECT ArticlesData.title FROM ArticlesData WHERE title Like :searchQuery")
    fun getTitleBySearch(searchQuery: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLike(likeEntity: LikeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLikeServerData(list: ArrayList<LikeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmark(bookmarkEntity: BookmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmarkServerData(list: ArrayList<BookmarkEntity>)

    @Query("SELECT  a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM ArticlesData a LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id ORDER BY a.published_on DESC")
    fun getDetailNewsFromDb(): LiveData<List<DetailNewsData>>

    @Query("SELECT * FROM LikeData")
    fun getLikeDataFromDb(): LiveData<List<LikeEntity>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT  a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM SearchData a LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id ORDER BY a.published_on DESC")
    fun getDetailSearchNewsFromDb(): List<DetailNewsData>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT  a.* FROM SearchData a ORDER BY datetime(a.published_on) DESC, a.article_score ASC")
    fun getSearchNewsFromDb(): List<NewsEntity>

    @Query("DELETE FROM SearchData")
    fun deleteSearchTableData()

    @Query("DELETE FROM RecommendedData")
    fun deleteRecommendedTableData()

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT  a.*,COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM RecommendedData a LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id ORDER BY a.published_on DESC")
    fun getRecommendedNewsFromDb(): LiveData<List<DetailNewsData>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(c.is_like, '2') AS like_status,b.status AS bookmark_status FROM BookmarkData  b LEFT JOIN ArticlesData a ON a.article_id=b.article_id LEFT JOIN LikeData c ON c.article_id=b.article_id WHERE a.article_id IS NOT NULL AND b.status=1 ORDER BY a.published_on DESC")
    fun getbookmarkedNewsFromDb(): LiveData<List<DetailNewsData>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(c.is_like, '2') AS like_status,b.status AS bookmark_status FROM BookmarkData  b LEFT JOIN SearchData a ON a.article_id=b.article_id LEFT JOIN LikeData c ON c.article_id=b.article_id WHERE a.article_id IS NOT NULL AND b.status=1 ORDER BY a.published_on DESC")
    fun getbookmarkedNewsSearchFromDb(): List<DetailNewsData>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(c.is_like, '2') AS like_status,b.status AS bookmark_status FROM BookmarkData  b LEFT JOIN ArticlesData a ON a.article_id=b.article_id LEFT JOIN LikeData c ON c.article_id=b.article_id WHERE a.article_id IS NOT NULL AND b.status=1 ORDER BY a.published_on DESC")
    fun getbookmarkNewsFromDb(): List<DetailNewsData>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT  a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM ArticlesData a  LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id WHERE a.article_id IN (SELECT HashTagData.article_id FROM HashTagData WHERE HashTagData.name IN (:tags)) ORDER BY a.published_on DESC")
    fun getDetailNewsByCategory(tags: Array<String?>): LiveData<List<DetailNewsData>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM ArticlesData a LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id WHERE a.category Like:categoryType ORDER BY a.published_on DESC")
    fun getDetailNewsFromDbPaging(categoryType: String): androidx.paging.DataSource.Factory<Int, DetailNewsData>

    @Query("SELECT ArticlesData.* FROM ArticlesData INNER JOIN HashTagData WHERE ArticlesData.article_id = HashTagData.article_id AND HashTagData.name Like:tag ORDER BY ArticlesData.published_on")
    fun getNewsByTagFromDb(tag: String): androidx.paging.DataSource.Factory<Int, NewsEntity>

    @Query("SELECT * FROM ArticlesData WHERE ArticlesData.article_id IN (SELECT HashTagData.article_id FROM HashTagData WHERE HashTagData.name IN (:tags))  ORDER BY ArticlesData.published_on DESC")
    fun getNewsByHashTagFromDb(tags: Array<String?>): androidx.paging.DataSource.Factory<Int, NewsEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.category_id, a.hash_tags, a.article_score, COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM ArticlesData a LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id WHERE a.hash_tags Like :categoryType ORDER BY a.published_on DESC")
    fun getTestNewsByTagFromDb(categoryType: String): androidx.paging.DataSource.Factory<Int, NewsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMenuHeading(menuHeading: HeadingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubMenuHeading(subMenuHeading: SubMenuEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubMenuHashTagData(subMenuHashTag: SubMenuHashTagEntity)

    @Query("SELECT * FROM HeadingData")
    fun getMenuHeadingFromDb(): LiveData<List<MenuHeading>>

    @Query("SELECT * FROM HeadingData")
    fun getMenuHeadingListFromDb(): List<MenuHeading>

    @Query("SELECT SubMenuData.id,SubMenuData.heading_id,SubMenuData.name FROM SubMenuData where SubMenuData.heading_id = :heading_id")
    fun getSubMenuDataFromDb(heading_id: Int): LiveData<List<SubMenuResultData>>

    @Query("SELECT SubMenuData.id,SubMenuData.heading_id,SubMenuData.name FROM SubMenuData where SubMenuData.heading_id = :heading_id")
    fun getSubMenuDataListFromDb(heading_id: Int): List<SubMenuResultData>

    @Query("SELECT SubMenuData.id,SubMenuData.heading_id,SubMenuData.name FROM SubMenuData where SubMenuData.heading_id = :heading_id")
    fun getSubMenuTestDataFromDb(heading_id: Int): List<SubMenuResultData>

    @Query("SELECT SubMenuHashTagData.name FROM SubMenuHashTagData WHERE SubMenuHashTagData.submenu_id = :subMenuId")
    fun getSubMenuTagsFromDb(subMenuId: Int): LiveData<List<String>>

    @Query("SELECT SubMenuHashTagData.name FROM SubMenuHashTagData WHERE SubMenuHashTagData.submenu_id = :subMenuId")
    fun getSubMenuTagsTestFromDb(subMenuId: Int): List<String>

    @Query("SELECT * FROM ArticlesData WHERE ArticlesData.article_id IN (SELECT HashTagData.article_id FROM HashTagData WHERE HashTagData.name IN (:tags))  ORDER BY ArticlesData.published_on DESC")
    fun getNewsByTagTestFromDb(tags: Array<String?>): List<NewsEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM ArticlesData a LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id ORDER BY a.published_on desc LIMIT 25")
    fun getTopFiveArticles(): List<DetailNewsData>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM ArticlesData a LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id ORDER BY a.published_on desc LIMIT 25")
    fun getTopFiveSgstArticles(): DataSource.Factory<Int, DetailNewsData>

    @RawQuery
    fun getResultByRawQuery(query: SupportSQLiteQuery): List<NewsEntity>

    @RawQuery
    fun getDetailNewsByRawQuery(query: SupportSQLiteQuery): List<DetailNewsData>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.article_id,a.title,a.source,a.category,a.source_url,a.cover_image,a.description,a.published_on, a.article_score, COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM ArticlesData a LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id ORDER BY RANDOM() LIMIT 1")
    fun getShuffledNewsFromDb(): List<DetailNewsData>

    @Query("SELECT * FROM ArticlesData WHERE ArticlesData.category_id= :categoryId ORDER BY ArticlesData.published_on DESC")
    fun getNewsByNodeIdFromDb(categoryId: Int): List<NewsEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select a.article_id,a.title,a.source,a.category,a.source_url,a.cover_image,a.description,a.published_on, a.article_score, COALESCE(l.is_like, '2') AS like_status, COALESCE(b.status,'0') AS bookmark_status from ArticlesData as a LEFT JOIN LikeData as l on a.article_id = l.article_id LEFT JOIN BookmarkData as b on a.article_id = b.article_id where a.category_id = :categoryId ORDER BY datetime(a.published_on) DESC, a.article_score ASC")
    fun getDetailNewsByNodeId(categoryId: Int): List<DetailNewsData>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.article_id,a.title,a.source,a.category,a.source_url,a.cover_image,a.description,a.published_on, a.article_score, CASE  WHEN article_id NOTNULL THEN 2  END AS like_status,CASE WHEN article_id NOTNULL THEN 0 END AS bookmark_status FROM ArticlesData a  WHERE a.category_id=:categoryId ORDER BY datetime(a.published_on) DESC, a.article_score ASC")
    fun getDefaultDetailNewsByNodeId(categoryId: Int): List<DetailNewsData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrendingData(trendingList: ArrayList<TrendingEntity>)

    @Query("SELECT * FROM( SELECT TrendingData.cluster_id,TrendingData.count, a.article_id,a.title,a.source,a.category,a.source_url,a.cover_image,a.description,a.published_on, a.category_id FROM TrendingArticlesData as a,TrendingData  WHERE TrendingData.article_id = a.article_id ORDER BY published_on ASC ) as sub GROUP BY cluster_id ORDER BY published_on DESC")
    fun getTrendingDataFromDb(): LiveData<List<TrendingNewsData>>

    @Query("DELETE FROM TrendingData")
    fun deleteTrendingData()

    @Query("SELECT * FROM TrendingArticlesData ta WHERE ta.article_id IN (SELECT TrendingData.article_id  FROM TrendingData WHERE TrendingData.cluster_id=:clusterId) ORDER BY ta.published_on DESC")
    fun getTrendingByClusterId(clusterId: Int): LiveData<List<NewsEntity>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM TrendingArticlesData a LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id WHERE a.article_id IN (SELECT TrendingData.article_id  FROM TrendingData WHERE TrendingData.cluster_id=:clusterId) ORDER BY a.published_on DESC")
    fun getTrendingDetailByClusterId(clusterId: Int): LiveData<List<DetailNewsData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearchQuery(query: SearchSuggestionEntity)

    @Query("DELETE FROM LikeData")
    fun deleteLikeTableData()

    @Query("DELETE FROM BookmarkData")
    fun deleteBookmarkTableData()

    @Query("SELECT ss.`query` FROM SearchSuggestionData ss")
    fun getSearchSuggestionFromDb(): LiveData<List<String>>

    @Query("SELECT id FROM SubMenuData WHERE name LIKE '%' || :fieldName || '%' OR name LIKE '%' || :fieldName1 || '%'")
    fun getLatestNewsId(fieldName: String? = "Uncategorised", fieldName1: String? = "Uncategorized"): List<Int>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT a.* FROM ArticlesData a ORDER BY a.published_on DESC")
    fun getPaggedDetailNewsFromDb(): DataSource.Factory<Int, NewsEntity>

    @Query("SELECT SubMenuData.id,SubMenuData.heading_id,SubMenuData.name FROM SubMenuData")
    fun getAllSubMenuFromDb(): List<SubMenuResultData>

    @Query("SELECT SubMenuHashTagData.name FROM SubMenuHashTagData WHERE SubMenuHashTagData.submenu_id = :subMenuId")
    fun getMenuTagsFromDb(subMenuId: Int): List<String>

    @Query("SELECT * FROM ArticlesData WHERE ArticlesData.category LIKE :categoryName ORDER BY ArticlesData.published_on DESC")
    fun getPagedNewsByNodeIdFromDb(categoryName: String): DataSource.Factory<Int, NewsEntity>

    @Query("SELECT * FROM( SELECT TrendingData.cluster_id,TrendingData.count,a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.category_id FROM ArticlesData as a,TrendingData  WHERE TrendingData.article_id = a.article_id ORDER BY published_on ASC ) as sub GROUP BY cluster_id ORDER BY published_on DESC")
    fun getPagingTrendingDataFromDb(): DataSource.Factory<Int, TrendingNewsData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTNews(news: ArrayList<TrendingNewsEntity>)

    @Query("DELETE FROM TrendingArticlesData")
    fun deleteFromTNews()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrendingAPIData(news: ArrayList<TrendingData>)

    @Transaction
    fun removeTrending(newsData: ArrayList<TrendingNewsEntity>, tEntity: ArrayList<TrendingEntity>) {
        deleteTrendingData()
        deleteFromTNews()

        insertTrendingData(tEntity)
        insertTNews(newsData)
    }

    @Query("SELECT * FROM TrendingAPIData")
    fun getAllTrendingData(): LiveData<List<TrendingData>>

    @Query("SELECT * FROM TrendingArticlesData")
    fun getAllTrendingEntity(): LiveData<List<TrendingNewsEntity>>

    @Query("SELECT * FROM( SELECT TrendingData.cluster_id,TrendingData.count, a.article_id,a.title,a.source,a.category,a.source_url,a.cover_image,a.description,a.published_on, a.category_id FROM TrendingArticlesData as a,TrendingData  WHERE TrendingData.article_id = a.article_id ORDER BY published_on ASC ) as sub GROUP BY cluster_id ORDER BY published_on DESC")
    fun getTrendingDataListFromDb(): List<TrendingNewsData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDDNews(list: ArrayList<DailyDigestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDDHashTagList(list: ArrayList<DDHashTagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDDArticleMediaList(list: ArrayList<DDArticleMediaEntity>)

    @Query("SELECT * FROM Dailydigest ORDER BY published_on DESC")
    fun getPagedNewsByNodeIdFromDb(): DataSource.Factory<Int, DailyDigestEntity>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT  a.article_id, a.title, a.source, a.category, a.source_url, a.cover_image, a.description, a.published_on, a.article_score, COALESCE(b.is_like, '2') AS like_status,COALESCE(c.status,'0') AS bookmark_status FROM Dailydigest a LEFT JOIN LikeData b ON a.article_id=b.article_id LEFT JOIN BookmarkData c ON c.article_id=b.article_id ORDER BY a.published_on DESC")
    fun getDDDetailNewsFromDb(): LiveData<List<DetailNewsData>>

    @Query("DELETE FROM DDArticleMediaData")
    fun deleteDDArticleMediaList()

    @Query("DELETE FROM DDHashTagData")
    fun deleteDDHashTagList()

    @Query("DELETE FROM Dailydigest")
    fun deleteDDNews()

    @Transaction
    fun removeDDNews(newsList: ArrayList<DailyDigestEntity>, htList: ArrayList<DDHashTagEntity>, amList: ArrayList<DDArticleMediaEntity>) {
        deleteDDArticleMediaList()
        deleteDDHashTagList()
        deleteDDNews()

        insertDDNews(newsList)
        insertDDHashTagList(htList)
        insertDDArticleMediaList(amList)
    }

    @Query("SELECT * FROM SubMenuData WHERE name LIKE '%' || :fieldName || '%' OR name LIKE '%' || :fieldName1 || '%'")
    fun getLatestNewsName(fieldName: String? = "Uncategorised", fieldName1: String? = "Uncategorized" ): List<SubMenuEntity>
}