package com.fafadiatech.newscout.api

import com.fafadiatech.newscout.BuildConfig
import com.fafadiatech.newscout.appconstants.TRACKING_URL
import com.fafadiatech.newscout.comments.CaptchaResponseData
import com.fafadiatech.newscout.comments.CommentResponseData
import com.fafadiatech.newscout.model.*
import com.fafadiatech.newscout.model.trending.TrendingDataHeaderApi
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @GET("article/search/" + BuildConfig.DOMAIN_NAME)
    fun getNewsFromTag(@Query("tag", encoded = true) tag: String): Call<NewsDataApi>

    @GET("article/search/" + BuildConfig.DOMAIN_NAME)
    fun searchNewsFromApi(@Query("q", encoded = true) q: String): Call<NewsDataApi>

    @GET("article/search/" + BuildConfig.DOMAIN_NAME)
    fun searchPaginatedNewsFromApi(@Query("q", encoded = true) q: String, @Query("page") page: Int): Call<NewsDataApi>

    @FormUrlEncoded
    @POST("signup/")
    fun signUpByApi(@Field("first_name") first_name: String, @Field("last_name") last_name: String,
                    @Field("email") email: String, @Field("password") password: String): Call<SignUpMessageData>

    @FormUrlEncoded
    @POST("login/")
    fun loginByApi(@Field("email") email: String, @Field("password") password: String, @Field("device_id") device_id: String, @Field("device_name") device_name: String): Call<MessageLoginData>

    @FormUrlEncoded
    @POST("articles/vote/")
    fun voteArticlesByApi(@Header("authorization") token: String, @Field("isLike") isLike: Int, @Field("article_id") article_id: Int): Call<VoteArticleData>

    @FormUrlEncoded
    @POST("articles/bookmark/")
    fun bookmarkArticlesByApi(@Header("authorization") token: String, @Field("article_id") article_id: Int): Call<BookmarkArticleData>

    @FormUrlEncoded
    @POST("change-password/")
    fun changePassword(@Header("authorization") token: String, @Field("password") password: String, @Field("confirm_password") confirm_password: String, @Field("old_password") old_password: String): Call<ChangePasswordData>

    @FormUrlEncoded
    @POST("forgot-password/")
    fun forgotPassword(@Field("email") emal: String): Call<ForgotPasswordData>

    @GET("articles/{id}/recommendations/")
    fun getRecommendedArticles(@Path("id") id: Int): Call<RecommendedDataApi>

    @GET("logout/")
    fun logoutByApi(@Header("authorization") token: String): Call<VoteArticleData>

    @FormUrlEncoded
    @POST("categories/save-remove/")
    fun saveCategory(@Header("authorization") token: String, @Field("category") category: String): Call<VoteArticleData>

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "categories/save-remove/", hasBody = true)
    fun removeCategory(@Header("authorization") token: String, @Field("category") category: String): Call<VoteArticleData>

    @GET("articles/like-news-list/")
    fun getLikedListFromServer(@Header("authorization") token: String): Call<VoteArticleDataServer>

    @GET("bookmark-articles/bookmark-news-list/")
    fun getBookmarkListFromServer(@Header("authorization") token: String): Call<BookmarkArticleDataServer>

    @GET("tags/" + BuildConfig.DOMAIN_NAME)
    fun getDailyTagsFromServer(): Call<ServerTagData>

    @GET("tags/?weekly=1")
    fun getWeeklyTagsFromServer(): Call<ServerTagData>

    @GET("tags/?monthly=1")
    fun getMonthlyTagFromServer(): Call<ServerTagData>

    @GET("menus" + BuildConfig.DOMAIN_NAME)
    fun getMenusFromApi(): Call<CategoryResponseData>

    @GET("article/search/" + BuildConfig.DOMAIN_NAME)
    fun getNewsFromTagByPage(@Query("page") page: Int, @Query("tag", encoded = true) tag: String): Call<NewsDataApi>

    @GET("article/search/" + BuildConfig.DOMAIN_NAME)
    fun getNewsFromNodeIdByPage(@Query("page") page: Int, @Query("category", encoded = false) category: String): Call<NewsDataApi>

    @GET("article/search/" + BuildConfig.DOMAIN_NAME)
    fun getNewsFromSource(@Query("source") source: String, @Query("page") page: Int): Call<NewsDataApi>

    @FormUrlEncoded
    @POST("device/" + BuildConfig.DOMAIN_NAME)
    fun sendDeviceIdToServerWithToken(@Header("Authorization") token: String, @Field("device_id") device_id: String, @Field("device_name") device_name: String): Call<DeviceServerResponseData>

    @FormUrlEncoded
    @POST("device/" + BuildConfig.DOMAIN_NAME)
    fun sendDeviceIdToServerWithoutToken(@Field("device_id") device_id: String, @Field("device_name") device_name: String): Call<DeviceServerResponseData>

    @FormUrlEncoded
    @POST("notification/" + BuildConfig.DOMAIN_NAME)
    fun updateNotification(@Field("device_id") device_id: String, @Field("device_name") device_name: String, @Field("breaking_news") breaking_news: Boolean, @Field("daily_edition") daily_edition: Boolean, @Field("personalized") personalized: Boolean): Call<DeviceServerResponseData>

    @FormUrlEncoded
    @POST("social-login/")
    fun loginBySocial(@Field("provider") provider: String, @Field("device_id") device_id: String, @Field("device_name") device_name: String, @Field("token_id") token_id: String): Call<MessageLoginData>

    @GET("trending" + BuildConfig.DOMAIN_NAME)
    fun getNewsByTrending(): Call<TrendingDataApi>

    @GET(TRACKING_URL)
    fun trackUserSelection(@Query("action") action: String, @Query("device_id") device_id: String, @Query("platform") platform: String, @Query("item_id") item_id: Int, @Query("item_name", encoded = true) item_name: String): Call<Void>

    @GET(TRACKING_URL)
    fun trackUserSearchQuery(@Query("action") action: String, @Query("device_id") device_id: String, @Query("platform") platform: String, @Query("q", encoded = true) item_name: String): Call<Void>

    @GET("trending" + BuildConfig.DOMAIN_NAME)
    fun getNewsByTrendingAPI(): Call<TrendingDataHeaderApi>


    @GET("daily-digest/" + BuildConfig.DOMAIN_NAME)
    fun getDDNewsFromNodeIdByPage(@Query("page") page: Int, @Query("device_id", encoded = true) deviceId: String): Call<NewsDataApi>

    @GET("ads/schedules/" + BuildConfig.DOMAIN_NAME)
    fun getAds(@Query("category") categoryName:String = "") : Call<NewsAdsApi>

    @GET(TRACKING_URL + BuildConfig.DOMAIN_NAME)/*+ BuildConfig.DOMAIN_NAME */
    fun trackApp(@Query("article_id") itemId:Int = 0,
                 @Query("article_title") itemName:String = "",
                 @Query("category_id") categoryId: Int = 0,
                 @Query("category_name") categoryName: String = "",
                 @Query("author_name") authorName:String = "",
                 @Query("action") action:String,
                 @Query("device_id") deviceId:String,
                 @Query("platform") plateform:String = "android",
                 @Query("type") type:String,
                 @Query("sid") sessionId:String,
                 @Query("source_name") sourceName:String,
                 @Query("source_id") sourceId:Int,
                 @Query("cluster_id") clusterId: Int
                ): Call<Void>

    @GET(TRACKING_URL + BuildConfig.DOMAIN_NAME)
    fun signInTrackApp(
                 @Query("action") action:String,
                 @Query("device_id") deviceId:String,
                 @Query("platform") plateform:String = "android",
                 @Query("type") type:String,
                 @Query("sid") sessionId:String,
                 @Query("first_name") firstName:String,
                 @Query("last_name") lastName:String,
                 @Query("token") token: String,
                 @Query("email") email: String
    ): Call<Void>

    @GET("articles/{id}/recommendations/")
    fun getSuggestedArticles(@Path("id") id: Int): Call<NewsDataApi>

    @GET("comment-captcha/")
    fun getCaptchaText(@Header("authorization") token: String): Call<CaptchaResponseData>

    @GET("comment/")
    fun getAllComments(@Header("authorization") token:String, @Query("article_id") articleId:Int, @Query("page") page: Int): Call<CommentResponseData>
}