package com.app.splitter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.app.splitter.data.local.entity.ActivityLogEntity
import com.app.splitter.data.local.entity.ExpenseEntity
import com.app.splitter.data.local.entity.ExpensePayerEntity
import com.app.splitter.data.local.entity.ExpenseSplitEntity
import com.app.splitter.data.local.entity.FriendEntity
import com.app.splitter.data.local.entity.GroupEntity
import com.app.splitter.data.local.entity.GroupMemberEntity
import com.app.splitter.data.local.entity.UserEntity
import com.app.splitter.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserOneShot(): UserEntity?

    @Query("UPDATE users SET isCurrentUser = (id = :userId)")
    suspend fun setCurrentUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("SELECT u.* FROM users u INNER JOIN friends f ON u.id = f.userId")
    fun getFriends(): Flow<List<UserEntity>>

    @Query("SELECT u.* FROM users u INNER JOIN friends f ON u.id = f.userId")
    suspend fun getFriendsList(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE userId = :userId")
    suspend fun deleteFriend(userId: String)
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY createdDate DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembers(members: List<GroupMemberEntity>)

    @Query("SELECT u.* FROM users u INNER JOIN group_members gm ON u.id = gm.userId WHERE gm.groupId = :groupId")
    fun getGroupMembers(groupId: String): Flow<List<UserEntity>>

    @Query("SELECT u.* FROM users u INNER JOIN group_members gm ON u.id = gm.userId WHERE gm.groupId = :groupId")
    suspend fun getGroupMembersList(groupId: String): List<UserEntity>

    @Query("SELECT groupId FROM group_members WHERE userId = :userId")
    suspend fun getGroupIdsForUser(userId: String): List<String>

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)

    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteGroupMembers(groupId: String)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpensesList(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY date DESC")
    fun getGroupExpenses(groupId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY date DESC")
    suspend fun getGroupExpensesList(groupId: String): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpensePayers(payers: List<ExpensePayerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseSplits(splits: List<ExpenseSplitEntity>)

    @Query("SELECT * FROM expense_payers WHERE expenseId = :expenseId")
    suspend fun getExpensePayers(expenseId: String): List<ExpensePayerEntity>

    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun getExpenseSplits(expenseId: String): List<ExpenseSplitEntity>

    @Query("SELECT * FROM expense_payers WHERE expenseId IN (:expenseIds)")
    suspend fun getPayersForExpenses(expenseIds: List<String>): List<ExpensePayerEntity>

    @Query("SELECT * FROM expense_splits WHERE expenseId IN (:expenseIds)")
    suspend fun getSplitsForExpenses(expenseIds: List<String>): List<ExpenseSplitEntity>

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: String)

    @Query("DELETE FROM expense_payers WHERE expenseId = :expenseId")
    suspend fun deleteExpensePayers(expenseId: String)

    @Query("DELETE FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun deleteExpenseSplits(expenseId: String)

    @Transaction
    suspend fun deleteExpenseComplete(expenseId: String) {
        deleteExpensePayers(expenseId)
        deleteExpenseSplits(expenseId)
        deleteExpense(expenseId)
    }
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    suspend fun getAllActivitiesList(): List<ActivityLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityLogEntity)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAllActivities()
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsOneShot(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserSettings(settings: UserSettingsEntity)
}
