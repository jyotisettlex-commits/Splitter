package com.app.splitter.di

import android.content.Context
import androidx.room.Room
import com.app.splitter.data.local.AppDatabase
import com.app.splitter.data.local.dao.ActivityDao
import com.app.splitter.data.local.dao.ExpenseDao
import com.app.splitter.data.local.dao.GroupDao
import com.app.splitter.data.local.dao.UserDao
import com.app.splitter.data.local.dao.UserSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "splitter_db"
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()

    @Provides
    fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideActivityDao(db: AppDatabase): ActivityDao = db.activityDao()

    @Provides
    fun provideUserSettingsDao(db: AppDatabase): UserSettingsDao = db.userSettingsDao()
}
