package com.app.splitter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.splitter.data.local.dao.ActivityDao
import com.app.splitter.data.local.dao.ExpenseDao
import com.app.splitter.data.local.dao.GroupDao
import com.app.splitter.data.local.dao.UserDao
import com.app.splitter.data.local.dao.UserSettingsDao
import com.app.splitter.data.local.entity.ActivityLogEntity
import com.app.splitter.data.local.entity.ExpenseEntity
import com.app.splitter.data.local.entity.ExpensePayerEntity
import com.app.splitter.data.local.entity.ExpenseSplitEntity
import com.app.splitter.data.local.entity.FriendEntity
import com.app.splitter.data.local.entity.GroupEntity
import com.app.splitter.data.local.entity.GroupMemberEntity
import com.app.splitter.data.local.entity.UserEntity
import com.app.splitter.data.local.entity.UserSettingsEntity

@Database(
    entities = [
        UserEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        FriendEntity::class,
        ExpenseEntity::class,
        ExpensePayerEntity::class,
        ExpenseSplitEntity::class,
        ActivityLogEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun activityDao(): ActivityDao
    abstract fun userSettingsDao(): UserSettingsDao
}
