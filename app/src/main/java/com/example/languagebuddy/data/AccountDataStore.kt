package com.example.languagebuddy.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.accountDataStore by preferencesDataStore(name = "account_store")
