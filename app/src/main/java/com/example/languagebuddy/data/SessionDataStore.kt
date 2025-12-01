package com.example.languagebuddy.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.sessionDataStore by preferencesDataStore(name = "session_store")
