package com.app.splitter.data.repository

import com.app.splitter.data.local.AppDatabase
import com.app.splitter.data.local.DataInitializer
import com.app.splitter.data.local.entity.ActivityLogEntity
import com.app.splitter.data.local.entity.ExpenseEntity
import com.app.splitter.data.local.entity.ExpensePayerEntity
import com.app.splitter.data.local.entity.ExpenseSplitEntity
import com.app.splitter.data.local.entity.FriendEntity
import com.app.splitter.data.local.entity.GroupEntity
import com.app.splitter.data.local.entity.GroupMemberEntity
import com.app.splitter.data.local.entity.UserEntity
import com.app.splitter.data.local.entity.UserSettingsEntity
import com.app.splitter.domain.model.ActivityLog
import com.app.splitter.domain.model.ActivityType
import com.app.splitter.domain.model.BalanceSummary
import com.app.splitter.domain.model.DebtPair
import com.app.splitter.domain.model.Expense
import com.app.splitter.domain.model.ExpenseCategory
import com.app.splitter.domain.model.ExpensePayer
import com.app.splitter.domain.model.ExpenseSplit
import com.app.splitter.domain.model.FriendBalanceSummary
import com.app.splitter.domain.model.Group
import com.app.splitter.domain.model.GroupBalanceSummary
import com.app.splitter.domain.model.GroupCategory
import com.app.splitter.domain.model.SplitType
import com.app.splitter.domain.model.User
import com.app.splitter.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface SplitterRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun getCurrentUserOneShot(): User?
    fun getAllUsers(): Flow<List<User>>
    fun getFriends(): Flow<List<User>>
    suspend fun addFriend(name: String, email: String, phone: String): User

    fun getGroups(): Flow<List<Group>>
    suspend fun getGroupById(groupId: String): Group?
    fun getGroupMembers(groupId: String): Flow<List<User>>
    suspend fun createGroup(name: String, category: GroupCategory, memberIds: List<String>): Group

    fun getAllExpenses(): Flow<List<Expense>>
    fun getGroupExpenses(groupId: String): Flow<List<Expense>>
    fun getFriendExpenses(friendId: String): Flow<List<Expense>>
    suspend fun getExpenseById(expenseId: String): Expense?

    suspend fun addExpense(
        description: String,
        amount: Double,
        currencySymbol: String,
        groupId: String?,
        createdUserId: String,
        splitType: SplitType,
        category: ExpenseCategory,
        payers: List<ExpensePayer>,
        splits: List<ExpenseSplit>,
        isSettlement: Boolean = false
    ): Expense

    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expenseId: String)

    suspend fun settleUp(
        payerId: String,
        payeeId: String,
        amount: Double,
        groupId: String?,
        currencySymbol: String
    ): Expense

    fun getActivities(): Flow<List<ActivityLog>>
    suspend fun addActivityLog(type: ActivityType, title: String, subtitle: String, groupId: String?, relatedUserId: String?, expenseId: String?)

    fun getUserSettings(): Flow<UserSettings>
    suspend fun updateUserSettings(settings: UserSettings)

    fun getGlobalBalanceSummary(): Flow<BalanceSummary>
    fun getGroupBalanceSummary(groupId: String): Flow<GroupBalanceSummary?>
    fun getFriendBalanceSummary(friendId: String): Flow<FriendBalanceSummary?>

    suspend fun switchCurrentUser(userId: String)
    suspend fun resetData()
}

