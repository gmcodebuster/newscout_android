package com.fafadiatech.newscout.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.application.MyApplication
import com.fafadiatech.newscout.db.*
import com.fafadiatech.newscout.model.CategoryResponseData
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.nio.charset.Charset

class DbInsertMenuWork(context: Context, params: WorkerParameters) : Worker(context, params) {

    var newsDao: NewsDao
    private var newsDatabase: NewsDatabase? = null
    var apiInterface: ApiInterface
    var headingList = ArrayList<String>()
    var subList = ArrayList<String>()
    val fileName: String = "navmenu.json"
    val charset: Charset = Charsets.UTF_8
    lateinit var context: Context

    init {
        this.context = context
        newsDatabase = NewsDatabase.getInstance(context)
        newsDao = newsDatabase!!.newsDao()
        apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
    }

    override fun doWork(): Result {
        try {
            var call: Call<CategoryResponseData> = apiInterface.getMenusFromApi()
            var response: Response<CategoryResponseData> = call.execute()
            var result = response.body()!!.body.results
            val jsonStrRes = Gson().toJson(response.body())

            for (i in 0 until result.size) {
                var category = result.get(i).heading.name
                var categoryId = result.get(i).heading.category_id
                var entity = HeadingEntity(categoryId, category)
                newsDao.insertMenuHeading(entity)
                var subMenu = result.get(i).heading.submenu

                for (j in 0 until subMenu.size) {
                    var subCategory = subMenu.get(j).name
                    var subCategoryId = subMenu.get(j).category_id
                    var subMenuEntity = SubMenuEntity(subCategoryId, categoryId, subCategory)
                    newsDao.insertSubMenuHeading(subMenuEntity)
                    subList.add(subCategory)
                    var tagList = ArrayList<String>()
                    for (k in 0 until subMenu.get(j).hash_tags.size) {
                        var tagName = subMenu.get(j).hash_tags.get(k).name
                        var tagId = subMenu.get(j).hash_tags.get(k).id
                        var tagCount = subMenu.get(j).hash_tags.get(k).count
                        var subMenuHashTagEntity = SubMenuHashTagEntity(tagId, subCategoryId, tagName, tagCount)
                        newsDao.insertSubMenuHashTagData(subMenuHashTagEntity)
                        tagList.add(tagName)
                    }
                    MyApplication.tagDataHashMap.put(subCategory, tagList)
                    MyApplication.categoryIdHashMap.put(subCategory, subCategoryId)
                }
                headingList.add(category)
            }
        } catch (e: Throwable) {
            return Result.failure()
        }
        return Result.success()
    }

    fun writeDataToFile(data: String) {
        try {
            File(fileName).writeText(data, Charset.defaultCharset())
        } catch (e: Exception) {
        }
    }
}