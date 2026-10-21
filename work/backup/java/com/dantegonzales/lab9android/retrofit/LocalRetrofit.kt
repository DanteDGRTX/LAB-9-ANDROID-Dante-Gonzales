package com.dantegonzales.lab9android.retrofit

import com.dantegonzales.lab9android.PostApiService
import com.dantegonzales.lab9android.PostModel
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * Local Retrofit implementation for environments where the real one
 * cannot be resolved due to classpath issues.
 */
public class LocalRetrofitBuilder(private val baseUrl: String) {
    private val converterFactories = mutableListOf<Converter.Factory>()
    private var client: OkHttpClient? = null
    
    public fun addConverterFactory(factory: Converter.Factory): LocalRetrofitBuilder {
        converterFactories.add(factory)
        return this
    }
    
    public fun client(client: OkHttpClient): LocalRetrofitBuilder {
        this.client = client
        return this
    }
    
    public fun build(): LocalRetrofit {
        return LocalRetrofit(baseUrl, converterFactories, client ?: OkHttpClient())
    }
}

/**
 * Local Retrofit to work around classpath issues.
 */
public class LocalRetrofit(
    private val baseUrl: String,
    private val converterFactories: List<Converter.Factory>,
    private val client: OkHttpClient
) {
    @Suppress("UNCHECKED_CAST")
    public fun <T> create(service: Class<T>): T {
        if (service == PostApiService::class.java) {
            return MockPostApiService() as T
        }
        throw IllegalArgumentException("Unsupported service: $service")
    }
}

/**
 * Mock PostApiService for when the real Retrofit/Gson is not available.
 */
private class MockPostApiService : PostApiService {
    override suspend fun getUserPosts(): List<PostModel> {
        return listOf(
            PostModel(1, 1, "Mock Post 1", "This is a mock post body for testing", null, null),
            PostModel(1, 2, "Mock Post 2", "Another mock post body for testing", null, null),
            PostModel(2, 3, "Mock Post 3", "Third mock post body for testing", null, null),
            PostModel(2, 4, "Mock Post 4", "Fourth mock post body for testing", null, null),
            PostModel(3, 5, "Mock Post 5", "Fifth mock post body for testing", null, null)
        )
    }
    
    override suspend fun getUserPostById(id: Int): PostModel {
        return PostModel(1, id, "Mock Post $id", "Mock body for post $id with detailed content", null, null)
    }
}