package com.app.splitter.data.local

import com.app.splitter.data.local.entity.ActivityLogEntity
import com.app.splitter.data.local.entity.ExpenseEntity
import com.app.splitter.data.local.entity.ExpensePayerEntity
import com.app.splitter.data.local.entity.ExpenseSplitEntity
import com.app.splitter.data.local.entity.FriendEntity
import com.app.splitter.data.local.entity.GroupEntity
import com.app.splitter.data.local.entity.GroupMemberEntity
import com.app.splitter.data.local.entity.UserEntity
import com.app.splitter.data.local.entity.UserSettingsEntity
import com.app.splitter.domain.model.ActivityType
import com.app.splitter.domain.model.ExpenseCategory
import com.app.splitter.domain.model.GroupCategory
import com.app.splitter.domain.model.SplitType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataInitializer @Inject constructor(
    private val database: AppDatabase
) {
    suspend fun populateInitialDataIfEmpty() {
        val userDao = database.userDao()
        if (userDao.getCurrentUserOneShot() != null) {
            // Already initialized
            return
        }

        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        // 1. Current User
        val currentUser = UserEntity(
            id = "user_me",
            name = "Alex Johnson",
            email = "alex.johnson@example.com",
            phone = "+1 (555) 019-2831",
            avatarUrl = "",
            isCurrentUser = true
        )

        // 2. Friends / Other Users
        val bob = UserEntity("user_bob", "Bob Smith", "bob.smith@example.com", "+1 (555) 012-3456", "", false)
        val charlie = UserEntity("user_charlie", "Charlie Davis", "charlie.d@example.com", "+1 (555) 098-7654", "", false)
        val diana = UserEntity("user_diana", "Diana Prince", "diana.p@example.com", "+1 (555) 044-3322", "", false)
        val ethan = UserEntity("user_ethan", "Ethan Hunt", "ethan.h@example.com", "+1 (555) 077-8899", "", false)
        val fiona = UserEntity("user_fiona", "Fiona Gallagher", "fiona.g@example.com", "+1 (555) 011-2233", "", false)

        val users = listOf(currentUser, bob, charlie, diana, ethan, fiona)
        userDao.insertUsers(users)

        // Mark as friends
        val friends = listOf(
            FriendEntity(bob.id, now - 30 * dayMs),
            FriendEntity(charlie.id, now - 25 * dayMs),
            FriendEntity(diana.id, now - 20 * dayMs),
            FriendEntity(ethan.id, now - 15 * dayMs),
            FriendEntity(fiona.id, now - 10 * dayMs)
        )
        for (f in friends) {
            userDao.insertFriend(f)
        }

        // 3. Groups
        val groupDao = database.groupDao()

        val groupHomeId = "group_apartment"
        val groupHome = GroupEntity(
            id = groupHomeId,
            name = "Apartment 4B",
            category = GroupCategory.HOME.name,
            createdDate = now - 20 * dayMs,
            createdByUserId = currentUser.id
        )

        val groupTripId = "group_trip"
        val groupTrip = GroupEntity(
            id = groupTripId,
            name = "Road Trip to Vegas",
            category = GroupCategory.TRIP.name,
            createdDate = now - 10 * dayMs,
            createdByUserId = currentUser.id
        )

        val groupBrunchId = "group_brunch"
        val groupBrunch = GroupEntity(
            id = groupBrunchId,
            name = "Weekend Brunch Club",
            category = GroupCategory.EVENT.name,
            createdDate = now - 5 * dayMs,
            createdByUserId = fiona.id
        )

        groupDao.insertGroup(groupHome)
        groupDao.insertGroup(groupTrip)
        groupDao.insertGroup(groupBrunch)

        // Group Members
        val homeMembers = listOf(
            GroupMemberEntity(groupHomeId, currentUser.id),
            GroupMemberEntity(groupHomeId, bob.id),
            GroupMemberEntity(groupHomeId, charlie.id)
        )
        val tripMembers = listOf(
            GroupMemberEntity(groupTripId, currentUser.id),
            GroupMemberEntity(groupTripId, diana.id),
            GroupMemberEntity(groupTripId, ethan.id)
        )
        val brunchMembers = listOf(
            GroupMemberEntity(groupBrunchId, currentUser.id),
            GroupMemberEntity(groupBrunchId, fiona.id),
            GroupMemberEntity(groupBrunchId, bob.id)
        )

        groupDao.insertGroupMembers(homeMembers)
        groupDao.insertGroupMembers(tripMembers)
        groupDao.insertGroupMembers(brunchMembers)

        // 4. Expenses
        val expenseDao = database.expenseDao()

        // Expense 1: Apartment Rent ($1500 paid by Alex, split equally between Alex, Bob, Charlie)
        val exp1Id = "exp_rent"
        val exp1 = ExpenseEntity(
            id = exp1Id,
            description = "Monthly Apartment Rent",
            amount = 1500.0,
            currencySymbol = "$",
            date = now - 18 * dayMs,
            groupId = groupHomeId,
            createdUserId = currentUser.id,
            splitType = SplitType.EQUAL.name,
            category = ExpenseCategory.RENT.name,
            isSettlement = false
        )
        expenseDao.insertExpense(exp1)
        expenseDao.insertExpensePayers(listOf(ExpensePayerEntity(UUID.randomUUID().toString(), exp1Id, currentUser.id, 1500.0)))
        expenseDao.insertExpenseSplits(listOf(
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp1Id, currentUser.id, 500.0, 33.33, 1),
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp1Id, bob.id, 500.0, 33.33, 1),
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp1Id, charlie.id, 500.0, 33.33, 1)
        ))

        // Expense 2: Grocery Shopping ($120 paid by Bob, split equally between Alex, Bob, Charlie)
        val exp2Id = "exp_groceries"
        val exp2 = ExpenseEntity(
            id = exp2Id,
            description = "Trader Joe's Groceries",
            amount = 120.0,
            currencySymbol = "$",
            date = now - 12 * dayMs,
            groupId = groupHomeId,
            createdUserId = bob.id,
            splitType = SplitType.EQUAL.name,
            category = ExpenseCategory.GROCERIES.name,
            isSettlement = false
        )
        expenseDao.insertExpense(exp2)
        expenseDao.insertExpensePayers(listOf(ExpensePayerEntity(UUID.randomUUID().toString(), exp2Id, bob.id, 120.0)))
        expenseDao.insertExpenseSplits(listOf(
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp2Id, currentUser.id, 40.0, 33.33, 1),
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp2Id, bob.id, 40.0, 33.33, 1),
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp2Id, charlie.id, 40.0, 33.33, 1)
        ))

        // Expense 3: Fuel for Road Trip ($90 paid by Alex, split between Alex, Diana, Ethan)
        val exp3Id = "exp_fuel"
        val exp3 = ExpenseEntity(
            id = exp3Id,
            description = "Gas Station & Highway Tolls",
            amount = 90.0,
            currencySymbol = "$",
            date = now - 8 * dayMs,
            groupId = groupTripId,
            createdUserId = currentUser.id,
            splitType = SplitType.EQUAL.name,
            category = ExpenseCategory.TRANSPORTATION.name,
            isSettlement = false
        )
        expenseDao.insertExpense(exp3)
        expenseDao.insertExpensePayers(listOf(ExpensePayerEntity(UUID.randomUUID().toString(), exp3Id, currentUser.id, 90.0)))
        expenseDao.insertExpenseSplits(listOf(
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp3Id, currentUser.id, 30.0, 33.33, 1),
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp3Id, diana.id, 30.0, 33.33, 1),
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp3Id, ethan.id, 30.0, 33.33, 1)
        ))

        // Expense 4: Dinner at Italian Restaurant ($140 paid by Diana, split Alex, Diana, Ethan)
        val exp4Id = "exp_dinner"
        val exp4 = ExpenseEntity(
            id = exp4Id,
            description = "Italian Dinner & Cocktails",
            amount = 140.0,
            currencySymbol = "$",
            date = now - 6 * dayMs,
            groupId = groupTripId,
            createdUserId = diana.id,
            splitType = SplitType.EQUAL.name,
            category = ExpenseCategory.DINING.name,
            isSettlement = false
        )
        expenseDao.insertExpense(exp4)
        expenseDao.insertExpensePayers(listOf(ExpensePayerEntity(UUID.randomUUID().toString(), exp4Id, diana.id, 140.0)))
        expenseDao.insertExpenseSplits(listOf(
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp4Id, currentUser.id, 46.67, 33.33, 1),
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp4Id, diana.id, 46.67, 33.33, 1),
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp4Id, ethan.id, 46.66, 33.33, 1)
        ))

        // Expense 5: Individual expense between Alex and Fiona ($65 paid by Alex for Coffee & Dessert)
        val exp5Id = "exp_fiona_coffee"
        val exp5 = ExpenseEntity(
            id = exp5Id,
            description = "Coffee & Artisan Bakery",
            amount = 65.0,
            currencySymbol = "$",
            date = now - 2 * dayMs,
            groupId = null,
            createdUserId = currentUser.id,
            splitType = SplitType.EQUAL.name,
            category = ExpenseCategory.DINING.name,
            isSettlement = false
        )
        expenseDao.insertExpense(exp5)
        expenseDao.insertExpensePayers(listOf(ExpensePayerEntity(UUID.randomUUID().toString(), exp5Id, currentUser.id, 65.0)))
        expenseDao.insertExpenseSplits(listOf(
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp5Id, currentUser.id, 32.50, 50.0, 1),
            ExpenseSplitEntity(UUID.randomUUID().toString(), exp5Id, fiona.id, 32.50, 50.0, 1)
        ))

        // 5. Activity Logs
        val activityDao = database.activityDao()
        val activities = listOf(
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                type = ActivityType.GROUP_CREATED.name,
                title = "Apartment 4B created",
                subtitle = "Alex Johnson created group 'Apartment 4B'",
                timestamp = now - 20 * dayMs,
                groupId = groupHomeId,
                relatedUserId = currentUser.id,
                expenseId = null
            ),
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                type = ActivityType.EXPENSE_ADDED.name,
                title = "Monthly Apartment Rent added",
                subtitle = "Alex Johnson paid $1,500.00 in Apartment 4B",
                timestamp = now - 18 * dayMs,
                groupId = groupHomeId,
                relatedUserId = currentUser.id,
                expenseId = exp1Id
            ),
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                type = ActivityType.EXPENSE_ADDED.name,
                title = "Trader Joe's Groceries added",
                subtitle = "Bob Smith paid $120.00 in Apartment 4B",
                timestamp = now - 12 * dayMs,
                groupId = groupHomeId,
                relatedUserId = bob.id,
                expenseId = exp2Id
            ),
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                type = ActivityType.GROUP_CREATED.name,
                title = "Road Trip to Vegas created",
                subtitle = "Alex Johnson created group 'Road Trip to Vegas'",
                timestamp = now - 10 * dayMs,
                groupId = groupTripId,
                relatedUserId = currentUser.id,
                expenseId = null
            ),
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                type = ActivityType.EXPENSE_ADDED.name,
                title = "Gas Station & Highway Tolls added",
                subtitle = "Alex Johnson paid $90.00 in Road Trip to Vegas",
                timestamp = now - 8 * dayMs,
                groupId = groupTripId,
                relatedUserId = currentUser.id,
                expenseId = exp3Id
            ),
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                type = ActivityType.EXPENSE_ADDED.name,
                title = "Italian Dinner & Cocktails added",
                subtitle = "Diana Prince paid $140.00 in Road Trip to Vegas",
                timestamp = now - 6 * dayMs,
                groupId = groupTripId,
                relatedUserId = diana.id,
                expenseId = exp4Id
            ),
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                type = ActivityType.EXPENSE_ADDED.name,
                title = "Coffee & Artisan Bakery added",
                subtitle = "Alex Johnson paid $65.00 with Fiona Gallagher",
                timestamp = now - 2 * dayMs,
                groupId = null,
                relatedUserId = fiona.id,
                expenseId = exp5Id
            )
        )

        for (act in activities) {
            activityDao.insertActivity(act)
        }

        // 6. User Settings
        val settingsDao = database.userSettingsDao()
        settingsDao.updateUserSettings(
            UserSettingsEntity(
                id = 1,
                currencySymbol = "$",
                isDarkMode = false,
                notificationsEnabled = true
            )
        )
    }
}
