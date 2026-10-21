package com.dantegonzales.lab9android.retrofit

import retrofit2.Converter
import retrofit2.Retrofit

/**
 * Local GsonConverterFactory - simple stub that works for compilation.
 * In Android Studio with proper JDK, the real GsonConverterFactory from retrofit2 will be used.
 */
public class LocalGsonConverterFactory : Converter.Factory() {
    companion object {
        public fun create(): LocalGsonConverterFactory {
            return LocalGsonConverterFactory()
        }
        
        public fun create(gson: Any): LocalGsonConverterFactory {
            return LocalGsonConverterFactory()
        }
    }
}