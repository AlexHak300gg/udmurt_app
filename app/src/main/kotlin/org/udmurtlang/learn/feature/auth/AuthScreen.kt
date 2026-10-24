package org.udmurtlang.learn.feature.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.StarPattern
import org.udmurtlang.learn.core.designsystem.ZcInkPill
import org.udmurtlang.learn.core.designsystem.ZcRedBottom
import org.udmurtlang.learn.core.designsystem.ZcRedTop
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.GhostButton

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val forgotCloudText = stringResource(R.string.auth_forgot_cloud)
    val forgotLocalText = stringResource(R.string.auth_forgot_local)
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthEffect.Authenticated -> onAuthenticated()
                is AuthEffect.ShowMessage -> {
                    val text = when (effect.text) {
                        "auth_forgot_cloud" -> forgotCloudText
                        else -> forgotLocalText
                    }
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Красная шапка с орнаментом
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(Brush.verticalGradient(listOf(ZcRedTop, ZcRedBottom))),
        ) {
            StarPattern(
                modifier = Modifier.matchParentSize(),
                color = ZcWhite,
                alpha = 0.10f,
                cell = 56.dp,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
            ) {
                Text(
                    text = stringResource(R.string.auth_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = ZcWhite,
                )
                Text(
                    text = stringResource(R.string.auth_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZcWhite.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Сегментация Вход / Регистрация
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShapeCompat)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                AuthTab(
                    label = stringResource(R.string.auth_tab_login),
                    selected = !state.isRegisterMode,
                    onClick = { if (state.isRegisterMode) viewModel.onIntent(AuthIntent.ToggleMode) },
                    modifier = Modifier.weight(1f),
                )
                AuthTab(
                    label = stringResource(R.string.auth_tab_register),
                    selected = state.isRegisterMode,
                    onClick = { if (!state.isRegisterMode) viewModel.onIntent(AuthIntent.ToggleMode) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (state.isRegisterMode) {
                AuthField(
                    value = state.name,
                    onValueChange = { viewModel.onIntent(AuthIntent.NameChanged(it)) },
                    placeholder = stringResource(R.string.auth_name),
                    icon = Icons.Filled.Person,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            val emailInvalid = state.error == "auth_error_email"
            AuthField(
                value = state.email,
                onValueChange = { viewModel.onIntent(AuthIntent.EmailChanged(it)) },
                placeholder = stringResource(R.string.auth_email),
                icon = Icons.Filled.Mail,
                error = emailInvalid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(modifier = Modifier.height(12.dp))
            AuthField(
                value = state.password,
                onValueChange = { viewModel.onIntent(AuthIntent.PasswordChanged(it)) },
                placeholder = stringResource(R.string.auth_password),
                icon = Icons.Filled.Key,
                error = state.error == "auth_error_password",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailing = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
            )

            state.error?.let { key ->
                Text(
                    text = when (key) {
                        "auth_error_email" -> stringResource(R.string.auth_error_email)
                        "auth_error_password" -> stringResource(R.string.auth_error_password)
                        "auth_error_name" -> stringResource(R.string.auth_error_name)
                        else -> key
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            GameButton(
                text = stringResource(if (state.isRegisterMode) R.string.auth_register else R.string.auth_login),
                onClick = { viewModel.onIntent(AuthIntent.Submit) },
                enabled = !state.isLoading,
                containerColor = ZcInkPill,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth(),
            )
            GhostButton(
                text = stringResource(R.string.auth_guest),
                onClick = { viewModel.onIntent(AuthIntent.ContinueAsGuest) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            )
            TextButton(
                onClick = { viewModel.onIntent(AuthIntent.ToggleMode) },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = stringResource(
                        if (state.isRegisterMode) R.string.auth_switch_to_login
                        else R.string.auth_switch_to_register,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            TextButton(onClick = { viewModel.onIntent(AuthIntent.ForgotPassword) }) {
                Text(stringResource(R.string.auth_forgot), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private val CircleShapeCompat = RoundedCornerShape(999.dp)

@Composable
private fun AuthTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) ZcInkPill else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
        )
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    error: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailing: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        enabled = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        isError = error,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = trailing,
        label = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
    )
}
