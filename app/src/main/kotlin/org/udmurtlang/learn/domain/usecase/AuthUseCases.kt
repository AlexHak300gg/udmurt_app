package org.udmurtlang.learn.domain.usecase

import org.udmurtlang.learn.domain.model.Profile
import org.udmurtlang.learn.domain.repository.AccountRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val account: AccountRepository,
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<Profile> =
        account.register(email, password, name)
}

class LoginUseCase @Inject constructor(
    private val account: AccountRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<Profile> =
        account.login(email, password)
}

class ContinueAsGuestUseCase @Inject constructor(
    private val account: AccountRepository,
) {
    suspend operator fun invoke(): Profile = account.continueAsGuest()
}

class LogoutUseCase @Inject constructor(
    private val account: AccountRepository,
) {
    suspend operator fun invoke() = account.logout()
}

class ChangePasswordUseCase @Inject constructor(
    private val account: AccountRepository,
) {
    suspend operator fun invoke(old: String, new: String) = account.changePassword(old, new)
}

class UpdateProfileUseCase @Inject constructor(
    private val account: AccountRepository,
) {
    suspend fun updateName(name: String) = account.updateName(name)
    suspend fun updateAvatar(avatar: String) = account.updateAvatar(avatar)
}