@Singleton
class SplitterRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dataInitializer: DataInitializer
) : SplitterRepository {

    private val userDao = database.userDao()
    private val groupDao = database.groupDao()
    private val expenseDao = database.expenseDao()
    private val activityDao = database.activityDao()
    private val settingsDao = database.userSettingsDao()

    init {
        // We will trigger initialization from Application or ViewModel, or suspend call.
    }

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getCurrentUser().map { it?.toDomain() }
    }

    override suspend fun getCurrentUserOneShot(): User? {
        dataInitializer.populateInitialDataIfEmpty()
        return userDao.getCurrentUserOneShot()?.toDomain()
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { list -> list.map { it.toDomain() } }
    }

    override fun getFriends(): Flow<List<User>> {
        return userDao.getFriends().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addFriend(name: String, email: String, phone: String): User {
        val id = "user_${UUID.randomUUID()}"
        val userEntity = UserEntity(
            id = id,
            name = name,
            email = email,
            phone = phone,
            avatarUrl = "",
            isCurrentUser = false
        )
        userDao.insertUser(userEntity)
        userDao.insertFriend(FriendEntity(id, System.currentTimeMillis()))

        val me = getCurrentUserOneShot()
        addActivityLog(
            type = ActivityType.FRIEND_ADDED,
            title = "Friend added",
            subtitle = "${me?.name ?: "You"} added $name as a friend",
            groupId = null,
            relatedUserId = id,
            expenseId = null
        )
        return userEntity.toDomain()
    }

    override fun getGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups().map { list ->
            list.map { groupEntity ->
                val members = groupDao.getGroupMembersList(groupEntity.id).map { it.id }
                groupEntity.toDomain(members)
            }
        }
    }

    override suspend fun getGroupById(groupId: String): Group? {
        val entity = groupDao.getGroupById(groupId) ?: return null
        val memberIds = groupDao.getGroupMembersList(groupId).map { it.id }
        return entity.toDomain(memberIds)
    }

    override fun getGroupMembers(groupId: String): Flow<List<User>> {
        return groupDao.getGroupMembers(groupId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun createGroup(
        name: String,
        category: GroupCategory,
        memberIds: List<String>
    ): Group {
        dataInitializer.populateInitialDataIfEmpty()
        val currentUser = userDao.getCurrentUserOneShot() ?: throw IllegalStateException("Current user missing")
        val groupId = "group_${UUID.randomUUID()}"
        val now = System.currentTimeMillis()

        val groupEntity = GroupEntity(
            id = groupId,
            name = name,
            category = category.name,
            createdDate = now,
            createdByUserId = currentUser.id
        )
        groupDao.insertGroup(groupEntity)

        val allMembers = (memberIds + currentUser.id).distinct()
        val memberEntities = allMembers.map { userId ->
            GroupMemberEntity(groupId, userId)
        }
        groupDao.insertGroupMembers(memberEntities)

        addActivityLog(
            type = ActivityType.GROUP_CREATED,
            title = "Group created",
            subtitle = "${currentUser.name} created group '$name'",
            groupId = groupId,
            relatedUserId = currentUser.id,
            expenseId = null
        )

        return groupEntity.toDomain(allMembers)
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.map { mapExpenseEntityToDomain(it) }
        }
    }

    override fun getGroupExpenses(groupId: String): Flow<List<Expense>> {
        return expenseDao.getGroupExpenses(groupId).map { entities ->
            entities.map { mapExpenseEntityToDomain(it) }
        }
    }

    override fun getFriendExpenses(friendId: String): Flow<List<Expense>> {
        return combine(
            userDao.getCurrentUser(),
            expenseDao.getAllExpenses()
        ) { meEntity, allExpenses ->
            val meId = meEntity?.id ?: "user_me"
            allExpenses.filter { exp ->
                val payers = expenseDao.getExpensePayers(exp.id).map { it.userId }
                val splits = expenseDao.getExpenseSplits(exp.id).map { it.userId }
                val participants = (payers + splits).toSet()
                // Friend expense is either explicitly non-group expense involving both, or any shared expense involving both
                participants.contains(meId) && participants.contains(friendId)
            }.map { mapExpenseEntityToDomain(it) }
        }
    }

    override suspend fun getExpenseById(expenseId: String): Expense? {
        val entity = expenseDao.getExpenseById(expenseId) ?: return null
        return mapExpenseEntityToDomain(entity)
    }

    private suspend fun mapExpenseEntityToDomain(entity: ExpenseEntity): Expense {
        val payers = expenseDao.getExpensePayers(entity.id).map {
            ExpensePayer(it.userId, it.amountPaid)
        }
        val splits = expenseDao.getExpenseSplits(entity.id).map {
            ExpenseSplit(it.userId, it.amountOwed, it.percentage, it.shares)
        }
        return Expense(
            id = entity.id,
            description = entity.description,
            amount = entity.amount,
            currencySymbol = entity.currencySymbol,
            date = entity.date,
            groupId = entity.groupId,
            createdUserId = entity.createdUserId,
            splitType = try { SplitType.valueOf(entity.splitType) } catch (e: Exception) { SplitType.EQUAL },
            category = try { ExpenseCategory.valueOf(entity.category) } catch (e: Exception) { ExpenseCategory.GENERAL },
            isSettlement = entity.isSettlement,
            payers = payers,
            splits = splits
        )
    }

    override suspend fun addExpense(
        description: String,
        amount: Double,
        currencySymbol: String,
        groupId: String?,
        createdUserId: String,
        splitType: SplitType,
        category: ExpenseCategory,
        payers: List<ExpensePayer>,
        splits: List<ExpenseSplit>,
        isSettlement: Boolean
    ): Expense {
        val expenseId = "exp_${UUID.randomUUID()}"
        val now = System.currentTimeMillis()

        val entity = ExpenseEntity(
            id = expenseId,
            description = description,
            amount = amount,
            currencySymbol = currencySymbol,
            date = now,
            groupId = groupId,
            createdUserId = createdUserId,
            splitType = splitType.name,
            category = category.name,
            isSettlement = isSettlement
        )

        expenseDao.insertExpense(entity)

        val payerEntities = payers.map {
            ExpensePayerEntity(UUID.randomUUID().toString(), expenseId, it.userId, it.amountPaid)
        }
        val splitEntities = splits.map {
            ExpenseSplitEntity(UUID.randomUUID().toString(), expenseId, it.userId, it.amountOwed, it.percentage, it.shares)
        }
        expenseDao.insertExpensePayers(payerEntities)
        expenseDao.insertExpenseSplits(splitEntities)

        val creator = userDao.getUserById(createdUserId)
        val creatorName = creator?.name ?: "Someone"
        val groupName = groupId?.let { groupDao.getGroupById(it)?.name }

        val subtitle = if (groupName != null) {
            "$creatorName paid $currencySymbol${String.format("%.2f", amount)} in $groupName"
        } else {
            "$creatorName paid $currencySymbol${String.format("%.2f", amount)}"
        }

        val logType = if (isSettlement) ActivityType.SETTLED_UP else ActivityType.EXPENSE_ADDED
        addActivityLog(
            type = logType,
            title = if (isSettlement) "Settlement: $description" else "$description added",
            subtitle = subtitle,
            groupId = groupId,
            relatedUserId = createdUserId,
            expenseId = expenseId
        )

        return Expense(
            id = expenseId,
            description = description,
            amount = amount,
            currencySymbol = currencySymbol,
            date = now,
            groupId = groupId,
            createdUserId = createdUserId,
            splitType = splitType,
            category = category,
            isSettlement = isSettlement,
            payers = payers,
            splits = splits
        )
    }

    override suspend fun updateExpense(expense: Expense) {
        val entity = ExpenseEntity(
            id = expense.id,
            description = expense.description,
            amount = expense.amount,
            currencySymbol = expense.currencySymbol,
            date = expense.date,
            groupId = expense.groupId,
            createdUserId = expense.createdUserId,
            splitType = expense.splitType.name,
            category = expense.category.name,
            isSettlement = expense.isSettlement
        )
        expenseDao.insertExpense(entity)

        expenseDao.deleteExpensePayers(expense.id)
        expenseDao.deleteExpenseSplits(expense.id)

        val payerEntities = expense.payers.map {
            ExpensePayerEntity(UUID.randomUUID().toString(), expense.id, it.userId, it.amountPaid)
        }
        val splitEntities = expense.splits.map {
            ExpenseSplitEntity(UUID.randomUUID().toString(), expense.id, it.userId, it.amountOwed, it.percentage, it.shares)
        }
        expenseDao.insertExpensePayers(payerEntities)
        expenseDao.insertExpenseSplits(splitEntities)

        addActivityLog(
            type = ActivityType.EXPENSE_EDITED,
            title = "Expense updated",
            subtitle = "Updated '${expense.description}'",
            groupId = expense.groupId,
            relatedUserId = expense.createdUserId,
            expenseId = expense.id
        )
    }

    override suspend fun deleteExpense(expenseId: String) {
        val expense = getExpenseById(expenseId) ?: return
        expenseDao.deleteExpenseComplete(expenseId)

        addActivityLog(
            type = ActivityType.EXPENSE_DELETED,
            title = "Expense deleted",
            subtitle = "Deleted '${expense.description}'",
            groupId = expense.groupId,
            relatedUserId = expense.createdUserId,
            expenseId = null
        )
    }

    override suspend fun settleUp(
        payerId: String,
        payeeId: String,
        amount: Double,
        groupId: String?,
        currencySymbol: String
    ): Expense {
        val payerUser = userDao.getUserById(payerId)
        val payeeUser = userDao.getUserById(payeeId)
        val payerName = payerUser?.name ?: "Payer"
        val payeeName = payeeUser?.name ?: "Payee"

        val description = "$payerName paid $payeeName"

        val payers = listOf(ExpensePayer(userId = payerId, amountPaid = amount))
        val splits = listOf(ExpenseSplit(userId = payeeId, amountOwed = amount))

        return addExpense(
            description = description,
            amount = amount,
            currencySymbol = currencySymbol,
            groupId = groupId,
            createdUserId = payerId,
            splitType = SplitType.EQUAL,
            category = ExpenseCategory.GENERAL,
            payers = payers,
            splits = splits,
            isSettlement = true
        )
    }

    override fun getActivities(): Flow<List<ActivityLog>> {
        return activityDao.getAllActivities().map { list ->
            list.map { entity ->
                ActivityLog(
                    id = entity.id,
                    type = try { ActivityType.valueOf(entity.type) } catch (e: Exception) { ActivityType.EXPENSE_ADDED },
                    title = entity.title,
                    subtitle = entity.subtitle,
                    timestamp = entity.timestamp,
                    groupId = entity.groupId,
                    relatedUserId = entity.relatedUserId,
                    expenseId = entity.expenseId
                )
            }
        }
    }

    override suspend fun addActivityLog(
        type: ActivityType,
        title: String,
        subtitle: String,
        groupId: String?,
        relatedUserId: String?,
        expenseId: String?
    ) {
        activityDao.insertActivity(
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                type = type.name,
                title = title,
                subtitle = subtitle,
                timestamp = System.currentTimeMillis(),
                groupId = groupId,
                relatedUserId = relatedUserId,
                expenseId = expenseId
            )
        )
    }

    override fun getUserSettings(): Flow<UserSettings> {
        return settingsDao.getUserSettings().map { entity ->
            if (entity == null) {
                UserSettings()
            } else {
                UserSettings(
                    id = entity.id,
                    currencySymbol = entity.currencySymbol,
                    isDarkMode = entity.isDarkMode,
                    notificationsEnabled = entity.notificationsEnabled
                )
            }
        }
    }

    override suspend fun updateUserSettings(settings: UserSettings) {
        settingsDao.updateUserSettings(
            UserSettingsEntity(
                id = 1,
                currencySymbol = settings.currencySymbol,
                isDarkMode = settings.isDarkMode,
                notificationsEnabled = settings.notificationsEnabled
            )
        )
    }

    override fun getGlobalBalanceSummary(): Flow<BalanceSummary> {
        return combine(
            userDao.getCurrentUser(),
            getAllExpenses()
        ) { currentUserEntity, expenses ->
            val meId = currentUserEntity?.id ?: "user_me"
            var youOweTotal = 0.0
            var youAreOwedTotal = 0.0

            for (expense in expenses) {
                val paidByMe = expense.payers.firstOrNull { it.userId == meId }?.amountPaid ?: 0.0
                val owedByMe = expense.splits.firstOrNull { it.userId == meId }?.amountOwed ?: 0.0
                val net = paidByMe - owedByMe
                if (net > 0) {
                    youAreOwedTotal += net
                } else if (net < 0) {
                    youOweTotal += (-net)
                }
            }

            BalanceSummary(
                totalYouOwe = youOweTotal,
                totalYouAreOwed = youAreOwedTotal,
                netBalance = youAreOwedTotal - youOweTotal
            )
        }
    }

    override fun getGroupBalanceSummary(groupId: String): Flow<GroupBalanceSummary?> {
        return combine(
            flow { emit(groupDao.getGroupById(groupId)) },
            groupDao.getGroupMembers(groupId),
            getGroupExpenses(groupId),
            userDao.getAllUsers()
        ) { groupEntity, membersEntities, groupExpenses, allUsersEntities ->
            if (groupEntity == null) return@combine null

            val members = membersEntities.map { it.toDomain() }
            val usersMap = allUsersEntities.map { it.toDomain() }.associateBy { it.id }
            val meId = userDao.getCurrentUserOneShot()?.id ?: "user_me"

            val groupObj = groupEntity.toDomain(members.map { it.id })
            val totalGroupSpend = groupExpenses.sumOf { it.amount }

            // Calculate net balance for each member in group
            val netBalances = mutableMapOf<String, Double>()
            members.forEach { netBalances[it.id] = 0.0 }

            for (exp in groupExpenses) {
                for (payer in exp.payers) {
                    netBalances[payer.userId] = (netBalances[payer.userId] ?: 0.0) + payer.amountPaid
                }
                for (split in exp.splits) {
                    netBalances[split.userId] = (netBalances[split.userId] ?: 0.0) - split.amountOwed
                }
            }

            val userNetBalance = netBalances[meId] ?: 0.0
            val debtPairs = com.app.splitter.domain.usecase.DebtSimplifier.simplifyDebts(netBalances, usersMap)

            GroupBalanceSummary(
                group = groupObj,
                members = members,
                totalSpend = totalGroupSpend,
                userNetBalance = userNetBalance,
                debtPairs = debtPairs
            )
        }
    }

    override fun getFriendBalanceSummary(friendId: String): Flow<FriendBalanceSummary?> {
        return combine(
            userDao.getAllUsers(),
            getFriendExpenses(friendId),
            userDao.getCurrentUser()
        ) { allUsersEntities, sharedExpenses, currentUserEntity ->
            val meId = currentUserEntity?.id ?: "user_me"
            val allUsers = allUsersEntities.map { it.toDomain() }
            val friendUser = allUsers.firstOrNull { it.id == friendId } ?: return@combine null

            val usersMap = allUsers.associateBy { it.id }

            val netBalances = mutableMapOf(
                meId to 0.0,
                friendId to 0.0
            )

            for (exp in sharedExpenses) {
                for (payer in exp.payers) {
                    if (netBalances.containsKey(payer.userId)) {
                        netBalances[payer.userId] = (netBalances[payer.userId] ?: 0.0) + payer.amountPaid
                    }
                }
                for (split in exp.splits) {
                    if (netBalances.containsKey(split.userId)) {
                        netBalances[split.userId] = (netBalances[split.userId] ?: 0.0) - split.amountOwed
                    }
                }
            }

            val meNet = netBalances[meId] ?: 0.0
            val debtPairs = com.app.splitter.domain.usecase.DebtSimplifier.simplifyDebts(netBalances, usersMap)

            FriendBalanceSummary(
                friend = friendUser,
                netBalance = meNet,
                debtPairs = debtPairs
            )
        }
    }

    override suspend fun switchCurrentUser(userId: String) {
        userDao.setCurrentUser(userId)
    }

    override suspend fun resetData() {
        database.clearAllTables()
        dataInitializer.populateInitialDataIfEmpty()
    }

    private fun UserEntity.toDomain() = User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        avatarUrl = avatarUrl,
        isCurrentUser = isCurrentUser
    )

    private fun GroupEntity.toDomain(memberIds: List<String>) = Group(
        id = id,
        name = name,
        category = try { GroupCategory.valueOf(category) } catch (e: Exception) { GroupCategory.OTHER },
        createdDate = createdDate,
        createdByUserId = createdByUserId,
        memberIds = memberIds
    )
}
