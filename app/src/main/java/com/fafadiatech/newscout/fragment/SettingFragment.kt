package com.fafadiatech.newscout.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.*
import com.facebook.login.LoginManager
import com.fafadiatech.newscout.BuildConfig
import com.fafadiatech.newscout.R
import com.fafadiatech.newscout.activity.AboutUsActivity
import com.fafadiatech.newscout.activity.ProfileActivity
import com.fafadiatech.newscout.activity.SignInActivity
import com.fafadiatech.newscout.api.ApiClient
import com.fafadiatech.newscout.api.ApiInterface
import com.fafadiatech.newscout.appconstants.AppConstant
import com.fafadiatech.newscout.db.NewsDao
import com.fafadiatech.newscout.db.NewsDatabase
import com.fafadiatech.newscout.model.VoteArticleData
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingFragment() : PreferenceFragmentCompat() {

    lateinit var themepreference: SharedPreferences
    var nightModeEnable: Boolean = false
    lateinit var fontSize: String
    var themes: Int = R.style.DefaultMedium
    lateinit var fontSizeListPref: ListPreference
    lateinit var apiInterfaceObj: ApiInterface
    val TAG = SettingFragment::class.java.simpleName
    lateinit var preference: Preference
    lateinit var mEditor: SharedPreferences.Editor
    lateinit var versionPreference: Preference
    lateinit var newsDao: NewsDao
    var newsDatabase: NewsDatabase? = null
    lateinit var mGoogleApiClient: GoogleApiClient

    override fun onAttach(context: Context) {
        super.onAttach(context)
        newsDatabase = NewsDatabase.getInstance(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themepreference = this.activity!!.getSharedPreferences(AppConstant.APPPREF, Context.MODE_PRIVATE)
        nightModeEnable = themepreference.getBoolean("night mode enable", false)
        apiInterfaceObj = ApiClient.getClient().create(ApiInterface::class.java)
        newsDao = newsDatabase!!.newsDao()

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = GoogleApiClient.Builder(context!!)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
        mGoogleApiClient.connect()
    }

    fun getSavedTheme(theme: Int): Int {
        return theme
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        themes = themepreference.getInt("theme", R.style.DefaultMedium)
        mEditor = themepreference.edit()
        activity!!.setTheme(getSavedTheme(themes))
        PreferenceManager.getDefaultSharedPreferences(this.activity)
        addPreferencesFromResource(R.xml.setting_preference)
        var versionCode = BuildConfig.VERSION_CODE
        var versionName = BuildConfig.VERSION_NAME
        fontSizeListPref = findPreference<ListPreference>(this.getResources().getString(R.string.key_font_size_pref)) as ListPreference
        preference = findPreference<Preference>("login") as Preference
        versionPreference = findPreference<Preference>("version") as Preference
        versionPreference.title = "Version : " + versionName

        var breakingNewsSwitch = findPreference<SwitchPreferenceCompat>(this.resources.getString(R.string.key_breaking_news))
        var dailyEditionSwitch = findPreference<SwitchPreferenceCompat>(this.resources.getString(R.string.key_daily_edition))
        var personalisedSwitch = findPreference<SwitchPreferenceCompat>(this.resources.getString(R.string.key_personalised))

        var isBreakingNewsEnabled: Boolean
        var isDailyEditionEnabled: Boolean
        var isPersonalisedEnabled: Boolean

        var name = themepreference.getString("text_font_size", "Small")
        fontSizeListPref.title = name.capitalize()
        fontSizeListPref.setSummary("Text Size")

        fontSizeListPref.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
                var fontSize: String = p1 as String
                var editor = themepreference.edit()
                editor.putString("text_font_size", fontSize)
                editor.commit()
                if (nightModeEnable) {
                    chooseNightTheme()
                } else {
                    chooseDefaultTheme()
                }
                mEditor = themepreference.edit()
                mEditor.putInt("theme", themes)

                mEditor.commit()
                activity!!.recreate()
                return true
            }
        }

        breakingNewsSwitch?.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
                if (breakingNewsSwitch!!.isChecked) {
                    isBreakingNewsEnabled = false
                } else {
                    isBreakingNewsEnabled = true
                }
                var editor = themepreference.edit()
                editor.putBoolean("breaking_news_state", isBreakingNewsEnabled)
                editor.apply()
                var deviceId = themepreference.getString("device_token", "")
                var isPersonalised = themepreference.getBoolean("personalised_news_state", false)
                var isDailyEdition = themepreference.getBoolean("daily_edition_news_state", false)

                return true
            }
        }

        dailyEditionSwitch?.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
                if (dailyEditionSwitch!!.isChecked) {
                    isDailyEditionEnabled = false
                } else {
                    isDailyEditionEnabled = true
                }
                var editor = themepreference.edit()
                editor.putBoolean("daily_edition_news_state", isDailyEditionEnabled)
                editor.apply()
                var deviceId = themepreference.getString("device_token", "")
                var isBreakingNews = themepreference.getBoolean("breaking_news_state", false)
                var isPersonalised = themepreference.getBoolean("personalised_news_state", false)

                return true
            }
        }

        personalisedSwitch?.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
                if (personalisedSwitch!!.isChecked) {
                    isPersonalisedEnabled = true
                } else {
                    isPersonalisedEnabled = true
                }
                var editor = themepreference.edit()
                editor.putBoolean("personalised_news_state", isPersonalisedEnabled)
                editor.apply()
                var deviceId = themepreference.getString("device_token", "")
                var isBreakingNews = themepreference.getBoolean("breaking_news_state", false)
                var isDailyEdition = themepreference.getBoolean("daily_edition_news_state", false)

                return true
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun chooseNightTheme() {
        fontSize = themepreference.getString("text_font_size", "medium")
        if (fontSize.equals("small")) {
            themes = R.style.DefaultSmall
        } else if (fontSize.equals("medium")) {
            themes = R.style.DefaultMedium
        } else if (fontSize.equals("large")) {
            themes = R.style.DefaultLarge
        }
    }

    fun chooseDefaultTheme() {
        fontSize = themepreference.getString("text_font_size", "medium")
        if (fontSize.equals("small")) {
            themes = R.style.DefaultSmall
        } else if (fontSize.equals("medium")) {
            themes = R.style.DefaultMedium
        } else if (fontSize.equals("large")) {
            themes = R.style.DefaultLarge
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val key = preference!!.key
        if (key == "login") {
            val token = themepreference.getString("token value", "")
            if (preference.title.equals("Login")) {
                val intent = Intent(activity, SignInActivity::class.java)
                startActivity(intent)
            } else if (preference.summary.contains("Logout")) {
                LoginManager.getInstance().logOut()
                if (mGoogleApiClient != null) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                }
                newsDao.deleteBookmarkTableData()
                newsDao.deleteLikeTableData()
                logOut(token)
                mEditor.putString("token value", "")
                mEditor.commit()
                preference.title = "Login"
                preference.summary = "Tap to Login"
            }

            return true
        } else if (key == "profile") {
            val i = Intent(activity, ProfileActivity::class.java)
            startActivity(i)
            return true
        } else if (key == "share_app") {
            var sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Check my app at play.google/fafadiatech")
            sendIntent.setType("text/plain")
            startActivity(sendIntent)
            return true
        } else if (key == "rate_app") {
            return true
        } else if (key == "about_us") {
            var aboutIntent = Intent(activity, AboutUsActivity::class.java)
            startActivity(aboutIntent)
            return true
        } else return false
    }

    override fun onResume() {
        super.onResume()
        val token = themepreference.getString("token value", "")
        if (token.isNullOrBlank()) {
            preference.title = "Login"
        } else {
            preference.title = "Logout"
        }

        if (preference.title.equals("Login")) {
            preference.title = "Login"
            preference.summary = "Tap to Login"
        } else {
            val emailId = themepreference.getString("login success", "")
            preference.title = emailId
            preference.summary = "Tap to Logout"
        }
    }

    fun logOut(token: String) {
        var call: Call<VoteArticleData> = apiInterfaceObj.logoutByApi(token)
        try {
            call.enqueue(object : Callback<VoteArticleData> {
                override fun onFailure(call: Call<VoteArticleData>, t: Throwable) {

                }

                override fun onResponse(call: Call<VoteArticleData>, response: Response<VoteArticleData>) {
                    var message = response.body()?.body?.Msg
                    if (nightModeEnable == true) {

                    }
                    themepreference.edit().remove("token value").apply()
                    themepreference.edit().putString("login success", "").apply()
                }
            })
        } catch (e: Throwable) {

        }
    }
}